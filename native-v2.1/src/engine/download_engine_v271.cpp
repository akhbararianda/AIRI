#ifdef _WIN32
#include "engine/download_engine.h"
#include "common/airi_text.h"
#include <windows.h>
#include <winhttp.h>
#include <algorithm>
#include <chrono>
#include <filesystem>
#include <mutex>
#include <string>
#include <thread>
#include <utility>
#include <vector>
#pragma comment(lib,"winhttp.lib")

namespace airi { namespace {
std::wstring W(const std::string&s){if(s.empty())return{};int n=MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0);std::wstring o((std::size_t)n,L'\0');MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n);return o;}
std::string U8(const std::wstring&s){if(s.empty())return{};int n=WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0,nullptr,nullptr);std::string o((std::size_t)n,'\0');WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n,nullptr,nullptr);return o;}
struct Url{std::wstring host,path;INTERNET_PORT port{};bool https{};};
bool parse(const std::string&u,Url&o){auto w=W(u);URL_COMPONENTS c{sizeof(c)};c.dwHostNameLength=(DWORD)-1;c.dwUrlPathLength=(DWORD)-1;c.dwExtraInfoLength=(DWORD)-1;if(!WinHttpCrackUrl(w.c_str(),0,0,&c))return false;o.host.assign(c.lpszHostName,c.dwHostNameLength);o.path.assign(c.lpszUrlPath,c.dwUrlPathLength);if(c.dwExtraInfoLength)o.path.append(c.lpszExtraInfo,c.dwExtraInfoLength);if(o.path.empty())o.path=L"/";o.port=c.nPort;o.https=c.nScheme==INTERNET_SCHEME_HTTPS;return true;}
struct Handles{HINTERNET session{},connect{},request{};~Handles(){if(request)WinHttpCloseHandle(request);if(connect)WinHttpCloseHandle(connect);if(session)WinHttpCloseHandle(session);}};

bool open_req(const DownloadRequest&r,const wchar_t*verb,Handles&h,const std::wstring&range=L""){
    Url u;if(!parse(r.url,u))return false;
    h.session=WinHttpOpen(L"AIRI Download Manager/2.7.1 LargeFile",WINHTTP_ACCESS_TYPE_AUTOMATIC_PROXY,WINHTTP_NO_PROXY_NAME,WINHTTP_NO_PROXY_BYPASS,0);
    if(!h.session)return false;
    WinHttpSetTimeouts(h.session,30000,45000,60000,120000);
    DWORD maxConn=(DWORD)std::clamp(r.connections,1,64);WinHttpSetOption(h.session,WINHTTP_OPTION_MAX_CONNS_PER_SERVER,&maxConn,sizeof(maxConn));
    h.connect=WinHttpConnect(h.session,u.host.c_str(),u.port,0);if(!h.connect)return false;
    DWORD flags=u.https?WINHTTP_FLAG_SECURE:0;
    h.request=WinHttpOpenRequest(h.connect,verb,u.path.c_str(),nullptr,WINHTTP_NO_REFERER,WINHTTP_DEFAULT_ACCEPT_TYPES,flags);if(!h.request)return false;
    std::wstring hdr=L"Accept: */*\r\nAccept-Encoding: identity\r\nConnection: keep-alive\r\n";
    if(!r.cookie.empty())hdr+=L"Cookie: "+W(r.cookie)+L"\r\n";
    if(!r.referer.empty())hdr+=L"Referer: "+W(r.referer)+L"\r\n";
    if(!r.origin.empty())hdr+=L"Origin: "+W(r.origin)+L"\r\n";
    if(!range.empty())hdr+=L"Range: bytes="+range+L"\r\n";
    if(!r.user_agent.empty())WinHttpAddRequestHeaders(h.request,(L"User-Agent: "+W(r.user_agent)).c_str(),-1L,WINHTTP_ADDREQ_FLAG_REPLACE|WINHTTP_ADDREQ_FLAG_ADD);
    WinHttpAddRequestHeaders(h.request,hdr.c_str(),-1L,WINHTTP_ADDREQ_FLAG_ADD);
    return WinHttpSendRequest(h.request,WINHTTP_NO_ADDITIONAL_HEADERS,0,WINHTTP_NO_REQUEST_DATA,0,0,0)&&WinHttpReceiveResponse(h.request,nullptr);
}
DWORD status(HINTERNET q){DWORD v=0,n=sizeof(v);WinHttpQueryHeaders(q,WINHTTP_QUERY_STATUS_CODE|WINHTTP_QUERY_FLAG_NUMBER,nullptr,&v,&n,nullptr);return v;}
std::uint64_t content_length(HINTERNET q){wchar_t b[128]{};DWORD n=sizeof(b);if(!WinHttpQueryHeaders(q,WINHTTP_QUERY_CONTENT_LENGTH,nullptr,b,&n,nullptr))return 0;try{return std::stoull(b);}catch(...){return 0;}}
bool content_range(HINTERNET q,std::uint64_t&begin,std::uint64_t&end,std::uint64_t&total){wchar_t b[256]{};DWORD n=sizeof(b);if(!WinHttpQueryHeaders(q,WINHTTP_QUERY_CONTENT_RANGE,nullptr,b,&n,nullptr))return false;std::wstring v=b;auto sp=v.find(L' '),dash=v.find(L'-'),slash=v.find(L'/');if(dash==std::wstring::npos||slash==std::wstring::npos)return false;try{begin=std::stoull(v.substr(sp==std::wstring::npos?0:sp+1,dash-(sp==std::wstring::npos?0:sp+1)));end=std::stoull(v.substr(dash+1,slash-dash-1));total=std::stoull(v.substr(slash+1));return true;}catch(...){return false;}}
std::wstring disposition_name(HINTERNET q){wchar_t b[2048]{};DWORD n=sizeof(b);if(!WinHttpQueryHeaders(q,WINHTTP_QUERY_CONTENT_DISPOSITION,nullptr,b,&n,nullptr))return{};std::wstring s=b;auto p=s.find(L"filename=");if(p==std::wstring::npos)return{};p+=9;if(p<s.size()&&s[p]=='\"'){auto e=s.find(L'\"',++p);return s.substr(p,e-p);}auto e=s.find(L';',p);return s.substr(p,e==std::wstring::npos?s.size()-p:e-p);}
std::uint64_t clamp_chunk(std::uint64_t bytes_per_sec){constexpr std::uint64_t MINC=1024ull*1024,MAXC=16ull*1024*1024;if(!bytes_per_sec)return 4ull*1024*1024;auto target=bytes_per_sec/2;return std::clamp<std::uint64_t>(target,MINC,MAXC);}
void retry_sleep(int attempt){DWORD ms=(DWORD)std::min(16000,750*(1<<std::min(attempt,4)));Sleep(ms);}
bool has_disk_space(const std::filesystem::path&p,std::uint64_t need){ULARGE_INTEGER freeBytes{};auto root=p.root_path();if(root.empty())root=p.parent_path();if(root.empty())return true;if(!GetDiskFreeSpaceExW(root.c_str(),&freeBytes,nullptr,nullptr))return true;return freeBytes.QuadPart>need+64ull*1024*1024;}
struct MissingRange{std::uint64_t begin{},end{};};
}

const char* status_name(DownloadStatus x){switch(x){case DownloadStatus::Queued:return"Queued";case DownloadStatus::Probing:return"Probing";case DownloadStatus::Downloading:return"Downloading";case DownloadStatus::Paused:return"Paused";case DownloadStatus::Completed:return"Completed";case DownloadStatus::Failed:return"Failed";case DownloadStatus::Cancelled:return"Cancelled";}return"Unknown";}
DownloadTask::DownloadTask(std::uint64_t id,DownloadRequest r,Callback cb):id_(id),req_(std::move(r)),cb_(std::move(cb)){s_.id=id_;s_.url=req_.url;s_.filename=req_.filename;}
DownloadTask::~DownloadTask(){cancelled_=true;paused_=false;if(th_.joinable())th_.join();}
void DownloadTask::start(){th_=std::thread([this]{run();});}
void DownloadTask::pause(){paused_=true;{std::lock_guard lk(m_);s_.status=DownloadStatus::Paused;}emit();}
void DownloadTask::resume(){paused_=false;{std::lock_guard lk(m_);if(s_.status==DownloadStatus::Paused)s_.status=DownloadStatus::Downloading;}emit();}
void DownloadTask::cancel(){cancelled_=true;paused_=false;{std::lock_guard lk(m_);s_.status=DownloadStatus::Cancelled;}emit();}
DownloadSnapshot DownloadTask::snapshot()const{std::lock_guard lk(m_);auto x=s_;x.downloaded_bytes=downloaded_;if(x.total_bytes)x.progress_percent=(int)std::min<std::uint64_t>(100,downloaded_*100/x.total_bytes);return x;}
void DownloadTask::emit(){if(cb_)cb_(snapshot());}
void DownloadTask::fail(std::string e){{std::lock_guard lk(m_);s_.status=DownloadStatus::Failed;s_.error=std::move(e);s_.bytes_per_second=0;}emit();}
void DownloadTask::mark_finished(){}
void DownloadTask::run(){{std::lock_guard lk(m_);s_.status=DownloadStatus::Probing;}emit();if(!probe())return;if(cancelled_)return;{std::lock_guard lk(m_);s_.status=DownloadStatus::Downloading;}speed_last_time_=std::chrono::steady_clock::now();speed_last_bytes_=downloaded_.load();emit();bool ok=ranges_&&s_.total_bytes>chunk_?segmented():sequential();if(ok&&!cancelled_){{std::lock_guard lk(m_);s_.status=DownloadStatus::Completed;s_.progress_percent=100;s_.bytes_per_second=0;s_.error.clear();}emit();}}

bool DownloadTask::probe(){Handles h;if(!open_req(req_,L"GET",h,L"0-0")){fail("Connection/probe failed");return false;}auto st=status(h.request);ranges_=st==206;std::uint64_t total=0;if(st==206){std::uint64_t rb=0,re=0,rt=0;if(content_range(h.request,rb,re,rt))total=rt;}if(!total)total=content_length(h.request);if(st>=400||!total){fail("Server rejected request or size unavailable (HTTP "+std::to_string(st)+")");return false;}std::string fn=req_.filename;if(fn.empty()){auto d=disposition_name(h.request);fn=d.empty()?text::filename_from_url_utf8(req_.url):U8(d);}fn=text::sanitize_filename_utf8(fn);auto out=req_.save_directory/fn;std::filesystem::create_directories(req_.save_directory);for(int i=1;std::filesystem::exists(out);++i){auto stem=out.stem().wstring(),ext=out.extension().wstring();out=req_.save_directory/(stem+L" ("+std::to_wstring(i)+L")"+ext);}if(!has_disk_space(out,total)){fail("Not enough free disk space for this download");return false;}{std::lock_guard lk(m_);s_.filename=fn;s_.total_bytes=total;s_.output_path=out;}return true;}

void DownloadTask::update_speed(){auto now=std::chrono::steady_clock::now();auto ms=std::chrono::duration_cast<std::chrono::milliseconds>(now-speed_last_time_).count();if(ms<350)return;auto cur=downloaded_.load();double bps=(cur>=speed_last_bytes_?(cur-speed_last_bytes_):0)*1000.0/ms;{std::lock_guard lk(m_);s_.bytes_per_second=s_.bytes_per_second<=0?bps:(s_.bytes_per_second*0.72+bps*0.28);}speed_last_bytes_=cur;speed_last_time_=now;emit();}

bool DownloadTask::segmented(){
    auto snap=snapshot();
    HANDLE init=CreateFileW(snap.output_path.c_str(),GENERIC_WRITE|GENERIC_READ,FILE_SHARE_READ|FILE_SHARE_WRITE,nullptr,CREATE_ALWAYS,FILE_ATTRIBUTE_NORMAL,nullptr);
    if(init==INVALID_HANDLE_VALUE){fail("Cannot create output file");return false;}
    LARGE_INTEGER sz{};sz.QuadPart=(LONGLONG)snap.total_bytes;
    if(!SetFilePointerEx(init,sz,nullptr,FILE_BEGIN)||!SetEndOfFile(init)){CloseHandle(init);fail("Cannot allocate output file; check disk space/filesystem");return false;}
    CloseHandle(init);

    std::atomic<std::uint64_t> nextByte{0};
    int workers=std::clamp(req_.connections,1,64);
    if(snap.total_bytes>=4ull*1024*1024*1024)workers=std::min(workers,16);
    std::atomic<int> exhausted{0};
    std::atomic<bool> stopAssign{false};
    std::mutex missingMtx;
    std::vector<MissingRange> missing;

    auto transfer_range=[&](HANDLE f,std::uint64_t begin,std::uint64_t end,int maxAttempts,std::uint64_t&resumeOff)->bool{
        std::vector<char>buf(512*1024);std::uint64_t off=begin;int attempt=0;
        while(off<=end&&!cancelled_){while(paused_&&!cancelled_)Sleep(40);if(cancelled_)break;
            Handles h;bool requestOk=open_req(req_,L"GET",h,std::to_wstring(off)+L"-"+std::to_wstring(end));
            DWORD st=requestOk?status(h.request):0;std::uint64_t rb=0,re=0,rt=0;bool cr=requestOk&&content_range(h.request,rb,re,rt);
            if(!requestOk||st!=206||!cr||rb!=off||rt!=snap.total_bytes){if(++attempt>=maxAttempts){resumeOff=off;return false;}retry_sleep(attempt);continue;}
            bool readFailed=false;
            while(off<=end&&!cancelled_){while(paused_&&!cancelled_)Sleep(40);if(cancelled_)break;DWORD avail=0;if(!WinHttpQueryDataAvailable(h.request,&avail)){readFailed=true;break;}if(!avail){if(off<=end)readFailed=true;break;}DWORD want=(DWORD)std::min<std::uint64_t>({(std::uint64_t)buf.size(),(std::uint64_t)avail,end-off+1});DWORD got=0;if(!WinHttpReadData(h.request,buf.data(),want,&got)||!got){readFailed=true;break;}OVERLAPPED ov{};ov.Offset=(DWORD)(off&0xffffffffu);ov.OffsetHigh=(DWORD)(off>>32);DWORD wrote=0;if(!WriteFile(f,buf.data(),got,&wrote,&ov)||wrote!=got){resumeOff=off;return false;}off+=got;downloaded_+=got;update_speed();}
            if(off>end){resumeOff=off;return true;}
            if(cancelled_)break;
            if(readFailed){if(++attempt>=maxAttempts){resumeOff=off;return false;}retry_sleep(attempt);continue;}
        }
        resumeOff=off;return false;
    };

    std::vector<std::thread>ts;ts.reserve((std::size_t)workers);
    for(int k=0;k<workers;k++)ts.emplace_back([&,this]{
        HANDLE f=CreateFileW(snap.output_path.c_str(),GENERIC_WRITE|GENERIC_READ,FILE_SHARE_READ|FILE_SHARE_WRITE,nullptr,OPEN_EXISTING,FILE_ATTRIBUTE_NORMAL,nullptr);
        if(f==INVALID_HANDLE_VALUE){stopAssign=true;return;}
        std::uint64_t localChunk=4ull*1024*1024;
        while(!cancelled_&&!stopAssign){while(paused_&&!cancelled_)Sleep(40);if(cancelled_)break;auto begin=nextByte.fetch_add(localChunk);if(begin>=snap.total_bytes)break;auto end=std::min<std::uint64_t>(snap.total_bytes-1,begin+localChunk-1);auto t0=std::chrono::steady_clock::now();std::uint64_t resume=begin;bool ok=transfer_range(f,begin,end,6,resume);auto elapsed=std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::steady_clock::now()-t0).count();auto got=resume>begin?resume-begin:0;if(got&&elapsed>0)localChunk=clamp_chunk(got*1000ull/(std::uint64_t)elapsed);if(!ok&&!cancelled_){std::lock_guard lk(missingMtx);if(resume<=end)missing.push_back({resume,end});if(++exhausted>=workers*3)stopAssign=true;}}
        CloseHandle(f);
    });
    for(auto&t:ts)t.join();
    if(cancelled_)return false;

    std::uint64_t tail=std::min<std::uint64_t>(nextByte.load(),snap.total_bytes);
    if(stopAssign&&tail<snap.total_bytes){std::lock_guard lk(missingMtx);missing.push_back({tail,snap.total_bytes-1});}

    if(!missing.empty()){
        HANDLE f=CreateFileW(snap.output_path.c_str(),GENERIC_WRITE|GENERIC_READ,FILE_SHARE_READ|FILE_SHARE_WRITE,nullptr,OPEN_EXISTING,FILE_ATTRIBUTE_NORMAL,nullptr);
        if(f==INVALID_HANDLE_VALUE){fail("Large-file recovery could not reopen output file");return false;}
        std::vector<MissingRange> recovery;{
            std::lock_guard lk(missingMtx);recovery=missing;
        }
        for(auto r:recovery){for(std::uint64_t b=r.begin;b<=r.end&&!cancelled_;){auto e=std::min<std::uint64_t>(r.end,b+8ull*1024*1024-1);std::uint64_t resume=b;if(!transfer_range(f,b,e,10,resume)){CloseHandle(f);fail("Large-file recovery failed after retries; check network/server stability");return false;}b=e+1;}}
        CloseHandle(f);
    }
    if(downloaded_.load()!=snap.total_bytes){fail("Download incomplete after recovery (received "+std::to_string(downloaded_.load())+" of "+std::to_string(snap.total_bytes)+" bytes)");return false;}
    return true;
}

bool DownloadTask::sequential(){
    auto snap=snapshot();
    for(int attempt=0;attempt<4&&!cancelled_;++attempt){if(attempt){retry_sleep(attempt);downloaded_=0;speed_last_bytes_=0;}
        Handles h;if(!open_req(req_,L"GET",h)||status(h.request)>=400)continue;
        HANDLE f=CreateFileW(snap.output_path.c_str(),GENERIC_WRITE,FILE_SHARE_READ,nullptr,CREATE_ALWAYS,FILE_ATTRIBUTE_NORMAL,nullptr);if(f==INVALID_HANDLE_VALUE){fail("Cannot create output file");return false;}
        std::vector<char>buf(512*1024);bool failed=false;
        while(!cancelled_){while(paused_&&!cancelled_)Sleep(40);DWORD avail=0;if(!WinHttpQueryDataAvailable(h.request,&avail)){failed=true;break;}if(!avail)break;DWORD got=0;if(!WinHttpReadData(h.request,buf.data(),(DWORD)std::min<std::size_t>(buf.size(),avail),&got)||!got){failed=true;break;}DWORD wrote=0;if(!WriteFile(f,buf.data(),got,&wrote,nullptr)||wrote!=got){CloseHandle(f);fail("Disk write failed");return false;}downloaded_+=got;update_speed();}
        CloseHandle(f);if(cancelled_)return false;if(!failed&&downloaded_.load()==snap.total_bytes)return true;
    }
    fail("Sequential download failed after automatic retries");return false;
}

DownloadManager::DownloadManager(Callback cb):cb_(std::move(cb)){}
std::shared_ptr<DownloadTask>DownloadManager::add(DownloadRequest r){auto t=std::make_shared<DownloadTask>(next_++,std::move(r),cb_);{std::lock_guard lk(m_);tasks_.push_back(t);}t->start();return t;}
std::vector<DownloadSnapshot>DownloadManager::snapshots()const{std::lock_guard lk(m_);std::vector<DownloadSnapshot>v;for(auto&t:tasks_)v.push_back(t->snapshot());return v;}
std::shared_ptr<DownloadTask>DownloadManager::find(std::uint64_t id)const{std::lock_guard lk(m_);for(auto&t:tasks_)if(t->snapshot().id==id)return t;return{};}
}
#endif

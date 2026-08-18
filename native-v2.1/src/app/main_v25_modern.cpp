#ifdef _WIN32
#include "engine/download_engine.h"
#include "engine/media_worker.h"
#include "common/airi_json.h"
#include "common/airi_text.h"
#include "app/resource.h"
#include <windows.h>
#include <commctrl.h>
#include <shellapi.h>
#include <shlobj.h>
#include <dwmapi.h>
#include <algorithm>
#include <atomic>
#include <chrono>
#include <cwctype>
#include <filesystem>
#include <fstream>
#include <iomanip>
#include <memory>
#include <mutex>
#include <sstream>
#include <string>
#include <thread>
#include <unordered_map>
#include <vector>

namespace {
constexpr wchar_t kClass[] = L"AIRI.DownloadManager.MainWindow";
constexpr int ID_ADD=3001,ID_RESUME=3002,ID_PAUSE=3003,ID_CANCEL=3004,ID_OPEN=3005,ID_PAUSE_ALL=3006,ID_RESUME_ALL=3007,ID_SEARCH=3010,ID_FILTER=3011,ID_LIST=3100;
constexpr UINT WM_REFRESH=WM_APP+1;
constexpr std::uint64_t MEDIA_BASE=0x4000000000000000ULL;
using Clock=std::chrono::system_clock;

// AIRI visual identity: warm cream + AIRI blue.
const COLORREF C_BG=RGB(255,248,234),C_SURFACE=RGB(255,255,255),C_SURFACE2=RGB(248,251,255),C_TEXT=RGB(23,40,63),C_MUTED=RGB(96,112,134),C_ACCENT=RGB(15,76,129),C_SKY=RGB(88,166,255),C_GREEN=RGB(45,145,96),C_RED=RGB(190,65,65),C_BORDER=RGB(221,230,239);
HWND g_hwnd{},g_list{},g_search{},g_filter{},g_add{},g_resume{},g_pause{},g_cancel{},g_open{},g_pause_all{},g_resume_all{};
HFONT g_font{},g_font_bold{},g_title_font{},g_stat_font{};
HBRUSH g_bg_brush{},g_surface_brush{};
HICON g_icon{};
std::unique_ptr<airi::DownloadManager> g_mgr;
std::wstring g_notice=L"Ready";
std::mutex g_notice_mtx;
int g_active{},g_completed{},g_failed{};
unsigned long long g_total_speed{};

struct Timing{Clock::time_point started{};Clock::time_point finished{};bool has_start{},has_finish{};};
std::mutex g_time_mtx;std::unordered_map<std::uint64_t,Timing>g_times;
struct MediaRow{
    std::uint64_t id{};
    std::wstring name,status=L"Queued";
    int progress{};
    std::filesystem::path dir,output_path;
    bool done{},failed{},cancelled{};
    std::wstring downloaded,total,speed,eta,fragment;
    std::shared_ptr<std::atomic_bool> cancel;
};
std::mutex g_media_mtx;std::atomic<std::uint64_t>g_media_next{MEDIA_BASE};std::vector<MediaRow>g_media;

std::wstring W(const std::string&s){if(s.empty())return{};int n=MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0);std::wstring o(n,L'\0');MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n);return o;}
std::string U8(const std::wstring&s){if(s.empty())return{};int n=WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0,nullptr,nullptr);std::string o(n,'\0');WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n,nullptr,nullptr);return o;}
void notice(std::wstring s){{std::lock_guard lk(g_notice_mtx);g_notice=std::move(s);}if(g_hwnd)PostMessageW(g_hwnd,WM_REFRESH,0,0);}
std::filesystem::path downloads(){PWSTR p{};if(SUCCEEDED(SHGetKnownFolderPath(FOLDERID_Downloads,0,nullptr,&p))){std::filesystem::path r(p);CoTaskMemFree(p);return r;}return std::filesystem::current_path();}
void timing_start(std::uint64_t id){std::lock_guard lk(g_time_mtx);auto&t=g_times[id];if(!t.has_start){t.started=Clock::now();t.has_start=true;}}
void timing_finish(std::uint64_t id){std::lock_guard lk(g_time_mtx);auto&t=g_times[id];if(!t.has_start){t.started=Clock::now();t.has_start=true;}if(!t.has_finish){t.finished=Clock::now();t.has_finish=true;}}
Timing timing_get(std::uint64_t id){std::lock_guard lk(g_time_mtx);auto it=g_times.find(id);return it==g_times.end()?Timing{}:it->second;}
std::wstring fmt_time(const Clock::time_point&tp,bool valid){if(!valid)return L"-";auto tt=Clock::to_time_t(tp);tm lt{};localtime_s(&lt,&tt);std::wostringstream os;os<<std::put_time(&lt,L"%d/%m %H:%M:%S");return os.str();}
std::wstring fmt_elapsed(const Timing&t){if(!t.has_start)return L"-";auto end=t.has_finish?t.finished:Clock::now();auto sec=std::chrono::duration_cast<std::chrono::seconds>(end-t.started).count();if(sec<0)sec=0;auto h=sec/3600,m=(sec%3600)/60,s=sec%60;std::wostringstream os;if(h)os<<h<<L"h ";if(h||m)os<<m<<L"m ";os<<s<<L"s";return os.str();}
std::wstring eta_text(std::uint64_t total,std::uint64_t done,double bps){if(!total||done>=total||bps<1024)return L"-";auto sec=(long long)((total-done)/bps);auto h=sec/3600,m=(sec%3600)/60,s=sec%60;std::wostringstream os;if(h)os<<h<<L"h ";if(h||m)os<<m<<L"m ";os<<s<<L"s";return os.str();}
std::wstring lower(std::wstring s){std::transform(s.begin(),s.end(),s.begin(),[](wchar_t c){return (wchar_t)towlower(c);});return s;}
unsigned long long speed_bytes(std::wstring s){if(s.empty()||s==L"-"||s==L"NA")return 0;auto l=lower(s);wchar_t*end=nullptr;double v=wcstod(l.c_str(),&end);if(end==l.c_str()||v<0)return 0;double mult=1.0;if(l.find(L"gib")!=std::wstring::npos)mult=1024.0*1024.0*1024.0;else if(l.find(L"mib")!=std::wstring::npos)mult=1024.0*1024.0;else if(l.find(L"kib")!=std::wstring::npos)mult=1024.0;else if(l.find(L"gb")!=std::wstring::npos)mult=1000.0*1000.0*1000.0;else if(l.find(L"mb")!=std::wstring::npos)mult=1000.0*1000.0;else if(l.find(L"kb")!=std::wstring::npos)mult=1000.0;return (unsigned long long)(v*mult);}

void register_bridge(){wchar_t exe[32768]{};GetModuleFileNameW(nullptr,exe,32768);auto dir=std::filesystem::path(exe).parent_path();auto bridge=dir/L"AIRIDMBridge.exe";if(!std::filesystem::exists(bridge))return;auto mf=dir/L"com.airi.downloadmanager.json";std::string p=U8(bridge.wstring()),esc;for(char c:p){if(c=='\\')esc+="\\\\";else if(c=='\"')esc+="\\\"";else esc+=c;}std::ofstream f(mf,std::ios::binary|std::ios::trunc);f<<"{\n\"name\":\"com.airi.downloadmanager\",\n\"description\":\"AIRI Native Bridge\",\n\"path\":\""<<esc<<"\",\n\"type\":\"stdio\",\n\"allowed_origins\":[\"chrome-extension://knmjnpnbngjeilejfdhccehndiengjan/\"]\n}";f.close();for(auto key:{L"Software\\Google\\Chrome\\NativeMessagingHosts\\com.airi.downloadmanager",L"Software\\Microsoft\\Edge\\NativeMessagingHosts\\com.airi.downloadmanager"}){HKEY h{};if(RegCreateKeyExW(HKEY_CURRENT_USER,key,0,nullptr,0,KEY_SET_VALUE,nullptr,&h,nullptr)==ERROR_SUCCESS){auto s=mf.wstring();RegSetValueExW(h,nullptr,0,REG_SZ,(BYTE*)s.c_str(),(DWORD)((s.size()+1)*sizeof(wchar_t)));RegCloseKey(h);}}}

struct AddData{std::wstring url,filename;int connections=16;};
INT_PTR CALLBACK AddDlg(HWND h,UINT m,WPARAM wp,LPARAM lp){
    auto*d=(AddData*)GetWindowLongPtrW(h,GWLP_USERDATA);
    if(m==WM_INITDIALOG){
        d=(AddData*)lp;SetWindowLongPtrW(h,GWLP_USERDATA,lp);SetWindowTextW(h,L"AIRI • New Download");BOOL dark=FALSE;DwmSetWindowAttribute(h,20,&dark,sizeof(dark));
        auto c=GetDlgItem(h,IDC_CONNECTIONS);for(int v:{1,2,4,8,16,32,64}){auto s=std::to_wstring(v);SendMessageW(c,CB_ADDSTRING,0,(LPARAM)s.c_str());}SendMessageW(c,CB_SETCURSEL,4,0);
        for(int id:{IDC_URL,IDC_FILENAME,IDC_CONNECTIONS,IDOK,IDCANCEL})if(auto x=GetDlgItem(h,id))SendMessageW(x,WM_SETFONT,(WPARAM)g_font,TRUE);
        SetFocus(GetDlgItem(h,IDC_URL));return FALSE;
    }
    if(m==WM_CTLCOLORDLG)return (INT_PTR)g_bg_brush;
    if(m==WM_CTLCOLORSTATIC){auto dc=(HDC)wp;SetTextColor(dc,C_TEXT);SetBkMode(dc,TRANSPARENT);return (INT_PTR)g_bg_brush;}
    if(m==WM_CTLCOLOREDIT||m==WM_CTLCOLORLISTBOX){auto dc=(HDC)wp;SetTextColor(dc,C_TEXT);SetBkColor(dc,C_SURFACE2);return (INT_PTR)g_surface_brush;}
    if(m==WM_COMMAND){
        if(LOWORD(wp)==IDOK&&d){wchar_t url[8192]{},fn[1024]{};GetDlgItemTextW(h,IDC_URL,url,8192);GetDlgItemTextW(h,IDC_FILENAME,fn,1024);std::wstring s=url;if(s.rfind(L"http://",0)!=0&&s.rfind(L"https://",0)!=0){MessageBoxW(h,L"Enter a valid HTTP/HTTPS URL.",L"AIRI Download Manager",MB_ICONWARNING);return TRUE;}d->url=url;d->filename=fn;int idx=(int)SendDlgItemMessageW(h,IDC_CONNECTIONS,CB_GETCURSEL,0,0);int vals[]={1,2,4,8,16,32,64};d->connections=(idx>=0&&idx<7)?vals[idx]:16;EndDialog(h,IDOK);return TRUE;}
        if(LOWORD(wp)==IDCANCEL){EndDialog(h,IDCANCEL);return TRUE;}
    }
    return FALSE;
}

std::uint64_t selected(){int row=ListView_GetNextItem(g_list,-1,LVNI_SELECTED);if(row<0)return 0;LVITEMW i{};i.mask=LVIF_PARAM;i.iItem=row;return ListView_GetItem(g_list,&i)?(std::uint64_t)i.lParam:0;}
bool media_id(std::uint64_t id){return id>=MEDIA_BASE;}
bool terminal(airi::DownloadStatus s){return s==airi::DownloadStatus::Completed||s==airi::DownloadStatus::Failed||s==airi::DownloadStatus::Cancelled;}

bool add_direct(const std::string&url,const std::string&filename={},const std::string&cookie={},const std::string&referer={},const std::string&origin={},const std::string&ua={},int conn=16){if(url.rfind("http://",0)&&url.rfind("https://",0)){notice(L"Invalid URL rejected");return false;}airi::DownloadRequest r;r.url=url;r.filename=filename;r.save_directory=downloads();r.cookie=cookie;r.referer=referer;r.origin=origin;r.user_agent=ua;r.connections=std::clamp(conn,1,64);auto task=g_mgr->add(std::move(r));timing_start(task->snapshot().id);notice(L"Added to AIRI Turbo");return true;}

bool add_media(const std::string&raw){
    airi::MediaRequest r;
    r.page_url=airi::json::get_string(raw,"pageUrl").value_or(airi::json::get_string(raw,"url").value_or(""));r.title=airi::json::get_string(raw,"title").value_or("Media");r.quality=airi::json::get_string(raw,"quality").value_or("best");r.cookie=airi::json::get_string(raw,"cookie").value_or("");r.referer=airi::json::get_string(raw,"referer").value_or(r.page_url);r.user_agent=airi::json::get_string(raw,"userAgent").value_or("");r.save_directory=downloads();
    if(r.page_url.rfind("http://",0)&&r.page_url.rfind("https://",0)){notice(L"Invalid media URL");return false;}
    auto id=g_media_next++;auto token=std::make_shared<std::atomic_bool>(false);r.cancel_requested=token;timing_start(id);bool available=airi::MediaWorker::available();
    {std::lock_guard lk(g_media_mtx);MediaRow row;row.id=id;row.name=W(r.title);row.status=available?L"Preparing":L"Engine missing";row.dir=r.save_directory;row.failed=!available;row.cancel=token;g_media.push_back(std::move(row));}
    if(!available){timing_finish(id);notice(L"Media engine missing");return false;}
    std::thread([id,r=std::move(r)]()mutable{
        bool ok=airi::MediaWorker::download(r,[id](const airi::MediaProgress&p){
            {std::lock_guard lk(g_media_mtx);for(auto&j:g_media)if(j.id==id){
                if(!p.status.empty())j.status=W(p.status);
                if(p.percent>=0)j.progress=std::clamp(p.percent,0,100);
                if(!p.downloaded_text.empty())j.downloaded=W(p.downloaded_text);
                if(!p.total_text.empty())j.total=W(p.total_text);
                if(!p.speed_text.empty())j.speed=W(p.speed_text);
                if(!p.eta_text.empty())j.eta=W(p.eta_text);
                if(!p.fragment_text.empty())j.fragment=W(p.fragment_text);
                if(!p.output_path.empty())j.output_path=std::filesystem::path(W(p.output_path));
                j.done=p.status=="Completed";if(p.status=="Failed")j.failed=true;if(p.status=="Cancelled")j.cancelled=true;
                break;
            }}
            if(g_hwnd)PostMessageW(g_hwnd,WM_REFRESH,0,0);
        });
        timing_finish(id);
        bool wasCancelled=r.cancel_requested&&r.cancel_requested->load();
        notice(ok?L"Media completed":wasCancelled?L"Media cancelled":L"Media failed");
    }).detach();
    return true;
}

bool payload(const std::string&raw){auto a=airi::json::get_string(raw,"action").value_or("download");if(a=="ping")return true;if(a=="download"||a=="capture")return add_direct(airi::json::get_string(raw,"url").value_or(""),airi::json::get_string(raw,"filename").value_or(""),airi::json::get_string(raw,"cookie").value_or(""),airi::json::get_string(raw,"referer").value_or(""),airi::json::get_string(raw,"origin").value_or(""),airi::json::get_string(raw,"userAgent").value_or(""));if(a=="media_page")return add_media(raw);return false;}

bool matches_filter(const std::wstring&name,const std::wstring&status){wchar_t q[256]{};GetWindowTextW(g_search,q,256);auto needle=lower(q);if(!needle.empty()&&lower(name).find(needle)==std::wstring::npos)return false;int f=(int)SendMessageW(g_filter,CB_GETCURSEL,0,0);if(f==1&&status!=L"Downloading"&&status!=L"Preparing"&&status!=L"Starting"&&status!=L"Merging"&&status!=L"Finalizing"&&status!=L"Cancelling...")return false;if(f==2&&status!=L"Completed")return false;if(f==3&&status!=L"Paused")return false;if(f==4&&status!=L"Failed")return false;if(f==5&&status!=L"Cancelled")return false;return true;}
void setcell(int r,int c,const std::wstring&v){ListView_SetItemText(g_list,r,c,(LPWSTR)v.c_str());}

void buttons(){
    auto id=selected();bool canPause=false,canResume=false,canCancel=false;
    if(id&&media_id(id)){
        std::lock_guard lk(g_media_mtx);for(auto&j:g_media)if(j.id==id){canCancel=!j.done&&!j.failed&&!j.cancelled;break;}
    }else if(id){
        if(auto t=g_mgr->find(id)){auto s=t->snapshot();canPause=s.status==airi::DownloadStatus::Downloading;canResume=s.status==airi::DownloadStatus::Paused;canCancel=!terminal(s.status);}
    }
    EnableWindow(g_pause,canPause);EnableWindow(g_resume,canResume);EnableWindow(g_cancel,canCancel);EnableWindow(g_open,TRUE);
    for(HWND x:{g_pause,g_resume,g_cancel,g_open})InvalidateRect(x,nullptr,TRUE);
}

void refresh(){
    auto sel=selected();ListView_DeleteAllItems(g_list);int row=0;g_active=g_completed=g_failed=0;g_total_speed=0;
    for(auto&s:g_mgr->snapshots()){
        if(terminal(s.status))timing_finish(s.id);auto name=W(s.filename.empty()?"download":s.filename),st=W(airi::status_name(s.status));
        if(s.status==airi::DownloadStatus::Downloading){g_active++;g_total_speed+=(unsigned long long)s.bytes_per_second;}if(s.status==airi::DownloadStatus::Completed)g_completed++;if(s.status==airi::DownloadStatus::Failed)g_failed++;
        if(!matches_filter(name,st))continue;LVITEMW i{};i.mask=LVIF_TEXT|LVIF_PARAM;i.iItem=row;i.pszText=name.data();i.lParam=(LPARAM)s.id;int r=ListView_InsertItem(g_list,&i);setcell(r,1,st);setcell(r,2,std::to_wstring(s.progress_percent)+L"%");setcell(r,3,s.bytes_per_second>0?W(airi::text::human_bytes((unsigned long long)s.bytes_per_second))+L"/s":L"-");auto size=W(airi::text::human_bytes(s.downloaded_bytes))+L" / "+W(airi::text::human_bytes(s.total_bytes));setcell(r,4,size);setcell(r,5,eta_text(s.total_bytes,s.downloaded_bytes,s.bytes_per_second));auto t=timing_get(s.id);setcell(r,6,fmt_time(t.started,t.has_start));setcell(r,7,fmt_elapsed(t));if(s.id==sel)ListView_SetItemState(g_list,r,LVIS_SELECTED|LVIS_FOCUSED,LVIS_SELECTED|LVIS_FOCUSED);row++;
    }
    {std::lock_guard lk(g_media_mtx);for(auto&j:g_media){
        if(!j.done&&!j.failed&&!j.cancelled){g_active++;g_total_speed+=speed_bytes(j.speed);}if(j.done)g_completed++;if(j.failed)g_failed++;
        if(!matches_filter(j.name,j.status))continue;LVITEMW i{};i.mask=LVIF_TEXT|LVIF_PARAM;i.iItem=row;i.pszText=j.name.data();i.lParam=(LPARAM)j.id;int r=ListView_InsertItem(g_list,&i);setcell(r,1,j.status);setcell(r,2,std::to_wstring(j.progress)+L"%");setcell(r,3,j.speed.empty()?L"-":j.speed);std::wstring sz=j.downloaded;if(!j.total.empty()&&j.total!=L"NA")sz+=(sz.empty()?L"":L" / ")+j.total;setcell(r,4,sz.empty()?L"-":sz);setcell(r,5,j.eta.empty()?L"-":j.eta);auto t=timing_get(j.id);setcell(r,6,fmt_time(t.started,t.has_start));setcell(r,7,fmt_elapsed(t));if(j.id==sel)ListView_SetItemState(g_list,r,LVIS_SELECTED|LVIS_FOCUSED,LVIS_SELECTED|LVIS_FOCUSED);row++;
    }}
    InvalidateRect(g_hwnd,nullptr,FALSE);buttons();
}

void open_folder(){
    auto d=downloads();auto id=selected();
    if(id&&media_id(id)){std::lock_guard lk(g_media_mtx);for(auto&j:g_media)if(j.id==id){d=!j.output_path.empty()?j.output_path.parent_path():j.dir;break;}}
    else if(id){if(auto t=g_mgr->find(id)){auto s=t->snapshot();if(!s.output_path.empty())d=s.output_path.parent_path();}}
    ShellExecuteW(g_hwnd,L"open",d.c_str(),nullptr,nullptr,SW_SHOWNORMAL);
}
void open_selected(){
    auto id=selected();if(!id){open_folder();return;}
    if(media_id(id)){std::filesystem::path p;{std::lock_guard lk(g_media_mtx);for(auto&j:g_media)if(j.id==id){p=j.output_path;break;}}if(!p.empty()&&std::filesystem::exists(p)){ShellExecuteW(g_hwnd,L"open",p.c_str(),nullptr,nullptr,SW_SHOWNORMAL);return;}open_folder();return;}
    if(auto t=g_mgr->find(id)){auto s=t->snapshot();if(!s.output_path.empty()&&std::filesystem::exists(s.output_path)){ShellExecuteW(g_hwnd,L"open",s.output_path.c_str(),nullptr,nullptr,SW_SHOWNORMAL);return;}}open_folder();
}
void cancel_media(std::uint64_t id){std::lock_guard lk(g_media_mtx);for(auto&j:g_media)if(j.id==id&&!j.done&&!j.failed&&!j.cancelled){if(j.cancel)j.cancel->store(true);j.status=L"Cancelling...";notice(L"Cancelling media download...");break;}}
void act_all(bool resume){for(auto&s:g_mgr->snapshots()){auto t=g_mgr->find(s.id);if(!t)continue;if(resume&&s.status==airi::DownloadStatus::Paused)t->resume();if(!resume&&s.status==airi::DownloadStatus::Downloading)t->pause();}notice(resume?L"Resumed all direct downloads":L"Paused all direct downloads");}

void round_box(HDC h,RECT r,COLORREF fill,COLORREF border,int radius=14){HBRUSH b=CreateSolidBrush(fill);HPEN p=CreatePen(PS_SOLID,1,border);auto ob=SelectObject(h,b);auto op=SelectObject(h,p);RoundRect(h,r.left,r.top,r.right,r.bottom,radius,radius);SelectObject(h,op);SelectObject(h,ob);DeleteObject(p);DeleteObject(b);}
void round_fill(HDC h,RECT r,COLORREF c,int radius=14){round_box(h,r,c,c,radius);}
void draw_text(HDC h,const wchar_t*t,RECT r,COLORREF c,HFONT f,UINT flags=DT_LEFT|DT_VCENTER|DT_SINGLELINE){SetBkMode(h,TRANSPARENT);SetTextColor(h,c);auto old=SelectObject(h,f);DrawTextW(h,t,-1,&r,flags);SelectObject(h,old);}
void draw_card(HDC h,RECT r,const wchar_t*label,const std::wstring&value,COLORREF accent){round_box(h,r,C_SURFACE,C_BORDER,18);RECT a{r.left,r.top,r.left+5,r.bottom};round_fill(h,a,accent,5);RECT lr{r.left+19,r.top+10,r.right-12,r.top+30};draw_text(h,label,lr,C_MUTED,g_font);RECT vr{r.left+19,r.top+29,r.right-12,r.bottom-8};draw_text(h,value.c_str(),vr,C_TEXT,g_stat_font);}

LRESULT custom_draw(NMLVCUSTOMDRAW*cd){
    if(cd->nmcd.dwDrawStage==CDDS_PREPAINT)return CDRF_NOTIFYITEMDRAW;
    if(cd->nmcd.dwDrawStage==CDDS_ITEMPREPAINT){cd->clrText=C_TEXT;cd->clrTextBk=(cd->nmcd.dwItemSpec%2)?C_SURFACE:C_SURFACE2;return CDRF_NOTIFYSUBITEMDRAW;}
    if(cd->nmcd.dwDrawStage==(CDDS_ITEMPREPAINT|CDDS_SUBITEM)){
        if(cd->iSubItem==2){RECT rc{};ListView_GetSubItemRect(g_list,(int)cd->nmcd.dwItemSpec,2,LVIR_BOUNDS,&rc);wchar_t txt[32]{};ListView_GetItemText(g_list,(int)cd->nmcd.dwItemSpec,2,txt,32);int pct=std::clamp(_wtoi(txt),0,100);round_fill(cd->nmcd.hdc,RECT{rc.left+8,rc.top+8,rc.right-8,rc.bottom-8},RGB(225,235,245),8);RECT fill{rc.left+8,rc.top+8,rc.left+8+(rc.right-rc.left-16)*pct/100,rc.bottom-8};if(fill.right>fill.left)round_fill(cd->nmcd.hdc,fill,C_SKY,8);draw_text(cd->nmcd.hdc,txt,rc,C_TEXT,g_font_bold,DT_CENTER|DT_VCENTER|DT_SINGLELINE);return CDRF_SKIPDEFAULT;}
        return CDRF_DODEFAULT;
    }
    return CDRF_DODEFAULT;
}

void layout(HWND h){RECT r{};GetClientRect(h,&r);int w=(int)r.right,y=158,top=214,x=24;MoveWindow(g_search,x,y,210,36,TRUE);x+=222;MoveWindow(g_filter,x,y,132,220,TRUE);x+=144;MoveWindow(g_add,x,y,126,36,TRUE);x+=134;MoveWindow(g_pause,x,y,80,36,TRUE);x+=88;MoveWindow(g_resume,x,y,86,36,TRUE);x+=94;MoveWindow(g_cancel,x,y,80,36,TRUE);x+=88;MoveWindow(g_pause_all,x,y,94,36,TRUE);x+=102;MoveWindow(g_resume_all,x,y,100,36,TRUE);x+=108;MoveWindow(g_open,x,y,108,36,TRUE);MoveWindow(g_list,24,top,std::max(400,w-48),(int)std::max<LONG>(120,r.bottom-top-64),TRUE);}

LRESULT CALLBACK Proc(HWND h,UINT m,WPARAM wp,LPARAM lp){switch(m){
case WM_CREATE:{
    g_hwnd=h;BOOL dark=FALSE;DwmSetWindowAttribute(h,20,&dark,sizeof(dark));
    auto mkbtn=[&](HWND&out,int id,const wchar_t*t){out=CreateWindowW(L"BUTTON",t,WS_CHILD|WS_VISIBLE|WS_TABSTOP|BS_OWNERDRAW,0,0,100,36,h,(HMENU)(INT_PTR)id,nullptr,nullptr);SendMessageW(out,WM_SETFONT,(WPARAM)g_font_bold,TRUE);};
    mkbtn(g_add,ID_ADD,L"+ New");mkbtn(g_resume,ID_RESUME,L"Resume");mkbtn(g_pause,ID_PAUSE,L"Pause");mkbtn(g_cancel,ID_CANCEL,L"Cancel");mkbtn(g_open,ID_OPEN,L"Open file");mkbtn(g_pause_all,ID_PAUSE_ALL,L"Pause all");mkbtn(g_resume_all,ID_RESUME_ALL,L"Resume all");
    g_search=CreateWindowExW(WS_EX_CLIENTEDGE,L"EDIT",L"",WS_CHILD|WS_VISIBLE|WS_TABSTOP|ES_AUTOHSCROLL,0,0,200,36,h,(HMENU)(INT_PTR)ID_SEARCH,nullptr,nullptr);SendMessageW(g_search,EM_SETCUEBANNER,TRUE,(LPARAM)L"Search downloads...");
    g_filter=CreateWindowW(WC_COMBOBOXW,L"",WS_CHILD|WS_VISIBLE|CBS_DROPDOWNLIST,0,0,150,200,h,(HMENU)(INT_PTR)ID_FILTER,nullptr,nullptr);for(auto x:{L"All downloads",L"Active",L"Completed",L"Paused",L"Failed",L"Cancelled"})SendMessageW(g_filter,CB_ADDSTRING,0,(LPARAM)x);SendMessageW(g_filter,CB_SETCURSEL,0,0);
    g_list=CreateWindowExW(0,WC_LISTVIEWW,L"",WS_CHILD|WS_VISIBLE|LVS_REPORT|LVS_SINGLESEL|LVS_SHOWSELALWAYS,0,0,800,400,h,(HMENU)(INT_PTR)ID_LIST,nullptr,nullptr);ListView_SetExtendedListViewStyle(g_list,LVS_EX_FULLROWSELECT|LVS_EX_DOUBLEBUFFER|LVS_EX_LABELTIP);ListView_SetBkColor(g_list,C_SURFACE);ListView_SetTextBkColor(g_list,C_SURFACE);ListView_SetTextColor(g_list,C_TEXT);SendMessageW(g_list,WM_SETFONT,(WPARAM)g_font,TRUE);
    struct Col{const wchar_t*t;int width;}cs[]={{L"Name",300},{L"Status",118},{L"Progress",150},{L"Speed",110},{L"Downloaded / Total",170},{L"ETA",92},{L"Started",118},{L"Elapsed",94}};for(int i=0;i<8;i++){LVCOLUMNW c{};c.mask=LVCF_TEXT|LVCF_WIDTH;c.pszText=(LPWSTR)cs[i].t;c.cx=cs[i].width;ListView_InsertColumn(g_list,i,&c);}
    SendMessageW(g_search,WM_SETFONT,(WPARAM)g_font,TRUE);SendMessageW(g_filter,WM_SETFONT,(WPARAM)g_font,TRUE);SetTimer(h,1,500,nullptr);layout(h);buttons();return 0;
}
case WM_GETMINMAXINFO:{auto*mi=(MINMAXINFO*)lp;mi->ptMinTrackSize.x=1160;mi->ptMinTrackSize.y=650;return 0;}
case WM_SIZE:layout(h);InvalidateRect(h,nullptr,TRUE);return 0;
case WM_ERASEBKGND:return 1;
case WM_PAINT:{
    PAINTSTRUCT ps{};HDC dc=BeginPaint(h,&ps);RECT cr{};GetClientRect(h,&cr);FillRect(dc,&cr,g_bg_brush);
    draw_text(dc,L"AIRI Download Manager",RECT{24,16,700,52},C_TEXT,g_title_font);
    draw_text(dc,L"Smart Turbo + Universal Media Center  •  v2.6 AIRI Edition",RECT{26,52,850,78},C_MUTED,g_font);
    int gap=12,cw=((int)std::max<LONG>(900,cr.right)-48-gap*3)/4,x=24;draw_card(dc,RECT{x,88,x+cw,142},L"ACTIVE",std::to_wstring(g_active),C_ACCENT);x+=cw+gap;draw_card(dc,RECT{x,88,x+cw,142},L"TOTAL SPEED",W(airi::text::human_bytes(g_total_speed))+L"/s",C_SKY);x+=cw+gap;draw_card(dc,RECT{x,88,x+cw,142},L"COMPLETED",std::to_wstring(g_completed),C_GREEN);x+=cw+gap;draw_card(dc,RECT{x,88,x+cw,142},L"FAILED",std::to_wstring(g_failed),g_failed?C_RED:C_MUTED);
    std::wstring nt;{std::lock_guard lk(g_notice_mtx);nt=g_notice;}draw_text(dc,(L"Developed by AIRI Technology  •  Founder Akhbar Arianda  •  "+nt).c_str(),RECT{24,cr.bottom-46,cr.right-24,cr.bottom-12},C_MUTED,g_font);
    EndPaint(h,&ps);return 0;
}
case WM_CTLCOLOREDIT:case WM_CTLCOLORLISTBOX:{HDC dc=(HDC)wp;SetTextColor(dc,C_TEXT);SetBkColor(dc,C_SURFACE2);return (LRESULT)g_surface_brush;}
case WM_DRAWITEM:{
    auto*d=(DRAWITEMSTRUCT*)lp;if(d&&d->CtlType==ODT_BUTTON){bool enabled=!(d->itemState&ODS_DISABLED),pressed=(d->itemState&ODS_SELECTED)!=0;COLORREF fill=C_SURFACE2,border=C_BORDER,text=C_ACCENT;if(d->CtlID==ID_ADD){fill=pressed?RGB(11,61,105):C_ACCENT;border=fill;text=RGB(255,255,255);}else if(d->CtlID==ID_CANCEL&&enabled){fill=pressed?RGB(247,222,222):RGB(255,241,241);border=RGB(235,190,190);text=C_RED;}else if(!enabled){fill=RGB(241,244,247);border=RGB(226,231,237);text=RGB(150,160,173);}else if(pressed){fill=RGB(231,241,251);border=RGB(173,205,232);}round_box(d->hDC,d->rcItem,fill,border,12);int n=GetWindowTextLengthW(d->hwndItem);std::vector<wchar_t>buf((std::size_t)n+1);GetWindowTextW(d->hwndItem,buf.data(),n+1);draw_text(d->hDC,buf.data(),d->rcItem,text,g_font_bold,DT_CENTER|DT_VCENTER|DT_SINGLELINE);return TRUE;}break;
}
case WM_NOTIFY:{auto*n=(NMHDR*)lp;if(n&&n->idFrom==ID_LIST){if(n->code==NM_CUSTOMDRAW)return custom_draw((NMLVCUSTOMDRAW*)lp);if(n->code==LVN_ITEMCHANGED)buttons();if(n->code==NM_DBLCLK)open_selected();}return 0;}
case WM_TIMER:case WM_REFRESH:refresh();return 0;
case WM_COPYDATA:{auto*c=(COPYDATASTRUCT*)lp;if(!c||!c->lpData||!c->cbData)return FALSE;std::string raw((char*)c->lpData,c->cbData);if(!raw.empty()&&raw.back()=='\0')raw.pop_back();return payload(raw)?TRUE:FALSE;}
case WM_COMMAND:{
    int id=LOWORD(wp);if(id==ID_SEARCH&&HIWORD(wp)==EN_CHANGE){refresh();return 0;}if(id==ID_FILTER&&HIWORD(wp)==CBN_SELCHANGE){refresh();return 0;}
    if(id==ID_ADD&&HIWORD(wp)==BN_CLICKED){AddData d;auto r=DialogBoxParamW(GetModuleHandleW(nullptr),MAKEINTRESOURCEW(IDD_ADD_URL),h,AddDlg,(LPARAM)&d);if(r==IDOK)add_direct(U8(d.url),U8(d.filename),{},{},{},{},d.connections);return 0;}
    if(id==ID_OPEN&&HIWORD(wp)==BN_CLICKED){open_selected();return 0;}if(id==ID_PAUSE_ALL&&HIWORD(wp)==BN_CLICKED){act_all(false);return 0;}if(id==ID_RESUME_ALL&&HIWORD(wp)==BN_CLICKED){act_all(true);return 0;}
    if((id==ID_PAUSE||id==ID_RESUME||id==ID_CANCEL)&&HIWORD(wp)==BN_CLICKED){auto sid=selected();if(!sid){notice(L"Select a download first");return 0;}if(media_id(sid)){if(id==ID_CANCEL)cancel_media(sid);else notice(L"Pause/Resume for segmented media will be added in the next engine revision");refresh();return 0;}auto t=g_mgr->find(sid);if(!t)return 0;if(id==ID_PAUSE)t->pause();else if(id==ID_RESUME)t->resume();else{t->cancel();timing_finish(sid);}refresh();return 0;}
    return 0;
}
case WM_DESTROY:KillTimer(h,1);g_hwnd=nullptr;PostQuitMessage(0);return 0;
}return DefWindowProcW(h,m,wp,lp);}
}

int WINAPI wWinMain(HINSTANCE hi,HINSTANCE,PWSTR,int show){
    SetProcessDPIAware();CoInitializeEx(nullptr,COINIT_APARTMENTTHREADED);INITCOMMONCONTROLSEX ic{sizeof(ic),ICC_LISTVIEW_CLASSES|ICC_STANDARD_CLASSES};InitCommonControlsEx(&ic);
    NONCLIENTMETRICSW n{sizeof(n)};SystemParametersInfoW(SPI_GETNONCLIENTMETRICS,sizeof(n),&n,0);LOGFONTW lf=n.lfMessageFont;wcscpy_s(lf.lfFaceName,L"Segoe UI");lf.lfHeight=-15;g_font=CreateFontIndirectW(&lf);lf.lfWeight=FW_SEMIBOLD;g_font_bold=CreateFontIndirectW(&lf);lf.lfHeight=-28;lf.lfWeight=FW_BOLD;g_title_font=CreateFontIndirectW(&lf);lf.lfHeight=-22;g_stat_font=CreateFontIndirectW(&lf);
    g_bg_brush=CreateSolidBrush(C_BG);g_surface_brush=CreateSolidBrush(C_SURFACE2);g_icon=LoadIconW(hi,MAKEINTRESOURCEW(IDI_AIRI));g_mgr=std::make_unique<airi::DownloadManager>([](const airi::DownloadSnapshot&){if(g_hwnd)PostMessageW(g_hwnd,WM_REFRESH,0,0);});register_bridge();
    WNDCLASSEXW wc{sizeof(wc)};wc.lpfnWndProc=Proc;wc.hInstance=hi;wc.lpszClassName=kClass;wc.hCursor=LoadCursorW(nullptr,IDC_ARROW);wc.hIcon=g_icon?g_icon:LoadIconW(nullptr,IDI_APPLICATION);wc.hIconSm=wc.hIcon;wc.hbrBackground=g_bg_brush;RegisterClassExW(&wc);
    auto h=CreateWindowExW(0,kClass,L"AIRI Download Manager v2.6 AIRI Edition",WS_OVERLAPPEDWINDOW|WS_CLIPCHILDREN,CW_USEDEFAULT,CW_USEDEFAULT,1400,840,nullptr,nullptr,hi,nullptr);if(!h)return 2;ShowWindow(h,show);UpdateWindow(h);
    MSG msg{};while(GetMessageW(&msg,nullptr,0,0)>0){TranslateMessage(&msg);DispatchMessageW(&msg);}g_mgr.reset();DeleteObject(g_font);DeleteObject(g_font_bold);DeleteObject(g_title_font);DeleteObject(g_stat_font);DeleteObject(g_bg_brush);DeleteObject(g_surface_brush);CoUninitialize();return(int)msg.wParam;
}
#endif

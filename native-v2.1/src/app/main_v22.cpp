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
#include <algorithm>
#include <atomic>
#include <chrono>
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
constexpr int ID_ADD=3001,ID_RESUME=3002,ID_PAUSE=3003,ID_CANCEL=3004,ID_OPEN=3005,ID_LIST=3100;
constexpr UINT WM_REFRESH=WM_APP+1;
constexpr std::uint64_t MEDIA_BASE=0x4000000000000000ULL;
using Clock=std::chrono::system_clock;

HWND g_hwnd{},g_list{},g_status{},g_add{},g_resume{},g_pause{},g_cancel{},g_open{};
HFONT g_font{},g_title_font{};
HICON g_icon{};
std::unique_ptr<airi::DownloadManager> g_mgr;
std::wstring g_notice=L"Ready";
std::mutex g_notice_mtx;

struct Timing{Clock::time_point started{};Clock::time_point finished{};bool has_start{},has_finish{};};
std::mutex g_time_mtx;
std::unordered_map<std::uint64_t,Timing> g_times;

struct MediaRow{std::uint64_t id{};std::wstring name,status=L"Queued";int progress{};std::filesystem::path dir;bool done{},failed{};};
std::mutex g_media_mtx;
std::atomic<std::uint64_t> g_media_next{MEDIA_BASE};
std::vector<MediaRow> g_media;

std::wstring w(const std::string&s){if(s.empty())return{};int n=MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0);std::wstring o(n,L'\0');MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n);return o;}
std::string u8(const std::wstring&s){if(s.empty())return{};int n=WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0,nullptr,nullptr);std::string o(n,'\0');WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n,nullptr,nullptr);return o;}
void notice(std::wstring s){{std::lock_guard lk(g_notice_mtx);g_notice=std::move(s);}if(g_hwnd)PostMessageW(g_hwnd,WM_REFRESH,0,0);}
std::filesystem::path downloads(){PWSTR p{};if(SUCCEEDED(SHGetKnownFolderPath(FOLDERID_Downloads,0,nullptr,&p))){std::filesystem::path r(p);CoTaskMemFree(p);return r;}return std::filesystem::current_path();}

void timing_start(std::uint64_t id){std::lock_guard lk(g_time_mtx);auto&t=g_times[id];if(!t.has_start){t.started=Clock::now();t.has_start=true;}}
void timing_finish(std::uint64_t id){std::lock_guard lk(g_time_mtx);auto&t=g_times[id];if(!t.has_start){t.started=Clock::now();t.has_start=true;}if(!t.has_finish){t.finished=Clock::now();t.has_finish=true;}}
Timing timing_get(std::uint64_t id){std::lock_guard lk(g_time_mtx);auto it=g_times.find(id);return it==g_times.end()?Timing{}:it->second;}
std::wstring fmt_time(const Clock::time_point&tp,bool valid){if(!valid)return L"-";auto tt=Clock::to_time_t(tp);tm lt{};localtime_s(&lt,&tt);std::wostringstream os;os<<std::put_time(&lt,L"%d/%m/%Y %H:%M:%S");return os.str();}
std::wstring fmt_elapsed(const Timing&t){if(!t.has_start)return L"-";auto end=t.has_finish?t.finished:Clock::now();auto sec=std::chrono::duration_cast<std::chrono::seconds>(end-t.started).count();if(sec<0)sec=0;auto h=sec/3600,m=(sec%3600)/60,s=sec%60;std::wostringstream os;if(h)os<<h<<L"h ";if(h||m)os<<m<<L"m ";os<<s<<L"s";return os.str();}

void register_bridge(){wchar_t exe[32768]{};GetModuleFileNameW(nullptr,exe,32768);auto dir=std::filesystem::path(exe).parent_path();auto bridge=dir/L"AIRIDMBridge.exe";if(!std::filesystem::exists(bridge))return;auto mf=dir/L"com.airi.downloadmanager.json";std::string p=u8(bridge.wstring()),esc;for(char c:p){if(c=='\\')esc+="\\\\";else if(c=='\"')esc+="\\\"";else esc+=c;}std::ofstream f(mf,std::ios::binary|std::ios::trunc);f<<"{\n\"name\":\"com.airi.downloadmanager\",\n\"description\":\"AIRI Native Bridge\",\n\"path\":\""<<esc<<"\",\n\"type\":\"stdio\",\n\"allowed_origins\":[\"chrome-extension://knmjnpnbngjeilejfdhccehndiengjan/\"]\n}";f.close();for(auto key:{L"Software\\Google\\Chrome\\NativeMessagingHosts\\com.airi.downloadmanager",L"Software\\Microsoft\\Edge\\NativeMessagingHosts\\com.airi.downloadmanager"}){HKEY h{};if(RegCreateKeyExW(HKEY_CURRENT_USER,key,0,nullptr,0,KEY_SET_VALUE,nullptr,&h,nullptr)==ERROR_SUCCESS){auto s=mf.wstring();RegSetValueExW(h,nullptr,0,REG_SZ,(BYTE*)s.c_str(),(DWORD)((s.size()+1)*sizeof(wchar_t)));RegCloseKey(h);}}}

struct AddData{std::wstring url,filename;int connections=16;};
INT_PTR CALLBACK AddDlg(HWND h,UINT m,WPARAM wp,LPARAM lp){auto*d=(AddData*)GetWindowLongPtrW(h,GWLP_USERDATA);if(m==WM_INITDIALOG){d=(AddData*)lp;SetWindowLongPtrW(h,GWLP_USERDATA,lp);auto c=GetDlgItem(h,IDC_CONNECTIONS);for(int v:{1,2,4,8,16,32,64}){auto s=std::to_wstring(v);SendMessageW(c,CB_ADDSTRING,0,(LPARAM)s.c_str());}SendMessageW(c,CB_SETCURSEL,4,0);SetFocus(GetDlgItem(h,IDC_URL));return FALSE;}if(m==WM_COMMAND){if(LOWORD(wp)==IDOK&&d){wchar_t url[8192]{},fn[1024]{};GetDlgItemTextW(h,IDC_URL,url,8192);GetDlgItemTextW(h,IDC_FILENAME,fn,1024);std::wstring s=url;if(s.rfind(L"http://",0)!=0&&s.rfind(L"https://",0)!=0){MessageBoxW(h,L"Enter a valid HTTP/HTTPS URL.",L"AIRI Download Manager",MB_ICONWARNING);return TRUE;}d->url=url;d->filename=fn;int idx=(int)SendDlgItemMessageW(h,IDC_CONNECTIONS,CB_GETCURSEL,0,0);int vals[]={1,2,4,8,16,32,64};d->connections=(idx>=0&&idx<7)?vals[idx]:16;EndDialog(h,IDOK);return TRUE;}if(LOWORD(wp)==IDCANCEL){EndDialog(h,IDCANCEL);return TRUE;}}return FALSE;}

std::uint64_t selected(){int row=ListView_GetNextItem(g_list,-1,LVNI_SELECTED);if(row<0)return 0;LVITEMW i{};i.mask=LVIF_PARAM;i.iItem=row;return ListView_GetItem(g_list,&i)?(std::uint64_t)i.lParam:0;}
bool media_id(std::uint64_t id){return id>=MEDIA_BASE;}
bool terminal(airi::DownloadStatus s){return s==airi::DownloadStatus::Completed||s==airi::DownloadStatus::Failed||s==airi::DownloadStatus::Cancelled;}

bool add_direct(const std::string&url,const std::string&filename={},const std::string&cookie={},const std::string&referer={},const std::string&origin={},const std::string&ua={},int conn=16){if(url.rfind("http://",0)&&url.rfind("https://",0)){notice(L"Invalid URL rejected.");return false;}airi::DownloadRequest r;r.url=url;r.filename=filename;r.save_directory=downloads();r.cookie=cookie;r.referer=referer;r.origin=origin;r.user_agent=ua;r.connections=std::clamp(conn,1,64);auto task=g_mgr->add(std::move(r));timing_start(task->snapshot().id);notice(L"Download queued in Turbo Engine.");return true;}

bool add_media(const std::string&raw){airi::MediaRequest r;r.page_url=airi::json::get_string(raw,"pageUrl").value_or(airi::json::get_string(raw,"url").value_or(""));r.title=airi::json::get_string(raw,"title").value_or("Media");r.quality=airi::json::get_string(raw,"quality").value_or("best");r.cookie=airi::json::get_string(raw,"cookie").value_or("");r.referer=airi::json::get_string(raw,"referer").value_or(r.page_url);r.user_agent=airi::json::get_string(raw,"userAgent").value_or("");r.save_directory=downloads();if(r.page_url.rfind("http://",0)&&r.page_url.rfind("https://",0)){notice(L"Invalid media URL.");return false;}auto id=g_media_next++;timing_start(id);bool available=airi::MediaWorker::available();{std::lock_guard lk(g_media_mtx);g_media.push_back({id,w(r.title),available?L"Preparing":L"Media engine missing",0,r.save_directory,false,!available});}if(!available){timing_finish(id);notice(L"Media engine missing. Reinstall AIRI.");return false;}notice(L"Media accepted; preparing silently...");std::thread([id,r=std::move(r)]()mutable{bool ok=airi::MediaWorker::download(r,[id](const airi::MediaProgress&p){{std::lock_guard lk(g_media_mtx);for(auto&j:g_media)if(j.id==id){j.status=w(p.status);j.progress=std::clamp(p.percent,0,100);j.done=p.status=="Completed";j.failed=p.status=="Failed";break;}}if(g_hwnd)PostMessageW(g_hwnd,WM_REFRESH,0,0);});timing_finish(id);notice(ok?L"Media download completed.":L"Media download failed; see status.");}).detach();return true;}

bool payload(const std::string&raw){auto a=airi::json::get_string(raw,"action").value_or("download");if(a=="ping")return true;if(a=="download"||a=="capture")return add_direct(airi::json::get_string(raw,"url").value_or(""),airi::json::get_string(raw,"filename").value_or(""),airi::json::get_string(raw,"cookie").value_or(""),airi::json::get_string(raw,"referer").value_or(""),airi::json::get_string(raw,"origin").value_or(""),airi::json::get_string(raw,"userAgent").value_or(""));if(a=="media_page")return add_media(raw);notice(L"Unsupported browser command.");return false;}

void buttons(){auto id=selected();auto t=(!id||media_id(id))?nullptr:g_mgr->find(id);EnableWindow(g_pause,t!=nullptr);EnableWindow(g_resume,t!=nullptr);EnableWindow(g_cancel,t!=nullptr);EnableWindow(g_open,TRUE);}
void setcell(int r,int c,const std::wstring&v){ListView_SetItemText(g_list,r,c,(LPWSTR)v.c_str());}

void refresh(){auto sel=selected();ListView_DeleteAllItems(g_list);int row=0,active=0;double speed=0;for(auto&s:g_mgr->snapshots()){if(terminal(s.status))timing_finish(s.id);LVITEMW i{};i.mask=LVIF_TEXT|LVIF_PARAM;i.iItem=row;auto n=w(s.filename.empty()?"download":s.filename);i.pszText=n.data();i.lParam=(LPARAM)s.id;int r=ListView_InsertItem(g_list,&i);setcell(r,1,w(airi::text::human_bytes(s.total_bytes)));setcell(r,2,w(airi::status_name(s.status)));setcell(r,3,std::to_wstring(s.progress_percent)+L"%");setcell(r,4,w(airi::text::human_bytes((unsigned long long)s.bytes_per_second))+L"/s");auto ext=s.output_path.extension().wstring();setcell(r,5,ext.empty()?L"Any type":ext);auto t=timing_get(s.id);setcell(r,6,fmt_time(t.started,t.has_start));setcell(r,7,fmt_time(t.finished,t.has_finish));setcell(r,8,fmt_elapsed(t));if(s.id==sel)ListView_SetItemState(g_list,r,LVIS_SELECTED|LVIS_FOCUSED,LVIS_SELECTED|LVIS_FOCUSED);if(s.status==airi::DownloadStatus::Downloading){++active;speed+=s.bytes_per_second;}++row;}
{std::lock_guard lk(g_media_mtx);for(auto&j:g_media){LVITEMW i{};i.mask=LVIF_TEXT|LVIF_PARAM;i.iItem=row;auto n=j.name;i.pszText=n.data();i.lParam=(LPARAM)j.id;int r=ListView_InsertItem(g_list,&i);setcell(r,2,j.status);setcell(r,3,std::to_wstring(j.progress)+L"%");setcell(r,5,L"Media");auto t=timing_get(j.id);setcell(r,6,fmt_time(t.started,t.has_start));setcell(r,7,fmt_time(t.finished,t.has_finish));setcell(r,8,fmt_elapsed(t));if(j.id==sel)ListView_SetItemState(g_list,r,LVIS_SELECTED|LVIS_FOCUSED,LVIS_SELECTED|LVIS_FOCUSED);if(!j.done&&!j.failed)++active;++row;}}
std::wstring nt;{std::lock_guard lk(g_notice_mtx);nt=g_notice;}auto st=L"AIRI Turbo v2.2  |  "+std::to_wstring(active)+L" active  |  "+w(airi::text::human_bytes((unsigned long long)speed))+L"/s  |  "+nt;SendMessageW(g_status,SB_SETTEXTW,0,(LPARAM)st.c_str());buttons();}

void open_folder(){auto id=selected();auto d=downloads();if(id&&!media_id(id)){if(auto t=g_mgr->find(id)){auto s=t->snapshot();if(!s.output_path.empty())d=s.output_path.parent_path();}}ShellExecuteW(g_hwnd,L"open",d.c_str(),nullptr,nullptr,SW_SHOWNORMAL);notice(L"Opened Downloads folder.");}

LRESULT CALLBACK Proc(HWND h,UINT m,WPARAM wp,LPARAM lp){switch(m){
case WM_CREATE:{g_hwnd=h;if(g_icon){SendMessageW(h,WM_SETICON,ICON_BIG,(LPARAM)g_icon);SendMessageW(h,WM_SETICON,ICON_SMALL,(LPARAM)g_icon);}auto title=CreateWindowW(L"STATIC",L"AIRI Download Manager",WS_CHILD|WS_VISIBLE,16,12,310,28,h,nullptr,nullptr,nullptr);SendMessageW(title,WM_SETFONT,(WPARAM)g_title_font,TRUE);auto sub=CreateWindowW(L"STATIC",L"Turbo Engine v2.2",WS_CHILD|WS_VISIBLE,18,40,220,20,h,nullptr,nullptr,nullptr);SendMessageW(sub,WM_SETFONT,(WPARAM)g_font,TRUE);auto mk=[&](HWND&out,int id,const wchar_t*t,int x,int ww){out=CreateWindowW(L"BUTTON",t,WS_CHILD|WS_VISIBLE|WS_TABSTOP|BS_PUSHBUTTON,x,18,ww,34,h,(HMENU)(INT_PTR)id,nullptr,nullptr);SendMessageW(out,WM_SETFONT,(WPARAM)g_font,TRUE);};mk(g_add,ID_ADD,L"+ Add URL",350,100);mk(g_resume,ID_RESUME,L"Resume",458,85);mk(g_pause,ID_PAUSE,L"Pause",551,80);mk(g_cancel,ID_CANCEL,L"Cancel",639,80);mk(g_open,ID_OPEN,L"Open folder",727,110);
g_list=CreateWindowExW(WS_EX_CLIENTEDGE,WC_LISTVIEWW,L"",WS_CHILD|WS_VISIBLE|LVS_REPORT|LVS_SINGLESEL|LVS_SHOWSELALWAYS,10,74,1200,560,h,(HMENU)(INT_PTR)ID_LIST,nullptr,nullptr);SendMessageW(g_list,WM_SETFONT,(WPARAM)g_font,TRUE);ListView_SetExtendedListViewStyle(g_list,LVS_EX_FULLROWSELECT|LVS_EX_DOUBLEBUFFER|LVS_EX_GRIDLINES);struct C{const wchar_t*t;int width;}cs[]={{L"Name",300},{L"Size",90},{L"Status",110},{L"Progress",80},{L"Speed",105},{L"Type",85},{L"Started",155},{L"Finished",155},{L"Elapsed",90}};for(int i=0;i<9;i++){LVCOLUMNW c{};c.mask=LVCF_TEXT|LVCF_WIDTH;c.pszText=(LPWSTR)cs[i].t;c.cx=cs[i].width;ListView_InsertColumn(g_list,i,&c);}g_status=CreateStatusWindowW(WS_CHILD|WS_VISIBLE,L"Ready",h,(HMENU)(INT_PTR)4001);SetTimer(h,1,500,nullptr);buttons();return 0;}
case WM_SIZE:{RECT r{};GetClientRect(h,&r);MoveWindow(g_list,10,74,(int)std::max<LONG>(100,r.right-20),(int)std::max<LONG>(100,r.bottom-109),TRUE);SendMessageW(g_status,WM_SIZE,0,0);return 0;}
case WM_TIMER:case WM_REFRESH:refresh();return 0;
case WM_COPYDATA:{auto*c=(COPYDATASTRUCT*)lp;if(!c||!c->lpData||!c->cbData)return FALSE;std::string raw((char*)c->lpData,c->cbData);if(!raw.empty()&&raw.back()=='\0')raw.pop_back();return payload(raw)?TRUE:FALSE;}
case WM_COMMAND:{int id=LOWORD(wp);if(id==ID_ADD&&HIWORD(wp)==BN_CLICKED){AddData d;auto r=DialogBoxParamW(GetModuleHandleW(nullptr),MAKEINTRESOURCEW(IDD_ADD_URL),h,AddDlg,(LPARAM)&d);if(r==IDOK)add_direct(u8(d.url),u8(d.filename),{},{},{},{},d.connections);else if(r==-1)MessageBoxW(h,L"Add URL dialog failed.",L"AIRI",MB_ICONERROR);return 0;}if(id==ID_OPEN&&HIWORD(wp)==BN_CLICKED){open_folder();return 0;}if((id==ID_PAUSE||id==ID_RESUME||id==ID_CANCEL)&&HIWORD(wp)==BN_CLICKED){auto sid=selected();if(!sid||media_id(sid)){notice(L"Select a direct download first.");return 0;}auto t=g_mgr->find(sid);if(!t)return 0;if(id==ID_PAUSE){t->pause();notice(L"Pause requested.");}else if(id==ID_RESUME){t->resume();notice(L"Resume requested.");}else{t->cancel();timing_finish(sid);notice(L"Cancel requested.");}return 0;}return 0;}
case WM_NOTIFY:{auto*n=(NMHDR*)lp;if(n&&n->idFrom==ID_LIST&&n->code==LVN_ITEMCHANGED)buttons();return 0;}
case WM_DESTROY:KillTimer(h,1);g_hwnd=nullptr;PostQuitMessage(0);return 0;
}return DefWindowProcW(h,m,wp,lp);}
}

int WINAPI wWinMain(HINSTANCE hi,HINSTANCE,PWSTR,int show){CoInitializeEx(nullptr,COINIT_APARTMENTTHREADED);INITCOMMONCONTROLSEX ic{sizeof(ic),ICC_LISTVIEW_CLASSES|ICC_BAR_CLASSES};InitCommonControlsEx(&ic);NONCLIENTMETRICSW n{sizeof(n)};SystemParametersInfoW(SPI_GETNONCLIENTMETRICS,sizeof(n),&n,0);g_font=CreateFontIndirectW(&n.lfMessageFont);LOGFONTW lf=n.lfMessageFont;lf.lfHeight=-22;lf.lfWeight=FW_BOLD;g_title_font=CreateFontIndirectW(&lf);g_icon=LoadIconW(hi,MAKEINTRESOURCEW(IDI_AIRI));g_mgr=std::make_unique<airi::DownloadManager>([](const airi::DownloadSnapshot&){if(g_hwnd)PostMessageW(g_hwnd,WM_REFRESH,0,0);});register_bridge();WNDCLASSEXW wc{sizeof(wc)};wc.lpfnWndProc=Proc;wc.hInstance=hi;wc.lpszClassName=kClass;wc.hCursor=LoadCursorW(nullptr,IDC_ARROW);wc.hIcon=g_icon?g_icon:LoadIconW(nullptr,IDI_APPLICATION);wc.hIconSm=wc.hIcon;wc.hbrBackground=(HBRUSH)(COLOR_WINDOW+1);RegisterClassExW(&wc);auto h=CreateWindowExW(0,kClass,L"AIRI Download Manager - Turbo v2.2",WS_OVERLAPPEDWINDOW|WS_CLIPCHILDREN,CW_USEDEFAULT,CW_USEDEFAULT,1320,760,nullptr,nullptr,hi,nullptr);if(!h)return 2;ShowWindow(h,show);UpdateWindow(h);MSG msg{};while(GetMessageW(&msg,nullptr,0,0)>0){if(!IsDialogMessageW(h,&msg)){TranslateMessage(&msg);DispatchMessageW(&msg);}}g_mgr.reset();DeleteObject(g_font);DeleteObject(g_title_font);CoUninitialize();return(int)msg.wParam;}
#endif

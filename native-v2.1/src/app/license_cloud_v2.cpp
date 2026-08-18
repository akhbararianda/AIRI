#ifdef _WIN32
#include "app/license_cloud.h"
#include "common/airi_json.h"
#include <windows.h>
#include <winhttp.h>
#include <bcrypt.h>
#include <algorithm>
#include <array>
#include <atomic>
#include <chrono>
#include <cwctype>
#include <filesystem>
#include <fstream>
#include <iomanip>
#include <sstream>
#include <string>
#include <thread>
#include <vector>

namespace airi::cloud { namespace {
constexpr wchar_t kRegPath[]=L"Software\\AIRI Technology\\AIRI Download Manager\\Cloud";
constexpr long long kGraceSeconds=7ll*24*60*60;
std::atomic_bool g_heartbeat_inflight{false};
std::atomic_bool g_activation_inflight{false};

long long now_sec(){return std::chrono::duration_cast<std::chrono::seconds>(std::chrono::system_clock::now().time_since_epoch()).count();}
std::wstring W(const std::string&s){if(s.empty())return{};int n=MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0);std::wstring o((std::size_t)n,L'\0');MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n);return o;}
std::string U8(const std::wstring&s){if(s.empty())return{};int n=WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0,nullptr,nullptr);std::string o((std::size_t)n,'\0');WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n,nullptr,nullptr);return o;}
std::wstring reg_read(const wchar_t*name){HKEY h{};if(RegOpenKeyExW(HKEY_CURRENT_USER,kRegPath,0,KEY_READ,&h)!=ERROR_SUCCESS)return{};DWORD type=0,bytes=0;if(RegQueryValueExW(h,name,nullptr,&type,nullptr,&bytes)!=ERROR_SUCCESS||type!=REG_SZ||bytes<sizeof(wchar_t)){RegCloseKey(h);return{};}std::wstring v(bytes/sizeof(wchar_t),L'\0');if(RegQueryValueExW(h,name,nullptr,nullptr,reinterpret_cast<BYTE*>(v.data()),&bytes)!=ERROR_SUCCESS){RegCloseKey(h);return{};}RegCloseKey(h);while(!v.empty()&&v.back()==L'\0')v.pop_back();return v;}
bool reg_write(const wchar_t*name,const std::wstring&v){HKEY h{};if(RegCreateKeyExW(HKEY_CURRENT_USER,kRegPath,0,nullptr,0,KEY_SET_VALUE,nullptr,&h,nullptr)!=ERROR_SUCCESS)return false;DWORD bytes=(DWORD)((v.size()+1)*sizeof(wchar_t));bool ok=RegSetValueExW(h,name,0,REG_SZ,reinterpret_cast<const BYTE*>(v.c_str()),bytes)==ERROR_SUCCESS;RegCloseKey(h);return ok;}
long long reg_i64(const wchar_t*name,long long fallback=0){try{auto s=reg_read(name);return s.empty()?fallback:std::stoll(s);}catch(...){return fallback;}}
bool reg_flag(const wchar_t*name){auto s=reg_read(name);return s==L"1"||s==L"true"||s==L"TRUE";}
std::filesystem::path appdir(){wchar_t p[32768]{};GetModuleFileNameW(nullptr,p,32768);return std::filesystem::path(p).parent_path();}
std::wstring trim(std::wstring s){while(!s.empty()&&iswspace(s.front()))s.erase(s.begin());while(!s.empty()&&iswspace(s.back()))s.pop_back();while(!s.empty()&&s.back()==L'/')s.pop_back();return s;}
std::wstring env(const wchar_t*name){DWORD n=GetEnvironmentVariableW(name,nullptr,0);if(!n)return{};std::wstring v(n,L'\0');GetEnvironmentVariableW(name,v.data(),n);while(!v.empty()&&v.back()==L'\0')v.pop_back();return v;}
std::wstring base_url_w(){auto e=trim(env(L"AIRI_LICENSE_CLOUD_URL"));if(!e.empty())return e;auto r=trim(reg_read(L"BaseUrl"));if(!r.empty())return r;auto p=appdir()/L"airi-cloud.url";std::wifstream f(p);std::wstring line;if(f&&std::getline(f,line))return trim(line);return{};}
bool enforcement(){auto e=env(L"AIRI_LICENSE_CLOUD_ENFORCE");if(!e.empty())return e==L"1"||e==L"true"||e==L"TRUE";if(reg_flag(L"Enforcement"))return true;return std::filesystem::exists(appdir()/L"airi-cloud.enforce");}

std::vector<unsigned char> sha256_bytes(const std::string&s){BCRYPT_ALG_HANDLE alg{};BCRYPT_HASH_HANDLE hash{};DWORD objLen=0,cb=0;std::vector<unsigned char>out(32);if(BCryptOpenAlgorithmProvider(&alg,BCRYPT_SHA256_ALGORITHM,nullptr,0)<0)return{};if(BCryptGetProperty(alg,BCRYPT_OBJECT_LENGTH,reinterpret_cast<PUCHAR>(&objLen),sizeof(objLen),&cb,0)<0){BCryptCloseAlgorithmProvider(alg,0);return{};}std::vector<unsigned char>obj(objLen);if(BCryptCreateHash(alg,&hash,obj.data(),objLen,nullptr,0,0)<0){BCryptCloseAlgorithmProvider(alg,0);return{};}if(BCryptHashData(hash,reinterpret_cast<PUCHAR>(const_cast<char*>(s.data())),(ULONG)s.size(),0)<0||BCryptFinishHash(hash,out.data(),(ULONG)out.size(),0)<0)out.clear();BCryptDestroyHash(hash);BCryptCloseAlgorithmProvider(alg,0);return out;}
std::string sha256_hex(const std::string&s){auto v=sha256_bytes(s);std::ostringstream os;os<<std::hex<<std::setfill('0');for(auto b:v)os<<std::setw(2)<<(int)b;return os.str();}
std::string random_install_id(){std::array<unsigned char,16>b{};if(BCryptGenRandom(nullptr,b.data(),(ULONG)b.size(),BCRYPT_USE_SYSTEM_PREFERRED_RNG)<0)return"AIRI-INSTALL-UNKNOWN";b[6]=(unsigned char)((b[6]&0x0f)|0x40);b[8]=(unsigned char)((b[8]&0x3f)|0x80);std::ostringstream os;os<<std::hex<<std::setfill('0');for(std::size_t i=0;i<b.size();++i){os<<std::setw(2)<<(int)b[i];if(i==3||i==5||i==7||i==9)os<<'-';}return os.str();}
std::string os_version(){OSVERSIONINFOW v{};v.dwOSVersionInfoSize=sizeof(v);BOOL ok=GetVersionExW(&v);if(!ok)return"Windows";return"Windows "+std::to_string(v.dwMajorVersion)+"."+std::to_string(v.dwMinorVersion)+" build "+std::to_string(v.dwBuildNumber);}
std::string architecture(){SYSTEM_INFO s{};GetNativeSystemInfo(&s);switch(s.wProcessorArchitecture){case PROCESSOR_ARCHITECTURE_AMD64:return"x64";case PROCESSOR_ARCHITECTURE_ARM64:return"arm64";case PROCESSOR_ARCHITECTURE_INTEL:return"x86";default:return"unknown";}}
std::string local_license_state(const airi::license::Info&i){switch(i.state){case airi::license::State::Trial:return"trial";case airi::license::State::Licensed:return"licensed";case airi::license::State::Expired:return"expired";case airi::license::State::ClockRollback:return"blocked";case airi::license::State::Invalid:return"unlicensed";}return"unlicensed";}

bool http_post(const std::wstring&base,const std::wstring&endpoint,const std::string&body,std::string&response,DWORD&status){std::wstring url=base+endpoint;URL_COMPONENTSW c{};c.dwStructSize=sizeof(c);c.dwSchemeLength=(DWORD)-1;c.dwHostNameLength=(DWORD)-1;c.dwUrlPathLength=(DWORD)-1;c.dwExtraInfoLength=(DWORD)-1;if(!WinHttpCrackUrl(url.c_str(),0,0,&c))return false;std::wstring host(c.lpszHostName,c.dwHostNameLength);std::wstring path(c.lpszUrlPath,c.dwUrlPathLength);if(c.dwExtraInfoLength)path.append(c.lpszExtraInfo,c.dwExtraInfoLength);if(path.empty())path=L"/";bool secure=c.nScheme==INTERNET_SCHEME_HTTPS;std::wstring lowerHost=host;std::transform(lowerHost.begin(),lowerHost.end(),lowerHost.begin(),[](wchar_t ch){return (wchar_t)towlower(ch);});if(!secure&&lowerHost!=L"localhost"&&lowerHost!=L"127.0.0.1")return false;HINTERNET session=WinHttpOpen(L"AIRI-Download-Manager/2.7",WINHTTP_ACCESS_TYPE_AUTOMATIC_PROXY,WINHTTP_NO_PROXY_NAME,WINHTTP_NO_PROXY_BYPASS,0);if(!session)return false;WinHttpSetTimeouts(session,5000,5000,8000,10000);HINTERNET connect=WinHttpConnect(session,host.c_str(),c.nPort,0);if(!connect){WinHttpCloseHandle(session);return false;}DWORD flags=secure?WINHTTP_FLAG_SECURE:0;HINTERNET req=WinHttpOpenRequest(connect,L"POST",path.c_str(),nullptr,WINHTTP_NO_REFERER,WINHTTP_DEFAULT_ACCEPT_TYPES,flags);if(!req){WinHttpCloseHandle(connect);WinHttpCloseHandle(session);return false;}const wchar_t*headers=L"Content-Type: application/json\r\nAccept: application/json\r\nCache-Control: no-store\r\n";BOOL ok=WinHttpSendRequest(req,headers,(DWORD)-1L,(LPVOID)body.data(),(DWORD)body.size(),(DWORD)body.size(),0)&&WinHttpReceiveResponse(req,nullptr);if(ok){DWORD len=sizeof(status);WinHttpQueryHeaders(req,WINHTTP_QUERY_STATUS_CODE|WINHTTP_QUERY_FLAG_NUMBER,WINHTTP_HEADER_NAME_BY_INDEX,&status,&len,WINHTTP_NO_HEADER_INDEX);for(;;){DWORD avail=0;if(!WinHttpQueryDataAvailable(req,&avail)||!avail)break;if(response.size()+avail>65536){ok=FALSE;break;}std::vector<char>buf(avail);DWORD got=0;if(!WinHttpReadData(req,buf.data(),avail,&got))break;response.append(buf.data(),got);}}WinHttpCloseHandle(req);WinHttpCloseHandle(connect);WinHttpCloseHandle(session);return ok==TRUE;}

void ensure_grace_start(){if(reg_i64(L"GraceStart")<=0)reg_write(L"GraceStart",std::to_wstring(now_sec()));}
void apply_response(const std::string&response,DWORD httpStatus){auto allowed=airi::json::get_bool(response,"allowed");auto status=airi::json::get_string(response,"status").value_or(httpStatus>=400?"denied":"ok");auto reason=airi::json::get_string(response,"reason").value_or("");reg_write(L"LastAttempt",std::to_wstring(now_sec()));reg_write(L"Status",W(status));reg_write(L"Reason",W(reason));if(allowed.has_value()){if(*allowed){reg_write(L"Blocked",L"0");reg_write(L"LastOk",std::to_wstring(now_sec()));}else reg_write(L"Blocked",L"1");}}
std::string device_body(const airi::license::Info&i,const std::string&version){return airi::json::flat_object({{"installation_id",installation_id()},{"machine_hash",machine_hash()},{"app_version",version},{"os_version",os_version()},{"architecture",architecture()},{"channel","commercial"},{"license_state",local_license_state(i)}});}

bool sync_activate(const std::string&licenseKey,const airi::license::Info&i,const std::string&version){auto base=base_url_w();if(base.empty()||licenseKey.empty())return false;std::string body=airi::json::flat_object({{"installation_id",installation_id()},{"machine_id",airi::license::machine_id()},{"license_key",licenseKey},{"app_version",version},{"os_version",os_version()},{"architecture",architecture()},{"channel","commercial"},{"license_state",local_license_state(i)}});std::string response;DWORD status=0;if(!http_post(base,L"/api/v1/license/activate",body,response,status)){reg_write(L"Reason",L"network_unreachable");reg_write(L"LastAttempt",std::to_wstring(now_sec()));return false;}apply_response(response,status);auto hash=airi::json::get_string(response,"license_hash");if(hash&&hash->size()==64)reg_write(L"LicenseHash",W(*hash));return status>=200&&status<300&&airi::json::get_bool(response,"allowed").value_or(false);}

void heartbeat_worker(airi::license::Info info,std::string version){if(g_heartbeat_inflight.exchange(true))return;struct Guard{~Guard(){g_heartbeat_inflight=false;}}guard;auto base=base_url_w();if(base.empty())return;ensure_grace_start();if(!reg_flag(L"Registered")){std::string response;DWORD status=0;if(http_post(base,L"/api/v1/install/register",device_body(info,version),response,status)){apply_response(response,status);if(status>=200&&status<300)reg_write(L"Registered",L"1");}}
if(info.state==airi::license::State::Licensed&&reg_read(L"LicenseHash").empty()){auto key=airi::license::stored_license_key();if(!key.empty())sync_activate(key,info,version);}
std::string body=airi::json::flat_object({{"installation_id",installation_id()},{"machine_hash",machine_hash()},{"license_hash",U8(reg_read(L"LicenseHash"))},{"app_version",version},{"os_version",os_version()},{"architecture",architecture()},{"channel","commercial"},{"license_state",local_license_state(info)}});std::string response;DWORD status=0;if(http_post(base,L"/api/v1/license/heartbeat",body,response,status))apply_response(response,status);else{reg_write(L"Reason",L"network_unreachable");reg_write(L"LastAttempt",std::to_wstring(now_sec()));}}
}

std::string installation_id(){auto s=U8(reg_read(L"InstallationId"));if(!s.empty())return s;s=random_install_id();reg_write(L"InstallationId",W(s));return s;}
std::string machine_hash(){return sha256_hex(airi::license::machine_id());}
std::string policy_name(Policy p){switch(p){case Policy::Disabled:return"Disabled";case Policy::Allowed:return"Connected";case Policy::Grace:return"Offline grace";case Policy::Blocked:return"Blocked";case Policy::Unknown:return"Unknown";}return"Unknown";}
Info current(){Info i;i.base_url=U8(base_url_w());i.configured=!i.base_url.empty();i.enforcement=enforcement();i.installation_id=installation_id();i.status=U8(reg_read(L"Status"));i.reason=U8(reg_read(L"Reason"));i.last_ok=reg_i64(L"LastOk");if(!i.configured){i.policy=Policy::Disabled;return i;}ensure_grace_start();auto graceStart=reg_i64(L"GraceStart");i.grace_until=(i.last_ok>0?i.last_ok:graceStart)+kGraceSeconds;if(reg_flag(L"Blocked")){i.policy=Policy::Blocked;return i;}if(!i.enforcement){i.policy=Policy::Allowed;return i;}auto now=now_sec();if(i.last_ok>0&&now-i.last_ok<=kGraceSeconds)i.policy=Policy::Allowed;else if(graceStart>0&&now-graceStart<=kGraceSeconds)i.policy=Policy::Grace;else{i.policy=Policy::Blocked;if(i.reason.empty())i.reason="offline_grace_expired";}return i;}
bool can_download(){auto i=current();return !(i.configured&&i.enforcement&&i.policy==Policy::Blocked);}
void heartbeat_async(const airi::license::Info&license,const std::string&version){std::thread(heartbeat_worker,license,version).detach();}
void activation_async(const std::string&licenseKey,const airi::license::Info&license,const std::string&version){if(g_activation_inflight.exchange(true))return;std::thread([licenseKey,license,version]{struct Guard{~Guard(){g_activation_inflight=false;}}guard;ensure_grace_start();sync_activate(licenseKey,license,version);}).detach();}
void report_event_async(const std::string&eventType,const std::string&detail){auto base=base_url_w();if(base.empty())return;std::thread([base,eventType,detail]{std::string body=airi::json::flat_object({{"installation_id",installation_id()},{"event_type",eventType},{"detail",detail}});std::string response;DWORD status=0;http_post(base,L"/api/v1/security/event",body,response,status);}).detach();}
}
#endif

#ifdef _WIN32
#define can_download airi_local_can_download
#include "licensing_v2.cpp"
#undef can_download
#include <windows.h>
#include <chrono>
#include <filesystem>
#include <fstream>
#include <string>

namespace airi::license { namespace {
constexpr wchar_t kCloudRegPath[]=L"Software\\AIRI Technology\\AIRI Download Manager\\Cloud";
constexpr long long kGraceSeconds=7ll*24*60*60;
std::wstring cloud_read(const wchar_t*name){HKEY h{};if(RegOpenKeyExW(HKEY_CURRENT_USER,kCloudRegPath,0,KEY_READ,&h)!=ERROR_SUCCESS)return{};DWORD type=0,bytes=0;if(RegQueryValueExW(h,name,nullptr,&type,nullptr,&bytes)!=ERROR_SUCCESS||type!=REG_SZ||bytes<sizeof(wchar_t)){RegCloseKey(h);return{};}std::wstring v(bytes/sizeof(wchar_t),L'\0');if(RegQueryValueExW(h,name,nullptr,nullptr,reinterpret_cast<BYTE*>(v.data()),&bytes)!=ERROR_SUCCESS){RegCloseKey(h);return{};}RegCloseKey(h);while(!v.empty()&&v.back()==L'\0')v.pop_back();return v;}
bool cloud_write(const wchar_t*name,const std::wstring&v){HKEY h{};if(RegCreateKeyExW(HKEY_CURRENT_USER,kCloudRegPath,0,nullptr,0,KEY_SET_VALUE,nullptr,&h,nullptr)!=ERROR_SUCCESS)return false;DWORD bytes=(DWORD)((v.size()+1)*sizeof(wchar_t));bool ok=RegSetValueExW(h,name,0,REG_SZ,reinterpret_cast<const BYTE*>(v.c_str()),bytes)==ERROR_SUCCESS;RegCloseKey(h);return ok;}
long long cloud_i64(const wchar_t*name){try{auto s=cloud_read(name);return s.empty()?0:std::stoll(s);}catch(...){return 0;}}
bool cloud_flag(const wchar_t*name){auto s=cloud_read(name);return s==L"1"||s==L"true"||s==L"TRUE";}
std::filesystem::path exe_dir(){wchar_t p[32768]{};GetModuleFileNameW(nullptr,p,32768);return std::filesystem::path(p).parent_path();}
bool cloud_enforced(){wchar_t b[32]{};DWORD n=GetEnvironmentVariableW(L"AIRI_LICENSE_CLOUD_ENFORCE",b,32);if(n>0){std::wstring v(b,n);return v==L"1"||v==L"true"||v==L"TRUE";}if(cloud_flag(L"Enforcement"))return true;return std::filesystem::exists(exe_dir()/L"airi-cloud.enforce");}
long long cloud_now(){return std::chrono::duration_cast<std::chrono::seconds>(std::chrono::system_clock::now().time_since_epoch()).count();}
bool cloud_policy_allows(){if(!cloud_enforced())return true;if(cloud_flag(L"Blocked"))return false;auto lastOk=cloud_i64(L"LastOk");auto graceStart=cloud_i64(L"GraceStart");auto now=cloud_now();if(graceStart<=0){graceStart=now;cloud_write(L"GraceStart",std::to_wstring(now));}if(lastOk>0&&now-lastOk<=kGraceSeconds)return true;if(now-graceStart<=kGraceSeconds)return true;return false;}
}
bool can_download(){auto i=current();if(i.state!=State::Trial&&i.state!=State::Licensed)return false;return cloud_policy_allows();}
}
#endif

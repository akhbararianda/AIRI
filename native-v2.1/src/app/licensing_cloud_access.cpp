#ifdef _WIN32
#include "app/licensing.h"
#include <windows.h>
#include <string>
namespace airi::license {
namespace {
constexpr wchar_t kRegPath[]=L"Software\\AIRI Technology\\AIRI Download Manager\\License";
std::string U8(const std::wstring&s){if(s.empty())return{};int n=WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0,nullptr,nullptr);std::string o((std::size_t)n,'\0');WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n,nullptr,nullptr);return o;}
}
std::string stored_license_key(){HKEY h{};if(RegOpenKeyExW(HKEY_CURRENT_USER,kRegPath,0,KEY_READ,&h)!=ERROR_SUCCESS)return{};DWORD type=0,bytes=0;if(RegQueryValueExW(h,L"LicenseKey",nullptr,&type,nullptr,&bytes)!=ERROR_SUCCESS||type!=REG_SZ||bytes<sizeof(wchar_t)){RegCloseKey(h);return{};}std::wstring v(bytes/sizeof(wchar_t),L'\0');if(RegQueryValueExW(h,L"LicenseKey",nullptr,nullptr,reinterpret_cast<BYTE*>(v.data()),&bytes)!=ERROR_SUCCESS){RegCloseKey(h);return{};}RegCloseKey(h);while(!v.empty()&&v.back()==L'\0')v.pop_back();return U8(v);}
}
#endif

#ifdef _WIN32
#include "app/licensing.h"
#include "common/airi_json.h"
#include <windows.h>
#include <bcrypt.h>
#include <algorithm>
#include <array>
#include <chrono>
#include <cctype>
#include <cstdint>
#include <cstring>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>
#pragma comment(lib,"bcrypt.lib")

namespace airi::license { namespace {
constexpr wchar_t kRegPath[]=L"Software\\AIRI Technology\\AIRI Download Manager\\License";
constexpr std::int64_t kTrialSeconds=14ll*24*60*60;
constexpr std::int64_t kRollbackTolerance=6ll*60*60;
const unsigned char kRsaModulus[256]={
0x94,0x1e,0xc5,0x9f,0x49,0xca,0xae,0x2e,0x18,0x89,0x15,0x75,0x97,0xe4,0x57,0x2f,
0x6d,0xeb,0x94,0x7d,0xea,0x2c,0x04,0xe1,0x3a,0xf7,0x59,0x9c,0x2b,0xd0,0xe1,0x85,
0x1c,0x5a,0x4f,0x28,0xe8,0xe9,0xac,0xf1,0xb1,0x8a,0x52,0xc5,0xb0,0x39,0xd4,0x86,
0x2b,0x92,0x56,0xc3,0x4d,0x24,0xa3,0x36,0x1e,0x7c,0xc8,0xc0,0x0d,0x91,0x4c,0x22,
0x38,0xd3,0xbd,0xed,0x02,0x5b,0x3d,0x7d,0x9d,0xdd,0xcc,0x2e,0x19,0x60,0xe5,0x70,
0x43,0xc9,0x3b,0x56,0xa3,0x9b,0x13,0x4a,0x63,0xc5,0xc0,0x45,0xb1,0xa3,0xe6,0xd6,
0x45,0xc0,0x40,0x03,0x1d,0x85,0x99,0xd9,0x8e,0xaa,0x2c,0x81,0x20,0xfe,0x37,0xe4,
0xb1,0x50,0x1d,0x7a,0x04,0x12,0xa0,0xdd,0xca,0x77,0x5a,0xd1,0x14,0x6c,0x57,0xa9,
0x84,0x2c,0xde,0x2f,0x1d,0x2a,0x3c,0xd9,0x37,0x07,0x70,0x4d,0xd0,0xb4,0xde,0x1f,
0xcf,0x10,0x31,0x7b,0xb9,0xc0,0x30,0xef,0x4b,0x07,0xe4,0xdb,0xe8,0xfb,0xac,0x50,
0x8f,0xcd,0x75,0x8a,0x95,0xd5,0xc9,0xcf,0x1f,0xde,0x13,0x7e,0xe3,0xe5,0x72,0x24,
0xa1,0xf9,0xe7,0x81,0x45,0xf4,0x1b,0xa3,0x93,0xef,0x12,0xde,0xc7,0x65,0x47,0x02,
0xf4,0xe3,0x02,0xe0,0x9b,0x75,0x4b,0x27,0xea,0xe0,0xe5,0xe1,0xeb,0x55,0x1c,0x20,
0xdb,0x7e,0xcb,0x33,0xbe,0xbc,0x7d,0xab,0x30,0x5d,0x5f,0x56,0xe2,0xd8,0xfd,0x4b,
0xcd,0x21,0x5e,0x14,0xe5,0x21,0xdc,0xed,0x19,0x6c,0x8f,0x2f,0x66,0xf8,0x06,0xaf,
0x2a,0x7c,0xeb,0xbe,0xf3,0x0d,0xa3,0x36,0xd5,0x46,0x33,0x56,0x04,0x9a,0x1c,0xa9};
const unsigned char kRsaExponent[3]={0x01,0x00,0x01};

std::int64_t now_sec(){return std::chrono::duration_cast<std::chrono::seconds>(std::chrono::system_clock::now().time_since_epoch()).count();}
std::wstring W(const std::string&s){if(s.empty())return{};int n=MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0);std::wstring o(n,L'\0');MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n);return o;}
std::string U8(const std::wstring&s){if(s.empty())return{};int n=WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0,nullptr,nullptr);std::string o(n,'\0');WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),o.data(),n,nullptr,nullptr);return o;}
std::wstring reg_read(const wchar_t*name){HKEY h{};if(RegOpenKeyExW(HKEY_CURRENT_USER,kRegPath,0,KEY_READ,&h)!=ERROR_SUCCESS)return{};DWORD type=0,bytes=0;if(RegQueryValueExW(h,name,nullptr,&type,nullptr,&bytes)!=ERROR_SUCCESS||type!=REG_SZ||bytes<sizeof(wchar_t)){RegCloseKey(h);return{};}std::wstring v(bytes/sizeof(wchar_t),L'\0');if(RegQueryValueExW(h,name,nullptr,nullptr,(BYTE*)v.data(),&bytes)!=ERROR_SUCCESS){RegCloseKey(h);return{};}RegCloseKey(h);while(!v.empty()&&v.back()==L'\0')v.pop_back();return v;}
bool reg_write(const wchar_t*name,const std::wstring&value){HKEY h{};if(RegCreateKeyExW(HKEY_CURRENT_USER,kRegPath,0,nullptr,0,KEY_SET_VALUE,nullptr,&h,nullptr)!=ERROR_SUCCESS)return false;auto bytes=(DWORD)((value.size()+1)*sizeof(wchar_t));auto ok=RegSetValueExW(h,name,0,REG_SZ,(const BYTE*)value.c_str(),bytes)==ERROR_SUCCESS;RegCloseKey(h);return ok;}
std::int64_t parse_i64(const std::wstring&s,std::int64_t fallback=0){try{return std::stoll(s);}catch(...){return fallback;}}
std::int64_t parse_i64s(const std::string&s,std::int64_t fallback=0){try{return std::stoll(s);}catch(...){return fallback;}}
std::vector<unsigned char> sha256(const unsigned char*data,std::size_t size){BCRYPT_ALG_HANDLE alg{};BCRYPT_HASH_HANDLE hash{};std::vector<unsigned char>out(32);if(BCryptOpenAlgorithmProvider(&alg,BCRYPT_SHA256_ALGORITHM,nullptr,0)<0)return{};DWORD objLen=0,cb=0;if(BCryptGetProperty(alg,BCRYPT_OBJECT_LENGTH,(PUCHAR)&objLen,sizeof(objLen),&cb,0)<0){BCryptCloseAlgorithmProvider(alg,0);return{};}std::vector<unsigned char>obj(objLen);if(BCryptCreateHash(alg,&hash,obj.data(),objLen,nullptr,0,0)<0){BCryptCloseAlgorithmProvider(alg,0);return{};}if(BCryptHashData(hash,(PUCHAR)data,(ULONG)size,0)<0||BCryptFinishHash(hash,out.data(),(ULONG)out.size(),0)<0)out.clear();BCryptDestroyHash(hash);BCryptCloseAlgorithmProvider(alg,0);return out;}
std::vector<unsigned char> sha256(const std::string&s){return sha256((const unsigned char*)s.data(),s.size());}
std::string hex_upper(const std::vector<unsigned char>&v,std::size_t n){std::ostringstream os;os<<std::uppercase<<std::hex<<std::setfill('0');for(std::size_t i=0;i<std::min(n,v.size());++i)os<<std::setw(2)<<(int)v[i];return os.str();}
std::string raw_machine_source(){wchar_t buf[512]{};DWORD bytes=sizeof(buf),type=0;HKEY h{};if(RegOpenKeyExW(HKEY_LOCAL_MACHINE,L"SOFTWARE\\Microsoft\\Cryptography",0,KEY_READ|KEY_WOW64_64KEY,&h)==ERROR_SUCCESS){if(RegQueryValueExW(h,L"MachineGuid",nullptr,&type,(BYTE*)buf,&bytes)==ERROR_SUCCESS&&type==REG_SZ){RegCloseKey(h);return U8(buf);}RegCloseKey(h);}DWORD n=512;if(GetComputerNameW(buf,&n))return U8(buf);return "unknown-machine";}
std::vector<unsigned char> b64url_decode(std::string s){for(char&c:s){if(c=='-')c='+';else if(c=='_')c='/';}while(s.size()%4)s.push_back('=');static const std::string table="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";std::array<int,256>map{};map.fill(-1);for(int i=0;i<64;i++)map[(unsigned char)table[i]]=i;std::vector<unsigned char>out;unsigned int val=0;int bits=-8;for(unsigned char c:s){if(c=='=')break;int d=map[c];if(d<0)return{};val=(val<<6)|(unsigned)d;bits+=6;if(bits>=0){out.push_back((unsigned char)((val>>bits)&0xff));bits-=8;}}return out;}
bool verify_signature(const std::string&payload,const std::vector<unsigned char>&sig){auto digest=sha256(payload);if(digest.size()!=32||sig.empty())return false;BCRYPT_ALG_HANDLE alg{};BCRYPT_KEY_HANDLE key{};if(BCryptOpenAlgorithmProvider(&alg,BCRYPT_RSA_ALGORITHM,nullptr,0)<0)return false;BCRYPT_RSAKEY_BLOB hdr{};hdr.Magic=BCRYPT_RSAPUBLIC_MAGIC;hdr.BitLength=2048;hdr.cbPublicExp=sizeof(kRsaExponent);hdr.cbModulus=sizeof(kRsaModulus);std::vector<unsigned char>blob(sizeof(hdr)+sizeof(kRsaExponent)+sizeof(kRsaModulus));memcpy(blob.data(),&hdr,sizeof(hdr));memcpy(blob.data()+sizeof(hdr),kRsaExponent,sizeof(kRsaExponent));memcpy(blob.data()+sizeof(hdr)+sizeof(kRsaExponent),kRsaModulus,sizeof(kRsaModulus));bool ok=false;if(BCryptImportKeyPair(alg,nullptr,BCRYPT_RSAPUBLIC_BLOB,&key,blob.data(),(ULONG)blob.size(),0)>=0){BCRYPT_PKCS1_PADDING_INFO pad{BCRYPT_SHA256_ALGORITHM};ok=BCryptVerifySignature(key,&pad,digest.data(),(ULONG)digest.size(),(PUCHAR)sig.data(),(ULONG)sig.size(),BCRYPT_PAD_PKCS1)>=0;BCryptDestroyKey(key);}BCryptCloseAlgorithmProvider(alg,0);return ok;}
bool validate_key(const std::string&license,Info&info,std::string&error){auto p1=license.find('.'),p2=p1==std::string::npos?std::string::npos:license.find('.',p1+1);if(p1==std::string::npos||p2==std::string::npos||license.substr(0,p1)!="AIRI1"){error="License format is invalid.";return false;}auto payloadBytes=b64url_decode(license.substr(p1+1,p2-p1-1));auto sig=b64url_decode(license.substr(p2+1));if(payloadBytes.empty()||sig.empty()){error="License encoding is invalid.";return false;}std::string payload(payloadBytes.begin(),payloadBytes.end());if(!verify_signature(payload,sig)){error="License signature is invalid.";return false;}auto product=json::get_string(payload,"product").value_or("");auto machine=json::get_string(payload,"machine").value_or("");auto customer=json::get_string(payload,"customer").value_or("");auto edition=json::get_string(payload,"edition").value_or("Pro");auto expiry=parse_i64s(json::get_string(payload,"expiry").value_or("0"));if(product!="AIRI-DM"){error="License is for another product.";return false;}auto mid=machine_id();if(machine!="*"&&machine!=mid){error="License is registered to another device.";return false;}if(expiry>0&&now_sec()>expiry){error="License has expired.";return false;}info.state=State::Licensed;info.days_left=-1;info.edition=edition;info.customer=customer;info.machine_id=mid;info.message="Licensed to "+(customer.empty()?std::string("customer"):customer);return true;}
}

std::string machine_id(){auto h=sha256("AIRI-DM|"+raw_machine_source());auto x=hex_upper(h,12);if(x.size()<24)return "AIRI-UNKNOWN";return "AIRI-"+x.substr(0,4)+"-"+x.substr(4,4)+"-"+x.substr(8,4)+"-"+x.substr(12,4)+"-"+x.substr(16,4)+"-"+x.substr(20,4);}
Info current(){Info info;info.machine_id=machine_id();auto stored=U8(reg_read(L"LicenseKey"));if(!stored.empty()){std::string error;if(validate_key(stored,info,error))return info;}auto now=now_sec();auto first=parse_i64(reg_read(L"FirstRun"));auto last=parse_i64(reg_read(L"LastSeen"));if(first<=0){first=now;reg_write(L"FirstRun",std::to_wstring(first));}if(last<=0){last=now;reg_write(L"LastSeen",std::to_wstring(last));}if(now+kRollbackTolerance<last){info.state=State::ClockRollback;info.days_left=0;info.edition="Trial";info.message="System clock moved backwards. Correct the clock or activate a license.";return info;}if(now>last)reg_write(L"LastSeen",std::to_wstring(now));auto remain=(first+kTrialSeconds)-now;if(remain<=0){info.state=State::Expired;info.days_left=0;info.edition="Trial";info.message="14-day trial has expired.";return info;}info.state=State::Trial;info.days_left=(int)((remain+86399)/86400);info.edition="Trial";info.message="Full-feature trial";return info;}
bool can_download(){auto i=current();return i.state==State::Trial||i.state==State::Licensed;}
bool activate(const std::string&license_key,std::string&error){Info info;if(!validate_key(license_key,info,error))return false;if(!reg_write(L"LicenseKey",W(license_key))){error="Could not save license to Windows user profile.";return false;}return true;}
const char* state_name(State s){switch(s){case State::Trial:return"Trial";case State::Licensed:return"Licensed";case State::Expired:return"Expired";case State::ClockRollback:return"Clock rollback";case State::Invalid:return"Invalid";}return"Invalid";}
}
#endif

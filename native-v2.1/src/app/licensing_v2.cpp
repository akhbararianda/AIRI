#ifdef _WIN32
#include "app/licensing.h"
#include "common/airi_json.h"
#include <windows.h>
#include <bcrypt.h>
#include <algorithm>
#include <array>
#include <chrono>
#include <cstdint>
#include <cstring>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>

namespace airi::license { namespace {
constexpr wchar_t kRegPath[]=L"Software\\AIRI Technology\\AIRI Download Manager\\License";
constexpr std::int64_t kTrialSeconds=14ll*24*60*60;
constexpr std::int64_t kRollbackTolerance=6ll*60*60;
const unsigned char kRsaModulus[256]={
0xd0,0xf5,0xe8,0xc0,0x0a,0x82,0x8b,0x71,0xe6,0x7d,0xfc,0x4e,0x63,0x78,0x31,0x8c,
0xeb,0x4c,0x79,0xd7,0xc6,0xc6,0x4d,0x55,0x90,0x18,0xb1,0x2e,0x91,0x8b,0x33,0xff,
0x22,0x1c,0xfa,0x82,0x9f,0x35,0x88,0x51,0x46,0xd6,0x2d,0xf9,0xe8,0xe7,0xc4,0x02,
0x8f,0xac,0x56,0x3c,0x3c,0x18,0x0c,0x63,0xf6,0xec,0x06,0x5d,0x3c,0x22,0x14,0x1f,
0x45,0x23,0x87,0x0c,0x29,0xaa,0xc9,0xc6,0x8b,0x3c,0xbb,0xda,0x12,0x50,0x64,0x1f,
0x6a,0x1a,0x0e,0x3e,0xf0,0xc1,0xc0,0x16,0xf0,0x78,0xd2,0xb2,0xe1,0xbb,0xe8,0xcf,
0x8d,0xff,0x62,0x19,0x6c,0xfc,0x64,0xe4,0xea,0x87,0x25,0x9b,0xf7,0x39,0xc7,0x97,
0x9d,0x1f,0xfd,0xda,0x45,0x6a,0xd3,0x12,0xda,0x48,0x39,0x28,0x11,0xfc,0x2b,0x04,
0x0a,0x0f,0x90,0x15,0xf2,0x35,0xbe,0x24,0x8b,0x60,0x7d,0xc4,0xaf,0x37,0x81,0xb2,
0xb1,0x25,0x79,0xdc,0xe2,0x18,0xd2,0xb4,0xfa,0xb8,0xfe,0x90,0x9e,0x9e,0x4c,0x8f,
0xa7,0x3e,0x51,0x66,0x91,0x81,0xbb,0x96,0xdb,0x09,0x8c,0xf5,0x62,0xf6,0x73,0x7c,
0x61,0x44,0xca,0x1d,0x99,0xd9,0x0b,0xa7,0x10,0x7e,0x7a,0x2e,0xc9,0xe0,0x31,0xc3,
0x11,0xc9,0x47,0xa1,0xa4,0xd9,0x01,0x71,0x82,0x23,0x7e,0x7c,0xfa,0x26,0x1c,0x07,
0x9a,0x13,0x53,0x31,0x83,0x7e,0xb9,0x25,0x5e,0x85,0x79,0x5b,0xca,0x6a,0x24,0x41,
0xd4,0x02,0x67,0xd9,0x37,0xee,0x62,0x39,0x4d,0xac,0x05,0x37,0xa3,0x27,0x5b,0x31,
0x7d,0xce,0xef,0x32,0x1e,0x87,0x5c,0x8a,0x86,0xaf,0x70,0x58,0x64,0x3b,0xe2,0x15};
const unsigned char kRsaExponent[3]={0x01,0x00,0x01};

std::int64_t now_sec(){return std::chrono::duration_cast<std::chrono::seconds>(std::chrono::system_clock::now().time_since_epoch()).count();}
std::wstring W(const std::string&s){if(s.empty())return{};int n=MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0);std::wstring out((std::size_t)n,L'\0');MultiByteToWideChar(CP_UTF8,0,s.data(),(int)s.size(),out.data(),n);return out;}
std::string U8(const std::wstring&s){if(s.empty())return{};int n=WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),nullptr,0,nullptr,nullptr);std::string out((std::size_t)n,'\0');WideCharToMultiByte(CP_UTF8,0,s.data(),(int)s.size(),out.data(),n,nullptr,nullptr);return out;}
std::wstring reg_read(const wchar_t*name){HKEY h{};if(RegOpenKeyExW(HKEY_CURRENT_USER,kRegPath,0,KEY_READ,&h)!=ERROR_SUCCESS)return{};DWORD type=0,bytes=0;if(RegQueryValueExW(h,name,nullptr,&type,nullptr,&bytes)!=ERROR_SUCCESS||type!=REG_SZ||bytes<sizeof(wchar_t)){RegCloseKey(h);return{};}std::wstring v(bytes/sizeof(wchar_t),L'\0');if(RegQueryValueExW(h,name,nullptr,nullptr,reinterpret_cast<BYTE*>(v.data()),&bytes)!=ERROR_SUCCESS){RegCloseKey(h);return{};}RegCloseKey(h);while(!v.empty()&&v.back()==L'\0')v.pop_back();return v;}
bool reg_write(const wchar_t*name,const std::wstring&value){HKEY h{};if(RegCreateKeyExW(HKEY_CURRENT_USER,kRegPath,0,nullptr,0,KEY_SET_VALUE,nullptr,&h,nullptr)!=ERROR_SUCCESS)return false;DWORD bytes=(DWORD)((value.size()+1)*sizeof(wchar_t));bool ok=RegSetValueExW(h,name,0,REG_SZ,reinterpret_cast<const BYTE*>(value.c_str()),bytes)==ERROR_SUCCESS;RegCloseKey(h);return ok;}
std::int64_t parse64(const std::wstring&s,std::int64_t fallback=0){try{return std::stoll(s);}catch(...){return fallback;}}
std::int64_t parse64(const std::string&s,std::int64_t fallback=0){try{return std::stoll(s);}catch(...){return fallback;}}

std::vector<unsigned char> sha256(const unsigned char*data,std::size_t size){BCRYPT_ALG_HANDLE alg{};BCRYPT_HASH_HANDLE hash{};DWORD objLen=0,cb=0;std::vector<unsigned char>out(32);if(BCryptOpenAlgorithmProvider(&alg,BCRYPT_SHA256_ALGORITHM,nullptr,0)<0)return{};if(BCryptGetProperty(alg,BCRYPT_OBJECT_LENGTH,reinterpret_cast<PUCHAR>(&objLen),sizeof(objLen),&cb,0)<0){BCryptCloseAlgorithmProvider(alg,0);return{};}std::vector<unsigned char>obj(objLen);if(BCryptCreateHash(alg,&hash,obj.data(),objLen,nullptr,0,0)<0){BCryptCloseAlgorithmProvider(alg,0);return{};}if(BCryptHashData(hash,const_cast<PUCHAR>(data),(ULONG)size,0)<0||BCryptFinishHash(hash,out.data(),(ULONG)out.size(),0)<0)out.clear();BCryptDestroyHash(hash);BCryptCloseAlgorithmProvider(alg,0);return out;}
std::vector<unsigned char> sha256(const std::string&s){return sha256(reinterpret_cast<const unsigned char*>(s.data()),s.size());}
std::string hex_upper(const std::vector<unsigned char>&v,std::size_t n){std::ostringstream os;os<<std::uppercase<<std::hex<<std::setfill('0');for(std::size_t i=0;i<std::min(n,v.size());++i)os<<std::setw(2)<<(int)v[i];return os.str();}
std::string raw_machine_source(){wchar_t buf[512]{};DWORD bytes=sizeof(buf),type=0;HKEY h{};if(RegOpenKeyExW(HKEY_LOCAL_MACHINE,L"SOFTWARE\\Microsoft\\Cryptography",0,KEY_READ|KEY_WOW64_64KEY,&h)==ERROR_SUCCESS){if(RegQueryValueExW(h,L"MachineGuid",nullptr,&type,reinterpret_cast<BYTE*>(buf),&bytes)==ERROR_SUCCESS&&type==REG_SZ){RegCloseKey(h);return U8(buf);}RegCloseKey(h);}DWORD n=512;if(GetComputerNameW(buf,&n))return U8(buf);return"unknown-machine";}

std::vector<unsigned char> b64url_decode(std::string s){for(char&c:s){if(c=='-')c='+';else if(c=='_')c='/';}while(s.size()%4)s.push_back('=');static const std::string table="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";std::array<int,256>map{};map.fill(-1);for(int i=0;i<64;++i)map[(unsigned char)table[(std::size_t)i]]=i;std::vector<unsigned char>out;unsigned int val=0;int bits=-8;for(unsigned char c:s){if(c=='=')break;int d=map[c];if(d<0)return{};val=(val<<6)|(unsigned)d;bits+=6;if(bits>=0){out.push_back((unsigned char)((val>>bits)&0xff));bits-=8;}}return out;}
bool verify_signature(const std::string&payload,const std::vector<unsigned char>&sig){auto digest=sha256(payload);if(digest.size()!=32||sig.empty())return false;BCRYPT_ALG_HANDLE alg{};BCRYPT_KEY_HANDLE key{};if(BCryptOpenAlgorithmProvider(&alg,BCRYPT_RSA_ALGORITHM,nullptr,0)<0)return false;BCRYPT_RSAKEY_BLOB hdr{};hdr.Magic=BCRYPT_RSAPUBLIC_MAGIC;hdr.BitLength=2048;hdr.cbPublicExp=(ULONG)sizeof(kRsaExponent);hdr.cbModulus=(ULONG)sizeof(kRsaModulus);std::vector<unsigned char>blob(sizeof(hdr)+sizeof(kRsaExponent)+sizeof(kRsaModulus));std::memcpy(blob.data(),&hdr,sizeof(hdr));std::memcpy(blob.data()+sizeof(hdr),kRsaExponent,sizeof(kRsaExponent));std::memcpy(blob.data()+sizeof(hdr)+sizeof(kRsaExponent),kRsaModulus,sizeof(kRsaModulus));bool ok=false;if(BCryptImportKeyPair(alg,nullptr,BCRYPT_RSAPUBLIC_BLOB,&key,blob.data(),(ULONG)blob.size(),0)>=0){BCRYPT_PKCS1_PADDING_INFO pad{BCRYPT_SHA256_ALGORITHM};ok=BCryptVerifySignature(key,&pad,digest.data(),(ULONG)digest.size(),const_cast<PUCHAR>(sig.data()),(ULONG)sig.size(),BCRYPT_PAD_PKCS1)>=0;BCryptDestroyKey(key);}BCryptCloseAlgorithmProvider(alg,0);return ok;}

bool validate_key(const std::string&license,Info&info,std::string&error){auto p1=license.find('.');auto p2=p1==std::string::npos?std::string::npos:license.find('.',p1+1);if(p1==std::string::npos||p2==std::string::npos||license.substr(0,p1)!="AIRI1"){error="License format is invalid.";return false;}auto payloadBytes=b64url_decode(license.substr(p1+1,p2-p1-1));auto sig=b64url_decode(license.substr(p2+1));if(payloadBytes.empty()||sig.empty()){error="License encoding is invalid.";return false;}std::string payload(payloadBytes.begin(),payloadBytes.end());if(!verify_signature(payload,sig)){error="License signature is invalid.";return false;}auto product=json::get_string(payload,"product").value_or("");auto machine=json::get_string(payload,"machine").value_or("");auto customer=json::get_string(payload,"customer").value_or("");auto edition=json::get_string(payload,"edition").value_or("Pro");auto expiry=parse64(json::get_string(payload,"expiry").value_or("0"));if(product!="AIRI-DM"){error="License is for another product.";return false;}auto mid=machine_id();if(machine!="*"&&machine!=mid){error="License is registered to another device.";return false;}if(expiry>0&&now_sec()>expiry){error="License has expired.";return false;}info.state=State::Licensed;info.days_left=-1;info.edition=edition;info.customer=customer;info.machine_id=mid;info.message="Licensed to "+(customer.empty()?std::string("customer"):customer);return true;}
}

std::string machine_id(){auto h=sha256("AIRI-DM|"+raw_machine_source());auto x=hex_upper(h,12);if(x.size()<24)return"AIRI-UNKNOWN";return"AIRI-"+x.substr(0,4)+"-"+x.substr(4,4)+"-"+x.substr(8,4)+"-"+x.substr(12,4)+"-"+x.substr(16,4)+"-"+x.substr(20,4);}
Info current(){Info info;info.machine_id=machine_id();auto stored=U8(reg_read(L"LicenseKey"));if(!stored.empty()){std::string error;if(validate_key(stored,info,error))return info;}auto now=now_sec();auto first=parse64(reg_read(L"FirstRun"));auto last=parse64(reg_read(L"LastSeen"));if(first<=0){first=now;reg_write(L"FirstRun",std::to_wstring(first));}if(last<=0){last=now;reg_write(L"LastSeen",std::to_wstring(last));}if(now+kRollbackTolerance<last){info.state=State::ClockRollback;info.days_left=0;info.edition="Trial";info.message="System clock moved backwards. Correct the clock or activate a license.";return info;}if(now>last)reg_write(L"LastSeen",std::to_wstring(now));auto remain=(first+kTrialSeconds)-now;if(remain<=0){info.state=State::Expired;info.days_left=0;info.edition="Trial";info.message="14-day trial has expired.";return info;}info.state=State::Trial;info.days_left=(int)((remain+86399)/86400);info.edition="Trial";info.message="Full-feature trial";return info;}
bool can_download(){auto i=current();return i.state==State::Trial||i.state==State::Licensed;}
bool activate(const std::string&license_key,std::string&error){Info info;if(!validate_key(license_key,info,error))return false;if(!reg_write(L"LicenseKey",W(license_key))){error="Could not save license to Windows user profile.";return false;}return true;}
const char* state_name(State s){switch(s){case State::Trial:return"Trial";case State::Licensed:return"Licensed";case State::Expired:return"Expired";case State::ClockRollback:return"Clock rollback";case State::Invalid:return"Invalid";}return"Invalid";}
}
#endif

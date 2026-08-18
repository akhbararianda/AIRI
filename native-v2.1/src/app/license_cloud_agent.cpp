#ifdef _WIN32
#include "app/license_cloud.h"
#include "app/licensing.h"
#include <windows.h>
#include <bcrypt.h>
#include <array>
#include <chrono>
#include <filesystem>
#include <fstream>
#include <iomanip>
#include <sstream>
#include <string>
#include <thread>
#include <vector>

namespace {
std::filesystem::path self_path(){wchar_t p[32768]{};GetModuleFileNameW(nullptr,p,32768);return std::filesystem::path(p);}
std::string self_sha256(){auto p=self_path();std::ifstream f(p,std::ios::binary);if(!f)return{};BCRYPT_ALG_HANDLE alg{};BCRYPT_HASH_HANDLE hash{};DWORD objLen=0,cb=0;if(BCryptOpenAlgorithmProvider(&alg,BCRYPT_SHA256_ALGORITHM,nullptr,0)<0)return{};if(BCryptGetProperty(alg,BCRYPT_OBJECT_LENGTH,reinterpret_cast<PUCHAR>(&objLen),sizeof(objLen),&cb,0)<0){BCryptCloseAlgorithmProvider(alg,0);return{};}std::vector<unsigned char>obj(objLen),digest(32);if(BCryptCreateHash(alg,&hash,obj.data(),objLen,nullptr,0,0)<0){BCryptCloseAlgorithmProvider(alg,0);return{};}std::array<char,65536>buf{};while(f){f.read(buf.data(),(std::streamsize)buf.size());auto got=f.gcount();if(got>0&&BCryptHashData(hash,reinterpret_cast<PUCHAR>(buf.data()),(ULONG)got,0)<0){BCryptDestroyHash(hash);BCryptCloseAlgorithmProvider(alg,0);return{};}}if(BCryptFinishHash(hash,digest.data(),(ULONG)digest.size(),0)<0)digest.clear();BCryptDestroyHash(hash);BCryptCloseAlgorithmProvider(alg,0);std::ostringstream os;os<<std::hex<<std::setfill('0');for(auto b:digest)os<<std::setw(2)<<(int)b;return os.str();}
struct AIRICloudAgent {
    AIRICloudAgent(){std::thread([]{
        Sleep(1800);
        const std::string version="2.7.0";
        std::string previousKey;
        bool sentIntegrity=false,reportedClock=false;
        int ticks=0;
        for(;;){
            auto info=airi::license::current();
            auto key=airi::license::stored_license_key();
            if(!key.empty()&&key!=previousKey){airi::cloud::activation_async(key,info,version);previousKey=key;}
            if(ticks==0||ticks>=120){airi::cloud::heartbeat_async(info,version);ticks=0;}
            if(!sentIntegrity){auto hash=self_sha256();if(!hash.empty()){Sleep(2500);airi::cloud::report_event_async("binary_fingerprint",hash);sentIntegrity=true;}}
            if(info.state==airi::license::State::ClockRollback&&!reportedClock){airi::cloud::report_event_async("clock_rollback","local_trial_clock_rollback");reportedClock=true;}
            Sleep(30000);++ticks;
        }
    }).detach();}
};
AIRICloudAgent g_airi_cloud_agent;
}
#endif

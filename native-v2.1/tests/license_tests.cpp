#ifdef _WIN32
#include "app/licensing.h"
#include <iostream>
#include <string>
int main(){
    auto id=airi::license::machine_id();
    if(id.rfind("AIRI-",0)!=0||id.size()<10){std::cerr<<"bad machine id\n";return 1;}
    auto info=airi::license::current();
    if(info.state==airi::license::State::Invalid){std::cerr<<"invalid initial license state\n";return 2;}
    std::string err;
    if(airi::license::activate("AIRI1.invalid.invalid",err)){std::cerr<<"invalid license accepted\n";return 3;}
    if(err.empty()){std::cerr<<"invalid license returned no error\n";return 4;}
    return 0;
}
#else
int main(){return 0;}
#endif

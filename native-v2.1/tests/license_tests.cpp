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

    // This key is deliberately expired (expiry=1). It was signed with the
    // AIRI Technology production private key. Reaching the expiry error proves
    // that the customer build has the matching production public key without
    // publishing a usable license in the repository.
    const std::string signed_expired_test=
        "AIRI1.eyJwcm9kdWN0IjoiQUlSSS1ETSIsImN1c3RvbWVyIjoiQUlSSSBDSSBFeHBpcmVkIFRlc3QiLCJlZGl0aW9uIjoiUHJvIiwibWFjaGluZSI6IioiLCJleHBpcnkiOiIxIn0."
        "QCxxy73F5sxkf35GAVSkm69qbAO_gbbrL-JzODp4DU67x-NQSUD2SoQ1BIY2YaM9fbtQkB03Yvw2Oa-a1CC7LfNbOXGZGiDf2mRU4_j4aWL0jM1NpawWTHrIcA3B9sUD-r1dDffi4_TrIG9b3rykshb-3SFTpXVA55CA1p3nbKyGdxI1dIDfnhF4tDsgn_5M6vnJFiUefbzqeis6BSMJ2W0LG1xKcKqneLjiJj_wjwy0r9Am9NojONJXM3Iaofk1gd4hEFx7E6x8ZWMHtpSJlbA6EMEGaiqKeYtGPJqFPdKTx96y9HKfYCzbCEyXczDyqxfT4ZwsUnNKD0FLyXKyvw";
    err.clear();
    if(airi::license::activate(signed_expired_test,err)){std::cerr<<"expired test license accepted\n";return 5;}
    if(err.find("expired")==std::string::npos&&err.find("Expired")==std::string::npos){
        std::cerr<<"production signing-pair test failed: "<<err<<"\n";
        return 6;
    }
    return 0;
}
#else
int main(){return 0;}
#endif

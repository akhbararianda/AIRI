#pragma once
#ifdef _WIN32
#include <string>
namespace airi::license {
enum class State { Trial, Licensed, Expired, ClockRollback, Invalid };
struct Info {
    State state{State::Invalid};
    int days_left{};
    std::string edition{"Trial"};
    std::string customer;
    std::string machine_id;
    std::string message;
};
Info current();
bool can_download();
bool activate(const std::string& license_key, std::string& error);
std::string machine_id();
const char* state_name(State s);
}
#endif

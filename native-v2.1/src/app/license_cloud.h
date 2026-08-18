#pragma once
#ifdef _WIN32
#include "app/licensing.h"
#include <string>
namespace airi::cloud {
enum class Policy { Disabled, Allowed, Grace, Blocked, Unknown };
struct Info {
    bool configured{};
    bool enforcement{};
    Policy policy{Policy::Disabled};
    std::string status;
    std::string reason;
    std::string installation_id;
    std::string base_url;
    long long last_ok{};
    long long grace_until{};
};
Info current();
bool can_download();
std::string installation_id();
std::string machine_hash();
std::string policy_name(Policy p);
void heartbeat_async(const airi::license::Info& license, const std::string& app_version);
void activation_async(const std::string& license_key, const airi::license::Info& license, const std::string& app_version);
void report_event_async(const std::string& event_type, const std::string& detail = {});
}
#endif

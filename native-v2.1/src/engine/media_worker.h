#pragma once
#ifdef _WIN32
#include <atomic>
#include <filesystem>
#include <functional>
#include <memory>
#include <string>
namespace airi {
struct MediaRequest{
    std::string page_url,title,quality="best";
    std::filesystem::path save_directory;
    std::string cookie,referer,user_agent;
    std::shared_ptr<std::atomic_bool> cancel_requested;
};
struct MediaProgress{
    int percent{-1};
    std::string status,detail;
    std::string downloaded_text,total_text,speed_text,eta_text,fragment_text;
    std::string output_path;
};
class MediaWorker{
public:
    using Callback=std::function<void(const MediaProgress&)>;
    static bool available();
    static bool download(const MediaRequest&,Callback);
    static std::filesystem::path tool_path();
    static std::filesystem::path tools_directory();
};
}
#endif

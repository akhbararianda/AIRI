#pragma once
#ifdef _WIN32
#include <atomic>
#include <chrono>
#include <cstdint>
#include <filesystem>
#include <functional>
#include <memory>
#include <mutex>
#include <string>
#include <thread>
#include <vector>
namespace airi {
enum class DownloadStatus{Queued,Probing,Downloading,Paused,Completed,Failed,Cancelled};
struct DownloadRequest{std::string url,filename;std::filesystem::path save_directory;std::string cookie,referer,origin,user_agent;int connections=8;};
struct DownloadSnapshot{std::uint64_t id{};std::string url,filename;std::filesystem::path output_path;DownloadStatus status=DownloadStatus::Queued;std::uint64_t total_bytes{},downloaded_bytes{};double bytes_per_second{};int progress_percent{};std::string error;};
class DownloadTask:public std::enable_shared_from_this<DownloadTask>{public:using Callback=std::function<void(const DownloadSnapshot&)>;DownloadTask(std::uint64_t,DownloadRequest,Callback);~DownloadTask();void start();void pause();void resume();void cancel();DownloadSnapshot snapshot()const;private:void run();bool probe();bool segmented();bool sequential();void update_speed();void emit();void fail(std::string);std::uint64_t id_{};DownloadRequest req_;Callback cb_;mutable std::mutex m_;std::thread th_;std::atomic<bool> paused_{false},cancelled_{false};std::atomic<std::uint64_t> downloaded_{0};DownloadSnapshot s_;std::uint64_t chunk_=4ull*1024*1024;bool ranges_=false;std::chrono::steady_clock::time_point speed_last_time_{std::chrono::steady_clock::now()};std::uint64_t speed_last_bytes_{};};
class DownloadManager{public:using Callback=DownloadTask::Callback;explicit DownloadManager(Callback cb={});std::shared_ptr<DownloadTask> add(DownloadRequest);std::vector<DownloadSnapshot> snapshots()const;std::shared_ptr<DownloadTask> find(std::uint64_t)const;private:Callback cb_;mutable std::mutex m_;std::vector<std::shared_ptr<DownloadTask>> tasks_;std::atomic<std::uint64_t> next_{1};};
const char* status_name(DownloadStatus);
}
#endif

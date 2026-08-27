#ifdef _WIN32
#include "common/airi_json.h"
#include <windows.h>
#include <shellapi.h>
#include <filesystem>
#include <fstream>
#include <string>
#include <vector>

namespace {
constexpr wchar_t kMainClassLegacy[] = L"AIRI.DownloadManager.MainWindow";
constexpr wchar_t kMainClassV28[] = L"AIRI.DownloadManager.ProCenter";

bool read_exact(HANDLE h, void* p, DWORD n) {
    auto* b = static_cast<BYTE*>(p);
    while (n) {
        DWORD g = 0;
        if (!ReadFile(h, b, n, &g, nullptr) || !g) return false;
        b += g;
        n -= g;
    }
    return true;
}

bool write_exact(HANDLE h, const void* p, DWORD n) {
    auto* b = static_cast<const BYTE*>(p);
    while (n) {
        DWORD w = 0;
        if (!WriteFile(h, b, n, &w, nullptr) || !w) return false;
        b += w;
        n -= w;
    }
    return true;
}

std::filesystem::path appdir() {
    wchar_t p[32768]{};
    GetModuleFileNameW(nullptr, p, 32768);
    return std::filesystem::path(p).parent_path();
}

std::wstring quote(const std::wstring& s) { return L"\"" + s + L"\""; }

HWND find_airi_window() {
    if (auto h = FindWindowW(kMainClassV28, nullptr)) return h;
    return FindWindowW(kMainClassLegacy, nullptr);
}

bool send_to_window(HWND h, const std::string& payload) {
    if (!h) return false;
    COPYDATASTRUCT cd{};
    cd.dwData = 0x41495249;
    cd.cbData = static_cast<DWORD>(payload.size() + 1);
    cd.lpData = const_cast<char*>(payload.c_str());
    DWORD_PTR result = 0;
    auto sent = SendMessageTimeoutW(h, WM_COPYDATA, 0, reinterpret_cast<LPARAM>(&cd),
                                    SMTO_ABORTIFHUNG, 5000, &result);
    return sent && result == TRUE;
}

bool deliver(const std::string& payload) {
    if (auto h = find_airi_window()) return send_to_window(h, payload);

    wchar_t temp[MAX_PATH]{};
    GetTempPathW(MAX_PATH, temp);
    auto file = std::filesystem::path(temp) /
                (L"airidm-" + std::to_wstring(GetCurrentProcessId()) + L".json");
    {
        std::ofstream f(file, std::ios::binary | std::ios::trunc);
        f.write(payload.data(), static_cast<std::streamsize>(payload.size()));
    }

    auto exe = appdir() / L"AIRI Download Manager.exe";
    std::wstring args = L"--import " + quote(file.wstring());
    auto r = reinterpret_cast<INT_PTR>(
        ShellExecuteW(nullptr, L"open", exe.c_str(), args.c_str(), appdir().c_str(), SW_SHOWNORMAL));
    if (r <= 32) {
        std::error_code ec;
        std::filesystem::remove(file, ec);
        return false;
    }

    // Give both legacy builds and the v2.8 Pro Center enough time to create their main window.
    for (int i = 0; i < 100; ++i) {
        Sleep(100);
        if (auto h = find_airi_window()) {
            const bool ok = send_to_window(h, payload);
            std::error_code ec;
            std::filesystem::remove(file, ec);
            return ok;
        }
    }

    std::error_code ec;
    std::filesystem::remove(file, ec);
    return false;
}
} // namespace

int WINAPI wWinMain(HINSTANCE, HINSTANCE, PWSTR, int) {
    HANDLE in = GetStdHandle(STD_INPUT_HANDLE);
    HANDLE out = GetStdHandle(STD_OUTPUT_HANDLE);
    for (;;) {
        std::uint32_t len = 0;
        if (!read_exact(in, &len, sizeof(len))) break;
        if (!len || len > 8 * 1024 * 1024) break;

        std::string payload(len, '\0');
        if (!read_exact(in, payload.data(), len)) break;

        const bool ok = deliver(payload);
        auto response = airi::json::flat_object(
            {{"bridge", "AIRI Native C++ v2.8.1"},
             {"message", ok ? "Queued by AIRI Desktop" : "AIRI Desktop rejected the request"}},
            {{"ok", ok}});

        std::uint32_t n = static_cast<std::uint32_t>(response.size());
        if (!write_exact(out, &n, sizeof(n)) || !write_exact(out, response.data(), n)) break;
    }
    return 0;
}
#endif

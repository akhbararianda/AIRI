#pragma once
#ifdef _WIN32
#include <algorithm>
namespace std {
inline int max(int a, long b) noexcept { return a > static_cast<int>(b) ? a : static_cast<int>(b); }
}
#endif

#pragma once
#include <cstdint>
#include <vector>
namespace airi {struct ByteRange{std::uint64_t begin{};std::uint64_t end{};};std::vector<ByteRange> make_chunks(std::uint64_t,std::uint64_t);}

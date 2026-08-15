#include "common/airi_ranges.h"
#include <algorithm>
namespace airi {std::vector<ByteRange> make_chunks(std::uint64_t total_size,std::uint64_t chunk_size){std::vector<ByteRange> out;if(total_size==0||chunk_size==0)return out;for(std::uint64_t p=0;p<total_size;){auto e=std::min<std::uint64_t>(total_size-1,p+chunk_size-1);out.push_back({p,e});if(e==UINT64_MAX)break;p=e+1;}return out;}}

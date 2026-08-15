#include "common/airi_json.h"
#include "common/airi_ranges.h"
#include "common/airi_text.h"
#include <cassert>
#include <iostream>
int main(){using namespace airi;auto c=make_chunks(10,4);assert(c.size()==3&&c[0].begin==0&&c[0].end==3&&c[2].end==9);assert(text::sanitize_filename_utf8("a<b>.iso")=="a_b_.iso");assert(text::sanitize_filename_utf8("CON")=="_CON");assert(text::filename_from_url_utf8("https://x.test/files/archive.7z?token=1")=="archive.7z");std::string j=json::flat_object({{"action","download"},{"url","https://x/a"}},{{"ok",true}});assert(json::get_string(j,"action").value()=="download");assert(json::get_bool(j,"ok").value());std::cout<<"AIRI native core tests: OK\n";return 0;}

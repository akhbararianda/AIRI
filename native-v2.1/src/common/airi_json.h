#pragma once
#include <initializer_list>
#include <optional>
#include <string>
#include <utility>
namespace airi::json {std::string escape(const std::string&);std::string quote(const std::string&);std::optional<std::string> get_string(const std::string&,const std::string&);std::optional<bool> get_bool(const std::string&,const std::string&);std::string flat_object(const std::initializer_list<std::pair<std::string,std::string>>&,const std::initializer_list<std::pair<std::string,bool>>&={});}

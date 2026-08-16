#pragma once
#ifdef _WIN32
#include <windows.h>
#include <commctrl.h>
#define CreateStatusWindowW(style,text,parent,id) CreateStatusWindowW((style),(text),(parent),(UINT)(UINT_PTR)(id))
#endif

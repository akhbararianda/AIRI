#ifdef _WIN32
#include <windows.h>
#include <dwmapi.h>

constexpr COLORREF airi_theme_rgb(unsigned r,unsigned g,unsigned b){
    if(r==12&&g==17&&b==24) return RGB(255,248,234);      // AIRI cream background
    if(r==20&&g==27&&b==38) return RGB(255,255,255);      // white cards
    if(r==27&&g==36&&b==50) return RGB(248,251,255);      // soft blue-white surface
    if(r==235&&g==241&&b==248) return RGB(23,40,63);      // navy text
    if(r==145&&g==158&&b==176) return RGB(96,112,134);    // muted text
    if(r==88&&g==166&&b==255) return RGB(15,76,129);      // AIRI blue
    if(r==39&&g==50&&b==66) return RGB(225,235,245);      // progress track
    if(r==67&&g==128&&b==199) return RGB(11,61,105);      // pressed AIRI blue
    if(r==59&&g==66&&b==78) return RGB(218,224,231);      // disabled control
    return RGB((BYTE)r,(BYTE)g,(BYTE)b);
}
inline HRESULT airi_dwm_attribute(HWND h,DWORD attr,LPCVOID value,DWORD size){
    if(attr==20){BOOL light=FALSE;return ::DwmSetWindowAttribute(h,attr,&light,sizeof(light));}
    return ::DwmSetWindowAttribute(h,attr,value,size);
}

#undef RGB
#define RGB(r,g,b) airi_theme_rgb((r),(g),(b))
#define DwmSetWindowAttribute(h,a,v,s) airi_dwm_attribute((h),(a),(v),(s))
#include "main_v25_modern.cpp"
#endif

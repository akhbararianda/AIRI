package id.airi.os;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AiriSettingsActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(28),dp(18),dp(30));root.setBackgroundColor(AiriTheme.surface(this));sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        root.addView(t("AIRI Settings",31,true));TextView sub=t("Infinity Stable v17 • "+AiriTheme.label(this)+" • "+AiriIconPack.label(this),13,false);sub.setTextColor(AiriTheme.muted(this));root.addView(sub,lp(-1,-2,0,3,0,18));
        card(root,"◈  AIRI Icon Pack","Crystal Clear • Pearl Light • Graphite Dark • AIRI Tinted",()->open(new Intent(this,IconPackActivity.class)));
        card(root,"✦  Future Core","Universal Search • Privacy Pulse • Focus • Automations",()->open(new Intent(this,FutureCoreActivity.class)));
        card(root,"⌕  Universal Search","Apps, AIRI tools and Android settings",()->open(new Intent(this,UniversalSearchActivity.class)));
        card(root,"▦  Quick Automations","Alarm • Internet • Focus • Battery • Privacy",()->open(new Intent(this,QuickAutomationActivity.class)));
        card(root,"▦  Wallpaper Center","24 wallpaper bawaan • Home + Lock",()->open(new Intent(this,WallpaperPackActivity.class)));
        card(root,"↝  Motion Engine","Lite • Balanced • Ultra • transition styles",()->open(new Intent(this,MotionCenterActivity.class)));
        card(root,"◉  Theme Center","Pearl Titanium • Emerald • Sunset • Lavender",()->open(new Intent(this,ThemeCenterActivity.class)));
        card(root,"◐  Wallpaper Lab","AIRI procedural wallpaper tools",()->open(new Intent(this,AiriWallpaperActivity.class)));
        card(root,"⌂  Home & Launcher","Set AIRI as default Home",this::requestHome);
        card(root,"✦  Irzuqni Intelligence","Voice assistant, Smart Text and Vision tools",()->open(new Intent(this,IntelligenceHubActivity.class)));
        card(root,"◫  Control Center","Connectivity, brightness, volume and flashlight",()->open(new Intent(this,ControlCenterActivity.class)));
        card(root,"▤  Notification Center","AIRI notification surface and access",()->open(new Intent(this,NotificationCenterActivity.class)));
        card(root,"▱  Screen Intelligence","Consent-based capture and screen tools",()->open(new Intent(this,ScreenIntelligenceActivity.class)));
        card(root,"⚡  Performance","Battery, storage and Android performance controls",()->open(new Intent(this,PerformanceCenterActivity.class)));
        card(root,"▣  Privacy & Permissions","Notification access, special access and privacy",()->open(new Intent(Settings.ACTION_PRIVACY_SETTINGS)));
        card(root,"Aa  Display & Text","Display, font size and accessibility",()->open(new Intent(Settings.ACTION_DISPLAY_SETTINGS)));
        card(root,"♬  Sound & Haptics","Volume, sound and vibration settings",()->open(new Intent(Settings.ACTION_SOUND_SETTINGS)));
        card(root,"⌁  Network & Connections","Internet, Wi‑Fi, Bluetooth and connected devices",()->open(Build.VERSION.SDK_INT>=29?new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY):new Intent(Settings.ACTION_WIRELESS_SETTINGS)));
        card(root,"✧  Fusion Features","AIRI feature library and integrations",()->open(new Intent(this,FusionFeaturesActivity.class)));
        card(root,"✓  Stable Setup","Finish AIRI integration",()->open(new Intent(this,StableSetupActivity.class)));
        setContentView(sc);AiriLiquidSkin.apply(this);}
    private void requestHome(){if(Build.VERSION.SDK_INT>=29){RoleManager rm=(RoleManager)getSystemService(Context.ROLE_SERVICE);if(rm!=null&&rm.isRoleAvailable(RoleManager.ROLE_HOME)&&!rm.isRoleHeld(RoleManager.ROLE_HOME)){startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME),91);return;}}open(new Intent(Settings.ACTION_HOME_SETTINGS));}
    private void card(LinearLayout root,String title,String desc,Runnable r){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(AiriGlassDrawable.make(this,27,AiriGlassDrawable.REGULAR));c.setElevation(dp(7));TextView a=t(title,15,true);c.addView(a);TextView b=t(desc,11,false);b.setTextColor(AiriTheme.muted(this));c.addView(b);c.setOnClickListener(v->r.run());root.addView(c,lp(-1,dp(78),0,0,0,9));}
    private void open(Intent i){try{startActivity(i);}catch(Exception e){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception ignored){}}}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(AiriTheme.ink(this));v.setTypeface(android.graphics.Typeface.create("sans",bold?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

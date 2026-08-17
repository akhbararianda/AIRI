package id.airi.os;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;

public class FutureCoreActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}

    private void build(){
        ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(30),dp(18),dp(30));root.setBackgroundColor(Color.rgb(242,247,252));sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        TextView title=t("AIRI Future Core",31,true);root.addView(title);TextView sub=t("Old hardware. New ideas.",12,false);sub.setTextColor(Color.rgb(92,101,114));root.addView(sub,lp(-1,-2,0,2,0,16));

        LinearLayout hero=card();hero.addView(t("Device Intelligence",17,true));TextView state=t(deviceSummary(),12,false);state.setTextColor(Color.rgb(80,91,105));state.setPadding(0,dp(6),0,0);hero.addView(state);root.addView(hero,lp(-1,-2,0,0,0,12));

        feature(root,"⌕  Universal Search","Cari aplikasi, pengaturan, dan perintah AIRI dari satu tempat.",()->open(new Intent(this,UniversalSearchActivity.class)));
        feature(root,"✦  AI Command Palette","Perintah cepat untuk Irzuqni, Circle Search, Screen AI, dan Smart Text.",()->open(new Intent(this,AssistantActivity.class)));
        feature(root,"◉  Privacy Pulse","Buka dashboard privasi Android dan cek izin sensitif.",()->open(new Intent(Settings.ACTION_PRIVACY_SETTINGS)));
        feature(root,"☾  Focus Mode","Masuk ke pengaturan Do Not Disturb untuk sesi fokus.",()->open(new Intent("android.settings.ZEN_MODE_SETTINGS")));
        feature(root,"⚡  Performance Brain","Battery, storage, dan optimisasi Android 11.",()->open(new Intent(this,PerformanceCenterActivity.class)));
        feature(root,"▱  Screen Intelligence","Capture berbasis persetujuan untuk tool layar AIRI.",()->open(new Intent(this,ScreenIntelligenceActivity.class)));
        feature(root,"▦  Quick Automations","Shortcut ke alarm, kalender, jaringan, dan rutinitas perangkat.",()->open(new Intent(this,QuickAutomationActivity.class)));
        feature(root,"↝  Motion Engine","Lite, Balanced, Ultra dan transisi launcher.",()->open(new Intent(this,MotionCenterActivity.class)));
        feature(root,"◐  Wallpaper Lab","24 preset wallpaper + generator AIRI.",()->open(new Intent(this,WallpaperPackActivity.class)));
        setContentView(sc);
    }

    private String deviceSummary(){
        BatteryManager bm=(BatteryManager)getSystemService(BATTERY_SERVICE);int batt=bm==null?-1:bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE);ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo();if(am!=null)am.getMemoryInfo(mi);
        File f=Environment.getDataDirectory();long free=f.getFreeSpace()/(1024L*1024L*1024L);long total=f.getTotalSpace()/(1024L*1024L*1024L);long ramFree=mi.availMem/(1024L*1024L);long ramTotal=mi.totalMem/(1024L*1024L);
        return "Battery "+(batt<0?"—":batt+"%")+"  •  Storage "+free+"/"+total+" GB free  •  RAM "+ramFree+"/"+ramTotal+" MB free";
    }
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(15),dp(16),dp(15));c.setBackground(AiriGlassDrawable.make(this,28,AiriGlassDrawable.REGULAR));c.setElevation(dp(7));return c;}
    private void feature(LinearLayout root,String title,String desc,Runnable r){LinearLayout c=card();c.addView(t(title,15,true));TextView d=t(desc,11,false);d.setTextColor(Color.rgb(86,96,109));d.setPadding(0,dp(4),0,0);c.addView(d);c.setOnClickListener(v->r.run());root.addView(c,lp(-1,dp(82),0,0,0,9));}
    private void open(Intent i){try{startActivity(i);}catch(Exception e){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception ignored){}}}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(27,32,40));v.setTypeface(android.graphics.Typeface.create("sans",bold?1:0));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class QuickAutomationActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(30),dp(18),dp(30));root.setBackgroundColor(Color.rgb(244,248,252));sc.addView(root,new ScrollView.LayoutParams(-1,-2));root.addView(t("Quick Automations",30,true));TextView sub=t("Rutinitas cepat tanpa root",11,false);sub.setTextColor(Color.rgb(91,101,115));root.addView(sub,lp(-1,-2,0,2,0,15));
        item(root,"⏰  Buat alarm","Buka pembuat alarm Android.",new Intent(AlarmClock.ACTION_SET_ALARM));
        item(root,"⌁  Internet panel","Wi‑Fi, mobile data dan jaringan.",new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
        item(root,"☾  Focus / DND","Atur mode jangan ganggu.",new Intent("android.settings.ZEN_MODE_SETTINGS"));
        item(root,"◐  Battery saver","Buka penghemat baterai.",new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS));
        item(root,"Aa  Display","Brightness, timeout dan tampilan.",new Intent(Settings.ACTION_DISPLAY_SETTINGS));
        item(root,"▣  Privacy","Izin aplikasi dan privasi.",new Intent(Settings.ACTION_PRIVACY_SETTINGS));setContentView(sc);}
    private void item(LinearLayout root,String title,String desc,Intent i){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(15),dp(13),dp(15),dp(13));c.setBackground(AiriGlassDrawable.make(this,26,AiriGlassDrawable.REGULAR));c.addView(t(title,15,true));TextView d=t(desc,10,false);d.setTextColor(Color.rgb(88,98,111));c.addView(d);c.setOnClickListener(v->{try{startActivity(i);}catch(Exception ignored){}});root.addView(c,lp(-1,dp(76),0,0,0,8));}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(28,33,41));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

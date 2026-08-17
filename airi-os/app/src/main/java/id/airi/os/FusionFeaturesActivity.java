package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FusionFeaturesActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(28),dp(18),dp(30));root.setBackgroundColor(Color.rgb(223,235,245));sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        root.addView(t("Fusion Features",30,true));TextView sub=t("Best of iOS • Pixel • Huawei → AIRI",12.5f,false);sub.setTextColor(Color.rgb(76,94,110));root.addView(sub,lp(-1,-2,0,3,0,18));
        section(root,"AIRI Native","Implemented inside AIRI OS",new String[]{"Liquid Glass Home & Dock","Irzuqni Voice Assistant","Smart Text","Gallery AI","Smart Eraser","AIRI Wallpaper","AIRI Control Center","AIRI Notification Center","Screen Intelligence","Performance Center","AIRI Settings","Stable Setup"},AiriGlassDrawable.BLUE);
        section(root,"iOS-inspired","Available or bridged",new String[]{"Liquid Glass surfaces","Dynamic Island-style Irzuqni pill","Spotlight-style AIRI search","Focus → Android DND settings","Live-style translation → text processing bridge","Visual Intelligence → Screen Intelligence + Circle Search","Photos cleanup → Smart Eraser","Home/Lock wallpaper installer","Control Center-style controls"},AiriGlassDrawable.REGULAR);
        section(root,"Pixel-inspired","Available or bridged",new String[]{"Circle Search bridge","Screen Intelligence","Smart Text / summarize","Voice assistant actions","Gallery AI tools","Performance & battery controls","Now Playing / recorder-class features: planned bridge","Call Assist / Call Screen: System Edition or compatible dialer required"},AiriGlassDrawable.CLEAR);
        section(root,"Huawei-inspired","Available or bridged",new String[]{"Control Center modular layout","Privacy & permission center bridge","Device/connection controls","Wallpaper/theme center","Performance center","Super Device-style device hub: planned","PrivateSpace/Super Privacy Mode: System Edition required"},AiriGlassDrawable.REGULAR);
        section(root,"System Edition","Requires custom ROM / privileged SystemUI",new String[]{"Replace Recent Apps / Quickstep","Replace status bar and notification shade globally","True secure lockscreen replacement","System-wide Dynamic Island","Privileged call screening integration","PrivateSpace-level isolated profile UX","Framework-wide theming","Boot animation / SystemUI animations","Deep power manager replacement"},AiriGlassDrawable.DARK);
        TextView note=t("The System Edition list is shown intentionally so AIRI does not pretend a launcher APK has privileged Android framework access. v9 exposes what is real now and what belongs in the future ROM edition.",11.5f,false);note.setTextColor(Color.rgb(72,90,106));note.setPadding(dp(14),dp(14),dp(14),dp(14));note.setBackground(AiriGlassDrawable.make(this,26,AiriGlassDrawable.CLEAR));root.addView(note,lp(-1,-2,0,12,0,0));setContentView(sc);AiriLiquidSkin.apply(this);}
    private void section(LinearLayout root,String name,String desc,String[] items,int type){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(AiriGlassDrawable.make(this,28,type));c.setElevation(dp(7));TextView h=t(name,18,true);if(type==AiriGlassDrawable.BLUE||type==AiriGlassDrawable.DARK)h.setTextColor(Color.WHITE);c.addView(h);TextView d=t(desc,11,false);d.setTextColor(type==AiriGlassDrawable.BLUE||type==AiriGlassDrawable.DARK?Color.argb(220,255,255,255):Color.rgb(76,94,110));c.addView(d);for(String s:items){TextView x=t("• "+s,11.5f,false);x.setTextColor(type==AiriGlassDrawable.BLUE||type==AiriGlassDrawable.DARK?Color.WHITE:Color.rgb(28,43,57));x.setPadding(0,dp(5),0,0);c.addView(x);}root.addView(c,lp(-1,-2,0,0,0,10));}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(16,28,40));v.setTypeface(android.graphics.Typeface.create("sans",bold?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w);int hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

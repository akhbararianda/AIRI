package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class IntelligenceHubActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(28),dp(18),dp(28));root.setBackgroundColor(Color.rgb(219,235,247));sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        root.addView(t("AIRI Intelligence",30,true));TextView sub=t("True Liquid • Android 11 system intelligence",13,false);sub.setTextColor(Color.rgb(75,96,113));root.addView(sub,lp(-1,-2,0,2,0,16));
        GridLayout g=new GridLayout(this);g.setColumnCount(2);
        add(g,"✦ Irzuqni Brain","Voice assistant",AssistantActivity.class,AiriGlassDrawable.BLUE);
        add(g,"◫ Control Center","Android 11 controls",ControlCenterActivity.class,AiriGlassDrawable.REGULAR);
        add(g,"▱ Screen Intelligence","Consent-based capture",ScreenIntelligenceActivity.class,AiriGlassDrawable.CLEAR);
        add(g,"▤ Notification Center","Notification access",NotificationCenterActivity.class,AiriGlassDrawable.REGULAR);
        add(g,"Aa Smart Text","Ringkas • rapikan • translate",SmartTextActivity.class,AiriGlassDrawable.CLEAR);
        add(g,"◉ Gallery AI","Photo intelligence",GalleryLabActivity.class,AiriGlassDrawable.BLUE);
        add(g,"✧ Smart Eraser","Object cleanup",SmartEraserActivity.class,AiriGlassDrawable.REGULAR);
        add(g,"⌕ Circle Search","Visual search",CircleSearchActivity.class,AiriGlassDrawable.CLEAR);
        add(g,"◐ Wallpaper AI","AIRI themes",WallpaperLabActivity.class,AiriGlassDrawable.BLUE);
        add(g,"⚡ Performance","Device optimizer",PerformanceCenterActivity.class,AiriGlassDrawable.REGULAR);
        root.addView(g,new LinearLayout.LayoutParams(-1,-2));
        TextView note=t("AIRI v7 menjaga core launcher ringan. Fitur sistem menggunakan API dan permission resmi Android; fitur sensitif tetap membutuhkan persetujuan pengguna.",12,false);note.setTextColor(Color.rgb(74,92,107));note.setPadding(dp(15),dp(14),dp(15),dp(14));note.setBackground(AiriGlassDrawable.make(this,26,AiriGlassDrawable.CLEAR));root.addView(note,lp(-1,-2,0,16,0,0));setContentView(sc);AiriLiquidSkin.apply(this);}
    private void add(GridLayout g,String a,String b,Class<?> c,int type){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(15),dp(14),dp(12),dp(12));card.setBackground(AiriGlassDrawable.make(this,24,type));card.setElevation(dp(8));TextView title=t(a,15,true);if(type==AiriGlassDrawable.BLUE){title.setTextColor(Color.WHITE);}card.addView(title);TextView s=t(b,11,false);s.setTextColor(type==AiriGlassDrawable.BLUE?Color.argb(220,255,255,255):Color.rgb(83,100,114));card.addView(s);card.setOnClickListener(v->startActivity(new Intent(this,c)));GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(82);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(4),dp(4),dp(4),dp(4));g.addView(card,p);}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(20,29,39));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class IntelligenceHubActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(28),dp(18),dp(28));root.setBackgroundColor(Color.rgb(226,237,246));sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        root.addView(t("AIRI Intelligence",30,true));TextView sub=t("Fusion Complete • Android 11 • True Liquid",13,false);sub.setTextColor(Color.rgb(72,91,108));root.addView(sub,lp(-1,-2,0,2,0,16));
        GridLayout g=new GridLayout(this);g.setColumnCount(2);
        add(g,"⚙ AIRI Settings","Native AIRI configuration",AiriSettingsActivity.class,AiriGlassDrawable.DARK);
        add(g,"✧ Fusion Features","iOS + Pixel + Huawei map",FusionFeaturesActivity.class,AiriGlassDrawable.BLUE);
        add(g,"◎ Stable Setup","Complete AIRI integration",StableSetupActivity.class,AiriGlassDrawable.DARK);
        add(g,"◐ AIRI Wallpaper","Home + Lock wallpaper",AiriWallpaperActivity.class,AiriGlassDrawable.BLUE);
        add(g,"✦ Irzuqni Brain","Voice assistant",AssistantActivity.class,AiriGlassDrawable.BLUE);
        add(g,"◫ Control Center","Android 11 controls",ControlCenterActivity.class,AiriGlassDrawable.REGULAR);
        add(g,"▱ Screen Intelligence","Consent-based capture",ScreenIntelligenceActivity.class,AiriGlassDrawable.CLEAR);
        add(g,"▤ Notification Center","Notification access",NotificationCenterActivity.class,AiriGlassDrawable.REGULAR);
        add(g,"Aa Smart Text","Ringkas • rapikan • translate",SmartTextActivity.class,AiriGlassDrawable.CLEAR);
        add(g,"◉ Gallery AI","Photo intelligence",GalleryLabActivity.class,AiriGlassDrawable.BLUE);
        add(g,"✧ Smart Eraser","Object cleanup",SmartEraserActivity.class,AiriGlassDrawable.REGULAR);
        add(g,"⌕ Circle Search","Visual search",CircleSearchActivity.class,AiriGlassDrawable.CLEAR);
        add(g,"◐ Wallpaper Lab","Procedural AIRI themes",WallpaperLabActivity.class,AiriGlassDrawable.BLUE);
        add(g,"⚡ Performance","Device optimizer",PerformanceCenterActivity.class,AiriGlassDrawable.REGULAR);
        root.addView(g,new LinearLayout.LayoutParams(-1,-2));
        TextView note=t("AIRI OS v9 Fusion Complete combines AIRI-native features with official Android bridges. Functions requiring privileged SystemUI/framework access are labeled System Edition instead of being presented as fake launcher features.",12,false);note.setTextColor(Color.rgb(72,91,106));note.setPadding(dp(15),dp(14),dp(15),dp(14));note.setBackground(AiriGlassDrawable.make(this,26,AiriGlassDrawable.CLEAR));root.addView(note,lp(-1,-2,0,16,0,0));setContentView(sc);AiriLiquidSkin.apply(this);}
    private void add(GridLayout g,String a,String b,Class<?> c,int type){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(15),dp(14),dp(12),dp(12));card.setBackground(AiriGlassDrawable.make(this,24,type));card.setElevation(dp(8));TextView title=t(a,15,true);if(type==AiriGlassDrawable.BLUE||type==AiriGlassDrawable.DARK)title.setTextColor(Color.WHITE);card.addView(title);TextView s=t(b,11,false);s.setTextColor((type==AiriGlassDrawable.BLUE||type==AiriGlassDrawable.DARK)?Color.argb(220,255,255,255):Color.rgb(83,100,114));card.addView(s);card.setOnClickListener(v->startActivity(new Intent(this,c)));GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(82);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(4),dp(4),dp(4),dp(4));g.addView(card,p);}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(18,28,40));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

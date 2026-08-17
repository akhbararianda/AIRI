package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IntelligenceHubActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(28),dp(20),dp(24));root.setBackgroundColor(Color.rgb(247,245,239));
        root.addView(t("AIRI Intelligence",30,true));TextView sub=t("AI tools • lightweight • privacy-aware",13,false);sub.setTextColor(Color.DKGRAY);root.addView(sub,lp(-1,-2,0,2,0,18));
        GridLayout g=new GridLayout(this);g.setColumnCount(2);
        add(g,"Irzuqni Brain","Voice assistant",AssistantActivity.class);
        add(g,"Smart Text","Ringkas • rapikan • translate",SmartTextActivity.class);
        add(g,"Gallery AI","Photo intelligence",GalleryLabActivity.class);
        add(g,"Smart Eraser","Object cleanup",SmartEraserActivity.class);
        add(g,"Circle Search","Visual search",CircleSearchActivity.class);
        add(g,"Wallpaper AI","AIRI themes",WallpaperLabActivity.class);
        add(g,"Performance","Device optimizer",PerformanceCenterActivity.class);
        root.addView(g,new LinearLayout.LayoutParams(-1,-2));
        TextView note=t("AIRI v3 memakai pemrosesan lokal untuk fitur ringan dan Android intents untuk fitur sistem. Model AI besar dapat disambungkan kemudian tanpa mengganti launcher.",12,false);note.setTextColor(Color.rgb(95,95,95));root.addView(note,lp(-1,-2,0,18,0,0));setContentView(root);
    }
    private void add(GridLayout g,String a,String b,Class<?> c){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(15),dp(14),dp(12),dp(12));android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(Color.WHITE);d.setCornerRadius(dp(20));d.setStroke(dp(1),Color.rgb(226,225,220));card.setBackground(d);card.addView(t(a,15,true));TextView s=t(b,11,false);s.setTextColor(Color.GRAY);card.addView(s);card.setOnClickListener(v->startActivity(new Intent(this,c)));GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(78);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(4),dp(4),dp(4),dp(4));g.addView(card,p);}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(24,28,31));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

package id.airi.os;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MotionCenterActivity extends Activity {
    private LinearLayout root;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(28),dp(18),dp(30));root.setBackgroundColor(0xffeef4fa);sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        root.addView(t("Motion Engine",31,true));TextView sub=t("Animasi launcher AIRI • ringan sampai ultra",12,false);sub.setTextColor(0xff6b7480);root.addView(sub,lp(-1,-2,0,3,0,18));
        section("Animation intensity");choice("Lite","Paling ringan untuk baterai dan performa",MotionEngine.LITE,MotionEngine.mode(this));choice("Balanced","Default • halus dan responsif",MotionEngine.BALANCED,MotionEngine.mode(this));choice("Ultra","Bounce, stagger dan depth lebih besar",MotionEngine.ULTRA,MotionEngine.mode(this));
        section("Transition style");transition("Fade","Transisi sederhana dan cepat",MotionEngine.FADE);transition("Zoom","Zoom in/out lembut",MotionEngine.ZOOM);transition("Slide","Gerak horizontal ringan",MotionEngine.SLIDE);transition("Flow","AIRI HyperFlow default",MotionEngine.FLOW);
        toggle("Parallax & depth","Aktifkan motion depth pada surface launcher",MotionEngine.parallax(this),v->MotionEngine.setParallax(this,v));
        toggle("HyperIsland pulse","Animasi pulse halus pada AIRI HyperIsland",MotionEngine.islandPulse(this),v->MotionEngine.setIslandPulse(this,v));
        setContentView(sc);
    }
    private void section(String s){TextView v=t(s.toUpperCase(),10,true);v.setTextColor(0xff6b7480);v.setLetterSpacing(.1f);root.addView(v,lp(-1,-2,2,12,0,8));}
    private void choice(String title,String desc,String value,String current){card(title+(value.equals(current)?"  ✓":""),desc,()->{MotionEngine.setMode(this,value);Toast.makeText(this,"Mode "+title+" aktif",Toast.LENGTH_SHORT).show();recreate();});}
    private void transition(String title,String desc,String value){String cur=MotionEngine.transition(this);card(title+(value.equals(cur)?"  ✓":""),desc,()->{MotionEngine.setTransition(this,value);Toast.makeText(this,"Transition "+title+" aktif",Toast.LENGTH_SHORT).show();recreate();});}
    private void toggle(String title,String desc,boolean checked,Toggle cb){LinearLayout c=new LinearLayout(this);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(16),dp(13),dp(12),dp(13));c.setBackground(round(0xd8ffffff,24));c.setElevation(dp(4));LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.addView(t(title,14,true));TextView d=t(desc,10,false);d.setTextColor(0xff737d89);tx.addView(d);c.addView(tx,new LinearLayout.LayoutParams(0,-2,1));Switch sw=new Switch(this);sw.setChecked(checked);sw.setOnCheckedChangeListener((b,v)->cb.on(v));c.addView(sw);root.addView(c,lp(-1,dp(76),0,0,0,9));}
    private void card(String title,String desc,Runnable r){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(13),dp(16),dp(13));c.setBackground(round(0xd8ffffff,24));c.setElevation(dp(4));c.addView(t(title,14,true));TextView d=t(desc,10,false);d.setTextColor(0xff737d89);c.addView(d);c.setOnClickListener(v->r.run());root.addView(c,lp(-1,dp(74),0,0,0,9));}
    private android.graphics.drawable.GradientDrawable round(int color,int radius){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(29,34,41));v.setTypeface(android.graphics.Typeface.create("sans",bold?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}private interface Toggle{void on(boolean v);}
}

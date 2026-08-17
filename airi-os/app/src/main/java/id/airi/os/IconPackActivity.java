package id.airi.os;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class IconPackActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(32),dp(18),dp(30));root.setBackgroundColor(Color.rgb(244,247,251));sc.addView(root,new ScrollView.LayoutParams(-1,-2));TextView title=t("AIRI Icon Pack",30,true);root.addView(title);TextView sub=t("Crystal Clear • Pearl Light • Graphite Dark • AIRI Tinted",12,false);sub.setTextColor(Color.rgb(103,111,124));root.addView(sub,lp(-1,-2,0,4,0,18));preview(root);option(root,"Crystal Clear",AiriIconPack.CLEAR);option(root,"Pearl Light",AiriIconPack.LIGHT);option(root,"Graphite Dark",AiriIconPack.DARK);option(root,"AIRI Tinted",AiriIconPack.TINTED);setContentView(sc);}
    private void preview(LinearLayout root){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);String[] pkgs={"com.android.settings","com.android.camera","com.android.contacts","com.android.calculator2"};for(String pkg:pkgs){ImageView iv=new ImageView(this);Drawable d=null;try{d=getPackageManager().getApplicationIcon(pkg);}catch(Exception ignored){}iv.setImageDrawable(AiriIconPack.drawable(this,d,dp(70)));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(74),1);p.setMargins(dp(5),0,dp(5),0);row.addView(iv,p);}root.addView(row,lp(-1,dp(78),0,0,0,20));}
    private void option(LinearLayout root,String label,String id){TextView v=t((id.equals(AiriIconPack.style(this))?"✓  ":"○  ")+label,16,true);v.setGravity(Gravity.CENTER_VERTICAL);v.setPadding(dp(16),0,dp(16),0);v.setBackground(round(Color.WHITE,dp(24)));v.setElevation(dp(5));v.setOnClickListener(x->{AiriIconPack.setStyle(this,id);recreate();});root.addView(v,lp(-1,dp(64),0,0,0,10));}
    private android.graphics.drawable.GradientDrawable round(int c,float r){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(c);g.setCornerRadius(r);g.setStroke(dp(1),Color.rgb(230,234,241));return g;}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(24,28,35));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

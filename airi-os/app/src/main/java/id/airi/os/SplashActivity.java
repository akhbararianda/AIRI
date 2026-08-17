package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(8,10,17));getWindow().setNavigationBarColor(Color.rgb(8,10,17));build();new Handler(Looper.getMainLooper()).postDelayed(()->{startActivity(new Intent(this,MainActivity.class));overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);finish();},900);}
    private void build(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(dp(30),dp(60),dp(30),dp(50));android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(6,8,14),Color.rgb(20,24,38),Color.rgb(15,11,31)});root.setBackground(bg);AiriLogoView logo=new AiriLogoView(this);root.addView(logo,new LinearLayout.LayoutParams(dp(180),dp(180)));TextView title=t("AIRI OS",36,true);title.setLetterSpacing(.18f);root.addView(title,lp(-1,-2,0,22,0,0));TextView sub=t("INFINITY STABLE",12,true);sub.setLetterSpacing(.28f);sub.setTextColor(Color.rgb(184,198,232));root.addView(sub,lp(-1,-2,0,8,0,0));TextView future=t("Future Core • Android 11",10,false);future.setTextColor(Color.rgb(126,143,178));root.addView(future,lp(-1,-2,0,12,0,0));setContentView(root);}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setGravity(Gravity.CENTER);v.setTextColor(Color.WHITE);v.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

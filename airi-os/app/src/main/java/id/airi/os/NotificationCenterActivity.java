package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class NotificationCenterActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView s=new ScrollView(this);LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(18),dp(28),dp(18),dp(28));r.setBackgroundColor(Color.rgb(219,235,247));s.addView(r,new ScrollView.LayoutParams(-1,-2));r.addView(t("Notification Center",30,true));TextView sub=t("AIRI • private on-device notification view",12,false);sub.setTextColor(Color.rgb(79,97,113));r.addView(sub,lp(-1,-2,0,2,0,14));TextView grant=button("Allow notification access");grant.setOnClickListener(v->{try{startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}});r.addView(grant,lp(-1,dp(48),0,0,0,14));synchronized(AiriNotificationListener.RECENT){if(AiriNotificationListener.RECENT.isEmpty()){TextView empty=t("Belum ada notifikasi yang dibaca AIRI. Aktifkan Notification Access, lalu notifikasi baru akan muncul di sini.",14,false);empty.setPadding(dp(16),dp(18),dp(16),dp(18));empty.setBackground(AiriGlassDrawable.make(this,28,AiriGlassDrawable.REGULAR));r.addView(empty);}else for(String item:AiriNotificationListener.RECENT){TextView n=t(item,14,false);n.setPadding(dp(16),dp(14),dp(16),dp(14));n.setBackground(AiriGlassDrawable.make(this,26,AiriGlassDrawable.REGULAR));n.setElevation(dp(7));r.addView(n,lp(-1,-2,0,0,0,9));}}setContentView(s);AiriLiquidSkin.apply(this);}
    private TextView button(String x){TextView v=t(x,14,true);v.setGravity(Gravity.CENTER);v.setTextColor(Color.WHITE);v.setBackground(AiriGlassDrawable.make(this,25,AiriGlassDrawable.BLUE));return v;}private TextView t(String x,float z,boolean b){TextView v=new TextView(this);v.setText(x);v.setTextSize(z);v.setTextColor(Color.rgb(18,28,38));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int rr,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(rr),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

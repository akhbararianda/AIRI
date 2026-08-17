package id.airi.os;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StableSetupActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}

    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(28),dp(18),dp(26));root.setBackgroundColor(android.graphics.Color.rgb(226,237,246));
        root.addView(t("AIRI OS v8 Stable Setup",30,true));
        TextView sub=t("Finish these steps once for the complete AIRI experience.",12,false);sub.setTextColor(android.graphics.Color.rgb(75,93,110));root.addView(sub,lp(-1,-2,0,4,0,18));
        add(root,"1. Set AIRI as Home","Use AIRI as the default launcher",this::setHome);
        add(root,"2. Apply AIRI Wallpaper","Install the Frost Blue liquid wallpaper",()->startActivity(new Intent(this,AiriWallpaperActivity.class)));
        add(root,"3. Notification Center","Grant Android notification access",()->open(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        add(root,"4. Brightness Control","Allow AIRI to change brightness",()->startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,Uri.parse("package:"+getPackageName()))));
        add(root,"5. AIRI Control Center","Open the integrated system control surface",()->startActivity(new Intent(this,ControlCenterActivity.class)));
        add(root,"6. Screen Intelligence","Start consent-based screen capture",()->startActivity(new Intent(this,ScreenIntelligenceActivity.class)));
        add(root,"7. AIRI Intelligence","Irzuqni, Gallery AI and smart tools",()->startActivity(new Intent(this,IntelligenceHubActivity.class)));
        TextView note=t("AIRI Stable replaces the Home experience and adds its own wallpaper and control surfaces. Android Recents and the status bar remain part of the phone's OEM SystemUI unless AIRI is installed as a privileged ROM/system component.",11,false);note.setTextColor(android.graphics.Color.rgb(78,94,108));root.addView(note,lp(-1,-2,0,18,0,0));
        setContentView(root);AiriLiquidSkin.apply(this);
    }

    private void setHome(){
        if(Build.VERSION.SDK_INT>=29){RoleManager rm=(RoleManager)getSystemService(Context.ROLE_SERVICE);if(rm!=null&&rm.isRoleAvailable(RoleManager.ROLE_HOME)){startActivity(rm.createRequestRoleIntent(RoleManager.ROLE_HOME));return;}}
        open(Settings.ACTION_HOME_SETTINGS);
    }
    private void add(LinearLayout root,String title,String sub,Runnable r){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(16),dp(14),dp(14),dp(14));card.setBackground(AiriGlassDrawable.make(this,27,AiriGlassDrawable.REGULAR));card.setElevation(dp(9));card.addView(t(title,15,true));TextView s=t(sub,11,false);s.setTextColor(android.graphics.Color.rgb(75,92,108));card.addView(s);card.setOnClickListener(v->r.run());root.addView(card,lp(-1,dp(78),0,0,0,8));}
    private void open(String action){try{startActivity(new Intent(action));}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(android.graphics.Color.rgb(16,28,41));v.setTypeface(android.graphics.Typeface.create("sans",bold?1:0));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

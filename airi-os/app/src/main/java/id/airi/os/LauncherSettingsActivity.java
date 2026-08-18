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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class LauncherSettingsActivity extends Activity {
    private LinearLayout root;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(38),dp(18),dp(40));root.setBackgroundColor(AiriTheme.surface(this));sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        TextView title=t("Fusion Launcher",30,true);root.addView(title);TextView sub=t("AIRI v20 Ultimate Fusion • launcher controls",12,false);sub.setTextColor(AiriTheme.muted(this));root.addView(sub,lp(-1,-2,0,2,0,18));
        toggle("Predictive suggestions","Favorites + apps you launch most",LauncherPrefs.suggestions(this),v->LauncherPrefs.suggestions(this,v));
        toggle("Notification badges","Show per-app notification counters on Home, Focus and Dock",LauncherPrefs.badges(this),v->LauncherPrefs.badges(this,v));
        toggle("Smart Dock","Fill empty dock slots using favorites and usage intelligence",LauncherPrefs.smartDock(this),v->LauncherPrefs.smartDock(this,v));
        toggle("Auto categories","Group apps into Social, Media, Work, Games and more",LauncherPrefs.categories(this),v->LauncherPrefs.categories(this,v));
        toggle("App labels","Show app names below icons",LauncherPrefs.labels(this),v->LauncherPrefs.labels(this,v));
        toggle("Compact mode","Smaller app tiles in AIRI Library",LauncherPrefs.compact(this),v->LauncherPrefs.compact(this,v));
        action("Grid size",LauncherPrefs.columns(this)+" columns • tap to switch 4 / 5",()->{int next=LauncherPrefs.columns(this)==4?5:4;LauncherPrefs.columns(this,next);Toast.makeText(this,next+" columns",Toast.LENGTH_SHORT).show();recreate();});
        action("Smart Dock editor","Choose any installed app for all 5 dock slots",()->startActivity(new Intent(this,DockEditorActivity.class)));
        action("Focus Workspace",LauncherPrefs.favorites(this).size()+" favorites • priority apps and usage intelligence",()->startActivity(new Intent(this,FavoriteWorkspaceActivity.class)));
        action("Backup / Restore","Copy your Fusion layout or restore it later",()->startActivity(new Intent(this,LauncherBackupActivity.class)));
        action("Notification access","Required for live notification badges and AIRI Notification Center",()->{try{startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));}catch(Exception e){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception ignored){}}});
        action("Hidden apps",LauncherPrefs.hidden(this).size()+" hidden • tap to restore all",()->{LauncherPrefs.clearHidden(this);Toast.makeText(this,"All hidden apps restored",Toast.LENGTH_SHORT).show();recreate();});
        action("Default Home","Choose AIRI Fusion as your launcher",()->{try{startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(Exception ignored){}});
        action("Wallpaper","Open Android wallpaper picker",()->{try{startActivity(new Intent(Intent.ACTION_SET_WALLPAPER));}catch(Exception ignored){}});
        action("AIRI Control Center","Connectivity, brightness, sound and flashlight",()->startActivity(new Intent(this,ControlCenterActivity.class)));
        action("AIRI Notification Center","Notification surface and access",()->startActivity(new Intent(this,NotificationCenterActivity.class)));
        action("Reset launcher preferences","Reset grid, labels, categories, favorites, dock and hidden apps",()->{LauncherPrefs.reset(this);Toast.makeText(this,"Fusion preferences reset",Toast.LENGTH_SHORT).show();recreate();});
        setContentView(sc);AiriLiquidSkin.apply(this);}
    private interface BoolSet{void set(boolean v);}private void toggle(String title,String desc,boolean checked,BoolSet set){LinearLayout c=card();LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);TextView a=t(title,15,true);tx.addView(a);TextView b=t(desc,10.5f,false);b.setTextColor(AiriTheme.muted(this));tx.addView(b);row.addView(tx,new LinearLayout.LayoutParams(0,-2,1));Switch sw=new Switch(this);sw.setChecked(checked);sw.setOnCheckedChangeListener((x,v)->set.set(v));row.addView(sw);c.addView(row);root.addView(c,lp(-1,dp(82),0,0,0,9));}
    private void action(String title,String desc,Runnable r){LinearLayout c=card();TextView a=t(title,15,true);c.addView(a);TextView b=t(desc,10.5f,false);b.setTextColor(AiriTheme.muted(this));c.addView(b);c.setOnClickListener(v->r.run());root.addView(c,lp(-1,dp(78),0,0,0,9));}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(16),dp(12),dp(16),dp(12));c.setBackground(AiriGlassDrawable.make(this,26,AiriGlassDrawable.REGULAR));c.setElevation(dp(6));return c;}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(AiriTheme.ink(this));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:w,hh=h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h;LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

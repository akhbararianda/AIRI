package id.airi.os;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class ControlCenterActivity extends Activity {
    private AudioManager audio;private CameraManager camera;private String torchId;private boolean torchOn=false;
    @Override protected void onCreate(Bundle b){super.onCreate(b);audio=(AudioManager)getSystemService(Context.AUDIO_SERVICE);camera=(CameraManager)getSystemService(Context.CAMERA_SERVICE);findTorch();build();}
    private void findTorch(){try{for(String id:camera.getCameraIdList()){Boolean f=camera.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE);if(Boolean.TRUE.equals(f)){torchId=id;break;}}}catch(Exception ignored){}}
    private void build(){FrameLayout stage=new FrameLayout(this);stage.addView(new HyperFlowBackdropView(this),new FrameLayout.LayoutParams(-1,-1));LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(38),dp(18),dp(24));stage.addView(root,new FrameLayout.LayoutParams(-1,-1));
        TextView title=t("Control Center",29,true);root.addView(title);TextView sub=t("AIRI HyperFlow • Android 11",11,false);sub.setTextColor(Color.rgb(100,108,120));root.addView(sub,lp(-1,-2,0,2,0,14));
        GridLayout top=new GridLayout(this);top.setColumnCount(2);add(top,"Internet","Wi‑Fi • Bluetooth",()->open(Settings.Panel.ACTION_INTERNET_CONNECTIVITY),Color.rgb(91,143,255));add(top,"Focus","Do Not Disturb",()->open("android.settings.ZEN_MODE_SETTINGS"),Color.rgb(169,112,255));add(top,"Display","Rotation & display",()->open(Settings.ACTION_DISPLAY_SETTINGS),Color.rgb(255,159,95));add(top,"AIRI","Assistant & intelligence",()->open(new Intent(this,AssistantActivity.class)),Color.rgb(72,199,175));root.addView(top);
        root.addView(t("Brightness",12,true),lp(-1,-2,2,16,0,4));SeekBar bright=new SeekBar(this);bright.setMax(255);try{bright.setProgress(Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS));}catch(Exception e){bright.setProgress(128);}bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){if(f&&Settings.System.canWrite(ControlCenterActivity.this))Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,Math.max(1,p));}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){if(!Settings.System.canWrite(ControlCenterActivity.this))open(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,Uri.parse("package:"+getPackageName())));}});root.addView(bright);
        root.addView(t("Volume",12,true),lp(-1,-2,2,8,0,4));SeekBar vol=new SeekBar(this);vol.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));vol.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){if(f)audio.setStreamVolume(AudioManager.STREAM_MUSIC,p,0);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});root.addView(vol);
        GridLayout tools=new GridLayout(this);tools.setColumnCount(4);tool(tools,"🔦","Flash",this::toggleTorch);tool(tools,"◉","Camera",()->open(new Intent("android.media.action.IMAGE_CAPTURE")));tool(tools,"▤","Notify",()->open(new Intent(this,NotificationCenterActivity.class)));tool(tools,"▱","Screen",()->open(new Intent(this,ScreenIntelligenceActivity.class)));tool(tools,"⌕","Circle",()->open(new Intent(this,CircleSearchActivity.class)));tool(tools,"✿","Gallery",()->open(new Intent(this,GalleryLabActivity.class)));tool(tools,"⚡","Boost",()->open(new Intent(this,PerformanceCenterActivity.class)));tool(tools,"⚙","Settings",()->open(new Intent(this,AiriSettingsActivity.class)));root.addView(tools,lp(-1,-2,0,16,0,0));setContentView(stage);}
    private void add(GridLayout g,String a,String b,Runnable r,int accent){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(15),dp(14),dp(13),dp(12));c.setBackground(round(Color.argb(216,255,255,255),dp(27)));c.setElevation(dp(9));TextView dot=t("●",15,true);dot.setTextColor(accent);c.addView(dot);c.addView(t(a,15,true));TextView x=t(b,10,false);x.setTextColor(Color.rgb(101,109,121));c.addView(x);c.setOnClickListener(v->r.run());GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(100);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(4),dp(4),dp(4),dp(4));g.addView(c,p);}
    private void tool(GridLayout g,String icon,String name,Runnable r){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);TextView i=t(icon,21,true);i.setGravity(Gravity.CENTER);i.setBackground(round(Color.argb(205,255,255,255),dp(20)));i.setElevation(dp(5));c.addView(i,new LinearLayout.LayoutParams(dp(56),dp(56)));TextView n=t(name,9,true);n.setGravity(Gravity.CENTER);c.addView(n,new LinearLayout.LayoutParams(-1,dp(24)));c.setOnClickListener(v->r.run());GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(88);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(3),dp(2),dp(3));g.addView(c,p);}
    private void toggleTorch(){if(torchId==null)return;if(Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.CAMERA},701);return;}try{torchOn=!torchOn;camera.setTorchMode(torchId,torchOn);}catch(Exception ignored){}}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==701&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)toggleTorch();}
    private android.graphics.drawable.GradientDrawable round(int color,float radius){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(color);g.setCornerRadius(radius);g.setStroke(dp(1),Color.argb(70,255,255,255));return g;}
    private void open(String action){open(new Intent(action));}private void open(Intent i){try{startActivity(i);}catch(Exception e){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception ignored){}}}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(28,33,41));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

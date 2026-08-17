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
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class ControlCenterActivity extends Activity {
    private AudioManager audio;
    private CameraManager camera;
    private String torchId;
    private boolean torchOn=false;

    @Override protected void onCreate(Bundle b){super.onCreate(b);audio=(AudioManager)getSystemService(Context.AUDIO_SERVICE);camera=(CameraManager)getSystemService(Context.CAMERA_SERVICE);findTorch();build();}

    private void findTorch(){try{for(String id:camera.getCameraIdList()){Boolean f=camera.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE);if(Boolean.TRUE.equals(f)){torchId=id;break;}}}catch(Exception ignored){}}

    private void build(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(26),dp(18),dp(26));root.setBackgroundColor(Color.rgb(218,235,247));
        TextView title=t("Control Center",30,true);root.addView(title);TextView sub=t("AIRI • Android 11 system controls",12,false);sub.setTextColor(Color.rgb(77,98,116));root.addView(sub,lp(-1,-2,0,2,0,16));
        GridLayout top=new GridLayout(this);top.setColumnCount(2);add(top,"✈  Connectivity","Wi‑Fi • Bluetooth • Network",()->open(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));add(top,"▶  Media","Volume & playback controls",()->{});add(top,"☾  Focus","Do Not Disturb settings",()->open(Settings.ACTION_ZEN_MODE_SETTINGS));add(top,"↻  Rotation","Display settings",()->open(Settings.ACTION_DISPLAY_SETTINGS));root.addView(top);
        root.addView(t("Brightness",13,true),lp(-1,-2,2,18,0,4));SeekBar bright=new SeekBar(this);bright.setMax(255);try{bright.setProgress(Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS));}catch(Exception e){bright.setProgress(128);}bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){if(!f)return;if(Settings.System.canWrite(ControlCenterActivity.this)){try{Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,Math.max(1,p));}catch(Exception ignored){}}}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){if(!Settings.System.canWrite(ControlCenterActivity.this))open(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:"+getPackageName())));}});root.addView(bright);
        root.addView(t("Volume",13,true),lp(-1,-2,2,10,0,4));SeekBar vol=new SeekBar(this);int max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);vol.setMax(max);vol.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){if(f)audio.setStreamVolume(AudioManager.STREAM_MUSIC,p,0);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});root.addView(vol);
        GridLayout tools=new GridLayout(this);tools.setColumnCount(4);tool(tools,"🔦","Flash",this::toggleTorch);tool(tools,"◉","Camera",()->open(new Intent("android.media.action.IMAGE_CAPTURE")));tool(tools,"▣","QR",()->open(new Intent(this,CircleSearchActivity.class)));tool(tools,"✦","AIRI",()->open(new Intent(this,AssistantActivity.class)));tool(tools,"▤","Notify",()->open(new Intent(this,NotificationCenterActivity.class)));tool(tools,"▱","Screen",()->open(new Intent(this,ScreenIntelligenceActivity.class)));tool(tools,"⚙","Settings",()->open(new Intent(Settings.ACTION_SETTINGS)));tool(tools,"◐","Wallpaper",()->open(new Intent(this,WallpaperLabActivity.class)));root.addView(tools,lp(-1,-2,0,18,0,0));setContentView(root);AiriLiquidSkin.apply(this);}

    private void toggleTorch(){if(torchId==null)return;if(Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.CAMERA},701);return;}try{torchOn=!torchOn;camera.setTorchMode(torchId,torchOn);}catch(Exception ignored){}}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==701&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)toggleTorch();}
    private void add(GridLayout g,String a,String b,Runnable r){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(14),dp(14),dp(12),dp(12));c.setBackground(AiriGlassDrawable.make(this,28,AiriGlassDrawable.REGULAR));c.setElevation(dp(10));c.addView(t(a,15,true));TextView x=t(b,11,false);x.setTextColor(Color.rgb(82,100,115));c.addView(x);c.setOnClickListener(v->r.run());GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(82);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(4),dp(4),dp(4),dp(4));g.addView(c,p);}
    private void tool(GridLayout g,String icon,String name,Runnable r){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);TextView i=t(icon,22,true);i.setGravity(Gravity.CENTER);i.setBackground(AiriGlassDrawable.make(this,24,AiriGlassDrawable.CLEAR));c.addView(i,new LinearLayout.LayoutParams(dp(58),dp(58)));TextView n=t(name,10,true);n.setGravity(Gravity.CENTER);c.addView(n);c.setOnClickListener(v->r.run());GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(92);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(3),dp(2),dp(3));g.addView(c,p);}
    private void open(String action){open(new Intent(action));}private void open(Intent i){try{startActivity(i);}catch(Exception e){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception ignored){}}}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(20,29,39));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

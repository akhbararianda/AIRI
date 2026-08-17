package com.airi.ios266stable;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); configureWindow();
        webView = new WebView(this); webView.setBackgroundColor(Color.TRANSPARENT); webView.setOverScrollMode(View.OVER_SCROLL_NEVER); webView.setVerticalScrollBarEnabled(false); webView.setHorizontalScrollBarEnabled(false); webView.setLayerType(View.LAYER_TYPE_HARDWARE,null); setContentView(webView);
        WebSettings s=webView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(true); s.setMediaPlaybackRequiresUserGesture(false); s.setBuiltInZoomControls(false); s.setDisplayZoomControls(false); s.setSupportZoom(false); s.setLoadWithOverviewMode(false); s.setUseWideViewPort(false); s.setCacheMode(WebSettings.LOAD_DEFAULT); s.setTextZoom(100);
        webView.addJavascriptInterface(new AiriBridge(),"AIRI");
        webView.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r){return handleUri(r.getUrl());}@Override public boolean shouldOverrideUrlLoading(WebView v,String u){return handleUri(Uri.parse(u));}});
        if(savedInstanceState==null)webView.loadUrl("file:///android_asset/index.html");else webView.restoreState(savedInstanceState);
    }

    private void configureWindow(){
        Window w=getWindow(); w.setStatusBarColor(Color.TRANSPARENT); w.setNavigationBarColor(Color.TRANSPARENT); w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if(Build.VERSION.SDK_INT>=28){WindowManager.LayoutParams lp=w.getAttributes();lp.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;w.setAttributes(lp);}
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    @Override protected void onResume(){super.onResume();configureWindow();if(webView!=null){webView.onResume();webView.postDelayed(()->webView.evaluateJavascript("window.airiRefresh&&window.airiRefresh()",null),120);}}
    @Override protected void onPause(){if(webView!=null)webView.onPause();super.onPause();}
    @Override protected void onSaveInstanceState(Bundle b){if(webView!=null)webView.saveState(b);super.onSaveInstanceState(b);}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);configureWindow();if(webView!=null)webView.evaluateJavascript("window.airiHome&&window.airiHome()",null);}

    private boolean handleUri(Uri u){if(u==null||!"app".equalsIgnoreCase(u.getScheme()))return false;String p=u.getHost();if(p!=null)launchPackage(p);return true;}
    private void launchPackage(String pkg){
        if("airi.camera".equals(pkg)){startActivity(new Intent(this,CameraActivity.class));return;}
        if("settings".equals(pkg)){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception e){show("Pengaturan Android tidak tersedia");}return;}
        try{Intent in=getPackageManager().getLaunchIntentForPackage(pkg);if(in==null){if("com.android.contacts".equals(pkg)){in=getPackageManager().getLaunchIntentForPackage("com.android.dialer");if(in==null)in=getPackageManager().getLaunchIntentForPackage("com.google.android.dialer");if(in==null)in=new Intent(Intent.ACTION_DIAL);}else if("com.android.mms".equals(pkg)){in=getPackageManager().getLaunchIntentForPackage("com.google.android.apps.messaging");if(in==null)in=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING);}else if("com.coloros.filemanager".equals(pkg))in=new Intent(Intent.ACTION_GET_CONTENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);}if(in==null){show("Aplikasi belum tersedia di perangkat");return;}in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);startActivity(in);}catch(ActivityNotFoundException e){show("Aplikasi tidak ditemukan");}catch(Exception e){show("Tidak dapat membuka aplikasi");}}
    private void openAppInfo(String pkg){try{startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+pkg)));}catch(Exception e){show("Info aplikasi tidak tersedia");}}
    private void show(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    @Override public void onBackPressed(){if(webView==null){moveTaskToBack(true);return;}webView.evaluateJavascript("(window.airiBack?window.airiBack():false)",v->{if(v==null||"false".equals(v)||"null".equals(v))moveTaskToBack(true);});}

    private class AiriBridge{
        @JavascriptInterface public void launchApp(String p){runOnUiThread(()->launchPackage(p));}
        @JavascriptInterface public void openCamera(){runOnUiThread(()->startActivity(new Intent(MainActivity.this,CameraActivity.class)));}
        @JavascriptInterface public void openAppInfo(String p){runOnUiThread(()->MainActivity.this.openAppInfo(p));}
        @JavascriptInterface public void openHomeSettings(){runOnUiThread(()->{try{startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(Exception e){show("Menu launcher default tidak tersedia");}});}
        @JavascriptInterface public void openPanel(String panel){runOnUiThread(()->{try{Intent i;if("wifi".equals(panel)){if(Build.VERSION.SDK_INT>=29)i=new Intent(Settings.Panel.ACTION_WIFI);else i=new Intent(Settings.ACTION_WIFI_SETTINGS);}else if("bluetooth".equals(panel))i=new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);else if("display".equals(panel))i=new Intent(Settings.ACTION_DISPLAY_SETTINGS);else if("focus".equals(panel))i=new Intent(Settings.ACTION_ZEN_MODE_SETTINGS);else i=new Intent(Settings.ACTION_SETTINGS);startActivity(i);}catch(Exception e){show("Panel sistem tidak tersedia");}});}
        @JavascriptInterface public void setBrightness(int value){runOnUiThread(()->{WindowManager.LayoutParams lp=getWindow().getAttributes();lp.screenBrightness=Math.max(.05f,Math.min(1f,value/100f));getWindow().setAttributes(lp);});}
        @JavascriptInterface public void setVolume(int value){runOnUiThread(()->{AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);if(am!=null){int max=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);am.setStreamVolume(AudioManager.STREAM_MUSIC,Math.round(max*Math.max(0,Math.min(100,value))/100f),0);}});}
        @JavascriptInterface public String getInstalledApps(){JSONArray out=new JSONArray();try{PackageManager pm=getPackageManager();List<ApplicationInfo> all=pm.getInstalledApplications(PackageManager.GET_META_DATA),ls=new ArrayList<>();for(ApplicationInfo ai:all){if(ai.packageName.equals(getPackageName())||ai.packageName.startsWith("com.airi.ios266"))continue;if(pm.getLaunchIntentForPackage(ai.packageName)!=null)ls.add(ai);}Collections.sort(ls,new Comparator<ApplicationInfo>(){@Override public int compare(ApplicationInfo a,ApplicationInfo b){return String.valueOf(pm.getApplicationLabel(a)).compareToIgnoreCase(String.valueOf(pm.getApplicationLabel(b)));}});for(ApplicationInfo ai:ls){JSONObject x=new JSONObject();x.put("name",String.valueOf(pm.getApplicationLabel(ai)));x.put("pkg",ai.packageName);out.put(x);}}catch(Exception ignored){}return out.toString();}
        @JavascriptInterface public String getDeviceStatus(){JSONObject o=new JSONObject();try{BatteryManager bm=(BatteryManager)getSystemService(BATTERY_SERVICE);int bat=bm!=null?bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY):-1;ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE);ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo();if(am!=null)am.getMemoryInfo(mi);long ram=mi.totalMem;StatFs fs=new StatFs(Environment.getDataDirectory().getAbsolutePath());long total=fs.getBlockCountLong()*fs.getBlockSizeLong(),free=fs.getAvailableBlocksLong()*fs.getBlockSizeLong();double gb=1024d*1024d*1024d;DisplayMetrics dm=new DisplayMetrics();getWindowManager().getDefaultDisplay().getRealMetrics(dm);o.put("battery",bat<0?76:bat);o.put("ramGb",String.format(Locale.US,"%.1f",ram/gb));o.put("totalGb",String.format(Locale.US,"%.0f",total/gb));o.put("freeGb",String.format(Locale.US,"%.1f",free/gb));o.put("widthPx",dm.widthPixels);o.put("heightPx",dm.heightPixels);o.put("density",dm.density);o.put("lite",(am!=null&&am.isLowRamDevice())||(ram>0&&ram<=5L*1024L*1024L*1024L));}catch(Exception ignored){}return o.toString();}
    }
}

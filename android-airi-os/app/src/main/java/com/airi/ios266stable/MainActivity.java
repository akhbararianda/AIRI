package com.airi.ios266stable;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();

        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setLoadWithOverviewMode(false);
        s.setUseWideViewPort(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setTextZoom(100);

        webView.addJavascriptInterface(new AiriBridge(), "AIRI");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUri(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUri(Uri.parse(url));
            }
        });

        if (savedInstanceState == null) webView.loadUrl("file:///android_asset/index.html");
        else webView.restoreState(savedInstanceState);
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        configureWindow();
        if (webView != null) {
            webView.onResume();
            webView.postDelayed(() -> webView.evaluateJavascript("window.airiRefresh&&window.airiRefresh()", null), 120);
        }
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        configureWindow();
        if (webView != null) webView.evaluateJavascript("window.airiHome&&window.airiHome()", null);
    }

    private boolean handleUri(Uri uri) {
        if (uri == null || !"app".equalsIgnoreCase(uri.getScheme())) return false;
        String pkg = uri.getHost();
        if (pkg == null || pkg.trim().isEmpty()) return true;
        launchPackage(pkg);
        return true;
    }

    private void launchPackage(String pkg) {
        if ("settings".equals(pkg)) {
            try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
            catch (Exception e) { show("Pengaturan Android tidak tersedia"); }
            return;
        }
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
            if (intent == null) {
                if ("com.android.contacts".equals(pkg)) {
                    intent = getPackageManager().getLaunchIntentForPackage("com.android.dialer");
                    if (intent == null) intent = getPackageManager().getLaunchIntentForPackage("com.google.android.dialer");
                    if (intent == null) intent = new Intent(Intent.ACTION_DIAL);
                } else if ("com.android.mms".equals(pkg)) {
                    intent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.messaging");
                    if (intent == null) intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING);
                } else if ("com.oppo.camera".equals(pkg)) {
                    intent = new Intent("android.media.action.IMAGE_CAPTURE");
                } else if ("com.coloros.filemanager".equals(pkg)) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
                }
            }
            if (intent == null) { show("Aplikasi belum tersedia di perangkat"); return; }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            show("Aplikasi tidak ditemukan");
        } catch (Exception e) {
            show("Tidak dapat membuka aplikasi");
        }
    }

    private void openAppInfo(String pkg) {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkg)));
        } catch (Exception e) { show("Info aplikasi tidak tersedia"); }
    }

    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (webView == null) { moveTaskToBack(true); return; }
        webView.evaluateJavascript("(window.airiBack?window.airiBack():false)", value -> {
            if (value == null || "false".equals(value) || "null".equals(value)) moveTaskToBack(true);
        });
    }

    private class AiriBridge {
        @JavascriptInterface
        public void launchApp(String pkg) {
            runOnUiThread(() -> launchPackage(pkg));
        }

        @JavascriptInterface
        public void openAppInfo(String pkg) {
            runOnUiThread(() -> MainActivity.this.openAppInfo(pkg));
        }

        @JavascriptInterface
        public void openHomeSettings() {
            runOnUiThread(() -> {
                try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }
                catch (Exception e) { show("Menu launcher default tidak tersedia"); }
            });
        }

        @JavascriptInterface
        public String getInstalledApps() {
            JSONArray out = new JSONArray();
            try {
                PackageManager pm = getPackageManager();
                List<ApplicationInfo> all = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                List<ApplicationInfo> launchable = new ArrayList<>();
                for (ApplicationInfo ai : all) {
                    if (ai.packageName.equals(getPackageName()) || ai.packageName.startsWith("com.airi.ios266")) continue;
                    if (pm.getLaunchIntentForPackage(ai.packageName) != null) launchable.add(ai);
                }
                Collections.sort(launchable, new Comparator<ApplicationInfo>() {
                    @Override
                    public int compare(ApplicationInfo a, ApplicationInfo b) {
                        return String.valueOf(pm.getApplicationLabel(a)).compareToIgnoreCase(String.valueOf(pm.getApplicationLabel(b)));
                    }
                });
                for (ApplicationInfo ai : launchable) {
                    JSONObject x = new JSONObject();
                    x.put("name", String.valueOf(pm.getApplicationLabel(ai)));
                    x.put("pkg", ai.packageName);
                    out.put(x);
                }
            } catch (Exception ignored) {}
            return out.toString();
        }

        @JavascriptInterface
        public String getDeviceStatus() {
            JSONObject o = new JSONObject();
            try {
                BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                int battery = bm != null ? bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) : -1;
                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                if (am != null) am.getMemoryInfo(mi);
                long ram = mi.totalMem;
                StatFs fs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
                long total = fs.getBlockCountLong() * fs.getBlockSizeLong();
                long free = fs.getAvailableBlocksLong() * fs.getBlockSizeLong();
                double gb = 1024d * 1024d * 1024d;
                o.put("battery", battery < 0 ? 76 : battery);
                o.put("ramGb", String.format(Locale.US, "%.1f", ram / gb));
                o.put("totalGb", String.format(Locale.US, "%.0f", total / gb));
                o.put("freeGb", String.format(Locale.US, "%.1f", free / gb));
                o.put("lite", (am != null && am.isLowRamDevice()) || (ram > 0 && ram <= 5L * 1024L * 1024L * 1024L));
            } catch (Exception ignored) {}
            return o.toString();
        }
    }
}

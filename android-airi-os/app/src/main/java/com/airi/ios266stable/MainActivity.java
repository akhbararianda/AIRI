package com.airi.ios266stable;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

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

        webView.loadUrl("file:///android_asset/index.html");
    }

    private boolean handleUri(Uri uri) {
        if (uri == null) return false;
        if (!"app".equalsIgnoreCase(uri.getScheme())) return false;

        String pkg = uri.getHost();
        if (pkg == null || pkg.trim().isEmpty()) {
            show("Shortcut aplikasi tidak valid");
            return true;
        }

        if ("settings".equals(pkg)) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
            return true;
        }

        launchPackage(pkg);
        return true;
    }

    private void launchPackage(String pkg) {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
            if (intent == null) {
                if ("com.android.contacts".equals(pkg)) {
                    intent = new Intent(Intent.ACTION_DIAL);
                } else if ("com.android.mms".equals(pkg)) {
                    intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING);
                } else if ("com.oppo.camera".equals(pkg)) {
                    intent = new Intent("android.media.action.IMAGE_CAPTURE");
                } else if ("com.coloros.filemanager".equals(pkg)) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT)
                            .setType("*/*")
                            .addCategory(Intent.CATEGORY_OPENABLE);
                }
            }

            if (intent == null) {
                show("Aplikasi belum tersedia di perangkat");
                return;
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            show("Aplikasi tidak ditemukan");
        } catch (Exception e) {
            show("Tidak dapat membuka aplikasi");
        }
    }

    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            moveTaskToBack(true);
        }
    }
}

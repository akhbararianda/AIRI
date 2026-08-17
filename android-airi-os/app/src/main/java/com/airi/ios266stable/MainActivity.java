package com.airi.ios266stable;

import android.app.*;
import android.app.role.RoleManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.*;
import android.widget.*;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int REQ_VOICE = 71;
    private final ArrayList<AppItem> apps = new ArrayList<>();
    private final ArrayList<AppItem> smartApps = new ArrayList<>();
    private GridView grid;
    private FrameLayout root;
    private LinearLayout dock;
    private TextView island, ownerGreeting, storageHint, deviceCard;
    private SharedPreferences prefs;
    private float downX, downY, startX, startY;
    private Handler clockHandler = new Handler(Looper.getMainLooper());
    private TextView clockSmall, clockBig, dateText;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("airi7_owner", MODE_PRIVATE);
        if (!prefs.contains("owner_name")) prefs.edit().putString("owner_name", "Akhbar").apply();
        configureWindow();
        buildUi();
        ensureHomeRole();
        clockHandler.post(clockTick);
    }

    @Override protected void onResume() {
        super.onResume();
        configureWindow();
        loadApps();
        updateOwnerUi();
        rebuildDock();
    }

    @Override protected void onDestroy() {
        clockHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            Date now = new Date();
            String t = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now);
            if (clockSmall != null) clockSmall.setText(t);
            if (clockBig != null) clockBig.setText(t);
            if (dateText != null) dateText.setText(new SimpleDateFormat("EEEE, d MMMM", new Locale("id","ID")).format(now));
            if (ownerGreeting != null) ownerGreeting.setText(greeting());
            clockHandler.postDelayed(this, 30000);
        }
    };

    private void configureWindow() {
        Window w = getWindow();
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            w.setAttributes(lp);
        }
        w.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private int dp(float v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    private GradientDrawable rounded(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius));
        return g;
    }
    private TextView text(String s, int sp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextColor(Color.WHITE); t.setTextSize(sp);
        if (bold) t.setTypeface(null, 1);
        return t;
    }

    private void buildUi() {
        root = new FrameLayout(this);
        GradientDrawable wall = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xff66d9ff, 0xff5659ef, 0xff1a2048, 0xff8b3fc5, 0xffff6e7c});
        root.setBackground(wall);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(15), dp(46), dp(15), dp(105));
        root.addView(main, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout status = new LinearLayout(this);
        status.setGravity(Gravity.CENTER_VERTICAL);
        clockSmall = text("--:--", 15, true);
        status.addView(clockSmall, new LinearLayout.LayoutParams(0, dp(36), 1));
        TextView model = text("RMX1851 • Android 11", 11, true);
        model.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        status.addView(model, new LinearLayout.LayoutParams(0, dp(36), 1));
        main.addView(status);

        ownerGreeting = text(greeting(), 18, true);
        ownerGreeting.setPadding(dp(3), dp(4), 0, dp(8));
        main.addView(ownerGreeting);

        LinearLayout cards = new LinearLayout(this);
        cards.setPadding(0, 0, 0, dp(9));

        LinearLayout timeCard = new LinearLayout(this);
        timeCard.setOrientation(LinearLayout.VERTICAL);
        timeCard.setPadding(dp(17), dp(12), dp(17), dp(10));
        timeCard.setBackground(rounded(0x36ffffff, 27));
        clockBig = text("--:--", 45, false);
        timeCard.addView(clockBig);
        dateText = text("", 13, true);
        timeCard.addView(dateText);
        TextView ownerSub = text("AIRI OS Stable 7 • Owner Edition", 10, false);
        ownerSub.setTextColor(0xccffffff);
        timeCard.addView(ownerSub);
        cards.addView(timeCard, new LinearLayout.LayoutParams(0, dp(126), 3));

        LinearLayout devBox = new LinearLayout(this);
        devBox.setOrientation(LinearLayout.VERTICAL);
        devBox.setGravity(Gravity.CENTER);
        devBox.setPadding(dp(8), dp(8), dp(8), dp(8));
        devBox.setBackground(rounded(0x36ffffff, 27));
        deviceCard = text(deviceSummary(), 11, true);
        deviceCard.setGravity(Gravity.CENTER);
        devBox.addView(deviceCard, new LinearLayout.LayoutParams(-1, 0, 1));
        storageHint = text(storageLabel(), 9, false);
        storageHint.setGravity(Gravity.CENTER);
        devBox.addView(storageHint);
        LinearLayout.LayoutParams devLp = new LinearLayout.LayoutParams(0, dp(126), 1);
        devLp.leftMargin = dp(9);
        cards.addView(devBox, devLp);
        main.addView(cards);

        LinearLayout quick = new LinearLayout(this);
        addQuick(quick, "Control", v -> showControl());
        addQuick(quick, "AIRI AI", v -> showAssistant());
        addQuick(quick, "Owner", v -> showOwnerHub());
        addQuick(quick, "Apps", v -> showDrawer());
        main.addView(quick);

        grid = new GridView(this);
        grid.setNumColumns(4);
        grid.setVerticalSpacing(dp(9));
        grid.setHorizontalSpacing(dp(5));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setSelector(android.R.color.transparent);
        grid.setPadding(0, dp(9), 0, 0);
        main.addView(grid, new LinearLayout.LayoutParams(-1, 0, 1));

        dock = new LinearLayout(this);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(dp(10), dp(8), dp(10), dp(8));
        dock.setBackground(rounded(0x55ffffff, 30));
        FrameLayout.LayoutParams dockLp = new FrameLayout.LayoutParams(dp(358), dp(80), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        dockLp.bottomMargin = dp(10);
        root.addView(dock, dockLp);

        island = text("AIRI", 12, true);
        island.setGravity(Gravity.CENTER);
        island.setBackground(rounded(Color.BLACK, 24));
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(dp(132), dp(38));
        ip.leftMargin = prefs.getInt("island_x", (getResources().getDisplayMetrics().widthPixels - dp(132)) / 2);
        ip.topMargin = prefs.getInt("island_y", dp(10));
        root.addView(island, ip);
        island.setOnTouchListener((v, e) -> dragIsland(e));
        island.setOnClickListener(v -> showOwnerHub());

        setContentView(root);
        loadApps();
        rebuildDock();
    }

    private void addQuick(LinearLayout row, String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setTextSize(10);
        b.setBackground(rounded(0x33ffffff, 17));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1);
        lp.rightMargin = dp(5);
        row.addView(b, lp); b.setOnClickListener(listener);
    }

    private boolean dragIsland(MotionEvent e) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) island.getLayoutParams();
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            downX = e.getRawX(); downY = e.getRawY(); startX = lp.leftMargin; startY = lp.topMargin; return true;
        }
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            int nx = (int) (startX + e.getRawX() - downX);
            int ny = (int) (startY + e.getRawY() - downY);
            nx = Math.max(0, Math.min(root.getWidth() - island.getWidth(), nx));
            ny = Math.max(0, Math.min(root.getHeight() - island.getHeight(), ny));
            lp.leftMargin = nx; lp.topMargin = ny; island.setLayoutParams(lp); return true;
        }
        if (e.getAction() == MotionEvent.ACTION_UP) {
            prefs.edit().putInt("island_x", lp.leftMargin).putInt("island_y", lp.topMargin).apply(); return true;
        }
        return false;
    }

    private String ownerName() { return prefs.getString("owner_name", "Akhbar").trim(); }
    private String greeting() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String p = h < 11 ? "Selamat pagi" : h < 15 ? "Selamat siang" : h < 19 ? "Selamat sore" : "Selamat malam";
        return p + ", " + ownerName();
    }

    private int batteryPct() {
        try {
            BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
            return bm == null ? 0 : bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } catch (Exception e) { return 0; }
    }

    private double freeStorageGb() {
        try {
            StatFs fs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            return fs.getAvailableBytes() / 1073741824d;
        } catch (Exception e) { return -1; }
    }

    private double totalStorageGb() {
        try {
            StatFs fs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            return fs.getTotalBytes() / 1073741824d;
        } catch (Exception e) { return -1; }
    }

    private String kernel() {
        String k = System.getProperty("os.version");
        return k == null ? "?" : k;
    }

    private String deviceSummary() {
        return batteryPct() + "%\n6 GB RAM\nAndroid " + Build.VERSION.RELEASE;
    }

    private String storageLabel() {
        double f = freeStorageGb();
        if (f < 0) return "Storage";
        return String.format(Locale.US, "%.1f GB kosong", f);
    }

    private void updateOwnerUi() {
        if (ownerGreeting != null) ownerGreeting.setText(greeting());
        if (deviceCard != null) deviceCard.setText(deviceSummary());
        if (storageHint != null) {
            double f = freeStorageGb();
            storageHint.setText(storageLabel() + (f >= 0 && f < 5 ? " • perlu dibersihkan" : ""));
            storageHint.setTextColor(f >= 0 && f < 5 ? 0xffffe38a : 0xffffffff);
        }
    }

    private void loadApps() {
        apps.clear();
        Intent i = new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(i, 0);
        Collections.sort(list, (a,b) -> String.valueOf(a.loadLabel(getPackageManager())).compareToIgnoreCase(String.valueOf(b.loadLabel(getPackageManager()))));
        HashSet<String> seen = new HashSet<>();
        for (ResolveInfo r : list) {
            String pkg = r.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || pkg.startsWith("com.airi.ios266")) continue;
            if (seen.add(pkg)) apps.add(new AppItem(String.valueOf(r.loadLabel(getPackageManager())), pkg, r));
        }
        if (grid != null) grid.setAdapter(new HomeAdapter());
        buildSmartApps();
    }

    private class HomeAdapter extends BaseAdapter {
        public int getCount() { return Math.min(16, apps.size()); }
        public Object getItem(int p) { return apps.get(p); }
        public long getItemId(int p) { return p; }
        public View getView(int p, View v, ViewGroup parent) { return appView(apps.get(p), 56, 10, true); }
    }

    private View appView(AppItem a, int iconDp, int textSp, boolean longPress) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL); l.setGravity(Gravity.CENTER); l.setPadding(dp(2),dp(2),dp(2),dp(2));
        ImageView im = new ImageView(this);
        im.setImageDrawable(a.r.loadIcon(getPackageManager()));
        l.addView(im, new LinearLayout.LayoutParams(dp(iconDp), dp(iconDp)));
        TextView n = text(a.name, textSp, false); n.setGravity(Gravity.CENTER); n.setSingleLine(true);
        l.addView(n, new LinearLayout.LayoutParams(-1, dp(22)));
        l.setOnClickListener(v -> launch(a.pkg));
        if (longPress) l.setOnLongClickListener(v -> { openAppInfo(a.pkg); return true; });
        return l;
    }

    private static class AppItem {
        String name, pkg; ResolveInfo r;
        AppItem(String n, String p, ResolveInfo x) { name=n; pkg=p; r=x; }
    }

    private void buildSmartApps() {
        smartApps.clear();
        boolean smart = prefs.getBoolean("smart_dock", true);
        if (smart && hasUsageAccess()) {
            try {
                UsageStatsManager usm = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
                long end = System.currentTimeMillis(), start = end - 7L*24*60*60*1000;
                List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
                HashMap<String,Long> totals = new HashMap<>();
                if (stats != null) for (UsageStats s : stats) totals.put(s.getPackageName(), totals.getOrDefault(s.getPackageName(), 0L) + s.getTotalTimeInForeground());
                ArrayList<Map.Entry<String,Long>> rank = new ArrayList<>(totals.entrySet());
                rank.sort((a,b) -> Long.compare(b.getValue(), a.getValue()));
                for (Map.Entry<String,Long> e : rank) {
                    AppItem item = findApp(e.getKey());
                    if (item != null && !containsPkg(smartApps,item.pkg)) smartApps.add(item);
                    if (smartApps.size() >= 4) break;
                }
            } catch (Exception ignored) {}
        }
        String[] fallback = {"com.android.contacts", "com.whatsapp", "com.android.chrome", "com.google.android.youtube"};
        for (String pkg : fallback) {
            AppItem item = findApp(pkg);
            if (item != null && !containsPkg(smartApps,pkg)) smartApps.add(item);
            if (smartApps.size() >= 4) break;
        }
        for (AppItem a : apps) {
            if (!containsPkg(smartApps,a.pkg)) smartApps.add(a);
            if (smartApps.size() >= 4) break;
        }
    }

    private AppItem findApp(String pkg) { for (AppItem a:apps) if (a.pkg.equals(pkg)) return a; return null; }
    private boolean containsPkg(List<AppItem> list, String pkg) { for (AppItem a:list) if (a.pkg.equals(pkg)) return true; return false; }

    private void rebuildDock() {
        if (dock == null) return;
        buildSmartApps(); dock.removeAllViews();
        for (int i=0;i<Math.min(4, smartApps.size());i++) {
            AppItem a = smartApps.get(i);
            LinearLayout slot = new LinearLayout(this); slot.setGravity(Gravity.CENTER);
            ImageView im = new ImageView(this); im.setImageDrawable(a.r.loadIcon(getPackageManager()));
            slot.addView(im, new LinearLayout.LayoutParams(dp(56),dp(56)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(64), 1); lp.leftMargin=dp(6);lp.rightMargin=dp(6);
            dock.addView(slot, lp); slot.setOnClickListener(v -> launch(a.pkg)); slot.setOnLongClickListener(v->{openAppInfo(a.pkg);return true;});
        }
    }

    private boolean hasUsageAccess() {
        try {
            UsageStatsManager u = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            List<UsageStats> s = u.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis()-3600000, System.currentTimeMillis());
            return s != null && !s.isEmpty();
        } catch (Exception e) { return false; }
    }

    private void launch(String pkg) {
        try {
            if (pkg.equals("airi.camera")) { startActivity(new Intent(this, CameraActivity.class)); return; }
            Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
            if (i == null && pkg.equals("com.android.contacts")) i = new Intent(Intent.ACTION_DIAL);
            if (i == null) { Toast.makeText(this,"Aplikasi tidak tersedia",Toast.LENGTH_SHORT).show(); return; }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(i);
        } catch (Exception e) { Toast.makeText(this,"Tidak dapat membuka aplikasi",Toast.LENGTH_SHORT).show(); }
    }

    private void openAppInfo(String pkg) {
        try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+pkg))); }
        catch (Exception ignored) {}
    }

    private void showDrawer() {
        Dialog d = new Dialog(this);
        LinearLayout box = dialogBox();
        box.addView(text("Semua Aplikasi",22,true));
        EditText search = edit("Cari aplikasi"); box.addView(search,new LinearLayout.LayoutParams(-1,dp(48)));
        GridView g = new GridView(this); g.setNumColumns(4); g.setVerticalSpacing(dp(10));
        final ArrayList<AppItem> shown = new ArrayList<>(apps);
        BaseAdapter ad = new BaseAdapter(){public int getCount(){return shown.size();}public Object getItem(int p){return shown.get(p);}public long getItemId(int p){return p;}public View getView(int p,View v,ViewGroup x){return appView(shown.get(p),50,9,true);}};
        g.setAdapter(ad); box.addView(g,new LinearLayout.LayoutParams(-1,0,1));
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){shown.clear();String q=s.toString().toLowerCase();for(AppItem x:apps)if(x.name.toLowerCase().contains(q))shown.add(x);ad.notifyDataSetChanged();}public void afterTextChanged(android.text.Editable e){}});
        d.setContentView(box); d.show(); sizeBottomDialog(d,.80f);
    }

    private void showOwnerHub() {
        Dialog d = new Dialog(this);
        LinearLayout b = dialogBox();
        b.addView(text("Owner Hub",24,true));
        TextView info = text("RMX1851 • Android 11 • kernel " + kernel(),10,false); info.setTextColor(0xbbffffff); b.addView(info);
        EditText name = edit("Nama pemilik"); name.setText(ownerName()); b.addView(name,new LinearLayout.LayoutParams(-1,dp(52)));

        Switch smart = new Switch(this); smart.setText("Smart Dock belajar aplikasi favorit secara lokal"); smart.setTextColor(Color.WHITE); smart.setChecked(prefs.getBoolean("smart_dock",true)); b.addView(smart);
        Button usage = button(hasUsageAccess()?"Usage Access aktif":"Aktifkan Usage Access"); b.addView(usage); usage.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));}catch(Exception ignored){}});
        Button storage = button("Storage Guardian • " + storageLabel()); b.addView(storage); storage.setOnClickListener(v->openStorageManager());
        Button resetIsland = button("Reset posisi Island"); b.addView(resetIsland); resetIsland.setOnClickListener(v->{prefs.edit().remove("island_x").remove("island_y").apply();Toast.makeText(this,"Posisi Island direset",Toast.LENGTH_SHORT).show();d.dismiss();recreate();});
        Button save = button("Simpan Owner Profile"); b.addView(save); save.setOnClickListener(v->{String n=name.getText().toString().trim();if(!n.isEmpty())prefs.edit().putString("owner_name",n).putBoolean("smart_dock",smart.isChecked()).apply();updateOwnerUi();rebuildDock();d.dismiss();});
        d.setContentView(b); d.show(); sizeDialog(d,.92f);
    }

    private void showControl() {
        Dialog d = new Dialog(this); LinearLayout b = dialogBox(); b.addView(text("AIRI Control Center",23,true));
        String[] names={"Wi‑Fi","Bluetooth","Display","Focus / DND","Notification Access","Live Island Overlay","Default Home","AIRI Camera","Storage Guardian","Usage Access"};
        for(String n:names){Button x=button(n);b.addView(x);x.setOnClickListener(v->{d.dismiss();control(n);});}
        TextView bl=text("Brightness",11,true);b.addView(bl);SeekBar br=new SeekBar(this);br.setMax(100);br.setProgress(85);b.addView(br);br.setOnSeekBarChangeListener(new SimpleSeek(){public void onProgressChanged(SeekBar s,int p,boolean f){WindowManager.LayoutParams lp=getWindow().getAttributes();lp.screenBrightness=Math.max(.05f,p/100f);getWindow().setAttributes(lp);}});
        TextView vl=text("Volume",11,true);b.addView(vl);SeekBar vol=new SeekBar(this);vol.setMax(100);vol.setProgress(60);b.addView(vol);vol.setOnSeekBarChangeListener(new SimpleSeek(){public void onProgressChanged(SeekBar s,int p,boolean f){AudioManager a=(AudioManager)getSystemService(AUDIO_SERVICE);if(a!=null){int m=a.getStreamMaxVolume(AudioManager.STREAM_MUSIC);a.setStreamVolume(AudioManager.STREAM_MUSIC,Math.round(m*p/100f),0);}}});
        d.setContentView(b);d.show();sizeDialog(d,.92f);
    }

    private abstract class SimpleSeek implements SeekBar.OnSeekBarChangeListener {public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}}

    private void control(String n) {
        try {
            if(n.equals("Wi‑Fi")) startActivity(Build.VERSION.SDK_INT>=29?new Intent(Settings.Panel.ACTION_WIFI):new Intent(Settings.ACTION_WIFI_SETTINGS));
            else if(n.equals("Bluetooth")) startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            else if(n.equals("Display")) startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
            else if(n.startsWith("Focus")) startActivity(new Intent("android.settings.ZEN_MODE_SETTINGS"));
            else if(n.startsWith("Notification")) startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            else if(n.startsWith("Live Island")){if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this))startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));else startIsland();}
            else if(n.startsWith("Default")) ensureHomeRole();
            else if(n.contains("Camera")) startActivity(new Intent(this,CameraActivity.class));
            else if(n.startsWith("Storage")) openStorageManager();
            else if(n.startsWith("Usage")) startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } catch(Exception e){Toast.makeText(this,"Fitur tidak tersedia pada sistem ini",Toast.LENGTH_SHORT).show();}
    }

    private void openStorageManager() {
        try { startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)); }
        catch (Exception e) { try { startActivity(new Intent(Settings.ACTION_MANAGE_STORAGE)); } catch(Exception ignored){} }
    }

    private void ensureHomeRole() {
        try {
            if(Build.VERSION.SDK_INT>=29){RoleManager rm=(RoleManager)getSystemService(ROLE_SERVICE);if(rm!=null&&!rm.isRoleHeld(RoleManager.ROLE_HOME))startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME),91);}
            else startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch(Exception ignored){}
    }

    private void startIsland() {
        prefs.edit().putBoolean("overlay",true).apply();
        Intent i=new Intent(this,IslandOverlayService.class);
        if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);
    }

    private void showAssistant() {
        Dialog d=new Dialog(this);LinearLayout b=dialogBox();b.addView(text("AIRI Assistant • Owner",23,true));
        TextView ctx=text("Mengenal perangkat: RMX1851, Android 11, kernel "+kernel()+". Kebiasaan aplikasi tetap lokal.",10,false);ctx.setTextColor(0xbbffffff);b.addView(ctx);
        EditText input=edit("Contoh: buka WhatsApp / status HP / storage / apa yang harus saya kerjakan?");b.addView(input,new LinearLayout.LayoutParams(-1,dp(58)));
        TextView out=text("Siap membantu, "+ownerName()+".",12,false);out.setPadding(0,dp(8),0,dp(8));b.addView(out,new LinearLayout.LayoutParams(-1,dp(120)));
        LinearLayout row=new LinearLayout(this);Button ask=button("Kirim");Button voice=button("Voice");row.addView(ask,new LinearLayout.LayoutParams(0,dp(48),1));row.addView(voice,new LinearLayout.LayoutParams(0,dp(48),1));b.addView(row);
        EditText gateway=edit("https://gateway-anda.example/ai");gateway.setText(prefs.getString("gateway",""));b.addView(gateway,new LinearLayout.LayoutParams(-1,dp(50)));
        Spinner provider=new Spinner(this);provider.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"OpenAI / ChatGPT","Google Gemini"}));b.addView(provider);
        ask.setOnClickListener(v->{String q=input.getText().toString().trim();if(q.isEmpty())return;String local=localCommand(q);if(local!=null){out.setText(local);return;}String g=gateway.getText().toString().trim();prefs.edit().putString("gateway",g).apply();if(g.isEmpty()){out.setText("Perintah perangkat bisa saya jalankan lokal. Untuk percakapan AI cloud, tambahkan gateway HTTPS.");return;}out.setText("Memproses…");callGateway(g,provider.getSelectedItemPosition()==0?"openai":"gemini",q,out);});
        voice.setOnClickListener(v->{try{Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"id-ID");i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Bicara dengan AIRI");startActivityForResult(i,REQ_VOICE);}catch(Exception e){Toast.makeText(this,"Voice recognition tidak tersedia",Toast.LENGTH_SHORT).show();}});
        d.setContentView(b);d.show();sizeDialog(d,.94f);
    }

    private String localCommand(String q) {
        String x=q.toLowerCase(new Locale("id","ID"));
        if(x.contains("whatsapp")){launch("com.whatsapp");return "Membuka WhatsApp.";}
        if(x.contains("kamera")||x.contains("camera")){startActivity(new Intent(this,CameraActivity.class));return "Membuka AIRI Camera.";}
        if(x.contains("youtube")){launch("com.google.android.youtube");return "Membuka YouTube.";}
        if(x.contains("chrome")){launch("com.android.chrome");return "Membuka Chrome.";}
        if(x.contains("wifi")||x.contains("wi-fi")){control("Wi‑Fi");return "Membuka panel Wi‑Fi.";}
        if(x.contains("bluetooth")){control("Bluetooth");return "Membuka Bluetooth.";}
        if(x.contains("baterai")){return "Baterai saat ini "+batteryPct()+"%.";}
        if(x.contains("storage")||x.contains("penyimpanan")){double f=freeStorageGb();return String.format(Locale.US,"Penyimpanan kosong sekitar %.1f GB dari %.0f GB. %s",f,totalStorageGb(),f<5?"Saya sarankan bersihkan file besar agar sistem tetap lancar.":"Kondisinya masih cukup lega.");}
        if(x.contains("kernel")){return "Kernel perangkat: "+kernel()+".";}
        if(x.contains("android")){return "Perangkat ini menjalankan Android "+Build.VERSION.RELEASE+" pada "+Build.MODEL+".";}
        if(x.contains("favorit")||x.contains("sering")){if(!hasUsageAccess())return "Aktifkan Usage Access di Owner Hub agar Smart Dock bisa belajar aplikasi favorit secara lokal.";StringBuilder s=new StringBuilder("Aplikasi yang saya prioritaskan di Smart Dock: ");for(int i=0;i<smartApps.size();i++){if(i>0)s.append(", ");s.append(smartApps.get(i).name);}return s.toString();}
        if(x.contains("status hp")||x.equals("status")){return "RMX1851 • Android "+Build.VERSION.RELEASE+" • baterai "+batteryPct()+"% • "+storageLabel()+" • kernel "+kernel();}
        return null;
    }

    private void callGateway(String gateway,String provider,String message,TextView out) {
        if(!gateway.startsWith("https://")){out.setText("Gateway harus HTTPS.");return;}
        new Thread(()->{HttpURLConnection c=null;try{c=(HttpURLConnection)new URL(gateway).openConnection();c.setRequestMethod("POST");c.setConnectTimeout(10000);c.setReadTimeout(30000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");JSONObject body=new JSONObject();body.put("provider",provider);body.put("message",message);JSONObject device=new JSONObject();device.put("model",Build.MODEL);device.put("android",Build.VERSION.RELEASE);device.put("kernel",kernel());device.put("battery",batteryPct());device.put("free_storage_gb",freeStorageGb());device.put("owner_name",ownerName());body.put("device",device);body.put("system","Anda adalah AIRI Assistant, asisten pribadi pemilik perangkat Android. Jawab ringkas, praktis, bahasa Indonesia. Jangan mengklaim tindakan yang tidak dilakukan aplikasi. Jangan meminta rahasia yang tidak perlu.");byte[] data=body.toString().getBytes(StandardCharsets.UTF_8);try(OutputStream os=c.getOutputStream()){os.write(data);}int code=c.getResponseCode();InputStream stream=code>=200&&code<300?c.getInputStream():c.getErrorStream();BufferedReader br=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);String raw=sb.toString(),reply=raw;try{JSONObject j=new JSONObject(raw);reply=j.optString("reply",j.optString("output_text",raw));}catch(Exception ignored){}final String r=reply;runOnUiThread(()->out.setText(r));}catch(Exception e){runOnUiThread(()->out.setText("AI Gateway tidak dapat dihubungi: "+e.getClass().getSimpleName()));}finally{if(c!=null)c.disconnect();}}).start();
    }

    private LinearLayout dialogBox(){LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setPadding(dp(16),dp(18),dp(16),dp(16));b.setBackground(rounded(0xff151a28,28));return b;}
    private EditText edit(String hint){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(Color.WHITE);e.setHintTextColor(0x88ffffff);return e;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b;}
    private void sizeDialog(Dialog d,float width){Window w=d.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.setLayout((int)(getResources().getDisplayMetrics().widthPixels*width),-2);}}
    private void sizeBottomDialog(Dialog d,float h){Window w=d.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*h));w.setGravity(Gravity.BOTTOM);}}

    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==REQ_VOICE&&res==RESULT_OK&&data!=null){ArrayList<String> r=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(r!=null&&!r.isEmpty()){String answer=localCommand(r.get(0));Toast.makeText(this,answer==null?r.get(0):answer,Toast.LENGTH_LONG).show();}}}
    @Override public void onBackPressed(){moveTaskToBack(true);}
}

package id.airi.os;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(247, 245, 239);
    private static final int INK = Color.rgb(28, 32, 35);
    private static final int MUTED = Color.rgb(104, 108, 110);
    private static final int BLUE = Color.rgb(48, 86, 117);
    private static final int CARD = Color.rgb(255, 255, 252);

    private GridLayout appGrid;
    private PackageManager packageManager;
    private final List<AppEntry> allApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        if (Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        packageManager = getPackageManager();
        buildUi();
        loadInstalledApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appGrid != null) {
            loadInstalledApps();
        }
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(18), dp(22), dp(28));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout brandRow = new LinearLayout(this);
        brandRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("AIRI OS", 14, BLUE, true);
        brandRow.addView(brand, new LinearLayout.LayoutParams(0, dp(42), 1f));

        TextView homeButton = pill("Jadikan AIRI Home");
        homeButton.setOnClickListener(v -> requestHomeRole());
        brandRow.addView(homeButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(38)));
        content.addView(brandRow);

        TextView device = text("LIGHTWEIGHT SHELL  •  " + Build.MODEL, 10, MUTED, true);
        device.setLetterSpacing(0.12f);
        content.addView(device, marginParams(-1, -2, 0, 2, 0, 12));

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        TextView clock = text(time, 54, INK, true);
        clock.setTypeface(Typeface.create("sans", Typeface.BOLD));
        content.addView(clock);

        String date = new SimpleDateFormat("EEEE, d MMMM", new Locale("id", "ID")).format(new Date());
        TextView dateView = text(date, 16, MUTED, false);
        content.addView(dateView, marginParams(-1, -2, 0, -6, 0, 18));

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Cari aplikasi…");
        search.setHintTextColor(Color.rgb(137, 139, 140));
        search.setTextColor(INK);
        search.setTextSize(16);
        search.setPadding(dp(18), 0, dp(18), 0);
        search.setBackground(rounded(CARD, 22, Color.rgb(226, 224, 218), 1));
        content.addView(search, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderApps(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        TextView quickTitle = sectionTitle("Quick access");
        content.addView(quickTitle, marginParams(-1, -2, 0, 22, 0, 10));

        GridLayout quickGrid = new GridLayout(this);
        quickGrid.setColumnCount(2);
        addQuick(quickGrid, "Wi‑Fi", "Jaringan", Settings.ACTION_WIFI_SETTINGS);
        addQuick(quickGrid, "Bluetooth", "Perangkat", Settings.ACTION_BLUETOOTH_SETTINGS);
        addQuick(quickGrid, "Wallpaper", "Personalisasi", Intent.ACTION_SET_WALLPAPER);
        addQuick(quickGrid, "Settings", "Android", Settings.ACTION_SETTINGS);
        content.addView(quickGrid);

        TextView appsTitle = sectionTitle("Aplikasi");
        content.addView(appsTitle, marginParams(-1, -2, 0, 24, 0, 8));

        TextView hint = text("Semua aplikasi launcher • tap untuk membuka", 12, MUTED, false);
        content.addView(hint, marginParams(-1, -2, 0, 0, 0, 14));

        appGrid = new GridLayout(this);
        appGrid.setColumnCount(4);
        appGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        appGrid.setUseDefaultMargins(false);
        content.addView(appGrid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView footer = text("AIRI OS v1.0  •  no root  •  launcher shell", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        content.addView(footer, marginParams(-1, dp(52), 0, 24, 0, 0));

        setContentView(root);
    }

    private void addQuick(GridLayout grid, String title, String subtitle, String action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(13), dp(12), dp(12));
        card.setBackground(rounded(CARD, 18, Color.rgb(230, 228, 222), 1));
        TextView t = text(title, 15, INK, true);
        TextView s = text(subtitle, 11, MUTED, false);
        card.addView(t);
        card.addView(s);
        card.setOnClickListener(v -> {
            try { startActivity(new Intent(action)); }
            catch (Exception ignored) { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        });

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = dp(68);
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        grid.addView(card, lp);
    }

    private void loadInstalledApps() {
        allApps.clear();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = packageManager.queryIntentActivities(launcherIntent, 0);
        for (ResolveInfo info : infos) {
            if (info.activityInfo == null) continue;
            String pkg = info.activityInfo.packageName;
            if (getPackageName().equals(pkg)) continue;
            CharSequence labelCs = info.loadLabel(packageManager);
            String label = labelCs == null ? pkg : labelCs.toString();
            allApps.add(new AppEntry(label, pkg));
        }
        Collections.sort(allApps, (a, b) -> a.label.compareToIgnoreCase(b.label));
        renderApps("");
    }

    private void renderApps(String filter) {
        if (appGrid == null) return;
        appGrid.removeAllViews();
        String needle = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
        int shown = 0;
        for (AppEntry app : allApps) {
            if (!needle.isEmpty() && !app.label.toLowerCase(Locale.ROOT).contains(needle)) continue;
            appGrid.addView(appTile(app), appTileParams());
            shown++;
        }
        if (shown == 0) {
            TextView empty = text("Aplikasi tidak ditemukan", 14, MUTED, false);
            empty.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = GridLayout.LayoutParams.MATCH_PARENT;
            lp.height = dp(90);
            lp.columnSpec = GridLayout.spec(0, 4);
            appGrid.addView(empty, lp);
        }
    }

    private View appTile(AppEntry app) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(dp(3), dp(7), dp(3), dp(7));

        ImageView icon = new ImageView(this);
        try { icon.setImageDrawable(packageManager.getApplicationIcon(app.packageName)); }
        catch (Exception ignored) { icon.setImageResource(android.R.drawable.sym_def_app_icon); }
        tile.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView label = text(app.label, 11, INK, false);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        tile.addView(label, marginParams(-1, dp(34), 0, 5, 0, 0));

        tile.setOnClickListener(v -> {
            Intent launch = packageManager.getLaunchIntentForPackage(app.packageName);
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launch);
            }
        });
        return tile;
    }

    private GridLayout.LayoutParams appTileParams() {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = dp(100);
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(1), dp(2), dp(1), dp(2));
        return lp;
    }

    private void requestHomeRole() {
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME), 9001);
                    return;
                }
            }
        }
        try {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Exception e) {
            Intent chooser = new Intent(Intent.ACTION_MAIN);
            chooser.addCategory(Intent.CATEGORY_HOME);
            startActivity(chooser);
        }
    }

    private TextView sectionTitle(String value) {
        return text(value, 19, INK, true);
    }

    private TextView pill(String value) {
        TextView view = text(value, 12, Color.WHITE, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(14), 0, dp(14), 0);
        view.setBackground(rounded(BLUE, 20, BLUE, 0));
        return view;
    }

    private TextView text(String value, float sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    private GradientDrawable rounded(int fill, int radiusDp, int stroke, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) drawable.setStroke(dp(strokeDp), stroke);
        return drawable;
    }

    private LinearLayout.LayoutParams marginParams(int w, int h, int l, int t, int r, int b) {
        int width = w == -1 ? ViewGroup.LayoutParams.MATCH_PARENT : (w == -2 ? ViewGroup.LayoutParams.WRAP_CONTENT : w);
        int height = h == -1 ? ViewGroup.LayoutParams.MATCH_PARENT : (h == -2 ? ViewGroup.LayoutParams.WRAP_CONTENT : h);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        return lp;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class AppEntry {
        final String label;
        final String packageName;
        AppEntry(String label, String packageName) {
            this.label = label;
            this.packageName = packageName;
        }
    }
}

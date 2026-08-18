package id.airi.os;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class LauncherPrefs {
    private static final String PREF = "airi_fusion_launcher";
    private static final String FAVORITES = "favorites";
    private static final String HIDDEN = "hidden";
    private LauncherPrefs() {}

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static boolean suggestions(Context c) { return p(c).getBoolean("suggestions", true); }
    public static void suggestions(Context c, boolean v) { p(c).edit().putBoolean("suggestions", v).apply(); }
    public static boolean categories(Context c) { return p(c).getBoolean("categories", true); }
    public static void categories(Context c, boolean v) { p(c).edit().putBoolean("categories", v).apply(); }
    public static boolean labels(Context c) { return p(c).getBoolean("labels", true); }
    public static void labels(Context c, boolean v) { p(c).edit().putBoolean("labels", v).apply(); }
    public static boolean compact(Context c) { return p(c).getBoolean("compact", false); }
    public static void compact(Context c, boolean v) { p(c).edit().putBoolean("compact", v).apply(); }
    public static int columns(Context c) { return Math.max(4, Math.min(5, p(c).getInt("columns", 4))); }
    public static void columns(Context c, int v) { p(c).edit().putInt("columns", Math.max(4, Math.min(5, v))).apply(); }

    public static Set<String> favorites(Context c) {
        return Collections.unmodifiableSet(new HashSet<>(p(c).getStringSet(FAVORITES, Collections.emptySet())));
    }
    public static Set<String> hidden(Context c) {
        return Collections.unmodifiableSet(new HashSet<>(p(c).getStringSet(HIDDEN, Collections.emptySet())));
    }
    public static boolean isFavorite(Context c, String pkg) { return favorites(c).contains(pkg); }
    public static boolean isHidden(Context c, String pkg) { return hidden(c).contains(pkg); }

    public static void favorite(Context c, String pkg, boolean on) {
        Set<String> s = new HashSet<>(favorites(c));
        if (on) s.add(pkg); else s.remove(pkg);
        p(c).edit().putStringSet(FAVORITES, s).apply();
    }
    public static void hidden(Context c, String pkg, boolean on) {
        Set<String> s = new HashSet<>(hidden(c));
        if (on) s.add(pkg); else s.remove(pkg);
        p(c).edit().putStringSet(HIDDEN, s).apply();
    }
    public static void clearHidden(Context c) { p(c).edit().remove(HIDDEN).apply(); }
    public static void reset(Context c) { p(c).edit().clear().apply(); }
}

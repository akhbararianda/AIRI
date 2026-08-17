package id.airi.os;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public final class AiriTheme {
    public static final String PEARL="pearl", EMERALD="emerald", SUNSET="sunset", LAVENDER="lavender";
    private static final String PREF="airi_signature_theme", KEY="theme";
    private AiriTheme(){}

    public static String current(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString(KEY,PEARL);}
    public static void set(Context c,String id){c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString(KEY,id).apply();}
    public static String label(Context c){String t=current(c);if(EMERALD.equals(t))return "Obsidian Emerald";if(SUNSET.equals(t))return "Sunset Glass";if(LAVENDER.equals(t))return "Aurora Lavender";return "Pearl Titanium";}

    public static int[] background(Context c){String t=current(c);
        if(EMERALD.equals(t))return new int[]{rgb("111713"),rgb("243B31"),rgb("5F806F"),rgb("DCE3D8")};
        if(SUNSET.equals(t))return new int[]{rgb("251A1C"),rgb("6C3431"),rgb("C37B65"),rgb("F2D8BE")};
        if(LAVENDER.equals(t))return new int[]{rgb("211B27"),rgb("54445F"),rgb("9D8AA8"),rgb("EEE6ED")};
        return new int[]{rgb("19191B"),rgb("5B554F"),rgb("B9AA93"),rgb("F2EBDD")};
    }
    public static int accent(Context c){String t=current(c);if(EMERALD.equals(t))return rgb("79A88B");if(SUNSET.equals(t))return rgb("C8765F");if(LAVENDER.equals(t))return rgb("A789B4");return rgb("C6A86A");}
    public static int accent2(Context c){String t=current(c);if(EMERALD.equals(t))return rgb("B9D0BF");if(SUNSET.equals(t))return rgb("E4B397");if(LAVENDER.equals(t))return rgb("D2BDD8");return rgb("D9CCB5");}
    public static int dark(Context c){String t=current(c);if(EMERALD.equals(t))return rgb("111713");if(SUNSET.equals(t))return rgb("211416");if(LAVENDER.equals(t))return rgb("201A25");return rgb("18181A");}
    public static int surface(Context c){String t=current(c);if(EMERALD.equals(t))return rgb("E3E9E1");if(SUNSET.equals(t))return rgb("F4E2D2");if(LAVENDER.equals(t))return rgb("EEE7EF");return rgb("F1E9DB");}
    public static int ink(Context c){return dark(c);}
    public static int muted(Context c){String t=current(c);if(EMERALD.equals(t))return rgb("607067");if(SUNSET.equals(t))return rgb("795F57");if(LAVENDER.equals(t))return rgb("746879");return rgb("70685D");}
    public static int glow1(Context c){String t=current(c);if(EMERALD.equals(t))return rgb("CCE0D1");if(SUNSET.equals(t))return rgb("FFD6B4");if(LAVENDER.equals(t))return rgb("E7D1E8");return rgb("FFF0CE");}
    public static int glow2(Context c){return accent(c);}
    public static int glow3(Context c){return accent2(c);}
    public static int nav(Context c){return surface(c);}
    private static int rgb(String hex){return Color.parseColor("#"+hex);}
}

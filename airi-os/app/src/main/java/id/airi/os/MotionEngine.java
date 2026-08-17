package id.airi.os;

import android.content.Context;
import android.content.SharedPreferences;

public final class MotionEngine {
    public static final String LITE="lite", BALANCED="balanced", ULTRA="ultra";
    public static final String FADE="fade", ZOOM="zoom", SLIDE="slide", FLOW="flow";
    private static final String PREF="airi_motion_engine";
    private MotionEngine(){}

    private static SharedPreferences p(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE);}
    public static String mode(Context c){return p(c).getString("mode",BALANCED);}
    public static String transition(Context c){return p(c).getString("transition",FLOW);}
    public static boolean parallax(Context c){return p(c).getBoolean("parallax",true);}
    public static boolean islandPulse(Context c){return p(c).getBoolean("island_pulse",true);}
    public static void setMode(Context c,String v){p(c).edit().putString("mode",v).apply();}
    public static void setTransition(Context c,String v){p(c).edit().putString("transition",v).apply();}
    public static void setParallax(Context c,boolean v){p(c).edit().putBoolean("parallax",v).apply();}
    public static void setIslandPulse(Context c,boolean v){p(c).edit().putBoolean("island_pulse",v).apply();}

    public static float pressScale(Context c){String m=mode(c);return LITE.equals(m)?.975f:ULTRA.equals(m)?.90f:.94f;}
    public static float releaseScale(Context c){String m=mode(c);return LITE.equals(m)?1.008f:ULTRA.equals(m)?1.045f:1.025f;}
    public static long pressDownMs(Context c){String m=mode(c);return LITE.equals(m)?45:ULTRA.equals(m)?82:65;}
    public static long releaseMs(Context c){String m=mode(c);return LITE.equals(m)?80:ULTRA.equals(m)?135:100;}
    public static long settleMs(Context c){String m=mode(c);return LITE.equals(m)?95:ULTRA.equals(m)?230:160;}
    public static long enterMs(Context c){String m=mode(c);return LITE.equals(m)?180:ULTRA.equals(m)?520:330;}
    public static int enterOffsetDp(Context c){String m=mode(c);return LITE.equals(m)?4:ULTRA.equals(m)?18:10;}
    public static int staggerMs(Context c){String m=mode(c);return LITE.equals(m)?10:ULTRA.equals(m)?38:24;}
}

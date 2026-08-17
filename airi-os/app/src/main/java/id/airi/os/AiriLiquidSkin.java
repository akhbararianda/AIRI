package id.airi.os;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class AiriLiquidSkin {
    private static final int BG = Color.rgb(238,246,252);
    private static final int INK = Color.rgb(20,28,38);
    private static final int MUTED = Color.rgb(100,111,122);
    private static final int BLUE = Color.rgb(53,103,150);
    private AiriLiquidSkin() {}

    public static void apply(Activity a) {
        try {
            Window w = a.getWindow();
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(228,239,247));
            if (Build.VERSION.SDK_INT >= 23) w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            View root = w.getDecorView().findViewById(android.R.id.content);
            if (root instanceof ViewGroup) walk(a, (ViewGroup) root, 0);
        } catch (Exception ignored) {}
    }

    private static void walk(Activity a, ViewGroup group, int depth) {
        for (int i=0; i<group.getChildCount(); i++) {
            View v = group.getChildAt(i);
            if (v instanceof TextView) {
                TextView t=(TextView)v;
                if (!(t instanceof EditText)) {
                    if (t.getCurrentTextColor()==Color.BLACK || t.getCurrentTextColor()==Color.DKGRAY) t.setTextColor(INK);
                    if (t.getTextSize() >= sp(a,26)) t.setTypeface(Typeface.create("sans", Typeface.BOLD));
                } else {
                    t.setTextColor(INK); t.setHintTextColor(MUTED);
                }
            }
            if (v instanceof LinearLayout && depth>0) {
                LinearLayout l=(LinearLayout)v;
                if (l.getBackground()==null && l.getChildCount()>1) {
                    GradientDrawable d=new GradientDrawable();
                    d.setColor(Color.argb(125,255,255,255));
                    d.setCornerRadius(dp(a,26));
                    d.setStroke(dp(a,1),Color.argb(110,255,255,255));
                    l.setBackground(d);
                }
            }
            if (v instanceof ViewGroup) walk(a,(ViewGroup)v,depth+1);
        }
    }

    public static GradientDrawable glass(Activity a, int alpha, int radius) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(Color.argb(alpha,255,255,255));
        d.setCornerRadius(dp(a,radius));
        d.setStroke(dp(a,1),Color.argb(135,255,255,255));
        return d;
    }
    public static GradientDrawable blueGlass(Activity a, int radius) {
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(64,122,172), Color.rgb(115,161,199)});
        d.setCornerRadius(dp(a,radius));
        return d;
    }
    private static int dp(Activity a,int v){return Math.round(v*a.getResources().getDisplayMetrics().density);}
    private static float sp(Activity a,int v){return v*a.getResources().getDisplayMetrics().scaledDensity;}
}

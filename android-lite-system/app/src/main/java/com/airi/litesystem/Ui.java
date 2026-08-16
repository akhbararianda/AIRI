package com.airi.litesystem;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    static final int BG = Color.rgb(247,247,248);
    static final int TEXT = Color.rgb(32,33,36);
    static final int MUTED = Color.rgb(111,114,120);
    static final int ACCENT = Color.rgb(26,115,232);
    static int dp(Context c, int v) { return Math.round(v * c.getResources().getDisplayMetrics().density); }
    static TextView text(Context c, String s, float sp, int color) {
        TextView t = new TextView(c); t.setText(s); t.setTextSize(sp); t.setTextColor(color); t.setGravity(Gravity.CENTER_VERTICAL); return t;
    }
    static GradientDrawable bg(int color, float radius, Context c) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c,(int)radius)); return d;
    }
    static TextView button(Context c, String text) {
        TextView v = text(c, text, 15, Color.WHITE); v.setGravity(Gravity.CENTER); v.setPadding(dp(c,18),dp(c,13),dp(c,18),dp(c,13)); v.setBackground(bg(ACCENT,24,c)); v.setClickable(true); v.setFocusable(true); return v;
    }
    static LinearLayout card(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(c,18),dp(c,16),dp(c,18),dp(c,16)); l.setBackground(bg(Color.WHITE,24,c)); return l;
    }
    static LinearLayout.LayoutParams lpMatch(int h, Context c) { return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,h < 0 ? h : dp(c,h)); }
    static void setMargins(View v,int l,int t,int r,int b,Context c) {
        if (v.getLayoutParams() instanceof LinearLayout.LayoutParams) { LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)v.getLayoutParams(); p.setMargins(dp(c,l),dp(c,t),dp(c,r),dp(c,b)); v.setLayoutParams(p); }
    }
}

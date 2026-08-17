package id.airi.os;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class AiriLiquidSkin {
    private AiriLiquidSkin(){}
    public static void apply(Activity a){
        try{
            Window w=a.getWindow();w.setStatusBarColor(Color.TRANSPARENT);w.setNavigationBarColor(AiriTheme.nav(a));
            if(Build.VERSION.SDK_INT>=23)w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            View root=w.getDecorView().findViewById(android.R.id.content);
            if(root instanceof ViewGroup)walk(a,(ViewGroup)root,0);
        }catch(Exception ignored){}
    }
    private static void walk(Activity a,ViewGroup group,int depth){
        int ink=AiriTheme.ink(a),muted=AiriTheme.muted(a);
        for(int i=0;i<group.getChildCount();i++){
            View v=group.getChildAt(i);
            if(v instanceof TextView){TextView t=(TextView)v;if(t instanceof EditText){t.setTextColor(ink);t.setHintTextColor(muted);}else if(t.getCurrentTextColor()==Color.BLACK||t.getCurrentTextColor()==Color.DKGRAY){t.setTextColor(ink);}if(t.getTextSize()>=sp(a,24))t.setTypeface(Typeface.create("sans",Typeface.BOLD));}
            if(v instanceof LinearLayout && depth>0 && v.getBackground()==null){LinearLayout l=(LinearLayout)v;if(l.getChildCount()>1){l.setBackground(AiriGlassDrawable.make(a,28,AiriGlassDrawable.REGULAR));l.setElevation(dp(a,8));press(v);}}
            if(v instanceof ViewGroup)walk(a,(ViewGroup)v,depth+1);
        }
    }
    public static void press(View v){v.setOnTouchListener((x,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){x.animate().scaleX(.965f).scaleY(.965f).translationZ(dpv(x,2)).setDuration(80).start();}else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){x.animate().scaleX(1f).scaleY(1f).translationZ(0).setDuration(220).start();}return false;});}
    private static int dp(Activity a,int v){return Math.round(v*a.getResources().getDisplayMetrics().density);}private static float sp(Activity a,int v){return v*a.getResources().getDisplayMetrics().scaledDensity;}private static float dpv(View v,int n){return n*v.getResources().getDisplayMetrics().density;}
}

package id.airi.os;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.PorterDuff;

public final class AiriIconPack {
    public static final String CLEAR="clear", LIGHT="light", DARK="dark", TINTED="tinted";
    private static final String PREF="airi_icon_pack";
    private AiriIconPack(){}
    private static SharedPreferences p(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE);}
    public static String style(Context c){return p(c).getString("style",CLEAR);}
    public static void setStyle(Context c,String s){p(c).edit().putString("style",s).apply();}
    public static String label(Context c){String s=style(c);if(LIGHT.equals(s))return "Pearl Light";if(DARK.equals(s))return "Graphite Dark";if(TINTED.equals(s))return "AIRI Tinted";return "Crystal Clear";}

    public static Bitmap render(Context c, Drawable src, int sizePx){
        int s=Math.max(64,sizePx);Bitmap out=Bitmap.createBitmap(s,s,Bitmap.Config.ARGB_8888);Canvas cv=new Canvas(out);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);RectF r=new RectF(1,1,s-1,s-1);float rad=s*.235f;String st=style(c);
        int top,bottom;
        if(DARK.equals(st)){top=Color.rgb(31,34,41);bottom=Color.rgb(8,10,14);}else if(LIGHT.equals(st)){top=Color.WHITE;bottom=Color.rgb(232,236,243);}else if(TINTED.equals(st)){top=Color.rgb(229,236,255);bottom=Color.rgb(220,207,255);}else{top=Color.argb(220,255,255,255);bottom=Color.argb(166,222,232,247);}
        p.setShader(new LinearGradient(0,0,s,s,top,bottom,Shader.TileMode.CLAMP));cv.drawRoundRect(r,rad,rad,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,s*.018f));p.setColor(Color.argb(CLEAR.equals(st)?180:90,255,255,255));cv.drawRoundRect(new RectF(s*.03f,s*.03f,s*.97f,s*.97f),rad*.9f,rad*.9f,p);p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(42,255,255,255));cv.drawOval(new RectF(s*.10f,s*.045f,s*.90f,s*.36f),p);
        if(src!=null){Drawable d=src.getConstantState()!=null?src.getConstantState().newDrawable().mutate():src.mutate();if(TINTED.equals(st))d.setColorFilter(Color.rgb(97,91,238),PorterDuff.Mode.SRC_ATOP);int pad=(int)(s*.145f);d.setBounds(pad,pad,s-pad,s-pad);cv.save();Path clip=new Path();clip.addRoundRect(new RectF(pad,pad,s-pad,s-pad),s*.15f,s*.15f,Path.Direction.CW);cv.clipPath(clip);d.draw(cv);cv.restore();d.clearColorFilter();}
        return out;
    }
    public static BitmapDrawable drawable(Context c,Drawable src,int sizePx){return new BitmapDrawable(c.getResources(),render(c,src,sizePx));}
}

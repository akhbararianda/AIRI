package id.airi.os;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class AiriGlassDrawable extends Drawable {
    public static final int REGULAR=0, CLEAR=1, BLUE=2, DARK=3;
    private final Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG), shine=new Paint(Paint.ANTI_ALIAS_FLAG), rim=new Paint(Paint.ANTI_ALIAS_FLAG), innerRim=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect=new RectF();
    private final float radius; private final int variant; private final Activity activity; private int alpha=255; private float density=1f;

    public AiriGlassDrawable(Activity a,int radiusDp,int variant){activity=a;radius=dp(a,radiusDp);this.variant=variant;density=a.getResources().getDisplayMetrics().density;rim.setStyle(Paint.Style.STROKE);rim.setStrokeWidth(dp(a,1.15f));innerRim.setStyle(Paint.Style.STROKE);innerRim.setStrokeWidth(dp(a,.6f));}

    @Override public void draw(Canvas canvas){rect.set(getBounds());float w=rect.width(),h=rect.height();int accent=AiriTheme.accent(activity),accent2=AiriTheme.accent2(activity),surface=AiriTheme.surface(activity),dark=AiriTheme.dark(activity);int[] colors;
        if(variant==BLUE) colors=new int[]{a(accent,220),a(mix(accent,dark,.34f),200),a(accent2,158)};
        else if(variant==DARK) colors=new int[]{a(mix(dark,Color.WHITE,.07f),247),a(dark,242),a(mix(dark,accent,.24f),210)};
        else if(variant==CLEAR) colors=new int[]{a(Color.WHITE,150),a(surface,92),a(Color.WHITE,118)};
        else colors=new int[]{a(surface,218),a(mix(surface,accent2,.28f),132),a(Color.WHITE,174)};
        fill.setShader(new LinearGradient(rect.left,rect.top,rect.right,rect.bottom,colors,new float[]{0f,.56f,1f},Shader.TileMode.CLAMP));canvas.drawRoundRect(rect,radius,radius,fill);
        canvas.save();canvas.clipRect(rect);shine.setShader(new RadialGradient(rect.left+w*.18f,rect.top+h*.10f,Math.max(w,h)*.82f,new int[]{a(Color.WHITE,variant==DARK?66:185),a(Color.WHITE,variant==DARK?24:58),Color.TRANSPARENT},new float[]{0f,.34f,1f},Shader.TileMode.CLAMP));canvas.drawCircle(rect.left+w*.18f,rect.top+h*.10f,Math.max(w,h)*.82f,shine);
        shine.setShader(new RadialGradient(rect.right-w*.04f,rect.bottom+h*.05f,Math.max(w,h)*.70f,new int[]{a(accent,variant==DARK?34:78),Color.TRANSPARENT},new float[]{0f,1f},Shader.TileMode.CLAMP));canvas.drawCircle(rect.right-w*.04f,rect.bottom+h*.05f,Math.max(w,h)*.70f,shine);canvas.restore();
        rim.setColor(a(Color.WHITE,variant==DARK?74:198));canvas.drawRoundRect(inset(rect,dpv(.7f)),radius,radius,rim);innerRim.setColor(a(variant==DARK?mix(dark,accent,.42f):mix(accent,Color.DKGRAY,.35f),48));canvas.drawRoundRect(inset(rect,dpv(2f)),Math.max(0,radius-dpv(2)),Math.max(0,radius-dpv(2)),innerRim);
        Paint spec=new Paint(Paint.ANTI_ALIAS_FLAG);spec.setStrokeWidth(dpv(1.2f));spec.setStrokeCap(Paint.Cap.ROUND);spec.setShader(new LinearGradient(rect.left+w*.13f,rect.top+dpv(2),rect.right-w*.20f,rect.top+dpv(2),new int[]{Color.TRANSPARENT,a(Color.WHITE,variant==DARK?110:230),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));canvas.drawLine(rect.left+w*.13f,rect.top+dpv(2),rect.right-w*.20f,rect.top+dpv(2),spec);
    }
    private int mix(int a,int b,float t){return Color.rgb(Math.round(Color.red(a)*(1-t)+Color.red(b)*t),Math.round(Color.green(a)*(1-t)+Color.green(b)*t),Math.round(Color.blue(a)*(1-t)+Color.blue(b)*t));}
    private int a(int color,int raw){int x=Math.round(raw*(alpha/255f));return Color.argb(x,Color.red(color),Color.green(color),Color.blue(color));}
    private RectF inset(RectF r,float v){return new RectF(r.left+v,r.top+v,r.right-v,r.bottom-v);}private float dpv(float v){return v*density;}private static float dp(Activity a,float v){return v*a.getResources().getDisplayMetrics().density;}
    public static AiriGlassDrawable make(Activity a,int radiusDp,int variant){return new AiriGlassDrawable(a,radiusDp,variant);}
    @Override public void setAlpha(int a){alpha=a;invalidateSelf();}@Override public void setColorFilter(ColorFilter f){fill.setColorFilter(f);invalidateSelf();}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}

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
    private final Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shine=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rim=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerRim=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect=new RectF();
    private final float radius;
    private final int variant;
    private int alpha=255;

    public AiriGlassDrawable(Activity a,int radiusDp,int variant){
        this.radius=dp(a,radiusDp); this.variant=variant;
        rim.setStyle(Paint.Style.STROKE);rim.setStrokeWidth(dp(a,1.15f));
        innerRim.setStyle(Paint.Style.STROKE);innerRim.setStrokeWidth(dp(a,.6f));
    }

    @Override public void draw(Canvas canvas){
        rect.set(getBounds());
        float w=rect.width(),h=rect.height();
        int[] colors;
        if(variant==BLUE) colors=new int[]{a(Color.rgb(92,164,219),220),a(Color.rgb(45,104,163),198),a(Color.rgb(214,239,255),155)};
        else if(variant==DARK) colors=new int[]{a(Color.rgb(22,25,31),245),a(Color.rgb(8,11,16),238),a(Color.rgb(58,69,81),210)};
        else if(variant==CLEAR) colors=new int[]{a(Color.WHITE,148),a(Color.rgb(224,242,252),92),a(Color.WHITE,118)};
        else colors=new int[]{a(Color.WHITE,205),a(Color.rgb(224,242,252),125),a(Color.rgb(247,251,255),180)};
        fill.setShader(new LinearGradient(rect.left,rect.top,rect.right,rect.bottom,colors,new float[]{0f,.56f,1f},Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect,radius,radius,fill);

        canvas.save();
        canvas.clipRect(rect);
        shine.setShader(new RadialGradient(rect.left+w*.18f,rect.top+h*.10f,Math.max(w,h)*.82f,
                new int[]{a(Color.WHITE,variant==DARK?70:190),a(Color.WHITE,variant==DARK?26:62),Color.TRANSPARENT},
                new float[]{0f,.34f,1f},Shader.TileMode.CLAMP));
        canvas.drawCircle(rect.left+w*.18f,rect.top+h*.10f,Math.max(w,h)*.82f,shine);
        shine.setShader(new RadialGradient(rect.right-w*.04f,rect.bottom+h*.05f,Math.max(w,h)*.70f,
                new int[]{a(Color.rgb(117,196,244),variant==DARK?34:86),Color.TRANSPARENT},new float[]{0f,1f},Shader.TileMode.CLAMP));
        canvas.drawCircle(rect.right-w*.04f,rect.bottom+h*.05f,Math.max(w,h)*.70f,shine);
        canvas.restore();

        rim.setColor(a(Color.WHITE,variant==DARK?76:205));
        canvas.drawRoundRect(inset(rect,dpv(0.7f)),radius,radius,rim);
        innerRim.setColor(a(variant==DARK?Color.rgb(119,145,166):Color.rgb(85,128,158),variant==DARK?52:52));
        canvas.drawRoundRect(inset(rect,dpv(2.0f)),Math.max(0,radius-dpv(2)),Math.max(0,radius-dpv(2)),innerRim);

        Paint spec=new Paint(Paint.ANTI_ALIAS_FLAG);spec.setStrokeWidth(dpv(1.2f));spec.setStrokeCap(Paint.Cap.ROUND);
        spec.setShader(new LinearGradient(rect.left+w*.13f,rect.top+dpv(2),rect.right-w*.20f,rect.top+dpv(2),
                new int[]{Color.TRANSPARENT,a(Color.WHITE,variant==DARK?115:235),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));
        canvas.drawLine(rect.left+w*.13f,rect.top+dpv(2),rect.right-w*.20f,rect.top+dpv(2),spec);
    }

    private float density=1f;
    private int a(int color,int raw){int x=Math.round(raw*(alpha/255f));return Color.argb(x,Color.red(color),Color.green(color),Color.blue(color));}
    private RectF inset(RectF r,float v){return new RectF(r.left+v,r.top+v,r.right-v,r.bottom-v);}
    private float dpv(float v){return v*density;}
    private static float dp(Activity a,float v){return v*a.getResources().getDisplayMetrics().density;}
    public static AiriGlassDrawable make(Activity a,int radiusDp,int variant){AiriGlassDrawable d=new AiriGlassDrawable(a,radiusDp,variant);d.density=a.getResources().getDisplayMetrics().density;return d;}
    @Override public void setAlpha(int a){alpha=a;invalidateSelf();}
    @Override public void setColorFilter(ColorFilter f){fill.setColorFilter(f);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}

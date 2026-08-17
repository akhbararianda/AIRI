package id.airi.os;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

public class AiriBackdropView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    public AiriBackdropView(Context c){super(c);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();int[] bg=AiriTheme.background(getContext());
        p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,0,w,h,bg,new float[]{0f,.33f,.72f,1f},Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);
        glow(c,w*.16f,h*.12f,w*.66f,AiriTheme.glow1(getContext()),125);glow(c,w*.82f,h*.24f,w*.58f,AiriTheme.glow2(getContext()),118);glow(c,w*.26f,h*.67f,w*.72f,AiriTheme.glow3(getContext()),105);glow(c,w*.90f,h*.80f,w*.60f,Color.WHITE,90);
        p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(w*.12f);p.setColor(Color.argb(35,255,255,255));Path a=new Path();a.moveTo(-w*.16f,h*.19f);a.cubicTo(w*.24f,h*.06f,w*.37f,h*.37f,w*.67f,h*.24f);a.cubicTo(w*.86f,h*.16f,w*.91f,h*.03f,w*1.16f,h*.03f);c.drawPath(a,p);
        p.setStrokeWidth(w*.032f);int ac=AiriTheme.accent(getContext());p.setColor(Color.argb(52,Color.red(ac),Color.green(ac),Color.blue(ac)));c.drawPath(a,p);
        Path b=new Path();b.moveTo(-w*.12f,h*.78f);b.cubicTo(w*.21f,h*.62f,w*.44f,h*.91f,w*.72f,h*.72f);b.cubicTo(w*.89f,h*.61f,w*.98f,h*.54f,w*1.13f,h*.58f);p.setStrokeWidth(w*.10f);p.setColor(Color.argb(28,255,255,255));c.drawPath(b,p);
    }
    private void glow(Canvas c,float x,float y,float r,int color,int alpha){p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(x,y,r,new int[]{Color.argb(alpha,Color.red(color),Color.green(color),Color.blue(color)),Color.TRANSPARENT},new float[]{0f,1f},Shader.TileMode.CLAMP));c.drawCircle(x,y,r,p);p.setShader(null);}
}

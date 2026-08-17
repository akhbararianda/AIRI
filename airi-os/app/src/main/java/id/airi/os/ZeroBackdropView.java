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

public class ZeroBackdropView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    public ZeroBackdropView(Context c){super(c);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();
        p.setShader(new LinearGradient(0,0,w,h,new int[]{Color.rgb(2,2,3),Color.rgb(5,5,5),Color.rgb(8,7,12)},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
        glow(c,w*.78f,h*.15f,w*.58f,Color.rgb(60,225,255),82);glow(c,w*.18f,h*.42f,w*.68f,Color.rgb(156,84,255),64);glow(c,w*.78f,h*.80f,w*.70f,Color.rgb(34,119,255),42);
        p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(w*.08f);p.setColor(Color.argb(16,255,255,255));Path a=new Path();a.moveTo(-w*.1f,h*.27f);a.cubicTo(w*.22f,h*.12f,w*.55f,h*.42f,w*1.12f,h*.18f);c.drawPath(a,p);p.setStrokeWidth(w*.015f);p.setColor(Color.argb(38,74,229,255));c.drawPath(a,p);
    }
    private void glow(Canvas c,float x,float y,float r,int color,int alpha){p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(x,y,r,new int[]{Color.argb(alpha,Color.red(color),Color.green(color),Color.blue(color)),Color.TRANSPARENT},new float[]{0f,1f},Shader.TileMode.CLAMP));c.drawCircle(x,y,r,p);p.setShader(null);}
}

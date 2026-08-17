package id.airi.os;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

public class AiriLogoView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    public AiriLogoView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),s=Math.min(w,h),cx=w/2f,cy=h/2f;float r=s*.43f;
        p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(cx,cy,r,new int[]{Color.argb(238,248,251,255),Color.argb(205,208,224,246),Color.argb(130,129,104,242)},new float[]{0f,.72f,1f},Shader.TileMode.CLAMP));c.drawCircle(cx,cy,r,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(s*.025f);p.setColor(Color.argb(220,255,255,255));c.drawCircle(cx,cy,r*.92f,p);p.setStyle(Paint.Style.FILL);
        Path a=new Path();a.moveTo(cx-r*.48f,cy+r*.38f);a.quadTo(cx-r*.14f,cy-r*.56f,cx,cy-r*.63f);a.quadTo(cx+r*.18f,cy-r*.50f,cx+r*.48f,cy+r*.38f);a.quadTo(cx+r*.30f,cy+r*.28f,cx+r*.16f,cy-r*.05f);a.quadTo(cx,cy-r*.31f,cx-r*.16f,cy-r*.05f);a.quadTo(cx-r*.30f,cy+r*.28f,cx-r*.48f,cy+r*.38f);a.close();p.setShader(new LinearGradient(cx-r*.4f,cy-r*.5f,cx+r*.45f,cy+r*.45f,new int[]{Color.WHITE,Color.rgb(190,225,255),Color.rgb(136,106,246),Color.rgb(90,212,255)},null,Shader.TileMode.CLAMP));c.drawPath(a,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(s*.075f);p.setStrokeCap(Paint.Cap.ROUND);p.setShader(new LinearGradient(cx-r*.62f,cy+r*.16f,cx+r*.62f,cy-r*.05f,new int[]{Color.rgb(81,205,255),Color.rgb(112,110,249),Color.rgb(227,122,255)},null,Shader.TileMode.CLAMP));RectF o=new RectF(cx-r*.66f,cy-r*.25f,cx+r*.66f,cy+r*.31f);c.drawArc(o,12,162,false,p);p.setShader(null);p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);p.setShadowLayer(s*.06f,0,0,Color.rgb(122,113,255));c.drawCircle(cx,cy+r*.13f,s*.045f,p);p.clearShadowLayer();
    }
}

package id.airi.os;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

public class HyperFlowBackdropView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    public HyperFlowBackdropView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    @Override protected void onDraw(Canvas c){
        float w=getWidth(),h=getHeight();
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,0,w,h,
                new int[]{Color.rgb(237,246,255),Color.rgb(246,244,255),Color.rgb(255,244,238),Color.rgb(231,246,244)},
                new float[]{0f,.34f,.68f,1f},Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);
        glow(c,w*.14f,h*.10f,w*.68f,Color.rgb(93,167,255),78);
        glow(c,w*.90f,h*.22f,w*.62f,Color.rgb(172,121,255),64);
        glow(c,w*.20f,h*.77f,w*.68f,Color.rgb(255,157,112),54);
        glow(c,w*.88f,h*.82f,w*.62f,Color.rgb(92,215,192),60);
        glow(c,w*.55f,h*.48f,w*.43f,Color.WHITE,115);
        p.setShader(null);
    }
    private void glow(Canvas c,float x,float y,float r,int color,int alpha){
        p.setShader(new RadialGradient(x,y,r,
                new int[]{Color.argb(alpha,Color.red(color),Color.green(color),Color.blue(color)),Color.TRANSPARENT},
                new float[]{0f,1f},Shader.TileMode.CLAMP));
        c.drawCircle(x,y,r,p);p.setShader(null);
    }
}

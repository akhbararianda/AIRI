package id.airi.os;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class AiriWallpaperActivity extends Activity {
    private Bitmap wallpaper;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(28),dp(18),dp(24));root.setBackgroundColor(Color.rgb(229,238,246));root.addView(t("AIRI Wallpaper",30,true));TextView sub=t("Frost Blue • Pearl Cream • Deep Navy",12,false);sub.setTextColor(Color.rgb(75,93,111));root.addView(sub,lp(-1,-2,0,3,0,16));DisplayMetrics dm=getResources().getDisplayMetrics();int w=Math.max(1080,dm.widthPixels);int h=Math.max(2400,dm.heightPixels*2);wallpaper=createWallpaper(w,h);ImageView preview=new ImageView(this);preview.setImageBitmap(wallpaper);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);preview.setBackground(AiriGlassDrawable.make(this,34,AiriGlassDrawable.REGULAR));preview.setClipToOutline(true);preview.setElevation(dp(10));root.addView(preview,lp(-1,0,0,0,0,16,1));Button home=b("Set Home Wallpaper");home.setOnClickListener(v->apply(WallpaperManager.FLAG_SYSTEM));root.addView(home,lp(-1,dp(54),0,0,0,8));Button lock=b("Set Lock Wallpaper");lock.setOnClickListener(v->apply(Build.VERSION.SDK_INT>=24?WallpaperManager.FLAG_LOCK:WallpaperManager.FLAG_SYSTEM));root.addView(lock,lp(-1,dp(54),0,0,0,8));Button both=b("Set Home + Lock");both.setOnClickListener(v->{apply(WallpaperManager.FLAG_SYSTEM);if(Build.VERSION.SDK_INT>=24)apply(WallpaperManager.FLAG_LOCK);});root.addView(both,lp(-1,dp(54),0,0,0,0));setContentView(root);AiriLiquidSkin.apply(this);}
    private Bitmap createWallpaper(int w,int h){Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setShader(new LinearGradient(0,0,w,h,new int[]{Color.rgb(224,235,244),Color.rgb(130,184,219),Color.rgb(27,74,117),Color.rgb(7,30,55)},new float[]{0f,.34f,.72f,1f},Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);blob(c,p,w*.20f,h*.16f,w*.68f,Color.rgb(255,243,221),190);blob(c,p,w*.82f,h*.28f,w*.62f,Color.rgb(102,210,236),165);blob(c,p,w*.35f,h*.58f,w*.70f,Color.rgb(205,232,248),120);blob(c,p,w*.86f,h*.78f,w*.58f,Color.rgb(55,115,190),150);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w*.055f);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(Color.argb(80,255,255,255));Path wave=new Path();wave.moveTo(-w*.1f,h*.34f);wave.cubicTo(w*.20f,h*.24f,w*.34f,h*.55f,w*.62f,h*.42f);wave.cubicTo(w*.78f,h*.34f,w*.82f,h*.18f,w*1.08f,h*.12f);c.drawPath(wave,p);p.setStrokeWidth(w*.018f);p.setColor(Color.argb(65,135,226,255));c.drawPath(wave,p);return b;}
    private void blob(Canvas c,Paint p,float x,float y,float r,int color,int alpha){p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(x,y,r,new int[]{Color.argb(alpha,Color.red(color),Color.green(color),Color.blue(color)),Color.TRANSPARENT},new float[]{0f,1f},Shader.TileMode.CLAMP));c.drawCircle(x,y,r,p);p.setShader(null);}
    private void apply(int flag){try{WallpaperManager.getInstance(this).setBitmap(wallpaper,null,true,flag);Toast.makeText(this,"AIRI wallpaper applied",Toast.LENGTH_SHORT).show();}catch(IOException|SecurityException e){Toast.makeText(this,"Wallpaper gagal diterapkan: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
    private Button b(String s){Button x=new Button(this);x.setText(s);x.setTextSize(14);x.setTextColor(Color.rgb(15,27,39));x.setBackground(AiriGlassDrawable.make(this,26,AiriGlassDrawable.REGULAR));return x;}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(16,29,42));v.setTypeface(android.graphics.Typeface.create("sans",bold?1:0));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){return lp(w,h,l,t,r,b,0);}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b,float weight){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w);int hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh,weight);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

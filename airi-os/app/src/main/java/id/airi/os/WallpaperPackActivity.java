package id.airi.os;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class WallpaperPackActivity extends Activity {
    private static final String[] NAMES={
            "Ice Bloom","Aurora Glass","Peach Mist","Mint Flow","Blue Silk","Lavender Air",
            "Sunrise Pearl","Ocean Haze","Cloud Prism","Coral Frost","Emerald Veil","Sky Porcelain",
            "Night Cyan","Violet Pulse","Midnight Peach","Obsidian Mint","Deep Aurora","Carbon Bloom",
            "AIRI Pearl","AIRI Titanium","AIRI Sunset","AIRI Emerald","AIRI Lavender","HyperFlow Original"
    };
    private static final int[][] COLORS={
            {0xffd9ecff,0xfff6f3ff,0xffffeee4},{0xffd6e7ff,0xffeadcff,0xffd9fff8},{0xffffe1d2,0xfffff1e8,0xfff3dcff},{0xffd9fff0,0xffe6f7ff,0xfff8f4ff},{0xffcfe3ff,0xffe8f2ff,0xffefe4ff},{0xffeadbff,0xfff7edff,0xffdff4ff},
            {0xffffe3bd,0xfffff1dc,0xffdceeff},{0xffd7efff,0xffe7f8ff,0xffe8f1ff},{0xfff3f4ff,0xffe4f1ff,0xffffedf4},{0xffffd8cd,0xffffeee9,0xffe8eeff},{0xffd8f3e4,0xffeffaf3,0xffe7ecff},{0xffe7f3ff,0xfff9fbff,0xffeee7ff},
            {0xff06131e,0xff10334a,0xff156d7b},{0xff0d0a18,0xff332052,0xff7a43b0},{0xff130d11,0xff442432,0xffa3585c},{0xff090d0c,0xff16352d,0xff2f6d5f},{0xff07131d,0xff152b49,0xff5c347d},{0xff08090b,0xff23262d,0xff5d404d},
            {0xfff2eadc,0xfff8f4ec,0xffd9c9ad},{0xff161719,0xff66615b,0xffc8b28e},{0xff2b1718,0xff8a4f43,0xffe7b68d},{0xff111713,0xff456758,0xffb5d4c0},{0xff1e1823,0xff66506f,0xffd1b9dc},{0xffedf6ff,0xfff5f1ff,0xffffeee7}
    };

    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(28),dp(16),dp(28));root.setBackgroundColor(0xffeef4fa);sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        root.addView(t("Wallpaper Center",31,true));TextView sub=t("24 wallpaper bawaan • Home + Lock",12,false);sub.setTextColor(0xff6b7480);root.addView(sub,lp(-1,-2,0,3,0,16));
        GridLayout grid=new GridLayout(this);grid.setColumnCount(2);root.addView(grid,new LinearLayout.LayoutParams(-1,-2));
        for(int i=0;i<NAMES.length;i++){final int idx=i;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(8),dp(8),dp(8),dp(10));card.setBackground(round(0xd8ffffff,26));card.setElevation(dp(5));
            View preview=new View(this);preview.setBackground(new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,COLORS[i]));card.addView(preview,new LinearLayout.LayoutParams(-1,dp(126)));
            TextView name=t(NAMES[i],13,true);name.setPadding(dp(2),dp(8),0,0);card.addView(name);TextView hint=t(i<12?"Light pack":(i<18?"AMOLED pack":"AIRI pack"),9,false);hint.setTextColor(0xff7b8490);card.addView(hint);
            card.setOnClickListener(v->apply(idx,false));card.setOnLongClickListener(v->{apply(idx,true);return true;});GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(180);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(4),dp(4),dp(4),dp(4));grid.addView(card,p);
        }
        TextView note=t("Tap = Home wallpaper • tahan = Home + Lock wallpaper",10,false);note.setTextColor(0xff69727e);note.setGravity(Gravity.CENTER);root.addView(note,lp(-1,-2,0,12,0,0));setContentView(sc);
    }
    private void apply(int idx,boolean lock){try{Bitmap bmp=render(idx,1080,2400);WallpaperManager wm=WallpaperManager.getInstance(this);if(Build.VERSION.SDK_INT>=24){wm.setBitmap(bmp,null,true,WallpaperManager.FLAG_SYSTEM);if(lock)wm.setBitmap(bmp,null,true,WallpaperManager.FLAG_LOCK);}else wm.setBitmap(bmp);Toast.makeText(this,lock?"Home + Lock wallpaper diterapkan":"Home wallpaper diterapkan",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Gagal menerapkan wallpaper",Toast.LENGTH_SHORT).show();}}
    private Bitmap render(int idx,int w,int h){Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);int[] cs=COLORS[idx];p.setShader(new LinearGradient(0,0,w,h,cs,null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);glow(c,p,w*.18f,h*.16f,w*.72f,Color.WHITE,idx<12?145:45);glow(c,p,w*.84f,h*.28f,w*.68f,cs[2],idx<12?120:150);glow(c,p,w*.25f,h*.78f,w*.75f,cs[1],idx<12?110:130);return b;}
    private void glow(Canvas c,Paint p,float x,float y,float r,int color,int alpha){p.setShader(new RadialGradient(x,y,r,new int[]{Color.argb(alpha,Color.red(color),Color.green(color),Color.blue(color)),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,r,p);p.setShader(null);}
    private android.graphics.drawable.GradientDrawable round(int color,float radius){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(color);g.setCornerRadius(dp((int)radius));return g;}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(0xff20252c);v.setTypeface(android.graphics.Typeface.create("sans",bold?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

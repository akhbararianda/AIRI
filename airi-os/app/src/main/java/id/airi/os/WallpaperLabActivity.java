package id.airi.os;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Random;

public class WallpaperLabActivity extends Activity {
    private Bitmap current;
    private ImageView preview;
    private EditText prompt;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();generate();}
    private void build(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(28),dp(18),dp(18));root.setBackgroundColor(Color.rgb(247,245,239));TextView title=t("AIRI Wallpaper Lab",28,true);root.addView(title);TextView sub=t("Buat wallpaper generatif ringan langsung di perangkat. Coba kata: midnight, cream, ocean, emerald, sunrise.",13,false);sub.setTextColor(Color.DKGRAY);root.addView(sub,lp(-1,-2,0,4,0,14));prompt=new EditText(this);prompt.setHint("Tema wallpaper…");prompt.setSingleLine(true);root.addView(prompt,new LinearLayout.LayoutParams(-1,dp(50)));LinearLayout row=new LinearLayout(this);TextView gen=button("Generate");gen.setOnClickListener(v->generate());row.addView(gen,new LinearLayout.LayoutParams(0,dp(48),1));TextView set=button("Set Wallpaper");set.setOnClickListener(v->setWallpaper());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);p.setMargins(dp(8),0,0,0);row.addView(set,p);root.addView(row,lp(-1,-2,0,10,0,10));preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);root.addView(preview,lp(-1,0,0,0,0,0,1));setContentView(root);}
    private void generate(){int w=1080,h=2160;current=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(current);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);String s=prompt==null?"":prompt.getText().toString().toLowerCase();int a=Color.rgb(30,40,58),b=Color.rgb(245,236,216);if(s.contains("midnight")){a=Color.rgb(11,18,32);b=Color.rgb(42,70,99);}else if(s.contains("cream")){a=Color.rgb(247,241,226);b=Color.rgb(190,208,222);}else if(s.contains("ocean")){a=Color.rgb(14,76,108);b=Color.rgb(112,188,186);}else if(s.contains("emerald")){a=Color.rgb(15,80,63);b=Color.rgb(158,218,189);}else if(s.contains("sunrise")){a=Color.rgb(244,164,96);b=Color.rgb(119,92,147);}for(int y=0;y<h;y++){float t=y/(float)h;int r=(int)(Color.red(a)*(1-t)+Color.red(b)*t),g=(int)(Color.green(a)*(1-t)+Color.green(b)*t),bl=(int)(Color.blue(a)*(1-t)+Color.blue(b)*t);p.setColor(Color.rgb(r,g,bl));c.drawRect(0,y,w,y+2,p);}Random rnd=new Random((s+System.nanoTime()).hashCode());for(int i=0;i<26;i++){int alpha=18+rnd.nextInt(35);p.setColor(Color.argb(alpha,255,255,255));float rad=60+rnd.nextInt(240);c.drawCircle(rnd.nextInt(w),rnd.nextInt(h),rad,p);}preview.setImageBitmap(current);}
    private void setWallpaper(){if(current==null)return;try{WallpaperManager.getInstance(this).setBitmap(current);android.widget.Toast.makeText(this,"Wallpaper AIRI diterapkan",android.widget.Toast.LENGTH_SHORT).show();}catch(Exception e){android.widget.Toast.makeText(this,"Gagal menerapkan wallpaper",android.widget.Toast.LENGTH_SHORT).show();}}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(28,32,35));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}
    private TextView button(String s){TextView v=t(s,14,true);v.setTextColor(Color.WHITE);v.setGravity(Gravity.CENTER);android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(Color.rgb(48,86,117));g.setCornerRadius(dp(22));v.setBackground(g);return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){return lp(w,h,l,t,r,b,0);}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b,float weight){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w);int hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh,weight);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

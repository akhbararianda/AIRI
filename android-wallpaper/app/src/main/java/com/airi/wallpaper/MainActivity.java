package com.airi.wallpaper;

import android.app.*;
import android.app.WallpaperManager;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.Toast;
import java.io.OutputStream;
import java.util.*;

public class MainActivity extends Activity {
    WallpaperView view;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(11,11,16));
        getWindow().setNavigationBarColor(Color.rgb(11,11,16));
        view = new WallpaperView(this);
        setContentView(view);
    }
    @Override public void onBackPressed() {
        if (view.preview >= 0) { view.preview = -1; view.invalidate(); }
        else super.onBackPressed();
    }

    public class WallpaperView extends View {
        final Paint p = new Paint(3), text = new Paint(3);
        final String[] names={"Aurora Glass","Midnight Bloom","Ocean Flux","Solar Drift","Velvet Neon","Arctic Wave","Rose Quartz","Emerald Night","Purple Orbit","Desert Glow","Blue Nova","Mono Luxe"};
        final String[] cats={"Trending","Abstract","Dark","Nature","Minimal"};
        final int[][] colors={
            {0xff10164a,0xff8c52ff,0xff40e0d0},{0xff090a0f,0xff461b7e,0xffe85d75},{0xff001b2e,0xff0077b6,0xff90e0ef},
            {0xff3b1f0e,0xffff7b00,0xffffd166},{0xff10002b,0xff7b2cbf,0xffff4d9d},{0xff052f5f,0xff00a6fb,0xffb9f3fc},
            {0xff40233b,0xffd46a92,0xffffd6e8},{0xff031d13,0xff087f5b,0xff63e6be},{0xff160d2e,0xff5f3dc4,0xffd0bfff},
            {0xff4a2c17,0xffe9a23b,0xffffe0a3},{0xff071a35,0xff1864ab,0xff74c0fc},{0xff111111,0xff555555,0xffdedede}
        };
        final SharedPreferences prefs;
        float density, downY, lastY, scroll=0;
        boolean dragging=false;
        int preview=-1, selectedCat=0;
        RectF[] cards = new RectF[names.length];
        RectF[] chips = new RectF[cats.length];

        WallpaperView(Context c){
            super(c); density=getResources().getDisplayMetrics().density;
            text.setTypeface(Typeface.create("sans",Typeface.NORMAL));
            prefs=getSharedPreferences("airi",MODE_PRIVATE);
            setBackgroundColor(0xff0b0b10);
        }
        float dp(float v){return v*density;}
        void round(Canvas c,float l,float t,float r,float b,float rad,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRoundRect(l,t,r,b,rad,rad,p);}
        void label(Canvas c,String s,float x,float y,float size,int color,boolean bold){text.setTextSize(dp(size));text.setColor(color);text.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,x,y,text);}
        @Override protected void onDraw(Canvas c){super.onDraw(c); if(preview>=0) drawPreview(c); else drawHome(c);}

        void drawHome(Canvas c){
            float w=getWidth(), pad=dp(18); c.save(); c.translate(0,-scroll);
            label(c,"AIRI",pad,dp(42),14,0xff9d8cff,true); label(c,"Wallpaper",pad,dp(73),28,Color.WHITE,true);
            label(c,"Curated for your screen",pad,dp(96),13,0xff94949f,false);
            round(c,w-dp(57),dp(38),w-dp(18),dp(77),dp(13),0xff1c1c25); label(c,"✦",w-dp(46),dp(65),18,0xffffd166,true);
            float x=pad,y=dp(120);
            for(int i=0;i<cats.length;i++){
                float cw=textWidth(cats[i],13)+dp(30); chips[i]=new RectF(x,y,x+cw,y+dp(38));
                round(c,x,y,x+cw,y+dp(38),dp(19),i==selectedCat?0xff7657ff:0xff191920); label(c,cats[i],x+dp(15),y+dp(25),13,i==selectedCat?Color.WHITE:0xffb8b8c2,i==selectedCat); x+=cw+dp(8);
            }
            y=dp(182); label(c,selectedCat==0?"Trending now":cats[selectedCat],pad,y,19,Color.WHITE,true); label(c,"Fresh picks",w-dp(88),y,12,0xff8d7cff,true);
            float gap=dp(10), cw=(w-pad*2-gap)/2, ch=dp(245), top=dp(200);
            for(int i=0;i<names.length;i++){
                float left=pad+(i%2)*(cw+gap), t=top+(i/2)*(ch+gap); cards[i]=new RectF(left,t,left+cw,t+ch);
                drawCard(c,i,cards[i]);
            }
            float bottom=top+6*(ch+gap)+dp(24); label(c,"AIRI Wallpaper • v1.0",pad,bottom,11,0xff666672,false);
            c.restore();
        }
        float textWidth(String s,float size){text.setTextSize(dp(size)); return text.measureText(s);}
        void drawCard(Canvas c,int i,RectF r){
            Path path=new Path(); path.addRoundRect(r,dp(18),dp(18),Path.Direction.CW); c.save(); c.clipPath(path);
            Paint g=new Paint(3); g.setShader(new LinearGradient(r.left,r.top,r.right,r.bottom,colors[i],null,Shader.TileMode.CLAMP)); c.drawRect(r,g);
            p.setStyle(Paint.Style.FILL); p.setColor(0x55ffffff); c.drawCircle(r.left+r.width()*.72f,r.top+r.height()*.30f,r.width()*.34f,p);
            p.setColor(0x33ffffff); c.drawCircle(r.left+r.width()*.18f,r.top+r.height()*.57f,r.width()*.27f,p);
            Paint shade=new Paint(); shade.setShader(new LinearGradient(0,r.centerY(),0,r.bottom,new int[]{0x00000000,0x99000000},null,Shader.TileMode.CLAMP)); c.drawRect(r,shade);
            label(c,names[i],r.left+dp(12),r.bottom-dp(30),13,Color.WHITE,true); label(c,(i%3==0?"4K • Premium":"4K • Free"),r.left+dp(12),r.bottom-dp(12),10,0xffdddddf,false);
            if(prefs.getBoolean("fav"+i,false)){round(c,r.right-dp(40),r.top+dp(10),r.right-dp(10),r.top+dp(40),dp(15),0x66000000);label(c,"♥",r.right-dp(33),r.top+dp(32),16,0xffff7aa2,true);}
            c.restore();
        }
        void drawPreview(Canvas c){
            int i=preview; float w=getWidth(),h=getHeight(); Paint g=new Paint(3); g.setShader(new LinearGradient(0,0,w,h,colors[i],null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,g);
            p.setColor(0x55ffffff);c.drawCircle(w*.78f,h*.29f,w*.32f,p);p.setColor(0x22ffffff);c.drawCircle(w*.12f,h*.54f,w*.35f,p);
            Paint shade=new Paint();shade.setShader(new LinearGradient(0,h*.55f,0,h,new int[]{0x00000000,0xee08080c},null,Shader.TileMode.CLAMP));c.drawRect(0,h*.5f,w,h,shade);
            round(c,dp(18),dp(24),dp(58),dp(64),dp(20),0x66000000);label(c,"‹",dp(31),dp(53),28,Color.WHITE,false);
            boolean fav=prefs.getBoolean("fav"+i,false);round(c,w-dp(58),dp(24),w-dp(18),dp(64),dp(20),0x66000000);label(c,fav?"♥":"♡",w-dp(49),dp(52),20,fav?0xffff7aa2:Color.WHITE,true);
            label(c,names[i],dp(22),h-dp(164),28,Color.WHITE,true);label(c,"AIRI Collection • 4K generated",dp(22),h-dp(140),12,0xffc5c5cc,false);
            round(c,dp(22),h-dp(118),w-dp(22),h-dp(66),dp(18),0xff7657ff);label(c,"Set wallpaper",w/2-textWidth("Set wallpaper",15)/2,h-dp(85),15,Color.WHITE,true);
            round(c,dp(22),h-dp(56),(w-dp(54))/2,h-dp(14),dp(16),0xbb24242c);label(c,"Save",dp(22)+(w-dp(98))/4-textWidth("Save",13)/2,h-dp(30),13,Color.WHITE,true);
            round(c,(w+dp(10))/2,h-dp(56),w-dp(22),h-dp(14),dp(16),0xbb24242c);label(c,"Favorite",(w+dp(10))/2+(w-dp(54))/4-textWidth("Favorite",13)/2,h-dp(30),13,Color.WHITE,true);
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){downY=lastY=y;dragging=false;return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE && preview<0){float dy=y-lastY;if(Math.abs(y-downY)>dp(5))dragging=true;scroll-=dy;float max=Math.max(0,dp(200)+6*(dp(255))-getHeight());scroll=Math.max(0,Math.min(scroll,max));lastY=y;invalidate();return true;}
            if(e.getAction()==MotionEvent.ACTION_UP){if(!dragging) click(x,y);return true;} return true;
        }
        void click(float x,float y){
            if(preview>=0){float h=getHeight(),w=getWidth();
                if(x<dp(75)&&y<dp(85)){preview=-1;invalidate();return;}
                if(x>w-dp(80)&&y<dp(85)){toggleFav(preview);return;}
                if(y>h-dp(124)&&y<h-dp(62)){chooseSet(preview);return;}
                if(y>h-dp(60)&&x<w/2){saveWallpaper(preview);return;}
                if(y>h-dp(60)&&x>=w/2){toggleFav(preview);return;}
            } else {
                float yy=y+scroll;
                for(int i=0;i<chips.length;i++)if(chips[i]!=null&&chips[i].contains(x,yy)){selectedCat=i;invalidate();return;}
                for(int i=0;i<cards.length;i++)if(cards[i]!=null&&cards[i].contains(x,yy)){preview=i;invalidate();return;}
            }
        }
        void toggleFav(int i){boolean n=!prefs.getBoolean("fav"+i,false);prefs.edit().putBoolean("fav"+i,n).apply();Toast.makeText(MainActivity.this,n?"Added to favorites":"Removed from favorites",Toast.LENGTH_SHORT).show();invalidate();}
        void chooseSet(final int i){new AlertDialog.Builder(MainActivity.this).setTitle("Set wallpaper").setItems(new String[]{"Home screen","Lock screen","Home + Lock"},(d,which)->applyWallpaper(i,which)).setNegativeButton("Cancel",null).show();}
        Bitmap makeBitmap(int i){int W=1440,H=2560;Bitmap b=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);Paint q=new Paint(3);q.setShader(new LinearGradient(0,0,W,H,colors[i],null,Shader.TileMode.CLAMP));c.drawRect(0,0,W,H,q);q.setShader(null);q.setColor(0x55ffffff);c.drawCircle(W*.78f,H*.29f,W*.34f,q);q.setColor(0x25ffffff);c.drawCircle(W*.14f,H*.58f,W*.38f,q);q.setColor(0x18ffffff);c.drawCircle(W*.55f,H*.72f,W*.22f,q);return b;}
        void applyWallpaper(int i,int which){try{WallpaperManager wm=WallpaperManager.getInstance(MainActivity.this);Bitmap b=makeBitmap(i);if(Build.VERSION.SDK_INT>=24){if(which==0)wm.setBitmap(b,null,true,WallpaperManager.FLAG_SYSTEM);else if(which==1)wm.setBitmap(b,null,true,WallpaperManager.FLAG_LOCK);else {wm.setBitmap(b,null,true,WallpaperManager.FLAG_SYSTEM);wm.setBitmap(b,null,true,WallpaperManager.FLAG_LOCK);}}else wm.setBitmap(b);Toast.makeText(MainActivity.this,"Wallpaper applied ✓",Toast.LENGTH_LONG).show();}catch(Exception ex){Toast.makeText(MainActivity.this,"Could not set wallpaper: "+ex.getMessage(),Toast.LENGTH_LONG).show();}}
        void saveWallpaper(int i){try{Bitmap b=makeBitmap(i);ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"AIRI_"+names[i].replace(" ","_")+".jpg");v.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/AIRI Wallpaper");Uri u=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(u==null)throw new Exception("Media storage unavailable");try(OutputStream os=getContentResolver().openOutputStream(u)){b.compress(Bitmap.CompressFormat.JPEG,95,os);}Toast.makeText(MainActivity.this,"Saved to Pictures/AIRI Wallpaper",Toast.LENGTH_LONG).show();}catch(Exception ex){Toast.makeText(MainActivity.this,"Save failed on this device",Toast.LENGTH_LONG).show();}}
    }
}

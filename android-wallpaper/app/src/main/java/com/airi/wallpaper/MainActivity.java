package com.airi.wallpaper;

import android.app.*;
import android.app.WallpaperManager;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.Toast;
import java.io.OutputStream;
import java.util.Random;

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
        static final int TOTAL = 1000;
        static final int PER_CATEGORY = 100;
        final Paint p = new Paint(3), text = new Paint(3);
        final String[] cats={"All 1000","Trending","AMOLED","Abstract","Gradient","Nature","Space","Minimal","Neon","Pastel","Luxury"};
        final String[] adjectives={"Aurora","Midnight","Velvet","Solar","Crystal","Nova","Silent","Lunar","Cosmic","Royal","Electric","Golden","Arctic","Ocean","Emerald","Rose","Obsidian","Dream","Prism","Eclipse"};
        final String[] nouns={"Flow","Bloom","Wave","Orbit","Pulse","Drift","Glow","Mist","Horizon","Flux","Echo","Veil","Storm","Dusk","Aura","Glass","Field","Ray","Night","Luxe"};
        final SharedPreferences prefs;
        final RectF[] chips = new RectF[cats.length];
        float density, downY, lastY, scroll=0, gridTop=dpStatic(225);
        boolean dragging=false;
        int preview=-1, selectedCat=0;

        WallpaperView(Context c){
            super(c); density=getResources().getDisplayMetrics().density;
            text.setTypeface(Typeface.create("sans",Typeface.NORMAL));
            prefs=getSharedPreferences("airi",MODE_PRIVATE);
            setBackgroundColor(0xff0b0b10);
        }
        static float dpStatic(float v){ return v; }
        float dp(float v){return v*density;}
        void round(Canvas c,float l,float t,float r,float b,float rad,int color){p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRoundRect(l,t,r,b,rad,rad,p);}
        void label(Canvas c,String s,float x,float y,float size,int color,boolean bold){text.setTextSize(dp(size));text.setColor(color);text.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,x,y,text);}
        float textWidth(String s,float size){text.setTextSize(dp(size)); return text.measureText(s);}
        @Override protected void onDraw(Canvas c){super.onDraw(c); if(preview>=0) drawPreview(c); else drawHome(c);}

        int filteredCount(){return selectedCat==0?TOTAL:PER_CATEGORY;}
        int itemForPosition(int pos){return selectedCat==0?pos:(selectedCat-1)*PER_CATEGORY+pos;}
        int categoryOf(int i){return Math.min(9,Math.max(0,i/PER_CATEGORY));}
        String categoryName(int i){return cats[categoryOf(i)+1];}
        String nameFor(int i){return adjectives[(i*7+categoryOf(i))%adjectives.length]+" "+nouns[(i*11+3)%nouns.length]+" "+String.format("%03d",i+1);}
        int hsv(float h,float s,float v){return Color.HSVToColor(new float[]{(h%360+360)%360,Math.max(0,Math.min(1,s)),Math.max(0,Math.min(1,v))});}
        int[] palette(int i){
            int cat=categoryOf(i); float h=(i*37f+cat*29f)%360;
            if(cat==1) return new int[]{0xff000000,0xff050505,hsv(h,.92f,.72f)};
            if(cat==8) return new int[]{hsv(h,.22f,1f),hsv(h+35,.18f,.96f),hsv(h+80,.25f,.92f)};
            if(cat==9) return new int[]{0xff090909,0xff34270e,0xffc79a3b};
            if(cat==5) return new int[]{0xff020515,hsv(h,.72f,.30f),hsv(h+55,.58f,.82f)};
            return new int[]{hsv(h,.70f,.22f),hsv(h+55,.78f,.72f),hsv(h+120,.58f,.93f)};
        }

        void drawHome(Canvas c){
            float w=getWidth(), pad=dp(18); c.save(); c.translate(0,-scroll);
            label(c,"AIRI",pad,dp(42),14,0xff9d8cff,true); label(c,"Wallpaper",pad,dp(73),28,Color.WHITE,true);
            label(c,"1,000 offline designs • lightweight",pad,dp(96),13,0xff94949f,false);
            round(c,w-dp(57),dp(38),w-dp(18),dp(77),dp(13),0xff1c1c25); label(c,"✦",w-dp(46),dp(65),18,0xffffd166,true);

            float x=pad,y=dp(120),lineH=dp(44);
            for(int i=0;i<cats.length;i++){
                float cw=textWidth(cats[i],12)+dp(26);
                if(x+cw>w-pad){x=pad;y+=lineH;}
                chips[i]=new RectF(x,y,x+cw,y+dp(36));
                round(c,x,y,x+cw,y+dp(36),dp(18),i==selectedCat?0xff7657ff:0xff191920);
                label(c,cats[i],x+dp(13),y+dp(24),12,i==selectedCat?Color.WHITE:0xffb8b8c2,i==selectedCat);
                x+=cw+dp(7);
            }
            gridTop=y+dp(62);
            label(c,selectedCat==0?"Explore all":cats[selectedCat],pad,gridTop-dp(18),19,Color.WHITE,true);
            label(c,filteredCount()+" designs",w-dp(95),gridTop-dp(18),11,0xff8d7cff,true);

            float gap=dp(10), cw=(w-pad*2-gap)/2, ch=dp(245);
            int count=filteredCount(), rows=(count+1)/2;
            int start=Math.max(0,(int)((scroll-gridTop)/(ch+gap))-1);
            int end=Math.min(rows-1,(int)((scroll+getHeight()-gridTop)/(ch+gap))+1);
            for(int row=start;row<=end;row++){
                for(int col=0;col<2;col++){
                    int pos=row*2+col; if(pos>=count) break;
                    int i=itemForPosition(pos);
                    float left=pad+col*(cw+gap), t=gridTop+row*(ch+gap);
                    drawCard(c,i,new RectF(left,t,left+cw,t+ch));
                }
            }
            float bottom=gridTop+rows*(ch+gap)+dp(28);
            label(c,"AIRI Wallpaper • 1,000 procedural designs",pad,bottom,11,0xff666672,false);
            c.restore();
        }

        void renderPattern(Canvas c, RectF r, int i){
            int cat=categoryOf(i); int[] cs=palette(i); Random rnd=new Random(918273L+i*7919L);
            Paint g=new Paint(3); g.setShader(new LinearGradient(r.left,r.top,r.right,r.bottom,cs,null,Shader.TileMode.CLAMP)); c.drawRect(r,g);
            float min=Math.min(r.width(),r.height());
            if(cat==1){
                p.setShader(null); p.setColor(cs[2]); p.setAlpha(190); c.drawCircle(r.centerX(),r.centerY(),min*.23f,p); p.setAlpha(255);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,min*.018f));p.setColor(Color.WHITE);p.setAlpha(90);c.drawCircle(r.centerX(),r.centerY(),min*.34f,p);p.setStyle(Paint.Style.FILL);p.setAlpha(255);
            } else if(cat==4){
                float horizon=r.top+r.height()*.62f; p.setShader(null);p.setColor(0x33000000);c.drawRect(r.left,horizon,r.right,r.bottom,p);
                Path m=new Path();m.moveTo(r.left,r.bottom); for(int k=0;k<=7;k++){float xx=r.left+r.width()*k/7f;float yy=horizon-rnd.nextFloat()*r.height()*.28f;m.lineTo(xx,yy);}m.lineTo(r.right,r.bottom);m.close();p.setColor(0x66000000);c.drawPath(m,p);
                p.setColor(0x88ffffff);c.drawCircle(r.left+r.width()*.76f,r.top+r.height()*.24f,min*.09f,p);
            } else if(cat==5){
                p.setShader(null); for(int s=0;s<35;s++){p.setColor(Color.WHITE);p.setAlpha(70+rnd.nextInt(150));float rad=Math.max(1,min*(.003f+rnd.nextFloat()*.006f));c.drawCircle(r.left+rnd.nextFloat()*r.width(),r.top+rnd.nextFloat()*r.height()*.75f,rad,p);}p.setAlpha(255);
                p.setColor(cs[2]);c.drawCircle(r.left+r.width()*.72f,r.top+r.height()*.34f,min*.18f,p);p.setColor(0x44000000);c.drawCircle(r.left+r.width()*.77f,r.top+r.height()*.30f,min*.18f,p);
            } else if(cat==6){
                p.setShader(null);p.setColor(0x88ffffff);c.drawCircle(r.left+r.width()*.72f,r.top+r.height()*.30f,min*.22f,p);p.setColor(0x55ffffff);c.drawCircle(r.left+r.width()*.24f,r.top+r.height()*.66f,min*.10f,p);
            } else if(cat==7){
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(3,min*.025f));p.setColor(cs[2]);p.setAlpha(220);for(int k=0;k<3;k++){float inset=min*(.11f+k*.10f);c.drawRoundRect(r.left+inset,r.top+inset,r.right-inset,r.bottom-inset,min*.12f,min*.12f,p);}p.setStyle(Paint.Style.FILL);p.setAlpha(255);
            } else if(cat==8){
                p.setShader(null);for(int k=0;k<7;k++){p.setColor(k%2==0?0x55ffffff:0x33ffffff);float rad=min*(.10f+rnd.nextFloat()*.18f);c.drawCircle(r.left+rnd.nextFloat()*r.width(),r.top+rnd.nextFloat()*r.height(),rad,p);}
            } else if(cat==9){
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,min*.012f));p.setColor(0xffd6b45d);for(int k=-3;k<5;k++){float off=k*min*.18f;c.drawLine(r.left+off,r.bottom,r.left+r.width()*.7f+off,r.top,p);}p.setStyle(Paint.Style.FILL);p.setColor(0x33d6b45d);c.drawCircle(r.centerX(),r.centerY(),min*.26f,p);
            } else {
                p.setShader(null);for(int k=0;k<5;k++){p.setColor(k%2==0?0x44ffffff:0x22000000);float rad=min*(.12f+rnd.nextFloat()*.22f);c.drawCircle(r.left+rnd.nextFloat()*r.width(),r.top+rnd.nextFloat()*r.height(),rad,p);}
            }
        }

        void drawCard(Canvas c,int i,RectF r){
            Path path=new Path(); path.addRoundRect(r,dp(18),dp(18),Path.Direction.CW); c.save(); c.clipPath(path);
            renderPattern(c,r,i);
            Paint shade=new Paint(); shade.setShader(new LinearGradient(0,r.centerY(),0,r.bottom,new int[]{0x00000000,0xaa000000},null,Shader.TileMode.CLAMP)); c.drawRect(r,shade);
            label(c,nameFor(i),r.left+dp(12),r.bottom-dp(32),12,Color.WHITE,true);
            label(c,categoryName(i)+" • 4K",r.left+dp(12),r.bottom-dp(14),10,0xffdddddf,false);
            if(prefs.getBoolean("fav"+i,false)){round(c,r.right-dp(40),r.top+dp(10),r.right-dp(10),r.top+dp(40),dp(15),0x66000000);label(c,"♥",r.right-dp(33),r.top+dp(32),16,0xffff7aa2,true);}
            c.restore();
        }

        void drawPreview(Canvas c){
            int i=preview; float w=getWidth(),h=getHeight(); RectF full=new RectF(0,0,w,h); renderPattern(c,full,i);
            Paint shade=new Paint();shade.setShader(new LinearGradient(0,h*.50f,0,h,new int[]{0x00000000,0xee08080c},null,Shader.TileMode.CLAMP));c.drawRect(0,h*.46f,w,h,shade);
            round(c,dp(18),dp(24),dp(58),dp(64),dp(20),0x66000000);label(c,"‹",dp(31),dp(53),28,Color.WHITE,false);
            boolean fav=prefs.getBoolean("fav"+i,false);round(c,w-dp(58),dp(24),w-dp(18),dp(64),dp(20),0x66000000);label(c,fav?"♥":"♡",w-dp(49),dp(52),20,fav?0xffff7aa2:Color.WHITE,true);
            label(c,nameFor(i),dp(22),h-dp(164),26,Color.WHITE,true);label(c,categoryName(i)+" • 4K procedural • #"+(i+1),dp(22),h-dp(140),12,0xffc5c5cc,false);
            round(c,dp(22),h-dp(118),w-dp(22),h-dp(66),dp(18),0xff7657ff);label(c,"Set wallpaper",w/2-textWidth("Set wallpaper",15)/2,h-dp(85),15,Color.WHITE,true);
            round(c,dp(22),h-dp(56),(w-dp(54))/2,h-dp(14),dp(16),0xbb24242c);label(c,"Save",dp(22)+(w-dp(98))/4-textWidth("Save",13)/2,h-dp(30),13,Color.WHITE,true);
            round(c,(w+dp(10))/2,h-dp(56),w-dp(22),h-dp(14),dp(16),0xbb24242c);label(c,"Favorite",(w+dp(10))/2+(w-dp(54))/4-textWidth("Favorite",13)/2,h-dp(30),13,Color.WHITE,true);
        }

        float maxScroll(){int rows=(filteredCount()+1)/2;float content=gridTop+rows*(dp(245)+dp(10))+dp(70);return Math.max(0,content-getHeight());}
        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){downY=lastY=y;dragging=false;return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE && preview<0){float dy=y-lastY;if(Math.abs(y-downY)>dp(5))dragging=true;scroll-=dy;scroll=Math.max(0,Math.min(scroll,maxScroll()));lastY=y;invalidate();return true;}
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
                for(int i=0;i<chips.length;i++)if(chips[i]!=null&&chips[i].contains(x,yy)){selectedCat=i;scroll=0;invalidate();return;}
                float pad=dp(18),gap=dp(10),cw=(getWidth()-pad*2-gap)/2,ch=dp(245);
                if(yy>=gridTop){int row=(int)((yy-gridTop)/(ch+gap));float inRow=(yy-gridTop)-row*(ch+gap);if(inRow<=ch){int col=x<pad+cw+gap/2?0:1;float left=pad+col*(cw+gap);if(x>=left&&x<=left+cw){int pos=row*2+col;if(pos<filteredCount()){preview=itemForPosition(pos);invalidate();}}}}
            }
        }
        void toggleFav(int i){boolean n=!prefs.getBoolean("fav"+i,false);prefs.edit().putBoolean("fav"+i,n).apply();Toast.makeText(MainActivity.this,n?"Added to favorites":"Removed from favorites",Toast.LENGTH_SHORT).show();invalidate();}
        void chooseSet(final int i){new AlertDialog.Builder(MainActivity.this).setTitle("Set wallpaper").setItems(new String[]{"Home screen","Lock screen","Home + Lock"},(d,which)->applyWallpaper(i,which)).setNegativeButton("Cancel",null).show();}

        Bitmap makeBitmap(int i){
            int W=1440,H=2560;Bitmap b=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);renderPattern(c,new RectF(0,0,W,H),i);return b;
        }
        void applyWallpaper(int i,int which){Bitmap b=null;try{WallpaperManager wm=WallpaperManager.getInstance(MainActivity.this);b=makeBitmap(i);if(Build.VERSION.SDK_INT>=24){if(which==0)wm.setBitmap(b,null,true,WallpaperManager.FLAG_SYSTEM);else if(which==1)wm.setBitmap(b,null,true,WallpaperManager.FLAG_LOCK);else {wm.setBitmap(b,null,true,WallpaperManager.FLAG_SYSTEM);wm.setBitmap(b,null,true,WallpaperManager.FLAG_LOCK);}}else wm.setBitmap(b);Toast.makeText(MainActivity.this,"Wallpaper applied ✓",Toast.LENGTH_LONG).show();}catch(Exception ex){Toast.makeText(MainActivity.this,"Could not set wallpaper: "+ex.getMessage(),Toast.LENGTH_LONG).show();}finally{if(b!=null&&!b.isRecycled())b.recycle();}}
        void saveWallpaper(int i){Bitmap b=null;try{b=makeBitmap(i);ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"AIRI_"+nameFor(i).replace(" ","_")+".jpg");v.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/AIRI Wallpaper");Uri u=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(u==null)throw new Exception("Media storage unavailable");try(OutputStream os=getContentResolver().openOutputStream(u)){b.compress(Bitmap.CompressFormat.JPEG,95,os);}Toast.makeText(MainActivity.this,"Saved to Pictures/AIRI Wallpaper",Toast.LENGTH_LONG).show();}catch(Exception ex){Toast.makeText(MainActivity.this,"Save failed on this device",Toast.LENGTH_LONG).show();}finally{if(b!=null&&!b.isRecycled())b.recycle();}}
    }
}

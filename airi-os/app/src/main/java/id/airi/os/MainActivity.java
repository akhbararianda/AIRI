package id.airi.os;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private TextView time,island;
    private int enterIndex=0;
    private static final int WHITE=Color.WHITE, INK=Color.rgb(17,29,41), MUTED=Color.rgb(77,96,113);

    @Override protected void onCreate(Bundle b){super.onCreate(b);Window w=getWindow();w.setStatusBarColor(Color.TRANSPARENT);w.setNavigationBarColor(Color.TRANSPARENT);w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);build();tick();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}

    private void build(){FrameLayout stage=new FrameLayout(this);stage.addView(new AiriBackdropView(this),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(18),dp(18),dp(118));stage.addView(root,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);time=text(now("HH:mm"),15,WHITE,true);top.addView(time,new LinearLayout.LayoutParams(0,dp(34),1));TextView signal=text("5G  ◉◉  84%",12,WHITE,true);signal.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);top.addView(signal,new LinearLayout.LayoutParams(0,dp(34),1));root.addView(enter(top));

        island=text("✦  Irzuqni  •  ready",12,WHITE,true);island.setGravity(Gravity.CENTER);island.setBackground(AiriGlassDrawable.make(this,34,AiriGlassDrawable.DARK));island.setElevation(dp(22));press(island);island.setOnClickListener(v->open(new Intent(this,AssistantActivity.class)));LinearLayout.LayoutParams ip=lp(dp(188),dp(45),0,4,0,14);ip.gravity=Gravity.CENTER_HORIZONTAL;root.addView(enter(island),ip);pulse();

        LinearLayout widgets=new LinearLayout(this);widgets.setGravity(Gravity.CENTER);
        LinearLayout weather=widget(AiriGlassDrawable.CLEAR);weather.addView(text("AIRI Weather",11,WHITE,true));weather.addView(text("28°",37,WHITE,false));weather.addView(text("Partly Cloudy",10,Color.argb(220,255,255,255),false));press(weather);weather.setOnClickListener(v->web("weather near me"));widgets.addView(weather,new LinearLayout.LayoutParams(0,dp(132),1));
        LinearLayout smart=widget(AiriGlassDrawable.REGULAR);TextView a=text("AIRI Intelligence",12,INK,true);a.setGravity(Gravity.CENTER);smart.addView(a);TextView orb=text("◉",34,Color.rgb(56,129,190),true);orb.setGravity(Gravity.CENTER);smart.addView(orb);TextView b=text("Ask Irzuqni",10,MUTED,false);b.setGravity(Gravity.CENTER);smart.addView(b);press(smart);smart.setOnClickListener(v->open(new Intent(this,IntelligenceHubActivity.class)));LinearLayout.LayoutParams sw=new LinearLayout.LayoutParams(0,dp(132),1);sw.setMargins(dp(10),0,0,0);widgets.addView(smart,sw);root.addView(enter(widgets));

        GridLayout grid=new GridLayout(this);grid.setColumnCount(4);
        icon(grid,"☎","Phone",()->openCategory(Intent.CATEGORY_APP_CONTACTS),AiriGlassDrawable.CLEAR);
        icon(grid,"✉","Messages",()->openCategory(Intent.CATEGORY_APP_MESSAGING),AiriGlassDrawable.BLUE);
        icon(grid,"✦","Irzuqni",()->open(new Intent(this,AssistantActivity.class)),AiriGlassDrawable.DARK);
        icon(grid,"◉","Camera",()->open(new Intent("android.media.action.IMAGE_CAPTURE")),AiriGlassDrawable.CLEAR);
        icon(grid,"✿","Gallery",()->open(new Intent(this,GalleryLabActivity.class)),AiriGlassDrawable.BLUE);
        icon(grid,"⌕","Circle",()->open(new Intent(this,CircleSearchActivity.class)),AiriGlassDrawable.CLEAR);
        icon(grid,"✧","Eraser",()->open(new Intent(this,SmartEraserActivity.class)),AiriGlassDrawable.CLEAR);
        icon(grid,"Aa","Smart Text",()->open(new Intent(this,SmartTextActivity.class)),AiriGlassDrawable.REGULAR);
        icon(grid,"◫","Control",()->open(new Intent(this,ControlCenterActivity.class)),AiriGlassDrawable.BLUE);
        icon(grid,"▤","Notify",()->open(new Intent(this,NotificationCenterActivity.class)),AiriGlassDrawable.CLEAR);
        icon(grid,"▱","Screen AI",()->open(new Intent(this,ScreenIntelligenceActivity.class)),AiriGlassDrawable.DARK);
        icon(grid,"⚙","Settings",()->open(new Intent(this,AiriSettingsActivity.class)),AiriGlassDrawable.REGULAR);
        icon(grid,"◐","Wallpaper",()->open(new Intent(this,AiriWallpaperActivity.class)),AiriGlassDrawable.CLEAR);
        icon(grid,"⚡","Performance",()->open(new Intent(this,PerformanceCenterActivity.class)),AiriGlassDrawable.REGULAR);
        icon(grid,"✧","Fusion",()->open(new Intent(this,FusionFeaturesActivity.class)),AiriGlassDrawable.BLUE);
        icon(grid,"▦","Library",()->open(new Intent(this,AppLibraryActivity.class)),AiriGlassDrawable.CLEAR);
        root.addView(enter(grid),lp(-1,-2,0,15,0,0));

        TextView search=text("⌕  Search",13,WHITE,true);search.setGravity(Gravity.CENTER);search.setBackground(AiriGlassDrawable.make(this,30,AiriGlassDrawable.CLEAR));search.setElevation(dp(12));press(search);search.setOnClickListener(v->open(new Intent(this,AppLibraryActivity.class)));LinearLayout.LayoutParams sp=lp(dp(126),dp(42),0,5,0,5);sp.gravity=Gravity.CENTER_HORIZONTAL;root.addView(enter(search),sp);
        TextView pages=text("●  ○  ○",10,Color.argb(220,255,255,255),true);pages.setGravity(Gravity.CENTER);root.addView(pages,new LinearLayout.LayoutParams(-1,dp(22)));

        LinearLayout dock=new LinearLayout(this);dock.setGravity(Gravity.CENTER);dock.setPadding(dp(10),dp(9),dp(10),dp(9));dock.setBackground(AiriGlassDrawable.make(this,38,AiriGlassDrawable.REGULAR));dock.setElevation(dp(28));dockIcon(dock,"☎",()->openCategory(Intent.CATEGORY_APP_CONTACTS));dockIcon(dock,"⌕",()->open(new Intent(this,AppLibraryActivity.class)));dockIcon(dock,"✦",()->open(new Intent(this,AssistantActivity.class)));dockIcon(dock,"⚙",()->open(new Intent(this,AiriSettingsActivity.class)));FrameLayout.LayoutParams dl=new FrameLayout.LayoutParams(-1,dp(88),Gravity.BOTTOM);dl.setMargins(dp(22),0,dp(22),dp(18));stage.addView(dock,dl);
        setContentView(stage);
    }

    private LinearLayout widget(int type){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setGravity(Gravity.CENTER);l.setPadding(dp(12),dp(12),dp(12),dp(12));l.setBackground(AiriGlassDrawable.make(this,31,type));l.setElevation(dp(16));return l;}
    private void icon(GridLayout g,String glyph,String label,Runnable run,int type){LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);TextView iv=text(glyph,24,(type==AiriGlassDrawable.BLUE||type==AiriGlassDrawable.DARK)?WHITE:INK,true);iv.setGravity(Gravity.CENTER);iv.setBackground(AiriGlassDrawable.make(this,25,type));iv.setElevation(dp(13));tile.addView(iv,new LinearLayout.LayoutParams(dp(64),dp(64)));TextView name=text(label,10,WHITE,true);name.setGravity(Gravity.CENTER);name.setMaxLines(1);name.setShadowLayer(dp(2),0,dp(1),Color.argb(100,0,0,0));tile.addView(name,new LinearLayout.LayoutParams(-1,dp(27)));press(tile);tile.setOnClickListener(v->run.run());GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(96);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(1),dp(2),dp(1));g.addView(tile,p);}
    private void dockIcon(LinearLayout d,String glyph,Runnable run){TextView v=text(glyph,26,INK,true);v.setGravity(Gravity.CENTER);v.setBackground(AiriGlassDrawable.make(this,27,AiriGlassDrawable.CLEAR));v.setElevation(dp(9));press(v);v.setOnClickListener(x->run.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(62),1);p.setMargins(dp(4),0,dp(4),0);d.addView(v,p);}
    private void openCategory(String cat){try{Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(cat);startActivity(i);}catch(Exception e){open(new Intent(this,AppLibraryActivity.class));}}
    private void web(String q){try{Intent i=new Intent(Intent.ACTION_WEB_SEARCH);i.putExtra("query",q);startActivity(i);}catch(Exception ignored){}}
    private void open(Intent i){try{startActivity(i);overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);}catch(Exception ignored){}}
    private void press(View v){v.setClickable(true);v.setOnTouchListener((x,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN)x.animate().scaleX(.90f).scaleY(.90f).setDuration(70).start();else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)x.animate().scaleX(1.04f).scaleY(1.04f).setDuration(110).withEndAction(()->x.animate().scaleX(1).scaleY(1).setDuration(170).setInterpolator(new DecelerateInterpolator(2f)).start()).start();return false;});}
    private View enter(View v){v.setAlpha(0);v.setTranslationY(dp(12));int delay=Math.min(260,enterIndex++*28);v.animate().alpha(1).translationY(0).setStartDelay(delay).setDuration(380).setInterpolator(new DecelerateInterpolator(1.8f)).start();return v;}
    private void pulse(){handler.postDelayed(new Runnable(){public void run(){if(island==null)return;island.animate().scaleX(1.035f).scaleY(1.035f).setDuration(680).withEndAction(()->island.animate().scaleX(1).scaleY(1).setDuration(780).start()).start();handler.postDelayed(this,2600);}},900);}
    private void tick(){handler.post(new Runnable(){public void run(){if(time!=null)time.setText(now("HH:mm"));handler.postDelayed(this,30000);}});}private String now(String f){return new SimpleDateFormat(f,Locale.getDefault()).format(new Date());}
    private TextView text(String s,float z,int c,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

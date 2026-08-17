package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final List<AppEntry> apps=new ArrayList<>();
    private PackageManager pm;
    private TextView heroTime, assistantPulse;
    private GridLayout appShelf;
    private float downX, downY;
    private long downAt;
    private int enterIndex=0;

    @Override protected void onCreate(Bundle state){
        super.onCreate(state);
        pm=getPackageManager();
        Window w=getWindow();
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(Color.TRANSPARENT);
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        build();
        loadApps();
        tick();
    }
    @Override protected void onResume(){super.onResume();if(appShelf!=null)loadApps();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}

    @Override public boolean dispatchTouchEvent(MotionEvent e){
        if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();downAt=System.currentTimeMillis();}
        else if(e.getAction()==MotionEvent.ACTION_UP){
            float dx=e.getX()-downX,dy=e.getY()-downY;long dt=System.currentTimeMillis()-downAt;
            if(dt<700 && Math.abs(dy)>dp(120) && Math.abs(dy)>Math.abs(dx)*1.25f){
                if(dy<0)open(new Intent(this,AppLibraryActivity.class));
                else open(new Intent(this,ControlCenterActivity.class));
            }
        }
        return super.dispatchTouchEvent(e);
    }

    private void build(){
        final int WHITE=Color.WHITE, INK=AiriTheme.ink(this), MUTED=AiriTheme.muted(this), ACCENT=AiriTheme.accent(this);
        FrameLayout stage=new FrameLayout(this);
        stage.addView(new AiriBackdropView(this),new FrameLayout.LayoutParams(-1,-1));

        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(31),dp(18),dp(112));
        stage.addView(root,new FrameLayout.LayoutParams(-1,-1));

        // Signature header: brand + date, intentionally not a fake iOS status row.
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout brandBox=new LinearLayout(this);brandBox.setOrientation(LinearLayout.VERTICAL);
        TextView brand=text("AIRI",24,WHITE,true);brand.setLetterSpacing(.17f);brandBox.addView(brand);
        TextView edition=text("SIGNATURE HOME  •  v12",9,Color.argb(205,255,255,255),true);edition.setLetterSpacing(.08f);brandBox.addView(edition);
        brandBox.setOnLongClickListener(v->{open(new Intent(this,ThemeCenterActivity.class));return true;});
        header.addView(enter(brandBox),new LinearLayout.LayoutParams(0,dp(54),1));
        TextView date=text(new SimpleDateFormat("EEE  d MMM",new Locale("id","ID")).format(new Date()).toUpperCase(new Locale("id","ID")),11,WHITE,true);date.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);header.addView(date,new LinearLayout.LayoutParams(-2,dp(54)));
        root.addView(header);

        // Irzuqni Command Capsule: the visual anchor of AIRI Home.
        LinearLayout command=new LinearLayout(this);command.setGravity(Gravity.CENTER_VERTICAL);command.setPadding(dp(16),dp(12),dp(14),dp(12));command.setBackground(AiriGlassDrawable.make(this,31,AiriGlassDrawable.DARK));command.setElevation(dp(22));
        TextView orb=text("✦",27,ACCENT,true);orb.setGravity(Gravity.CENTER);orb.setBackground(AiriGlassDrawable.make(this,25,AiriGlassDrawable.CLEAR));command.addView(orb,new LinearLayout.LayoutParams(dp(54),dp(54)));
        LinearLayout cmdText=new LinearLayout(this);cmdText.setOrientation(LinearLayout.VERTICAL);cmdText.setPadding(dp(13),0,0,0);
        cmdText.addView(text("Irzuqni",16,WHITE,true));assistantPulse=text("Tanya, buka aplikasi, cari, atau bantu layar",10,Color.argb(215,255,255,255),false);cmdText.addView(assistantPulse);command.addView(cmdText,new LinearLayout.LayoutParams(0,-2,1));
        TextView mic=text("●",18,ACCENT,true);mic.setGravity(Gravity.CENTER);command.addView(mic,new LinearLayout.LayoutParams(dp(42),dp(42)));
        press(command);command.setOnClickListener(v->open(new Intent(this,AssistantActivity.class)));root.addView(enter(command),lp(-1,dp(82),0,8,0,14));pulse();

        // Asymmetric Smart Deck: deliberately different from the old two equal widgets.
        LinearLayout deck=new LinearLayout(this);deck.setGravity(Gravity.CENTER);
        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setGravity(Gravity.BOTTOM);hero.setPadding(dp(16),dp(15),dp(16),dp(14));hero.setBackground(AiriGlassDrawable.make(this,32,AiriGlassDrawable.CLEAR));hero.setElevation(dp(15));
        TextView label=text("NOW",9,Color.argb(210,255,255,255),true);label.setLetterSpacing(.15f);hero.addView(label);
        heroTime=text(now("HH:mm"),44,WHITE,false);hero.addView(heroTime);
        hero.addView(text(new SimpleDateFormat("EEEE",new Locale("id","ID")).format(new Date()),12,WHITE,true));
        press(hero);hero.setOnClickListener(v->open(new Intent(this,NotificationCenterActivity.class)));
        deck.addView(enter(hero),new LinearLayout.LayoutParams(0,dp(174),1.35f));

        LinearLayout miniCol=new LinearLayout(this);miniCol.setOrientation(LinearLayout.VERTICAL);LinearLayout.LayoutParams mc=new LinearLayout.LayoutParams(0,dp(174),.85f);mc.setMargins(dp(10),0,0,0);deck.addView(miniCol,mc);
        LinearLayout performance=miniCard("⚡","PERFORMANCE","Storage & battery",AiriGlassDrawable.REGULAR,INK,MUTED);performance.setOnClickListener(v->open(new Intent(this,PerformanceCenterActivity.class)));miniCol.addView(enter(performance),new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout weather=miniCard("◌","WEATHER","Tap for forecast",AiriGlassDrawable.BLUE,WHITE,Color.argb(215,255,255,255));weather.setOnClickListener(v->web("weather near me"));LinearLayout.LayoutParams mw=new LinearLayout.LayoutParams(-1,0,1);mw.setMargins(0,dp(9),0,0);miniCol.addView(enter(weather),mw);
        root.addView(deck);

        // AIRI Quick Ribbon: system + intelligence shortcuts, not another app grid.
        LinearLayout ribbon=new LinearLayout(this);ribbon.setGravity(Gravity.CENTER);ribbon.setPadding(dp(7),dp(7),dp(7),dp(7));ribbon.setBackground(AiriGlassDrawable.make(this,27,AiriGlassDrawable.REGULAR));ribbon.setElevation(dp(11));
        ribbonItem(ribbon,"◫","Control",()->open(new Intent(this,ControlCenterActivity.class)));
        ribbonItem(ribbon,"⌕","Circle",()->open(new Intent(this,CircleSearchActivity.class)));
        ribbonItem(ribbon,"✿","Gallery",()->open(new Intent(this,GalleryLabActivity.class)));
        ribbonItem(ribbon,"Aa","Text",()->open(new Intent(this,SmartTextActivity.class)));
        ribbonItem(ribbon,"▱","Screen",()->open(new Intent(this,ScreenIntelligenceActivity.class)));
        root.addView(enter(ribbon),lp(-1,dp(76),0,13,0,13));

        // Real installed-app shelf.
        LinearLayout shelfHeader=new LinearLayout(this);shelfHeader.setGravity(Gravity.CENTER_VERTICAL);
        shelfHeader.addView(text("App Shelf",16,WHITE,true),new LinearLayout.LayoutParams(0,dp(30),1));
        TextView all=text("ALL APPS  →",10,Color.argb(225,255,255,255),true);all.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);all.setOnClickListener(v->open(new Intent(this,AppLibraryActivity.class)));shelfHeader.addView(all,new LinearLayout.LayoutParams(-2,dp(30)));root.addView(enter(shelfHeader));
        appShelf=new GridLayout(this);appShelf.setColumnCount(4);root.addView(appShelf,new LinearLayout.LayoutParams(-1,dp(190)));

        TextView hint=text("↑ apps     •     ↓ control center     •     tahan AIRI untuk tema",9,Color.argb(185,255,255,255),false);hint.setGravity(Gravity.CENTER);root.addView(hint,new LinearLayout.LayoutParams(-1,dp(30)));

        // AIRI Command Bar: compact actions with a dominant central assistant orb.
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER);bar.setPadding(dp(8),dp(8),dp(8),dp(8));bar.setBackground(AiriGlassDrawable.make(this,34,AiriGlassDrawable.REGULAR));bar.setElevation(dp(26));
        commandItem(bar,"☎","Phone",()->openCategory(Intent.CATEGORY_APP_CONTACTS),false);
        commandItem(bar,"✉","Chat",()->openCategory(Intent.CATEGORY_APP_MESSAGING),false);
        commandItem(bar,"✦","Irzuqni",()->open(new Intent(this,AssistantActivity.class)),true);
        commandItem(bar,"▦","Apps",()->open(new Intent(this,AppLibraryActivity.class)),false);
        commandItem(bar,"⚙","AIRI",()->open(new Intent(this,AiriSettingsActivity.class)),false);
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,dp(86),Gravity.BOTTOM);bp.setMargins(dp(18),0,dp(18),dp(17));stage.addView(bar,bp);

        setContentView(stage);
    }

    private LinearLayout miniCard(String glyph,String title,String subtitle,int glass,int ink,int muted){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(13),dp(10),dp(13),dp(10));c.setBackground(AiriGlassDrawable.make(this,28,glass));c.setElevation(dp(12));
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView g=text(glyph,21,ink,true);row.addView(g,new LinearLayout.LayoutParams(0,-2,1));TextView arrow=text("↗",14,ink,true);row.addView(arrow);c.addView(row);c.addView(text(title,10,ink,true));c.addView(text(subtitle,9,muted,false));press(c);return c;
    }

    private void ribbonItem(LinearLayout row,String glyph,String label,Runnable run){
        LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);TextView g=text(glyph,19,AiriTheme.ink(this),true);g.setGravity(Gravity.CENTER);item.addView(g,new LinearLayout.LayoutParams(-1,dp(31)));TextView l=text(label,8.5f,AiriTheme.muted(this),true);l.setGravity(Gravity.CENTER);item.addView(l,new LinearLayout.LayoutParams(-1,dp(20)));press(item);item.setOnClickListener(v->run.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(60),1);p.setMargins(dp(2),0,dp(2),0);row.addView(item,p);
    }

    private void commandItem(LinearLayout row,String glyph,String label,Runnable run,boolean hero){
        LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);TextView g=text(glyph,hero?26:20,hero?Color.WHITE:AiriTheme.ink(this),true);g.setGravity(Gravity.CENTER);if(hero){g.setBackground(AiriGlassDrawable.make(this,27,AiriGlassDrawable.BLUE));g.setElevation(dp(12));}item.addView(g,new LinearLayout.LayoutParams(hero?dp(55):dp(46),hero?dp(55):dp(46)));TextView l=text(label,8.5f,AiriTheme.ink(this),true);l.setGravity(Gravity.CENTER);item.addView(l,new LinearLayout.LayoutParams(-1,dp(18)));press(item);item.setOnClickListener(v->run.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(70),1);row.addView(item,p);
    }

    private void loadApps(){
        apps.clear();Intent i=new Intent(Intent.ACTION_MAIN,null);i.addCategory(Intent.CATEGORY_LAUNCHER);
        for(ResolveInfo r:pm.queryIntentActivities(i,0)){
            if(r.activityInfo==null||getPackageName().equals(r.activityInfo.packageName))continue;
            CharSequence l=r.loadLabel(pm);apps.add(new AppEntry(l==null?r.activityInfo.packageName:l.toString(),r.activityInfo.packageName));
        }
        Collections.sort(apps,(a,b)->a.label.compareToIgnoreCase(b.label));renderShelf();
    }
    private void renderShelf(){
        if(appShelf==null)return;appShelf.removeAllViews();int count=Math.min(8,apps.size());
        for(int n=0;n<count;n++){AppEntry a=apps.get(n);LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);FrameLayout box=new FrameLayout(this);box.setBackground(AiriGlassDrawable.make(this,22,AiriGlassDrawable.CLEAR));box.setElevation(dp(8));ImageView iv=new ImageView(this);try{iv.setImageDrawable(pm.getApplicationIcon(a.pkg));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}iv.setPadding(dp(5),dp(5),dp(5),dp(5));box.addView(iv,new FrameLayout.LayoutParams(-1,-1));tile.addView(box,new LinearLayout.LayoutParams(dp(55),dp(55)));TextView name=text(a.label,9,Color.WHITE,true);name.setGravity(Gravity.CENTER);name.setMaxLines(1);name.setShadowLayer(dp(2),0,dp(1),Color.argb(90,0,0,0));tile.addView(name,new LinearLayout.LayoutParams(-1,dp(24)));press(tile);tile.setOnClickListener(v->{Intent x=pm.getLaunchIntentForPackage(a.pkg);if(x!=null)open(x);});GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(90);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(2),dp(2),dp(2));appShelf.addView(tile,p);}
    }

    private void openCategory(String category){try{Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(category);startActivity(i);}catch(Exception e){open(new Intent(this,AppLibraryActivity.class));}}
    private void web(String q){try{Intent i=new Intent(Intent.ACTION_WEB_SEARCH);i.putExtra("query",q);startActivity(i);}catch(Exception ignored){}}
    private void open(Intent i){try{startActivity(i);overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);}catch(Exception ignored){}}

    private void press(View v){v.setClickable(true);v.setOnTouchListener((x,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN)x.animate().scaleX(.94f).scaleY(.94f).translationY(dp(1)).setDuration(65).start();else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)x.animate().scaleX(1.025f).scaleY(1.025f).translationY(0).setDuration(115).withEndAction(()->x.animate().scaleX(1).scaleY(1).setDuration(170).setInterpolator(new DecelerateInterpolator(2f)).start()).start();return false;});}
    private View enter(View v){v.setAlpha(0);v.setTranslationY(dp(14));int delay=Math.min(300,enterIndex++*34);v.animate().alpha(1).translationY(0).setStartDelay(delay).setDuration(400).setInterpolator(new DecelerateInterpolator(1.8f)).start();return v;}
    private void pulse(){handler.postDelayed(new Runnable(){public void run(){if(assistantPulse==null)return;assistantPulse.animate().alpha(.48f).setDuration(650).withEndAction(()->assistantPulse.animate().alpha(1).setDuration(750).start()).start();handler.postDelayed(this,2600);}},1000);}
    private void tick(){handler.post(new Runnable(){public void run(){if(heroTime!=null)heroTime.setText(now("HH:mm"));handler.postDelayed(this,30000);}});}
    private String now(String fmt){return new SimpleDateFormat(fmt,Locale.getDefault()).format(new Date());}
    private TextView text(String s,float size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private static final class AppEntry{final String label,pkg;AppEntry(String l,String p){label=l;pkg=p;}}
}

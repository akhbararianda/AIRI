package id.airi.os;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int INK=Color.rgb(18,27,37), MUTED=Color.rgb(76,96,113), BLUE=Color.rgb(42,104,168), DARK=Color.rgb(9,13,19);
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final List<AppEntry> apps=new ArrayList<>();
    private PackageManager pm; private GridLayout appGrid; private TextView clock,island; private int enterIndex=0;

    @Override protected void onCreate(Bundle b){super.onCreate(b);Window w=getWindow();w.setStatusBarColor(Color.TRANSPARENT);w.setNavigationBarColor(Color.rgb(216,234,247));if(Build.VERSION.SDK_INT>=23)w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);pm=getPackageManager();build();loadApps();tick();}
    @Override protected void onResume(){super.onResume();if(appGrid!=null)loadApps();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}

    private void build(){
        FrameLayout stage=new FrameLayout(this);stage.addView(new DepthWallpaper(this),new FrameLayout.LayoutParams(-1,-1));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(8),dp(16),dp(120));scroll.addView(root,new ScrollView.LayoutParams(-1,-2));stage.addView(scroll,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout status=new LinearLayout(this);status.setGravity(Gravity.CENTER_VERTICAL);TextView brand=text("AIRI",13,Color.WHITE,true);brand.setLetterSpacing(.16f);status.addView(brand,new LinearLayout.LayoutParams(0,dp(36),1));TextView home=glassText("v7",11,Color.WHITE,AiriGlassDrawable.CLEAR);press(home);home.setOnClickListener(v->requestHome());status.addView(home,new LinearLayout.LayoutParams(-2,dp(34)));root.addView(enter(status));

        island=text("Irzuqni   ▮▮▮",12,Color.WHITE,true);island.setGravity(Gravity.CENTER);island.setBackground(AiriGlassDrawable.make(this,30,AiriGlassDrawable.DARK));island.setElevation(dp(15));press(island);island.setOnClickListener(v->open(new Intent(this,AssistantActivity.class)));LinearLayout.LayoutParams ip=lp(dp(172),dp(46),0,0,0,10);ip.gravity=Gravity.CENTER_HORIZONTAL;root.addView(enter(island),ip);pulse();

        clock=text(now("HH:mm"),70,Color.WHITE,false);clock.setGravity(Gravity.CENTER_HORIZONTAL);clock.setShadowLayer(dp(4),0,dp(2),Color.argb(80,0,0,0));root.addView(enter(clock));TextView date=text(new SimpleDateFormat("EEEE, d MMMM",new Locale("id","ID")).format(new Date()),15,Color.WHITE,true);date.setGravity(Gravity.CENTER);root.addView(enter(date),lp(-1,-2,0,-10,0,16));

        LinearLayout widgets=new LinearLayout(this);widgets.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout weather=widget(AiriGlassDrawable.CLEAR);weather.addView(text("☀  AIRI Weather",11,Color.WHITE,true));weather.addView(text("Tap to view",20,Color.WHITE,true));weather.addView(text("Weather & forecast",10,Color.argb(220,255,255,255),false));press(weather);weather.setOnClickListener(v->webSearch("weather near me"));widgets.addView(weather,new LinearLayout.LayoutParams(0,dp(132),1));
        LinearLayout intelligence=widget(AiriGlassDrawable.REGULAR);TextView orb=text("◉",28,BLUE,true);orb.setGravity(Gravity.CENTER);intelligence.addView(orb);TextView it=text("AIRI Intelligence",12,INK,true);it.setGravity(Gravity.CENTER);intelligence.addView(it);TextView iq=text("Ask Irzuqni",10,MUTED,false);iq.setGravity(Gravity.CENTER);intelligence.addView(iq);press(intelligence);intelligence.setOnClickListener(v->open(new Intent(this,IntelligenceHubActivity.class)));LinearLayout.LayoutParams iw=new LinearLayout.LayoutParams(0,dp(132),1);iw.setMargins(dp(8),0,dp(8),0);widgets.addView(intelligence,iw);
        LinearLayout calendar=widget(AiriGlassDrawable.CLEAR);calendar.addView(text(new SimpleDateFormat("EEE",Locale.US).format(new Date()).toUpperCase(Locale.US),10,Color.WHITE,true));calendar.addView(text(new SimpleDateFormat("d",Locale.US).format(new Date()),31,Color.WHITE,false));calendar.addView(text("Today",11,Color.WHITE,true));press(calendar);calendar.setOnClickListener(v->open(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)));widgets.addView(calendar,new LinearLayout.LayoutParams(0,dp(132),1));root.addView(enter(widgets));

        root.addView(enter(spacer(9)));
        GridLayout features=new GridLayout(this);features.setColumnCount(4);
        addFeature(features,"☎","Phone",()->openCategory(Intent.CATEGORY_APP_CONTACTS),AiriGlassDrawable.CLEAR);
        addFeature(features,"✉","Messages",()->openCategory(Intent.CATEGORY_APP_MESSAGING),AiriGlassDrawable.BLUE);
        addFeature(features,"◉","AIRI",()->open(new Intent(this,IntelligenceHubActivity.class)),AiriGlassDrawable.DARK);
        addFeature(features,"✦","Irzuqni",()->open(new Intent(this,AssistantActivity.class)),AiriGlassDrawable.CLEAR);
        addFeature(features,"◉","Camera",this::openCamera,AiriGlassDrawable.DARK);
        addFeature(features,"✿","Gallery AI",()->open(new Intent(this,GalleryLabActivity.class)),AiriGlassDrawable.BLUE);
        addFeature(features,"✧","Smart Eraser",()->open(new Intent(this,SmartEraserActivity.class)),AiriGlassDrawable.CLEAR);
        addFeature(features,"⌕","Circle Search",()->open(new Intent(this,CircleSearchActivity.class)),AiriGlassDrawable.CLEAR);
        addFeature(features,"◫","Control",()->open(new Intent(this,ControlCenterActivity.class)),AiriGlassDrawable.BLUE);
        addFeature(features,"▤","Notify",()->open(new Intent(this,NotificationCenterActivity.class)),AiriGlassDrawable.CLEAR);
        addFeature(features,"▱","Screen AI",()->open(new Intent(this,ScreenIntelligenceActivity.class)),AiriGlassDrawable.DARK);
        addFeature(features,"⚙","Settings",()->open(new Intent(Settings.ACTION_SETTINGS)),AiriGlassDrawable.REGULAR);
        root.addView(enter(features));

        EditText search=new EditText(this);search.setSingleLine(true);search.setHint("⌕  Search with AIRI");search.setTextColor(Color.WHITE);search.setHintTextColor(Color.argb(230,255,255,255));search.setTextSize(15);search.setGravity(Gravity.CENTER_VERTICAL);search.setPadding(dp(18),0,dp(18),0);search.setBackground(AiriGlassDrawable.make(this,28,AiriGlassDrawable.CLEAR));search.setElevation(dp(10));root.addView(enter(search),lp(-1,dp(54),dp(34),10,dp(34),12));search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){render(s.toString());}public void afterTextChanged(Editable e){}});

        TextView appsTitle=text("App Library",17,Color.WHITE,true);appsTitle.setPadding(dp(4),0,0,0);root.addView(enter(appsTitle),lp(-1,-2,0,4,0,7));appGrid=new GridLayout(this);appGrid.setColumnCount(4);root.addView(appGrid,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout dock=new LinearLayout(this);dock.setGravity(Gravity.CENTER);dock.setPadding(dp(9),dp(9),dp(9),dp(9));dock.setBackground(AiriGlassDrawable.make(this,36,AiriGlassDrawable.REGULAR));dock.setElevation(dp(24));addDock(dock,"☎",()->openCategory(Intent.CATEGORY_APP_CONTACTS));addDock(dock,"⌕",()->webSearch(""));addDock(dock,"✦",()->open(new Intent(this,AssistantActivity.class)));addDock(dock,"◫",()->open(new Intent(this,ControlCenterActivity.class)));FrameLayout.LayoutParams dl=new FrameLayout.LayoutParams(-1,dp(86),Gravity.BOTTOM);dl.setMargins(dp(22),0,dp(22),dp(18));stage.addView(dock,dl);
        setContentView(stage);
    }

    private LinearLayout widget(int type){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setGravity(Gravity.CENTER);l.setPadding(dp(12),dp(12),dp(12),dp(12));l.setBackground(AiriGlassDrawable.make(this,29,type));l.setElevation(dp(12));return l;}
    private View spacer(int h){View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));return v;}
    private TextView glassText(String s,float z,int c,int type){TextView t=text(s,z,c,true);t.setGravity(Gravity.CENTER);t.setPadding(dp(12),0,dp(12),0);t.setBackground(AiriGlassDrawable.make(this,22,type));return t;}
    private void addFeature(GridLayout g,String glyph,String label,Runnable run,int type){LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);TextView icon=text(glyph,23,type==AiriGlassDrawable.DARK||type==AiriGlassDrawable.BLUE?Color.WHITE:INK,true);icon.setGravity(Gravity.CENTER);icon.setBackground(AiriGlassDrawable.make(this,24,type));icon.setElevation(dp(11));tile.addView(icon,new LinearLayout.LayoutParams(dp(62),dp(62)));TextView name=text(label,10,Color.WHITE,true);name.setGravity(Gravity.CENTER);name.setMaxLines(1);name.setShadowLayer(dp(2),0,dp(1),Color.argb(90,0,0,0));tile.addView(name,lp(-1,dp(28),0,4,0,0));press(tile);tile.setOnClickListener(v->run.run());GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(98);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(3),dp(2),dp(3));g.addView(tile,p);}
    private void addDock(LinearLayout d,String glyph,Runnable run){TextView v=text(glyph,25,INK,true);v.setGravity(Gravity.CENTER);v.setBackground(AiriGlassDrawable.make(this,25,AiriGlassDrawable.CLEAR));v.setElevation(dp(8));press(v);v.setOnClickListener(x->run.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(61),1);p.setMargins(dp(4),0,dp(4),0);d.addView(v,p);}

    private void loadApps(){apps.clear();Intent i=new Intent(Intent.ACTION_MAIN,null);i.addCategory(Intent.CATEGORY_LAUNCHER);for(ResolveInfo r:pm.queryIntentActivities(i,0)){if(r.activityInfo==null||getPackageName().equals(r.activityInfo.packageName))continue;CharSequence label=r.loadLabel(pm);apps.add(new AppEntry(label==null?r.activityInfo.packageName:label.toString(),r.activityInfo.packageName));}Collections.sort(apps,(a,b)->a.label.compareToIgnoreCase(b.label));render("");}
    private void render(String filter){if(appGrid==null)return;appGrid.removeAllViews();String q=filter==null?"":filter.trim().toLowerCase(Locale.ROOT);int idx=0;for(AppEntry a:apps){if(!q.isEmpty()&&!a.label.toLowerCase(Locale.ROOT).contains(q))continue;View t=appTile(a);appGrid.addView(t,tileLp());t.setAlpha(0);t.setScaleX(.84f);t.setScaleY(.84f);t.animate().alpha(1).scaleX(1).scaleY(1).setStartDelay(Math.min(200,idx++*12)).setDuration(280).setInterpolator(new DecelerateInterpolator(1.7f)).start();}}
    private View appTile(AppEntry a){LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.setGravity(Gravity.CENTER_HORIZONTAL);FrameLayout badge=new FrameLayout(this);badge.setBackground(AiriGlassDrawable.make(this,24,AiriGlassDrawable.CLEAR));badge.setElevation(dp(9));ImageView iv=new ImageView(this);try{iv.setImageDrawable(pm.getApplicationIcon(a.pkg));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}iv.setPadding(dp(6),dp(6),dp(6),dp(6));badge.addView(iv,new FrameLayout.LayoutParams(-1,-1));t.addView(badge,new LinearLayout.LayoutParams(dp(62),dp(62)));TextView l=text(a.label,10,Color.WHITE,true);l.setGravity(Gravity.CENTER);l.setMaxLines(1);l.setShadowLayer(dp(2),0,dp(1),Color.argb(100,0,0,0));t.addView(l,lp(-1,dp(28),0,4,0,0));press(t);t.setOnClickListener(v->{Intent x=pm.getLaunchIntentForPackage(a.pkg);if(x!=null)open(x);});return t;}
    private GridLayout.LayoutParams tileLp(){GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(99);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(1),dp(2),dp(1),dp(2));return p;}

    private void openCategory(String cat){try{Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(cat);startActivity(i);}catch(Exception e){open(new Intent(Settings.ACTION_SETTINGS));}}
    private void webSearch(String q){try{Intent i=new Intent(Intent.ACTION_WEB_SEARCH);i.putExtra("query",q);startActivity(i);}catch(Exception e){try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.google.com/search?q="+android.net.Uri.encode(q))));}catch(Exception ignored){}}}
    private void openCamera(){try{open(new Intent("android.media.action.IMAGE_CAPTURE"));}catch(Exception ignored){}}
    private void open(Intent i){try{startActivity(i);overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);}catch(Exception ignored){}}
    private void requestHome(){if(Build.VERSION.SDK_INT>=29){RoleManager rm=(RoleManager)getSystemService(Context.ROLE_SERVICE);if(rm!=null&&rm.isRoleAvailable(RoleManager.ROLE_HOME)&&!rm.isRoleHeld(RoleManager.ROLE_HOME)){startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME),91);return;}}open(new Intent(Settings.ACTION_HOME_SETTINGS));}

    private void press(View v){v.setClickable(true);v.setOnTouchListener((x,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){x.animate().scaleX(.91f).scaleY(.91f).translationY(dp(2)).setDuration(70).start();}else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){x.animate().scaleX(1.03f).scaleY(1.03f).translationY(0).setDuration(125).withEndAction(()->x.animate().scaleX(1).scaleY(1).setDuration(180).setInterpolator(new DecelerateInterpolator(2f)).start()).start();}return false;});}
    private View enter(View v){v.setAlpha(0);v.setTranslationY(dp(16));int delay=Math.min(350,enterIndex++*36);v.animate().alpha(1).translationY(0).setStartDelay(delay).setDuration(420).setInterpolator(new DecelerateInterpolator(1.7f)).start();return v;}
    private void pulse(){handler.postDelayed(new Runnable(){public void run(){if(island==null)return;island.animate().scaleX(1.045f).scaleY(1.045f).setDuration(760).withEndAction(()->island.animate().scaleX(1).scaleY(1).setDuration(850).start()).start();handler.postDelayed(this,2600);}},900);}
    private void tick(){handler.post(new Runnable(){public void run(){if(clock!=null)clock.setText(now("HH:mm"));handler.postDelayed(this,30000);}});}private String now(String f){return new SimpleDateFormat(f,Locale.getDefault()).format(new Date());}
    private TextView text(String s,float z,int c,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setTypeface(Typeface.create("sans",b?Typeface.BOLD:Typeface.NORMAL));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}private static class AppEntry{String label,pkg;AppEntry(String l,String p){label=l;pkg=p;}}

    private static class DepthWallpaper extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final float d;DepthWallpaper(Context c){super(c);d=getResources().getDisplayMetrics().density;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}protected void onDraw(Canvas c){super.onDraw(c);int w=getWidth(),h=getHeight();c.drawColor(Color.rgb(16,79,139));blob(c,w*.12f,h*.10f,270*d,Color.rgb(233,225,207),185);blob(c,w*.84f,h*.20f,260*d,Color.rgb(54,172,230),210);blob(c,w*.24f,h*.48f,300*d,Color.rgb(0,175,205),150);blob(c,w*.82f,h*.58f,300*d,Color.rgb(43,103,209),175);blob(c,w*.18f,h*.84f,260*d,Color.rgb(236,225,202),140);blob(c,w*.82f,h*.92f,270*d,Color.rgb(0,103,217),190);}private void blob(Canvas c,float x,float y,float r,int col,int alpha){p.setShader(new RadialGradient(x,y,r,new int[]{Color.argb(alpha,Color.red(col),Color.green(col),Color.blue(col)),Color.TRANSPARENT},new float[]{0,.74f},Shader.TileMode.CLAMP));c.drawCircle(x,y,r,p);p.setShader(null);}}
}

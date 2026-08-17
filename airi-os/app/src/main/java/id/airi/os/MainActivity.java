package id.airi.os;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
    private static final int BG=Color.rgb(248,246,240), INK=Color.rgb(24,29,33), MUTED=Color.rgb(102,108,111), BLUE=Color.rgb(47,84,115), BLUE_SOFT=Color.rgb(232,240,246), CARD=Color.rgb(255,255,252), DARK=Color.rgb(29,34,39);
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final List<AppEntry> apps=new ArrayList<>();
    private GridLayout appGrid; private PackageManager pm; private TextView clock; private TextView island; private int entranceIndex=0;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b); Window w=getWindow(); w.setStatusBarColor(BG);w.setNavigationBarColor(BG);
        if(Build.VERSION.SDK_INT>=23)w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        pm=getPackageManager();build();loadApps();tickClock();
    }
    @Override protected void onResume(){super.onResume();if(appGrid!=null)loadApps();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}

    private void build(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(14),dp(20),dp(30));root.setBackgroundColor(BG);scroll.addView(root,new ScrollView.LayoutParams(-1,-2));

        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView brand=text("AIRI OS",14,BLUE,true);brand.setLetterSpacing(.16f);top.addView(brand,new LinearLayout.LayoutParams(0,dp(40),1));TextView home=pill("AIRI Home");press(home);home.setOnClickListener(v->requestHome());top.addView(home,new LinearLayout.LayoutParams(-2,dp(38)));root.addView(enter(top));
        TextView device=text("FULL PERFORMANCE EDITION  •  "+Build.MODEL,10,MUTED,true);device.setLetterSpacing(.11f);root.addView(enter(device),lp(-1,-2,0,0,0,8));

        clock=text(now("HH:mm"),54,INK,true);root.addView(enter(clock));TextView date=text(new SimpleDateFormat("EEEE, d MMMM",new Locale("id","ID")).format(new Date()),15,MUTED,false);root.addView(enter(date),lp(-1,-2,0,-7,0,12));

        island=text("✦  Irzuqni ready   •   AIRI Engine optimized",12,Color.WHITE,true);island.setGravity(Gravity.CENTER);island.setBackground(round(DARK,25,DARK,0));press(island);island.setOnClickListener(v->open(new Intent(this,IntelligenceHubActivity.class)));root.addView(enter(island),lp(-1,dp(46),0,0,0,14));pulseIsland();

        LinearLayout hero=new LinearLayout(this);hero.setGravity(Gravity.CENTER_VERTICAL);hero.setPadding(dp(18),dp(16),dp(14),dp(16));hero.setBackground(round(BLUE_SOFT,26,Color.rgb(207,220,230),1));
        LinearLayout ht=new LinearLayout(this);ht.setOrientation(LinearLayout.VERTICAL);ht.addView(text("Irzuqni Intelligence",20,INK,true));ht.addView(text("Voice • Vision • Smart Text • Gallery AI",12,MUTED,false));hero.addView(ht,new LinearLayout.LayoutParams(0,-2,1));TextView talk=pill("✦ Bicara");press(talk);talk.setOnClickListener(v->open(new Intent(this,AssistantActivity.class)));hero.addView(talk,new LinearLayout.LayoutParams(-2,dp(40)));press(hero);hero.setOnClickListener(v->open(new Intent(this,IntelligenceHubActivity.class)));root.addView(enter(hero),lp(-1,-2,0,0,0,14));

        EditText search=new EditText(this);search.setSingleLine(true);search.setHint("Cari aplikasi…");search.setTextColor(INK);search.setHintTextColor(Color.rgb(140,142,142));search.setTextSize(15);search.setPadding(dp(17),0,dp(17),0);search.setBackground(round(CARD,24,Color.rgb(225,224,219),1));root.addView(enter(search),new LinearLayout.LayoutParams(-1,dp(52)));search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){render(s.toString());}public void afterTextChanged(Editable e){}});

        root.addView(enter(section("AIRI Intelligence")),lp(-1,-2,0,20,0,8));GridLayout hub=new GridLayout(this);hub.setColumnCount(2);
        addTool(hub,"AI Hub","Semua fitur AIRI",()->open(new Intent(this,IntelligenceHubActivity.class)));
        addTool(hub,"Smart Text","Ringkas & translate",()->open(new Intent(this,SmartTextActivity.class)));
        addTool(hub,"Gallery AI","Photo intelligence",()->open(new Intent(this,GalleryLabActivity.class)));
        addTool(hub,"Irzuqni","Voice assistant",()->open(new Intent(this,AssistantActivity.class)));
        addTool(hub,"Smart Eraser","Object cleanup",()->open(new Intent(this,SmartEraserActivity.class)));
        addTool(hub,"Circle Search","Visual search",()->open(new Intent(this,CircleSearchActivity.class)));root.addView(enter(hub));

        root.addView(enter(section("Performance Deck")),lp(-1,-2,0,18,0,8));GridLayout quick=new GridLayout(this);quick.setColumnCount(3);
        addQuick(quick,"Camera",this::openCamera);addQuick(quick,"Boost",()->open(new Intent(this,PerformanceCenterActivity.class)));addQuick(quick,"Wallpaper",()->open(new Intent(this,WallpaperLabActivity.class)));
        addQuick(quick,"Wi-Fi",()->safeStart(new Intent(Settings.ACTION_WIFI_SETTINGS)));addQuick(quick,"Bluetooth",()->safeStart(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)));addQuick(quick,"Settings",()->safeStart(new Intent(Settings.ACTION_SETTINGS)));root.addView(enter(quick));

        root.addView(enter(section("Aplikasi")),lp(-1,-2,0,20,0,4));root.addView(enter(text("AIRI Icon System • smooth motion • lightweight core",11,MUTED,false)),lp(-1,-2,0,0,0,10));
        appGrid=new GridLayout(this);appGrid.setColumnCount(4);appGrid.setUseDefaultMargins(false);root.addView(appGrid,new LinearLayout.LayoutParams(-1,-2));
        TextView foot=text("AIRI OS v4.0 • Full Performance • RMX1851",10,MUTED,false);foot.setGravity(Gravity.CENTER);root.addView(enter(foot),lp(-1,dp(48),0,18,0,0));setContentView(scroll);
    }

    private View enter(View v){v.setAlpha(0f);v.setTranslationY(dp(16));int delay=Math.min(420,entranceIndex++*45);v.animate().alpha(1f).translationY(0).setStartDelay(delay).setDuration(360).setInterpolator(new DecelerateInterpolator(1.35f)).start();return v;}
    private void press(View v){v.setClickable(true);v.setOnTouchListener((x,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){x.animate().scaleX(.965f).scaleY(.965f).setDuration(90).start();}else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){x.animate().scaleX(1f).scaleY(1f).setDuration(160).setInterpolator(new DecelerateInterpolator(1.5f)).start();}return false;});}
    private void pulseIsland(){handler.postDelayed(new Runnable(){public void run(){if(island==null)return;island.animate().scaleX(1.018f).scaleY(1.018f).alpha(.94f).setDuration(900).withEndAction(()->island.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(900).start()).start();handler.postDelayed(this,2300);}},1200);}
    private void tickClock(){handler.post(new Runnable(){public void run(){if(clock!=null)clock.setText(now("HH:mm"));handler.postDelayed(this,30000);}});}
    private String now(String f){return new SimpleDateFormat(f,Locale.getDefault()).format(new Date());}
    private void open(Intent i){View decor=getWindow().getDecorView();decor.animate().alpha(.94f).setDuration(100).withEndAction(()->{try{startActivity(i);overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);}catch(Exception ignored){}decor.setAlpha(1f);}).start();}

    private void addTool(GridLayout g,String title,String sub,Runnable action){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(15),dp(13),dp(12),dp(12));c.setBackground(round(CARD,21,Color.rgb(228,227,222),1));c.addView(text(title,15,INK,true));c.addView(text(sub,11,MUTED,false));press(c);c.setOnClickListener(v->action.run());GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(74);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(3),dp(3),dp(3),dp(3));g.addView(c,p);}
    private void addQuick(GridLayout g,String s,Runnable a){TextView v=text(s,12,INK,true);v.setGravity(Gravity.CENTER);v.setBackground(round(CARD,19,Color.rgb(230,228,223),1));press(v);v.setOnClickListener(x->a.run());GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(56);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(3),dp(3),dp(3),dp(3));g.addView(v,p);}

    private void openCamera(){Intent i=new Intent("android.media.action.IMAGE_CAPTURE");try{open(i);}catch(Exception e){for(AppEntry a:apps)if(a.label.toLowerCase(Locale.ROOT).contains("camera")||a.label.toLowerCase(Locale.ROOT).contains("kamera")){Intent l=pm.getLaunchIntentForPackage(a.pkg);if(l!=null){open(l);return;}}}}
    private void safeStart(Intent i){try{open(i);}catch(Exception e){open(new Intent(Settings.ACTION_SETTINGS));}}
    private void loadApps(){apps.clear();Intent i=new Intent(Intent.ACTION_MAIN,null);i.addCategory(Intent.CATEGORY_LAUNCHER);for(ResolveInfo r:pm.queryIntentActivities(i,0)){if(r.activityInfo==null||getPackageName().equals(r.activityInfo.packageName))continue;CharSequence cs=r.loadLabel(pm);apps.add(new AppEntry(cs==null?r.activityInfo.packageName:cs.toString(),r.activityInfo.packageName));}Collections.sort(apps,(a,b)->a.label.compareToIgnoreCase(b.label));render("");}
    private void render(String filter){if(appGrid==null)return;appGrid.removeAllViews();String n=filter==null?"":filter.trim().toLowerCase(Locale.ROOT);int idx=0;for(AppEntry a:apps){if(!n.isEmpty()&&!a.label.toLowerCase(Locale.ROOT).contains(n))continue;View tile=tile(a);appGrid.addView(tile,tileLp());tile.setAlpha(0f);tile.setScaleX(.92f);tile.setScaleY(.92f);tile.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(Math.min(260,idx++*16)).setDuration(220).setInterpolator(new DecelerateInterpolator(1.4f)).start();}}
    private View tile(AppEntry a){LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.setGravity(Gravity.CENTER_HORIZONTAL);FrameLayout badge=new FrameLayout(this);badge.setBackground(round(Color.rgb(238,242,245),19,Color.rgb(219,225,229),1));ImageView iv=new ImageView(this);try{iv.setImageDrawable(pm.getApplicationIcon(a.pkg));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}iv.setPadding(dp(8),dp(8),dp(8),dp(8));badge.addView(iv,new FrameLayout.LayoutParams(-1,-1));t.addView(badge,new LinearLayout.LayoutParams(dp(56),dp(56)));TextView l=text(a.label,10.5f,INK,false);l.setGravity(Gravity.CENTER);l.setMaxLines(2);t.addView(l,lp(-1,dp(32),0,4,0,0));press(t);t.setOnClickListener(v->{Intent x=pm.getLaunchIntentForPackage(a.pkg);if(x!=null)open(x);});return t;}
    private GridLayout.LayoutParams tileLp(){GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(100);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(1),dp(2),dp(1),dp(2));return p;}
    private void requestHome(){if(Build.VERSION.SDK_INT>=29){RoleManager rm=(RoleManager)getSystemService(Context.ROLE_SERVICE);if(rm!=null&&rm.isRoleAvailable(RoleManager.ROLE_HOME)&&!rm.isRoleHeld(RoleManager.ROLE_HOME)){startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME),91);return;}}safeStart(new Intent(Settings.ACTION_HOME_SETTINGS));}
    private TextView section(String s){return text(s,18,INK,true);}private TextView pill(String s){TextView v=text(s,12,Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setPadding(dp(13),0,dp(13),0);v.setBackground(round(BLUE,20,BLUE,0));return v;}private TextView text(String s,float z,int c,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setTypeface(Typeface.create("sans",b?Typeface.BOLD:Typeface.NORMAL));return v;}private GradientDrawable round(int fill,int radius,int stroke,int sw){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));if(sw>0)d.setStroke(dp(sw),stroke);return d;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}private static class AppEntry{String label,pkg;AppEntry(String l,String p){label=l;pkg=p;}}
}

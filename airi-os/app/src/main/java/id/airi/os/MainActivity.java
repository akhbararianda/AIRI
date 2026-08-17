package id.airi.os;

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
    private static final int INK=Color.rgb(19,28,39), MUTED=Color.rgb(87,103,117), BLUE=Color.rgb(54,104,151), DARK=Color.rgb(20,24,29);
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final List<AppEntry> apps=new ArrayList<>();
    private PackageManager pm; private GridLayout appGrid; private TextView clock,island; private int enterIndex=0;

    @Override protected void onCreate(Bundle b){super.onCreate(b);Window w=getWindow();w.setStatusBarColor(Color.TRANSPARENT);w.setNavigationBarColor(Color.rgb(225,238,248));if(Build.VERSION.SDK_INT>=23)w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);pm=getPackageManager();build();loadApps();tick();}
    @Override protected void onResume(){super.onResume();if(appGrid!=null)loadApps();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}

    private void build(){
        FrameLayout stage=new FrameLayout(this); stage.setBackground(wallpaper());
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(10),dp(18),dp(116));
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));stage.addView(scroll,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout status=new LinearLayout(this);status.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=text("AIRI",13,INK,true);brand.setLetterSpacing(.18f);status.addView(brand,new LinearLayout.LayoutParams(0,dp(34),1));
        TextView mode=glassPill("Liquid 26.6");press(mode);mode.setOnClickListener(v->requestHome());status.addView(mode,new LinearLayout.LayoutParams(-2,dp(34)));root.addView(enter(status));

        island=text("✦  Irzuqni",12,Color.WHITE,true);island.setGravity(Gravity.CENTER);island.setBackground(round(DARK,26,DARK,0));press(island);island.setOnClickListener(v->open(new Intent(this,AssistantActivity.class)));LinearLayout.LayoutParams ip=lp(dp(158),dp(43),0,2,0,12);ip.gravity=Gravity.CENTER_HORIZONTAL;root.addView(enter(island),ip);pulse();

        clock=text(now("HH:mm"),64,INK,true);clock.setGravity(Gravity.CENTER);root.addView(enter(clock));TextView date=text(new SimpleDateFormat("EEEE, d MMMM",new Locale("id","ID")).format(new Date()),15,MUTED,true);date.setGravity(Gravity.CENTER);root.addView(enter(date),lp(-1,-2,0,-8,0,16));

        LinearLayout widgetRow=new LinearLayout(this);widgetRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout intelligence=glassCard();intelligence.setOrientation(LinearLayout.VERTICAL);intelligence.setPadding(dp(17),dp(15),dp(15),dp(14));intelligence.addView(text("AIRI Intelligence",12,MUTED,true));intelligence.addView(text("Irzuqni",24,INK,true));intelligence.addView(text("Voice • Vision • Smart Text",11,MUTED,false));press(intelligence);intelligence.setOnClickListener(v->open(new Intent(this,IntelligenceHubActivity.class)));widgetRow.addView(intelligence,new LinearLayout.LayoutParams(0,dp(112),1));
        LinearLayout perf=glassCard();perf.setOrientation(LinearLayout.VERTICAL);perf.setGravity(Gravity.CENTER);perf.addView(text("⚡",26,BLUE,true));TextView pt=text("Performance",11,INK,true);pt.setGravity(Gravity.CENTER);perf.addView(pt);press(perf);perf.setOnClickListener(v->open(new Intent(this,PerformanceCenterActivity.class)));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(dp(104),dp(112));pp.setMargins(dp(10),0,0,0);widgetRow.addView(perf,pp);root.addView(enter(widgetRow));

        EditText search=new EditText(this);search.setSingleLine(true);search.setHint("🔎  Cari");search.setTextColor(INK);search.setHintTextColor(Color.rgb(103,118,131));search.setTextSize(15);search.setPadding(dp(18),0,dp(18),0);search.setBackground(glass(172,25));root.addView(enter(search),lp(-1,dp(50),0,13,0,10));search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){render(s.toString());}public void afterTextChanged(Editable e){}});

        root.addView(enter(section("AIRI Space")),lp(-1,-2,2,8,0,6));GridLayout airi=new GridLayout(this);airi.setColumnCount(4);
        addFeature(airi,"✦","Irzuqni",AssistantActivity.class);addFeature(airi,"Aa","Smart Text",SmartTextActivity.class);addFeature(airi,"◉","Gallery AI",GalleryLabActivity.class);addFeature(airi,"⌕","Circle",CircleSearchActivity.class);
        addFeature(airi,"✧","Eraser",SmartEraserActivity.class);addFeature(airi,"◐","Wallpaper",WallpaperLabActivity.class);addFeature(airi,"⚡","Boost",PerformanceCenterActivity.class);addFeature(airi,"AI","AI Hub",IntelligenceHubActivity.class);root.addView(enter(airi));

        root.addView(enter(section("App Library")),lp(-1,-2,2,18,0,6));appGrid=new GridLayout(this);appGrid.setColumnCount(4);root.addView(appGrid,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout dock=new LinearLayout(this);dock.setGravity(Gravity.CENTER);dock.setPadding(dp(8),dp(8),dp(8),dp(8));dock.setBackground(glass(205,31));
        addDock(dock,"📷",this::openCamera);addDock(dock,"✦",()->open(new Intent(this,AssistantActivity.class)));addDock(dock,"⚙",()->safeStart(new Intent(Settings.ACTION_SETTINGS)));addDock(dock,"◉",()->open(new Intent(this,GalleryLabActivity.class)));
        FrameLayout.LayoutParams dpDock=new FrameLayout.LayoutParams(-1,dp(82),Gravity.BOTTOM);dpDock.setMargins(dp(24),0,dp(24),dp(18));stage.addView(dock,dpDock);

        setContentView(stage);
    }

    private GradientDrawable wallpaper(){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(243,236,223),Color.rgb(222,239,250),Color.rgb(188,218,239),Color.rgb(248,243,234)});d.setGradientType(GradientDrawable.LINEAR_GRADIENT);return d;}
    private LinearLayout glassCard(){LinearLayout l=new LinearLayout(this);l.setBackground(glass(156,28));return l;}
    private GradientDrawable glass(int alpha,int radius){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.argb(alpha,255,255,255),Color.argb(Math.max(80,alpha-55),229,244,253)});d.setCornerRadius(dp(radius));d.setStroke(dp(1),Color.argb(145,255,255,255));return d;}

    private void addFeature(GridLayout g,String glyph,String label,Class<?> cls){LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.setGravity(Gravity.CENTER);TextView icon=text(glyph,22,INK,true);icon.setGravity(Gravity.CENTER);icon.setBackground(glass(180,21));t.addView(icon,new LinearLayout.LayoutParams(dp(58),dp(58)));TextView l=text(label,10.5f,INK,true);l.setGravity(Gravity.CENTER);l.setMaxLines(1);t.addView(l,lp(-1,dp(28),0,4,0,0));press(t);t.setOnClickListener(v->open(new Intent(this,cls)));GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(94);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(3),dp(2),dp(3));g.addView(t,p);}
    private void addDock(LinearLayout d,String glyph,Runnable run){TextView v=text(glyph,24,INK,true);v.setGravity(Gravity.CENTER);v.setBackground(glass(145,22));press(v);v.setOnClickListener(x->run.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(58),1);p.setMargins(dp(4),0,dp(4),0);d.addView(v,p);}
    private TextView glassPill(String s){TextView v=text(s,11,INK,true);v.setGravity(Gravity.CENTER);v.setPadding(dp(12),0,dp(12),0);v.setBackground(glass(170,20));return v;}

    private void loadApps(){apps.clear();Intent i=new Intent(Intent.ACTION_MAIN,null);i.addCategory(Intent.CATEGORY_LAUNCHER);for(ResolveInfo r:pm.queryIntentActivities(i,0)){if(r.activityInfo==null||getPackageName().equals(r.activityInfo.packageName))continue;CharSequence cs=r.loadLabel(pm);apps.add(new AppEntry(cs==null?r.activityInfo.packageName:cs.toString(),r.activityInfo.packageName));}Collections.sort(apps,(a,b)->a.label.compareToIgnoreCase(b.label));render("");}
    private void render(String filter){if(appGrid==null)return;appGrid.removeAllViews();String n=filter==null?"":filter.trim().toLowerCase(Locale.ROOT);int idx=0;for(AppEntry a:apps){if(!n.isEmpty()&&!a.label.toLowerCase(Locale.ROOT).contains(n))continue;View t=appTile(a);appGrid.addView(t,tileLp());t.setAlpha(0f);t.setScaleX(.86f);t.setScaleY(.86f);t.setTranslationY(dp(10));t.animate().alpha(1).scaleX(1).scaleY(1).translationY(0).setStartDelay(Math.min(220,idx++*12)).setDuration(260).setInterpolator(new DecelerateInterpolator(1.6f)).start();}}
    private View appTile(AppEntry a){LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.setGravity(Gravity.CENTER_HORIZONTAL);FrameLayout badge=new FrameLayout(this);badge.setBackground(glass(154,21));ImageView iv=new ImageView(this);try{iv.setImageDrawable(pm.getApplicationIcon(a.pkg));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}iv.setPadding(dp(7),dp(7),dp(7),dp(7));badge.addView(iv,new FrameLayout.LayoutParams(-1,-1));t.addView(badge,new LinearLayout.LayoutParams(dp(60),dp(60)));TextView l=text(a.label,10,INK,true);l.setGravity(Gravity.CENTER);l.setMaxLines(1);t.addView(l,lp(-1,dp(28),0,4,0,0));press(t);t.setOnClickListener(v->{Intent x=pm.getLaunchIntentForPackage(a.pkg);if(x!=null)open(x);});return t;}
    private GridLayout.LayoutParams tileLp(){GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(96);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(1),dp(2),dp(1),dp(2));return p;}

    private void press(View v){v.setClickable(true);v.setOnTouchListener((x,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){x.animate().scaleX(.93f).scaleY(.93f).setDuration(90).start();}else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){x.animate().scaleX(1f).scaleY(1f).setDuration(230).setInterpolator(new DecelerateInterpolator(1.8f)).start();}return false;});}
    private View enter(View v){v.setAlpha(0);v.setTranslationY(dp(14));int delay=Math.min(360,enterIndex++*42);v.animate().alpha(1).translationY(0).setStartDelay(delay).setDuration(430).setInterpolator(new DecelerateInterpolator(1.55f)).start();return v;}
    private void pulse(){handler.postDelayed(new Runnable(){public void run(){if(island==null)return;island.animate().scaleX(1.035f).scaleY(1.035f).setDuration(800).withEndAction(()->island.animate().scaleX(1).scaleY(1).setDuration(800).start()).start();handler.postDelayed(this,2500);}},900);}
    private void tick(){handler.post(new Runnable(){public void run(){if(clock!=null)clock.setText(now("HH:mm"));handler.postDelayed(this,30000);}});}
    private String now(String f){return new SimpleDateFormat(f,Locale.getDefault()).format(new Date());}
    private void open(Intent i){try{startActivity(i);overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);}catch(Exception ignored){}}
    private void openCamera(){Intent i=new Intent("android.media.action.IMAGE_CAPTURE");try{open(i);}catch(Exception e){}}
    private void safeStart(Intent i){try{open(i);}catch(Exception e){open(new Intent(Settings.ACTION_SETTINGS));}}
    private void requestHome(){if(Build.VERSION.SDK_INT>=29){RoleManager rm=(RoleManager)getSystemService(Context.ROLE_SERVICE);if(rm!=null&&rm.isRoleAvailable(RoleManager.ROLE_HOME)&&!rm.isRoleHeld(RoleManager.ROLE_HOME)){startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME),91);return;}}safeStart(new Intent(Settings.ACTION_HOME_SETTINGS));}

    private TextView section(String s){return text(s,17,INK,true);}private TextView text(String s,float z,int c,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setTypeface(Typeface.create("sans",b?Typeface.BOLD:Typeface.NORMAL));return v;}private GradientDrawable round(int fill,int radius,int stroke,int sw){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));if(sw>0)d.setStroke(dp(sw),stroke);return d;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}private static class AppEntry{String label,pkg;AppEntry(String l,String p){label=l;pkg=p;}}
}

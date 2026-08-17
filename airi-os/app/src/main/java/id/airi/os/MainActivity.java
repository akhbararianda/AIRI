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
    private GridLayout appGrid;
    private TextView clock,islandText;
    private float downX,downY;
    private long downAt;
    private int enterIndex=0;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);pm=getPackageManager();
        Window w=getWindow();w.setStatusBarColor(Color.TRANSPARENT);w.setNavigationBarColor(Color.rgb(244,247,251));
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        build();loadApps();tick();
    }
    @Override protected void onResume(){super.onResume();if(appGrid!=null)loadApps();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
    @Override public void onBackPressed(){}

    @Override public boolean dispatchTouchEvent(MotionEvent e){
        if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();downAt=System.currentTimeMillis();}
        else if(e.getAction()==MotionEvent.ACTION_UP){
            float dx=e.getX()-downX,dy=e.getY()-downY;long dt=System.currentTimeMillis()-downAt;
            if(dt<750&&Math.abs(dy)>dp(115)&&Math.abs(dy)>Math.abs(dx)*1.15f){
                if(dy<0)open(new Intent(this,AppLibraryActivity.class));
                else if(downX<getResources().getDisplayMetrics().widthPixels/2f)open(new Intent(this,NotificationCenterActivity.class));
                else open(new Intent(this,ControlCenterActivity.class));
            }
        }
        return super.dispatchTouchEvent(e);
    }

    private void build(){
        final int INK=Color.rgb(24,28,35), MUTED=Color.rgb(103,111,124), ACCENT=Color.rgb(82,123,255), WHITE=Color.WHITE;
        FrameLayout stage=new FrameLayout(this);stage.addView(new HyperFlowBackdropView(this),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(17),dp(28),dp(17),dp(112));stage.addView(root,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout status=new LinearLayout(this);status.setGravity(Gravity.CENTER_VERTICAL);
        clock=text(now("HH:mm"),14,INK,true);status.addView(clock,new LinearLayout.LayoutParams(0,dp(34),1));
        TextView brand=text("AIRI HyperFlow",11,INK,true);brand.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);status.addView(brand,new LinearLayout.LayoutParams(0,dp(34),1));root.addView(status);

        islandText=text("✦  Irzuqni  •  Ready",11,WHITE,true);islandText.setGravity(Gravity.CENTER);islandText.setBackground(round(Color.rgb(35,38,45),999));islandText.setElevation(dp(13));islandText.setOnClickListener(v->open(new Intent(this,AssistantActivity.class)));press(islandText);LinearLayout.LayoutParams ip=lp(dp(188),dp(42),0,2,0,12);ip.gravity=Gravity.CENTER_HORIZONTAL;root.addView(enter(islandText),ip);pulseIsland();

        LinearLayout widgets=new LinearLayout(this);widgets.setGravity(Gravity.CENTER);
        LinearLayout timeCard=whiteCard();timeCard.setGravity(Gravity.CENTER_VERTICAL);TextView cap=text(new SimpleDateFormat("EEEE",new Locale("id","ID")).format(new Date()),11,MUTED,true);timeCard.addView(cap);TextView big=text(now("HH:mm"),41,INK,false);timeCard.addView(big);TextView date=text(new SimpleDateFormat("d MMMM",new Locale("id","ID")).format(new Date()),12,MUTED,false);timeCard.addView(date);widgets.addView(enter(timeCard),new LinearLayout.LayoutParams(0,dp(132),1.18f));
        LinearLayout side=new LinearLayout(this);side.setOrientation(LinearLayout.VERTICAL);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,dp(132),.82f);sp.setMargins(dp(9),0,0,0);widgets.addView(side,sp);
        LinearLayout weather=miniCard("28°","Weather","Tap for forecast",Color.rgb(114,164,255));weather.setOnClickListener(v->web("weather near me"));side.addView(enter(weather),new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout ai=miniCard("✦","Irzuqni","Ask AIRI",Color.rgb(165,111,255));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,0,1);ap.setMargins(0,dp(8),0,0);side.addView(enter(ai),ap);ai.setOnClickListener(v->open(new Intent(this,AssistantActivity.class)));root.addView(widgets);

        LinearLayout smart=new LinearLayout(this);smart.setGravity(Gravity.CENTER);smart.setPadding(dp(7),dp(7),dp(7),dp(7));smart.setBackground(round(Color.argb(186,255,255,255),dp(25)));smart.setElevation(dp(9));
        smartAction(smart,"◫","Control",()->open(new Intent(this,ControlCenterActivity.class)));
        smartAction(smart,"▤","Notify",()->open(new Intent(this,NotificationCenterActivity.class)));
        smartAction(smart,"⌕","Circle",()->open(new Intent(this,CircleSearchActivity.class)));
        smartAction(smart,"✿","Gallery",()->open(new Intent(this,GalleryLabActivity.class)));
        smartAction(smart,"⚡","Boost",()->open(new Intent(this,PerformanceCenterActivity.class)));
        root.addView(enter(smart),lp(-1,dp(68),0,12,0,11));

        LinearLayout appsHeader=new LinearLayout(this);appsHeader.setGravity(Gravity.CENTER_VERTICAL);appsHeader.addView(text("Apps",16,INK,true),new LinearLayout.LayoutParams(0,dp(30),1));TextView all=text("All apps  ›",11,ACCENT,true);all.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);all.setOnClickListener(v->open(new Intent(this,AppLibraryActivity.class)));appsHeader.addView(all,new LinearLayout.LayoutParams(-2,dp(30)));root.addView(appsHeader);
        appGrid=new GridLayout(this);appGrid.setColumnCount(4);root.addView(appGrid,new LinearLayout.LayoutParams(-1,dp(282)));

        TextView search=text("⌕   Search apps",12,Color.rgb(83,91,105),true);search.setGravity(Gravity.CENTER);search.setBackground(round(Color.argb(205,255,255,255),dp(28)));search.setElevation(dp(8));search.setOnClickListener(v->open(new Intent(this,AppLibraryActivity.class)));press(search);LinearLayout.LayoutParams sr=lp(dp(164),dp(42),0,4,0,5);sr.gravity=Gravity.CENTER_HORIZONTAL;root.addView(search,sr);
        TextView gesture=text("↑ Apps     ↓ kiri Notifications     ↓ kanan Control Center",8.5f,Color.rgb(115,121,131),false);gesture.setGravity(Gravity.CENTER);root.addView(gesture,new LinearLayout.LayoutParams(-1,dp(23)));

        LinearLayout dock=new LinearLayout(this);dock.setGravity(Gravity.CENTER);dock.setPadding(dp(9),dp(8),dp(9),dp(8));dock.setBackground(round(Color.argb(218,255,255,255),dp(32)));dock.setElevation(dp(20));
        dockItem(dock,"☎",()->openCategory(Intent.CATEGORY_APP_CONTACTS));dockItem(dock,"✉",()->openCategory(Intent.CATEGORY_APP_MESSAGING));dockItem(dock,"✦",()->open(new Intent(this,AssistantActivity.class)));dockItem(dock,"◉",()->open(new Intent("android.media.action.IMAGE_CAPTURE")));dockItem(dock,"⚙",()->open(new Intent(this,AiriSettingsActivity.class)));
        FrameLayout.LayoutParams dl=new FrameLayout.LayoutParams(-1,dp(78),Gravity.BOTTOM);dl.setMargins(dp(19),0,dp(19),dp(16));stage.addView(dock,dl);
        setContentView(stage);
    }

    private LinearLayout whiteCard(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(15),dp(16),dp(14));c.setBackground(round(Color.argb(214,255,255,255),dp(28)));c.setElevation(dp(10));return c;}
    private LinearLayout miniCard(String big,String title,String sub,int accent){LinearLayout c=whiteCard();c.setPadding(dp(12),dp(7),dp(12),dp(7));LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView b=text(big,18,accent,true);row.addView(b,new LinearLayout.LayoutParams(0,-2,1));TextView arr=text("↗",12,Color.rgb(91,99,112),true);row.addView(arr);c.addView(row);c.addView(text(title,10,Color.rgb(33,38,46),true));c.addView(text(sub,8.5f,Color.rgb(107,114,126),false));press(c);return c;}
    private void smartAction(LinearLayout row,String glyph,String label,Runnable r){LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);TextView g=text(glyph,18,Color.rgb(42,47,56),true);g.setGravity(Gravity.CENTER);item.addView(g,new LinearLayout.LayoutParams(-1,dp(29)));TextView l=text(label,8.5f,Color.rgb(84,91,103),true);l.setGravity(Gravity.CENTER);item.addView(l,new LinearLayout.LayoutParams(-1,dp(18)));item.setOnClickListener(v->r.run());press(item);row.addView(item,new LinearLayout.LayoutParams(0,dp(54),1));}

    private void loadApps(){apps.clear();Intent i=new Intent(Intent.ACTION_MAIN,null);i.addCategory(Intent.CATEGORY_LAUNCHER);for(ResolveInfo r:pm.queryIntentActivities(i,0)){if(r.activityInfo==null||getPackageName().equals(r.activityInfo.packageName))continue;CharSequence l=r.loadLabel(pm);apps.add(new AppEntry(l==null?r.activityInfo.packageName:l.toString(),r.activityInfo.packageName));}Collections.sort(apps,(a,b)->a.label.compareToIgnoreCase(b.label));renderApps();}
    private void renderApps(){if(appGrid==null)return;appGrid.removeAllViews();int count=Math.min(12,apps.size());for(int n=0;n<count;n++){AppEntry a=apps.get(n);LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);FrameLayout iconBox=new FrameLayout(this);iconBox.setBackground(round(Color.argb(168,255,255,255),dp(20)));iconBox.setElevation(dp(5));ImageView iv=new ImageView(this);try{iv.setImageDrawable(pm.getApplicationIcon(a.pkg));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}iv.setPadding(dp(5),dp(5),dp(5),dp(5));iconBox.addView(iv,new FrameLayout.LayoutParams(-1,-1));tile.addView(iconBox,new LinearLayout.LayoutParams(dp(58),dp(58)));TextView name=text(a.label,9,Color.rgb(37,42,50),true);name.setGravity(Gravity.CENTER);name.setMaxLines(1);tile.addView(name,new LinearLayout.LayoutParams(-1,dp(25)));tile.setOnClickListener(v->{Intent x=pm.getLaunchIntentForPackage(a.pkg);if(x!=null)open(x);});press(tile);GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(92);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(1),dp(2),dp(1));appGrid.addView(enter(tile),p);}}
    private void dockItem(LinearLayout d,String glyph,Runnable r){TextView v=text(glyph,23,Color.rgb(36,41,50),true);v.setGravity(Gravity.CENTER);v.setBackground(round(Color.argb(110,255,255,255),dp(22)));v.setOnClickListener(x->r.run());press(v);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(58),1);p.setMargins(dp(3),0,dp(3),0);d.addView(v,p);}

    private android.graphics.drawable.GradientDrawable round(int color,float radius){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(color);g.setCornerRadius(radius);g.setStroke(dp(1),Color.argb(70,255,255,255));return g;}
    private void press(View v){v.setClickable(true);v.setOnTouchListener((x,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN)x.animate().scaleX(.94f).scaleY(.94f).setDuration(65).start();else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)x.animate().scaleX(1.025f).scaleY(1.025f).setDuration(100).withEndAction(()->x.animate().scaleX(1).scaleY(1).setDuration(160).setInterpolator(new DecelerateInterpolator(2f)).start()).start();return false;});}
    private View enter(View v){v.setAlpha(0);v.setTranslationY(dp(10));int delay=Math.min(260,enterIndex++*24);v.animate().alpha(1).translationY(0).setStartDelay(delay).setDuration(330).setInterpolator(new DecelerateInterpolator(1.7f)).start();return v;}
    private void pulseIsland(){handler.postDelayed(new Runnable(){public void run(){if(islandText==null)return;islandText.animate().scaleX(1.025f).scaleY(1.025f).setDuration(500).withEndAction(()->islandText.animate().scaleX(1).scaleY(1).setDuration(650).start()).start();handler.postDelayed(this,3200);}},1000);}
    private void openCategory(String cat){try{Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(cat);startActivity(i);}catch(Exception e){open(new Intent(this,AppLibraryActivity.class));}}
    private void web(String q){try{Intent i=new Intent(Intent.ACTION_WEB_SEARCH);i.putExtra("query",q);startActivity(i);}catch(Exception ignored){}}
    private void open(Intent i){try{startActivity(i);overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);}catch(Exception ignored){}}
    private void tick(){handler.post(new Runnable(){public void run(){if(clock!=null)clock.setText(now("HH:mm"));handler.postDelayed(this,30000);}});}
    private String now(String f){return new SimpleDateFormat(f,Locale.getDefault()).format(new Date());}
    private TextView text(String s,float z,int c,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private static final class AppEntry{final String label,pkg;AppEntry(String l,String p){label=l;pkg=p;}}
}

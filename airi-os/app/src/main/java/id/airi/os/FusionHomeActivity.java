package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FusionHomeActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private PackageManager pm; private GridLayout suggestionGrid; private TextView clock,date; private float downX,downY; private long downAt;
    @Override protected void onCreate(Bundle b){super.onCreate(b);pm=getPackageManager();build();refresh();tick();}
    @Override protected void onResume(){super.onResume();refresh();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
    @Override public void onBackPressed(){}
    @Override public boolean dispatchTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();downAt=System.currentTimeMillis();}else if(e.getAction()==MotionEvent.ACTION_UP){float dx=e.getX()-downX,dy=e.getY()-downY;long dt=System.currentTimeMillis()-downAt;if(dt<700&&Math.abs(dy)>dp(100)&&Math.abs(dy)>Math.abs(dx)*1.15f){if(dy<0)open(new Intent(this,AppLibraryActivity.class));else if(downX<getResources().getDisplayMetrics().widthPixels/2f)open(new Intent(this,NotificationCenterActivity.class));else open(new Intent(this,ControlCenterActivity.class));}}return super.dispatchTouchEvent(e);}

    private void build(){
        final int INK=Color.rgb(24,29,38), MUTED=Color.rgb(100,108,121), ACCENT=Color.rgb(76,112,245);
        FrameLayout stage=new FrameLayout(this);stage.addView(new HyperFlowBackdropView(this),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(34),dp(18),dp(110));stage.addView(root,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout status=new LinearLayout(this);status.setGravity(Gravity.CENTER_VERTICAL);TextView brand=t("AIRI FUSION",11,true);brand.setLetterSpacing(.14f);brand.setTextColor(INK);status.addView(brand,new LinearLayout.LayoutParams(0,dp(32),1));TextView edit=t("✦ v19",11,true);edit.setTextColor(ACCENT);edit.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);edit.setOnClickListener(v->open(new Intent(this,LauncherSettingsActivity.class)));status.addView(edit,new LinearLayout.LayoutParams(0,dp(32),1));root.addView(status);

        clock=t("--:--",55,false);clock.setTextColor(INK);clock.setGravity(Gravity.CENTER_HORIZONTAL);root.addView(clock,lp(-1,dp(70),0,8,0,0));date=t("",13,true);date.setTextColor(MUTED);date.setGravity(Gravity.CENTER_HORIZONTAL);root.addView(date,lp(-1,dp(26),0,0,0,18));

        TextView search=t("⌕   Search apps, tools & settings",13,true);search.setTextColor(Color.rgb(72,79,91));search.setGravity(Gravity.CENTER_VERTICAL);search.setPadding(dp(20),0,dp(16),0);search.setBackground(glass(30));search.setElevation(dp(10));search.setOnClickListener(v->open(new Intent(this,UniversalSearchActivity.class)));root.addView(search,lp(-1,dp(58),0,0,0,16));

        LinearLayout smart=new LinearLayout(this);smart.setGravity(Gravity.CENTER);smart.setPadding(dp(7),dp(6),dp(7),dp(6));smart.setBackground(glass(27));smart.setElevation(dp(7));quick(smart,"⌕","Search",()->open(new Intent(this,UniversalSearchActivity.class)));quick(smart,"▦","Apps",()->open(new Intent(this,AppLibraryActivity.class)));quick(smart,"◫","Control",()->open(new Intent(this,ControlCenterActivity.class)));quick(smart,"▤","Alerts",()->open(new Intent(this,NotificationCenterActivity.class)));quick(smart,"⚙","Edit",()->open(new Intent(this,LauncherSettingsActivity.class)));root.addView(smart,lp(-1,dp(70),0,0,0,18));

        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView h=t("Smart Suggestions",16,true);h.setTextColor(INK);row.addView(h,new LinearLayout.LayoutParams(0,dp(32),1));TextView all=t("All apps  ›",11,true);all.setTextColor(ACCENT);all.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);all.setOnClickListener(v->open(new Intent(this,AppLibraryActivity.class)));row.addView(all,new LinearLayout.LayoutParams(-2,dp(32)));root.addView(row);
        suggestionGrid=new GridLayout(this);suggestionGrid.setColumnCount(4);root.addView(suggestionGrid,new LinearLayout.LayoutParams(-1,dp(210)));

        TextView hint=t("Swipe ↑ apps   •   ↓ left notifications   •   ↓ right controls",9,false);hint.setTextColor(MUTED);hint.setGravity(Gravity.CENTER);root.addView(hint,lp(-1,dp(26),0,3,0,0));
        root.setOnLongClickListener(v->{open(new Intent(this,LauncherSettingsActivity.class));return true;});root.setLongClickable(true);

        LinearLayout dock=new LinearLayout(this);dock.setGravity(Gravity.CENTER);dock.setPadding(dp(8),dp(8),dp(8),dp(8));dock.setBackground(glass(34));dock.setElevation(dp(18));
        dock(dock,"☎",()->openAction(Intent.ACTION_DIAL));dock(dock,"✉",()->openCategory(Intent.CATEGORY_APP_MESSAGING));dock(dock,"⌕",()->open(new Intent(this,UniversalSearchActivity.class)));dock(dock,"▦",()->open(new Intent(this,AppLibraryActivity.class)));dock(dock,"⚙",()->open(new Intent(this,LauncherSettingsActivity.class)));
        FrameLayout.LayoutParams dl=new FrameLayout.LayoutParams(-1,dp(78),Gravity.BOTTOM);dl.setMargins(dp(18),0,dp(18),dp(16));stage.addView(dock,dl);
        setContentView(stage);AiriLiquidSkin.apply(this);
    }

    private void refresh(){if(suggestionGrid==null||pm==null)return;suggestionGrid.removeAllViews();List<LauncherCatalog.App> catalog=LauncherCatalog.load(this,false);List<LauncherCatalog.App> ordered=new ArrayList<>();
        for(LauncherCatalog.App a:catalog)if(LauncherPrefs.isFavorite(this,a.pkg))ordered.add(a);
        if(LauncherPrefs.suggestions(this)){for(String pkg:AppLaunchStats.top(this,24)){for(LauncherCatalog.App a:catalog)if(a.pkg.equals(pkg)&&!contains(ordered,pkg)){ordered.add(a);break;}}}
        for(LauncherCatalog.App a:catalog)if(!contains(ordered,a.pkg))ordered.add(a);
        int max=Math.min(8,ordered.size());for(int i=0;i<max;i++)addApp(ordered.get(i));
    }
    private boolean contains(List<LauncherCatalog.App> list,String pkg){for(LauncherCatalog.App a:list)if(a.pkg.equals(pkg))return true;return false;}
    private void addApp(LauncherCatalog.App a){LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);ImageView iv=new ImageView(this);try{iv.setImageDrawable(AiriIconPack.drawable(this,pm.getApplicationIcon(a.pkg),dp(60)));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}tile.addView(iv,new LinearLayout.LayoutParams(dp(58),dp(58)));if(LauncherPrefs.labels(this)){TextView name=t(a.label,9,true);name.setTextColor(Color.rgb(37,42,50));name.setGravity(Gravity.CENTER);name.setMaxLines(1);tile.addView(name,new LinearLayout.LayoutParams(-1,dp(25)));}tile.setOnClickListener(v->{Intent x=pm.getLaunchIntentForPackage(a.pkg);if(x!=null){AppLaunchStats.record(this,a.pkg);open(x);}});tile.setOnLongClickListener(v->{LauncherPrefs.favorite(this,a.pkg,!LauncherPrefs.isFavorite(this,a.pkg));refresh();return true;});GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(98);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(2),dp(2),dp(2));suggestionGrid.addView(tile,p);}

    private void quick(LinearLayout row,String glyph,String label,Runnable r){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);TextView a=t(glyph,20,true);a.setGravity(Gravity.CENTER);a.setTextColor(Color.rgb(37,43,52));c.addView(a,new LinearLayout.LayoutParams(-1,dp(30)));TextView b=t(label,8.5f,true);b.setGravity(Gravity.CENTER);b.setTextColor(Color.rgb(87,94,106));c.addView(b,new LinearLayout.LayoutParams(-1,dp(20)));c.setOnClickListener(v->r.run());row.addView(c,new LinearLayout.LayoutParams(0,dp(56),1));}
    private void dock(LinearLayout row,String glyph,Runnable r){TextView v=t(glyph,23,true);v.setGravity(Gravity.CENTER);v.setTextColor(Color.rgb(35,40,49));v.setBackground(glass(23));v.setOnClickListener(x->r.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(58),1);p.setMargins(dp(3),0,dp(3),0);row.addView(v,p);}
    private void openAction(String action){try{open(new Intent(action));}catch(Exception ignored){}}
    private void openCategory(String cat){try{Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(cat);startActivity(i);}catch(Exception e){open(new Intent(this,AppLibraryActivity.class));}}
    private void open(Intent i){try{startActivity(i);overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);}catch(Exception ignored){}}
    private void tick(){handler.post(new Runnable(){public void run(){Date d=new Date();if(clock!=null)clock.setText(new SimpleDateFormat("HH:mm",Locale.getDefault()).format(d));if(date!=null)date.setText(new SimpleDateFormat("EEEE, d MMMM",new Locale("id","ID")).format(d));handler.postDelayed(this,30000);}});}
    private android.graphics.drawable.GradientDrawable glass(float radius){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(Color.argb(210,255,255,255));g.setCornerRadius(dp((int)radius));g.setStroke(dp(1),Color.argb(75,255,255,255));return g;}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTypeface(android.graphics.Typeface.create("sans",bold?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:w;LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,h);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

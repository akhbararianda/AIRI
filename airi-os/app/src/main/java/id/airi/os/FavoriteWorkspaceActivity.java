package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteWorkspaceActivity extends Activity {
    private PackageManager pm;
    @Override protected void onCreate(Bundle b){super.onCreate(b);pm=getPackageManager();build();}
    private void build(){FrameLayout stage=new FrameLayout(this);stage.addView(new HyperFlowBackdropView(this),new FrameLayout.LayoutParams(-1,-1));ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(38),dp(18),dp(36));sc.addView(root,new ScrollView.LayoutParams(-1,-2));stage.addView(sc,new FrameLayout.LayoutParams(-1,-1));
        TextView title=t("Focus Workspace",30,true);root.addView(title);TextView sub=t("Favorites + most-used apps • swipe back to return Home",11,false);sub.setTextColor(AiriTheme.muted(this));root.addView(sub,lp(-1,-2,0,2,0,16));
        List<LauncherCatalog.App> all=LauncherCatalog.load(this,false);Map<String,LauncherCatalog.App> map=new HashMap<>();for(LauncherCatalog.App a:all)map.put(a.pkg,a);
        List<LauncherCatalog.App> chosen=new ArrayList<>();for(String pkg:LauncherPrefs.favorites(this)){LauncherCatalog.App a=map.get(pkg);if(a!=null)chosen.add(a);}for(String pkg:AppLaunchStats.top(this,24)){LauncherCatalog.App a=map.get(pkg);if(a!=null&&!contains(chosen,pkg))chosen.add(a);if(chosen.size()>=16)break;}
        TextView info=t(chosen.isEmpty()?"No favorites yet • long-press apps in AIRI Library to pin them":"Your priority space",13,true);info.setPadding(dp(15),dp(13),dp(15),dp(13));info.setBackground(AiriGlassDrawable.make(this,24,AiriGlassDrawable.REGULAR));root.addView(info,lp(-1,-2,0,0,0,14));
        GridLayout grid=new GridLayout(this);grid.setColumnCount(4);root.addView(grid,new LinearLayout.LayoutParams(-1,-2));for(LauncherCatalog.App a:chosen)addApp(grid,a);
        TextView library=t("Open full AIRI Library",13,true);library.setGravity(Gravity.CENTER);library.setBackground(AiriGlassDrawable.make(this,24,AiriGlassDrawable.REGULAR));library.setOnClickListener(v->startActivity(new Intent(this,AppLibraryActivity.class)));root.addView(library,lp(-1,dp(54),0,18,0,0));setContentView(stage);AiriLiquidSkin.apply(this);}
    private boolean contains(List<LauncherCatalog.App> list,String pkg){for(LauncherCatalog.App a:list)if(a.pkg.equals(pkg))return true;return false;}
    private void addApp(GridLayout grid,LauncherCatalog.App a){LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);FrameLayout box=new FrameLayout(this);ImageView iv=new ImageView(this);try{iv.setImageDrawable(AiriIconPack.drawable(this,pm.getApplicationIcon(a.pkg),dp(62)));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}box.addView(iv,new FrameLayout.LayoutParams(-1,-1));int count=LauncherPrefs.badges(this)?AiriNotificationListener.count(a.pkg):0;if(count>0){TextView badge=t(count>99?"99+":String.valueOf(count),8,true);badge.setTextColor(Color.WHITE);badge.setGravity(Gravity.CENTER);badge.setBackground(round(Color.rgb(235,70,70),dp(10)));FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(dp(count>9?25:20),dp(20),Gravity.TOP|Gravity.RIGHT);box.addView(badge,bp);}tile.addView(box,new LinearLayout.LayoutParams(dp(62),dp(62)));TextView n=t(a.label,9,true);n.setGravity(Gravity.CENTER);n.setMaxLines(1);tile.addView(n,new LinearLayout.LayoutParams(-1,dp(25)));tile.setOnClickListener(v->{Intent i=pm.getLaunchIntentForPackage(a.pkg);if(i!=null){AppLaunchStats.record(this,a.pkg);startActivity(i);}});GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(94);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(3),dp(2),dp(3));grid.addView(tile,p);}
    private android.graphics.drawable.GradientDrawable round(int color,float r){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(color);g.setCornerRadius(r);return g;}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(AiriTheme.ink(this));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h;LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

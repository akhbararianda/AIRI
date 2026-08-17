package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AppLibraryActivity extends Activity {
    private static class AppEntry{final String label,pkg;AppEntry(String l,String p){label=l;pkg=p;}}
    private final List<AppEntry> apps=new ArrayList<>();private GridLayout grid;private PackageManager pm;
    @Override protected void onCreate(Bundle b){super.onCreate(b);pm=getPackageManager();build();load();}
    private void build(){FrameLayout stage=new FrameLayout(this);stage.addView(new HyperFlowBackdropView(this),new FrameLayout.LayoutParams(-1,-1));ScrollView sc=new ScrollView(this);sc.setFillViewport(true);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(40),dp(18),dp(30));sc.addView(root,new ScrollView.LayoutParams(-1,-2));stage.addView(sc,new FrameLayout.LayoutParams(-1,-1));
        TextView title=t("Apps",30,true);title.setTextColor(Color.rgb(27,31,38));root.addView(title);TextView sub=t("AIRI Infinity • "+AiriIconPack.label(this),11,false);sub.setTextColor(Color.rgb(100,108,120));root.addView(sub,lp(-1,-2,0,2,0,14));
        EditText search=new EditText(this);search.setSingleLine(true);search.setHint("Search apps");search.setTextColor(Color.rgb(30,34,42));search.setHintTextColor(Color.rgb(124,132,144));search.setPadding(dp(18),0,dp(18),0);search.setBackground(round(Color.argb(218,255,255,255),dp(27)));search.setElevation(dp(8));root.addView(search,lp(-1,dp(52),0,0,0,16));search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){render(s.toString());}public void afterTextChanged(Editable e){}});
        grid=new GridLayout(this);grid.setColumnCount(4);root.addView(grid,new LinearLayout.LayoutParams(-1,-2));setContentView(stage);
    }
    private void load(){apps.clear();Intent i=new Intent(Intent.ACTION_MAIN,null);i.addCategory(Intent.CATEGORY_LAUNCHER);for(ResolveInfo r:pm.queryIntentActivities(i,0)){if(r.activityInfo==null||getPackageName().equals(r.activityInfo.packageName))continue;CharSequence l=r.loadLabel(pm);apps.add(new AppEntry(l==null?r.activityInfo.packageName:l.toString(),r.activityInfo.packageName));}Collections.sort(apps,(a,b)->a.label.compareToIgnoreCase(b.label));render("");}
    private void render(String f){grid.removeAllViews();String q=f==null?"":f.toLowerCase(Locale.ROOT).trim();for(AppEntry a:apps){if(!q.isEmpty()&&!a.label.toLowerCase(Locale.ROOT).contains(q))continue;LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);FrameLayout box=new FrameLayout(this);box.setElevation(dp(5));ImageView iv=new ImageView(this);try{iv.setImageDrawable(AiriIconPack.drawable(this,pm.getApplicationIcon(a.pkg),dp(64)));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}iv.setPadding(dp(2),dp(2),dp(2),dp(2));box.addView(iv,new FrameLayout.LayoutParams(-1,-1));tile.addView(box,new LinearLayout.LayoutParams(dp(62),dp(62)));TextView n=t(a.label,9.5f,true);n.setTextColor(Color.rgb(35,40,48));n.setGravity(Gravity.CENTER);n.setMaxLines(1);tile.addView(n,new LinearLayout.LayoutParams(-1,dp(27)));tile.setOnClickListener(v->{Intent x=pm.getLaunchIntentForPackage(a.pkg);if(x!=null){AppLaunchStats.record(this,a.pkg);startActivity(x);}});GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(94);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(2),dp(2),dp(2));grid.addView(tile,p);}}
    private android.graphics.drawable.GradientDrawable round(int color,float radius){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(color);g.setCornerRadius(radius);g.setStroke(dp(1),Color.argb(70,255,255,255));return g;}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

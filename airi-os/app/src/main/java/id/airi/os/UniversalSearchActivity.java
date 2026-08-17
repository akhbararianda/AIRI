package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UniversalSearchActivity extends Activity {
    private static class AppEntry{final String label,pkg;AppEntry(String l,String p){label=l;pkg=p;}}
    private final List<AppEntry> apps=new ArrayList<>();
    private LinearLayout results;private PackageManager pm;
    @Override protected void onCreate(Bundle b){super.onCreate(b);pm=getPackageManager();load();build();}
    private void build(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(30),dp(18),dp(20));root.setBackgroundColor(Color.rgb(244,248,252));root.addView(t("Universal Search",29,true));TextView sub=t("Apps • AIRI tools • Android settings",11,false);sub.setTextColor(Color.rgb(91,101,115));root.addView(sub,lp(-1,-2,0,2,0,12));EditText q=new EditText(this);q.setSingleLine(true);q.setHint("Cari apa saja...");q.setTextSize(15);q.setPadding(dp(16),0,dp(16),0);q.setBackground(AiriGlassDrawable.make(this,27,AiriGlassDrawable.REGULAR));root.addView(q,lp(-1,dp(54),0,0,0,12));ScrollView sc=new ScrollView(this);results=new LinearLayout(this);results.setOrientation(LinearLayout.VERTICAL);sc.addView(results,new ScrollView.LayoutParams(-1,-2));root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));q.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){render(s.toString());}public void afterTextChanged(Editable e){}});setContentView(root);render("");}
    private void load(){Intent i=new Intent(Intent.ACTION_MAIN,null);i.addCategory(Intent.CATEGORY_LAUNCHER);for(ResolveInfo r:pm.queryIntentActivities(i,0)){if(r.activityInfo==null||getPackageName().equals(r.activityInfo.packageName))continue;CharSequence l=r.loadLabel(pm);apps.add(new AppEntry(l==null?r.activityInfo.packageName:l.toString(),r.activityInfo.packageName));}Collections.sort(apps,(a,b)->a.label.compareToIgnoreCase(b.label));}
    private void render(String raw){results.removeAllViews();String q=raw==null?"":raw.toLowerCase(Locale.ROOT).trim();if(q.isEmpty()){action("✦  Irzuqni","AI assistant",new Intent(this,AssistantActivity.class));action("◫  Control Center","Quick controls",new Intent(this,ControlCenterActivity.class));action("▱  Screen Intelligence","Screen tools",new Intent(this,ScreenIntelligenceActivity.class));action("⚙  Android Settings","System settings",new Intent(Settings.ACTION_SETTINGS));}else{if(match(q,"wifi internet network"))action("⌁  Internet settings","Wi‑Fi and connectivity",new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));if(match(q,"bluetooth bt"))action("Bluetooth settings","Connected devices",new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));if(match(q,"battery power"))action("Battery settings","Battery and power",new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS));if(match(q,"display screen brightness"))action("Display settings","Brightness and display",new Intent(Settings.ACTION_DISPLAY_SETTINGS));if(match(q,"privacy permission"))action("Privacy settings","Permissions and privacy",new Intent(Settings.ACTION_PRIVACY_SETTINGS));for(AppEntry a:apps){if(a.label.toLowerCase(Locale.ROOT).contains(q)){Intent x=pm.getLaunchIntentForPackage(a.pkg);if(x!=null)appRow(a,x);}}}}
    private boolean match(String q,String words){for(String s:words.split(" "))if(q.contains(s)||s.contains(q))return true;return false;}
    private void appRow(AppEntry a,Intent x){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(8),dp(12),dp(8));row.setBackground(AiriGlassDrawable.make(this,24,AiriGlassDrawable.CLEAR));ImageView iv=new ImageView(this);try{iv.setImageDrawable(pm.getApplicationIcon(a.pkg));}catch(Exception ignored){}row.addView(iv,new LinearLayout.LayoutParams(dp(42),dp(42)));TextView n=t(a.label,14,true);n.setPadding(dp(12),0,0,0);row.addView(n,new LinearLayout.LayoutParams(0,dp(48),1));row.setOnClickListener(v->startActivity(x));results.addView(row,lp(-1,dp(62),0,0,0,6));}
    private void action(String title,String desc,Intent i){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(14),dp(10),dp(14),dp(10));row.setBackground(AiriGlassDrawable.make(this,24,AiriGlassDrawable.REGULAR));row.addView(t(title,14,true));TextView d=t(desc,10,false);d.setTextColor(Color.rgb(91,101,114));row.addView(d);row.setOnClickListener(v->{try{startActivity(i);}catch(Exception ignored){}});results.addView(row,lp(-1,dp(66),0,0,0,6));}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(27,32,40));v.setTypeface(android.graphics.Typeface.create("sans",bold?1:0));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

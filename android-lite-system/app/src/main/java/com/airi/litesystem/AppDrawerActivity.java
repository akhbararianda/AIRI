package com.airi.litesystem;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AppDrawerActivity extends Activity {
    private final ArrayList<AppItem> all=new ArrayList<>(), shown=new ArrayList<>(); private AppAdapter adapter;
    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(Ui.BG); getWindow().setNavigationBarColor(Ui.BG); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(Ui.dp(this,18),Ui.dp(this,12),Ui.dp(this,18),Ui.dp(this,12)); root.setBackgroundColor(Ui.BG); root.addView(Ui.text(this,"Semua aplikasi",28,Ui.TEXT)); EditText q=new EditText(this); q.setSingleLine(true); q.setHint("Cari aplikasi"); q.setTextSize(16); q.setBackground(Ui.bg(0xFFFFFFFF,26,this)); q.setPadding(Ui.dp(this,18),0,Ui.dp(this,18),0); root.addView(q,new LinearLayout.LayoutParams(-1,Ui.dp(this,54))); Ui.setMargins(q,0,14,0,12,this); ListView list=new ListView(this); list.setDivider(null); list.setBackgroundColor(Ui.BG); root.addView(list,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root); load(); adapter=new AppAdapter(); list.setAdapter(adapter); list.setOnItemClickListener((p,v,pos,id)->launch(shown.get(pos))); q.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){filter(s.toString());} public void afterTextChanged(Editable e){}}); }
    private void load(){ PackageManager pm=getPackageManager(); Intent i=new Intent(Intent.ACTION_MAIN,null); i.addCategory(Intent.CATEGORY_LAUNCHER); List<ResolveInfo> rs=pm.queryIntentActivities(i,0); for(ResolveInfo r:rs){String pkg=r.activityInfo.packageName;if(pkg.equals(getPackageName()))continue;AppItem x=new AppItem();x.label=r.loadLabel(pm).toString();x.pkg=pkg;x.cls=r.activityInfo.name;x.icon=r.loadIcon(pm);all.add(x);} Collections.sort(all,Comparator.comparing(a->a.label.toLowerCase(Locale.getDefault())));shown.addAll(all); }
    private void filter(String s){shown.clear();String q=s.toLowerCase(Locale.getDefault()).trim();for(AppItem a:all)if(q.isEmpty()||a.label.toLowerCase(Locale.getDefault()).contains(q))shown.add(a);adapter.notifyDataSetChanged();}
    private void launch(AppItem a){try{Intent i=new Intent();i.setClassName(a.pkg,a.cls);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}catch(Exception ignored){}}
    static class AppItem{String label,pkg,cls;Drawable icon;}
    class AppAdapter extends BaseAdapter{public int getCount(){return shown.size();}public Object getItem(int p){return shown.get(p);}public long getItemId(int p){return p;}public View getView(int p,View v,ViewGroup parent){AppItem a=shown.get(p);LinearLayout row=new LinearLayout(AppDrawerActivity.this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(Ui.dp(AppDrawerActivity.this,8),Ui.dp(AppDrawerActivity.this,9),Ui.dp(AppDrawerActivity.this,8),Ui.dp(AppDrawerActivity.this,9));ImageView im=new ImageView(AppDrawerActivity.this);im.setImageDrawable(a.icon);row.addView(im,new LinearLayout.LayoutParams(Ui.dp(AppDrawerActivity.this,42),Ui.dp(AppDrawerActivity.this,42)));TextView tx=Ui.text(AppDrawerActivity.this,a.label,16,Ui.TEXT);row.addView(tx,new LinearLayout.LayoutParams(0,Ui.dp(AppDrawerActivity.this,52),1));Ui.setMargins(tx,14,0,0,0,AppDrawerActivity.this);return row;}}
}

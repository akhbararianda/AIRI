package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ThemeCenterActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(28),dp(18),dp(28));root.setBackgroundColor(AiriTheme.surface(this));sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        TextView title=t("AIRI Theme Center",30,true);root.addView(title);TextView sub=t("Signature Edition • choose your AIRI identity",12,false);sub.setTextColor(AiriTheme.muted(this));root.addView(sub,lp(-1,-2,0,3,0,18));
        add(root,"Pearl Titanium","Warm pearl, graphite and champagne gold",AiriTheme.PEARL);
        add(root,"Obsidian Emerald","Deep graphite with soft emerald glass",AiriTheme.EMERALD);
        add(root,"Sunset Glass","Warm clay, copper and champagne highlights",AiriTheme.SUNSET);
        add(root,"Aurora Lavender","Muted plum, pearl and lavender glass",AiriTheme.LAVENDER);
        TextView note=t("Current: "+AiriTheme.label(this)+"\nTheme changes apply when AIRI Home or another AIRI screen is reopened.",11.5f,false);note.setPadding(dp(14),dp(14),dp(14),dp(14));note.setTextColor(AiriTheme.muted(this));note.setBackground(AiriGlassDrawable.make(this,26,AiriGlassDrawable.CLEAR));root.addView(note,lp(-1,-2,0,10,0,0));setContentView(sc);AiriLiquidSkin.apply(this);}
    private void add(LinearLayout root,String name,String desc,String id){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(16),dp(15),dp(16),dp(14));card.setBackground(AiriGlassDrawable.make(this,28,AiriGlassDrawable.REGULAR));card.setElevation(dp(8));TextView a=t(name,17,true);card.addView(a);TextView b=t(desc,11,false);b.setTextColor(AiriTheme.muted(this));card.addView(b);if(id.equals(AiriTheme.current(this))){TextView c=t("✓ Active",11,true);c.setTextColor(AiriTheme.accent(this));card.addView(c);}card.setOnClickListener(v->{AiriTheme.set(this,id);Toast.makeText(this,name+" applied",Toast.LENGTH_SHORT).show();Intent i=new Intent(this,MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);finish();});root.addView(card,lp(-1,dp(88),0,0,0,10));}
    private TextView t(String s,float z,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(AiriTheme.ink(this));v.setTypeface(android.graphics.Typeface.create("sans",bold?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

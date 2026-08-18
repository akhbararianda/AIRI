package id.airi.os;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LauncherBackupActivity extends Activity {
    private EditText box;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(38),dp(18),dp(36));root.setBackgroundColor(AiriTheme.surface(this));sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        TextView title=t("Backup Launcher",30,true);root.addView(title);TextView sub=t("Simpan favorites, hidden apps, dock, grid, labels dan preferensi Fusion.",11,false);sub.setTextColor(AiriTheme.muted(this));root.addView(sub,lp(-1,-2,0,2,0,16));
        box=new EditText(this);box.setMinLines(8);box.setGravity(Gravity.TOP);box.setText(LauncherPrefs.exportBackup(this));box.setTextSize(11);box.setTextColor(AiriTheme.ink(this));box.setBackground(AiriGlassDrawable.make(this,22,AiriGlassDrawable.REGULAR));box.setPadding(dp(14),dp(14),dp(14),dp(14));root.addView(box,lp(-1,dp(220),0,0,0,12));
        button(root,"Copy backup code",()->{ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);cm.setPrimaryClip(ClipData.newPlainText("AIRI Fusion Backup",box.getText().toString()));Toast.makeText(this,"Backup copied",Toast.LENGTH_SHORT).show();});
        button(root,"Load from clipboard",()->{ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);if(cm.hasPrimaryClip()&&cm.getPrimaryClip()!=null&&cm.getPrimaryClip().getItemCount()>0){CharSequence s=cm.getPrimaryClip().getItemAt(0).coerceToText(this);box.setText(s);Toast.makeText(this,"Clipboard loaded",Toast.LENGTH_SHORT).show();}});
        button(root,"Restore this backup",()->{boolean ok=LauncherPrefs.importBackup(this,box.getText().toString());Toast.makeText(this,ok?"Fusion layout restored":"Backup code invalid",Toast.LENGTH_LONG).show();if(ok)finish();});
        setContentView(sc);AiriLiquidSkin.apply(this);}
    private void button(LinearLayout root,String label,Runnable r){TextView b=t(label,14,true);b.setGravity(Gravity.CENTER);b.setBackground(AiriGlassDrawable.make(this,24,AiriGlassDrawable.REGULAR));b.setElevation(dp(5));b.setOnClickListener(v->r.run());root.addView(b,lp(-1,dp(54),0,0,0,9));}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(AiriTheme.ink(this));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h;LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

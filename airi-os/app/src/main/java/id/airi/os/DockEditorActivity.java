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
import android.widget.Toast;
import java.util.List;

public class DockEditorActivity extends Activity {
    private LinearLayout slots; private GridLayout grid; private PackageManager pm; private int selected=0; private List<LauncherCatalog.App> apps;
    @Override protected void onCreate(Bundle b){super.onCreate(b);pm=getPackageManager();apps=LauncherCatalog.load(this,false);build();}
    @Override protected void onResume(){super.onResume();if(slots!=null)renderSlots();}
    private void build(){ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(38),dp(18),dp(36));root.setBackgroundColor(AiriTheme.surface(this));sc.addView(root,new ScrollView.LayoutParams(-1,-2));
        TextView title=t("Smart Dock",30,true);root.addView(title);TextView sub=t("AIRI v20 • choose any app for each of the 5 dock slots",11,false);sub.setTextColor(AiriTheme.muted(this));root.addView(sub,lp(-1,-2,0,2,0,16));
        slots=new LinearLayout(this);slots.setGravity(Gravity.CENTER);slots.setPadding(dp(8),dp(8),dp(8),dp(8));slots.setBackground(AiriGlassDrawable.make(this,28,AiriGlassDrawable.REGULAR));slots.setElevation(dp(7));root.addView(slots,lp(-1,dp(82),0,0,0,12));renderSlots();
        TextView help=t("Tap a slot, then tap an app below. Long-press a slot to clear it.",10.5f,false);help.setTextColor(AiriTheme.muted(this));root.addView(help,lp(-1,-2,2,0,2,12));
        TextView clear=t("Reset dock",12,true);clear.setGravity(Gravity.CENTER);clear.setBackground(AiriGlassDrawable.make(this,22,AiriGlassDrawable.CLEAR));clear.setOnClickListener(v->{LauncherPrefs.clearDock(this);selected=0;renderSlots();Toast.makeText(this,"Dock reset",Toast.LENGTH_SHORT).show();});root.addView(clear,lp(-1,dp(44),0,0,0,14));
        grid=new GridLayout(this);grid.setColumnCount(4);root.addView(grid,new LinearLayout.LayoutParams(-1,-2));renderApps();setContentView(sc);AiriLiquidSkin.apply(this);}
    private void renderSlots(){if(slots==null)return;slots.removeAllViews();List<String>d=LauncherPrefs.dock(this);for(int i=0;i<5;i++){final int slot=i;LinearLayout cell=new LinearLayout(this);cell.setOrientation(LinearLayout.VERTICAL);cell.setGravity(Gravity.CENTER);cell.setPadding(dp(3),dp(3),dp(3),dp(3));cell.setBackground(round(i==selected?Color.argb(235,229,237,255):Color.argb(110,255,255,255),dp(18)));String pkg=d.get(i);ImageView iv=new ImageView(this);if(pkg!=null&&!pkg.isEmpty()){try{iv.setImageDrawable(AiriIconPack.drawable(this,pm.getApplicationIcon(pkg),dp(42)));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}}else iv.setImageResource(android.R.drawable.ic_input_add);cell.addView(iv,new LinearLayout.LayoutParams(dp(39),dp(39)));TextView n=t(pkg==null||pkg.isEmpty()?"Slot "+(i+1):label(pkg),8.5f,true);n.setGravity(Gravity.CENTER);n.setMaxLines(1);cell.addView(n,new LinearLayout.LayoutParams(-1,dp(22)));cell.setOnClickListener(v->{selected=slot;renderSlots();});cell.setOnLongClickListener(v->{LauncherPrefs.setDockSlot(this,slot,"");renderSlots();Toast.makeText(this,"Slot "+(slot+1)+" cleared",Toast.LENGTH_SHORT).show();return true;});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(64),1);p.setMargins(dp(2),0,dp(2),0);slots.addView(cell,p);}}
    private void renderApps(){grid.removeAllViews();for(LauncherCatalog.App a:apps){LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);ImageView iv=new ImageView(this);try{iv.setImageDrawable(AiriIconPack.drawable(this,pm.getApplicationIcon(a.pkg),dp(58)));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}tile.addView(iv,new LinearLayout.LayoutParams(dp(56),dp(56)));TextView n=t(a.label,9,true);n.setGravity(Gravity.CENTER);n.setMaxLines(1);tile.addView(n,new LinearLayout.LayoutParams(-1,dp(25)));tile.setOnClickListener(v->{LauncherPrefs.setDockSlot(this,selected,a.pkg);Toast.makeText(this,"Slot "+(selected+1)+": "+a.label,Toast.LENGTH_SHORT).show();selected=(selected+1)%5;renderSlots();});GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(92);p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(3),dp(2),dp(3));grid.addView(tile,p);}}
    private String label(String pkg){try{CharSequence l=pm.getApplicationLabel(pm.getApplicationInfo(pkg,0));return l==null?pkg:l.toString();}catch(Exception e){return pkg;}}
    private android.graphics.drawable.GradientDrawable round(int color,float radius){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(color);g.setCornerRadius(radius);return g;}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(AiriTheme.ink(this));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h;LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class AppLibraryActivity extends Activity {
    private final String[] cats={"All","Social","Media","Work","Games","Travel","News","Tools","Other"};
    private final List<LauncherCatalog.App> apps=new ArrayList<>();
    private GridLayout grid; private PackageManager pm; private EditText search; private String category="All"; private TextView count;

    @Override protected void onCreate(Bundle b){super.onCreate(b);pm=getPackageManager();build();load();}
    @Override protected void onResume(){super.onResume();if(grid!=null)load();}

    private void build(){
        FrameLayout stage=new FrameLayout(this); stage.addView(new HyperFlowBackdropView(this),new FrameLayout.LayoutParams(-1,-1));
        ScrollView sc=new ScrollView(this); sc.setFillViewport(true);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(38),dp(18),dp(36));
        sc.addView(root,new ScrollView.LayoutParams(-1,-2)); stage.addView(sc,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titles=new LinearLayout(this);titles.setOrientation(LinearLayout.VERTICAL);
        TextView title=t("AIRI Library",30,true);title.setTextColor(Color.rgb(25,30,38));titles.addView(title);
        count=t("Fusion drawer",11,false);count.setTextColor(Color.rgb(100,108,120));titles.addView(count);
        top.addView(titles,new LinearLayout.LayoutParams(0,-2,1));
        TextView settings=t("⚙",24,true);settings.setGravity(Gravity.CENTER);settings.setBackground(round(Color.argb(205,255,255,255),dp(24)));settings.setOnClickListener(v->startActivity(new Intent(this,LauncherSettingsActivity.class)));top.addView(settings,new LinearLayout.LayoutParams(dp(48),dp(48)));
        root.addView(top,lp(-1,-2,0,0,0,16));

        search=new EditText(this);search.setSingleLine(true);search.setHint("Search apps or package…");search.setTextColor(Color.rgb(30,34,42));search.setHintTextColor(Color.rgb(124,132,144));search.setPadding(dp(18),0,dp(18),0);search.setBackground(round(Color.argb(226,255,255,255),dp(28)));search.setElevation(dp(8));root.addView(search,lp(-1,dp(54),0,0,0,12));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){render();} public void afterTextChanged(Editable e){}});

        if(LauncherPrefs.categories(this)){
            HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);LinearLayout chips=new LinearLayout(this);chips.setGravity(Gravity.CENTER_VERTICAL);hsv.addView(chips,new HorizontalScrollView.LayoutParams(-2,dp(44)));
            for(String c:cats){TextView chip=t(c,11,true);chip.setGravity(Gravity.CENTER);chip.setPadding(dp(15),0,dp(15),0);chip.setBackground(round(Color.argb(c.equals("All")?235:160,255,255,255),dp(20)));chip.setOnClickListener(v->{category=((TextView)v).getText().toString();render();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,dp(38));cp.setMargins(0,0,dp(7),0);chips.addView(chip,cp);}root.addView(hsv,lp(-1,dp(46),0,0,0,10));
        }
        grid=new GridLayout(this);grid.setColumnCount(LauncherPrefs.columns(this));root.addView(grid,new LinearLayout.LayoutParams(-1,-2));setContentView(stage);
        String initial=getIntent().getStringExtra("query");if(initial!=null){search.setText(initial);search.setSelection(initial.length());}
    }

    private void load(){apps.clear();apps.addAll(LauncherCatalog.load(this,false));render();}

    private void render(){
        if(grid==null)return;grid.removeAllViews();String q=search==null?"":search.getText().toString();int visible=0;int columns=LauncherPrefs.columns(this);grid.setColumnCount(columns);boolean labels=LauncherPrefs.labels(this);boolean compact=LauncherPrefs.compact(this);
        for(LauncherCatalog.App a:apps){if(!LauncherCatalog.matches(a,q,category))continue;visible++;
            LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);tile.setPadding(dp(2),dp(5),dp(2),dp(3));
            FrameLayout box=new FrameLayout(this);ImageView iv=new ImageView(this);try{iv.setImageDrawable(AiriIconPack.drawable(this,pm.getApplicationIcon(a.pkg),dp(compact?54:62)));}catch(Exception e){iv.setImageResource(android.R.drawable.sym_def_app_icon);}iv.setPadding(dp(2),dp(2),dp(2),dp(2));box.addView(iv,new FrameLayout.LayoutParams(-1,-1));
            if(LauncherPrefs.isFavorite(this,a.pkg)){TextView star=t("★",12,true);star.setTextColor(Color.rgb(74,112,246));star.setGravity(Gravity.CENTER);star.setBackground(round(Color.WHITE,dp(10)));FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(dp(22),dp(22),Gravity.TOP|Gravity.RIGHT);box.addView(star,sp);}tile.addView(box,new LinearLayout.LayoutParams(dp(compact?54:62),dp(compact?54:62)));
            if(labels){TextView n=t(a.label,compact?8.5f:9.5f,true);n.setTextColor(Color.rgb(35,40,48));n.setGravity(Gravity.CENTER);n.setMaxLines(1);tile.addView(n,new LinearLayout.LayoutParams(-1,dp(26)));}
            tile.setOnClickListener(v->launch(a));tile.setOnLongClickListener(v->{menu(v,a);return true;});
            GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=dp(labels?(compact?82:94):(compact?64:74));p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(dp(2),dp(2),dp(2),dp(2));grid.addView(tile,p);
        }
        if(count!=null)count.setText(visible+" apps • "+columns+" columns • long-press for actions");
    }

    private void launch(LauncherCatalog.App a){Intent x=pm.getLaunchIntentForPackage(a.pkg);if(x!=null){AppLaunchStats.record(this,a.pkg);startActivity(x);}}
    private void menu(View anchor,LauncherCatalog.App a){PopupMenu m=new PopupMenu(this,anchor);boolean fav=LauncherPrefs.isFavorite(this,a.pkg);m.getMenu().add(fav?"Remove favorite":"Add favorite");m.getMenu().add("Hide app");m.getMenu().add("App info");m.setOnMenuItemClickListener(item->{String s=item.getTitle().toString();if(s.contains("favorite")){LauncherPrefs.favorite(this,a.pkg,!fav);load();return true;}if(s.equals("Hide app")){LauncherPrefs.hidden(this,a.pkg,true);Toast.makeText(this,a.label+" hidden",Toast.LENGTH_SHORT).show();load();return true;}if(s.equals("App info")){try{startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+a.pkg)));}catch(Exception ignored){}return true;}return false;});m.show();}

    private android.graphics.drawable.GradientDrawable round(int color,float radius){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(color);g.setCornerRadius(radius);g.setStroke(dp(1),Color.argb(65,255,255,255));return g;}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

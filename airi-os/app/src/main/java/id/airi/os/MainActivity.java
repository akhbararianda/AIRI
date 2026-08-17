package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private TextView clock, response, providerLabel;
    private EditText input;
    private AiProvider ai;
    private float downX,downY;
    private long downAt;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        ai=new LocalIrzuqniProvider(this);
        Window w=getWindow();w.setStatusBarColor(Color.TRANSPARENT);w.setNavigationBarColor(Color.BLACK);
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        build();tick();
    }
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
    @Override public void onBackPressed(){}

    @Override public boolean dispatchTouchEvent(MotionEvent e){
        if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();downAt=System.currentTimeMillis();}
        else if(e.getAction()==MotionEvent.ACTION_UP){float dx=e.getX()-downX,dy=e.getY()-downY;long dt=System.currentTimeMillis()-downAt;if(dt<700&&Math.abs(dy)>dp(120)&&Math.abs(dy)>Math.abs(dx)*1.2f){if(dy<0)open(new Intent(this,AppLibraryActivity.class));else open(new Intent(this,ControlCenterActivity.class));}}
        return super.dispatchTouchEvent(e);
    }

    private void build(){
        int white=Color.WHITE, muted=Color.rgb(173,181,190), ink=Color.rgb(16,18,22), cyan=Color.rgb(74,229,255), violet=Color.rgb(177,102,255);
        FrameLayout stage=new FrameLayout(this);stage.setBackgroundColor(Color.rgb(5,5,5));
        stage.addView(new ZeroBackdropView(this),new FrameLayout.LayoutParams(-1,-1));

        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(28),dp(18),dp(124));scroll.addView(root,new ScrollView.LayoutParams(-1,-2));stage.addView(scroll,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout brand=new LinearLayout(this);brand.setOrientation(LinearLayout.VERTICAL);TextView a=text("AIRI",24,white,true);a.setLetterSpacing(.16f);brand.addView(a);providerLabel=text(ai.name().toUpperCase(Locale.ROOT),8,Color.argb(185,255,255,255),true);providerLabel.setLetterSpacing(.08f);brand.addView(providerLabel);top.addView(brand,new LinearLayout.LayoutParams(0,dp(52),1));
        clock=text(now("HH:mm"),14,white,true);clock.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);top.addView(clock,new LinearLayout.LayoutParams(-2,dp(52)));root.addView(top);

        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(18),dp(18),dp(18),dp(18));hero.setBackground(AiriGlassDrawable.make(this,34,AiriGlassDrawable.DARK));hero.setElevation(dp(20));
        TextView orb=text("✦",44,cyan,true);orb.setGravity(Gravity.CENTER);hero.addView(orb,new LinearLayout.LayoutParams(-1,dp(64)));
        TextView title=text("Irzuqni Core",27,white,true);title.setGravity(Gravity.CENTER);hero.addView(title);
        TextView subtitle=text("AIRI dimulai dari percakapan, bukan dari ikon aplikasi.",12,muted,false);subtitle.setGravity(Gravity.CENTER);subtitle.setPadding(0,dp(5),0,dp(8));hero.addView(subtitle);
        response=text("Selamat datang. Saya siap membuka aplikasi, membantu pengaturan, mencari, dan menjadi pusat kendali AIRI OS.",14,white,false);response.setLineSpacing(dp(3),1f);response.setPadding(dp(14),dp(14),dp(14),dp(14));response.setBackground(AiriGlassDrawable.make(this,28,AiriGlassDrawable.CLEAR));hero.addView(response,new LinearLayout.LayoutParams(-1,-2));root.addView(hero,lp(-1,-2,0,8,0,14));

        TextView contextTitle=text("CONTEXT",9,Color.argb(190,255,255,255),true);contextTitle.setLetterSpacing(.14f);root.addView(contextTitle,lp(-1,-2,2,0,0,7));
        LinearLayout context=new LinearLayout(this);
        context.addView(contextCard("DEVICE","Android 11","Ready",cyan,ink),new LinearLayout.LayoutParams(0,dp(92),1));
        LinearLayout.LayoutParams gap=new LinearLayout.LayoutParams(0,dp(92),1);gap.setMargins(dp(9),0,0,0);context.addView(contextCard("AIRI","AI Core","Local bridge",violet,ink),gap);root.addView(context,lp(-1,-2,0,0,0,14));

        TextView sugTitle=text("SUGGESTED",9,Color.argb(190,255,255,255),true);sugTitle.setLetterSpacing(.14f);root.addView(sugTitle,lp(-1,-2,2,0,0,7));
        LinearLayout suggested=new LinearLayout(this);suggested.setGravity(Gravity.CENTER);
        action(suggested,"Camera",()->sendPrompt("buka kamera"));action(suggested,"Internet",()->sendPrompt("buka internet"));action(suggested,"Screen AI",()->open(new Intent(this,ScreenIntelligenceActivity.class)));action(suggested,"Gallery AI",()->open(new Intent(this,GalleryLabActivity.class)));root.addView(suggested,lp(-1,dp(78),0,0,0,12));

        LinearLayout intelligence=new LinearLayout(this);intelligence.setOrientation(LinearLayout.VERTICAL);intelligence.setPadding(dp(16),dp(14),dp(16),dp(14));intelligence.setBackground(AiriGlassDrawable.make(this,30,AiriGlassDrawable.REGULAR));intelligence.setElevation(dp(10));
        intelligence.addView(text("AIRI Intelligence Stack",15,ink,true));TextView desc=text("Circle Search  •  Smart Text  •  Smart Eraser  •  Screen Intelligence  •  Notification Center",11,AiriTheme.muted(this),false);desc.setPadding(0,dp(5),0,0);intelligence.addView(desc);intelligence.setOnClickListener(v->open(new Intent(this,IntelligenceHubActivity.class)));root.addView(intelligence,lp(-1,dp(86),0,0,0,12));

        TextView gesture=text("↑ swipe untuk App Drawer     ↓ swipe untuk Control Center",9,Color.argb(155,255,255,255),false);gesture.setGravity(Gravity.CENTER);root.addView(gesture,new LinearLayout.LayoutParams(-1,dp(32)));

        LinearLayout dock=new LinearLayout(this);dock.setGravity(Gravity.CENTER_VERTICAL);dock.setPadding(dp(10),dp(8),dp(8),dp(8));dock.setBackground(AiriGlassDrawable.make(this,34,AiriGlassDrawable.DARK));dock.setElevation(dp(28));
        TextView spark=text("✦",23,cyan,true);spark.setGravity(Gravity.CENTER);dock.addView(spark,new LinearLayout.LayoutParams(dp(48),dp(52)));
        input=new EditText(this);input.setSingleLine(true);input.setTextColor(white);input.setHintTextColor(Color.rgb(132,142,151));input.setHint("Ask Irzuqni...");input.setTextSize(14);input.setBackgroundColor(Color.TRANSPARENT);input.setImeOptions(EditorInfo.IME_ACTION_SEND);input.setOnEditorActionListener((v,id,event)->{if(id==EditorInfo.IME_ACTION_SEND||(event!=null&&event.getKeyCode()==KeyEvent.KEYCODE_ENTER)){submit();return true;}return false;});dock.addView(input,new LinearLayout.LayoutParams(0,dp(52),1));
        TextView mic=text("●",18,violet,true);mic.setGravity(Gravity.CENTER);mic.setOnClickListener(v->open(new Intent(this,AssistantActivity.class)));dock.addView(mic,new LinearLayout.LayoutParams(dp(44),dp(52)));
        TextView send=text("↑",22,white,true);send.setGravity(Gravity.CENTER);send.setBackground(AiriGlassDrawable.make(this,24,AiriGlassDrawable.BLUE));send.setOnClickListener(v->submit());dock.addView(send,new LinearLayout.LayoutParams(dp(48),dp(48)));
        FrameLayout.LayoutParams dp=new FrameLayout.LayoutParams(-1,dp(76),Gravity.BOTTOM);dp.setMargins(this.dp(14),0,this.dp(14),this.dp(14));stage.addView(dock,dp);
        setContentView(stage);
    }

    private LinearLayout contextCard(String cap,String big,String small,int accent,int ink){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(14),dp(10),dp(14),dp(10));c.setBackground(AiriGlassDrawable.make(this,28,AiriGlassDrawable.REGULAR));TextView x=text(cap,8,AiriTheme.muted(this),true);x.setLetterSpacing(.1f);c.addView(x);c.addView(text(big,17,ink,true));c.addView(text(small,10,accent,true));return c;}
    private void action(LinearLayout row,String label,Runnable run){TextView v=text(label,10,Color.WHITE,true);v.setGravity(Gravity.CENTER);v.setBackground(AiriGlassDrawable.make(this,24,AiriGlassDrawable.CLEAR));v.setOnClickListener(x->run.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(62),1);p.setMargins(dp(3),dp(4),dp(3),dp(4));row.addView(v,p);}
    private void submit(){if(input==null)return;String q=input.getText().toString().trim();if(q.isEmpty())return;input.setText("");sendPrompt(q);}
    private void sendPrompt(String q){response.setText("Irzuqni sedang memproses…");response.animate().alpha(.45f).setDuration(120).withEndAction(()->response.animate().alpha(1).setDuration(220).start()).start();ai.query(q,new AiProvider.Callback(){public void onResult(String r){runOnUiThread(()->response.setText(r));}public void onError(String m){runOnUiThread(()->response.setText(m));}});}
    private void open(Intent i){try{startActivity(i);overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);}catch(Exception ignored){}}
    private void tick(){handler.post(new Runnable(){public void run(){if(clock!=null)clock.setText(now("HH:mm"));handler.postDelayed(this,30000);}});}
    private String now(String f){return new SimpleDateFormat(f,Locale.getDefault()).format(new Date());}
    private TextView text(String s,float z,int c,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

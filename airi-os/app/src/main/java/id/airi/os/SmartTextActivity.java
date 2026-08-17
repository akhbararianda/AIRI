package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SmartTextActivity extends Activity {
    private EditText input; private TextView output;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();handleShare(getIntent());}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);handleShare(i);}
    private void build(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(18),dp(24),dp(18),dp(18));r.setBackgroundColor(Color.rgb(247,245,239));r.addView(t("AIRI Smart Text",28,true));TextView s=t("Ringkas, rapikan, dan siapkan teks untuk diterjemahkan.",12,false);s.setTextColor(Color.DKGRAY);r.addView(s,lp(-1,-2,0,2,0,14));input=new EditText(this);input.setMinLines(6);input.setGravity(Gravity.TOP);input.setHint("Tempel atau bagikan teks ke AIRI OS…");r.addView(input,new LinearLayout.LayoutParams(-1,dp(180)));LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);TextView sum=btn("Ringkas");sum.setOnClickListener(v->summarize());row.addView(sum,new LinearLayout.LayoutParams(0,dp(48),1));TextView clean=btn("Rapikan");clean.setOnClickListener(v->clean());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);p.setMargins(dp(8),0,0,0);row.addView(clean,p);r.addView(row,lp(-1,-2,0,10,0,10));TextView trans=btn("Terjemahkan dengan aplikasi tersedia");trans.setOnClickListener(v->translate());r.addView(trans,new LinearLayout.LayoutParams(-1,dp(48)));output=t("Hasil akan tampil di sini.",14,false);output.setTextIsSelectable(true);output.setPadding(0,dp(16),0,0);r.addView(output);setContentView(r);}
    private void handleShare(Intent i){if(i!=null&&Intent.ACTION_SEND.equals(i.getAction())&&"text/plain".equals(i.getType())){String x=i.getStringExtra(Intent.EXTRA_TEXT);if(x!=null&&input!=null)input.setText(x);}}
    private void summarize(){String s=input.getText().toString().trim();if(s.isEmpty())return;String[] parts=s.split("(?<=[.!?])\\s+");StringBuilder b=new StringBuilder();int n=Math.min(3,parts.length);for(int i=0;i<n;i++){if(parts[i].trim().isEmpty())continue;b.append("• ").append(parts[i].trim()).append("\n");}output.setText(b.length()>0?b.toString():s.substring(0,Math.min(220,s.length())));}
    private void clean(){String s=input.getText().toString().trim().replaceAll("[ \\t]+"," ").replaceAll("\\n{3,}","\\n\\n");input.setText(s);output.setText("Teks sudah dirapikan.");}
    private void translate(){String s=input.getText().toString().trim();if(s.isEmpty())return;Intent i=new Intent(Intent.ACTION_PROCESS_TEXT);i.setType("text/plain");i.putExtra(Intent.EXTRA_PROCESS_TEXT,s);try{startActivity(Intent.createChooser(i,"Terjemahkan teks"));}catch(Exception e){Intent share=new Intent(Intent.ACTION_SEND);share.setType("text/plain");share.putExtra(Intent.EXTRA_TEXT,s);startActivity(Intent.createChooser(share,"Kirim ke aplikasi penerjemah"));}}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(25,29,32));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private TextView btn(String s){TextView v=t(s,13,true);v.setTextColor(Color.WHITE);v.setGravity(Gravity.CENTER);android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(Color.rgb(48,86,117));d.setCornerRadius(dp(22));v.setBackground(d);return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int rr,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(rr),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

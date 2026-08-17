package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CircleSearchActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(22),dp(42),dp(22),dp(22));root.setBackgroundColor(Color.rgb(247,245,239));
        TextView title=t("AIRI Circle Search",30,true);root.addView(title);
        TextView sub=t("Cari apa pun dengan cepat. Untuk pencarian visual, AIRI membuka Lens/visual search yang tersedia di perangkat.",13,false);sub.setTextColor(Color.DKGRAY);root.addView(sub,lp(-1,-2,0,4,0,22));
        EditText q=new EditText(this);q.setHint("Ketik apa yang ingin dicari…");q.setSingleLine(true);q.setPadding(dp(16),0,dp(16),0);android.graphics.drawable.GradientDrawable gd=new android.graphics.drawable.GradientDrawable();gd.setColor(Color.WHITE);gd.setCornerRadius(dp(22));q.setBackground(gd);root.addView(q,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView search=button("Cari Sekarang");search.setOnClickListener(v->{String s=q.getText().toString().trim();if(!s.isEmpty()){Intent i=new Intent(Intent.ACTION_WEB_SEARCH);i.putExtra("query",s);try{startActivity(i);}catch(Exception e){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q="+Uri.encode(s))));}}});root.addView(search,lp(-1,dp(52),0,12,0,8));
        TextView lens=button("Visual Search / Lens");lens.setOnClickListener(v->{try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("googleapp://lens"));startActivity(i);}catch(Exception e){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://lens.google.com/")));}catch(Exception ignored){}}});root.addView(lens,lp(-1,dp(52),0,0,0,8));
        TextView note=t("Tip: dari aplikasi lain, ambil screenshot lalu buka Visual Search untuk mencari objek pada gambar. AIRI tidak membaca layar diam-diam; Android mewajibkan izin untuk screen capture.",12,false);note.setTextColor(Color.GRAY);root.addView(note,lp(-1,-2,0,14,0,0));
        setContentView(root);
    }
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(28,32,35));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}
    private TextView button(String s){TextView v=t(s,15,true);v.setTextColor(Color.WHITE);v.setGravity(Gravity.CENTER);android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(Color.rgb(48,86,117));g.setCornerRadius(dp(24));v.setBackground(g);return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w);int hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

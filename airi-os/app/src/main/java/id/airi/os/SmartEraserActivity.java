package id.airi.os;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.InputStream;

public class SmartEraserActivity extends Activity {
    private static final int PICK=41;
    private Bitmap bitmap;
    private ImageView image;
    @Override protected void onCreate(Bundle b){super.onCreate(b); build();}
    private void build(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(28),dp(18),dp(18)); root.setBackgroundColor(Color.rgb(247,245,239));
        TextView title=t("AIRI Smart Eraser",28,true); root.addView(title);
        TextView sub=t("Pilih foto, lalu sapu objek kecil yang ingin disamarkan. Pemrosesan dilakukan lokal di perangkat.",13,false); sub.setTextColor(Color.DKGRAY); root.addView(sub,lp(-1,-2,0,4,0,16));
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        TextView pick=button("Pilih Foto"); pick.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK);}); row.addView(pick,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView save=button("Simpan"); save.setOnClickListener(v->save()); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,dp(48),1);sp.setMargins(dp(8),0,0,0);row.addView(save,sp); root.addView(row);
        image=new ImageView(this); image.setAdjustViewBounds(true); image.setScaleType(ImageView.ScaleType.FIT_CENTER); image.setBackgroundColor(Color.rgb(230,230,226)); root.addView(image,lp(-1,0,0,14,0,0,1f));
        image.setOnTouchListener((v,e)->{ if(bitmap==null || (e.getAction()!=MotionEvent.ACTION_MOVE && e.getAction()!=MotionEvent.ACTION_DOWN)) return true; eraseAt(e.getX(),e.getY()); return true;});
        setContentView(root);
    }
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d); if(r==PICK&&c==RESULT_OK&&d!=null){try{Uri u=d.getData();InputStream in=getContentResolver().openInputStream(u);Bitmap src=BitmapFactory.decodeStream(in);if(src!=null){int max=1600;float scale=Math.min(1f,max/(float)Math.max(src.getWidth(),src.getHeight()));bitmap=Bitmap.createScaledBitmap(src,Math.max(1,(int)(src.getWidth()*scale)),Math.max(1,(int)(src.getHeight()*scale)),true).copy(Bitmap.Config.ARGB_8888,true);image.setImageBitmap(bitmap);}}catch(Exception ignored){}}}
    private void eraseAt(float vx,float vy){
        if(image.getDrawable()==null)return; float iw=image.getWidth(), ih=image.getHeight(); int bw=bitmap.getWidth(), bh=bitmap.getHeight(); float s=Math.min(iw/bw,ih/bh); float ox=(iw-bw*s)/2f, oy=(ih-bh*s)/2f; int x=(int)((vx-ox)/s), y=(int)((vy-oy)/s); if(x<0||y<0||x>=bw||y>=bh)return;
        int radius=Math.max(18,Math.min(bw,bh)/28); int sx=Math.max(0,x-radius*2), ex=Math.min(bw-1,x+radius*2), sy=Math.max(0,y-radius*2), ey=Math.min(bh-1,y+radius*2); long rr=0,gg=0,bb=0,n=0;
        for(int xx=sx;xx<=ex;xx+=4)for(int yy=sy;yy<=ey;yy+=4){double dist=Math.hypot(xx-x,yy-y);if(dist>radius*1.25&&dist<radius*2){int p=bitmap.getPixel(xx,yy);rr+=Color.red(p);gg+=Color.green(p);bb+=Color.blue(p);n++;}}
        if(n==0)return; Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(Color.rgb((int)(rr/n),(int)(gg/n),(int)(bb/n)));p.setStyle(Paint.Style.FILL);Canvas c=new Canvas(bitmap);c.drawCircle(x,y,radius,p);image.invalidate();
    }
    private void save(){if(bitmap==null)return; try{String u=MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"AIRI_Erased_"+System.currentTimeMillis(),"Edited with AIRI Smart Eraser"); android.widget.Toast.makeText(this,u==null?"Gagal menyimpan":"Tersimpan di galeri",android.widget.Toast.LENGTH_SHORT).show();}catch(Exception e){android.widget.Toast.makeText(this,"Gagal menyimpan",android.widget.Toast.LENGTH_SHORT).show();}}
    private TextView t(String s,float z,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(28,32,35));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}
    private TextView button(String s){TextView v=t(s,14,true);v.setTextColor(Color.WHITE);v.setGravity(Gravity.CENTER);android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(Color.rgb(48,86,117));g.setCornerRadius(dp(22));v.setBackground(g);return v;}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b){return lp(w,h,l,t,r,b,0);}
    private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int r,int b,float weight){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w);int hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh,weight);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

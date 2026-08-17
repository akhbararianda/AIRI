package id.airi.os;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ScreenIntelligenceActivity extends Activity {
    private static final int REQ=811;
    private MediaProjectionManager mgr; private TextView status;
    @Override protected void onCreate(Bundle b){super.onCreate(b);mgr=(MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);build();}
    private void build(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setGravity(Gravity.CENTER_HORIZONTAL);r.setPadding(dp(22),dp(34),dp(22),dp(28));r.setBackgroundColor(Color.rgb(219,235,247));r.addView(t("Screen Intelligence",30,true));TextView sub=t("Capture • inspect • prepare for AIRI analysis",13,false);sub.setTextColor(Color.rgb(78,98,114));r.addView(sub,lp(-1,-2,0,3,0,22));TextView orb=t("◎",68,true);orb.setGravity(Gravity.CENTER);orb.setTextColor(Color.rgb(45,111,177));orb.setBackground(AiriGlassDrawable.make(this,52,AiriGlassDrawable.CLEAR));orb.setElevation(dp(14));r.addView(orb,new LinearLayout.LayoutParams(dp(124),dp(124)));status=t("Android akan meminta izin sebelum AIRI melihat layar.",14,false);status.setGravity(Gravity.CENTER);r.addView(status,lp(-1,dp(80),0,18,0,12));TextView capture=button("Capture Screen");capture.setOnClickListener(v->startActivityForResult(mgr.createScreenCaptureIntent(),REQ));r.addView(capture,lp(-1,dp(52),0,0,0,10));TextView note=t("Hasil capture disimpan ke Pictures/AIRI. AIRI tidak dapat melewati dialog persetujuan screen capture Android.",12,false);note.setTextColor(Color.rgb(78,94,108));r.addView(note);setContentView(r);AiriLiquidSkin.apply(this);}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=REQ)return;if(result!=RESULT_OK||data==null){status.setText("Capture dibatalkan.");return;}status.setText("Capturing…");capture(result,data);}
    private void capture(int result,Intent data){MediaProjection p=mgr.getMediaProjection(result,data);WindowManager wm=(WindowManager)getSystemService(WINDOW_SERVICE);Display d=wm.getDefaultDisplay();android.util.DisplayMetrics m=new android.util.DisplayMetrics();d.getRealMetrics(m);int w=m.widthPixels,h=m.heightPixels,density=m.densityDpi;ImageReader reader=ImageReader.newInstance(w,h,android.graphics.PixelFormat.RGBA_8888,2);VirtualDisplay vd=p.createVirtualDisplay("AIRI Screen",w,h,density,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,null);Handler handler=new Handler(Looper.getMainLooper());reader.setOnImageAvailableListener(r->{Image image=null;try{image=r.acquireLatestImage();if(image==null)return;Image.Plane plane=image.getPlanes()[0];ByteBuffer buffer=plane.getBuffer();int pixelStride=plane.getPixelStride(),rowStride=plane.getRowStride(),padding=rowStride-pixelStride*w;Bitmap wide=Bitmap.createBitmap(w+padding/pixelStride,h,Bitmap.Config.ARGB_8888);wide.copyPixelsFromBuffer(buffer);Bitmap bmp=Bitmap.createBitmap(wide,0,0,w,h);wide.recycle();save(bmp);bmp.recycle();status.setText("Saved to Pictures/AIRI ✓");}catch(Exception e){status.setText("Capture gagal: "+e.getClass().getSimpleName());}finally{if(image!=null)image.close();try{vd.release();reader.close();p.stop();}catch(Exception ignored){}}},handler);}
    private void save(Bitmap b)throws Exception{ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"AIRI-Screen-"+System.currentTimeMillis()+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");if(android.os.Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/AIRI");Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new Exception("MediaStore");try(OutputStream o=getContentResolver().openOutputStream(uri)){b.compress(Bitmap.CompressFormat.PNG,100,o);}}
    private TextView button(String x){TextView v=t(x,14,true);v.setGravity(Gravity.CENTER);v.setTextColor(Color.WHITE);v.setBackground(AiriGlassDrawable.make(this,27,AiriGlassDrawable.BLUE));v.setElevation(dp(10));return v;}private TextView t(String x,float z,boolean b){TextView v=new TextView(this);v.setText(x);v.setTextSize(z);v.setTextColor(Color.rgb(18,28,38));v.setTypeface(android.graphics.Typeface.create("sans",b?1:0));return v;}private LinearLayout.LayoutParams lp(int w,int h,int l,int t,int rr,int b){int ww=w==-1?ViewGroup.LayoutParams.MATCH_PARENT:(w==-2?ViewGroup.LayoutParams.WRAP_CONTENT:w),hh=h==-1?ViewGroup.LayoutParams.MATCH_PARENT:(h==-2?ViewGroup.LayoutParams.WRAP_CONTENT:h);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ww,hh);p.setMargins(dp(l),dp(t),dp(rr),dp(b));return p;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}

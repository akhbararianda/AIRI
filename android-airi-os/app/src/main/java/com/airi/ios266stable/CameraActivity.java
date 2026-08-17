package com.airi.ios266stable;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements SurfaceHolder.Callback {
    private static final int REQ = 41;
    private SurfaceView surface; private Camera camera; private int cameraId=Camera.CameraInfo.CAMERA_FACING_BACK; private boolean flash=false; private TextView flashBtn; private GridOverlay grid;

    @Override protected void onCreate(Bundle b){super.onCreate(b);configureWindow();buildUi();requestNeeded();}
    private void configureWindow(){Window w=getWindow();w.setStatusBarColor(Color.BLACK);w.setNavigationBarColor(Color.BLACK);if(Build.VERSION.SDK_INT>=28){WindowManager.LayoutParams lp=w.getAttributes();lp.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;w.setAttributes(lp);}w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    private void requestNeeded(){if(Build.VERSION.SDK_INT>=23){java.util.ArrayList<String> p=new java.util.ArrayList<>();if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.CAMERA);if(Build.VERSION.SDK_INT<=28&&checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);if(!p.isEmpty())requestPermissions(p.toArray(new String[0]),REQ);}}
    private boolean granted(){return Build.VERSION.SDK_INT<23||checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED;}
    private TextView action(String s){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(13);v.setGravity(Gravity.CENTER);v.setBackgroundColor(0x33262626);v.setPadding(14,8,14,8);return v;}

    private void buildUi(){
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);
        DisplayMetrics dm=new DisplayMetrics();getWindowManager().getDefaultDisplay().getRealMetrics(dm);int w=dm.widthPixels;int previewW=w;int previewH=Math.round(w*4f/3f);int topH=(int)(72*dm.density);int bottomH=(int)(178*dm.density);int available=dm.heightPixels-topH-bottomH;if(previewH>available){previewH=available;previewW=Math.round(previewH*3f/4f);} 
        FrameLayout previewBox=new FrameLayout(this);previewBox.setBackgroundColor(0xFF050505);surface=new SurfaceView(this);surface.getHolder().addCallback(this);previewBox.addView(surface,new FrameLayout.LayoutParams(-1,-1));grid=new GridOverlay();previewBox.addView(grid,new FrameLayout.LayoutParams(-1,-1));FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(previewW,previewH,Gravity.TOP|Gravity.CENTER_HORIZONTAL);pp.topMargin=topH;root.addView(previewBox,pp);

        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(12,8,12,8);TextView back=action("‹");back.setTextSize(28);back.setOnClickListener(v->finish());flashBtn=action("Flash Off");flashBtn.setOnClickListener(v->toggleFlash());TextView ratio=action("4:3");ratio.setOnClickListener(v->Toast.makeText(this,"Viewfinder 4:3 • kualitas foto maksimum",Toast.LENGTH_SHORT).show());top.addView(back,new LinearLayout.LayoutParams(0,-1,1));top.addView(flashBtn,new LinearLayout.LayoutParams(0,-1,1));top.addView(ratio,new LinearLayout.LayoutParams(0,-1,1));root.addView(top,new FrameLayout.LayoutParams(-1,topH,Gravity.TOP));

        LinearLayout controls=new LinearLayout(this);controls.setOrientation(LinearLayout.VERTICAL);controls.setGravity(Gravity.CENTER);controls.setBackgroundColor(Color.BLACK);TextView mode=new TextView(this);mode.setText("AIRI NATURAL   •   PHOTO");mode.setTextColor(0xFFFFD95A);mode.setTextSize(12);mode.setGravity(Gravity.CENTER);controls.addView(mode,new LinearLayout.LayoutParams(-1,(int)(42*dm.density)));
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);TextView gridBtn=action("Grid");gridBtn.setOnClickListener(v->grid.setVisibility(grid.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE));TextView shutter=new TextView(this);shutter.setText("●");shutter.setTextColor(Color.WHITE);shutter.setTextSize(64);shutter.setGravity(Gravity.CENTER);shutter.setOnClickListener(v->capture());TextView flip=action("↻");flip.setTextSize(27);flip.setOnClickListener(v->switchCamera());row.addView(gridBtn,new LinearLayout.LayoutParams(0,-1,1));row.addView(shutter,new LinearLayout.LayoutParams((int)(112*dm.density),-1));row.addView(flip,new LinearLayout.LayoutParams(0,-1,1));controls.addView(row,new LinearLayout.LayoutParams(-1,0,1));FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,bottomH,Gravity.BOTTOM);root.addView(controls,cp);setContentView(root);
    }

    private void openCamera(){releaseCamera();if(!granted())return;try{camera=Camera.open(cameraId);Camera.Parameters p=camera.getParameters();List<String> focus=p.getSupportedFocusModes();if(focus!=null&&focus.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);Camera.Size best=null;for(Camera.Size s:p.getSupportedPictureSizes())if(best==null||s.width*s.height>best.width*best.height)best=s;if(best!=null)p.setPictureSize(best.width,best.height);p.setJpegQuality(96);p.setRotation(cameraId==Camera.CameraInfo.CAMERA_FACING_FRONT?270:90);camera.setParameters(p);camera.setDisplayOrientation(90);camera.setPreviewDisplay(surface.getHolder());camera.startPreview();applyFlash();}catch(Exception e){Toast.makeText(this,"Kamera tidak dapat dibuka",Toast.LENGTH_SHORT).show();}}
    private void toggleFlash(){flash=!flash;applyFlash();flashBtn.setText(flash?"Flash On":"Flash Off");}
    private void applyFlash(){if(camera==null)return;try{Camera.Parameters p=camera.getParameters();List<String> m=p.getSupportedFlashModes();if(m!=null){String want=flash?Camera.Parameters.FLASH_MODE_ON:Camera.Parameters.FLASH_MODE_OFF;if(m.contains(want)){p.setFlashMode(want);camera.setParameters(p);}}}catch(Exception ignored){}}
    private void switchCamera(){cameraId=cameraId==Camera.CameraInfo.CAMERA_FACING_BACK?Camera.CameraInfo.CAMERA_FACING_FRONT:Camera.CameraInfo.CAMERA_FACING_BACK;flash=false;flashBtn.setText("Flash Off");openCamera();}
    private void capture(){if(camera==null)return;try{camera.takePicture(null,null,(data,c)->{saveStyled(data);try{camera.startPreview();}catch(Exception ignored){}});}catch(Exception e){Toast.makeText(this,"Gagal mengambil foto",Toast.LENGTH_SHORT).show();}}

    private void saveStyled(byte[] jpeg){new Thread(()->{try{Bitmap src=BitmapFactory.decodeByteArray(jpeg,0,jpeg.length);Bitmap out=Bitmap.createBitmap(src.getWidth(),src.getHeight(),Bitmap.Config.ARGB_8888);Canvas cv=new Canvas(out);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);ColorMatrix sat=new ColorMatrix();sat.setSaturation(1.045f);ColorMatrix tone=new ColorMatrix(new float[]{1.035f,0,0,0,2,0,1.012f,0,0,1,0,0,0.99f,0,-1,0,0,0,1,0});sat.postConcat(tone);paint.setColorFilter(new ColorMatrixColorFilter(sat));cv.drawBitmap(src,0,0,paint);String name="AIRI_"+new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date())+".jpg";OutputStream os;if(Build.VERSION.SDK_INT>=29){ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,name);v.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");v.put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_DCIM+"/AIRI Camera");Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new Exception("insert");os=getContentResolver().openOutputStream(uri);}else{File dir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"AIRI Camera");if(!dir.exists())dir.mkdirs();File f=new File(dir,name);os=new FileOutputStream(f);sendBroadcast(new android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(f)));}out.compress(Bitmap.CompressFormat.JPEG,95,os);if(os!=null)os.close();src.recycle();out.recycle();runOnUiThread(()->Toast.makeText(this,"Foto tersimpan • AIRI Natural",Toast.LENGTH_SHORT).show());}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Gagal menyimpan foto",Toast.LENGTH_SHORT).show());}}).start();}

    @Override public void surfaceCreated(SurfaceHolder h){if(granted())openCamera();}@Override public void surfaceChanged(SurfaceHolder h,int f,int w,int he){if(camera!=null){try{camera.stopPreview();}catch(Exception ignored){}openCamera();}}@Override public void surfaceDestroyed(SurfaceHolder h){releaseCamera();}@Override protected void onPause(){releaseCamera();super.onPause();}@Override protected void onResume(){super.onResume();configureWindow();if(surface!=null&&surface.getHolder().getSurface()!=null&&surface.getHolder().getSurface().isValid()&&granted())openCamera();}@Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==REQ){if(granted())openCamera();else finish();}}
    private void releaseCamera(){if(camera!=null){try{camera.stopPreview();}catch(Exception ignored){}try{camera.release();}catch(Exception ignored){}camera=null;}}
    private class GridOverlay extends View{Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);GridOverlay(){super(CameraActivity.this);p.setColor(0x55FFFFFF);p.setStrokeWidth(1f);setBackgroundColor(Color.TRANSPARENT);}@Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();c.drawLine(w/3,0,w/3,h,p);c.drawLine(2*w/3,0,2*w/3,h,p);c.drawLine(0,h/3,w,h/3,p);c.drawLine(0,2*h/3,w,2*h/3,p);}}
}

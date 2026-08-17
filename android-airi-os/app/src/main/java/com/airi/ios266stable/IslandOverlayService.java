package com.airi.ios266stable;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.TextView;

public class IslandOverlayService extends Service {
    public static final String ACTION_TEXT="com.airi.ISLAND_TEXT";
    public static final String EXTRA_TEXT="text";
    private WindowManager wm;
    private TextView island;
    private WindowManager.LayoutParams lp;
    private float downX,downY; private int startX,startY;

    @Override public void onCreate(){super.onCreate();createChannel();startForeground(2666,notification());if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){stopSelf();return;}showIsland();}
    @Override public int onStartCommand(Intent i,int flags,int id){if(i!=null&&ACTION_TEXT.equals(i.getAction())&&island!=null){String t=i.getStringExtra(EXTRA_TEXT);if(t!=null&&!t.trim().isEmpty()){island.setText(t);island.postDelayed(()->island.setText("AIRI"),4200);}}return START_STICKY;}
    private void showIsland(){wm=(WindowManager)getSystemService(WINDOW_SERVICE);island=new TextView(this);island.setText("AIRI");island.setTextColor(Color.WHITE);island.setTextSize(12);island.setGravity(Gravity.CENTER);island.setPadding(dp(18),0,dp(18),0);GradientDrawable g=new GradientDrawable();g.setColor(Color.BLACK);g.setCornerRadius(dp(24));island.setBackground(g);int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;lp=new WindowManager.LayoutParams(dp(138),dp(40),type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);lp.gravity=Gravity.TOP|Gravity.START;SharedPreferences p=getSharedPreferences("airi6",MODE_PRIVATE);lp.x=p.getInt("overlay_x",(getResources().getDisplayMetrics().widthPixels-dp(138))/2);lp.y=p.getInt("overlay_y",dp(10));island.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getRawX();downY=e.getRawY();startX=lp.x;startY=lp.y;return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){lp.x=(int)(startX+e.getRawX()-downX);lp.y=(int)(startY+e.getRawY()-downY);wm.updateViewLayout(island,lp);return true;}if(e.getAction()==MotionEvent.ACTION_UP){p.edit().putInt("overlay_x",lp.x).putInt("overlay_y",lp.y).apply();return true;}return false;});wm.addView(island,lp);}
    private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}    
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel("airi_island","AIRI Live Island",NotificationManager.IMPORTANCE_MIN);c.setShowBadge(false);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}}
    private Notification notification(){Intent i=new Intent(this,MainActivity.class);PendingIntent pi=PendingIntent.getActivity(this,0,i,Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0);Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"airi_island"):new Notification.Builder(this);return b.setContentTitle("AIRI Live Island aktif").setContentText("Overlay sistem AIRI berjalan").setSmallIcon(android.R.drawable.presence_online).setContentIntent(pi).setOngoing(true).build();}
    @Override public void onDestroy(){if(wm!=null&&island!=null)try{wm.removeView(island);}catch(Exception ignored){}super.onDestroy();}
    @Override public android.os.IBinder onBind(Intent i){return null;}
}

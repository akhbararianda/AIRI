package com.airi.ios266stable;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class AiriNotificationService extends NotificationListenerService {
    @Override public void onNotificationPosted(StatusBarNotification sbn){try{CharSequence title=sbn.getNotification().extras.getCharSequence("android.title");CharSequence text=sbn.getNotification().extras.getCharSequence("android.text");String out=(title==null?"":title.toString())+(text==null?"":" • "+text.toString());if(out.trim().isEmpty())return;Intent i=new Intent(this,IslandOverlayService.class);i.setAction(IslandOverlayService.ACTION_TEXT);i.putExtra(IslandOverlayService.EXTRA_TEXT,out.length()>48?out.substring(0,48)+"…":out);if(android.os.Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);}catch(Exception ignored){}}
}

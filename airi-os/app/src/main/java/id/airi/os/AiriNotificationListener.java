package id.airi.os;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AiriNotificationListener extends NotificationListenerService {
    public static final List<String> RECENT=new ArrayList<>();
    private static final Map<String,Integer> COUNTS=new HashMap<>();
    private static final Set<String> ACTIVE_KEYS=new HashSet<>();

    @Override public void onListenerConnected(){super.onListenerConnected();try{StatusBarNotification[] all=getActiveNotifications();synchronized(COUNTS){COUNTS.clear();ACTIVE_KEYS.clear();if(all!=null)for(StatusBarNotification s:all)addKey(s.getKey(),s.getPackageName());}}catch(Exception ignored){}}
    @Override public void onNotificationPosted(StatusBarNotification sbn){try{Notification n=sbn.getNotification();CharSequence title=n.extras.getCharSequence(Notification.EXTRA_TITLE);CharSequence text=n.extras.getCharSequence(Notification.EXTRA_TEXT);String row=(title==null?sbn.getPackageName():title.toString())+(text==null?"":"\n"+text);synchronized(RECENT){RECENT.add(0,row);while(RECENT.size()>30)RECENT.remove(RECENT.size()-1);}synchronized(COUNTS){addKey(sbn.getKey(),sbn.getPackageName());}}catch(Exception ignored){}}
    @Override public void onNotificationRemoved(StatusBarNotification sbn){try{synchronized(COUNTS){String key=sbn.getKey();if(ACTIVE_KEYS.remove(key)){String pkg=sbn.getPackageName();int next=Math.max(0,COUNTS.containsKey(pkg)?COUNTS.get(pkg)-1:0);if(next==0)COUNTS.remove(pkg);else COUNTS.put(pkg,next);}}}catch(Exception ignored){}}
    private static void addKey(String key,String pkg){if(key==null||pkg==null||ACTIVE_KEYS.contains(key))return;ACTIVE_KEYS.add(key);COUNTS.put(pkg,(COUNTS.containsKey(pkg)?COUNTS.get(pkg):0)+1);}
    public static int count(String pkg){synchronized(COUNTS){return COUNTS.containsKey(pkg)?COUNTS.get(pkg):0;}}
    public static Map<String,Integer> snapshot(){synchronized(COUNTS){return new HashMap<>(COUNTS);}}
}

package id.airi.os;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;
import java.util.List;

public class AiriNotificationListener extends NotificationListenerService {
    public static final List<String> RECENT=new ArrayList<>();
    @Override public void onNotificationPosted(StatusBarNotification sbn){try{Notification n=sbn.getNotification();CharSequence title=n.extras.getCharSequence(Notification.EXTRA_TITLE);CharSequence text=n.extras.getCharSequence(Notification.EXTRA_TEXT);String row=(title==null?sbn.getPackageName():title.toString())+(text==null?"":"\n"+text);synchronized(RECENT){RECENT.add(0,row);while(RECENT.size()>30)RECENT.remove(RECENT.size()-1);}}catch(Exception ignored){}}
}

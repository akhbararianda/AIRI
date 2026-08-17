package com.airi.ios266stable;

import android.content.*;
import android.os.Build;
import android.provider.Settings;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){if(i==null||!Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()))return;boolean on=c.getSharedPreferences("airi7_owner",Context.MODE_PRIVATE).getBoolean("overlay",false);if(!on)return;if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(c))return;Intent s=new Intent(c,IslandOverlayService.class);if(Build.VERSION.SDK_INT>=26)c.startForegroundService(s);else c.startService(s);}
}

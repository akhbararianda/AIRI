package id.airi.os;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class AppLaunchStats {
    private static final String PREF="airi_predictive_launches";
    private AppLaunchStats(){}
    private static SharedPreferences p(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE);}
    public static void record(Context c,String pkg){if(pkg==null)return;SharedPreferences sp=p(c);sp.edit().putInt(pkg,sp.getInt(pkg,0)+1).putLong(pkg+"#last",System.currentTimeMillis()).apply();}
    public static List<String> top(Context c,int max){Map<String,?> all=p(c).getAll();List<String> pkgs=new ArrayList<>();for(String k:all.keySet())if(!k.endsWith("#last"))pkgs.add(k);Collections.sort(pkgs,new Comparator<String>(){public int compare(String a,String b){SharedPreferences sp=p(c);int ca=sp.getInt(a,0),cb=sp.getInt(b,0);if(ca!=cb)return Integer.compare(cb,ca);return Long.compare(sp.getLong(b+"#last",0),sp.getLong(a+"#last",0));}});if(pkgs.size()>max)return new ArrayList<>(pkgs.subList(0,max));return pkgs;}
}

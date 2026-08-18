package id.airi.os;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LauncherPrefs {
    private static final String PREF="airi_fusion_launcher";
    private static final String FAVORITES="favorites";
    private static final String HIDDEN="hidden";
    private static final String DOCK="dock_v20";
    private LauncherPrefs(){}
    private static SharedPreferences p(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE);}

    public static boolean suggestions(Context c){return p(c).getBoolean("suggestions",true);} public static void suggestions(Context c,boolean v){p(c).edit().putBoolean("suggestions",v).apply();}
    public static boolean categories(Context c){return p(c).getBoolean("categories",true);} public static void categories(Context c,boolean v){p(c).edit().putBoolean("categories",v).apply();}
    public static boolean labels(Context c){return p(c).getBoolean("labels",true);} public static void labels(Context c,boolean v){p(c).edit().putBoolean("labels",v).apply();}
    public static boolean compact(Context c){return p(c).getBoolean("compact",false);} public static void compact(Context c,boolean v){p(c).edit().putBoolean("compact",v).apply();}
    public static boolean badges(Context c){return p(c).getBoolean("badges",true);} public static void badges(Context c,boolean v){p(c).edit().putBoolean("badges",v).apply();}
    public static boolean smartDock(Context c){return p(c).getBoolean("smart_dock",true);} public static void smartDock(Context c,boolean v){p(c).edit().putBoolean("smart_dock",v).apply();}
    public static int columns(Context c){return Math.max(4,Math.min(5,p(c).getInt("columns",4)));} public static void columns(Context c,int v){p(c).edit().putInt("columns",Math.max(4,Math.min(5,v))).apply();}

    public static Set<String> favorites(Context c){return Collections.unmodifiableSet(new HashSet<>(p(c).getStringSet(FAVORITES,Collections.emptySet())));}    
    public static Set<String> hidden(Context c){return Collections.unmodifiableSet(new HashSet<>(p(c).getStringSet(HIDDEN,Collections.emptySet())));}    
    public static boolean isFavorite(Context c,String pkg){return favorites(c).contains(pkg);} public static boolean isHidden(Context c,String pkg){return hidden(c).contains(pkg);}
    public static void favorite(Context c,String pkg,boolean on){Set<String>s=new HashSet<>(favorites(c));if(on)s.add(pkg);else s.remove(pkg);p(c).edit().putStringSet(FAVORITES,s).apply();}
    public static void hidden(Context c,String pkg,boolean on){Set<String>s=new HashSet<>(hidden(c));if(on)s.add(pkg);else s.remove(pkg);p(c).edit().putStringSet(HIDDEN,s).apply();}
    public static void clearHidden(Context c){p(c).edit().remove(HIDDEN).apply();}

    public static List<String> dock(Context c){String raw=p(c).getString(DOCK,"");List<String> out=new ArrayList<>();if(raw!=null&&!raw.isEmpty()){String[] parts=raw.split("\\|",-1);for(int i=0;i<5;i++)out.add(i<parts.length?parts[i]:"");}while(out.size()<5)out.add("");return out;}
    public static void setDockSlot(Context c,int slot,String pkg){List<String>d=new ArrayList<>(dock(c));if(slot<0||slot>4)return;d.set(slot,pkg==null?"":pkg);saveDock(c,d);}
    public static void saveDock(Context c,List<String>d){StringBuilder b=new StringBuilder();for(int i=0;i<5;i++){if(i>0)b.append('|');String s=i<d.size()?d.get(i):"";if(s!=null)b.append(s.replace("|",""));}p(c).edit().putString(DOCK,b.toString()).apply();}
    public static void clearDock(Context c){p(c).edit().remove(DOCK).apply();}

    public static String exportBackup(Context c){try{JSONObject j=new JSONObject();j.put("version",20);j.put("suggestions",suggestions(c));j.put("categories",categories(c));j.put("labels",labels(c));j.put("compact",compact(c));j.put("badges",badges(c));j.put("smartDock",smartDock(c));j.put("columns",columns(c));j.put("favorites",new JSONArray(favorites(c)));j.put("hidden",new JSONArray(hidden(c)));j.put("dock",new JSONArray(dock(c)));return j.toString();}catch(Exception e){return "";}}
    public static boolean importBackup(Context c,String raw){try{JSONObject j=new JSONObject(raw);SharedPreferences.Editor e=p(c).edit();e.putBoolean("suggestions",j.optBoolean("suggestions",true));e.putBoolean("categories",j.optBoolean("categories",true));e.putBoolean("labels",j.optBoolean("labels",true));e.putBoolean("compact",j.optBoolean("compact",false));e.putBoolean("badges",j.optBoolean("badges",true));e.putBoolean("smart_dock",j.optBoolean("smartDock",true));e.putInt("columns",Math.max(4,Math.min(5,j.optInt("columns",4))));Set<String>fav=new HashSet<>(),hid=new HashSet<>();JSONArray f=j.optJSONArray("favorites"),h=j.optJSONArray("hidden"),d=j.optJSONArray("dock");if(f!=null)for(int i=0;i<f.length();i++)fav.add(f.optString(i));if(h!=null)for(int i=0;i<h.length();i++)hid.add(h.optString(i));e.putStringSet(FAVORITES,fav);e.putStringSet(HIDDEN,hid);List<String> dock=new ArrayList<>();if(d!=null)for(int i=0;i<Math.min(5,d.length());i++)dock.add(d.optString(i));while(dock.size()<5)dock.add("");StringBuilder b=new StringBuilder();for(int i=0;i<5;i++){if(i>0)b.append('|');b.append(dock.get(i).replace("|",""));}e.putString(DOCK,b.toString());e.apply();return true;}catch(Exception ex){return false;}}
    public static void reset(Context c){p(c).edit().clear().apply();}
}

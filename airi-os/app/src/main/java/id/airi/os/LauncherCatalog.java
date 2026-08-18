package id.airi.os;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class LauncherCatalog {
    public static final class App {
        public final String label, pkg, category;
        App(String label, String pkg, String category) { this.label=label; this.pkg=pkg; this.category=category; }
    }
    private LauncherCatalog() {}

    public static List<App> load(Context c, boolean includeHidden) {
        PackageManager pm=c.getPackageManager();
        Intent i=new Intent(Intent.ACTION_MAIN,null); i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<App> out=new ArrayList<>();
        for(ResolveInfo r:pm.queryIntentActivities(i,0)) {
            if(r.activityInfo==null || c.getPackageName().equals(r.activityInfo.packageName)) continue;
            String pkg=r.activityInfo.packageName;
            if(!includeHidden && LauncherPrefs.isHidden(c,pkg)) continue;
            CharSequence l=r.loadLabel(pm);
            out.add(new App(l==null?pkg:l.toString(),pkg,category(pm,pkg)));
        }
        Collections.sort(out,new Comparator<App>() {
            public int compare(App a,App b) {
                boolean af=LauncherPrefs.isFavorite(c,a.pkg), bf=LauncherPrefs.isFavorite(c,b.pkg);
                if(af!=bf) return af?-1:1;
                return a.label.compareToIgnoreCase(b.label);
            }
        });
        return out;
    }

    public static boolean matches(App a,String q,String cat) {
        String needle=q==null?"":q.trim().toLowerCase(Locale.ROOT);
        boolean text=needle.isEmpty() || a.label.toLowerCase(Locale.ROOT).contains(needle) || a.pkg.toLowerCase(Locale.ROOT).contains(needle);
        boolean group=cat==null || cat.equals("All") || cat.equals(a.category);
        return text&&group;
    }

    private static String category(PackageManager pm,String pkg) {
        try {
            ApplicationInfo ai=pm.getApplicationInfo(pkg,0);
            if(android.os.Build.VERSION.SDK_INT>=26) {
                switch(ai.category) {
                    case ApplicationInfo.CATEGORY_GAME:return "Games";
                    case ApplicationInfo.CATEGORY_AUDIO:return "Media";
                    case ApplicationInfo.CATEGORY_VIDEO:return "Media";
                    case ApplicationInfo.CATEGORY_IMAGE:return "Media";
                    case ApplicationInfo.CATEGORY_SOCIAL:return "Social";
                    case ApplicationInfo.CATEGORY_NEWS:return "News";
                    case ApplicationInfo.CATEGORY_MAPS:return "Travel";
                    case ApplicationInfo.CATEGORY_PRODUCTIVITY:return "Work";
                    case ApplicationInfo.CATEGORY_ACCESSIBILITY:return "Tools";
                }
            }
        } catch(Exception ignored) {}
        String p=pkg.toLowerCase(Locale.ROOT);
        if(p.contains("camera")||p.contains("gallery")||p.contains("photo")||p.contains("video")||p.contains("music")||p.contains("youtube")) return "Media";
        if(p.contains("whatsapp")||p.contains("telegram")||p.contains("instagram")||p.contains("facebook")||p.contains("twitter")||p.contains("tiktok")) return "Social";
        if(p.contains("map")||p.contains("travel")||p.contains("grab")||p.contains("gojek")) return "Travel";
        if(p.contains("office")||p.contains("docs")||p.contains("sheet")||p.contains("drive")||p.contains("mail")) return "Work";
        if(p.contains("setting")||p.contains("tool")||p.contains("file")||p.contains("manager")||p.contains("clock")||p.contains("calculator")) return "Tools";
        return "Other";
    }
}

package id.airi.os;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class AiriApp extends Application implements Application.ActivityLifecycleCallbacks {
    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    private void skin(Activity activity) {
        if (activity == null) return;
        activity.getWindow().getDecorView().post(() -> AiriLiquidSkin.apply(activity));
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) { skin(activity); }
    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityResumed(Activity activity) { skin(activity); }
    @Override public void onActivityPaused(Activity activity) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
    @Override public void onActivityDestroyed(Activity activity) { }
}

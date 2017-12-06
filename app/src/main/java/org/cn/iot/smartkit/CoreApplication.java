package org.cn.iot.smartkit;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import org.cn.plugin.common.optional.OptionalManager;
import org.cn.plugin.message.MessageConst;
import org.cn.plugin.message.service.MessageService;
import org.cn.plugin.message.utils.OrmHelper;

import java.util.LinkedList;

public class CoreApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        OptionalManager.init(getApplicationContext());
        OrmHelper.init(getApplicationContext());

//        DeviceConst.init();
        MessageConst.init();
        MessageService.start(getApplicationContext());

        init();
    }

    private LinkedList<Activity> cacheActivities = new LinkedList<>();

    private Activity topActivity;

    public Activity getTopActivity() {
        return topActivity;
    }

    private void init() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                topActivity = activity;
                addActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                topActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (topActivity == activity) {
                    topActivity = null;
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                removeActivity(activity);
            }
        });
    }


    private void addActivity(Activity instance) {
        cacheActivities.add(instance);
    }

    private void removeActivity(Activity instance) {
        cacheActivities.remove(instance);
    }

}

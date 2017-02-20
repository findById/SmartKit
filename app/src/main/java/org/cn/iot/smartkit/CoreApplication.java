package org.cn.iot.smartkit;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import org.cn.iot.device.DeviceConst;
import org.cn.iot.smartkit.optional.OptionalConst;
import org.cn.iot.smartkit.optional.OptionalManager;
import org.cn.plugin.message.MessageConst;
import org.cn.plugin.message.service.MessageService;
import org.cn.plugin.message.utils.OrmHelper;

import java.util.LinkedList;

public class CoreApplication extends Application {

    private static CoreApplication instance;

    public static CoreApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        OptionalManager.init(getApplicationContext());
        OrmHelper.init(getApplicationContext());

        String API_HOST = OptionalManager.getString(OptionalConst.KEY_API_HOST, "http://192.168.99.111:8080");

        DeviceConst.API_HOST = API_HOST;

        MessageConst.API_HOST = API_HOST;
        MessageConst.host = OptionalManager.getString("mqtt_message_server", "tcp://192.168.99.111:61613");
        MessageConst.username = OptionalManager.getString("mqtt_message_username", "admin");
        MessageConst.password = OptionalManager.getString("mqtt_message_password", "password");
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

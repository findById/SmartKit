package org.cn.plugin.message.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import org.cn.plugin.message.MessageConst;
import org.cn.plugin.message.MessagePlugin;

import java.util.Timer;
import java.util.TimerTask;

public class MessageService extends Service {
    private static final String TAG = "MessageService";
    public static final String ACTION_PLUGIN_MESSAGE_SERVICE = "org.cn.plugin.message.service";
    public static final String ACTION_IOT_MESSAGE_ARRIVED = "action.iot.message.arrived";
    public static final String ACTION_IOT_MESSAGE_PUBLISH = "action.iot.message.publish";
    public static final String EXTRA_CONSUMER_ID = "extra.consumer.id";
    public static final String EXTRA_MESSAGE_BODY = "extra.message.body";

    MessageReceiver receiver = new MessageReceiver();

    MessagePlugin plugin = new MessagePlugin();

    Timer timer = new Timer();

    public static void start(Context ctx) {
        if (TextUtils.isEmpty(MessageConst.host) || TextUtils.isEmpty(MessageConst.username) || TextUtils.isEmpty(MessageConst.password)) {
            Log.e(TAG, "MQTT server configuration can't be null.");
            return;
        }
        Intent intent = new Intent(ctx, MessageService.class);
        intent.setAction(ACTION_PLUGIN_MESSAGE_SERVICE);
        ctx.startService(intent);
    }

    public static void publish(Context ctx, String id, String message) {
        Intent intent = new Intent(ctx, MessageService.class);
        intent.setAction(ACTION_IOT_MESSAGE_PUBLISH);
        intent.putExtra(EXTRA_CONSUMER_ID, id);
        intent.putExtra(EXTRA_MESSAGE_BODY, message);
        ctx.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (plugin == null) {
                        cancel();
                        return;
                    }
                    if (!plugin.isConnected()) {
                        plugin.connect();
                    }
                    plugin.publish("heartbeat", "0");
                } catch (Throwable e) {
                    e.printStackTrace();
                    Log.e("MS", e.getMessage(), e);
                }
            }
        }, 5000, 5000);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, new IntentFilter(ACTION_IOT_MESSAGE_ARRIVED));
        plugin.setListener(new MessagePlugin.OnHandleMessageListener() {
            @Override
            public void onHandleMessage(String id, String text) {
                Intent intent = new Intent(ACTION_IOT_MESSAGE_ARRIVED);
                intent.putExtra(EXTRA_CONSUMER_ID, id);
                intent.putExtra(EXTRA_MESSAGE_BODY, text);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (plugin == null) {
            return START_STICKY;
        }
        if (!plugin.isConnected()) {
            plugin.connect();
        }
        switch (intent.getAction()) {
            case ACTION_IOT_MESSAGE_PUBLISH: {
                String id = intent.getStringExtra(EXTRA_CONSUMER_ID);
                String message = intent.getStringExtra(EXTRA_MESSAGE_BODY);
                boolean success = plugin.publish(id, message);
                break;
            }
            default:
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        if (plugin != null) {
            plugin.onDestroy();
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        super.onDestroy();
    }
}

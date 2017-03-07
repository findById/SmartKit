package org.cn.plugin.message.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.cn.plugin.message.MessageActivity;
import org.cn.plugin.message.R;
import org.cn.plugin.message.model.Message;

public class MessageReceiver extends BroadcastReceiver {

    private static String messageId = String.valueOf(System.currentTimeMillis());

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case MessageService.ACTION_IOT_MESSAGE_ARRIVED: {
                String consumerId = intent.getStringExtra(MessageService.EXTRA_CONSUMER_ID);
                String text = intent.getStringExtra(MessageService.EXTRA_MESSAGE_BODY);
                try {
                    JSONObject obj = JSON.parseObject(text);
                    String msgType = obj.getString("type");

                    Message message = new Message();

                    message.producerId = obj.getString("deviceId");
                    message.consumerId = consumerId;
                    message.msgType = msgType;
                    message.body = obj.getString("body");

                    showNotification(context, message);

                    Intent msg = new Intent(MessageActivity.ACTION_MESSAGE);
                    msg.putExtra(MessageActivity.EXTRA_MESSAGE_DATA, message);
                    context.sendBroadcast(msg);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;
        }
    }

    public void showNotification(Context ctx, Message message) {
        Intent intent = new Intent();
        intent.setClass(ctx, MessageActivity.class);

        messageId = messageId.substring(messageId.length() - 6, messageId.length());

        PendingIntent pi = PendingIntent.getActivity(ctx, Integer.valueOf(messageId), intent, PendingIntent.FLAG_CANCEL_CURRENT);// PendingIntent.FLAG_UPDATE_CURRENT FLAG_AUTO_CANCEL
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        builder.setContentIntent(pi);
        builder.setPriority(Notification.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);
        builder.setOngoing(false);
        builder.setTicker(message.body);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.body));
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle(message.producerId);
        builder.setContentText(message.body);

        Notification notification = builder.build();
        manager.notify(Integer.valueOf(messageId), notification);
    }

}

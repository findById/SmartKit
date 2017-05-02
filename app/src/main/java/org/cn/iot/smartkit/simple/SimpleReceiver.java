package org.cn.iot.smartkit.simple;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import org.cn.iot.smartkit.R;
import org.cn.plugin.message.MessageActivity;
import org.cn.plugin.message.MessageConst;
import org.cn.plugin.message.model.Message;
import org.cn.plugin.message.model.MessageType;

/**
 * Created by work on 17-5-2.
 */

public class SimpleReceiver extends BroadcastReceiver {
    private static String messageId = String.valueOf(System.currentTimeMillis());

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MessageConst.ACTION_MESSAGE_ARRIVED.equals(intent.getAction())) {
            Message message = (Message) intent.getSerializableExtra(MessageConst.ACTION_MESSAGE_ARRIVED);

            if (hasNotify(message.msgType)) {
                showNotification(context, message);
            }
        }
    }

    private boolean hasNotify(String type) {
        if (MessageType.REPORT.equals(type)) {
            return false;
        }
        return true;
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
        builder.setSmallIcon(R.drawable.ic_fingerprint);
        builder.setContentTitle(message.producerId);
        builder.setContentText(message.body);

        Notification notification = builder.build();
        manager.notify(Integer.valueOf(messageId), notification);
    }

}

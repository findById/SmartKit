package org.cn.plugin.message.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import org.cn.plugin.message.MessageActivity;
import org.cn.plugin.message.R;
import org.cn.plugin.message.model.Message;
import org.cn.plugin.message.utils.OrmHelper;

public class MessageReceiver extends BroadcastReceiver {

    private static String messageId = String.valueOf(System.currentTimeMillis());

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case MessageService.ACTION_IOT_MESSAGE_ARRIVED: {
                String id = intent.getStringExtra(MessageService.EXTRA_MESSAGE_ID);
                String message = intent.getStringExtra(MessageService.EXTRA_MESSAGE_BODY);

                Message bean = new Message(id, MessageActivity.userId, "text", message);
                OrmHelper.getInstance().insert(bean);

                showNotification(context, bean);

                Intent msg = new Intent(MessageActivity.ACTION_MESSAGE);
                msg.putExtra(MessageActivity.EXTRA_MESSAGE_DATA, bean);
                context.sendBroadcast(msg);
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
        builder.setTicker("ticker");
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.body));
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle(message.producerId);
        builder.setContentText(message.body);

        Notification notification = builder.build();
        manager.notify(Integer.valueOf(messageId), notification);
    }

}

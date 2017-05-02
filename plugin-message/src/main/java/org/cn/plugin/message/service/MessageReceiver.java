package org.cn.plugin.message.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.cn.plugin.message.MessageConst;
import org.cn.plugin.message.model.Message;

public class MessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case MessageService.ACTION_IOT_MESSAGE_ARRIVED: {
                String consumerId = intent.getStringExtra(MessageService.EXTRA_CONSUMER_ID);
                String text = intent.getStringExtra(MessageService.EXTRA_MESSAGE_BODY);
                try {
                    JSONObject obj = JSON.parseObject(text);
                    String msgType = obj.getString("t"); // type

                    Message message = new Message();
                    message.producerId = obj.getString("id"); // deviceId
                    message.consumerId = consumerId;
                    message.msgType = msgType;
                    message.body = obj.getString("l"); // logic

                    Intent msg = new Intent(MessageConst.ACTION_MESSAGE_ARRIVED);
                    msg.putExtra(MessageConst.EXTRA_MESSAGE_DATA, message);
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
}

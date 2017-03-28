package org.cn.iot.smartkit.simple;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.cn.iot.smartkit.R;
import org.cn.plugin.message.MessageActivity;
import org.cn.plugin.message.model.Message;
import org.cn.plugin.message.service.MessageService;

/**
 * Created by chenning on 17-3-28.
 */

public class SimpleActivity extends AppCompatActivity {

    private SwitchCompat mSwitchCompat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        registerReceiver(receiver, new IntentFilter(MessageActivity.ACTION_MESSAGE));

        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void initView() {
        mSwitchCompat = (SwitchCompat) findViewById(R.id.btnSwitch);
        mSwitchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageService.publish(mSwitchCompat.getContext(), "ESP8266", mSwitchCompat.isChecked() ? "051" : "050");
            }
        });
    }

    private void initData() {
        MessageService.publish(this, "ESP8266", "1");
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case MessageActivity.ACTION_MESSAGE: {
                    try {
                        Message message = (Message) intent.getSerializableExtra(MessageActivity.EXTRA_MESSAGE_DATA);
                        JSONObject obj = JSON.parseObject(message.body);
                        if ("opened".equals(obj.getString("metadata"))) {
                            mSwitchCompat.setChecked(true);
                        } else {
                            mSwitchCompat.setChecked(false);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };
}

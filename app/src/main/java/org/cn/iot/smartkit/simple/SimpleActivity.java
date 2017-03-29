package org.cn.iot.smartkit.simple;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

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
    private TextView mDeviceStatus;

    private String deviceId = "";

    private boolean online = false;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        deviceId = getIntent().getStringExtra("deviceId");
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "ESP8266";
        }

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
                MessageService.publish(mSwitchCompat.getContext(), deviceId, mSwitchCompat.isChecked() ? "051" : "050");
            }
        });

        mDeviceStatus = (TextView) findViewById(R.id.deviceStatus);
        mDeviceStatus.setText(deviceId);
    }

    private void initData() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        MessageService.publish(this, deviceId, "1");
        mHandler.postDelayed(runnable, 1000 * 5);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (online) {
                mDeviceStatus.setText(deviceId);
            } else {
                mDeviceStatus.setText("设备离线");
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case MessageActivity.ACTION_MESSAGE: {
                    try {
                        Message message = (Message) intent.getSerializableExtra(MessageActivity.EXTRA_MESSAGE_DATA);
                        if (message.producerId.equals(deviceId) || "ESP8266".equals(deviceId)) {
                            online = true;
                        }
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

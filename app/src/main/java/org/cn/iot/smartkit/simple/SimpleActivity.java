package org.cn.iot.smartkit.simple;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.cn.iot.smartkit.R;
import org.cn.plugin.message.MessageActivity;
import org.cn.plugin.message.model.Message;
import org.cn.plugin.message.model.MessageType;
import org.cn.plugin.message.service.MessageService;
import org.cn.plugin.rpc.utils.IOUtil;

import java.net.HttpURLConnection;
import java.net.URL;

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
        mDeviceStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageService.publish(v.getContext(), deviceId, "x");
            }
        });
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
                        switch (message.msgType) {
                            case MessageType.REPORT: {
                                JSONArray array = JSON.parseArray(message.body);
                                for (int i = 0; i < array.size(); i++) {
                                    JSONObject item = array.getJSONObject(i);
                                    System.out.println(item.toJSONString());
                                }
                                break;
                            }
                            case MessageType.NOTIFY: {
                                JSONArray array = JSON.parseArray(message.body);
                                for (int i = 0; i < array.size(); i++) {
                                    JSONObject item = array.getJSONObject(i);
                                    switch (item.getString("type")) {
                                        case "relay": {
                                            if ("opened".equals(item.getString("metadata"))) {
                                                mSwitchCompat.setChecked(true);
                                            } else {
                                                mSwitchCompat.setChecked(false);
                                            }
                                            break;
                                        }
                                        default:
                                            break;
                                    }
                                }
                                break;
                            }
                            case MessageType.UPGRADE: {
                                JSONObject obj = JSON.parseObject(message.body);
                                upgrade(message.producerId, obj.getString("v"));
                                break;
                            }
                            default:
                                break;
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

    private void upgrade(String deviceId, String version) {
        String check = "https://raw.githubusercontent.com/findById/esp-ota/master/v1/build";
        final String v1 = "https://github.com/findById/esp-ota/raw/master/v1/esp-8266.bin";
        new AsyncTask<String, Void, Boolean>() {
            String deviceId;
            String version;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(String... params) {
                deviceId = params[0];
                version = params[1];
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(params[2]).openConnection();
                    conn.setDoInput(true);
                    conn.setRequestMethod("GET");
                    conn.setReadTimeout(1000 * 10);
                    conn.setConnectTimeout(1000 * 5);
                    conn.setUseCaches(false);
                    int statusCode = conn.getResponseCode();
                    if (statusCode != 200) {
                        return false;
                    }
                    String result = IOUtil.asString(conn.getInputStream(), "UTF-8");
                    result = result.replaceAll("\\n", "");
                    if (TextUtils.isEmpty(result) || version.equals(result)) {
                        return false;
                    }
                    return true;
                } catch (Throwable e) {
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    new AlertDialog.Builder(SimpleActivity.this)
                            .setCancelable(true)
                            .setMessage("是否更新固件")
                            .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MessageService.publish(mSwitchCompat.getContext(), deviceId, "u" + v1);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else {
                    Toast.makeText(SimpleActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(deviceId, version, check);
    }
}

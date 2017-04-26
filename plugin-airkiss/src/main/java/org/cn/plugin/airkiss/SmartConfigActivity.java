package org.cn.plugin.airkiss;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.cn.plugin.airkiss.databinding.ActivityAirkissBinding;
import org.cn.plugin.airkiss.utils.EspWifiUtil;
import org.cn.plugin.common.optional.OptionalConst;
import org.cn.plugin.common.optional.OptionalManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SmartConfigActivity extends AppCompatActivity {

    private ActivityAirkissBinding mBinding;

    private AirKiss mAirKiss;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_airkiss);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        initData();
    }

    private void initView() {
        mBinding.apSsid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
    }

    private void initData() {
        mAirKiss = AirKissFactory.create(this, "esptouch");
        mAirKiss.registerListener(new AirKissListener() {
            @Override
            public void onHandled(int type, String message, AirKissResult result) {
                switch (type) {
                    case AirKissConst.CODE_ERROR: {
                        Toast.makeText(SmartConfigActivity.this, String.valueOf(message), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case AirKissConst.CODE_PROGRESS: {
                        mProgressDialog = new ProgressDialog(SmartConfigActivity.this);
                        mProgressDialog.setMessage("Esptouch is configuring, please wait for a moment...");
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mAirKiss.cancel();
                            }
                        });
                        mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Waiting...", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        mProgressDialog.show();
                        mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        break;
                    }
                    case AirKissConst.CODE_SUCCESS: {
                        mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Confirm");
                        mProgressDialog.setMessage("Success, bssid = " + result.bssid + ", InetAddress = " + result.address);
                        break;
                    }
                    case AirKissConst.CODE_FAILED: {
                        mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Confirm");
                        mProgressDialog.setMessage("Failed");
                        break;
                    }
                    case AirKissConst.CODE_CANCELLED: {
                        break;
                    }
                    case AirKissConst.CODE_WORKER_THREAD_SUCCESS: {
                        map.remove(AirKissConst.KEYS_AP_SSID);
                        map.remove(AirKissConst.KEYS_AP_BSSID);
                        map.remove(AirKissConst.KEYS_AP_PASSWORD);
                        startTCPConfig(result.address);
                        break;
                    }
                    default:
                        break;
                }
            }
        });

        mBinding.mqttServer.setText(OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_ADDR));
        mBinding.mqttUsername.setText(OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_USERNAME));
        mBinding.mqttPassword.setText(OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_PASSWORD));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_air, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_confirm) {
            startSmartConfig();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSsid();
    }

    private void getSsid() {
        mBinding.apSsid.postDelayed(new Runnable() {
            @Override
            public void run() {
                String apSsid = EspWifiUtil.getSSID(SmartConfigActivity.this);
                mBinding.apSsid.setText(TextUtils.isEmpty(apSsid) ? "" : apSsid);
            }
        }, 200);
    }

    private void startSmartConfig() {
        initDeviceData();

        String apSsid = mBinding.apSsid.getText().toString();
        if (TextUtils.isEmpty(apSsid)) {
            getSsid();
            return;
        }
        String apPassword = mBinding.apPassword.getText().toString();
        String apBssid = EspWifiUtil.getBSSID(SmartConfigActivity.this);

        map.put(AirKissConst.KEYS_AP_SSID, apSsid);
        map.put(AirKissConst.KEYS_AP_BSSID, apBssid);
        map.put(AirKissConst.KEYS_AP_PASSWORD, apPassword);

        mAirKiss.execute(map);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    Map<String, String> map = new HashMap<>();

    public void initDeviceData() {
        map.put(AirKissConst.KEYS_DEVICE_NAME, mBinding.deviceName.getText().toString());
        map.put(AirKissConst.KEYS_MQTT_SERVER, mBinding.mqttServer.getText().toString());
        map.put(AirKissConst.KEYS_MQTT_USERNAME, mBinding.mqttUsername.getText().toString());
        map.put(AirKissConst.KEYS_MQTT_PASSWORD, mBinding.mqttPassword.getText().toString());
    }

    int retryCount = 0;

    public void startTCPConfig(String host) {
        try {
            Socket socket = new Socket(host, 8266);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer;
            for (String key : map.keySet()) {
                buffer = encodePacket(key, map.get(key));
                if (buffer == null) {
                    continue;
                }
                baos.write(buffer);
            }
            baos.write(0);
            baos.write(0);

            OutputStream os = socket.getOutputStream();
            os.write(baos.toByteArray());
            os.flush();
            socket.close();
            retryCount = 0;
        } catch (IOException e) {
            e.printStackTrace();
            if (retryCount < 10) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                retryCount++;
                startTCPConfig(host);
            } else {
                retryCount = 0;
            }
        }
    }

    public byte[] encodePacket(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("'key' must not be null.");
        }
        if (value == null) {
            value = "";
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(key.length());
            baos.write(key.getBytes());
            baos.write(value.length());
            baos.write(value.getBytes());
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

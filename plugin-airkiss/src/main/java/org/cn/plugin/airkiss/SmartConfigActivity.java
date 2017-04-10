package org.cn.plugin.airkiss;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;

import org.cn.plugin.airkiss.databinding.ActivityAirkissBinding;
import org.cn.plugin.common.optional.OptionalConst;
import org.cn.plugin.common.optional.OptionalManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartConfigActivity extends AppCompatActivity {

    private EspWifiAdminSimple mWifiAdmin;

    private ActivityAirkissBinding mBinding;

    private String deviceId;

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
        mWifiAdmin = new EspWifiAdminSimple(this);

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
                String apSsid = mWifiAdmin.getWifiConnectedSsid();
                mBinding.apSsid.setText(TextUtils.isEmpty(apSsid) ? "" : apSsid);
            }
        }, 200);
    }

    private void startSmartConfig() {
        initDeviceData();

        if (TextUtils.isEmpty(deviceId)) {
            Toast.makeText(this, "请输入设备Id", Toast.LENGTH_SHORT).show();
            return;
        }

        String apSsid = mBinding.apSsid.getText().toString();
        if (TextUtils.isEmpty(apSsid)) {
            getSsid();
            return;
        }
        String apPassword = mBinding.apPassword.getText().toString();
        String apBssid = mWifiAdmin.getWifiConnectedBssid();

        new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword, "1");
    }

    private IEsptouchListener myListener = new IEsptouchListener() {
        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            startTCPConfig(result.getInetAddress().getHostAddress());
        }
    };

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private ProgressDialog mProgressDialog;

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(SmartConfigActivity.this);
            mProgressDialog.setMessage("Esptouch is configuring, please wait for a moment...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Waiting...", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                // !!!NOTICE
                String apSsid = mWifiAdmin.getWifiConnectedSsidAscii(params[0]);
                String apBssid = params[1];
                String apPassword = params[2];
                String taskResultCountStr = params[3];
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, SmartConfigActivity.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Confirm");
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                if (firstResult.isSuc()) {
                    mProgressDialog.setMessage("Success, bssid = "
                            + firstResult.getBssid()
                            + ", InetAddress = "
                            + firstResult.getInetAddress()
                            .getHostAddress());
                } else {
                    mProgressDialog.setMessage("Failed");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    Map<String, String> map = new HashMap<>();

    public void initDeviceData() {
        deviceId = mBinding.deviceId.getText().toString();
        map.put("device_id", deviceId);
        map.put("device_name", mBinding.deviceName.getText().toString());
        map.put("token", mBinding.token.getText().toString());
        map.put("mqtt_server", mBinding.mqttServer.getText().toString());
        map.put("mqtt_username", mBinding.mqttUsername.getText().toString());
        map.put("mqtt_password", mBinding.mqttPassword.getText().toString());
        map.put("ota_server", mBinding.ota.getText().toString());
    }

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
//            int len = baos.size();
//            os.write((len >> 24) & 0xFF);
//            os.write((len >> 16) & 0xFF);
//            os.write((len >> 8) & 0xFF);
//            os.write((len) & 0xFF);
            os.write(baos.toByteArray());
            os.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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

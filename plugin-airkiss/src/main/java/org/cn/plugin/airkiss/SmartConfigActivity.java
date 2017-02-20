package org.cn.plugin.airkiss;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;

import org.cn.plugin.airkiss.databinding.ActivityAirkissBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartConfigActivity extends AppCompatActivity {
    private static final String TAG = "SmartConfigActivity";

    private EspWifiAdminSimple mWifiAdmin;

    private SharedPreferences sp;
    private SharedPreferences.Editor spe;

    private ActivityAirkissBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_airkiss);

        setSupportActionBar(mBinding.toolbar);

        initView();
        initData();
    }

    private void initView() {
    }

    private void initData() {
        sp = getSharedPreferences("udp_config", Context.MODE_APPEND);
        spe = sp.edit();

        mWifiAdmin = new EspWifiAdminSimple(this);

        mBinding.verificationCode.setText(sp.getString("verification_code", ""));
        mBinding.mqttServer.setText(sp.getString("mqtt_server", ""));
        mBinding.mqttUserId.setText(sp.getString("mqtt_user_id", ""));
        mBinding.mqttUsername.setText(sp.getString("mqtt_username", ""));
        mBinding.mqttPassword.setText(sp.getString("mqtt_password", ""));
        mBinding.deviceName.setText(sp.getString("device_name", ""));
        mBinding.updateServer.setText(sp.getString("update_server", ""));
        mBinding.token.setText(sp.getString("token", ""));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initUDPConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_air, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_confirm) {
            start();
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
        String apSsid = mWifiAdmin.getWifiConnectedSsid();
        mBinding.apSsid.setText(TextUtils.isEmpty(apSsid) ? "" : apSsid);
    }

    private void start() {
        spe.putString("verification_code", mBinding.verificationCode.getText().toString());
        spe.putString("mqtt_server", mBinding.mqttServer.getText().toString());
        spe.putString("mqtt_user_id", mBinding.mqttUserId.getText().toString());
        spe.putString("mqtt_username", mBinding.mqttUsername.getText().toString());
        spe.putString("mqtt_password", mBinding.mqttPassword.getText().toString());
        spe.putString("update_server", mBinding.updateServer.getText().toString());
        spe.putString("device_name", mBinding.deviceName.getText().toString());
        spe.putString("token", mBinding.token.getText().toString());
        spe.commit();

        initLastData();

        String apSsid = mBinding.apSsid.getText().toString();
        if (TextUtils.isEmpty(apSsid)) {
            getSsid();
            return;
        }
        String apPassword = mBinding.apPassword.getText().toString();
        String apBssid = mWifiAdmin.getWifiConnectedBssid();

        new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword, "1");
    }

    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = result.getBssid() + " is connected to the wifi";
                Toast.makeText(SmartConfigActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
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
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("Esptouch success, bssid = "
                                + resultInList.getBssid()
                                + ",InetAddress = "
                                + resultInList.getInetAddress()
                                .getHostAddress() + "\n");
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count) + " more result(s) without showing\n");
                    }
                    mProgressDialog.setMessage(sb.toString());
                } else {
                    mProgressDialog.setMessage("Esptouch fail");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (socket != null) {
            try {
                socket.disconnect();
                socket.close();
            } catch (Throwable ignored) {
            }
        }
        super.onDestroy();
    }

    Map<String, String> map = new HashMap<>();

    public void initLastData() {
        map.put("verification_code", sp.getString("verification_code", ""));
        map.put("mqtt_server", sp.getString("mqtt_server", ""));
        map.put("mqtt_user_id", sp.getString("mqtt_user_id", ""));
        map.put("mqtt_username", sp.getString("mqtt_username", ""));
        map.put("mqtt_password", sp.getString("mqtt_password", ""));
        map.put("update_server", sp.getString("update_server", ""));
        map.put("device_name", sp.getString("device_name", ""));
        map.put("token", sp.getString("token", ""));
    }

    DatagramSocket socket;

    public void initUDPConfig() throws IOException {
        initLastData();

        List<String> deviceList = new ArrayList<>();

        socket = new DatagramSocket(new InetSocketAddress("0.0.0.0", 8266));
        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            socket.receive(packet);
            String data = new String(packet.getData());
            System.out.println("addr: " + packet.getAddress());
            System.out.println("data: " + data);
            System.out.println("length: " + packet.getLength());

            if (!data.startsWith("config:")) {
                continue;
            }

            String deviceId = data.substring(6, data.length());
            if (!deviceList.contains(deviceId)) {
                // find by database
            }

            for (String key : map.keySet()) {
                byte[] buffer = encodePacket(key, map.get(key));
                if (buffer == null) {
                    continue;
                }
                socket.send(new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort()));
                System.out.println("send: " + key);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            byte[] buffer = encodePacket("end", "end");
            socket.send(new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Success.", Toast.LENGTH_SHORT).show();
                }
            });
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

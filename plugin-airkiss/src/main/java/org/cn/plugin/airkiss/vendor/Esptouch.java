package org.cn.plugin.airkiss.vendor;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;

import org.cn.plugin.airkiss.AirKiss;
import org.cn.plugin.airkiss.AirKissConst;
import org.cn.plugin.airkiss.AirKissListener;
import org.cn.plugin.airkiss.AirKissResult;
import org.cn.plugin.airkiss.utils.EspWifiUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Esptouch implements AirKiss {

    private Context mContext;

    private IEsptouchTask mEsptouchTask;
    // without the lock, if the user tap confirm and cancel quickly enough,
    // the bug will arise. the reason is follows:
    // 0. task is starting created, but not finished
    // 1. the task is cancel for the task hasn't been created, it do nothing
    // 2. task is created
    // 3. Oops, the task should be cancelled, but it is running
    private final Object mLock = new Object();

    private AirKissListener listener;

    private Map<String, String> map = new HashMap<>();

    public Esptouch(Context ctx) {
        this.mContext = ctx;
    }

    @Override
    public void execute(Map<String, String> map) {
        this.map.clear();
        this.map.putAll(map);

        String ssid = this.map.get(AirKissConst.KEYS_AP_SSID);
        String bssid = this.map.get(AirKissConst.KEYS_AP_BSSID);
        String password = this.map.get(AirKissConst.KEYS_AP_PASSWORD);
        if (TextUtils.isEmpty(ssid)) {
            if (listener != null) {
                listener.onHandled(AirKissConst.CODE_ERROR, "'ssid' must not be null.", null);
            }
            return;
        }
        new EsptouchAsyncTask().execute(ssid, bssid, password, "1");
    }

    @Override
    public void cancel() {
        synchronized (mLock) {
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
        }
    }

    @Override
    public void registerListener(AirKissListener listener) {
        this.listener = listener;
    }

    @Override
    public void unregisterListener(AirKissListener listener) {

    }

    private IEsptouchListener esptouchListener = new IEsptouchListener() {
        @Override
        public void onEsptouchResultAdded(IEsptouchResult result) {
            if (listener != null) {
                listener.onHandled(AirKissConst.CODE_WORKER_THREAD_SUCCESS, "success",
                        new AirKissResult(true, result.getBssid(), result.getInetAddress().getHostAddress()));
            }
        }
    };

    private class EsptouchAsyncTask extends AsyncTask<String, Void, List<IEsptouchResult>> {

        @Override
        protected void onPreExecute() {
            if (listener != null) {
                listener.onHandled(AirKissConst.CODE_PROGRESS, "progress", null);
            }
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                // !!!NOTICE
                String apSsid = EspWifiUtil.getSSIDAscii(mContext, params[0]);
                String apBssid = params[1];
                String apPassword = params[2];
                String taskResultCountStr = params[3];
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, mContext);
                mEsptouchTask.setEsptouchListener(esptouchListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                if (firstResult.isSuc()) {
                    if (listener != null) {
                        listener.onHandled(AirKissConst.CODE_SUCCESS, "success",
                                new AirKissResult(true, firstResult.getBssid(), firstResult.getInetAddress().getHostAddress()));
                    }
                } else {
                    listener.onHandled(AirKissConst.CODE_FAILED, "failed", new AirKissResult(false));
                }
            }
        }
    }

}

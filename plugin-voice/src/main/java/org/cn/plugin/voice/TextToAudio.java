package org.cn.plugin.voice;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.cn.plugin.rpc.Response;
import org.cn.plugin.rpc.ResponseListener;
import org.cn.plugin.rpc.RpcEngine;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class TextToAudio {
    public static final String BAIDU_TOKEN_API = "https://openapi.baidu.com/oauth/2.0/token";
    public static final String BAIDU_CLIENT_ID = "";
    public static final String BAIDU_CLIENT_SECRET = "";

    public static void getToken(final ResponseListener listener) {
        StringBuffer param = new StringBuffer();
        param.append("grant_type=client_credentials");
        param.append("&client_id=" + BAIDU_CLIENT_ID);
        param.append("&client_secret=" + BAIDU_CLIENT_SECRET);

        RpcEngine.post(BAIDU_TOKEN_API, param.toString(), new ResponseListener<Response>() {
            @Override
            public void onResponse(Response response) {
                if (!response.isSuccess()) {
                    if (listener != null) {
                        listener.onResponse(response);
                    }
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(response.result);

                    VoiceConst.BAIDU_TOKEN = obj.optString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (listener != null) {
                    listener.onResponse(response);
                }
            }
        });

    }

    public static void send(final String text, final String audioPath, final HttpListener listener) {
        try {
            if (!TextUtils.isEmpty(VoiceConst.BAIDU_TOKEN)) {
                dowork(text, audioPath, listener);
                return;
            }

            getToken(new ResponseListener<Response>() {
                @Override
                public void onResponse(Response response) {
                    if (!response.isSuccess()) {
                        listener.onResult(response.statusCode, response.message);
                        return;
                    }

                    dowork(text, audioPath, listener);
                }
            });

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void dowork(String text, String audioPath, HttpListener listener) {
        try {
            StringBuffer param = new StringBuffer();
            param.append("tex=" + text);
            param.append("&lan=" + "zh");
            param.append("&tok=" + VoiceConst.BAIDU_TOKEN);
            param.append("&ctp=" + "1");
            param.append("&cuid=" + "123");

            send("http://tsn.baidu.com/text2audio", param.toString(), audioPath, listener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public interface HttpListener {
        void onResult(int statusCode, String result);
    }

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void send(final String url, final String body, final String audioPath, final HttpListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(1000 * 5);
                    conn.setUseCaches(false);
                    conn.addRequestProperty("content-type", "application/json;charset=utf-8");

                    OutputStream os = conn.getOutputStream();
                    os.write(body.getBytes(Charset.forName("UTF-8")));
                    os.flush();

                    final int statusCode = conn.getResponseCode();
                    if (statusCode != 200) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onResult(statusCode, null);
                                }
                            }
                        });
                        return;
                    }

                    InputStream is = conn.getInputStream();

                    String contentType = conn.getHeaderField("Content-Type");
                    Log.e("TTT", "" + contentType);
                    if (!contentType.contains("audio")) {
                        final ByteArrayOutputStream fos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while ((len = is.read(buffer)) >= 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.flush();

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onResult(201, fos.toString());
                                }
                            }
                        });
                        return;
                    }

                    final FileOutputStream fos = new FileOutputStream(audioPath);
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while ((len = is.read(buffer)) >= 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onResult(statusCode, audioPath);
                            }
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onResult(202, audioPath);
                            }
                        }
                    });
                }
            }
        }).start();
    }

}

package org.cn.plugin.rpc;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.cn.plugin.rpc.utils.IOUtil;
import org.cn.plugin.rpc.utils.ThreadUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class RealService {

    private Request request;

    public RealService(Request request) {
        this.request = request;
    }

    public Response execute() {
        Response response = getResponse();
        return response;
    }

    private Response getResponse() {
        Response response = new Response();
        response.timestamp = System.currentTimeMillis();
        try {
            response = httpRequest(request);
            // response.response = Service.post(request.url(), request.body());
        } catch (Throwable e) {
            response.statusCode = -500;
            response.message = e.getMessage();
        }
        return response;
    }

    public Response httpRequest(Request request) {
        Response response = new Response();
        response.timestamp = System.currentTimeMillis();
        HttpURLConnection connection = null;
        try {
            URL parsedUrl = new URL(request.url);
            connection = (HttpURLConnection) parsedUrl.openConnection();
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(1000 * 10);
            connection.setReadTimeout(1000 * 10);

            if ("GET".equals(request.method())) {
                connection.setInstanceFollowRedirects(true);
            } else {
                connection.setInstanceFollowRedirects(false);
            }

            if (request.header != null && !request.header.isEmpty()) {
                for (String key : request.header.keySet()) {
                    if (TextUtils.isEmpty(key)) {
                        continue;
                    }
                    connection.addRequestProperty(key, request.header.get(key));
                }
            }

            connection.addRequestProperty("Content-Type", request.contentType);
            connection.addRequestProperty("User-Agent", "Android-OS/" + Build.VERSION.SDK_INT + "; Model/" + Build.MODEL);

            switch (request.method()) {
                case "POST": {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.write(request.body().getBytes("UTF-8"));
                    out.flush();
                    out.close();
                    break;
                }
                case "GET":
                default: {
                    connection.setRequestMethod("GET");
                    break;
                }
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                response.result = IOUtil.asString(connection.getInputStream(), "UTF-8");
            }
            response.statusCode = responseCode;
            response.message = "success.";
        } catch (ConnectException e) { // Connect
            response.statusCode = -200;
            response.message = e.getMessage();
        } catch (SocketTimeoutException e) { // Socket Timeout
            response.statusCode = -200;
            response.message = e.getMessage();
        } catch (MalformedURLException e) { // Bad URL
            response.statusCode = -200;
            response.message = e.getMessage();
        } catch (ProtocolException e) { // Protocol
            response.statusCode = -200;
            response.message = e.getMessage();
        } catch (IOException e) {
            response.statusCode = -200;
            response.message = e.getMessage();
        } catch (Throwable e) {
            response.statusCode = -200;
            response.message = e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    // == asynchronous start ==
    public void enqueue(ResponseListener<Response> listener) {
        enqueue(null, listener);
    }

    public void enqueue(Looper callbackLooper, ResponseListener<Response> listener) {
        ThreadUtil.getInstance().execute(new AsyncTask(callbackLooper, listener));
    }

    class AsyncTask implements Runnable, Comparable<AsyncTask> {
        private final ResponseListener<Response> listener;
        private final Handler handler;

        private AsyncTask(Looper looper, ResponseListener<Response> listener) {
            this.listener = listener;
            if (looper != null) {
                this.handler = new Handler(looper);
            } else {
                this.handler = null;
            }
        }

        @Override
        public void run() {
            final Response response = getResponse();
            if (listener != null) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResponse(response);
                        }
                    });
                    return;
                }
                listener.onResponse(response);
            }
        }

        @Override
        public int compareTo(AsyncTask o) {
            return o.hashCode() - this.hashCode();
        }
    }
    // == asynchronous end ==
}

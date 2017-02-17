package org.cn.plugin.rpc;

public class RpcEngine {

    public static Response get(String url) {
        Request request = Request.create().get(url).build();
        RealService real = new RealService(request);
        return real.execute();
    }

    public static void get(String url, ResponseListener<Response> listener) {
        Request request = Request.create().get(url).build();
        RealService real = new RealService(request);
        real.enqueue(listener);
    }

    public static Response post(String url, String body) {
        Request request = Request.create().post(url).body(body).build();
        RealService real = new RealService(request);
        return real.execute();
    }

    public static void post(String url, String body, ResponseListener<Response> listener) {
        Request request = Request.create().post(url).body(body).build();
        RealService real = new RealService(request);
        real.enqueue(listener);
    }

}

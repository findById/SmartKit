package org.cn.plugin.dlna.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chenning on 17-1-17.
 */

public class ImageLoader {

    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private static int resId;

    public static void init(int defaultResId) {
        resId = defaultResId;
    }

    public static void display(final String url, final ImageView view) {
        if (url == null || view == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = load(url);
                if (bitmap == null) {
                    view.setImageResource(resId);
                    return;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap != null) {
                            view.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        }).start();

    }

    public static Bitmap load(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000 * 5);
            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                return null;
            }
            return BitmapFactory.decodeStream(conn.getInputStream());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}

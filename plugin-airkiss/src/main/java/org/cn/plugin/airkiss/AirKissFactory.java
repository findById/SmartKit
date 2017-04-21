package org.cn.plugin.airkiss;

import android.content.Context;

import org.cn.plugin.airkiss.vendor.Esptouch;

public class AirKissFactory {

    public static AirKiss create(Context ctx, String type) {
        switch (type) {
            case "esptouch": {
                return new Esptouch(ctx);
            }
            default:
                break;
        }
        return null;
    }

}

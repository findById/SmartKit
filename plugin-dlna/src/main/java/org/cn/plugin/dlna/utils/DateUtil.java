package org.cn.plugin.dlna.utils;

import java.text.SimpleDateFormat;

/**
 * Created by chenning on 17-1-17.
 */

public class DateUtil {
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String format(Object obj) {
        return format.format(obj);
    }
}

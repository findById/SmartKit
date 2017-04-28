package org.cn.plugin.dlna.model;

import org.cn.plugin.dlna.utils.DateUtil;

/**
 * Created by chenning on 2017/1/15.
 */

public class MediaInfo {
    public String id;
    public String name;
    public String path;
    public String updateTime;
    public String mode;
    public Long size;

    public String getUpdateTime() {
        return DateUtil.format((Long.parseLong(updateTime)));
    }

    public String getSize() {
        if (mode.charAt(0) == 100) {
            return "";
        }
        return format(size);
    }

    public static String format(long size) {
        if (size >> 20 < 1) {
            return String.format("%.2fKB", size / 1024.0);
        }
        if (size >> 30 < 1) {
            return String.format("%.2fMB", size / 1024.0 / 1024);
        }
        if (size >> 40 < 1) {
            return String.format("%.2fGB", size / 1024.0 / 1024 / 1024);
        }
        return String.format("%.2fKB", size / 1024.0);
    }

}

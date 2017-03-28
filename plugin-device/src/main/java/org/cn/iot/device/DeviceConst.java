package org.cn.iot.device;

import org.cn.plugin.common.optional.OptionalConst;
import org.cn.plugin.common.optional.OptionalManager;

public class DeviceConst {

    public static String API_HOST;

    public static void init() {
        API_HOST = OptionalManager.getString(OptionalConst.KEY_API_HOST, "");
    }

}

package org.cn.plugin.message;

import org.cn.plugin.common.optional.OptionalConst;
import org.cn.plugin.common.optional.OptionalManager;

public class MessageConst {
    public static String API_HOST;
    public static String host;
    public static String username;
    public static String password;

    public static final String ACTION_MESSAGE_ARRIVED = "action.message.arrived";
    public static final String EXTRA_MESSAGE_DATA = "extra.message.data";

    public static void init() {
        API_HOST = OptionalManager.getString(OptionalConst.KEY_API_HOST, "");
        host = OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_ADDR, "");
        username = OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_USERNAME, "");
        password = OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_PASSWORD, "");
    }

}

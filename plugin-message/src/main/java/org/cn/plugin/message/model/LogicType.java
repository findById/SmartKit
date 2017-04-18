package org.cn.plugin.message.model;

/**
 * Created by chenning on 17-4-18.
 */

public enum LogicType {
    RELAY("relay"),
    HT("HT");

    public String value;

    LogicType(String value) {
        this.value = value;
    }

}

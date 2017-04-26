package org.cn.plugin.message.model;

/**
 * Created by chenning on 17-4-18.
 */

public enum LogicType {
    RELAY("0"), // relay
    HT("1"); // temperature & humidity

    public String value;

    LogicType(String value) {
        this.value = value;
    }

}

package org.cn.plugin.message.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by chenning on 17-4-18.
 */

public class Logic {
    @JSONField(name = "p")
    public int pin;
    @JSONField(name = "t")
    public String type;
    @JSONField(name = "d")
    public String metadata;

    public void parseLogic(String text) {
        JSONObject obj = JSON.parseObject(text);
        this.pin = obj.getInteger("p");
        this.type = obj.getString("t");
        this.metadata = obj.getString("d");
    }
}

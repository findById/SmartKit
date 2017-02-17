package org.cn.plugin.message.model;

import java.io.Serializable;
import java.util.UUID;

import info.breezes.orm.annotation.Column;
import info.breezes.orm.annotation.Table;

/**
 * Created by chenning on 17-1-18.
 */

@Table(name = "iot_message")
public class Message implements Serializable {
    @Column(primaryKey = true, name = "id")
    public String id = UUID.randomUUID().toString();
    @Column(name = "producer_id")
    public String producerId;
    @Column(name = "consumer_id")
    public String consumerId;
    @Column(name = "msg_type")
    public String msgType = "text";
    @Column(name = "body")
    public String body;
    @Column(name = "update_time")
    public Long updateTime = System.currentTimeMillis();
    @Column(name = "read")
    public String read = "0";

    public Message() {
    }

    public Message(String producerId, String consumerId, String msgType, String body) {
        this.producerId = producerId;
        this.consumerId = consumerId;
        this.msgType = msgType;
        this.body = body;
    }
}

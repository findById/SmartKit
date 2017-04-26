package org.cn.iot.device.model;

import java.util.List;
import java.util.UUID;

import info.breezes.orm.annotation.Column;
import info.breezes.orm.annotation.Table;

/**
 * Created by chenning on 2016/8/24.
 */
@Table(name = "iot_device")
public class Device {
    @Column(primaryKey = true, name = "id")
    public String id = UUID.randomUUID().toString();
    @Column(name = "device_id")
    private String deviceId;
    @Column(name = "name")
    private String name;
    @Column(name = "model")
    private String model;
    @Column(name = "location")
    private String location;
    @Column(name = "type")
    private String type;
    @Column(name = "state")
    private String state = "enabled";
    @Column(name = "group_name")
    private String groupName;

    @Column(name = "description")
    private String description;
    @Column(name = "last_active_time")
    private String lastActiveTime;
    @Column(name = "parent_id")
    private String parentId;
    private List<DeviceItem> items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(String lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<DeviceItem> getItems() {
        return items;
    }

    public void setItems(List<DeviceItem> items) {
        this.items = items;
    }
}

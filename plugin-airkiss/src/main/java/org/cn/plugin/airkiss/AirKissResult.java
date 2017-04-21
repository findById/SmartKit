package org.cn.plugin.airkiss;

public class AirKissResult {
    public boolean isSuccess;
    public String bssid;
    public String address;

    public AirKissResult(boolean isSuccess) {
        this(isSuccess, null, null);
    }

    public AirKissResult(boolean isSuccess, String bssid, String address) {
        this.isSuccess = isSuccess;
        this.bssid = bssid;
        this.address = address;
    }
}

package com.shykad.yunke.sdk.okhttp.bean;

/**
 * Create by wanghong.he on 2019/2/28.
 * description：
 */
public class GetAdRequest {

    String os; //ios \android
    String type; //feed\ banner \splash \interstitial \wake-up \wake-up-strict
    String slotId;//广告位id
    String deviceNo;//IMEI 或 IDFA

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }
}

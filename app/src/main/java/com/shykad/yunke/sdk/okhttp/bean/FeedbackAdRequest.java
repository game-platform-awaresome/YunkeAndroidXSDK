package com.shykad.yunke.sdk.okhttp.bean;

/**
 * Create by wanghong.he on 2019/2/28.
 * description：
 */
public class FeedbackAdRequest {

    String id;//广告位id
    int type;//0 展示 1点击

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

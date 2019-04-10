package com.shykad.yunke.sdk.okhttp.bean;

/**
 * Create by wanghong.he on 2019/2/28.
 * description：!. 检查HTTP响应状态码是否为 200 ’. 检查接口响应状态码 code 是否为 0，0 为正常，非 0 则表示接口有误
 * 当 channel 值不为 0 时 根据appId 和 slotId参数调用腾迅或今日头条的广告
 */
public class GetAdResponse {

    int code;
    String message;
    AdCotent data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AdCotent getData() {
        return data;
    }

    public void setData(AdCotent data) {
        this.data = data;
    }

    public static class AdCotent{
        String id;//广告id
        String appId;//应用id
        String slotId;//广告位id
        int channel; // 0 为云克的广告 1 为腾迅的广告 2 为今日头条的广告
        String target;//点击广告后跳转url
        String title;//广告标题
        String icon;//广告图标
        String desc;//广告信息
        String src;//广告图片URL
        extData ext;//扩展参数

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getSlotId() {
            return slotId;
        }

        public void setSlotId(String slotId) {
            this.slotId = slotId;
        }

        public int getChannel() {
            return channel;
        }

        public void setChannel(int channel) {
            this.channel = channel;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public extData getExt() {
            return ext;
        }

        public void setExt(extData ext) {
            this.ext = ext;
        }
    }

    public static class extData{
        int width;
        int height;
    }
}

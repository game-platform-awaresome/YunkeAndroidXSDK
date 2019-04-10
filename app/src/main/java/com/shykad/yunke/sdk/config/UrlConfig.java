package com.shykad.yunke.sdk.config;

/**
 * Create by wanghong.he on 2019/2/28.
 * description：自有渠道接口链接
 */
public class UrlConfig {

    /**
     * 启动时获取应⽤用在腾迅/今⽇日头条平台的 AppId（今⽇日头条 sdk 需要）
     */
    public final static String GET_APPID = "/api/ad-setup";

    /**
     * 获取广告
     */
    public final static String GET_AD = "/api/ad-get";

    /**
     * 广告位展示/点击
     */
    public final static String FEEDBACK_AD = "/api/ad-feedback";
}

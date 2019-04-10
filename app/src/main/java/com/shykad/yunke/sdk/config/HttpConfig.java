package com.shykad.yunke.sdk.config;

import com.shykad.yunke.sdk.BuildConfig;

/**
 * Create by wanghong.he on 2019/2/28.
 * description：
 */
public class HttpConfig {

    /**
     * 开发环境
     */
    private static String BASE_URL_DEBUG = "http://39.105.165.155:8186";

    /**
     * 生产环境
     */
    private static String BASE_URL_RELEASE = "https://39.105.165.155:8186";

    /**
     * 系统标识
     */
    public static final String OS = "android";

    /**
     * 0 为成功，非0 则表示接口有误
     */
    public static final int CODE_SUCCESS = 0;

    /**
     * 0 为云克的广告
     */
    public static final int AD_CHANNEL_YUNKE = 0;

    /**
     * 1 为腾迅的广告
     */
    public static final int AD_CHANNEL_TENCENT = 1;

    /**
     * 2 为今日头条的广告
     */
    public static final int AD_CHANNEL_BYTEDANCE = 2;

    /**
     * 广告类型 feed
     */
    public static final String ADTYPE_FEED = "feed";

    /**
     * 广告类型 banner
     */
    public static final String ADTYPE_BANNER = "banner";

    /**
     * 广告类型 splash
     */
    public static final String ADTYPE_SPLASH = "splash";

    /**
     * 广告类型 interstitial
     */
    public static final String ADTYPE_INTERSTITIAL = "interstitial";

    /**
     * 广告类型 wake-up
     */
    public static final String ADTYPE_WAKE_UP = "wake-up";

    /**
     * 广告类型 wake-up-strict
     */
    public static final String ADTYPE_WAKE_UP_STRICE = "wake-up-strict";

    /**
     * 0 展示广告
     */
    public static final int AD_SHOW_YUNKE = 0;

    /**
     * 1 点击广告
     */
    public static final int AD_CLICK_YUNKE = 1;

    /**
     * Service
     */
    public static final String ACTION_BROADCAST_MSG = "com.phantom.plugin.component.action.BROADCAST_MSG";
    /**
     * 域名
     * @return 不同环境地址
     */
    public static String baseUrl() {
        if (BuildConfig.DEBUG) {
            return BASE_URL_DEBUG;
        } else {
            return BASE_URL_RELEASE;
        }
    }

}

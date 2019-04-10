package com.shykad.yunke.sdk.engine;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTInteractionAd;
import com.qq.e.ads.interstitial.InterstitialAD;
import com.qq.e.ads.interstitial.InterstitialADListener;
import com.qq.e.comm.util.AdError;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.manager.ShykadManager;
import com.shykad.yunke.sdk.manager.TTAdManagerHolder;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.ui.widget.YunkeInterstitialView;
import com.shykad.yunke.sdk.utils.LogUtils;
import com.shykad.yunke.sdk.utils.SPUtil;

/**
 * Create by wanghong.he on 2019/3/12.
 * description：插屏
 */
public class InterstitialEngine {

    private static Activity mContext;
    private Object response;
    private GetAdResponse.AdCotent adCotent;
    private InterstitialAdCallBack interstitialAdCallBack;
    private TTAdNative mTTAdNative;
    private InterstitialAD mTXAdNative;

    private static InterstitialEngine instance;

    private InterstitialEngine(){

    }

    public static InterstitialEngine getInstance(Activity context){
        mContext = context;
        if(instance == null){
            synchronized (InterstitialEngine.class){
                if(instance == null) instance = new InterstitialEngine();
            }
        }
        return instance;
    }

    public InterstitialEngine initEngine(Object response,InterstitialAdCallBack interstitialAdCallBack){
        this.response = response;
        this.interstitialAdCallBack = interstitialAdCallBack;

        //step2:创建TTAdNative对象,用于调用广告请求接口，createAdNative(Context context) 插屏广告context需要传入Activity对象
        mTTAdNative = TTAdManagerHolder.get().createAdNative(mContext);
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(mContext);
        return this;
    }


    /**
     * 加载插屏引擎
     */
    public InterstitialEngine launchInterstitial(int rootLayoutId){
        if (response instanceof GetAdResponse){
            adCotent = ((GetAdResponse) response).getData();
            flowOptimization(rootLayoutId);

        }
        return this;
    }

    /**
     * 流量转化:0 云客 1：腾讯 2：头条
     */
    private void flowOptimization(int rootLayoutId){
        int channel = ((GetAdResponse) response).getData().getChannel();
        switch (channel){
            case HttpConfig.AD_CHANNEL_YUNKE:
                showYunkeInterstitial(rootLayoutId);
                break;
            case HttpConfig.AD_CHANNEL_TENCENT:
                showTencentInterstitial(adCotent.getSlotId(), (String) SPUtil.get(mContext,SPUtil.TX_APPID,adCotent.getAppId()));
                break;
            case HttpConfig.AD_CHANNEL_BYTEDANCE:
                showByteDanceInterstitial(adCotent.getSlotId());
                break;
            default:
                break;
        }
    }

    /**
     * 云客插屏
     * @param rootLayoutId
     */
    private void showYunkeInterstitial(int rootLayoutId) {
        new YunkeInterstitialView(mContext,adCotent.getId(),adCotent, new YunkeInterstitialView.InterstitialCallBack() {

            @Override
            public void onAdShow(YunkeInterstitialView interstitialView) {
                interstitialView.showInterstitial(rootLayoutId);
                if (interstitialAdCallBack!=null){
                    interstitialAdCallBack.onInterstitialShow();
                }
                LogUtils.d("shykad","插屏广告展示");
            }

            @Override
            public void onAdError(String err) {
                if (interstitialAdCallBack!=null){
                    interstitialAdCallBack.onInterstitialErroe(err);
                }
                LogUtils.d("shykad","插屏广告异常："+err);
            }

            @Override
            public void onAdClicked(YunkeInterstitialView interstitialView) {
                interstitialView.dismiss();
                if (interstitialAdCallBack!=null){
                    interstitialAdCallBack.onInterstitialClick(true);
                }
                LogUtils.d("shykad","关闭广告点击");
            }

            @Override
            public void onAdClose() {
                if (interstitialAdCallBack!=null){
                    interstitialAdCallBack.onInterstitialClose();
                }
                LogUtils.d("shykad","关闭广告展示");
            }
        });
    }

    /**
     * 腾讯插屏:由于是单例模式 为了封装调用正常 本类也必须时单例模式！！！
     */
    private void showTencentInterstitial(String posId,String appId){
        getIAD(posId,appId).setADListener(new InterstitialADListener() {

            /**
             * 广告加载失败，error 对象包含了错误码和错误信息
             * @param error
             */
            @Override
            public void onNoAD(AdError error) {
                String err = String.format("LoadInterstitialAd Fail, error code: %d, error msg: %s",error.getErrorCode(), error.getErrorMsg());

                if (interstitialAdCallBack!=null){
                    interstitialAdCallBack.onInterstitialErroe(err);
                }
                LogUtils.d("yunke-gdt", "插屏广告异常："+err);
            }

            /**
             * 插屏广告展开时回调
             */
            @Override
            public void onADOpened() {
                LogUtils.d("yunke-gdt", " 插屏广告展开");
            }

            /**
             * 插屏广告曝光时回调
             */
            @Override
            public void onADExposure() {
                LogUtils.d("yunke-gdt", "插屏广告曝光");
            }

            /**
             * 插屏广告点击时回调
             */
            @Override
            public void onADClicked() {
                clickAdTask();

                LogUtils.d("yunke-gdt", "插屏广告点击");
            }

            /**
             * 插屏广告点击离开应用时回调
             */
            @Override
            public void onADLeftApplication() {
                LogUtils.d("yunke-gdt", " 插屏广告点击离开应用");
            }

            /**
             * 插屏广告关闭时回调
             */
            @Override
            public void onADClosed() {
                if (interstitialAdCallBack!=null){
                    interstitialAdCallBack.onInterstitialClose();
                }
                LogUtils.d("yunke-gdt", "插屏广告消失");
            }

            /**
             * 插屏广告加载完毕，此回调后才可以调用 show 方法
             */
            @Override
            public void onADReceive() {
                mTXAdNative.show();
                showAdTask();

                LogUtils.d("yunke-gdt", "插屏广告展示");
            }
        });
        if (mTXAdNative!=null){
            mTXAdNative.loadAD();
        }

    }

    private InterstitialAD getIAD(String posId,String appId) {
        if (mTXAdNative != null && !TextUtils.isEmpty(posId)) {
            return mTXAdNative;
        }
        if (this.mTXAdNative != null) {
            mTXAdNative.closePopupWindow();
            mTXAdNative.destroy();
            mTXAdNative = null;
        }
        if (mTXAdNative == null) {
            mTXAdNative = new InterstitialAD(mContext, appId, posId);
        }
        return mTXAdNative;
    }

    public InterstitialEngine destoryIntertitalAd(){
        if (mTXAdNative!=null){
            mTXAdNative.closePopupWindow();
            mTXAdNative.destroy();
            mTXAdNative = null;
        }
        return this;
    }

    /**
     * 头条插屏
     * @param codeId
     */
    private void showByteDanceInterstitial(String codeId){
        //step4:创建插屏广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(600, 600) //根据广告平台选择的尺寸，传入同比例尺寸
                .build();
        //step5:请求广告，调用插屏广告异步请求接口
        mTTAdNative.loadInteractionAd(adSlot, new TTAdNative.InteractionAdListener() {
            @Override
            public void onError(int code, String message) {
                if (interstitialAdCallBack!=null){
                    interstitialAdCallBack.onInterstitialErroe("code: " + code + "  message: " + message);
                }
                LogUtils.d("yunke-csj","code: " + code + "  message: " + message);
            }

            @Override
            public void onInteractionAdLoad(TTInteractionAd ttInteractionAd) {
                LogUtils.d("yunke-csj", "type:  " + ttInteractionAd.getInteractionType());
                ttInteractionAd.setAdInteractionListener(new TTInteractionAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked() {
                        clickAdTask();
                        LogUtils.d("yunke-csj", "插屏广告被点击");
                    }

                    @Override
                    public void onAdShow() {
                        showAdTask();
                        LogUtils.d("yunke-csj", "插屏广告被展示");
                    }

                    @Override
                    public void onAdDismiss() {
                        if (interstitialAdCallBack!=null){
                            interstitialAdCallBack.onInterstitialClose();
                        }
                        LogUtils.d("yunke-csj", "插屏广告消失");
                    }
                });
                //如果是下载类型的广告，可以注册下载状态回调监听
                if (ttInteractionAd.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
                    ttInteractionAd.setDownloadListener(new TTAppDownloadListener() {
                        @Override
                        public void onIdle() {
                            LogUtils.d("yunke-csj","点击开始下载");
                        }

                        @Override
                        public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                            LogUtils.d("yunke-csj","下载中");
                        }

                        @Override
                        public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                            LogUtils.d("yunke-csj","下载暂停");
                        }

                        @Override
                        public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                            LogUtils.d("yunke-csj","下载失败");
                        }

                        @Override
                        public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                            LogUtils.d("yunke-csj","下载完成");
                        }

                        @Override
                        public void onInstalled(String fileName, String appName) {
                            LogUtils.d("yunke-csj", "安装完成");
                        }
                    });
                }
                //弹出插屏广告
                ttInteractionAd.showInteractionAd(mContext);
            }
        });
    }

    /**
     * 展示广告(腾讯 头条--feedback)
     */
    private void showAdTask(){
        ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                (new Handler(Looper.getMainLooper())).post(new Runnable() {

                    @Override
                    public void run() {// TODO: 2019/3/7 埋点

                        YunKeEngine.getInstance(mContext).yunkeFeedAd(adCotent.getId(), HttpConfig.AD_SHOW_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {

                                if (interstitialAdCallBack!=null){
                                    interstitialAdCallBack.onInterstitialShow();
                                }

                            }

                            @Override
                            public void feedAdFailed(String err) {
                                if (interstitialAdCallBack!=null){
                                    interstitialAdCallBack.onInterstitialErroe(err);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 点击广告(腾讯 头条--feedback)
     */
    private void clickAdTask() {
        ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                (new Handler(Looper.getMainLooper())).post(new Runnable() {

                    @Override
                    public void run() {// TODO: 2019/3/7 埋点

                        YunKeEngine.getInstance(mContext).yunkeFeedAd(adCotent.getId(), HttpConfig.AD_CLICK_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {
                                if (interstitialAdCallBack!=null){
                                    interstitialAdCallBack.onInterstitialClick(false);
                                }

                            }

                            @Override
                            public void feedAdFailed(String err) {
                                if (interstitialAdCallBack!=null){
                                    interstitialAdCallBack.onInterstitialErroe(err);
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    public interface InterstitialAdCallBack{
        void onInterstitialClick(boolean isJump);
        void onInterstitialShow();
        void onInterstitialErroe(String err);
        void onInterstitialClose();
    }
}

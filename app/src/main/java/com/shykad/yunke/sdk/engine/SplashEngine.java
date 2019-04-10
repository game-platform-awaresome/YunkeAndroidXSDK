package com.shykad.yunke.sdk.engine;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.manager.ShykadManager;
import com.shykad.yunke.sdk.manager.TTAdManagerHolder;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.ui.widget.YunkeSplashView;
import com.shykad.yunke.sdk.utils.BaseRealVisibleUtil;
import com.shykad.yunke.sdk.utils.LogUtils;
import com.shykad.yunke.sdk.utils.SPUtil;

import androidx.annotation.MainThread;

/**
 * Create by wanghong.he on 2019/3/12.
 * description：开屏
 */
public class SplashEngine {

    private Activity context;
    private Object response;
    private GetAdResponse.AdCotent adCotent;
    private SplashAdCallBack splashAdCallBack;
    private TTAdNative mTTAdNative;
    private SplashAD splashAD;

    //开屏广告加载超时时间,建议大于1000,这里为了冷启动第一次加载到广告并且展示,示例设置了2000ms
    private static final int AD_TIME_OUT = 2000;
    /**
     * 为防止无广告时造成视觉上类似于"闪退"的情况，设定无广告时页面跳转根据需要延迟一定时间，demo
     * 给出的延时逻辑是从拉取广告开始算开屏最少持续多久，仅供参考，开发者可自定义延时逻辑，如果开发者采用demo
     * 中给出的延时逻辑，也建议开发者考虑自定义minSplashTimeWhenNoAD的值（单位ms）
     **/
    private int minSplashTimeWhenNoAD = 2000;
    /**
     * 记录拉取广告的时间
     */
    private long fetchSplashADTime = 0;
    private static final String SKIP_TEXT = "点击跳过 %d";
    private Handler handler = new Handler(Looper.getMainLooper());

    public SplashEngine(Activity context,Object response,SplashAdCallBack splashAdCallBack){
        this.context = context;
        this.response = response;
        this.splashAdCallBack = splashAdCallBack;
    }

    public SplashEngine initSplash(){
        //头条
        //step2:创建TTAdNative对象
        mTTAdNative = TTAdManagerHolder.get().createAdNative(context);
        //在合适的时机申请权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题
        //在开屏时候申请不太合适，因为该页面倒计时结束或者请求超时会跳转，在该页面申请权限，体验不好
        TTAdManagerHolder.get().requestPermissionIfNecessary(context);
        return this;
    }

    /**
     * 加载开屏页
     */
    public void launchSplash(YunkeSplashView splashView,ViewGroup splashContainer,ImageView splashContainerView, TextView skipView,int countDownTime){
        if (response instanceof GetAdResponse){
            adCotent = ((GetAdResponse) response).getData();
            flowOptimization(splashView,splashContainer,splashContainerView,skipView,countDownTime);

        }

    }

    /**
     * 流量转化:0 云客 1：腾讯 2：头条
     */
    private void flowOptimization(YunkeSplashView splashView, ViewGroup splashContainer,ImageView splashContainerView, TextView skipView,int countDownTime){
        int channel = ((GetAdResponse) response).getData().getChannel();
        switch (channel){
            case HttpConfig.AD_CHANNEL_YUNKE:
                showYunkeSplash(splashView,splashContainer,splashContainerView,skipView,countDownTime);
                break;
            case HttpConfig.AD_CHANNEL_TENCENT:
                showTencentSplash(splashContainer,skipView,(String) SPUtil.get(context, SPUtil.TX_APPID,((GetAdResponse) response).getData().getAppId()), adCotent.getSlotId(),countDownTime);
                break;
            case HttpConfig.AD_CHANNEL_BYTEDANCE:
                showByteDanceSplash(adCotent.getSlotId(),splashContainer,skipView);
                break;
            default:
                break;
        }
    }

    /**
     * 云客 splash广告
     * @param splashView
     * @param splashContainer
     * @param splashContainerView
     * @param skipView
     * @param countDownTime
     */
    private void showYunkeSplash(YunkeSplashView splashView, ViewGroup splashContainer, ImageView splashContainerView, TextView skipView,int countDownTime) {
        skipView.setVisibility(View.VISIBLE);

        splashView.lanuchSplash(context,splashContainer,splashContainerView, skipView, Long.valueOf(countDownTime),
                adCotent.getId(), adCotent.getAppId(),adCotent, new YunkeSplashView.SplashADCallBack() {
                    @Override
                    public void onAdPresent() {
                        LogUtils.d("shykad","广告数据已加载");
                        if (splashAdCallBack!=null){
                            splashAdCallBack.onSplashPresent();
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        LogUtils.d("shykad","点击广告");
                        if (splashAdCallBack!=null){
                            splashAdCallBack.onSplashClick(true);
                        }
                    }

                    @Override
                    public void onAdShow() {
                        LogUtils.d("shykad","展示广告");
                        if (splashAdCallBack!=null){
                            splashAdCallBack.onSplashShow();
                        }
                    }

                    @Override
                    public void onAdSkip() {
                        LogUtils.d("shykad","展示广告,点击跳过");
                        if (splashAdCallBack!=null){
                            splashAdCallBack.onSplashSkip();
                        }
                    }

                    @Override
                    public void onAdTimeOver() {
                        LogUtils.d("shykad","展示广告,倒计时结束");
                        if (splashAdCallBack!=null){
                            splashAdCallBack.onSplashTimeOver();
                        }
                    }

                    @Override
                    public void onAdError(String err) {
                        LogUtils.d("shykad","展示广告异常");
                        if (splashAdCallBack!=null){
                            splashAdCallBack.onSplashErroe(err,false);
                        }
                    }
                });

    }

    /**
     * 腾讯 splash广告: 切记拉起之前请求权限，拉取开屏广告，开屏广告的构造方法有3种，详细说明请参考开发者文档。
     *
     * @param splashContainer 展示广告的大容器
     * @param skipView        自定义的跳过按钮：传入该view给SDK后，SDK会自动给它绑定点击跳过事件。SkipView的样式可以由开发者自由定制，其尺寸限制请参考activity_splash.xml或者接入文档中的说明。
     * @param appId           应用ID
     * @param posId           广告位ID
     * @param //SListener     广告状态监听器
     * @param countDownTime   拉取广告的超时时长：取值范围[3000, 5000]，设为0表示使用广点通SDK默认的超时时长。
     */
    private void showTencentSplash(ViewGroup splashContainer, TextView skipView, String appId, String posId, int countDownTime){
        skipView.setVisibility(View.VISIBLE);
        fetchSplashADTime = System.currentTimeMillis();
        splashAD = new SplashAD(context, splashContainer, skipView, appId, posId, new SplashADListener() {
            /**
             * 广告关闭时调用，可能是用户关闭或者展示时间到。
             */
            @Override
            public void onADDismissed() {
                LogUtils.d("shykad-gdt", "SplashADDismissed");
                long alreadyDelayMills = System.currentTimeMillis() - fetchSplashADTime;//从拉广告开始到onNoAD已经消耗了多少时间

                if (splashAdCallBack!=null){
                    if (alreadyDelayMills > Long.valueOf(countDownTime)){
                        splashAdCallBack.onSplashTimeOver();
                    }else {
                        splashAdCallBack.onSplashSkip();
                    }
                    handler.removeCallbacksAndMessages(null);
                }
            }

            @Override
            public void onNoAD(AdError error) {
                String adError = String.format("LoadSplashADFail, eCode=%d, errorMsg=%s", error.getErrorCode(),error.getErrorMsg());
                LogUtils.d("shykad-gdt",adError);

                /**
                 * 为防止无广告时造成视觉上类似于"闪退"的情况，设定无广告时页面跳转根据需要延迟一定时间，demo
                 * 给出的延时逻辑是从拉取广告开始算开屏最少持续多久，仅供参考，开发者可自定义延时逻辑，如果开发者采用demo
                 * 中给出的延时逻辑，也建议开发者考虑自定义minSplashTimeWhenNoAD的值
                 **/
                long alreadyDelayMills = System.currentTimeMillis() - fetchSplashADTime;//从拉广告开始到onNoAD已经消耗了多少时间
                long shouldDelayMills = alreadyDelayMills > minSplashTimeWhenNoAD ? 0 : minSplashTimeWhenNoAD
                        - alreadyDelayMills;//为防止加载广告失败后立刻跳离开屏可能造成的视觉上类似于"闪退"的情况，根据设置的minSplashTimeWhenNoAD
                // 计算出还需要延时多久
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (splashAdCallBack!=null){
                            splashAdCallBack.onSplashErroe(adError,true);
                        }
                    }
                }, shouldDelayMills);
            }

            /**
             * 广告成功展示时调用，成功展示不等于有效展示（比如广告容器高度不够）
             */
            @Override
            public void onADPresent() {
                LogUtils.d("shykad-gdt", "SplashADPresent");
                if (splashAdCallBack!=null){
                    splashAdCallBack.onSplashPresent();
                }
            }

            @Override
            public void onADClicked() {
                LogUtils.d("shykad-gdt", "SplashADClicked");
                clickAdTask();
            }

            /**
             * 倒计时回调，返回广告还将被展示的剩余时间。
             * 通过这个接口，开发者可以自行决定是否显示倒计时提示，或者还剩几秒的时候显示倒计时
             *
             * @param millisUntilFinished 剩余毫秒数
             */
            @Override
            public void onADTick(long millisUntilFinished) {
                LogUtils.d("shykad-gdt", "SplashADTick " + millisUntilFinished + "ms");
                skipView.setText(String.format(SKIP_TEXT, Math.round(millisUntilFinished / 1000f)));
            }

            /**
             * 广告曝光时调用，此处的曝光不等于有效曝光（如展示时长未满足）
             */
            @Override
            public void onADExposure() {
                LogUtils.d("shykad-gdt", "SplashADExposure");
                showAdTask();
            }
        }, countDownTime);
    }

    /**
     * 头条 splash广告
     * @param codeId
     * @param splashContainer
     */
    private void showByteDanceSplash(String codeId,ViewGroup splashContainer,TextView skipView){

        skipView.setVisibility(View.GONE);
        if (BaseRealVisibleUtil.getInstance(context).isViewCovered(skipView)){
            if (splashAdCallBack!=null){
                splashAdCallBack.onSplashPresent();
            }
        }
        //step3:创建开屏广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .build();
        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            @MainThread
            public void onError(int code, String message) {
                LogUtils.d("shykad-csj", message);
                if (splashAdCallBack!=null){
                    splashAdCallBack.onSplashErroe("code:"+code+"   message:"+message,false);
                }
            }

            @Override
            @MainThread
            public void onTimeout() {
                LogUtils.d("shykad-csj","开屏广告加载超时");
                if (splashAdCallBack!=null){
                    splashAdCallBack.onSplashErroe("time out",true);
                }
            }

            @Override
            @MainThread
            public void onSplashAdLoad(TTSplashAd ad) {
                LogUtils.d("shykad-csj","开屏广告请求成功");
                if (ad == null) {
                    return;
                }
                //获取SplashView
                View view = ad.getSplashView();
                splashContainer.removeAllViews();
                //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽
                splashContainer.addView(view);
                //设置不开启开屏广告倒计时功能以及不显示跳过按钮,如果这么设置，您需要自定义倒计时逻辑
                //ad.setNotAllowSdkCountdown();

                //设置SplashView的交互监听器
                ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int type) {
                        LogUtils.d("shykad-csj", "开屏广告点击");
                        clickAdTask();
                    }

                    @Override
                    public void onAdShow(View view, int type) {
                        LogUtils.d("shykad-csj", "开屏广告展示");
                        showAdTask();
                    }

                    @Override
                    public void onAdSkip() {
                        LogUtils.d("shykad-csj", "开屏广告跳过");
                        if (splashAdCallBack!=null){
                            splashAdCallBack.onSplashSkip();
                        }

                    }

                    @Override
                    public void onAdTimeOver() {
                        LogUtils.d("shykad-csj", "开屏广告倒计时结束");
                        if (splashAdCallBack!=null){
                            splashAdCallBack.onSplashTimeOver();
                        }
                    }
                });
            }
        }, AD_TIME_OUT);
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

                        YunKeEngine.getInstance(context).yunkeFeedAd(adCotent.getId(), HttpConfig.AD_SHOW_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {

                                if (splashAdCallBack!=null){
                                    splashAdCallBack.onSplashShow();
                                }

                            }

                            @Override
                            public void feedAdFailed(String err) {
                                if (splashAdCallBack!=null){
                                    splashAdCallBack.onSplashErroe(err,true);
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

                        YunKeEngine.getInstance(context).yunkeFeedAd(adCotent.getId(), HttpConfig.AD_CLICK_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {

                                if (splashAdCallBack!=null){//头条、腾讯自身存在webview内部跳转
                                    splashAdCallBack.onSplashClick(false);
                                }
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                if (splashAdCallBack!=null){
                                    splashAdCallBack.onSplashErroe(err,true);
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    public interface SplashAdCallBack{
        void onSplashClick(boolean isJump);
        void onSplashSkip();
        void onSplashTimeOver();
        void onSplashErroe(String err,boolean isTimeOut);
        void onSplashShow();
        void onSplashPresent();
    }
}

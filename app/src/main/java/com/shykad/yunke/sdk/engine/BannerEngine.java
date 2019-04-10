package com.shykad.yunke.sdk.engine;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTBannerAd;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.BannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.comm.util.AdError;
import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.manager.ShykadManager;
import com.shykad.yunke.sdk.manager.TTAdManagerHolder;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.ui.widget.GlidImageManager;
import com.shykad.yunke.sdk.ui.widget.banner.YunKeBanner;
import com.shykad.yunke.sdk.utils.LogUtils;
import com.shykad.yunke.sdk.utils.SPUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Create by wanghong.he on 2019/3/11.
 * description：banner
 */
public class BannerEngine {

    private Activity context;
    private Object response;
    private GetAdResponse.AdCotent adCotent;
    private BannerAdCallBack bannerAdCallBack;
    private TTAdNative mTTAdNative;
    private BannerView mTXAdNative;

    public BannerEngine(Object response, Activity context, BannerAdCallBack bannerAdCallBack){
        this.response = response;
        this.context = context;
        this.bannerAdCallBack = bannerAdCallBack;
    }

    public BannerEngine initBanner(){
        //头条
        //step2:创建TTAdNative对象，createAdNative(Context context) banner广告context需要传入Activity对象
        mTTAdNative = TTAdManagerHolder.get().createAdNative(context);
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(context);
        return this;
    }

    /**
     * 广告轮播样式 可直接使用YunkeBannerVew 在布局中使用（去掉外部的Fragmentlayout）
     */
    public void launchBanner(YunKeBanner bannerView,ViewGroup bannerContainer,int rotationTime){
        if (response instanceof GetAdResponse){
            adCotent = ((GetAdResponse) response).getData();
            flowOptimization(bannerView,bannerContainer,rotationTime);

        }

    }

    /**
     * 流量转化:0 云客 1：腾讯 2：头条
     */
    private void flowOptimization(YunKeBanner bannerView,ViewGroup bannerContainer,int rotationTime){
        int channel = ((GetAdResponse) response).getData().getChannel();
        switch (channel){
            case HttpConfig.AD_CHANNEL_YUNKE:
                showYunkeBanner(bannerView,bannerContainer,rotationTime);
                break;
            case HttpConfig.AD_CHANNEL_TENCENT:
                showTencentBanner(adCotent.getSlotId(), (String) SPUtil.get(context,SPUtil.TX_APPID,((GetAdResponse) response).getData().getAppId()),bannerContainer,rotationTime);
                break;
            case HttpConfig.AD_CHANNEL_BYTEDANCE:
                showByteDanceBanner(adCotent.getSlotId(),mTTAdNative,bannerContainer,rotationTime);
                break;
            default:
                break;
        }
    }

    /**
     * 云客banner
     * @param bannerView
     * @param bannerContainer
     * @param rotationTime
     */
    private void showYunkeBanner(YunKeBanner bannerView, ViewGroup bannerContainer,int rotationTime){
        List<String> bannerList = new ArrayList<>();
        bannerList.add(adCotent.getSrc());
        bannerView.lanchBanner(bannerList,null)
                .setAdId(adCotent.getId())
                .setShowCancel(true)
                .setAutoPlayInterval(rotationTime)//时长
                .setAutoPlayAble(true)//自动播放
                .setBannerDataListener(new YunKeBanner.BannerDataCallBack<ImageView, String>() {
                    @Override
                    public void onBannerData(YunKeBanner banner, ImageView itemView, @Nullable String model, int position) {
                        GlidImageManager.getInstance().loadImageView(context, model, itemView, R.drawable.yunke_ic_default_image);
                        LogUtils.d("shykad","onBannerData ready Show");
                        if (bannerAdCallBack!=null){
                            bannerAdCallBack.onBannerShow();
                        }

                    }

                    @Override
                    public void onBannerDataError(String error) {
                        LogUtils.d("shykad","onBannerError"+error);
                    }
                })
                .setBannerClickListener(new YunKeBanner.BannerClickCallBack<ImageView, String>() {
                    @Override
                    public void onBannerItemClick(YunKeBanner banner, ImageView itemView, @Nullable String model, int position) {
                        LogUtils.d("shykad","onBannerClick");
                        if (bannerAdCallBack!=null){
                            bannerAdCallBack.onBannerClick(true);
                        }


                    }

                    @Override
                    public void onBannerItemError(String error) {
                        LogUtils.d("shykad","onBannerItemError----"+error);
                        if (bannerAdCallBack!=null){
                            bannerAdCallBack.onBannerErroe(error);
                        }

                    }
                }).setCancelClick(new YunKeBanner.YunKeCancleCallBack() {
                    @Override
                    public void cancelClick() {
                        bannerContainer.removeAllViews();
                        LogUtils.d("shykad","onBannerClose");
                    }
                });
        bannerContainer.removeAllViews();
        bannerContainer.addView(bannerView);
    }

    /**
     * 腾讯banner
     * @param posId
     * @param appId
     * @param bannerContainer
     * @param rotationTime
     */
    private void showTencentBanner(String posId,String appId,ViewGroup bannerContainer,int rotationTime){
        getBanner(posId,appId,bannerContainer,rotationTime).loadAD();
    }

    private BannerView getBanner(String posId,String appId,ViewGroup bannerContainer,int rotationTime) {
        if( this.mTXAdNative != null && !TextUtils.isEmpty(posId)) {
            return this.mTXAdNative;
        }
        if(this.mTXAdNative != null){
            bannerContainer.removeView(mTXAdNative);
            mTXAdNative.destroy();
        }
        this.mTXAdNative = new BannerView(context, ADSize.BANNER, appId,posId);
        // 注意：如果开发者的banner不是始终展示在屏幕中的话，请关闭自动刷新，否则将导致曝光率过低。
        // 并且应该自行处理：当banner广告区域出现在屏幕后，再手动loadAD。
        mTXAdNative.setRefresh(rotationTime/1000);
        mTXAdNative.setShowClose(true);
        mTXAdNative.setADListener(new BannerADListener() {

            /**
             * 广告加载失败
             * @param error 包含了错误码和错误信息
             */
            @Override
            public void onNoAD(AdError error) {
                String err = String.format("Banner onNoAD，eCode = %d, eMsg = %s", error.getErrorCode(),error.getErrorMsg());
                LogUtils.i("shykad-gdt","Banner广告:"+err);
                if (bannerAdCallBack!=null){
                    bannerAdCallBack.onBannerErroe(error.getErrorMsg());
                }
            }

            /**
             * 广告加载成功回调，表示广告相关的资源已经加载完毕，Ready To Show
             */
            @Override
            public void onADReceiv() {
                LogUtils.i("shykad-gdt", "Banner广告加载");
            }

            /**
             * 当广告曝光时发起的回调
             */
            @Override
            public void onADExposure() {
                LogUtils.i("shykad-gdt", "Banner广告曝光");
                showAdTask();
            }

            /**
             * 当广告关闭时调用，只有在使用了 Banner 广告自身的关闭按钮时生效
             */
            @Override
            public void onADClosed() {
                LogUtils.i("shykad-gdt", "Banner广告关闭");
            }

            /**
             * 当广告点击时发起的回调，由于点击去重等原因可能和联盟平台最终的统计数据有差异
             */
            @Override
            public void onADClicked() {
                LogUtils.i("shykad-gdt", "Banner广告点击");

                clickAdTask();
            }

            /**
             * 由于广告点击离开 APP 时调用
             */
            @Override
            public void onADLeftApplication() {
                LogUtils.i("shykad-gdt", "离开Banner广告");
            }

            /**
             * 当广告打开浮层时调用，如打开内置浏览器、内容展示浮层，一般发生在点击之后
             */
            @Override
            public void onADOpenOverlay() {
                LogUtils.i("shykad-gdt", "Banner广告打开浮层");
            }

            /**
             * 浮层关闭时调用
             */
            @Override
            public void onADCloseOverlay() {
                LogUtils.i("shykad-gdt", "Banner广告关闭浮层");
            }
        });
        bannerContainer.addView(mTXAdNative);
        return this.mTXAdNative;
    }

    /**
     * 头条banner
     * @param codeId
     * @param mTTAdNative
     * @param bannerContainer
     * @param rotationTime
     */
    private void showByteDanceBanner(String codeId,TTAdNative mTTAdNative,ViewGroup bannerContainer,int rotationTime){
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setSupportDeepLink(true)
                .setImageAcceptedSize(600, 257)
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadBannerAd(adSlot, new TTAdNative.BannerAdListener() {

            @Override
            public void onError(int code, String message) {
                bannerContainer.removeAllViews();
                if (bannerAdCallBack!=null){
                    bannerAdCallBack.onBannerErroe(message+" "+code);
                    LogUtils.d("shykad-csj","Banner广告异常："+message+" "+code);
                }
            }

            @Override
            public void onBannerAdLoad(final TTBannerAd ad) {
                if (ad == null) {
                    return;
                }
                View bannerView = ad.getBannerView();
                if (bannerView == null) {
                    return;
                }
                //设置轮播的时间间隔  间隔在30s到120秒之间的值，不设置默认不轮播
                ad.setSlideIntervalTime(rotationTime);
                bannerContainer.removeAllViews();
                bannerContainer.addView(bannerView);
                //设置广告互动监听回调
                ad.setBannerInteractionListener(new TTBannerAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int type) {
                        clickAdTask();
                        LogUtils.d("shykad-csj","Banner广告被点击");
                    }

                    @Override
                    public void onAdShow(View view, int type) {
                        showAdTask();
                        LogUtils.d("shykad-csj","Banner广告展示");
                    }
                });
                //（可选）设置下载类广告的下载监听
                bindDownloadListener(ad);
                //在banner中显示网盟提供的dislike icon，有助于广告投放精准度提升
                ad.setShowDislikeIcon(new TTAdDislike.DislikeInteractionCallback() {
                    @Override
                    public void onSelected(int position, String value) {
                        LogUtils.d("shykad-csj","Banner点击 " + value);
                        //用户选择不喜欢原因后，移除广告展示
                        bannerContainer.removeAllViews();
                    }

                    @Override
                    public void onCancel() {
                        LogUtils.d("shykad-csj","Banner点击关闭");
                    }
                });
            }
        });
    }

    private boolean mHasShowDownloadActive = false;

    private void bindDownloadListener(TTBannerAd ad) {
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                LogUtils.d("点击图片开始下载");
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    LogUtils.d("下载中，点击图片暂停");
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                LogUtils.d("下载暂停，点击图片继续");
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                LogUtils.d("下载失败，点击图片重新下载");
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                LogUtils.d("安装完成，点击图片打开");
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                LogUtils.d("点击安装");
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

                        YunKeEngine.getInstance(context).yunkeFeedAd(adCotent.getId(), HttpConfig.AD_SHOW_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {

                                if (bannerAdCallBack!=null){
                                    bannerAdCallBack.onBannerShow();
                                }

                            }

                            @Override
                            public void feedAdFailed(String err) {
                                if (bannerAdCallBack!=null){
                                    bannerAdCallBack.onBannerErroe(err);
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

                                if (bannerAdCallBack!=null){//头条、腾讯自身存在webview内部挑战
                                    bannerAdCallBack.onBannerClick(false);
                                }
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                if (bannerAdCallBack!=null){
                                    bannerAdCallBack.onBannerErroe(err);
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    public interface BannerAdCallBack{
        void onBannerClick(boolean isJump);
        void onBannerErroe(String err);
        void onBannerShow();
    }

}

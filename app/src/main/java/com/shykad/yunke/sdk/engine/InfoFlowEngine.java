package com.shykad.yunke.sdk.engine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.callback.AQuery2;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.nativ.NativeExpressMediaListener;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.pi.AdData;
import com.qq.e.comm.util.AdError;
import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.manager.ShykadManager;
import com.shykad.yunke.sdk.manager.TTAdManagerHolder;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.ui.widget.YunkeTemplateView;
import com.shykad.yunke.sdk.utils.LogUtils;
import com.shykad.yunke.sdk.utils.SPUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Create by wanghong.he on 2019/3/20.
 * description：信息流广告
 */
public class InfoFlowEngine {

    private InfoFlowAdCallBack infoFlowAdCallBack;
    private Object response;
    private GetAdResponse.AdCotent adCotent;
    private static Activity mContext;
    private static InfoFlowEngine instance;
    private NativeExpressAD mADManager;
    private YunkeTemplateView templateView;
    private TTAdNative mTTAdNative;
    private AQuery2 mAQuery;
    private Map<AdViewHolder, TTAppDownloadListener> mTTAppDownloadListenerMap = new WeakHashMap<>();

    private InfoFlowEngine(){

    }

    public static InfoFlowEngine getInstance(Activity context){
        mContext = context;
        if(instance == null){
            synchronized (InfoFlowEngine.class){
                if(instance == null) instance = new InfoFlowEngine();
            }
        }
        return instance;
    }

    /**
     * 初始化原生广告引擎
     * @param response
     * @param infoFlowAdCallBack
     * @return InfoFlowEngine
     */
    public InfoFlowEngine initEngine(Object response, InfoFlowAdCallBack infoFlowAdCallBack){
        this.response = response;
        this.infoFlowAdCallBack = infoFlowAdCallBack;
        templateView = new YunkeTemplateView(mContext);

        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = ttAdManager.createAdNative(mContext.getApplicationContext());
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(mContext);
        mAQuery = new AQuery2(mContext);
        return this;
    }

    /**
     * 加载原生广告引擎
     */
    public InfoFlowEngine launchInfoFlow(ViewGroup infoFlowContainer,int adCount){
        if (response instanceof GetAdResponse){
            adCotent = ((GetAdResponse) response).getData();
            flowOptimization(infoFlowContainer,adCount);
        }
        return this;
    }

    /**
     * 流量转化:0 云客 1：腾讯 2：头条
     */
    private void flowOptimization(ViewGroup infoFlowContainer,int adCount){
        int channel = ((GetAdResponse) response).getData().getChannel();
        switch (channel){
            case HttpConfig.AD_CHANNEL_YUNKE:
                showYunkeInfoFlow(infoFlowContainer,adCount);
                break;
            case HttpConfig.AD_CHANNEL_TENCENT:
                showTencentInfoFlow(adCotent.getSlotId(), (String) SPUtil.get(mContext,SPUtil.TX_APPID,adCotent.getAppId()),adCount);
                break;
            case HttpConfig.AD_CHANNEL_BYTEDANCE:
                showByteDanceInfoFlow(infoFlowContainer,adCotent.getSlotId(),adCount);
                break;
            default:
                break;
        }
    }

    /**
     * 云克信息流广告
     */
    private void showYunkeInfoFlow(ViewGroup infoFlowContainer,int adCount){
        templateView.setTemplateTitle(adCotent.getTitle())
                .setTemplateDesc(adCotent.getDesc())
                .setTemplateCancel(mContext.getResources().getDrawable(R.drawable.yunke_dislike_icon))
                .lanchTemplate(infoFlowContainer, response, new YunkeTemplateView.TemplateViewCallBack() {
                    @Override
                    public void onAdClicked(YunkeTemplateView templateView) {
                        LogUtils.d("shykad","模板广告点击");
                        if (infoFlowAdCallBack!=null){
                            infoFlowAdCallBack.onAdClick(true);
                        }
                    }

                    @Override
                    public void onAdShow(YunkeTemplateView templateView) {
                        LogUtils.d("shykad","模板广告展示");
                        if (infoFlowAdCallBack!=null){
                            infoFlowAdCallBack.onAdShow();
                        }
                    }

                    @Override
                    public void onAdError(String err) {
                        LogUtils.d("shykad","feed广告异常："+err);
                        if (infoFlowAdCallBack!=null){
                            infoFlowAdCallBack.onAdError(err);
                        }
                    }

                    @Override
                    public void onAdCancel(YunkeTemplateView templateView) {
                        LogUtils.d("模板广告关闭");
                        if (infoFlowAdCallBack!=null){
                            infoFlowAdCallBack.onAdCancel(templateView);
                        }
                    }

                    @Override
                    public void onAdLoad(YunkeTemplateView templateView) {
                        LogUtils.d("模板广告数据加载完成");
                        if (infoFlowContainer.getVisibility() != View.VISIBLE) {
                            infoFlowContainer.setVisibility(View.VISIBLE);
                        }
                        if (infoFlowContainer.getChildCount() > 0) {
                            infoFlowContainer.removeAllViews();
                        }
                        List<YunkeTemplateView> adList = new ArrayList<>();
                        for (int i=0;i<adCount;++i){
                            adList.add(templateView);
                        }
                        if (infoFlowAdCallBack!=null){
                            infoFlowAdCallBack.onAdLoad(adList,HttpConfig.AD_CHANNEL_YUNKE);
                        }
                    }
                });

        ViewGroup parent = (ViewGroup) templateView.getParent();
        if (parent!=null){
            templateView.removeView(parent);
            templateView.removeAllViews();
        }
        infoFlowContainer.addView(templateView);
    }

    /**
     * 腾讯信息流广告
     */
    private void showTencentInfoFlow(String posId,String appId,int AD_COUNT){
        ADSize adSize = new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT); // 消息流中用AUTO_HEIGHT
        mADManager = new NativeExpressAD(mContext, adSize, appId, posId, new NativeExpressAD.NativeExpressADListener() {
            @Override
            public void onADLoaded(List<NativeExpressADView> adList) {
                LogUtils.d("shykad-gdt", "onADLoaded: " + adList.size());
                if (infoFlowAdCallBack!=null){
                    infoFlowAdCallBack.onAdLoad(adList,HttpConfig.AD_CHANNEL_TENCENT);
                }
            }

            @Override
            public void onRenderFail(NativeExpressADView nativeExpressADView) {
                LogUtils.d("shykad-gdt", "onRenderFail: " + nativeExpressADView.toString());
                if (infoFlowAdCallBack!=null){
                    infoFlowAdCallBack.onAdError("renderFail:"+nativeExpressADView.toString());
                }
            }

            @Override
            public void onRenderSuccess(NativeExpressADView nativeExpressADView) {
                LogUtils.d("shykad-gdt", "onRenderSuccess: " + nativeExpressADView.toString() + ", adInfo: " + getAdInfo(nativeExpressADView));
            }

            @Override
            public void onADExposure(NativeExpressADView nativeExpressADView) {
                LogUtils.d("shykad-gdt", "onADExposure: " + nativeExpressADView.toString());
                showAdTask();
            }

            @Override
            public void onADClicked(NativeExpressADView nativeExpressADView) {
                LogUtils.d("shykad-gdt", "onADClicked: " + nativeExpressADView.toString());
                clickAdTask(false);
            }

            @Override
            public void onADClosed(NativeExpressADView nativeExpressADView) {
                LogUtils.d("shykad-gdt", "onADClosed: " + nativeExpressADView.toString());
                if (infoFlowAdCallBack!=null){
                    infoFlowAdCallBack.onAdCancel(nativeExpressADView);
                }
            }

            @Override
            public void onADLeftApplication(NativeExpressADView nativeExpressADView) {
                LogUtils.d("shykad-gdt", "onADLeftApplication: " + nativeExpressADView.toString());
            }

            @Override
            public void onADOpenOverlay(NativeExpressADView nativeExpressADView) {
                LogUtils.d("shykad-gdt", "onADOpenOverlay: " + nativeExpressADView.toString());
            }

            @Override
            public void onADCloseOverlay(NativeExpressADView nativeExpressADView) {
                LogUtils.d("shykad-gdt", "onADCloseOverlay");
            }

            @Override
            public void onNoAD(AdError adError) {// TODO: 2019/3/27  初始化错误，详细码：200102
                String err = String.format("onNoAD, error code: %d, error msg: %s", adError.getErrorCode(),adError.getErrorMsg());
                LogUtils.d("shykad-gdt",err);
                if (infoFlowAdCallBack!=null){
                    infoFlowAdCallBack.onAdError(err);
                }
            }
        });
        mADManager.loadAD(AD_COUNT);
    }

    /**
     * 获取广告数据
     *
     * @param nativeExpressADView
     * @return
     */
    public String getAdInfo(NativeExpressADView nativeExpressADView) {
        AdData adData = nativeExpressADView.getBoundData();
        if (adData != null) {
            StringBuilder infoBuilder = new StringBuilder();
            infoBuilder.append("title:").append(adData.getTitle()).append(",")
                    .append("desc:").append(adData.getDesc()).append(",")
                    .append("patternType:").append(adData.getAdPatternType());
            if (adData.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                infoBuilder.append(", video info: ")
                        .append(getVideoInfo(adData.getProperty(AdData.VideoPlayer.class)));
            }
            return infoBuilder.toString();
        }
        return null;
    }

    /**
     * 获取播放器实例
     *
     * 仅当视频回调{@link NativeExpressMediaListener#onVideoInit(NativeExpressADView)}调用后才会有返回值
     *
     * @param videoPlayer
     * @return
     */
    public String getVideoInfo(AdData.VideoPlayer videoPlayer) {
        if (videoPlayer != null) {
            StringBuilder videoBuilder = new StringBuilder();
            videoBuilder.append("state:").append(videoPlayer.getVideoState()).append(",")
                    .append("duration:").append(videoPlayer.getDuration()).append(",")
                    .append("position:").append(videoPlayer.getCurrentPosition());
            return videoBuilder.toString();
        }
        return null;
    }

    /**
     * 销毁资源
     * @param mAdViewList
     */
    public void destoryAd(List<NativeExpressADView> mAdViewList){
        // 使用完了每一个NativeExpressADView之后都要释放掉资源。
        if (mAdViewList != null) {
            for (NativeExpressADView view : mAdViewList) {
                view.destroy();
            }
        }
    }

    /**
     * 头条信息流广告
     */
    private void showByteDanceInfoFlow(ViewGroup container,String slotId,int adCount){

        //step4:创建feed广告请求类型参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(slotId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(640, 320)
                .setAdCount(adCount) //请求广告数量为1到3条
                .build();
        //step5:请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染
        mTTAdNative.loadFeedAd(adSlot, new TTAdNative.FeedAdListener() {
            @Override
            public void onError(int code, String message) {
                LogUtils.d("shykad-csj","code:"+code+" message:"+message);
                if (infoFlowAdCallBack!=null){
                    infoFlowAdCallBack.onAdError("code:"+code+" message:"+message);
                }
            }

            @Override
            public void onFeedAdLoad(List<TTFeedAd> ads) {

                //可以被点击的view, 也可以把convertView放进来意味item可被点击
                List<View> clickViewList = new ArrayList<>();

                //触发创意广告的view（点击下载或拨打电话）
                List<View> creativeViewList = new ArrayList<>();

                //回调load中list数据
                List<ViewGroup> containerList = new ArrayList<>();

                for (TTFeedAd ad: ads){

                    ViewGroup convertView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.yunke_template_ad_view,null);
                    InfoFlowEngine.LargeAdViewHolder adViewHolder = new LargeAdViewHolder();

                    adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_native_ad_title);
                    adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_native_ad_desc);
                    adViewHolder.mCancel = (ImageView) convertView.findViewById(R.id.img_native_dislike);
                    adViewHolder.mLargeImage = (ImageView) convertView.findViewById(R.id.iv_native_image);
                    adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_native_icon);
                    adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_native_create);
                    convertView.setTag(adViewHolder);

                    clickViewList.add(convertView);
                    creativeViewList.add(adViewHolder.mCreativeButton);//如果需要点击图文区域也能进行下载或者拨打电话动作，请将图文区域的view传入
                    creativeViewList.add(convertView);
                    containerList.add(convertView);

                    bindDownloadListener(adViewHolder.mCreativeButton, adViewHolder, ad);
                    /**
                     * 注册可点击的View，click/show会在内部完成 重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。
                     * @param container 渲染广告最外层的ViewGroup
                     * @param clickView 可点击的View
                     */
                    ad.registerViewForInteraction(convertView, clickViewList, creativeViewList,new TTNativeAd.AdInteractionListener() {
                        @Override
                        public void onAdClicked(View view, TTNativeAd ad) {
                            if (ad != null) {
                                LogUtils.d( "shykad-csj","广告" + ad.getTitle() + "被点击");
                                clickAdTask(false);
                            }
                        }

                        @Override
                        public void onAdCreativeClick(View view, TTNativeAd ad) {
                            if (ad != null) {
                                LogUtils.d( "shykad-csj","广告" + ad.getTitle() + "被创意按钮被点击");
                                clickAdTask(false);
                            }
                        }

                        @Override
                        public void onAdShow(TTNativeAd ad) {
                            if (ad != null) {
                                LogUtils.d( "shykad-csj", "广告" + ad.getTitle() + "展示");
                                showAdTask();
                            }
                        }
                    });

                    if (ad.getImageList() != null && !ad.getImageList().isEmpty()) {
                        TTImage image = ad.getImageList().get(0);
                        if (image != null && image.isValid()) {
                            mAQuery.id(adViewHolder.mLargeImage).image(image.getImageUrl());
                            mAQuery.id(adViewHolder.mIcon).image(ad.getAdLogo());
                            int i = 0;
                            LogUtils.i("imgUrl:"+i++,image.getImageUrl());
                        }

                    }
                    adViewHolder.mTitle.setText(ad.getTitle());
                    adViewHolder.mDescription.setText(ad.getDescription());
                    switch (ad.getInteractionType()){
                        case 2://在浏览器打开网页
                            adViewHolder.mCreativeButton.setText("查看详情");
                            break;
                        case 3://在app中打开
                            adViewHolder.mCreativeButton.setText("点击打开");
                            break;
                        case 4://下载应用
                            adViewHolder.mCreativeButton.setText("点击下载");
                            break;
                        case 5://拨打电话
                            adViewHolder.mCreativeButton.setText("点击拨打");
                            break;
                        default://其它：未知类型
                            adViewHolder.mCreativeButton.setVisibility(View.GONE);
                            break;
                    }

                    TTAdDislike ttAdDislike = ad.getDislikeDialog(mContext);
                    if (ttAdDislike!=null){

                        ttAdDislike.setDislikeInteractionCallback(new TTAdDislike.DislikeInteractionCallback() {
                            @Override
                            public void onSelected(int position, String value) {
                                LogUtils.d("shykad-csj","Template点击 " + value);
                                //用户选择不喜欢原因后，移除广告展示
                                convertView.removeAllViews();
                                if (infoFlowAdCallBack!=null){
                                    infoFlowAdCallBack.onAdCancel(convertView);
                                }
                            }

                            @Override
                            public void onCancel() {
                                LogUtils.d("shykad-csj","Template点击取消");
                            }
                        });

                        adViewHolder.mCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ttAdDislike.showDislikeDialog();
                            }
                        });
                    }


                }

                if (infoFlowAdCallBack!=null){
                    infoFlowAdCallBack.onAdLoad(containerList,HttpConfig.AD_CHANNEL_BYTEDANCE);
                }

            }
        });
    }

    private void bindDownloadListener(final Button adCreativeButton, final AdViewHolder adViewHolder, TTFeedAd ad) {
        TTAppDownloadListener downloadListener = new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                if (!isValid()) {
                    return;
                }
                adCreativeButton.setText("开始下载");
                adViewHolder.mCreativeButton.setText("开始下载");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                if (totalBytes <= 0L) {
                    adCreativeButton.setText("下载中 percent: 0");
                } else {
                    adCreativeButton.setText("下载中 percent: " + (currBytes * 100 / totalBytes));
                }
                adViewHolder.mCreativeButton.setText("下载中");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                if (totalBytes <= 0L) {
                    adCreativeButton.setText("下载中 percent: 0");
                } else {
                    adCreativeButton.setText("下载暂停 percent: " + (currBytes * 100 / totalBytes));
                }
                adViewHolder.mCreativeButton.setText("下载暂停");
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                adCreativeButton.setText("重新下载");
                adViewHolder.mCreativeButton.setText("重新下载");
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                adCreativeButton.setText("点击打开");
                adViewHolder.mCreativeButton.setText("点击打开");
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                adCreativeButton.setText("点击安装");
                adViewHolder.mCreativeButton.setText("点击安装");
            }

            @SuppressWarnings("BooleanMethodIsAlwaysInverted")
            private boolean isValid() {
                return mTTAppDownloadListenerMap.get(adViewHolder) == this;
            }
        };
        //一个ViewHolder对应一个downloadListener, isValid判断当前ViewHolder绑定的listener是不是自己
        ad.setDownloadListener(downloadListener); // 注册下载监听器
        mTTAppDownloadListenerMap.put(adViewHolder, downloadListener);
    }

    private static class LargeAdViewHolder extends InfoFlowEngine.AdViewHolder {
        ImageView mLargeImage;
    }

    private static class AdViewHolder {
        ImageView mIcon;
        ImageView mCancel;
        TextView mTitle;
        TextView mDescription;
        Button mCreativeButton;
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

                                if (infoFlowAdCallBack!=null){
                                    infoFlowAdCallBack.onAdShow();
                                }

                            }

                            @Override
                            public void feedAdFailed(String err) {
                                if (infoFlowAdCallBack!=null){
                                    infoFlowAdCallBack.onAdError(err);
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
    private void clickAdTask(boolean isJump) {
        ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                (new Handler(Looper.getMainLooper())).post(new Runnable() {

                    @Override
                    public void run() {// TODO: 2019/3/7 埋点

                        YunKeEngine.getInstance(mContext).yunkeFeedAd(adCotent.getId(), HttpConfig.AD_CLICK_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {
                                if (infoFlowAdCallBack!=null){
                                    infoFlowAdCallBack.onAdClick(isJump);
                                }

                            }

                            @Override
                            public void feedAdFailed(String err) {
                                if (infoFlowAdCallBack!=null){
                                    infoFlowAdCallBack.onAdError(err);
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    public interface InfoFlowAdCallBack{
        void onAdLoad(List<?> adList,int channel);
        void onAdShow();
        void onAdClick(boolean isJump);
        void onAdError(String err);
        void onAdCancel(Object view);
    }

}

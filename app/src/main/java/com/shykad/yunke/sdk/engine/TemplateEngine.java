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
import com.qq.e.ads.cfg.VideoOption;
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
 * Create by wanghong.he on 2019/3/18.
 * description：原生信息流feed
 */
public class TemplateEngine {

    private Object response;
    private GetAdResponse.AdCotent adCotent;
    private static Activity mContext;
    private static TemplateEngine instance;
    private NativeExpressAD nativeExpressAD;
    private NativeExpressADView nativeExpressADView;
    private TTAdNative mTTAdNative;
    private YunkeTemplateView templateView;
    private boolean isAdFullWidth, isAdAutoHeight; // 是否采用了ADSize.FULL_WIDTH，ADSize.AUTO_HEIGHT
    private TemplateAdCallBack templateAdCallBack;
    private AQuery2 mAQuery;
    private Map<TemplateEngine.AdViewHolder, TTAppDownloadListener> mTTAppDownloadListenerMap = new WeakHashMap<>();

    private TemplateEngine(){

    }

    public static TemplateEngine getInstance(Activity context){
        mContext = context;
        if(instance == null){
            synchronized (TemplateEngine.class){
                if(instance == null) instance = new TemplateEngine();
            }
        }
        return instance;
    }

    /**
     * 初始化原生广告引擎
     * @param response
     * @param templateAdCallBack
     * @return TemplateEngine
     */
    public TemplateEngine initEngine(Object response, TemplateAdCallBack templateAdCallBack){
        this.response = response;
        this.templateAdCallBack = templateAdCallBack;

        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = ttAdManager.createAdNative(mContext.getApplicationContext());
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(mContext);
        mAQuery = new AQuery2(mContext);
        templateView = new YunkeTemplateView(mContext);
        return this;
    }

    /**
     * 加载原生广告引擎
     */
    public TemplateEngine launchTemplate(ViewGroup container,String adWidth, String adHeight,int adCount){
        if (response instanceof GetAdResponse){
            adCotent = ((GetAdResponse) response).getData();
            flowOptimization(container,adWidth,adHeight,adCount);
        }
        return this;
    }

    /**
     * 流量转化:0 云客 1：腾讯 2：头条
     */
    private void flowOptimization(ViewGroup container,String adWidth, String adHeight,int adCount){
        int channel = ((GetAdResponse) response).getData().getChannel();
        switch (channel){
            case HttpConfig.AD_CHANNEL_YUNKE:
                showYunkeTemplate(container);
                break;
            case HttpConfig.AD_CHANNEL_TENCENT:
                showTencentTemplate(container,adCotent.getSlotId(), (String) SPUtil.get(mContext,SPUtil.TX_APPID,adCotent.getAppId()),adWidth,adHeight,adCount);
                break;
            case HttpConfig.AD_CHANNEL_BYTEDANCE:
                showByteDanceTemplate(container,adCotent.getSlotId(),adCount);
                break;
            default:
                break;
        }
    }

    /**
     * 云客Template
     * todo 展示数据不准确--单例模式造成
     */
    private void showYunkeTemplate(ViewGroup templeContainer){

        templateView.setTemplateTitle(adCotent.getTitle())
                .setTemplateDesc(adCotent.getDesc())
                .setTemplateCancel(mContext.getResources().getDrawable(R.drawable.yunke_dislike_icon))
                .lanchTemplate(templeContainer, response, new YunkeTemplateView.TemplateViewCallBack() {
                    @Override
                    public void onAdClicked(YunkeTemplateView templateView) {
                        LogUtils.d("shykad","模板广告点击");
                        if (templateAdCallBack!=null){
                            templateAdCallBack.onTemplateClicked(true);
                        }
                    }

                    @Override
                    public void onAdShow(YunkeTemplateView templateView) {
                        LogUtils.d("shykad","模板广告展示");
                        if (templateAdCallBack!=null){
                            templateAdCallBack.onTemplateShow();
                        }
                    }

                    @Override
                    public void onAdError(String err) {
                        LogUtils.d("shykad","feed广告异常："+err);
                        if (templateAdCallBack!=null){
                            templateAdCallBack.onTemplateError(err);
                        }
                    }

                    @Override
                    public void onAdCancel(YunkeTemplateView templateView) {
                        LogUtils.d("模板广告关闭");
                        if (templateAdCallBack!=null){
                            templateAdCallBack.onTemplateCancel();
                        }
                    }

                    @Override
                    public void onAdLoad(YunkeTemplateView templateView) {
                        LogUtils.d("模板广告数据加载完成");
                        if (templeContainer.getVisibility() != View.VISIBLE) {
                            templeContainer.setVisibility(View.VISIBLE);
                        }
                        if (templeContainer.getChildCount() > 0) {
                            templeContainer.removeAllViews();
                        }
                    }
                });

        ViewGroup parent = (ViewGroup) templateView.getParent();
        if (parent!=null){
            templateView.removeView(parent);
            templateView.removeAllViews();
        }
        templeContainer.addView(templateView);
    }

    /**
     * 腾讯Template
     * @param slotId
     * @param appId
     */
    private void showTencentTemplate(ViewGroup container,String slotId,String appId,String adWidth,String adHeight,int adCount){
        try {
            /**
             *  如果选择支持视频的模版样式，请使用{@link Constants#NativeExpressSupportVideoPosID}
             */
            nativeExpressAD = new NativeExpressAD(mContext, getMyADSize(Integer.parseInt(adWidth),Integer.parseInt(adHeight)), appId, slotId, new NativeExpressAD.NativeExpressADListener() {
                @Override
                public void onADLoaded(List<NativeExpressADView> adList) {
                    LogUtils.d("shykad-gdt", "onADLoaded: " + adList.size());
                    // 释放前一个展示的NativeExpressADView的资源
                    if (nativeExpressADView != null) {
                        nativeExpressADView.destroy();
                    }

                    if (container.getVisibility() != View.VISIBLE) {
                        container.setVisibility(View.VISIBLE);
                    }

                    if (container.getChildCount() > 0) {
                        container.removeAllViews();
                    }

                    nativeExpressADView = adList.get(0);
                    LogUtils.d("shykad-gdt", "onADLoaded, video info: " + getAdInfo(nativeExpressADView));
                    if (nativeExpressADView.getBoundData().getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                        nativeExpressADView.setMediaListener(mediaListener);
                    }
                    // 广告可见才会产生曝光，否则将无法产生收益。
                    container.addView(nativeExpressADView);
                    nativeExpressADView.render();

                    if (templateAdCallBack!=null){
                        templateAdCallBack.onTemplateLoad();
                    }
                }

                @Override
                public void onRenderFail(NativeExpressADView nativeExpressADView) {
                    LogUtils.d("shykad-gdt", "onRenderFail");
                    if (templateAdCallBack!=null){
                        templateAdCallBack.onTemplateError("NativeExpressADView onRenderFail!");
                    }
                }

                @Override
                public void onRenderSuccess(NativeExpressADView nativeExpressADView) {
                    LogUtils.d("shykad-gdt", "onRenderSuccess");

                }

                @Override
                public void onADExposure(NativeExpressADView nativeExpressADView) {
                    LogUtils.d("shykad-gdt", "onADExposure");
                    showAdTask();
                }

                @Override
                public void onADClicked(NativeExpressADView nativeExpressADView) {
                    LogUtils.d("shykad-gdt", "onADClicked");
                    clickAdTask(false);
                }

                @Override
                public void onADClosed(NativeExpressADView nativeExpressADView) {
                    LogUtils.d("shykad-gdt", "onADClosed");
                    // 当广告模板中的关闭按钮被点击时，广告将不再展示。NativeExpressADView也会被Destroy，释放资源，不可以再用来展示。
                    if (container != null && container.getChildCount() > 0) {
                        container.removeAllViews();
                        container.setVisibility(View.GONE);
                    }
                    if (templateAdCallBack!=null){
                        templateAdCallBack.onTemplateCancel();
                    }
                }

                @Override
                public void onADLeftApplication(NativeExpressADView nativeExpressADView) {
                    LogUtils.d("shykad-gdt", "onADLeftApplication");
                }

                @Override
                public void onADOpenOverlay(NativeExpressADView nativeExpressADView) {
                    LogUtils.d("shykad-gdt", "onADOpenOverlay");
                }

                @Override
                public void onADCloseOverlay(NativeExpressADView nativeExpressADView) {
                    LogUtils.d("shykad-gdt", "onADCloseOverlay");
                }

                @Override
                public void onNoAD(AdError adError) {// TODO: 2019/3/27  初始化错误，详细码：200102
                    String err = String.format("onNoAD, error code: %d, error msg: %s", adError.getErrorCode(),adError.getErrorMsg());
                    LogUtils.d("shykad-gdt",err);
                    if (templateAdCallBack!=null){
                        templateAdCallBack.onTemplateError(err);
                    }
                }
            });
            // 这里的Context必须为Activity
            nativeExpressAD.setVideoOption(new VideoOption.Builder()
                    .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.WIFI) // 设置什么网络环境下可以自动播放视频
                    .setAutoPlayMuted(true) // 设置自动播放视频时，是否静音
                    .build()); // setVideoOption是可选的，开发者可根据需要选择是否配置
            nativeExpressAD.loadAD(adCount);//加载广告，count 指定期望加载的广告数量，根据广告填充情况不同，返回不大于 count 数量的广告
        } catch (Exception e) {
            LogUtils.e("shykad-gdt", "ad size invalid.\n"+e.getMessage());
        }


    }

    /**
     * 广告尺寸
     * @param adWidth
     * @param adHeight
     * @return
     */
    private ADSize getMyADSize(int adWidth,int adHeight) {
        if (adHeight<=0 && adWidth <=0){
            isAdFullWidth = true;
            isAdAutoHeight = true;
        }else {
            isAdFullWidth = false;
            isAdAutoHeight = false;
        }
        int w = isAdFullWidth ? ADSize.FULL_WIDTH : adWidth;
        int h = isAdAutoHeight ? ADSize.AUTO_HEIGHT : adHeight;
        return new ADSize(w, h);
    }

    /**
     * 获取广告数据
     *
     * @param nativeExpressADView
     * @return
     */
    private String getAdInfo(NativeExpressADView nativeExpressADView) {
        AdData adData = nativeExpressADView.getBoundData();
        if (adData != null) {
            StringBuilder infoBuilder = new StringBuilder();
            infoBuilder.append("title:").append(adData.getTitle()).append(",")
                    .append("desc:").append(adData.getDesc()).append(",")
                    .append("patternType:").append(adData.getAdPatternType());
            if (adData.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                infoBuilder.append(", video info: ").append(getVideoInfo(adData.getProperty(AdData.VideoPlayer.class)));
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
    private String getVideoInfo(AdData.VideoPlayer videoPlayer) {
        if (videoPlayer != null) {
            StringBuilder videoBuilder = new StringBuilder();
            videoBuilder.append("{state:").append(videoPlayer.getVideoState()).append(",")
                    .append("duration:").append(videoPlayer.getDuration()).append(",")
                    .append("position:").append(videoPlayer.getCurrentPosition()).append("}");
            return videoBuilder.toString();
        }
        return null;
    }

    /**
     * 销毁广告
     * @return
     */
    public TemplateEngine destoryTemplate(){
        // 使用完了每一个NativeExpressADView之后都要释放掉资源
        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }
        return this;
    }

    private NativeExpressMediaListener mediaListener = new NativeExpressMediaListener() {
        @Override
        public void onVideoInit(NativeExpressADView nativeExpressADView) {
            LogUtils.d("shykad-gdt", "onVideoInit: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoLoading(NativeExpressADView nativeExpressADView) {
            LogUtils.d("shykad-gdt", "onVideoLoading");
        }

        @Override
        public void onVideoReady(NativeExpressADView nativeExpressADView, long l) {
            LogUtils.d("shykad-gdt", "onVideoReady");
        }

        @Override
        public void onVideoStart(NativeExpressADView nativeExpressADView) {
            LogUtils.d("shykad-gdt", "onVideoStart: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoPause(NativeExpressADView nativeExpressADView) {
            LogUtils.d("shykad-gdt", "onVideoPause: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoComplete(NativeExpressADView nativeExpressADView) {
            LogUtils.d("shykad-gdt", "onVideoComplete: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoError(NativeExpressADView nativeExpressADView, AdError adError) {
            LogUtils.d("shykad-gdt","onVideoError");
        }

        @Override
        public void onVideoPageOpen(NativeExpressADView nativeExpressADView) {
            LogUtils.d("shykad-gdt", "onVideoPageOpen");
        }

        @Override
        public void onVideoPageClose(NativeExpressADView nativeExpressADView) {
            LogUtils.d("shykad-gdt", "onVideoPageClose");
        }
    };

    /**
     * 头条Template
     * @param slotId
     */
    private void showByteDanceTemplate(final ViewGroup container,String slotId,int adCount){
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
                if (templateAdCallBack!=null){
                    templateAdCallBack.onTemplateError("code:"+code+" message:"+message);
                }
            }

            @Override
            public void onFeedAdLoad(List<TTFeedAd> ads) {
                View convertView = LayoutInflater.from(mContext).inflate(R.layout.yunke_template_ad_view,container);
                LargeAdViewHolder adViewHolder = new LargeAdViewHolder();
                adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_native_ad_title);
                adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_native_ad_desc);
                adViewHolder.mCancel = (ImageView) convertView.findViewById(R.id.img_native_dislike);
                adViewHolder.mLargeImage = (ImageView) convertView.findViewById(R.id.iv_native_image);
                adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_native_icon);
                adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_native_create);
                convertView.setTag(adViewHolder);

                //可以被点击的view, 也可以把convertView放进来意味item可被点击
                List<View> clickViewList = new ArrayList<>();
                clickViewList.add(convertView);
                //触发创意广告的view（点击下载或拨打电话）
                List<View> creativeViewList = new ArrayList<>();

                //如果需要点击图文区域也能进行下载或者拨打电话动作，请将图文区域的view传入
                creativeViewList.add(adViewHolder.mCreativeButton);
                creativeViewList.add(convertView);

                bindDownloadListener(adViewHolder.mCreativeButton, adViewHolder, ads.get(0));
                /**
                 * 注册可点击的View，click/show会在内部完成 重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。
                 * @param container 渲染广告最外层的ViewGroup
                 * @param clickView 可点击的View
                 */
                ads.get(0).registerViewForInteraction((ViewGroup) convertView, clickViewList, creativeViewList,new TTNativeAd.AdInteractionListener() {
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

                if (ads.get(0).getImageList() != null && !ads.get(0).getImageList().isEmpty()) {
                    TTImage image = ads.get(0).getImageList().get(0);
                    if (image != null && image.isValid()) {
                        mAQuery.id(adViewHolder.mLargeImage).image(image.getImageUrl());
                        mAQuery.id(adViewHolder.mIcon).image(ads.get(0).getAdLogo());
                    }
                }
                adViewHolder.mTitle.setText(ads.get(0).getTitle());
                adViewHolder.mDescription.setText(ads.get(0).getDescription());
                //adViewHolder.mDescription.setText(ads.get(0).getDescription()+"\n来源："+ (TextUtils.isEmpty(ads.get(0).getSource()) ? ads.get(0).getTitle():ads.get(0).getSource()));
                switch (ads.get(0).getInteractionType()){
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

                TTAdDislike ttAdDislike = ads.get(0).getDislikeDialog(mContext);
                if (ttAdDislike!=null){

                    ttAdDislike.setDislikeInteractionCallback(new TTAdDislike.DislikeInteractionCallback() {
                        @Override
                        public void onSelected(int position, String value) {
                            LogUtils.d("shykad-csj","Template点击 " + value);
                            //用户选择不喜欢原因后，移除广告展示
                            container.removeAllViews();
                            if (templateAdCallBack!=null){
                                templateAdCallBack.onTemplateCancel();
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


                if (templateAdCallBack!=null){
                    templateAdCallBack.onTemplateLoad();
                }
            }
        });

    }

    private static class LargeAdViewHolder extends AdViewHolder {
        ImageView mLargeImage;
    }

    private static class AdViewHolder {
        ImageView mIcon;
        ImageView mCancel;
        TextView mTitle;
        TextView mDescription;
        Button mCreativeButton;
    }

    private void bindDownloadListener(final Button adCreativeButton, final TemplateEngine.AdViewHolder adViewHolder, TTFeedAd ad) {
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

                                if (templateAdCallBack!=null){
                                    templateAdCallBack.onTemplateShow();
                                }

                            }

                            @Override
                            public void feedAdFailed(String err) {
                                if (templateAdCallBack!=null){
                                    templateAdCallBack.onTemplateError(err);
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
                                if (templateAdCallBack!=null){
                                    templateAdCallBack.onTemplateClicked(isJump);
                                }

                            }

                            @Override
                            public void feedAdFailed(String err) {
                                if (templateAdCallBack!=null){
                                    templateAdCallBack.onTemplateError(err);
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    public interface TemplateAdCallBack{
        void onTemplateShow();
        void onTemplateClicked(boolean isJump);
        void onTemplateCancel();
        void onTemplateLoad();
        void onTemplateError(String err);
    }
}

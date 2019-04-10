package com.shykad.yunke.sdk.engine;

import android.app.Activity;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.ui.widget.YunkeVideoView;
import com.shykad.yunke.sdk.utils.LogUtils;

/**
 * Create by wanghong.he on 2019/4/8.
 * description：
 */
public class VideoEngine {

    private Object response;
    private GetAdResponse.AdCotent adCotent;
    private static Activity mContext;
    private static VideoEngine instance;
    private VideoAdCallBack videoAdCallBack;
    private YunkeVideoView videoView;

    private VideoEngine(){

    }

    public static VideoEngine getInstance(Activity context){
        mContext = context;
        if(instance == null){
            synchronized (VideoEngine.class){
                if(instance == null) instance = new VideoEngine();
            }
        }
        return instance;
    }

    /**
     * 初始化原生广告引擎
     * @param response
     * @param videoAdCallBack
     * @return VideoEngine
     */
    public VideoEngine initEngine(Object response, VideoEngine.VideoAdCallBack videoAdCallBack){
        this.response = response;
        this.videoAdCallBack = videoAdCallBack;

        videoView = new YunkeVideoView(mContext);
        return this;
    }

    /**
     * 加载原生广告引擎
     */
    public VideoEngine launchVideo(ViewGroup container, String adWidth, String adHeight, int adCount){
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
                showYunkeVideo(container);
                break;
            case HttpConfig.AD_CHANNEL_TENCENT:
//                showTencentVideo(container,adCotent.getSlotId(), (String) SPUtil.get(mContext,SPUtil.TX_APPID,adCotent.getAppId()),adWidth,adHeight,adCount);
                break;
            case HttpConfig.AD_CHANNEL_BYTEDANCE:
//                showByteDanceVideo(container,adCotent.getSlotId(),adCount);
                break;
            default:
                break;
        }
    }

    /**
     * 云客视频广告
     * @param videoContainer
     */
    private void showYunkeVideo(ViewGroup videoContainer){

        videoView.setVideoPath(Environment.getExternalStorageDirectory().getPath()+"/V90408-184214.mp4")
                .setVideoTitle(adCotent.getTitle())
                .setVideoDesc(adCotent.getDesc())
                .setVideoCancel(mContext.getResources().getDrawable(R.drawable.yunke_dislike_icon))
                .lanchVideo(videoContainer, response, new YunkeVideoView.VideoViewCallBack() {
                    @Override
                    public void onAdClicked(YunkeVideoView videoView) {
                        LogUtils.d("shykad","模板广告点击");
                        if (videoAdCallBack!=null){
                            videoAdCallBack.onVideoClicked(true);
                        }
                    }

                    @Override
                    public void onAdShow(YunkeVideoView videoView) {
                        LogUtils.d("shykad","模板广告展示");
                        if (videoAdCallBack!=null){
                            videoAdCallBack.onVideoShow();
                        }
                    }

                    @Override
                    public void onAdError(String err) {
                        LogUtils.d("shykad","feed广告异常："+err);
                        if (videoAdCallBack!=null){
                            videoAdCallBack.onVideoError(err);
                        }
                    }

                    @Override
                    public void onAdCancel(YunkeVideoView videoView) {
                        LogUtils.d("模板广告关闭");
                        if (videoAdCallBack!=null){
                            videoAdCallBack.onVideoCancel();
                        }
                    }

                    @Override
                    public void onAdLoad(YunkeVideoView videoView) {
                        LogUtils.d("模板广告数据加载完成");
                        if (videoContainer.getVisibility() != View.VISIBLE) {
                            videoContainer.setVisibility(View.VISIBLE);
                        }
                        if (videoContainer.getChildCount() > 0) {
                            videoContainer.removeAllViews();
                        }
                    }
                });

        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent!=null){
            videoView.removeView(parent);
            videoView.removeAllViews();
        }
        videoContainer.addView(videoView);
    }

    public void onStop(){
        if (videoView!=null){
            videoView.stop();
        }
    }

    public void onPause(){
        if (videoView!=null){
            videoView.pause();
        }
    }

    public void onStart(){
        if (videoView!=null){
            videoView.start();
        }
    }

    public void onDestory(){
        if (videoView!=null){
            ViewGroup parent = (ViewGroup) videoView.getParent();
            if (parent!=null){
                videoView.removeView(parent);
                videoView.removeAllViews();
            }
            videoView.stop();
            videoView.pause();
            videoView = null;
        }
    }

    public interface VideoAdCallBack{
        void onVideoShow();
        void onVideoStart();
        void onVideoStop();
        void onVideoPause();
        void onVideoDestory();
        void onVideoClicked(boolean isJump);
        void onVideoCancel();
        void onVideoLoad();
        void onVideoError(String err);
    }
}

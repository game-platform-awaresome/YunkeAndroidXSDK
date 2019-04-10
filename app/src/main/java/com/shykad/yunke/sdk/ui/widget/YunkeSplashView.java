package com.shykad.yunke.sdk.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.engine.YunKeEngine;
import com.shykad.yunke.sdk.manager.ShykadManager;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.utils.BaseRealVisibleUtil;
import com.shykad.yunke.sdk.utils.RealVisibleInterface;
import com.shykad.yunke.sdk.utils.StringUtils;

/**
 * Create by wanghong.he on 2019/3/7.
 * description：
 */
public class YunkeSplashView {

    private Context mContext;
    private CountDownTimer timeCount;
    private String adId;
    private SplashADCallBack splashADCallBack;
    private SplashInitCallBack splashInitCallBack;
    private boolean isClicked = false;

    public YunkeSplashView(Context context) {
        mContext = context;
    }

    public void initView(String slotId,SplashInitCallBack splashInitCallBack){
        this.splashInitCallBack = splashInitCallBack;
        if (!StringUtils.isEmpty(slotId)){
            if (splashInitCallBack!=null){
                splashInitCallBack.initView(slotId);
            }
        }
    }



    public void lanuchSplash(Activity activity,ViewGroup adContainer, ImageView splashHolderView,TextView skipContainer,Long surPlusTime,
                             String adId, String appId, GetAdResponse.AdCotent adCotent, SplashADCallBack splashADCallBack){

        this.splashADCallBack = splashADCallBack;
        this.adId = adId;
        mContext = activity;
        if (!StringUtils.isEmpty(adId) && !StringUtils.isEmpty(appId) && activity!=null && adContainer!=null){
            if (splashADCallBack!=null) {
                splashADCallBack.onAdPresent();

                adContainer.removeAllViews();
                adContainer.addView(splashHolderView);
                GlidImageManager.getInstance().loadImageView(activity,adCotent.getSrc(),splashHolderView,R.drawable.yunke_ic_default_image);

                BaseRealVisibleUtil.getInstance(mContext).release().registerView(splashHolderView, new RealVisibleInterface.OnRealVisibleListener() {
                    @Override
                    public void onRealVisible(int position) {
                        // position 对于有子view的有用,如果注册的是单个view 这个position忽略
                        showAdTask();
                    }
                });
                BaseRealVisibleUtil.getInstance(mContext).calculateRealVisible();

                adContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickAdTask();
                        //点击广告时/点击跳过时 倒计时要取消
                        if (timeCount!=null){
                            timeCount.cancel();
                        }
                    }
                });

                skipContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        splashADCallBack.onAdSkip();
                        if (timeCount!=null){
                            timeCount.cancel();
                        }
                    }
                });

                ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        (new Handler(Looper.getMainLooper())).post(new Runnable() {
                            @Override
                            public void run() {
                                surPlusTime(surPlusTime, skipContainer);
                            }
                        });
                    }
                });
            }

        }
    }

    /**
     * 展示广告
     */
    private void showAdTask(){
        ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                (new Handler(Looper.getMainLooper())).post(new Runnable() {

                    @Override
                    public void run() {// TODO: 2019/3/7 埋点

                        YunKeEngine.getInstance(mContext).yunkeFeedAd(adId, HttpConfig.AD_SHOW_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {
                                splashADCallBack.onAdShow();
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                splashADCallBack.onAdError(err);
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 点击广告
     */
    private void clickAdTask() {
        ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                (new Handler(Looper.getMainLooper())).post(new Runnable() {

                    @Override
                    public void run() {// TODO: 2019/3/7 埋点

                        YunKeEngine.getInstance(mContext).yunkeFeedAd(adId, HttpConfig.AD_CLICK_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {
                                splashADCallBack.onAdClicked();
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                splashADCallBack.onAdError(err);
                            }
                        });
                    }
                });
            }
        });

    }


    private void surPlusTime(Long surPlusTime,TextView skipContainer){
        if (surPlusTime!=null && surPlusTime > 3000){
            countTime(surPlusTime,skipContainer);
        }else if (surPlusTime!=null && surPlusTime == 0){
            countTime(Long.valueOf(3*1000),skipContainer);
        }else {
            if (this.splashADCallBack!=null){
                this.splashADCallBack.onAdError("The remaining time must be greater than 3 seconds");
            }
        }
    }

    private void countTime(Long time,TextView skipContainer){
        timeCount = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                skipContainer.setText("点击跳过"+(int) (millisUntilFinished / 1000) + "s");
                skipContainer.setBackground(mContext.getDrawable(R.drawable.yunke_common_nonet_button));
                skipContainer.setTextColor(mContext.getResources().getColor(R.color.white));
            }

            @Override
            public void onFinish() {
                if (splashADCallBack!=null){
                    splashADCallBack.onAdTimeOver();
                }
            }
        }.start();
    }

    public interface SplashADCallBack{
        void onAdPresent();
        void onAdClicked();
        void onAdShow();
        void onAdSkip();
        void onAdTimeOver();
        void onAdError(String err);
    }

    public interface SplashInitCallBack{
        void initView(String slotId);
    }
}

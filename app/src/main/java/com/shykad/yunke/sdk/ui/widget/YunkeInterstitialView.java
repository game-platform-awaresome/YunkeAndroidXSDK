package com.shykad.yunke.sdk.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.engine.YunKeEngine;
import com.shykad.yunke.sdk.manager.ShykadManager;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.utils.BaseRealVisibleUtil;
import com.shykad.yunke.sdk.utils.StringUtils;

/**
 * Create by wanghong.he on 2019/3/8.
 * description：插屏
 */
public class YunkeInterstitialView extends PopupWindow {

    private Context mContext;
    private RelativeLayout rootContainer;
    private ImageView insertContainer,insertClosr;
    private InterstitialCallBack interstitialCallBack;
    private String adId;
    private TextView adTitle,adDesc;
    private GetAdResponse.AdCotent adCotent;
    private boolean isShowAniming;//show动画是否在执行中
    private boolean isHideAniming;//hide动画是否在执行中

    public YunkeInterstitialView(Context context, String adId, GetAdResponse.AdCotent adCotent, InterstitialCallBack interstitialCallBack){
        super(null, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        this.mContext = context;
        this.adId = adId;
        this.adCotent = adCotent;
        this.interstitialCallBack = interstitialCallBack;

        //设置点击空白处消失
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);
        setClippingEnabled(false);
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                /**
                 * 判断是不是点击了外部
                 */
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    return true;
                }
                //不是点击外部
                return false;
            }
        });
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int w = wm.getDefaultDisplay().getWidth();
        int h = wm.getDefaultDisplay().getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.parseColor("#88000000"));//填充颜色
        setBackgroundDrawable(new BitmapDrawable(context.getResources(), bitmap));

        initView();
    }

    private void initView() {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.yunke_interstital_ad_view, null);
        setContentView(rootView);
        rootContainer = rootView.findViewById(R.id.root_container);
        insertContainer = rootView.findViewById(R.id.insert_container);
        adTitle = rootView.findViewById(R.id.insert_ad_title);
        adDesc = rootView.findViewById(R.id.insert_ad_desc);
        adTitle.setText(adCotent.getTitle());
        adDesc.setText(adCotent.getDesc());
        GlidImageManager.getInstance().loadImageView(mContext,adCotent.getSrc(),insertContainer,R.drawable.yunke_ic_default_image);
        insertClosr = rootView.findViewById(R.id.insert_close);
        if (interstitialCallBack!=null){
            insertClosr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    interstitialCallBack.onAdClose();
                }
            });

            insertContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAdTask();
                }
            });
        }
        if (!StringUtils.isEmpty(rootView) && BaseRealVisibleUtil.getInstance(mContext).isViewCovered(rootView)){
            showAdTask();
        }
    }

    public YunkeInterstitialView showInterstitial(int rootLayoutId) {
        View parent = LayoutInflater.from(mContext).inflate(rootLayoutId, null);
        this.showAtLocation(parent, Gravity.CENTER | Gravity.LEFT, 0, 0);
        return this;
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
                                interstitialCallBack.onAdShow(YunkeInterstitialView.this);
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                interstitialCallBack.onAdError(err);
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
                                interstitialCallBack.onAdClicked(YunkeInterstitialView.this);
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                interstitialCallBack.onAdError(err);
                            }
                        });
                    }
                });
            }
        });

    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        if (!isShowAniming) {
            isShowAniming = true;
            popupAnim(rootContainer, 0.0f, 1.0f, 300, true);
        }
    }

    @Override
    public void dismiss() {
        if (!isHideAniming) {
            isHideAniming = true;
            popupAnim(rootContainer, 1.0f, 0.0f, 300, false);
        }
    }

    /**
     * popupWindow属性动画
     *
     * @param view     执行属性动画的view
     * @param start    start值
     * @param end      end值
     * @param duration 动画持续时间
     * @param flag     true代表show，false代表hide
     */
    private void popupAnim(final View view, float start, final float end, int duration, final boolean flag) {
        ValueAnimator va = ValueAnimator.ofFloat(start, end).setDuration(duration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setPivotX(0);
                view.setPivotY(view.getMeasuredHeight());
                view.setTextDirection(1000);
                view.setTranslationZ((1 - value) * view.getHeight());//如若想从屏幕下方弹出 则view.setTranslationY（）即可
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (!flag) {
                    isHideAniming = false;
                    YunkeInterstitialView.super.dismiss();
                } else {
                    isShowAniming = false;
                }
            }
        });
        va.start();
    }

    public interface InterstitialCallBack{
        void onAdShow(YunkeInterstitialView interstitialView);
        void onAdError(String err);
        void onAdClicked(YunkeInterstitialView interstitialView);
        void onAdClose();
    }
}

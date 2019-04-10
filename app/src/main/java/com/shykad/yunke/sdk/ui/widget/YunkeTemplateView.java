package com.shykad.yunke.sdk.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.engine.YunKeEngine;
import com.shykad.yunke.sdk.manager.ShykadManager;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.utils.BaseRealVisibleUtil;
import com.shykad.yunke.sdk.utils.StringUtils;
import com.shykad.yunke.sdk.utils.SystemUtils;

/**
 * Create by wanghong.he on 2019/3/8.
 * description：原生模板
 */
public class YunkeTemplateView extends RelativeLayout {

    private Context mContext;
    private TextView nativeTitle,nativeDesc;
    private ImageView nativeClose,nativeContainer,nativeIcon;
    private TemplateViewCallBack templateViewCallBack;
    private Button nativeBtn;
    private String adId;
    private GetAdResponse.AdCotent adCotent;

    public YunkeTemplateView(Context context) {
        this(context,null);
    }

    public YunkeTemplateView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public YunkeTemplateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //获取控件
        View view = LayoutInflater.from(context).inflate(R.layout.yunke_template_ad_view, this);
        initView(view);
        initAttrs(context,attrs);

    }

    @SuppressLint({"Recycle", "CustomViewStyleable"})
    private void initView(View view) {

        nativeTitle = view.findViewById(R.id.tv_native_ad_title);
        nativeClose = view.findViewById(R.id.img_native_dislike);
        nativeContainer = view.findViewById(R.id.iv_native_image);
        nativeIcon = view.findViewById(R.id.iv_native_icon);
        nativeDesc = view.findViewById(R.id.tv_native_ad_desc);
        nativeBtn = view.findViewById(R.id.btn_native_create);
    }

    private void initAttrs(Context context, AttributeSet attrs){
        //获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.YunkeTemplateView);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            initAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();
    }

    private void initAttr(int attr, TypedArray typedArray){
        if (attr == R.styleable.YunkeTemplateView_template_title){
            setTemplateTitle(typedArray.getText(attr));
        }
        if (attr == R.styleable.YunkeTemplateView_template_desc){
            setTemplateDesc(typedArray.getText(attr));
        }
        if (attr == R.styleable.YunkeTemplateView_template_content_drawable){
            setTemplateContentDrawable(typedArray.getDrawable(attr));
        }
        if (attr == R.styleable.YunkeTemplateView_template_logo_drawable){
            setTemplateLogoDrawable(typedArray.getDrawable(attr));
        }
        if (attr == R.styleable.YunkeTemplateView_template_cancel_drawable){
            setTemplateCancel(typedArray.getDrawable(attr));
        }
    }

    public YunkeTemplateView setTemplateTitle(CharSequence templateTitle){
        nativeTitle.setText(templateTitle);
        return this;
    }

    public YunkeTemplateView setTemplateDesc(CharSequence templateDesc){
        nativeDesc.setText(templateDesc);
        return this;
    }

    public YunkeTemplateView setTemplateContentDrawable(Drawable contentDrawable){
        nativeContainer.setImageDrawable(contentDrawable);
        return this;
    }

    public YunkeTemplateView setTemplateLogoDrawable(Drawable logoDrawable){
        nativeIcon.setImageDrawable(logoDrawable);
        return this;
    }

    public YunkeTemplateView setTemplateCancel(Drawable cancelDrawable){
        nativeClose.setImageDrawable(cancelDrawable);
        return this;
    }

    public YunkeTemplateView lanchTemplate(ViewGroup container, Object response, TemplateViewCallBack templateViewCallBack) {
        if (!SystemUtils.isFastDoubleClick()){
            this.templateViewCallBack = templateViewCallBack;
            if (templateViewCallBack!=null) {
                if (response instanceof GetAdResponse){
                    this.adCotent = ((GetAdResponse) response).getData();
                    this.adId = adCotent.getId();
                    if (!StringUtils.isEmpty(this.adCotent)){
                        templateViewCallBack.onAdLoad(YunkeTemplateView.this);
                    }
                }

                if (!StringUtils.isEmpty(adId) && !StringUtils.isEmpty(adCotent.getDesc()) && !StringUtils.isEmpty(adCotent.getTitle()) && !StringUtils.isEmpty(adCotent.getSrc())) {

                    nativeBtn.setVisibility(GONE);
                    GlidImageManager.getInstance().loadImageView(mContext,adCotent.getSrc(),nativeContainer,R.drawable.yunke_ic_default_image);
                    GlidImageManager.getInstance().loadImageView(mContext,adCotent.getSrc(),nativeIcon,R.drawable.yunke_ic_default_image);
                    nativeClose.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            container.removeAllViews();
                            templateViewCallBack.onAdCancel(YunkeTemplateView.this);
                        }
                    });

                    nativeContainer.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            clickAdTask();
                        }
                    });

                    if (BaseRealVisibleUtil.getInstance(mContext).isViewCovered(nativeContainer) || nativeContainer.getLocalVisibleRect(new Rect())){
                        showAdTask();
                    }
                }
            }

        }
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
                                templateViewCallBack.onAdShow(YunkeTemplateView.this);
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                templateViewCallBack.onAdError(err);
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
                                templateViewCallBack.onAdClicked(YunkeTemplateView.this);
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                templateViewCallBack.onAdError(err);
                            }
                        });
                    }
                });
            }
        });

    }

    public interface TemplateViewCallBack{
        void onAdClicked(YunkeTemplateView templateView);
        void onAdShow(YunkeTemplateView templateView);
        void onAdError(String err);
        void onAdCancel(YunkeTemplateView templateView);
        void onAdLoad(YunkeTemplateView templateView);
    }

}

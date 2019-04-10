package com.shykad.yunke.sdk.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

/**
 * Created by wanghong.he on 2019/1/5.
 * Class Note:
 */

public class YunKeWebview extends WebView {
    public YunKeWebview(Context context) {
        super(context);
        webViewSetting();
    }

    public YunKeWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
        webViewSetting();
    }

    public YunKeWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        webViewSetting();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public YunKeWebview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        webViewSetting();
    }

    public YunKeWebview(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        webViewSetting();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void webViewSetting() {

        WebSettings webSettings = getSettings();

        getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setBlockNetworkImage(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);//设置js可以直接打开窗口，如window.open()，默认为false
        webSettings.setJavaScriptEnabled(true);//是否允许JavaScript脚本运行，默认为false。设置true时，会提醒可能造成XSS漏洞
        webSettings.setSupportZoom(true);//是否可以缩放，默认true
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放。大视图模式
        webSettings.setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
        webSettings.setAppCacheEnabled(true);//是否使用缓存
        webSettings.setDomStorageEnabled(true);//开启本地DOM存储
        webSettings.setLoadsImagesAutomatically(true); // 加载图片
        webSettings.setMediaPlaybackRequiresUserGesture(false);//播放音频，多媒体需要用户手动？设置为false为可自动播放
        webSettings.setAllowFileAccess(true);//访问文件
        webSettings.setLoadsImagesAutomatically(true);//加载图片
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setDatabaseEnabled(true);
        webSettings.setTextZoom(100);
        webSettings.setBuiltInZoomControls(true);//支持内置缩放控件
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//使用缓存
        final String dbPath = getContext().getDir("db", Context.MODE_PRIVATE).getPath();
        webSettings.setDatabasePath(dbPath);
        if (Build.VERSION.SDK_INT >= 19) {//硬件加速器的使用
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//js
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    public void onDestroy() {
        //需要自己手动去调一下才能释放资源。否则就算依赖的 activity 或者 fragment 不在了，资源还是不会被释放干净。
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        try {
            // 要求api>11 未移除有风险的webview系统的隐藏接口-通过显示调用removeJavascriptInterface移除3个隐藏的系统接口：
            // ‘searchBoxJavaBridge_’,‘accessibilityTraversal’以及’accessibility’。
            if (Build.VERSION.SDK_INT >= 11) {
                removeJavascriptInterface("searchBoxJavaBridge_");
                removeJavascriptInterface("accessibilityTraversal");
                removeJavascriptInterface("accessibility");
            }
            getSettings().setJavaScriptEnabled(false);
            clearHistory();
            clearView();
            clearFormData();
            clearCache(true);
            clearSslPreferences();
            removeAllViews();
            this.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

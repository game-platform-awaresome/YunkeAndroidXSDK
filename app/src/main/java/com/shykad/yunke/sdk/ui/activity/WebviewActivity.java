package com.shykad.yunke.sdk.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.ui.widget.YunKeWebview;
import com.shykad.yunke.sdk.utils.AndroidBug5497Workaround;

/**
 * Create by wanghong.he on 2019/3/7.
 * description：
 */
public class WebviewActivity extends Activity {

    private String loadUrl;
    private String title;
    public static final String GET_URL = "url";
    public static final String GET_TITLE="title";
    private TextView tvTitleCenter;
    private YunKeWebview webView;
    private ProgressBar mLoadingProgress;
    private LinearLayout left_ll, rootLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yunke_activity_webview);
        getIntentExtra();
        initView();
        initData();
    }

    private void initView() {
        mLoadingProgress = findViewById(R.id.progressBarLoading);
        webView = findViewById(R.id.webView);
        tvTitleCenter = findViewById(R.id.tv_title_center);
        left_ll = findViewById(R.id.left_ll);
        rootLayout = findViewById(R.id.content);
        left_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getIntentExtra() {
        Intent intent = getIntent();
        if (intent != null) {
            loadUrl = intent.getStringExtra(GET_URL);
            title = intent.getStringExtra(GET_TITLE);
        }
    }

    protected void initData() {

        tvTitleCenter.setText(title);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                //设置加载进度条
                view.setWebChromeClient(new WebChromeClientProgress());
                return true;

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.getSettings().setBlockNetworkImage(false);

            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        webView.loadUrl(loadUrl);
        AndroidBug5497Workaround.assistActivity(this);//解决webview中的输入框获得焦点后输入法盖住了输入框
    }

    private class WebChromeClientProgress extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (mLoadingProgress != null) {
                mLoadingProgress.setProgress(progress);
                if (progress == 100) mLoadingProgress.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, progress);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return true;
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams
                fileChooserParams) {
            return true;
        }

        //<3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        }

        //>3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        }

        //>4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        /**
         * 但是注意：webview调用destory时,webview仍绑定在Activity上
         * 这是由于自定义webview构建时传入了该Activity的context对象
         * 因此需要先从父容器中移除webview,然后再销毁webview:
         */
        rootLayout.removeView(webView);
        if (null != webView) {
            webView.onDestroy();
        }
        super.onDestroy();
    }
}

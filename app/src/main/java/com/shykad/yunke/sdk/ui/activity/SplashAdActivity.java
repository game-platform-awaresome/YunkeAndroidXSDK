package com.shykad.yunke.sdk.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.engine.SplashEngine;
import com.shykad.yunke.sdk.engine.YunKeEngine;
import com.shykad.yunke.sdk.engine.permission.config.PermissionConfig;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.ui.widget.YunkeSplashView;
import com.shykad.yunke.sdk.utils.LogUtils;
import com.shykad.yunke.sdk.utils.ShykadUtils;
import com.shykad.yunke.sdk.utils.WeakHandler;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.REQUEST_INSTALL_PACKAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Create by wanghong.he on 2019/3/7.
 * description：
 */
public class SplashAdActivity extends PermissionActivity implements WeakHandler.IHandler{

    GetAdResponse.AdCotent adCotent;
    YunkeSplashView splashView;
    ViewGroup splashContainer;
    TextView skipView;
    ImageView splashHolder,splashContainerView;
    private static final int MSG_GO_MAIN = 1;
    //是否强制跳转到主页面
    private boolean mForceGoMain = false;
    //开屏广告加载发生超时但是SDK没有及时回调结果的时候，做的一层保护。
    private final WeakHandler mHandler = new WeakHandler((WeakHandler.IHandler) this);
    //开屏广告是否已经加载
    private boolean mHasLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yunke_activity_splash_ad);
        init();
    }

    private void init() {
        skipView = findViewById(R.id.skip_view);
        splashContainer = findViewById(R.id.splash_container);
        splashHolder = findViewById(R.id.splash_holder);
        splashContainerView = findViewById(R.id.splash_container_view);

        splashView = new YunkeSplashView(SplashAdActivity.this);
        splashView.initView("1086106659408973836", new YunkeSplashView.SplashInitCallBack() {
            @Override
            public void initView(String slotId) {
                permissTask(slotId);

            }
        });
    }

    /**
     * 获取广告
     */
    private void getAd(String slotId) {
        YunKeEngine.getInstance(this).yunkeGetAd(HttpConfig.ADTYPE_SPLASH, slotId, ShykadUtils.getIMEI(this), new YunKeEngine.YunKeAdCallBack() {

            @Override
            public void getAdSuccess(Object response) {
                if (response instanceof GetAdResponse){
                    adCotent = ((GetAdResponse) response).getData();
                    launchSplash(response);
                }

            }

            @Override
            public void getAdFailed(String err) {
                if (!TextUtils.isEmpty(err)){
                    LogUtils.d(err);
                }
            }
        });
    }

    /**
     * 请求权限 强烈建议合适的时机调用 防止获取不了相应权限，下载广告应用没有安装或应用安装失败的问题
     * 权限请求如不满足需求，请自行开发
     */
    public void permissionInstallTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            boolean installs = getPackageManager().canRequestPackageInstalls();
            if (installs){
                //需要用户手动运行，拥有权限去执行
                LogUtils.i("已获取install package权限");
            }else {
                String[] perms = {REQUEST_INSTALL_PACKAGES};
                performCodeWithPermission(getString(R.string.rationale_install_package), PermissionConfig.REQUEST_INSTALL_PACKAGES_PERM, perms, new PermissionCallback() {
                    @Override
                    public void hasPermission(List<String> allPerms) {
                        //需要用户手动运行，拥有权限去执行
                        LogUtils.i("已获取install package权限");
                    }

                    @Override
                    public void noPermission(List<String> deniedPerms, List<String> grantedPerms, Boolean hasPermanentlyDenied) {
                        if (hasPermanentlyDenied) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                            startActivityForResult(intent, PermissionConfig.REQUEST_INSTALL_PACKAGES_PERM);
                            LogUtils.i("获取install package权限被拒");
                        }
                    }
                });
            }
        }

    }


    /**
     * 请求权限 强烈建议合适的时机调用 防止获取不了相应权限，下载广告没有填充或者获取广告失败的问题
     * 权限请求如不满足需求，请自行开发
     */
    public void permissTask(String slotId) {
        String[] perms = {READ_PHONE_STATE,ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION,WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE};
        performCodeWithPermission(getString(R.string.rationale_permissions), PermissionConfig.REQUEST_PERMISSIONS_PERM, perms, new PermissionCallback() {
            @Override
            public void hasPermission(List<String> allPerms) {
                getAd(slotId);//需要拥有权限去执行
                LogUtils.i("已获取phone、location、storage权限");
            }

            @Override
            public void noPermission(List<String> deniedPerms, List<String> grantedPerms, Boolean hasPermanentlyDenied) {
                if (hasPermanentlyDenied) {
                    alertAppSetPermission(getString(R.string.rationale_ask_again), PermissionConfig.REQUEST_PERMISSIONS_PERM);
                    LogUtils.i("获取phone、location、storage权限被拒");
                }
            }
        });
    }

    private void launchSplash(Object response) {

        new SplashEngine(SplashAdActivity.this, response, new SplashEngine.SplashAdCallBack() {
            @Override
            public void onSplashClick(boolean isJump) {
                if (isJump){
                    Intent intent = new Intent();
                    intent.putExtra("url",adCotent.getTarget());
                    intent.putExtra("title","广告");
                    jump(WebviewActivity.class,intent);
                }
            }

            @Override
            public void onSplashSkip() {
                LogUtils.d("展示广告,点击跳过");
                jump(SplashActivity.class,null);
            }

            @Override
            public void onSplashTimeOver() {
                LogUtils.d("展示广告,倒计时结束");
                jump(SplashActivity.class,null);
            }

            @Override
            public void onSplashErroe(String err,boolean isTimeout) {
                LogUtils.d("展示广告异常:"+err);
                if (isTimeout){
                    mHasLoaded = true;
                    jump(SplashActivity.class,null);
                }else {
                    mHasLoaded = true;
                    jump(SplashActivity.class,null);
                }
            }

            @Override
            public void onSplashShow() {
                LogUtils.d("展示广告");

                mHasLoaded = true;
                mHandler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onSplashPresent() {
                splashHolder.setVisibility(View.GONE); // 广告展示后一定要把预设的开屏图片隐藏起来
            }

        }).initSplash().launchSplash(splashView,splashContainer,splashContainerView,skipView,5 * 1000);

    }

    private void jump(Class<?> cls,Intent intent) {
        splashContainer.removeAllViews();
        if (intent!=null){
            intent.setClass(SplashAdActivity.this,cls);
            startActivity(intent);
        }else {
            startActivity(new Intent(SplashAdActivity.this, cls));
        }
        this.finish();
    }

    /** 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //判断是否该跳转到主页面
        if (mForceGoMain) {
            mHandler.removeCallbacksAndMessages(null);
            jump(SplashActivity.class,null);
        }
        mForceGoMain = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mForceGoMain = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mForceGoMain = false;
    }

    @Override
    public void handleMsg(Message msg) {
        if (msg.what == MSG_GO_MAIN) {
            if (!mHasLoaded) {
                LogUtils.d("广告已超时，跳到主页面");
                jump(SplashActivity.class,null);
            }
        }
    }
}

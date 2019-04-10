package com.shykad.yunke.sdk.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.engine.BannerEngine;
import com.shykad.yunke.sdk.engine.YunKeEngine;
import com.shykad.yunke.sdk.engine.permission.config.PermissionConfig;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.ui.widget.banner.YunKeBanner;
import com.shykad.yunke.sdk.utils.LogUtils;
import com.shykad.yunke.sdk.utils.ShykadUtils;

import java.util.List;

import androidx.annotation.Nullable;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.REQUEST_INSTALL_PACKAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Create by wanghong.he on 2019/2/27.
 * description：Banner 广告分为文字链、纯图片和图文描述：
 */
public class BannerActivity extends PermissionActivity {

    Button bannerNative;
    YunKeBanner bannerView;
    ViewGroup bannerContainer;
    String posId = "1086106659408973835";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.yunke_activity_banner_ad);

        init();
    }

    private void init() {
        bannerView = new YunKeBanner(BannerActivity.this);
        bannerContainer = findViewById(R.id.banner_container);
        bannerNative = findViewById(R.id.btn_banner_native);
        bannerNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissTask(posId);
            }
        });

    }

    /**
     * 获取广告
     */
    private void getAd(String slotid) {
        YunKeEngine.getInstance(this).yunkeGetAd(HttpConfig.ADTYPE_BANNER, slotid, ShykadUtils.getIMEI(this), new YunKeEngine.YunKeAdCallBack() {

            @Override
            public void getAdSuccess(Object response) {
                launchBanner(response);
            }

            @Override
            public void getAdFailed(String err) {
                if (!TextUtils.isEmpty(err)) {
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
     * 请求权限 强烈建议合适的时机调用 防止获取不了imei 下载广告没有填充或者获取广告失败的问题
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

    /**
     * 广告轮播样式 可直接使用YunkeBannerVew 在布局中使用（去掉外部的Fragmentlayout）
     * initBanner必须先于launcheBanner 调用！！！
     */
    public void launchBanner(Object response){
        new BannerEngine(response, BannerActivity.this, new BannerEngine.BannerAdCallBack() {
            @Override
            public void onBannerClick(boolean isJump) {
                if (isJump && (response instanceof GetAdResponse)){

                    Intent intent = new Intent();
                    intent.setClass(BannerActivity.this, WebviewActivity.class);
                    intent.putExtra("url",((GetAdResponse) response).getData().getTarget());
                    intent.putExtra("title","广告");
                    startActivity(intent);
                }

            }

            @Override
            public void onBannerErroe(String err) {
                LogUtils.d(err);
            }

            @Override
            public void onBannerShow() {
                LogUtils.d("广告展示");
            }
        }).initBanner().launchBanner(bannerView,bannerContainer,30*1000);

    }
}

package com.shykad.yunke.sdk.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.engine.InterstitialEngine;
import com.shykad.yunke.sdk.engine.YunKeEngine;
import com.shykad.yunke.sdk.engine.permission.config.PermissionConfig;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.utils.LogUtils;
import com.shykad.yunke.sdk.utils.ShykadUtils;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.REQUEST_INSTALL_PACKAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Create by wanghong.he on 2019/3/8.
 * description：插屏
 */
public class InterstitialActivity extends PermissionActivity{

    private Button interstitialBtn;
    private GetAdResponse.AdCotent adCotent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yunke_activity_insert_ad);

        init();
    }

    private void init() {
        interstitialBtn = findViewById(R.id.show_ad_interstitial);
        interstitialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissTask("1086106659408973837");
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

    /**
     * 获取广告
     */
    private void getAd(String slotId) {
        YunKeEngine.getInstance(this).yunkeGetAd(HttpConfig.ADTYPE_INTERSTITIAL, slotId, ShykadUtils.getIMEI(this), new YunKeEngine.YunKeAdCallBack() {

            @Override
            public void getAdSuccess(Object response) {
                if (response instanceof GetAdResponse) {
                    adCotent = ((GetAdResponse) response).getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lanuchInterstitial(response);
                        }
                    });
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
     * 拉起插屏广告--为popupwindow
     */
    private void lanuchInterstitial(Object response) {
        InterstitialEngine.getInstance(InterstitialActivity.this).initEngine(response,new InterstitialEngine.InterstitialAdCallBack() {
            @Override
            public void onInterstitialClick(boolean isJump) {
                if (isJump){
                    Intent intent = new Intent();
                    intent.putExtra("url",adCotent.getTarget());
                    intent.putExtra("title","广告");
                    jump(WebviewActivity.class,intent);
                    LogUtils.d("点击插屏广告");
                }

            }

            @Override
            public void onInterstitialShow() {
                LogUtils.d("插屏广告展示");
            }

            @Override
            public void onInterstitialErroe(String err) {
                LogUtils.d("插屏广告异常："+err);
            }

            @Override
            public void onInterstitialClose() {
                LogUtils.d("插屏广告关闭");
            }

        }).launchInterstitial(R.layout.yunke_activity_insert_ad);
    }

    private void jump(Class<?> cls, Intent intent) {
        if (intent!=null){
            intent.setClass(InterstitialActivity.this,cls);
            startActivity(intent);
        }else {
            startActivity(new Intent(InterstitialActivity.this, cls));
        }
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InterstitialEngine.getInstance(InterstitialActivity.this).destoryIntertitalAd();
    }
}

package com.shykad.yunke.sdk.manager;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Pair;

import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdManagerFactory;
import com.shykad.yunke.sdk.engine.YunKeEngine;
import com.shykad.yunke.sdk.okhttp.OkHttpUtils;
import com.shykad.yunke.sdk.okhttp.bean.AdAppidResponse;
import com.shykad.yunke.sdk.okhttp.https.HttpsUtils;
import com.shykad.yunke.sdk.okhttp.log.LoggerInterceptor;
import com.shykad.yunke.sdk.utils.AppUtils;
import com.shykad.yunke.sdk.utils.LogUtils;
import com.shykad.yunke.sdk.utils.SPUtil;

import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

import static com.shykad.yunke.sdk.ShykadApplication.getAppContext;
import static com.shykad.yunke.sdk.ShykadApplication.getAppResources;

/**
 * Create by wanghong.he on 2019/2/26.
 * description：
 */
public class ShykadManager {

    private OkHttpClient okHttpClient;

    private static Context mContext;

    public static final ExecutorService INIT_EXECUTOR = Executors.newSingleThreadExecutor();

    private static ShykadManager instance;

    private ShykadManager(){

    }

    public static ShykadManager getInstance(Context context){
        mContext = context;
        if(instance == null){
            synchronized (ShykadManager.class){
                if(instance == null) instance = new ShykadManager();
            }
        }
        return instance;
    }

    /**
     * key : view id
     * pair.first: button content
     * pair.second: intent action
     */
    private static Map<String, Pair<String, String>> launcherMap = new HashMap<>();
    TTAdManager ttAdManager = TTAdManagerFactory.getInstance(mContext);

    public void init(String appId){
        initShykad(appId);
    }

    private void initShykad(String appId){
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
        okHttpClient = getUnsafeOkHttpClient().newBuilder()
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor(""))
                .addNetworkInterceptor(new LoggerInterceptor(""))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);

        YunKeEngine.getInstance(mContext).yunkeInit(appId, new YunKeEngine.YunKeInitCallBack() {
            @Override
            public void initSuccess(Object response) {
                LogUtils.d("初始化成功");
                if (response instanceof AdAppidResponse){
                    String txAppID = ((AdAppidResponse) response).getTxAppId();
                    String ttAppID = ((AdAppidResponse) response).getTtAppId();

                    SPUtil.insert(mContext,SPUtil.TX_APPID,txAppID);
                    SPUtil.insert(mContext,SPUtil.TT_APPID,ttAppID);

                    initCSJ();
                    initGDT();
                }

            }

            @Override
            public void initFailed(String err) {
                LogUtils.d("初始化异常："+err);
            }
        });
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initCSJ(){
        //强烈建议在应用对应的Application#onCreate()方法中调用，避免出现content为null的异常
        TTAdManagerHolder.init(mContext);
        ttAdManager.setAppId((String) SPUtil.get(mContext,SPUtil.TT_APPID,""));
        ttAdManager.setName(AppUtils.getPackageName(mContext));
    }

    private void initGDT(){
        try {
            String packageName = mContext.getPackageName();
            //Get all activity classes in the AndroidManifest.xml
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            if (packageInfo.activities != null) {
                for (ActivityInfo activity : packageInfo.activities) {
                    Bundle metaData = activity.metaData;
                    if (metaData != null && metaData.containsKey("id")
                            && metaData.containsKey("content") && metaData.containsKey("action")) {
                        LogUtils.e("yunke-gdt", activity.name);
                        try {
                            Class.forName(activity.name);
                        } catch (ClassNotFoundException e) {
                            continue;
                        }
                        String id = metaData.getString("id");
                        String content = metaData.getString("content");
                        String action = metaData.getString("action");
                        register(action, id, content);//将清单文件中每个activity的id和action读取并存入launcherMap
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            LogUtils.e("yunke-gdt","找不到配置信息的activity,请确保已配置Activity相应的ID、ACTION、CONTENT.");
        }
    }

    private void register(String action, String id, String content) {
        launcherMap.put(action, new Pair<>(id, content));
    }

    public static int getGDTRegisterId(){
        Iterator<Map.Entry<String, Pair<String, String>>> iterator = launcherMap.entrySet().iterator();
        int id = -1;

        while (iterator.hasNext()) {
            Map.Entry<String, Pair<String, String>> entry = iterator.next();
            final Pair<String, String> pair = entry.getValue();
            id = getAppResources().getIdentifier(pair.first, "id", mContext.getPackageName());
        }

        return id;
    }

    public static String getGDTRegisterContent(){
        Iterator<Map.Entry<String, Pair<String, String>>> iterator = launcherMap.entrySet().iterator();
        String content = "";
        while (iterator.hasNext()) {
            Map.Entry<String, Pair<String, String>> entry = iterator.next();
            final Pair<String, String> pair = entry.getValue();
            content = pair.second;
        }
        return content;
    }

    public static String getGDTRegisterAction(){
        Iterator<Map.Entry<String, Pair<String, String>>> iterator = launcherMap.entrySet().iterator();
        String action = "";
        while (iterator.hasNext()) {
            Map.Entry<String, Pair<String, String>> entry = iterator.next();
            action = entry.getKey();
        }
        return action;
    }

}

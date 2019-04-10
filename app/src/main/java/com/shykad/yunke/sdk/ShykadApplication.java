package com.shykad.yunke.sdk;

import android.content.Context;
import android.content.res.Resources;

import com.shykad.yunke.sdk.manager.ShykadManager;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

/**
 * Create by wanghong.he on 2019/2/26.
 * description：
 */
public class ShykadApplication extends MultiDexApplication {


    public static Context appContext;

    /**
     * @return the main context of the Application
     */
    public static Context getAppContext() {
        return appContext;
    }

    /**
     * @return the main resources from the Application
     */
    public static Resources getAppResources() {
        return appContext.getResources();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        ShykadManager.getInstance(appContext).init("1086104688845262849");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}

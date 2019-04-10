package com.shykad.yunke.sdk.manager;

/**
 * Create by wanghong.he on 2019/3/5.
 * description：
 */
public class TencentManager {

    private static TencentManager instance;

    private TencentManager(){

    }

    public static TencentManager getInstance(){
        if(instance == null){
            synchronized (TencentManager.class){
                if(instance == null) instance = new TencentManager();
            }
        }
        return instance;
    }

    public void showAd(Object response){

    }
}

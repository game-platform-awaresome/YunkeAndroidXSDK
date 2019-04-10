package com.shykad.yunke.sdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


import static com.shykad.yunke.sdk.config.HttpConfig.ACTION_BROADCAST_MSG;

/**
 * Create by wanghong.he on 2019/3/4.
 * description：
 */
public class PluginService extends Service {



    private final IBinder mBinder = new LocalBinder();



    @Override

    public void onCreate() {

        super.onCreate();

        sendMessage("onCreate");

    }



    @Override

    public int onStartCommand(Intent intent, int flags, int startId) {

        sendMessage("onStartCommand");



        return super.onStartCommand(intent, flags, startId);

    }



    @Override

    public void onDestroy() {

        sendMessage("onDestroy");

        super.onDestroy();

    }



    @Override

    public IBinder onBind(Intent intent) {

        sendMessage("onBind");

        return mBinder;

    }



    @Override

    public boolean onUnbind(Intent intent) {

        sendMessage("onUnbind");



        return super.onUnbind(intent);

    }



    private void sendMessage(String message) {

        Intent intent = new Intent(ACTION_BROADCAST_MSG);

        intent.putExtra("result","[PluginService] " + message);

        sendBroadcast(intent);

    }



    public class LocalBinder extends Binder {

        public PluginService getService() {

            return PluginService.this;

        }

    }
}

package com.shykad.yunke.sdk.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import static com.shykad.yunke.sdk.config.HttpConfig.ACTION_BROADCAST_MSG;

/**
 * Create by wanghong.he on 2019/3/4.
 * description：
 */
public class PluginIntentService extends IntentService {



    public PluginIntentService() {

        super("PluginIntentService");

    }



    @Override

    public void onCreate() {

        super.onCreate();

        sendMessage("onCreate");

    }



    @Override

    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        sendMessage("onStartCommand");

        return super.onStartCommand(intent, flags, startId);

    }



    @Override

    public void onDestroy() {

        sendMessage("onDestroy");

        super.onDestroy();

    }



    @Override

    protected void onHandleIntent(@Nullable Intent intent) {

        sendMessage("onHandleIntent");

    }



    private void sendMessage(String message) {

        Intent intent = new Intent(ACTION_BROADCAST_MSG);

        intent.putExtra("result", "[PluginIntentService] " + message);

        sendBroadcast(intent);

    }
}

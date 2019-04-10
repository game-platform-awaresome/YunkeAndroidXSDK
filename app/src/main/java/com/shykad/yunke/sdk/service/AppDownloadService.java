package com.shykad.yunke.sdk.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.bytedance.sdk.openadsdk.TTGlobalAppDownloadController;
import com.shykad.yunke.sdk.manager.TTAdManagerHolder;

public class AppDownloadService extends Service {
    public AppDownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        int action = intent.getIntExtra("action", 0);
        long id = intent.getLongExtra("id", -1L);
        int internalStatusKey = intent.getIntExtra("internalStatusKey", -1);
        TTGlobalAppDownloadController controller = TTAdManagerHolder.get()
                .getGlobalAppDownloadController(getApplicationContext());
        if (controller == null) {
            return;
        }

        switch (action) {
            case AppDownloadStatusListener.DOWNLOAD_STATUS_ACTIVE:
            case AppDownloadStatusListener.DOWNLOAD_STATUS_WATING:
                controller.changeDownloadStatus(internalStatusKey, id);
                break;
            case AppDownloadStatusListener.DOWNLOAD_STATUS_FINISH:
                hideNotification(id);
                controller.changeDownloadStatus(internalStatusKey, id);
                break;
            case AppDownloadStatusListener.DOWNLOAD_STATUS_DELETE:
                controller.removeDownloadTask(id);
                hideNotification(id);
                break;
            case 0:
                hideNotification(id);
                break;
            default:
                break;
        }
    }

    private void hideNotification(long id) {
        NotificationManager cancelNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (cancelNotificationManager == null) return;
        cancelNotificationManager.cancel((int) id);
    }

}

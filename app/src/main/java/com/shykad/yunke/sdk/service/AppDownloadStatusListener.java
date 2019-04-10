package com.shykad.yunke.sdk.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import com.bytedance.sdk.openadsdk.TTAppDownloadInfo;
import com.bytedance.sdk.openadsdk.TTGlobalAppDownloadListener;
import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.utils.LogUtils;
import com.shykad.yunke.sdk.utils.StringUtils;

import androidx.core.app.NotificationCompat;


/**
 * 实现TTGlobalAppDownloadListener接口，实现监听SDK内部下载进度状态回调
 * 如果你不允许SDK内部弹出Notification,可以在此回调中自如弹出Notification
 */

@SuppressWarnings("WeakerAccess")
public class AppDownloadStatusListener implements TTGlobalAppDownloadListener {
    public static final int DOWNLOAD_STATUS_ACTIVE = 1;
    public static final int DOWNLOAD_STATUS_WATING = 2;
    public static final int DOWNLOAD_STATUS_FINISH = 3;
    public static final int DOWNLOAD_STATUS_DELETE = 4;
    public static final int DOWNLOAD_STATUS_FAILED = 5;

    private final Context mContext;

    public AppDownloadStatusListener(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onDownloadActive(TTAppDownloadInfo info) {
//        updateNotification(DOWNLOAD_STATUS_ACTIVE, info);
        LogUtils.d("TTGlobalDownload", "下载中----" + info.getAppName() + "---" + getDownloadPercent(info) + "%");
    }

    @Override
    public void onDownloadPaused(TTAppDownloadInfo info) {
//        updateNotification(DOWNLOAD_STATUS_WATING, info);
        LogUtils.d("TTGlobalDownload", "暂停----" + info.getAppName() + "---" + getDownloadPercent(info) + "%");


    }

    @Override
    public void onDownloadFinished(TTAppDownloadInfo info) {
//        updateNotification(DOWNLOAD_STATUS_FINISH, info);
        LogUtils.d("TTGlobalDownload", "下载完成----" + info.getAppName() + "---" + getDownloadPercent(info) + "%" + "  file: " + info.getFileName());

    }

    @Override
    public void onInstalled(String pkgName, String appName, long id, int status) {
        LogUtils.d("TTGlobalDownload", "安装完成----" + "pkgName: " + pkgName);
    }

    @Override
    public void onDownloadFailed(TTAppDownloadInfo info) {
//        updateNotification(DOWNLOAD_STATUS_FINISH, info);
    }

    private int getDownloadPercent(TTAppDownloadInfo info) {
        if (info == null) {
            return 0;
        }
        double percentD = 0D;
        try {
            percentD = (double) info.getCurrBytes() / (double) info.getTotalBytes();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        int percent = (int) (percentD * 100);
        if (percent < 0) {
            percent = 0;
        }
        return percent;
    }


    @SuppressWarnings({"deprecation", "unused"})
    private void updateNotification(int type, TTAppDownloadInfo info) {
        if (info == null) {
            return;
        }

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(
                mContext);

        RemoteViews remoteView = new RemoteViews(mContext.getPackageName(), R.layout.yunke_download_notification_layout);
        int smallIcon = android.R.drawable.stat_sys_warning;
        switch (type) {
            case DOWNLOAD_STATUS_ACTIVE:
                smallIcon = android.R.drawable.stat_sys_download;
                break;
            case DOWNLOAD_STATUS_WATING:
                smallIcon = android.R.drawable.stat_sys_warning;
                break;
            case DOWNLOAD_STATUS_FINISH:
                smallIcon = android.R.drawable.stat_sys_download_done;
                break;
        }
        remoteView.setImageViewResource(R.id.icon, smallIcon);
        remoteView.setTextViewText(R.id.desc, info.getAppName());

        int contentAction = DOWNLOAD_STATUS_DELETE;
        int action = 1;
        String downloadSize = "";
        String downloadStatus = "";
        String buttonText = "";
        switch (type) {
            case DOWNLOAD_STATUS_ACTIVE:
                downloadSize = StringUtils.bytesToHuman(info.getCurrBytes()) + "/" + StringUtils.bytesToHuman(info.getTotalBytes());
                downloadStatus = "正在下载";
                buttonText = "暂停";
                remoteView.setViewVisibility(R.id.download_success, View.GONE);
                remoteView.setViewVisibility(R.id.download_text, View.VISIBLE);
                action = DOWNLOAD_STATUS_WATING;
                break;
            case DOWNLOAD_STATUS_WATING:
                downloadSize = StringUtils.bytesToHuman(info.getCurrBytes()) + "/" + StringUtils.bytesToHuman(info.getTotalBytes());
                downloadStatus = "暂停";
                buttonText = "继续";
                remoteView.setViewVisibility(R.id.download_success, View.GONE);
                remoteView.setViewVisibility(R.id.download_text, View.VISIBLE);
                action = DOWNLOAD_STATUS_ACTIVE;
                break;
            case DOWNLOAD_STATUS_FINISH:
                downloadSize = StringUtils.bytesToHuman(info.getTotalBytes());
                downloadStatus = "点击安装";
                buttonText = "安装";
                remoteView.setViewVisibility(R.id.download_success, View.VISIBLE);
                remoteView.setViewVisibility(R.id.download_text, View.GONE);
                remoteView.setViewVisibility(R.id.action, View.GONE);
                action = DOWNLOAD_STATUS_FINISH;
                contentAction = DOWNLOAD_STATUS_FINISH;
                break;
            case DOWNLOAD_STATUS_FAILED:
                downloadSize = StringUtils.bytesToHuman(info.getTotalBytes());
                downloadStatus = "下载失败";
                buttonText = "重新下载";
                remoteView.setViewVisibility(R.id.download_success, View.VISIBLE);
                remoteView.setViewVisibility(R.id.download_text, View.GONE);
                remoteView.setViewVisibility(R.id.action, View.GONE);
                action = DOWNLOAD_STATUS_FINISH;
                contentAction = DOWNLOAD_STATUS_FINISH;
                break;
        }

        //删除通知栏 intent
        Intent hintIntent = new Intent(mContext, AppDownloadService.class);
        hintIntent.putExtra("action", DOWNLOAD_STATUS_DELETE);
        hintIntent.putExtra("id", info.getId());
        notifyBuilder.setDeleteIntent(PendingIntent.getService(mContext, (int) SystemClock.uptimeMillis(), hintIntent, 0));


        Intent contentIntent = new Intent(mContext, AppDownloadService.class);
        contentIntent.putExtra("action", contentAction);
        contentIntent.putExtra("id", info.getId());
        contentIntent.putExtra("internalStatusKey", info.getInternalStatusKey());
        notifyBuilder.setContentIntent(PendingIntent.getService(mContext, (int) SystemClock.uptimeMillis(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        notifyBuilder.setAutoCancel(true);

        buttonText = buttonText + info.getId();
        downloadStatus = downloadStatus + info.getId();
        remoteView.setTextViewText(R.id.download_size, downloadSize);
        remoteView.setTextViewText(R.id.download_status, downloadStatus);
        remoteView.setTextViewText(R.id.download_success_size, downloadSize);
        remoteView.setTextViewText(R.id.download_success_status, downloadStatus);
        remoteView.setTextViewText(R.id.action, buttonText);

        Intent intent = new Intent(mContext, AppDownloadService.class);
        intent.putExtra("action", action);
        intent.putExtra("id", info.getId());
        intent.putExtra("internalStatusKey", info.getInternalStatusKey());
        notifyBuilder.setAutoCancel(true);
        remoteView.setOnClickPendingIntent(R.id.action, PendingIntent.getService(mContext,
                (int) info.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
        notifyBuilder.setSmallIcon(smallIcon);
        notifyBuilder.setContent(remoteView);
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(info.getId() + "", (int) info.getId(), notifyBuilder.build());
        }

    }

}

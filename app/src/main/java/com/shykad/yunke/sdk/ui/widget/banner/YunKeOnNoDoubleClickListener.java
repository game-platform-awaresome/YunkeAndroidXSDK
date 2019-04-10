package com.shykad.yunke.sdk.ui.widget.banner;

import android.view.View;

/**
 * Create by wanghong.he on 2019/3/6.
 * description：
 */
public abstract class YunKeOnNoDoubleClickListener implements View.OnClickListener {

    private int mThrottleFirstTime = 1000;

    private long mLastClickTime = 0;


    public YunKeOnNoDoubleClickListener() {

    }


    public YunKeOnNoDoubleClickListener(int throttleFirstTime) {

        mThrottleFirstTime = throttleFirstTime;

    }


    @Override

    public void onClick(View v) {

        long currentTime = System.currentTimeMillis();

        if (currentTime - mLastClickTime > mThrottleFirstTime) {

            mLastClickTime = currentTime;

            onNoDoubleClick(v);

        }

    }


    public abstract void onNoDoubleClick(View v);

}
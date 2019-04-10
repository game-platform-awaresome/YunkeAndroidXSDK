package com.shykad.yunke.sdk.ui.widget.banner;

import android.content.Context;
import android.widget.Scroller;


/**
 * Create by wanghong.he on 2019/3/6.
 * description：
 */
public class YunKeBannerScroller extends Scroller {

    private int mDuration = 1000;


    public YunKeBannerScroller(Context context, int duration) {

        super(context);

        mDuration = duration;

    }


    @Override

    public void startScroll(int startX, int startY, int dx, int dy) {

        super.startScroll(startX, startY, dx, dy, mDuration);

    }


    @Override

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {

        super.startScroll(startX, startY, dx, dy, mDuration);

    }

}
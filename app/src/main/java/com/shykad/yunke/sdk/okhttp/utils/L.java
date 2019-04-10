package com.shykad.yunke.sdk.okhttp.utils;

import android.util.Log;

import com.shykad.yunke.sdk.utils.LogUtils;

/**
 * Created by zhy on 15/11/6.
 */
public class L
{
    private static boolean debug = false;

    public static void e(String msg)
    {
        if (debug)
        {
            LogUtils.e("OkHttp", msg);
        }
    }

}


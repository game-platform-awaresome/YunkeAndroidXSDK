package com.shykad.yunke.sdk.okhttp.builder;


import com.shykad.yunke.sdk.okhttp.OkHttpUtils;
import com.shykad.yunke.sdk.okhttp.request.OtherRequest;
import com.shykad.yunke.sdk.okhttp.request.RequestCall;

/**
 * Created by zhy on 16/3/2.
 */
public class HeadBuilder extends GetBuilder
{
    @Override
    public RequestCall build()
    {
        return new OtherRequest(null, null, OkHttpUtils.METHOD.HEAD, url, tag, params, headers,id).build();
    }
}

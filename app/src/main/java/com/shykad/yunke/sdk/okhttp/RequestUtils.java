package com.shykad.yunke.sdk.okhttp;

import com.google.gson.Gson;
import com.shykad.yunke.sdk.okhttp.callback.BitmapCallback;
import com.shykad.yunke.sdk.okhttp.callback.Callback;
import com.shykad.yunke.sdk.okhttp.callback.FileCallBack;
import com.shykad.yunke.sdk.okhttp.callback.StringCallback;

import java.io.File;

import okhttp3.MediaType;

/**
 * Create by wanghong.he on 2019/2/28.
 * description：
 */
public class RequestUtils {

    private static RequestUtils instance;

    private RequestUtils(){

    }

    public static RequestUtils getInstance(){
        if(instance == null){
            synchronized (RequestUtils.class){
                if(instance == null) instance = new RequestUtils();
            }
        }
        return instance;
    }

    /**
     * json请求
     * @param url
     * @param params
     * @param requestCallBack
     */
    public void doPostJsonRequest(String url,Object params,RequestCallBack requestCallBack){
        OkHttpUtils
                .postString()
                .url(url)
                .content(new Gson().toJson(params))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(requestCallBack);

    }

    /**
     * 上传文件
     * @param url
     * @param file
     * @param stringCallBack
     */
    public void doPostUpFile(String url, File file, StringCallBack stringCallBack){
        OkHttpUtils
                .postFile()
                .url(url)
                .file(file)
                .build()
                .execute(stringCallBack);
    }

    /**
     * 下载文件
     * @param url
     * @param downloadCallBack
     */
    public void doPostDownload(String url,DownloadCallBack downloadCallBack){
        OkHttpUtils.get()
                .url(url)
                .build()
                .execute(downloadCallBack);
    }

    /**
     * 显示图片
     * @param url 图片地址
     * @param imageCallBack
     */
    public void doPostBitmap(String url,ImageCallBack imageCallBack){
        OkHttpUtils.get()
                .url(url)
                .tag(this)
                .build()
                .connTimeOut(20000)
                .readTimeOut(20000)
                .writeTimeOut(20000)
                .execute(imageCallBack);
    }

    public abstract static class StringCallBack extends StringCallback {

    }

    public abstract static class RequestCallBack extends Callback {

    }

    public abstract static class ImageCallBack extends BitmapCallback {

    }

    public abstract static class DownloadCallBack extends FileCallBack {

        public DownloadCallBack(String destFileDir, String destFileName) {
            super(destFileDir, destFileName);
        }
    }
}

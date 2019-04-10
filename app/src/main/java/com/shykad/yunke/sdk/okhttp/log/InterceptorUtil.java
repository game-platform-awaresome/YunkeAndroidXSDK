package com.shykad.yunke.sdk.okhttp.log;

import com.shykad.yunke.sdk.utils.LogUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Create by wanghong.he on 2019/2/28.
 * description：
 */
public class InterceptorUtil implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String TAG = "InterceptorUtil";

    @Override
    public Response intercept(Chain chain) throws IOException {
        //网络的请求信息
        Request request = chain.request();
        RequestBody requestBody = request.body();
        //网络返回的数据信息
        Response response = chain.proceed(request);
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);

        Charset charset = UTF8;
        MediaType contentType = requestBody.contentType();
        if (contentType != null) {
            try {
                charset = contentType.charset(UTF8);
            } catch (UnsupportedCharsetException e) {
                return response;
            }
        }
        String requestData = buffer.readString(charset);
        String method = request.method();
        String url = request.url().toString();
        LogUtils.d(TAG, "url:  " + url);
        LogUtils.d(TAG, "method:  " + method);
        if (requestData.length() > 5000) {
            requestData = requestData.substring(0, 5000);
        }
        LogUtils.d(TAG, "request---params" + format(requestData));
        ResponseBody responseBody = response.body();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer bufferResponse = source.buffer();

        String responseData = bufferResponse.clone().readString(charset);
        LogUtils.d(TAG, "response---data" + format(responseData));
        return response;
    }

    /**
     * 得到格式化json数据 退格用\t 换行用\r
     */
    public String format(String jsonStr) {
        int level = 0;
        jsonStr = "\n" + jsonStr;
        StringBuffer jsonForMatStr = new StringBuffer();
        for (int i = 0; i < jsonStr.length(); i++) {
            char c = jsonStr.charAt(i);
            if (level > 0
                    && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
            }
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }

    private static String getLevelStr(int level) {
        StringBuffer levelStr = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("    ");
        }
        return levelStr.toString();
    }
}
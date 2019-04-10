package com.shykad.yunke.sdk.okhttp.bean;

/**
 * Create by wanghong.he on 2019/2/28.
 * description：
 */
public class AdAppidResponse {

     String txAppId; // 腾迅 AppId
     String ttAppId; // 今日头条 AppId
     int code; // 0 表示接口成功 非 0 表示错误
     String message; // 错误消息

     public String getTxAppId() {
          return txAppId;
     }

     public void setTxAppId(String txAppId) {
          this.txAppId = txAppId;
     }

     public String getTtAppId() {
          return ttAppId;
     }

     public void setTtAppId(String ttAppId) {
          this.ttAppId = ttAppId;
     }

     public int getCode() {
          return code;
     }

     public void setCode(int code) {
          this.code = code;
     }

     public String getMessage() {
          return message;
     }

     public void setMessage(String message) {
          this.message = message;
     }
}

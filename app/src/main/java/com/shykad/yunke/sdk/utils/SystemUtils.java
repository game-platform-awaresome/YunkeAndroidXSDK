package com.shykad.yunke.sdk.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create by wanghong.he on 2019/3/6.
 * description：
 */
public class SystemUtils {

    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (timeD >= 0 && timeD <= 1000) {
            return true;
        } else {
            lastClickTime = time;
            return false;
        }
    }

    /**
     * 毫秒转日期
     *
     * @param millisecond
     * @return
     */
    public static String millisecond2Date(long millisecond) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        date.setTime(millisecond);
        return simpleDateFormat.format(date);
    }

    public static int getAccurateTime(int type,long millisecond){
        int year = 2018,month= 10,day= 26,hour= 19;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 用parse方法，可能会异常，所以要try-catch
            Date date = simpleDateFormat.parse(millisecond2Date(millisecond));
            // 获取日期实例
            Calendar calendar = Calendar.getInstance();
            // 将日历设置为指定的时间
            calendar.setTime(date);
            // 获取年
            year = calendar.get(Calendar.YEAR);
            // 这里要注意，月份是从0开始。
            month = calendar.get(Calendar.MONTH);
            // 获取天
            day = calendar.get(Calendar.DAY_OF_MONTH);
            //获取时
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (type) {
            case 0://年
                return year;
            case 1://月
                return month;
            case 2://日
                return day;
            case 3://时
                return hour;
            default:return -1;
        }
    }
    public static String utcDateToDate(String utcDate){
        try {
            //yyyy-MM-dd'T'HH:mm:ss.SSSZ
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            Date date = df.parse(utcDate);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return utcDate;
        }
    }

    public static boolean isEmail(String string) {
        if (string == null) {
            return false;
        }
//        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        String regEx1 = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(string);
        if (m.matches()){
            return true;
        }
        else {
            return false;
        }
    }


    public static boolean isPhone(String string) {
        if (string == null)
            return false;
        String regEx1 = "^[\\d]{11}";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(string);
        if (m.matches())
            return true;
        else
            return false;
    }

    public static boolean isSpecialChar(String str) {
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }
}

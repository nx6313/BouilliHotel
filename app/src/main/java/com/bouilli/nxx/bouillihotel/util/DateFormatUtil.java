package com.bouilli.nxx.bouillihotel.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 18230 on 2017/4/15.
 */

public class DateFormatUtil {
    /**
     * 日期格式"yyyy-MM-dd"
     */
    public static final String YYYYMMDD = "yyyy-MM-dd";

    /**
     * 日期格式"yyyy-MM"
     */
    public static final String YYYYMM = "yyyy-MM";
    /**
     * 日期格式"yyyyMM"
     */
    public static final String yyyymm = "yyyyMM";

    /**
     * 日期格式"yyyy"
     */
    public static final String YYYY = "yyyy";

    /**
     * 日期格式"MM"
     */
    public static final String MM = "MM";
    /**
     * 日期格式"dd"
     */
    public static final String dd = "dd";

    /**
     * 时间格式"HH:mm:ss"
     */
    public static final String HHMMSS = "HH:mm:ss";

    /**
     * "yyyy-MM-dd HH:mm:ss"
     */
    public static final String TYPE = "yyyy-MM-dd HH:mm:ss";

    /**
     * "yyyy-MM-dd HH:mm:ss:hm"
     */
    public static final String TYPE_ = "yyyy-MM-dd HH:mm:ss:SSS";

    /**
     * "HH:mm"
     */
    public static final String HHMM = "HH:mm";

    /**
     * "MM-dd"
     */
    public static final String MMDD = "MM-dd";

    /**
     * "MM月dd日"
     */
    public static final String MMDD1 = "MM月dd日";

    /**
     * yyyy年MM月dd日 HH时mm分
     */
    public static final String TYPE1 = "yyyy年MM月dd日 HH时mm分";

    /**
     * yyyy年MM月dd日
     */
    public static final String TYPE1_ = "yyyy 年 MM 月 dd 日";

    /**
     * yyyy-MM-dd HH:mm
     */
    public static final String TYPE2 = "yyyy-MM-dd HH:mm";

    /**
     * yyyyMMddHHmm
     */
    public static final String TYPE3 = "yyyyMMddHHmm";
    /**
     * yyyyMMddHHmmss
     */
    public static final String TYPE4 = "yyyyMMddHHmmss";
    /**
     * yy年MM月dd日 HH:mm:ss
     */
    public static final String TYPE5 = "yy年MM月dd日 HH:mm:ss";
    /**
     * yy-MM-dd HH:mm:ss
     */
    public static final String TYPE6 = "yy-MM-dd HH:mm:ss";

    /**
     * yyyyMMddHHmmssSSS
     */
    public static final String NUM_TYPE = "yyyyMMddHHmmssSSS";
    /**
     * yyyy/MM/dd HH:mm
     */
    public static final String NEW_TYPE1 = "yyyy/MM/dd HH:mm";
    /**
     * yyyy/MM/dd
     */
    public static final String NEW_TYPE2 = "yyyy/MM/dd";
    /**
     * yyyy/MM/dd HH:mm:ss
     */
    public static final String NEW_TYPE3 = "yyyy/MM/dd HH:mm:ss";

    /**
     * 将字符串转化为日期
     *
     * @param dateStr
     *            字符串的值
     * @param dateType
     *            要转化的类型
     * @return
     */
    public static Date strToDate(String dateStr, String dateType) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateType);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 日期转换为字符串格式
     *
     * @param date
     *            日期
     * @param dateType
     *            要转化的格式
     * @return
     */
    public static String dateToStr(Date date, String dateType) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateType);
        return sdf.format(date);
    }

    /**
     * 日期转换为字符串格式
     *
     * @param date
     *            日期
     * @return
     */
    public static String dateToStr(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(NUM_TYPE);
        return sdf.format(date);
    }

    /**
     * new Date()转换为指定日期格式
     *
     * @param dateType
     *            要转化的格式
     * @return
     */
    public static Date dateToDate(String dateType) {
        SimpleDateFormat sdf = new SimpleDateFormat(TYPE_);
        String dat = sdf.format(new Date());
        return strToDate(dat, dateType);
    }

    /**
     * date2比date1多的天数
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDays(Date date1, Date date2){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2-time1) / (1000*3600*24);
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * date2比date1多的小时数
     * @param date1
     * @param date2
     * @return
     */
    public static int differentHours(Date date1, Date date2){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();
        long between_hours = (time2-time1) / (1000*3600);
        return Integer.parseInt(String.valueOf(between_hours));
    }
}

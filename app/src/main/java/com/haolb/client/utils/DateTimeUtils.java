package com.haolb.client.utils;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeUtils {

    public static final int DATETIME_FIELD_REFERSH = 20; // 刷新时间(分钟)
    //
    public static final long ONE_SECOND = 1000L;
    public static final long ONE_MINUTE = ONE_SECOND * 60L;
    public static final long ONE_HOUR = ONE_MINUTE * 60L;
    public static final long ONE_DAY = ONE_HOUR * 24L;
    // 下面的pattern在print和parse时都可以使用
    public static final String MM_Yue_dd_Ri = "MM月dd日";
    public static final String yyyyMMdd = "yyyyMMdd";
    public static final String yyyy_MM_dd = "yyyy-MM-dd";
    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyy_MM_dd_HH_mm = "yyyy-MM-dd HH:mm";
    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
    public static final String yyyy_Nian_MM_Yue_dd_Ri = "yyyy年MM月dd日";
    public static final String MM_yy = "MM/yy";
    // 下面的pattern是print时用，parse时不应使用（只有时间，没有日期）
    public static final String HH_mm_ss = "HH:mm:ss";
    private static final String[] PATTERNS = { yyyy_MM_dd_HH_mm_ss, yyyy_MM_dd_HH_mm, yyyy_MM_dd, yyyyMMdd };

    public static void cleanCalendarTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    public static int getBirthIntervalDays(Calendar d1, Calendar d2) {
        if (d1 == null || d2 == null) {
            throw new IllegalArgumentException();
        }
        return (int) (Math.abs(d2.getTimeInMillis() - d1.getTimeInMillis() + ONE_HOUR) / ONE_DAY);
    }

    /**
     * 获得指定日期表示格式转换成Calendar的格式
     * 
     * @param src
     * @param fallback 若无法转换，返回一个默认值
     * @return
     */
    public static <T> Calendar getCalendar(T src, Calendar fallback) {
        if (src != null) {
            try {
                return getCalendar(src);
            } catch (Exception e) {
            }
        }
        fallback.setLenient(false);
        return fallback;
    }

    /**
     * 获得日期类型
     * 
     * @param src 任何可以表示时间的类型，目前支持Calendar,Date,long,String
     * @return Calendar类型表示的时间
     * @throws IllegalArgumentException
     */
    public static <T> Calendar getCalendar(T src) {
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        if (src == null) {
            return null;
        } else if (src instanceof Calendar) {
            calendar.setTimeInMillis(((Calendar) src).getTimeInMillis());
        } else if (src instanceof Date) {
            calendar.setTime((Date) src);
        } else if (src instanceof Long) {
            calendar.setTimeInMillis((Long) src);
        } else if (src instanceof String) {
            String nSrc = (String) src;
            try {
                // 直接匹配的时候不能匹配到月份或日期不是2位数的情况
                if (Pattern.compile("\\d{4}年\\d{1,2}月\\d{1,2}日").matcher(nSrc).find()) {
                    nSrc = fixDateString(nSrc);
                    return getCalendarByPattern(nSrc, yyyy_MM_dd);
                }
                return getCalendarByPatterns(nSrc, PATTERNS);
            } catch (Exception e) {
                try {
                    calendar.setTimeInMillis(Long.valueOf(nSrc));
                } catch (NumberFormatException e1) {
                    throw new IllegalArgumentException(e1);
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
        return calendar;
    }

    /** YYYY年MM月DD日 --> YYYY-MM-DD */
    private static String fixDateString(String date) {
        if (TextUtils.isEmpty(date)) {
            return date;
        }

        String[] dateArray = date.split("[年月日]");
        if (dateArray.length == 1) {
            dateArray = date.split("-");
        }
        for (int i = 0; i < 3; i++) {
            if (dateArray[i].length() == 1) {
                dateArray[i] = "0" + dateArray[i];
            }
        }
        return dateArray[0] + "-" + dateArray[1] + "-" + dateArray[2];
    }

    /**
     * 匹配pattern获得时间，若无法解析抛出异常
     * 
     * @param dateTimeStr
     * @param patternStr
     * @return
     * @throws IllegalArgumentException
     */
    public static Calendar getCalendarByPattern(String dateTimeStr, String patternStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(patternStr, Locale.US);
            sdf.setLenient(false);
            Date d = sdf.parse(dateTimeStr);
            Calendar c = Calendar.getInstance();
            c.setLenient(false);
            c.setTimeInMillis(d.getTime());
            return c;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 匹配pattern数组中的所有pattern解析时间格式，若没有可以解析的方式则抛出异常
     * 
     * @param dateTimeStr
     * @param patternStr
     * @return
     * @throws IllegalArgumentException
     */
    public static Calendar getCalendarByPatterns(String dateTimeStr, String[] patternStr) {
        for (String string : patternStr) {
            try {
                return getCalendarByPattern(dateTimeStr, string);
            } catch (Exception e) {
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * 是否有服务器时间
     */
    public static boolean hasServerTime;
    /**
     * 本地时间和服务器时间的间隔 time server local gap millis
     */
    public static long tslgapm;
    /**
     * 本地时间和服务器时间的间隔 time server string
     */
    public static String tss;

    /** 获取与服务器时间矫正过的当前时间 */
    public static Calendar getCurrentDateTime() {
        Calendar now = Calendar.getInstance();
        now.setLenient(false);
        if (hasServerTime) {
            now.setTimeInMillis(now.getTimeInMillis() + tslgapm);
        }
        return now;
    }

    public static Calendar getCurrentServerDate() {
        return getCalendar(tss);
    }

    /** 获得基准日期增加间隔天 */
    public static Calendar getDateAdd(Calendar start, int interval) {
        if (start == null) {
            return null;
        }
        Calendar c = (Calendar) start.clone();
        c.add(Calendar.DATE, interval);
        return c;
    }

    /**
     * 获得时间间隔
     * 
     * @param from
     * @param to
     * @param unit 时间间隔单位{@link DateTimeUtils#ONE_SECOND},{@link DateTimeUtils#ONE_MINUTE},
     *            {@link DateTimeUtils#ONE_HOUR}, {@link DateTimeUtils#ONE_DAY}
     * @return
     */
    public static long getIntervalTimes(Calendar from, Calendar to, long unit) {
        if (from == null || to == null) {
            throw new IllegalArgumentException();
        }
        return Math.abs(from.getTimeInMillis() - to.getTimeInMillis()) / unit;
    }

    /**
     * 获得日期间隔 忽略小时
     * 
     * @param from
     * @param to
     * @return
     */
    public static long getIntervalTimes(Calendar from, Calendar to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException();
        }
        cleanCalendarTime(from);
        cleanCalendarTime(to);
        return getIntervalTimes(from, to, ONE_DAY);
    }

    public static int getIntervalDays(String startdate, String enddate, String pattern) {
        int betweenDays = 0;
        if (startdate == null || enddate == null) {
            return betweenDays;
        }

        Calendar d1 = getCalendarByPattern(startdate, pattern);
        Calendar d2 = getCalendarByPattern(enddate, pattern);

        return getIntervalDays(d1, d2);
    }

    public static int getIntervalDays(Calendar from, Calendar to) {
        long dayMillis = 24 * 60 * 60 * 1000;
        int betweenDays = 0;
        Calendar c1 = Calendar.getInstance();
        c1.set(from.get(Calendar.YEAR), from.get(Calendar.MONTH), from.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        c1.set(Calendar.MILLISECOND, 0);
        Calendar c2 = Calendar.getInstance();
        c2.set(to.get(Calendar.YEAR), to.get(Calendar.MONTH), to.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        c2.set(Calendar.MILLISECOND, 0);
        long a = c1.getTimeInMillis();
        long b = c2.getTimeInMillis();
        long bt = Math.abs(a - b);
        betweenDays = (int) (bt / dayMillis);
        return betweenDays;
    }

    // 上车时间08:30,用时:1天23小时30分
    public static String getIntervalDaysForRailway(String startdate, String totalTime) {
        if (startdate == null || totalTime == null) {
            return "";
        }
        String[] startdates = startdate.split(":");
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startdates[0]));
        startCalendar.set(Calendar.MINUTE, Integer.parseInt(startdates[0]));

        Calendar endCalendar = (Calendar) startCalendar.clone();
        String patternStr = "((\\d+)天)?((\\d+)小时)?((\\d+)分)?";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(totalTime);
        if (matcher.find()) {
            String day = matcher.group(2);
            if (!TextUtils.isEmpty(day)) {
                endCalendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
            }
            String hour = matcher.group(4);
            if (!TextUtils.isEmpty(hour)) {
                endCalendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
            }
            String minute = matcher.group(6);
            if (!TextUtils.isEmpty(minute)) {
                endCalendar.add(Calendar.MINUTE, Integer.parseInt(minute));
            }
            long days = getIntervalDays(startCalendar, endCalendar);
            if (days == 1) {
                return "(次日)";
            } else if (days == 2) {
                return "(第三日)";
            }
        }
        return "";
    }

    /** calendar --> 周一～七 */
    public static String getWeekDayFromCalendar(Calendar cal) {
        if (cal == null) {
            throw new IllegalArgumentException();
        }
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
        case 1:
            return "周日";
        case 2:
            return "周一";
        case 3:
            return "周二";
        case 4:
            return "周三";
        case 5:
            return "周四";
        case 6:
            return "周五";
        case 7:
            return "周六";
        }
        throw new NullPointerException();
    }

    /** calendar --> 星期天～星期六 */
    public static String getWeekDayFromCalendar1(Calendar cal) {
        if (cal == null) {
            throw new IllegalArgumentException();
        }
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
        case 1:
            return "星期天";
        case 2:
            return "星期一";
        case 3:
            return "星期二";
        case 4:
            return "星期三";
        case 5:
            return "星期四";
        case 6:
            return "星期五";
        case 7:
            return "星期六";
        }
        throw new IllegalArgumentException();
    }

    /**
     * 判断是否是闰年
     * 
     * @param date(2009-10-13 || 2009年10月13日 || 2009)
     * @return true 是 false 不是
     * @author jie.cui
     */
    public static boolean isLeapyear(String date) {
        if (date.contains("-") || date.contains("年") && date.contains("月")) {
            String year = date.substring(0, 3);
            int yeatInt = Integer.parseInt(year);
            if (yeatInt % 100 == 0 && yeatInt % 400 == 0 || yeatInt % 100 != 0 && yeatInt % 4 == 0) {
                return true;
            }
        }
        return false;
    }

    // 是否到刷新时间
    public static boolean isRefersh(long beforeTime) {
        return isRefersh(DATETIME_FIELD_REFERSH * 1000 * 60, beforeTime);
    }

    // 是否到刷新时间
    public static boolean isRefersh(long gap, long beforeTime) {
        return new Date().getTime() - beforeTime >= gap;
    }

    public static String printCalendarByPattern(Calendar c, String patternStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(patternStr, Locale.US);
        sdf.setLenient(false);
        return sdf.format(c.getTime());
    }
}

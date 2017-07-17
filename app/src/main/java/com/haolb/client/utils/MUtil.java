package com.haolb.client.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts.Photo;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Toast;

import com.easemob.domain.User;
import com.haolb.client.app.AppConstants;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 一些小工具
 *
 * @author chenzeyue@meifuzhi.com
 * @version $Id: MUtil.java 5671 2014-05-22 09:08:17Z jiawenze $
 * @since 2012-11-24
 */
public class MUtil {
    /**
     * 获取库Phon表字段*
     */

    private static final String[] PHONES_PROJECTION = new String[]{

            Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID};
    /**
     * 联系人显示名称*
     */

    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

    /**
     * 电话号码*
     */

    private static final int PHONES_NUMBER_INDEX = 1;

    /**
     * /** 过滤手机号码，将真实手机号码提取出来
     *
     * @param oldPhoneNumber
     * @return 过滤后的手机号码，并不对手机号码进行验证
     * @author TonyZhao
     * @since 2013-2-28, v2.0.0
     */
    public final static String parsePhoneNumber(String oldPhoneNumber) {

        return oldPhoneNumber.replaceAll("[\\- \\.,:;\\*\\?a-zA-Z]", "")
                .replaceAll("^(\\+86|0086)", "").replaceAll("\\+", "")
                .replaceAll("^0", "");
    }

    public static boolean isMobileNum(String mobiles) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * @param @param  oldPhoneNumber
     * @param @return
     * @return String
     * @author ccx@mobileart.cc
     * @since 2014-4-9, v4.0.0
     */
    public final static String formatTimeed(long time) {

        long currentTimeMillis = System.currentTimeMillis();
        long s = (currentTimeMillis / 1000 - time);
        if (s < 0) {
            return "刚刚. . .";
        } else if (s >= 0 && s < 60) {
            return s + "秒前";
        } else if (s >= 60 && s < 60 * 60) {
            return s / 60 + "分钟前";
        } else if (s >= 60 * 60 && s < 60 * 60 * 24) {
            return s / 3600 + "小时前";
        } else if (s >= 60 * 60 * 24 && s < 60 * 60 * 24 * 30) {
            return s / 3600 / 24 + "天前";
        } else if (s >= 60 * 60 * 24 * 30 && s < 60 * 60 * 24 * 30 * 365) {
            return s / 3600 / 24 / 30 + "月前";
        } else if (s >= 60 * 60 * 24 * 30 * 365) {
            return s / 3600 / 24 / 30 / 365 + "年前";
        }
        return "刚刚. . .";

    }

    /**
     * 将dp单位转换成px(dp可以是浮点数，px只能是整数)
     *
     * @author chenzeyue
     * @since 2013-06-04
     */
    public final static float parseDpToPx(Resources r, float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                r.getDisplayMetrics());
        return px;
    }

    /**
     * 将dp单位转换成px
     *
     * @return float
     * @author chenzeyue@meifuzhi.com
     * @since 2012-11-24
     */
    public final static float parseDpToPx(Resources r, int dp) {

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                r.getDisplayMetrics());
        return px;
    }

    /**
     * 将dp单位转换成px
     *
     * @return int
     * @author chenzeyue@meifuzhi.com
     * @since 2012-11-24
     */
    public final static int parseDpToPxInt(Resources r, int dp) {

        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, r.getDisplayMetrics());
        return px;
    }

    /**
     * 计算字符串的md5
     *
     * @param string
     * @return string
     * @author vincent.qu@meifuzhi.com
     * @since 2012-12-04
     */
    public final static String md5(String string) {

        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public final static void toast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    public final static void toastLong(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }

    /**
     * 直接输入资源id的版本
     *
     * @param context
     * @param resId
     * @author TonyZhao
     * @since 2013-2-4
     */
    public final static void toast(Context context, int resId) {

        toast(context, context.getString(resId));
    }

    /**
     * 转换音频时长格式
     *
     * @param targetTime
     * @return
     * @author lifayu@mobileart.cc
     * @since 2014-2-17
     */
    public static String getAudioTimeString(long targetTime) {

        if (targetTime < 60) {
            // return String.format("00:%02d", targetTime);
            return String.format("%d秒", targetTime);
        } else if (targetTime > 60) {
            int m = (int) (targetTime / 60);
            int s = (int) (targetTime % 60);

            if (m > 60) {
                int h = (int) (m / 60);
                m = (int) (m % 60);
                return String.format("%02d:%02d:%02d", h, m, s);
            } else {
                return String.format("%02d:%02d", m, s);
            }
        }
        return "00:00";
    }

    /**
     * 获取格式友好的时间
     *
     * @param targetTime 待转换的目标时间
     * @author chenzeyue
     */
    public final static String getFriendlyTime(long targetTime) {

        String ftime = "";

        Date date = new Date(targetTime);
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        int year = ca.get(Calendar.YEAR);
        int month = ca.get(Calendar.MONTH) + 1;
        int day = ca.get(Calendar.DAY_OF_MONTH);
        int hour = ca.get(Calendar.HOUR_OF_DAY);
        int minute = ca.get(Calendar.MINUTE);

        Calendar caNow = Calendar.getInstance();
        int nowYear = caNow.get(Calendar.YEAR);
        int nowMonth = caNow.get(Calendar.MONTH) + 1;
        int nowDay = caNow.get(Calendar.DAY_OF_MONTH);
        if (year == nowYear && month == nowMonth && day == nowDay) {
            // ftime = hour + ":" + String.format("%02d", minute);
            //
            // Date now = new Date();
            // long offset = now.getTime() - targetTime;
            // if (offset < 60*60*1000 && offset > 0) {
            // int mm = (int) (offset/(60*1000));
            // return String.format("%d分钟前", mm);
            // }

            if (hour >= 0 && hour < 6) {
                ftime = String.format("凌晨 %02d:%02d", hour, minute);
            } else if (hour >= 6 && hour < 9) {
                ftime = String.format("早上 %02d:%02d", hour, minute);
            } else if (hour >= 9 && hour < 12) {
                ftime = String.format("上午 %02d:%02d", hour, minute);
            } else if (hour >= 12 && hour < 14) {
                ftime = String.format("中午 %02d:%02d", hour, minute);
            } else if (hour >= 14 && hour < 18) {
                ftime = String.format("下午 %02d:%02d", hour, minute);
            } else if (hour >= 18 && hour < 24) {
                ftime = String.format("晚上 %02d:%02d", hour, minute);
            }
        } else if (year == nowYear && month == nowMonth && day == nowDay - 1) {
            // ftime = "昨天 " + hour + ":" + String.format("%02d", minute);
            ftime = String.format("昨天 %02d:%02d", hour, minute);
        } else if (year == nowYear) {
            // ftime = month + "月" + day + "日 " + hour + ":" +
            // String.format("%02d", minute);
            ftime = String.format("%d月%d日 %02d:%02d", month, day, hour, minute);
        } else {
            // ftime = year + "年" + month + "月" + day + "日 " + hour + ":" +
            // String.format("%02d", minute);
            ftime = String.format("%d年%d月%d日 %02d:%02d", year, month, day,
                    hour, minute);
        }
        return ftime;
    }

    /**
     * 数字友好处理
     *
     * @param number
     * @return
     * @author lifayu@mobileart.cc
     * @since 2014-2-26
     */
    public static String getFriendlyNumber(int number) {
        String ret = "";
        if (number > 10000) {
            float f = number / 10000.0f;
            if (f < 100) {
                ret = new DecimalFormat("#.##万").format(f);
            } else if (f < 1000) {
                ret = new DecimalFormat("#.#万").format(f);
            } else {
                ret = new DecimalFormat("#万").format(f);
            }
        } else {
            ret = String.valueOf(number);
        }
        return ret;
    }

    /**
     * 获取屏幕大小
     *
     * @author Zeyue Chen
     */
    @SuppressLint("NewApi")
    public static Point getScreenSize(Activity a) {
        Point size = new Point();
        Display d = a.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            d.getSize(size);
        } else {
            size.x = d.getWidth();
            size.y = d.getHeight();
        }
        return size;
    }

    /**
     * 获取给定的view相对于屏幕的坐标
     *
     * @param view
     * @return
     * @author lifayu@mobileart.cc
     * @since 2014-1-18, v2.0.0
     */
    public static Point getLocationOfView(View view) {
        Point p = new Point();
        View root = view.getRootView();

        while (view instanceof View) {

            p.x -= view.getScrollX();
            p.y -= view.getScrollY();
            p.x += view.getLeft();
            p.y += view.getTop();
            QLog.v("loc", "[view]" + view.getClass().getName());
            QLog.v("loc", "[view][x]" + p.x);
            QLog.v("loc", "[view][y]" + p.y);
            if (view == root) {
                break;
            }

            view = (View) view.getParent();
        }
        return p;
    }

    /**
     * 获取用户头像（中等）
     *
     * @param avatar 服务器给定的头像地址
     * @return
     * @author lifayu@mobileart.cc
     * @since 2014-1-18, v4.0.0
     */
    public static String getMiddleAvatar(String avatar) {
        if (avatar != null && avatar.startsWith("http")) {
            return avatar + "_m.jpg";
        }
        if (avatar == null || avatar.isEmpty()) {
            avatar = "general_icon_avatar_default.png";
        }
        return avatar;
    }

    /**
     * 获取用户头像（小号）
     *
     * @param avatar 服务器给定的头像地址
     * @return
     * @author lifayu@mobileart.cc
     * @since 2014-1-18, v4.0.0
     */
    public static String getSmallAvatar(String avatar) {
        if (avatar != null && avatar.startsWith("http")) {
            return avatar + "_s.jpg";
        }
        if (avatar == null || avatar.isEmpty()) {
            avatar = "general_icon_avatar_default.png";
        }
        return avatar;
    }

    /**
     * 触发触觉反馈
     *
     * @param view
     * @author lifayu@mobileart.cc
     * @since 2014-2-28
     */
    public static void performHapticFeedback(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                        | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    /**
     * 获取文件或文件夹大小
     *
     * @param file
     * @return
     * @author lifayu@mobileart.cc
     * @since 2014-5-8
     */
    public static long getFileSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                size += getFileSize(f);
            }
        } else {
            size += file.length();
            if (AppConstants.DEBUG) {
                QLog.v("fileSize",
                        String.format("file:%s\n size:%s",
                                file.getAbsoluteFile(),
                                fileSizeFormat(file.length())));
            }
        }
        return size;
    }

    /**
     * 循环遍历删除文件或文件夹
     *
     * @param file
     * @return
     * @author lifayu@mobileart.cc
     * @since 2014-5-8
     */
    public static int deleteFile(File file) {
        int count = 0;
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                f.delete();
                count += 1;
            }
        } else {
            file.delete();
            count += 1;
        }
        return count;
    }

    /**
     * 文件大小友好格式化
     *
     * @param size
     * @return
     * @author lifayu@mobileart.cc
     * @since 2014-5-8
     */
    public static String fileSizeFormat(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (size < 1024) {
            fileSizeString = size + "B";
        } else if (size < 1048576) {
            fileSizeString = df.format((double) size / 1024) + "K";
        } else if (size < 1073741824) {
            fileSizeString = df.format((double) size / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) size / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * 检查能否访问互联网
     *
     * @author chenzeyue
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * 判断服务是否是运行状态
     *
     * @param className
     * @return
     */
    public static boolean isStartService(Context context, String className) {
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> mServiceList = mActivityManager
                .getRunningServices(30);
        for (int i = 0; i < mServiceList.size(); i++) {
            if (className.equals(mServiceList.get(i).service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static List<User> getPhoneContacts(Context mContext) {

            List<User> contactList = new ArrayList<User>();
            /**得到手机通讯录联系人信息**/

        ContentResolver resolver = mContext.getContentResolver();
        try{
            Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);
            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    String phoneNumber;
                    phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                    phoneNumber = parsePhoneNumber(phoneNumber);
                    if (TextUtils.isEmpty(phoneNumber) || !isMobileNum(phoneNumber) || phoneNumber.equals(UCUtils.getInstance().getAccount()))
                        continue;
                    String contactName;
                    contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                    User UserInfo = new User();
                    UserInfo.setContactName(contactName);
                    UserInfo.setUsername(phoneNumber);
                    UserInfo.setStatus(0);
                    contactList.add(UserInfo);
                }
                phoneCursor.close();
            }
            }catch (Exception e){

            }
            /**得到手机SIM卡联系人人信息**/
        try{
            Uri uri = Uri.parse("content://icc/adn");
            Cursor phonesimCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
                    null);
            if (phonesimCursor != null) {
                while (phonesimCursor.moveToNext()) {
                    String phoneNumber;
                    phoneNumber = phonesimCursor.getString(PHONES_NUMBER_INDEX);
                    phoneNumber = parsePhoneNumber(phoneNumber);
                    if (TextUtils.isEmpty(phoneNumber) || !isMobileNum(phoneNumber))
                        continue;
                    String contactName = phonesimCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                    User UserInfo = new User();
                    UserInfo.setContactName(contactName);
                    UserInfo.setUsername(phoneNumber);
                    UserInfo.setStatus(0);
                    contactList.add(UserInfo);
                }
                phonesimCursor.close();

            }
        }catch (Exception e){

        }
        return contactList;
    }

}

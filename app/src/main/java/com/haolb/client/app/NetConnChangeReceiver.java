package com.haolb.client.app;

import java.util.HashSet;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.haolb.client.utils.DataUtils;


/**
 * 
 * @author zexu
 *
 */
public class NetConnChangeReceiver extends BroadcastReceiver {

    public static boolean netGetted;
    public static boolean wifi;

    static HashSet<String> dealActivities = new HashSet<String>(20);

    static {
    	//在这里添加哪些哪些activity需要再网络发生变化时弹出吐司通知
//    	dealActivities.add(MainActivity.class.getName());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        dealNetworkInfo(context);
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(1);
        boolean isAppRunning = false;
        String MY_PKG_NAME = MainApplication.getInstance().getPackageName();
        for (RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(MY_PKG_NAME)
                    || info.baseActivity.getPackageName().equals(MY_PKG_NAME)) {
                if (dealActivities.contains(info.topActivity.getClassName())) {
                    isAppRunning = true;
                }
                break;
            }
        }
        if (isAppRunning) {
            if (wifi) {
                Toast.makeText(context, "当前网络己切换至wifi模式", Toast.LENGTH_SHORT).show();
            } else if (DataUtils.getPreferences("autoSwapImage", false)) {
            	Toast.makeText(context, "您不在wifi环境中，己智能切换到图片隐藏模式以节省流量", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void dealNetworkInfo(Context context) {
        // 获得网络连接服务
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info != null) {
            wifi = info.getType() == ConnectivityManager.TYPE_WIFI;
        } else {
            wifi = false;
        }
        netGetted = true;
    }
}

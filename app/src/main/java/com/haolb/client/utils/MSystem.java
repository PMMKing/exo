package com.haolb.client.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 系统类 用户获取系统级别的信息。和手机、操作系统内核、架构相关的信息。
 * 
 * @author vincent.qu@meifuzhi.com
 * @since 2012-12-04, v1.0.0
 * @version $Id: MSystem.java 5550 2014-05-09 02:33:21Z lifayu $
 */
public class MSystem {
	private final static String TAG = "MSystem";

	/**
	 * 获取系统mac地址
	 * 
	 * @return string
	 * @author vincent.qu@meifuzhi.com
	 * @since 2012-12-04, v1.0.0
	 */
	public final static String getMacAddress() {

		String macAddress = null;
		String str = "";
		try {
			Process pp = Runtime.getRuntime().exec(
					"cat /sys/class/net/wlan0/address ");
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);

			for (; null != str;) {
				str = input.readLine();
				if (str != null) {
					macAddress = str.trim();// 去空格
					break;
				}
			}
		} catch (IOException ex) {
			// 赋予默认值
			ex.printStackTrace();
		}
		QLog.v("letter", macAddress);
		return macAddress;
	}

	/**
	 * 获取手机SIM卡唯一串号IMSI
	 * 
	 * @author TonyZhao
	 * @since 2012-01-09, v1.0.0
	 * @update 2013-07-05 chenzeyue
	 * @param Context
	 * @return IMSI if SIM card is ready, or null
	 * 
	 */
	public final static String getIMSI(Context context) {

		String IMSI = null;
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		// sim卡若未准备好，则返回fake imsi，否则返回具体的sim值
		if (TelephonyManager.SIM_STATE_READY == tm.getSimState()) {
			IMSI = tm.getSubscriberId();
			return IMSI;
		} else {
			// IMSI = "460912345678888";
			// MLog.v("IMSI", "SIM NOT READ, generate a fake IMSI code " +
			// IMSI);
			return null;
		}
	}

	/**
	 * 取得手机IMEI串号
	 * 
	 * @author TonyZhao
	 * @since 2013-01-10, v1.0.0
	 * @param context
	 * @return String
	 */
	public final static String getIMEI(Context context) {

		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public final static boolean isWifiConnected(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		State state = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		if (state == State.CONNECTED) {
			return true;
		} else {
			return false;
		}
	}

    /**
     * 获取文件存储地址（有sd卡的时候返回sd卡跟目录，否则返回app的data目录）
     *
     * @author chenzeyue@meifuzhi.com
     * @since 2012-11-19
     * @modified by vincent.qu
     */
    public final static String getStoragePath(Context context) {

        String path = null;
        // 获取外部sd卡的状态
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            // 如果已挂载
            path = Environment.getExternalStorageDirectory() + "/haolb/";
        } else {
            // 否则
            path = context.getFilesDir() + "/images/";
        }
        File rootFile = new File(path);
        if (!rootFile.exists()) {
            // 如果根目录不存在，创建
            rootFile.mkdirs();
        }
        return path;
    }

	/**
	 * 获取CPU核心数
	 * 
	 * @author chenzeyue
	 */
	public final static int getCpuCoreCount() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			Log.d(TAG, "CPU Count: " + files.length);
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			// Print exception
			Log.d(TAG, "CPU Count: Failed.");
			e.printStackTrace();
			// Default to return 1 core
			return 1;
		}
	}

	/**
	 * 获取CPU最大主频
	 * 
	 * @return double GHZu
	 * @author chenzeyue
	 */
	public final static double getCpuMaxFrequence() {
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/bin/cat",
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = reader.readLine();
			Log.d(TAG, "CPU Max: " + line);
			return ((Integer.valueOf(line)) / 1000000.0);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获取系统版本号
	 * 
	 * @author lifayu@mobileart.cc
	 * @since 2014-1-16, v4.0.2
	 * @param context
	 * @return 系统版本号，如果获取失败，则返回空字符串
	 */
	public static String getVersionName(Context context) {
		try {
			PackageInfo pkg = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pkg.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 读取文件,返回base64字符串
	 * 
	 * @author lifayu@mobileart.cc
	 * @since 2014-2-17
	 * @param filepath
	 * @return
	 */
	public static String getBase64StringFromFile(String filepath) {
		FileInputStream fin;
		String ret = null;
		try {
			fin = new FileInputStream(filepath);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			ret = Base64.encodeToString(buffer, Base64.DEFAULT);
			fin.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
		// File file = new File(filepath);
		// if (!file.exists() || file.isDirectory()) {
		// return "";
		// }
		// ByteArrayOutputStream out = new ByteArrayOutputStream();
		// InputStream is = null;
		// try {
		// is = new FileInputStream(file);
		// byte[] bytes = new byte[1024];
		// int n;
		// while((n=is.read(bytes, 0, 1024)) != -1){
		// out.write(bytes, 0, n);
		// }
		// out.flush();
		// String ret = Base64.encodeToString(out.toByteArray(),
		// Base64.DEFAULT);
		// MLog.v(TAG, "audio string: " + ret);
		// is.close();
		// out.close();
		// return ret;
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// return "";
	}

	/**
	 * 删除指定的文件
	 * 
	 * @author lifayu@mobileart.cc
	 * @since 2014-1-18, v4.0.0
	 * @param path
	 *            文件绝对路径
	 */
	public static void deleteFileByPath(String path) {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * 判断指定文件是否存在
	 * 
	 * @author lifayu@mobileart.cc
	 * @since 2014-1-18, v4.0.0
	 * @param path
	 *            文件绝对路径
	 * @return true if this file exists, false otherwise.
	 */
	public static boolean isFileExists(String path) {
		File file = new File(path);
		return file.exists();

	}

	/**
	 * 根据时间获取一个随机文件名
	 * 
	 * @author lifayu@mobileart.cc
	 * @since 2014-1-18, v4.0.0
	 * @param prefix
	 *            前缀
	 * @param ext
	 *            扩展名
	 * @return 生成的文件名
	 */
	public static String getRandomFileName(String prefix, String ext) {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return prefix + dateFormat.format(date) + "." + ext;
	}

	/**
	 * 检测指定的程序是否安装
	 * 
	 * @author lifayu@mobileart.cc
	 * @since 2014-1-18, v4.0.0
	 * @param context
	 *            上下文
	 * @param pkgName
	 *            对应程序的包名
	 * @return true if the app is installed, false otherwise.
	 */
	public static boolean isPkgInstalled(Context context, String pkgName) {
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager()
					.getPackageInfo(pkgName, 0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		if (packageInfo == null) {
			return false;
		} else {
			return true;
		}
	}

}

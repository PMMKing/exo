package com.haolb.client.utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.haolb.client.app.MainApplication;


public class DataUtils {

	private static final String PREFERENCE_NAME = "BCarPreferences";
	private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 400;

	private static final SharedPreferences sharedPreferences = MainApplication.getInstance()
			.getSharedPreferences(PREFERENCE_NAME, 0);
	/** 小图片缓存 */
	public static final LruCache<String, Bitmap> resource = new LruCache<String, Bitmap>(
			DEFAULT_MEM_CACHE_SIZE);

	private DataUtils() {
	}

	public static void removePreferences(String key) {
		if (key == null) {
			return;
		}
		Editor editor = DataUtils.sharedPreferences.edit();
		editor.remove(key);
		editor.commit();
	}

	public static void putPreferences(String key, String value) {
		if (key == null || value == null) {
			return;
		}
		Editor editor = DataUtils.sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void putPreferences(String key, boolean value) {
		Editor editor = DataUtils.sharedPreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static void putPreferences(String key, int value) {
		Editor editor = DataUtils.sharedPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static void putPreferences(String key, long value) {
		Editor editor = DataUtils.sharedPreferences.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	public static void putPreferences(String key, float value) {
		Editor editor = DataUtils.sharedPreferences.edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	public static boolean getPreferences(String key, boolean defaultValue) {
		return DataUtils.sharedPreferences.getBoolean(key, defaultValue);
	}

	public static int getPreferences(String key, int defValue) {
		return DataUtils.sharedPreferences.getInt(key, defValue);
	}

	public static String getPreferences(String key, String defValue) {
		return DataUtils.sharedPreferences.getString(key, defValue);
	}

	public static long getPreferences(String key, long defValue) {
		return DataUtils.sharedPreferences.getLong(key, defValue);
	}

	public static float getPreferences(String key, float defValue) {
		return DataUtils.sharedPreferences.getFloat(key, defValue);
	}
	 public static Bitmap getResource(String key) {
	        if (key == null) {
	            return null;
	        }
	        return DataUtils.resource.get(key);
	    }

	    public static void putResource(String key, Bitmap bm) {
	        DataUtils.resource.put(key, bm);
	    }
}

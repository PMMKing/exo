package com.haolb.client.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;


import com.haolb.client.utils.Globals;

import java.io.File;

/**
 * 下载的数据:
 */
public class DownloadData {
    // 下载地址:
    public String url;
    // 已下载的字节数:
    public int downloaded;
    // 文件的总字节数:
    public int filesize;
    // 保存的文件:
    public File savefile;
    // 是否正在下载
    public boolean isRunning;
    // 描述
    public String desc;

    public String filepath;

    public String saveLocation;

    public String nversion;

    // 保存的断点信息:()
    public SharedPreferences preferences;

    public static final String KEY_FOR_DOWNLOADED = "key_for_downloaded";
    public static final String KEY_FOR_FILESIZE = "key_for_filesize";
    public static final String KEY_FOR_FILEPATH = "key_for_filepath";
    public static final String KEY_FOR_SAVE_LOCATION = "key_for_save_location";
    public static final String TAG_INTERNAL_STORAGE = "internal storage";
    public static final String TAG_SDCARD_STORAGE = "sdcard_storage";

    public final static String BASE_DIR = "BCar";

    /**
     * 
     * @param url ---下载的url地址
     * @param nversion -- 新版的版本号
     */
    public DownloadData(String url, String nversion, Context context) {
        this.nversion = nversion;
        this.url = url;
        String filename = "";
        if (url != null && url.length() > 0) {
            if (url.indexOf("/") > -1) {
                filename = url.substring(url.lastIndexOf('/') + 1);
                filename = filename.substring(0, filename.lastIndexOf("."));
            }
        }
        filename += nversion;
        preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        downloaded = preferences.getInt(KEY_FOR_DOWNLOADED, 0);
        filesize = preferences.getInt(KEY_FOR_FILESIZE, 0);
        filepath = preferences.getString(KEY_FOR_FILEPATH, "");
        if (Globals.isSDCardAvailable()) {
            savefile = new File(Environment.getExternalStorageDirectory() + File.separator + BASE_DIR, filename
                    + ".apk");
            saveLocation = TAG_SDCARD_STORAGE;
        } else {
            savefile = new File(context.getFilesDir(), filename + ".apk");
            saveLocation = TAG_INTERNAL_STORAGE;
        }
        if (!savefile.getParentFile().exists()) {
            savefile.getParentFile().mkdirs();
        }
        if (filepath.equals("")) {// 文件尚未创建:
            filepath = savefile.getAbsolutePath();
            downloaded = 0;
            filesize = 0;
        } else {
            if (!filepath.equals(savefile.getAbsolutePath())) {
                filepath = savefile.getAbsolutePath();
                downloaded = 0;
                filesize = 0;
            } else {
                if (savefile.length() < downloaded) {
                    downloaded = 0;
                    filesize = 0;
                    savefile.delete();
                }
            }
        }
        saveDatas();
    }

    public void saveDatas() {
        Editor editor = preferences.edit();
        editor.putInt(KEY_FOR_DOWNLOADED, downloaded);
        editor.putInt(KEY_FOR_FILESIZE, filesize);
        editor.putString(KEY_FOR_FILEPATH, filepath);
        editor.putString(KEY_FOR_SAVE_LOCATION, saveLocation);
        editor.commit();
    }

    public void setFileSize(int filesize) {
        this.filesize = filesize;
        saveDatas();
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
        saveDatas();
    }

}

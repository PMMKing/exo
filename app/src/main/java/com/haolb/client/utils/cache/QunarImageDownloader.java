package com.haolb.client.utils.cache;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.haolb.client.utils.QLog;
import com.squareup.picasso.Downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shutao.xiang on 2014/12/30.
 */
public class QunarImageDownloader implements Downloader {

	private static final int HTTP_CACHE_SIZE = 50 * 1024 * 1024; // 10MB
	private static final String PICASSO_CACHE = "qunar-picasso-cache";
	private static final int IO_BUFFER_SIZE = 8 * 1024;
	private static final int DISK_CACHE_INDEX = 0;

	private DiskLruCache mHttpDiskCache;
	protected boolean mPauseWork = false;
	private final Object mPauseWorkLock = new Object();
	private List<String> runningTaskList = Collections.synchronizedList(new LinkedList<String>());

	public static QunarImageDownloader createQunarImageDownloader(Context context) {
		try {
			File cache = new File(context.getCacheDir(), PICASSO_CACHE);
			if (!cache.exists()) {
				cache.mkdirs();
			}
			QunarImageDownloader qunarImageDownloader = new QunarImageDownloader();
			qunarImageDownloader.mHttpDiskCache = DiskLruCache.open(cache, 1, 1, HTTP_CACHE_SIZE);

			return qunarImageDownloader;
		} catch (Exception e) {
			return null;
		}
	}

	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

	public void onCancel(){
		synchronized (mPauseWorkLock) {
			mPauseWorkLock.notifyAll();
		}
	}


	public boolean checkTaskIsRunning(String url) {
		return runningTaskList.contains(url);
	}

	public boolean checkHasDiskCache(String url) {
		String key = hashKeyForDisk(url);
		try {
			DiskLruCache.Snapshot snapshot = mHttpDiskCache.get(key);
			return snapshot != null;
		} catch (Exception e) {
			return  false;
		}
	}

	/**
	 * A hashing method that changes a string (like a URL) into a hash suitable for using as a disk filename.
	 */
	public  String hashKeyForDisk(String key) {
		if(TextUtils.isEmpty(key)){
			return "" ;
		}
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}
	private  String bytesToHexString(byte[] bytes) {
		// http://stackoverflow.com/questions/332079
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public Response load(Uri uri, boolean localCacheOnly) throws IOException {

		// Wait here if work is paused and the task is not cancelled
		synchronized (mPauseWorkLock) {
			while (mPauseWork) {
				try {
					mPauseWorkLock.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		String url = uri.toString();
		String key = hashKeyForDisk(url);
		DiskLruCache.Snapshot snapshot = mHttpDiskCache.get(key);
		InputStream is = null;
		boolean fromCache = true;
		if (snapshot == null) {
			//磁盘没有
			DiskLruCache.Editor editor = mHttpDiskCache.edit(key);
			if (editor != null) {
				runningTaskList.add(url);
				try{
					if (downloadUrlToStream(url, editor.newOutputStream(DISK_CACHE_INDEX))) {
						editor.commit();
						fromCache = false;
						snapshot = mHttpDiskCache.get(key);
					} else {
						editor.abort();
					}
				} finally {
					runningTaskList.remove(url);
				}
			}
		}
		if (snapshot != null) {
			is = snapshot.getInputStream(DISK_CACHE_INDEX);
		} else {
			throw new ResponseException("download image error url is:" + uri);
		}

		return new Response(is, fromCache);
	}

    @Override
    public void shutdown() {

    }

    /**
	 * Download a bitmap from a URL and write the content to an output stream.
	 * @param urlString The URL to fetch
	 * @return true if successful, false otherwise
	 */
	public  boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		HttpURLConnection urlConnection = null;

		try {
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) new URL(urlString).openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
			out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			return true;
		} catch (final Exception e) {
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
			}
		}
		return false;
	}
}
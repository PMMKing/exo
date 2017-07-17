package com.haolb.client.net;

import android.net.Proxy;
import android.os.AsyncTask;

import com.haolb.client.app.MainApplication;
import com.haolb.client.app.NetConnChangeReceiver;
import com.haolb.client.utils.EqualUtils;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<Void, Void, Void> {
    private static final String SCHEMA_HTTP = "http://";
    private boolean isStop = false;
    private final File saveFile;
    private int start;
    private int filesize;
    private String downloadUrl;
    private final DownloadProgressChangeListener listener;
    private final DownloadData downloadData;
    private String proxyUrl = null;
    private String host = "";

    public DownloadTask(DownloadData downloadData, DownloadProgressChangeListener listener) {
        this.downloadData = downloadData;
        this.listener = listener;
        start = downloadData.downloaded;
        filesize = downloadData.filesize;
        saveFile = downloadData.savefile;
        downloadUrl = downloadData.url;
    }

    @SuppressWarnings("deprecation")
    public void setupProxy() {
        if (!NetConnChangeReceiver.netGetted) {
            NetConnChangeReceiver.dealNetworkInfo(MainApplication.getInstance());
        }
        String proxyHost;
        int proxyPort;
        if (NetConnChangeReceiver.wifi) {
            // WIFI: global http proxy
            proxyHost = NetworkManager.getProxyHost(false);
            proxyPort = NetworkManager.getProxyPort(false);
            if (EqualUtils.equalsIgnoreCase(proxyHost, Proxy.getDefaultHost())) {
                proxyHost = null;
            }
        } else {
            // GPRS: APN http proxy
            proxyHost = NetworkManager.getProxyHost(true);
            proxyPort = NetworkManager.getProxyPort(true);
        }
        if (proxyHost != null) {
            this.proxyUrl = proxyHost + ":" + proxyPort;
        }
    }

    private int getContentLength(String url) {
        HttpURLConnection httpURLConnection = null;
        int contentLength = 0;
        int times = 0;
        // try 4 times:
        if (proxyUrl != null) {
            this.proxyUrl = SCHEMA_HTTP + this.proxyUrl;
            String stemp = "";
            if (downloadUrl.startsWith(SCHEMA_HTTP) && downloadUrl.length() > SCHEMA_HTTP.length()) {
                stemp = downloadUrl.substring(SCHEMA_HTTP.length(), downloadUrl.length());
            } else {
                stemp = downloadUrl;
            }
            host = stemp.substring(0, stemp.indexOf("/"));
            downloadUrl = this.proxyUrl
                    + stemp.substring(stemp.indexOf("/") <= -1 ? stemp.length() : stemp.indexOf("/"), stemp.length());
        }
        while (times < 4) {
            try {
                URL serverUrl = new URL(downloadUrl);
                httpURLConnection = (HttpURLConnection) serverUrl.openConnection();
                if (httpURLConnection != null) {
                    httpURLConnection.setConnectTimeout(30000);
                    httpURLConnection.setReadTimeout(30000);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setUseCaches(false);
                    if (proxyUrl != null && host != null && host.length() > 0) {
                        httpURLConnection.setRequestProperty("X-Online-Host", host);
                    }
                    httpURLConnection.connect();
                    contentLength = httpURLConnection.getContentLength();
                    if (contentLength > 0) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                times++;
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return contentLength;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        setupProxy();
        filesize = getContentLength(downloadUrl);
        if (filesize != downloadData.filesize) {
            start = 0;
//            downloadData.setFileSize(filesize);
        }
        downloadData.setFileSize(filesize);
        if (filesize <= 0) {
            return null;
        }
        HttpURLConnection httpURLConnection = null;
        try {
            URL serverUrl = new URL(downloadUrl);
            httpURLConnection = (HttpURLConnection) serverUrl.openConnection();
            if (httpURLConnection != null) {
                httpURLConnection.setConnectTimeout(30000);
                httpURLConnection.setReadTimeout(30000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestProperty("connection", "keep-alive");
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("Range", "bytes=" + start + "-");
                if (proxyUrl != null && host != null && host.length() > 0) {
                    httpURLConnection.setRequestProperty("X-Online-Host", host);
                }
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                if (start < filesize) {
                    RandomAccessFile os = new RandomAccessFile(saveFile, "rw");
                    os.seek(start);
                    int len = 0;
                    byte[] data = new byte[4 * 1024];
                    int i = 0;
                    while ((len = inputStream.read(data)) != -1 && !isStop) {
                        os.write(data, 0, len);
                        start += len;
                        downloadData.setDownloaded(start);
                        if (listener != null && i % 20 == 0) {
                            listener.onProgressChange(downloadData);
                        }
                        i++;
                    }
                    os.close();
                    inputStream.close();
                }
                // 安装
                if (listener != null && !isStop) {
                    listener.onProgressChange(downloadData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            downloadData.setDownloaded(start);
            downloadData.setFileSize(filesize);
            if (listener != null && !isStop) {// 下载失败
                listener.onDownloadFailure();
            }
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return null;
    }

    // 暂停
    public void setStop(boolean isStop) {
        this.isStop = isStop;
    }

    public boolean isStop() {
        return isStop;
    }

    public DownloadData getDownloadData() {
        return downloadData;
    }

    public interface DownloadProgressChangeListener {
        void onProgressChange(DownloadData downloadData);

        void onDownloadFailure();
    }

}

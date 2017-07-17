package com.haolb.client.net;

import android.content.Context;
import android.database.Cursor;
import android.net.Proxy;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.haolb.client.app.AppConstants;
import com.haolb.client.app.MainApplication;
import com.haolb.client.app.NetConnChangeReceiver;
import com.haolb.client.domain.param.CommonParam;
import com.haolb.client.domain.response.BaseResult;
import com.haolb.client.utils.EqualUtils;
import com.haolb.client.utils.QLog;
import com.haolb.client.utils.SecureUtil;
import com.haolb.client.utils.UCUtils;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.LinkedList;

public class NetworkManager implements TaskListener {

    private static final String imei = NetworkManager.getIMEI();

    private static class QTasks extends AsyncTask<Void, Integer, BaseResult> {

        private NetworkTask networkTask = null;
        private TaskListener listener = null;
        String proxyHost;
        int proxyPort;
        private String hostUrl;

        public QTasks(TaskListener aListener, NetworkTask netTask) {
            listener = aListener;
            networkTask = netTask;
        }

        public boolean cancelWithHandler(Handler handler) {
            if (networkTask.handler == handler && networkTask.param.cancelAble) {
                networkTask.cancel = true;
                cancel(true);
                return true;
            }
            return false;
        }

        public boolean cancelWithKey(ServiceMap key) {
            if (networkTask.param.key == key && networkTask.param.cancelAble) {
                networkTask.cancel = true;
                cancel(true);
                return true;
            }
            return false;
        }

        public boolean cancelWithType(int type) {
            // type为all时可以结束cancelable的类型
            if (type == ServiceMap.NET_TASKTYPE_ALL) {
                networkTask.cancel = true;
                cancel(true);
                return true;
            } else if (networkTask.param.key.getCode() == type && networkTask.param.cancelAble) {
                networkTask.cancel = true;
                cancel(true);
                return true;
            }
            return false;
        }

        public boolean cancelWithUrl(String url) {
            if (networkTask.param.url.equals(url) && networkTask.param.cancelAble) {
                networkTask.cancel = true;
                cancel(true);
                return true;
            }
            return false;
        }

        public boolean cancelWithParam(NetworkParam param) {
            if (networkTask.param.equals(param) && networkTask.param.cancelAble) {
                networkTask.cancel = true;
                cancel(true);
                return true;
            }
            return false;
        }

        public NetworkParam getNetworkParam() {
            return networkTask == null ? null : networkTask.param;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!networkTask.cancel) {
                // set host
                switch (networkTask.param.key.getCode()) {
                default:
                    // 未指定host使用默认
                	hostUrl = TextUtils.isEmpty(networkTask.param.hostPath) ? AppConstants.COMMON_URL + networkTask.param.key.getDesc()
                            : networkTask.param.hostPath;
                    break;
                }
                // -- end -- set host
                // set proxy
                if (!NetConnChangeReceiver.netGetted) {
                    NetConnChangeReceiver.dealNetworkInfo(MainApplication.getInstance());
                }
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
                // -- end -- set proxy

            } else {
                cancel(false);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (listener != null) {
                listener.onTaskCancel(networkTask);
            }
        }

        public byte[] doRequest() {
            if (networkTask.param.param == null) {
                return null;
            }
            
            networkTask.param.param.cparam = new CommonParam();
            networkTask.param.param.cparam.imei = imei;
            networkTask.param.param.cparam.versionCode = MainApplication.getInstance().versionCode;
            networkTask.param.param.cparam.versionName = MainApplication.getInstance().versionName;
            networkTask.param.param.cparam.city =UCUtils.getInstance().getCity().id;
            String userId = UCUtils.getInstance().getUserid();
            networkTask.param.param.cparam.userId = TextUtils.isEmpty(userId) ? null : Integer.parseInt(userId);
            String token = UCUtils.getInstance().getToken();
            networkTask.param.param.cparam.token = TextUtils.isEmpty(token) ? "" : token;
            String bjson = JSON.toJSONString(networkTask.param.param);
    		String b = SecureUtil.encode(bjson, networkTask.param.ke);
    		networkTask.param.url = "b=" + b + "&key=" + networkTask.param.ke + "&ver=1" ;
            
    		
            if (networkTask.cancel) {
                return null;
            }

            if (AppConstants.DEBUG) {
                synchronized (QLog.class) {
                    QLog.v("request", "API=" + networkTask.param.key.name());
                    QLog.v("request", networkTask.param.url);
                    QLog.v("request", "b=" + JSON.toJSONString(networkTask.param.param, true));
                }
            }
            FileInputStream fis = null;
            HttpClient httpClient = NetworkManager.getHttpClient(proxyHost, proxyPort);
            try {
            	HttpPost request;
            	
            	if(networkTask.param.key.getCode() == ServiceMap.NET_TASKTYPE_CONTROL) {
            		request = new HttpPost(hostUrl);
            		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
            		// request.addHeader("User-Agent",
            		// "Mozilla/5.0 (Linux; U; Android 2.2) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
            		request.setEntity(new StringEntity(networkTask.param.url));
            	} else if(networkTask.param.key.getCode() == ServiceMap.NET_TASKTYPE_FILE) {
            		if(TextUtils.isEmpty(networkTask.param.filePath)) {
            			request = new HttpPost(hostUrl);
                		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
                		// request.addHeader("User-Agent",
                		// "Mozilla/5.0 (Linux; U; Android 2.2) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
                		request.setEntity(new StringEntity(networkTask.param.url));
            		} else {
            			String url = hostUrl;
            			if(url.lastIndexOf("?") != -1) {
            				url += "&";
            			} else {
            				url += "?";
            			}
            			url += networkTask.param.url;
            			request = new HttpPost(url);
            			request.addHeader("Content-Type", "application/octet-stream");
            			File file = new File(networkTask.param.filePath);
            			InputStreamEntity ise = new InputStreamEntity(fis = new FileInputStream(file), file.length());
            			request.setEntity(ise);
            		}
            	} else {
            		//不支持的类型
            		return null;
            	}
                
                
                HttpResponse response = httpClient.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                QLog.v("response", "http status code : %d", statusCode);
                if (statusCode == HttpStatus.SC_OK) {
                    byte[] data = EntityUtils.toByteArray(response.getEntity());
                    if (!networkTask.cancel) {
                        return data;
                    }
                }
            } catch (Exception e) {
                QLog.e("error", e.getLocalizedMessage(), e);
            } finally {
                if (httpClient != null) {
                    httpClient.getConnectionManager().shutdown();
                }
                if(fis != null) {
                	try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
            }
            return null;
        }


        @Override
        protected BaseResult doInBackground(Void... params) {
            if (!networkTask.cancel) {
            	BaseResult baseResult = Response.dealWithResponse(doRequest(), networkTask.param);
//            	if(networkTask.param.key == ServiceMap.GET_FRIENDS && baseResult != null) {
//            		if(baseResult.bstatus.code == 0) {
//            			//更新本地数据库记录
////            			FriendListResult friendListResult = (FriendListResult) baseResult;
//            			String uidStr = UCUtils.getInstance().getUserid();
//            			int myId = Integer.parseInt(uidStr);
////            			friendListResult.data.friendList = FriendManager.sync(friendListResult.data.friendList,myId);
//            		}
//            	}
            	return baseResult;
            }
            cancel(true);
            return null;
        }

        @Override
        protected void onPostExecute(BaseResult result) {
            Message m = null;
            if (!networkTask.cancel) {
                networkTask.param.result = result;
                if (networkTask.handler != null) {
                    if (networkTask.param.result != null) {
                        m = networkTask.handler.obtainMessage(TaskStatus.SUCCESS, networkTask.param);
                    } else {
                        m = networkTask.handler.obtainMessage(TaskStatus.ERROR, networkTask.param);
                    }
                    networkTask.handler.sendMessage(m);
                }
                if (listener != null) {
                    listener.onTaskComplete(networkTask);
                }
            } else {
                if (listener != null) {
                    listener.onTaskCancel(networkTask);
                }
            }
        }

    }

    private static NetworkManager singleInstance = null;

    public static NetworkManager getInstance() {
        synchronized (NetworkManager.class) {
            if (singleInstance == null) {
                singleInstance = new NetworkManager();
            }
        }
        return singleInstance;
    }

    private final LinkedList<NetworkTask> listSequence = new LinkedList<NetworkTask>();
    @SuppressWarnings("unchecked")
    private final LinkedList<QTasks>[] tasks = new LinkedList[ServiceMap.NET_TASKTYPE_ALL];
    /**
     * 不设置上限
     */
    private final int maxCount = Integer.MAX_VALUE;
    public static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    // 电信CTWAP时apn的名称:#777,ctwap
    public static final String CTWAP_APN_NAME_1 = "#777";
    public static final String CTWAP_APN_NAME_2 = "ctwap";

    private NetworkManager() {
        // maxCount = MainConstants.NET_MAXTASK_COUNT;
        tasks[ServiceMap.NET_TASKTYPE_CONTROL] = new LinkedList<QTasks>();
        tasks[ServiceMap.NET_TASKTYPE_FILE] = new LinkedList<QTasks>();
    }

    private boolean addCurrentTask(int taskType, QTasks task) {
        boolean suc = false;
        LinkedList<QTasks> taskList = tasks[taskType];
        if (taskList == null) {
            throw new IllegalArgumentException("no task type = " + taskType);
        }
        synchronized (taskList) {
            if (taskList.size() < maxCount) {
                taskList.add(task);
                suc = true;
            }
        }
        return suc;
    }

    public void addTask(NetworkParam param, Handler handler) {
        boolean isRepeat = false;
        NetworkTask task = new NetworkTask(param, handler);
        synchronized (this.listSequence) {
            Iterator<NetworkTask> listSequenceIterator = this.listSequence.iterator();
            while (listSequenceIterator.hasNext()) {
                NetworkTask networkTask = listSequenceIterator.next();
                NetworkParam tmp = networkTask.param;
                if (tmp.equals(param)) {
                    isRepeat = true;
                    break;
                }
            }

            LinkedList<QTasks> taskList = tasks[param.key.getCode()];
            if (taskList == null) {
                throw new IllegalArgumentException("param.key.getCode() returns not task type");
            }
            synchronized (taskList) {
                for (int i = 0; i < taskList.size(); i++) {
                    NetworkParam tmp = taskList.get(i).getNetworkParam();
                    if (tmp.equals(param)) {
                        isRepeat = true;
                        break;
                    }
                }
            }

            if (isRepeat) {
                return;
            }

            if (task.handler != null) {
                Message m = task.handler.obtainMessage(TaskStatus.START, task.param);
                task.handler.sendMessage(m);
            }

            switch (param.addType) {
            case Request.NET_ADD_ONORDER:
                this.listSequence.add(task);
                break;
            case Request.NET_ADD_INSERT2HEAD:
                this.listSequence.add(0, task);
                break;
            case Request.NET_ADD_CANCELPRE: {
                Iterator<NetworkTask> it = this.listSequence.iterator();
                while (it.hasNext()) {
                    NetworkTask nt = it.next();
                    if (param.key.getCode() == nt.param.key.getCode() && nt.param.cancelAble) {
                        it.remove();
                    }
                }

                synchronized (taskList) {
                    Iterator<QTasks> itt = taskList.iterator();
                    while (itt.hasNext()) {
                        QTasks qtask = itt.next();
                        qtask.cancelWithType(param.key.getCode());
                        itt.remove();
                    }
                }

                this.listSequence.add(0, task);
            }
                break;
            case Request.NET_ADD_CANCELSAMET: {

                Iterator<NetworkTask> it = this.listSequence.iterator();
                while (it.hasNext()) {
                    NetworkTask nt = it.next();
                    if (param.key == nt.param.key && nt.param.cancelAble) {
                        it.remove();
                    }
                }
                synchronized (taskList) {
                    Iterator<QTasks> itt = taskList.iterator();
                    while (itt.hasNext()) {
                        QTasks qtask = itt.next();
                        if (qtask.networkTask.param.key == param.key) {
                            qtask.cancelWithType(param.key.getCode());
                            itt.remove();
                        }
                    }
                }

                this.listSequence.add(task);

            }
                break;
            default:
                break;
            }
        }
        checkTasks();
    }

    @SuppressWarnings("rawtypes")
    public void cancelTaskByHandler(Handler handler) {
        synchronized (this.listSequence) {
            Iterator<NetworkTask> it = this.listSequence.iterator();
            while (it.hasNext()) {
                NetworkTask task = it.next();
                if (task.handler == handler && task.param.cancelAble) {
                    it.remove();
                }
            }
        }
        for (LinkedList<QTasks> taskList : tasks) {
            synchronized (taskList) {
                Iterator it = taskList.iterator();
                while (it.hasNext()) {
                    QTasks task = (QTasks) it.next();
                    if (task.networkTask.handler == handler) {
                        if (task.cancelWithHandler(handler)) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void cancelTaskByKey(ServiceMap key) {
        synchronized (this.listSequence) {
            Iterator<NetworkTask> it = this.listSequence.iterator();
            while (it.hasNext()) {
                NetworkTask task = it.next();
                if (task.param.key == key && task.param.cancelAble) {
                    it.remove();
                }
            }
        }
        for (LinkedList<QTasks> taskList : tasks) {
            synchronized (taskList) {
                Iterator it = taskList.iterator();
                while (it.hasNext()) {
                    QTasks task = (QTasks) it.next();
                    if (task.networkTask.param.key == key) {
                        if (task.cancelWithKey(key)) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void cancelTaskByType(int type) {

        synchronized (this.listSequence) {
            if (type == ServiceMap.NET_TASKTYPE_ALL) {
                this.listSequence.clear();
            } else {
                Iterator<NetworkTask> it = this.listSequence.iterator();
                while (it.hasNext()) {
                    NetworkTask task = it.next();
                    if (task.param.key.getCode() == type) {
                        it.remove();
                    }
                }
            }
        }

        if (type == ServiceMap.NET_TASKTYPE_ALL) {
            for (LinkedList<QTasks> taskList : tasks) {
                synchronized (taskList) {
                    Iterator it = taskList.iterator();
                    while (it.hasNext()) {
                        QTasks task = (QTasks) it.next();
                        if (task.cancelWithType(type)) {
                            it.remove();
                        }
                    }
                }
            }
        } else {
            LinkedList<QTasks> taskList = tasks[type];
            synchronized (taskList) {
                Iterator<QTasks> it = taskList.iterator();
                while (it.hasNext()) {
                    QTasks task = it.next();
                    if (task.cancelWithType(type)) {
                        it.remove();
                    }
                }
            }
        }
    }

    public void cancelTaskByUrl(String url) {
        synchronized (this.listSequence) {
            Iterator<NetworkTask> it = this.listSequence.iterator();
            while (it.hasNext()) {
                NetworkTask task = it.next();
                if (task.param.url.equals(url) && task.param.cancelAble) {
                    it.remove();
                }
            }
        }
        for (LinkedList<QTasks> taskList : tasks) {
            synchronized (taskList) {
                Iterator<QTasks> it = taskList.iterator();
                while (it.hasNext()) {
                    QTasks task = it.next();
                    if (task.networkTask.param.url.equals(url)) {
                        if (task.cancelWithUrl(url)) {
                            it.remove();
                        }
                    }
                }
            }
        }

    }

    public void cancelTaskByParam(NetworkParam param) {
        synchronized (this.listSequence) {
            Iterator<NetworkTask> it = this.listSequence.iterator();
            while (it.hasNext()) {
                NetworkTask task = it.next();
                if (task.param.equals(param) && task.param.cancelAble) {
                    it.remove();
                }
            }
        }
        for (LinkedList<QTasks> taskList : tasks) {
            synchronized (taskList) {
                Iterator<QTasks> it = taskList.iterator();
                while (it.hasNext()) {
                    QTasks task = it.next();
                    if (task.networkTask.param.equals(param)) {
                        if (task.cancelWithParam(param)) {
                            it.remove();
                        }
                    }
                }
            }
        }

    }

    public void checkTasks() {
        if (this.listSequence.size() == 0) {
            return;
        }
        boolean flag = true;
        synchronized (this.listSequence) {
            Iterator<NetworkTask> it = this.listSequence.iterator();
            while (it.hasNext()) {
                NetworkTask nt = it.next();
                QTasks task = new QTasks(this, nt);
                flag = addCurrentTask(nt.param.key.getCode(), task);
                if (flag) {
                    it.remove();
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }

    public void destroy() {
        if (singleInstance != null) {
            singleInstance.cancelTaskByType(ServiceMap.NET_TASKTYPE_ALL);
        }
        singleInstance = null;
    }

    public int getCurrentTaskCount(int type) {
        int count = 0;
        if (type == ServiceMap.NET_TASKTYPE_ALL) {
            for (LinkedList<QTasks> taskList : tasks) {
                synchronized (taskList) {
                    count += taskList.size();
                }
            }
        } else {
            LinkedList<QTasks> taskList = tasks[type];
            synchronized (taskList) {
                count = taskList.size();
            }
        }
        return count;
    }

    private void removeCurrentTask(NetworkTask netTask) {
        if (netTask.handler != null) {
            Message m = netTask.handler.obtainMessage(TaskStatus.END, netTask.param);
            netTask.handler.sendMessage(m);
        }
        LinkedList<QTasks> taskList = tasks[netTask.param.key.getCode()];
        synchronized (taskList) {
            Iterator<QTasks> it = taskList.iterator();
            while (it.hasNext()) {
                QTasks task = it.next();
                if (task.networkTask == netTask) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void onTaskCancel(NetworkTask task) {
        removeCurrentTask(task);
        checkTasks();
    }

    @Override
    public void onTaskComplete(NetworkTask task) {
        removeCurrentTask(task);
        checkTasks();
    }

    public static String getIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) MainApplication.getInstance().getSystemService(
                Context.TELEPHONY_SERVICE);
        String ret = telephonyManager.getDeviceId();
        return ret;
    }

    @SuppressWarnings("deprecation")
    public static int getProxyPort(boolean isDefault) {
        return isDefault ? Proxy.getDefaultPort() : Proxy.getPort(MainApplication.getInstance());
    }

    @SuppressWarnings("deprecation")
    public static String getProxyHost(boolean isDefault) {
        return isDefault ? Proxy.getDefaultHost() : Proxy.getHost(MainApplication.getInstance());
    }

    /**
     * 
     * @param proxyHost 代理host
     * @param proxyPort 代理port
     * 
     *            <pre>
     *   CMWAP 10.0.0.172:80
     *   CTWAP 10.0.0.200
     * </pre>
     * @return httpClient
     */
    public static HttpClient getHttpClient(String proxyHost, int proxyPort) {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new QSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpConnectionParams.setConnectionTimeout(params, 40000);
            HttpConnectionParams.setSoTimeout(params, 40000);

            // 设置代理:
            if (proxyHost != null && proxyHost.trim().length() > 0) {
                HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                ConnRouteParams.setDefaultProxy(params, proxy);
            }

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            e.printStackTrace();
            return new DefaultHttpClient();
        }
    }

    /**
     * 获取APN名称
     * 
     * @return
     */
    public static String getApnName() {
        String apnName = "";
        try {
            Cursor cursor = MainApplication.getInstance().getContentResolver()
                    .query(PREFERRED_APN_URI, new String[] { "_id", "apn", "type" }, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int counts = cursor.getCount();
                if (counts != 0) {
                    if (!cursor.isAfterLast()) {
                        apnName = cursor.getString(cursor.getColumnIndex("apn"));
                    }
                }
                cursor.close();
            } else {
                // 适配中国电信定制机,如海信EG968,上面方式获取的cursor为空，所以换种方式
                cursor = MainApplication.getInstance().getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    apnName = cursor.getString(cursor.getColumnIndex("user"));
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            apnName = "";
        }
        return apnName;
    }

}

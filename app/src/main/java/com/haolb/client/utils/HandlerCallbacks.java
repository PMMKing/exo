package com.haolb.client.utils;

import android.os.Handler.Callback;
import android.os.Message;
import android.view.WindowManager.BadTokenException;

import com.haolb.client.net.NetworkListener;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.net.TaskStatus;


/**
 * 
 * @author zexu.ge
 * @time 2013-4-18 下午4:37:25
 */
public class HandlerCallbacks {

    public final static int MESSAGE_COMPLETE = 1001; // 成功完成消息
    public final static int MESSAGE_ERROR = 1002; // 出现错误消息
    public final static int MESSAGE_ERROR_NO_SERVICE_TYPE = 1003; // 出现错误消息,业务接口类型丢失
    public final static int MESSAGE_SCROLL_READY = 1004; // 滚动完成消息
    public final static int MESSAGE_TIME_READY = 1005; // 延时到达消息
    public final static int MESSAGE_CLOCK = 1006;

    /**
     * 通用的callback,项目中所有callback必须继承这个类
     * 
     * @author zexu.ge
     * @time 2013-4-18 下午4:20:54
     */
    public static abstract class CommonCallback implements Callback {

        private Callback mCallback;

        public CommonCallback() {
            this(null);
        }

        public CommonCallback(Callback callback) {
            super();
            this.mCallback = callback;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (mCallback != null) {
                return mCallback.handleMessage(msg);
            }
            return false;
        }

        public void removeListener() {
            mCallback = null;
        }
    }

    public static class ActivityCallback extends CommonCallback {

        private NetworkListener listener;

        public ActivityCallback(NetworkListener listener) {
            this(listener, null);
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.obj instanceof NetworkParam) {

                NetworkParam param = (NetworkParam) msg.obj;
                switch (msg.what) {
                case TaskStatus.START:
                    try {
                        synchronized (this) {
                            if (listener != null) {
                                listener.onNetStart(param);
                            }
                        }
                    } catch (BadTokenException e) {
                        e.printStackTrace();
                    }
                    break;
                case TaskStatus.RUNNING:
                    break;
                case TaskStatus.ERROR:
                    synchronized (this) {
                        if (listener != null) {
                            listener.onNetError(param, MESSAGE_ERROR);
                        }
                    }
                    break;
                case TaskStatus.SUCCESS:
                    ServiceMap type = param.key;
                    if (type == null) {
                        synchronized (this) {
                            if (listener != null) {
                                listener.onNetError(param, MESSAGE_ERROR_NO_SERVICE_TYPE);
                            }
                        }
                    } else {
                        synchronized (this) {
                            if (listener != null) {
                                listener.onMsgSearchComplete(param);
                            }
                        }
                    }
                    break;
                case TaskStatus.END:
                    synchronized (this) {
                        if (listener != null) {
                            listener.onNetEnd(param);
                        }
                    }
                    break;
                }
                return false;
            }
            return super.handleMessage(msg);
        }

        public synchronized void removeListener() {
            super.removeListener();
            listener = null;
        }

        public ActivityCallback(NetworkListener listener, Callback callback) {
            super(callback);
            if (listener != null) {
                this.listener = listener;
            } else {
                throw new NullPointerException("NetworkListener must not be null");
            }
        }

    }

    public final static class FragmentCallback extends ActivityCallback {

        public FragmentCallback(NetworkListener listener) {
            super(listener);
        }

        public FragmentCallback(NetworkListener listener, Callback callback) {
            super(listener, callback);
        }
    }

}

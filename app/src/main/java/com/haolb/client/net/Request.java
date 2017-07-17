package com.haolb.client.net;

import android.os.Handler;

import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.utils.Enums;

import java.io.Serializable;

/**
 * @author zitian.zhang
 * @since 2012-11-8 下午6:21:41
 */
public final class Request {

    private Request() {
    }

    private static final String NO_MESSAGE = "NO_MESSAGE_1314";
    
    /**
     * 发业务接口请求
     * 
     * @param param 请求param
     * @param serviceMap
     * @param handler
     * @param features
     */
    public static void startRequest(BaseParam param, ServiceMap serviceMap, Handler handler, RequestFeature... features) {
        startRequest(param, null, serviceMap, handler, features);
    }

    /**
     * 发业务接口请求
     * 
     * @param param
     * @param serviceMap
     * @param handler
     * @param title 进度框title
     * @param message 进度框message
     * @param features 请求的附加参数
     */
    public static void startRequest(BaseParam param, ServiceMap serviceMap, Handler handler, String message,
            RequestFeature... features) {
        startRequest(param, null, serviceMap, handler, message, features);
    }

    /**
     * 发业务接口请求
     * 
     * @param param
     * @param serviceMap
     * @param handler
     * @param ext 不需要传到服务器的参数，用于返回时使用，常用的值为是否加载更多
     * @param features 请求的附加参数
     */
    public static void startRequest(BaseParam param, Serializable ext, ServiceMap serviceMap, Handler handler,
            RequestFeature... features) {
        startRequest(param, ext, serviceMap, handler, NO_MESSAGE, features);
    }

    /**
     * 发业务接口请求
     * 
     * @param param
     * @param serviceMap
     * @param handler
     * @param ext 不需要传到服务器的参数，用于返回时使用，常用的值为是否加载更多
     * @param title 进度框title
     * @param message 进度框message
     * @param features 请求的附加参数
     */
    public static void startRequest(BaseParam param, Serializable ext, ServiceMap serviceMap, Handler handler,
            String message, RequestFeature... features) {
        final NetworkParam netParam = getRequest(param, serviceMap, features);
        if (!NO_MESSAGE.equals(message)) {
            netParam.progressMessage = message;
        }
        netParam.ext = ext;
        startRequest(netParam, handler);
    }

    /**
     * 自定义发业务接口请求
     * 
     * @param networkParam 使用{@link #getRequest(BaseParam, ServiceMap, RequestFeature[])}方法生成
     * @param handler
     */
    public static void startRequest(NetworkParam networkParam, Handler handler) {
        NetworkManager.getInstance().addTask(networkParam, handler);
    }

    public static NetworkParam getRequest(BaseParam param, ServiceMap serviceMap, RequestFeature[] features) {
        NetworkParam networkParam = new NetworkParam(param, serviceMap);
        if (features == null || features.length == 0) {
            features = DEFAULT_FEATURE;
        }
        for (RequestFeature f : features) {
            switch (f) {
            case ADD_CANCELPRE:
            case ADD_CANCELSAMET:
            case ADD_INSERT2HEAD:
            case ADD_ONORDER:
                networkParam.addType = f.getCode();
                break;
            case BLOCK:
                networkParam.block = true;
                break;
            case CANCELABLE:
                networkParam.cancelAble = true;
                break;
            default:
                break;
            }
        }
        networkParam.progressMessage = "努力加载中……";
        return networkParam;
    }
    
    public final static int NET_ADD_ONORDER = 0; // 顺序添加
    public final static int NET_ADD_INSERT2HEAD = NET_ADD_ONORDER + 1; // 添加到开头
    public final static int NET_ADD_CANCELPRE = NET_ADD_INSERT2HEAD + 1; // 清空之前的再添加
    public final static int NET_ADD_CANCELSAMET = NET_ADD_CANCELPRE + 1; // 取消之前相同的请求再添加

    public static final RequestFeature[] DEFAULT_FEATURE = { RequestFeature.CANCELABLE, RequestFeature.ADD_CANCELSAMET, };

    public enum RequestFeature implements Enums.ITypeCode {
        BLOCK, //
        CANCELABLE, //
        ADD_ONORDER {
            @Override
            public int getCode() {
                return NET_ADD_ONORDER;
            }
        }, //
        ADD_INSERT2HEAD {
            @Override
            public int getCode() {
                return NET_ADD_INSERT2HEAD;
            }
        }, //
        ADD_CANCELPRE {
            @Override
            public int getCode() {
                return NET_ADD_CANCELPRE;
            }
        }, //
        ADD_CANCELSAMET {
            @Override
            public int getCode() {
                return NET_ADD_CANCELSAMET;
            }
        }, //
        ;

        @Override
        public int getCode() {
            return -1;
        }
    }
}

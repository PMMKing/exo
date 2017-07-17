package com.haolb.client.net;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.haolb.client.app.AppConstants;
import com.haolb.client.domain.response.BaseResult;
import com.haolb.client.utils.QLog;
import com.haolb.client.utils.SecureUtil;

/**
 * 处理数据解析
 * @author zexu
 *
 */
public final class Response {
    private Response() {
    }

    public static BaseResult dealWithResponse(byte[] data, NetworkParam param) {
        BaseResult result = null;
        if (data == null) {
            return null;
        }
        try {
            String str = null;
            ServiceMap serviceType = param.key;
            switch (serviceType.getCode()) {
            // 正常的请求
            case ServiceMap.NET_TASKTYPE_CONTROL:
            case ServiceMap.NET_TASKTYPE_FILE:
                str = new String(data);
//                QLog.v("str", "param.ke=" + param.ke);
                if (param.ke != null) {
                	str = SecureUtil.decode(str, param.ke);
//                	QLog.v("str", "str=" + str);
//                	QLog.v("str", "str=" + str.length());
                }
                break;
            }
            QLog.v("response", "API=" + serviceType.name());
            if (AppConstants.DEBUG) {
                JSONObject obj = null;
                try {
                    obj = JSON.parseObject(str);
                } catch (Exception e) {
                	QLog.v("response", String.valueOf(str));
                }
                String[] formattedJsons = JSONObject.toJSONString(obj, true).split("\n");

                for (String line : formattedJsons) {
                	QLog.v("response", line);
                }
            }
            result = JSON.parseObject(str, serviceType.getClazz());
        } catch (Exception e) {
        	QLog.v("", e.getMessage() );
        }
        return result;
    }

}

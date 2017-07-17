package com.haolb.client.net;


import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.domain.response.BaseResult;

import java.io.Serializable;

/**
 * 
 * @author zexu
 */
public class NetworkParam implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String PARAM = "param";
	public static final String TAG = NetworkParam.class.getSimpleName();

    public ServiceMap key;
    public String url;
    public String hostPath = "";
    public boolean block = false;
    public boolean cancelAble = false;
    public String progressMessage = "";
    public int addType = Request.NET_ADD_ONORDER;
    public BaseParam param;
    public BaseResult result;
    public String filePath;
    /** 本地用的参数 */
    public Serializable ext;
    public String ke;

    /* package */NetworkParam(BaseParam param, ServiceMap serviceType) {
        try {
            this.key = serviceType;
            ke = String.valueOf(System.currentTimeMillis());
            ke = new StringBuilder(ke).reverse().toString();
            this.param = param != null ? param : new BaseParam() {
                private static final long serialVersionUID = 1L;
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.key == null ? 0 : this.key.hashCode());
        result = prime * result + (this.param == null ? 0 : this.param.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NetworkParam other = (NetworkParam) obj;
        if (this.key != other.key) {
            return false;
        }
        if (this.param == null) {
            if (other.param != null) {
                return false;
            }
        } else if (!this.param.equals(other.param)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("NetworkParam [key=%s, param=%s]", this.key, this.param);
    }

}

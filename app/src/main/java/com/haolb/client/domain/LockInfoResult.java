package com.haolb.client.domain;

import com.haolb.client.domain.response.BaseResult;

import java.io.Serializable;
import java.util.List;

public class LockInfoResult extends BaseResult {
    private static final long serialVersionUID = 1L;
    public static final String TAG = "LockInfoResult";
    public LockInfoData data;

    public static class LockInfoData implements Serializable {
        private static final long serialVersionUID = 1L;
        public List<LockInfo> gateAuths;

    }

    public static class LockInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public  String gatedata;
        public long  gateid;
        public double  lat; //经度
        public double  lon; //纬度
        public String id;

    }
}

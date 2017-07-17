package com.haolb.client.domain.response;

import java.io.Serializable;
import java.util.List;

public class AuthListResult extends BaseResult {
    private static final long serialVersionUID = 1L;
    public AuthListData data;

    public static class AuthListData implements Serializable {
        private static final long serialVersionUID = 1L;
        public List<MyAuthInfo> applys;
    }
    public static class MyAuthInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public String id;
        public int expiretype;
        public String fromuser;
        public String touser;
        public String communityname;
        public String communityId;
//        public int status; //0授权已被取消  2授权  发出
        public String endtime;
        public int status; //0 申请授权中 1 申请成功 2授权 3 授权已被取消
        public String authtime;
    }

}

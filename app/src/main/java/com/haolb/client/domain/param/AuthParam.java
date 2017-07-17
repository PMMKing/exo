package com.haolb.client.domain.param;

public class AuthParam extends BaseParam {
    private static final long serialVersionUID = 1L;
    public static final String TAG = "AuthParam";
    public int id ;//id
    public String userName ;//会话人id
    public int authType;//0 发出授权 1申请授权 2 取消授权 3取消申请
    public long startTime; //授权开始时间
    public long effTime; //有效时长
    public String communitName; //授权小区
    public String communitId; //授权小区Id
}

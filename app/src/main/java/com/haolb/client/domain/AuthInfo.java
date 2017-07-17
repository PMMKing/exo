package com.haolb.client.domain;

import com.haolb.client.utils.BusinessUtils;

import java.io.Serializable;

public class AuthInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String TAG = "AuthInfo";
    public String authId;
    public int authType;//0 发出授权 1申请授权 2 取消授权 //3取消申请
//    public boolean status; //false未授权 true 已授权
    public boolean overdue; //0未过期 1 过期
    public String sendName; //授权人姓名
    public String acceptName; //授权人姓名
    public long startTime; //授权开始时间
    public long effTime; //有效时长
    public long expiretype; //有效时长
    public String strTime; //有效时长
    public String communit; //授权小区

    public int position;

    public boolean getOverdue(long currentTime) {
        if (currentTime - startTime > effTime ) {
            this.overdue = false;
        } else {
            this.overdue = true;
        }
        return overdue;
    }
    public String getOverTime() {
        int eff =(int)((startTime + effTime - System.currentTimeMillis())/1000);
        return BusinessUtils.formatTimeed(eff)+"后到期";

    }
//    已发出未过期的授权——>取消操作入口
//    已发出已过期的授权——>无
//    已请求未批准未过期的授权——>取消操作入口
//    已请求未批准已过期的授权——>无
//    已请求已批准未过期的授权——>无
//    已请求已批准已过期的授权——>无
}

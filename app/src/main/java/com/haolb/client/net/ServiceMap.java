package com.haolb.client.net;


import com.haolb.client.domain.LockInfoResult;
import com.haolb.client.domain.response.AuthListResult;
import com.haolb.client.domain.response.AuthResult;
import com.haolb.client.domain.response.BaseResult;
import com.haolb.client.domain.response.CheckVersionResult;
import com.haolb.client.domain.response.CityListResult;
import com.haolb.client.domain.response.CommunityDetailResult;
import com.haolb.client.domain.response.CommunityListResult;
import com.haolb.client.domain.response.CommunityResult;
import com.haolb.client.domain.response.ContactListResult;
import com.haolb.client.domain.response.LoginResult;
import com.haolb.client.domain.response.MessageDetailResult;
import com.haolb.client.domain.response.MessageResult;
import com.haolb.client.domain.response.StringResult;
import com.haolb.client.domain.response.UpdateMyPortraitResult;
import com.haolb.client.utils.Enums;

/**
 * 
 * @author zexu
 */
public enum ServiceMap implements Enums.IType {
    CHECK_VERSION("/checkVersion.do",CheckVersionResult.class),//检查客户端更新
    LOGIN("/customerLogin.do",LoginResult.class),//登录
    LOGOUT("/customerLogout.do",BaseResult.class),//退出
    GET_VERIFICATION_CODE("/getVerificationCode.do",BaseResult.class), //获取验证码
    FEED_BACK("/feedback.do",BaseResult.class), //反馈
    GET_MESSAGE("/getMessages.do", MessageResult.class), //小区 通知
    GET_MESSAGE_DETAIL("/getMessageDetail.do", MessageDetailResult.class), //小区 通知
//    GET_USERINFO("/getUserInfo.do",UserInfosResult.class), //获取用户信息
    UPDATE_INFO("/updateMyDetail.do",BaseResult.class),//更新个人信息
    GET_COMMUNITY("/getCommunities.do",CommunityListResult.class), //常用小区
    GETAUTHCOMMUNITIES("/getAuthCommunities.do",CommunityListResult.class), //常用小区
    GET_MY_COMMUNITY("/getMyCommunities.do",CommunityListResult.class), //常用小区
    CHOOSE_COMMUNITY("/chooseCommunity.do",BaseResult.class), //
    SETDEFAULTCOMMUNITY("/setDefaultCommunity.do",BaseResult.class), //设置我的小区
    DEL_COMMUNITY("/deleteMyCommunity.do",BaseResult.class), //删除小区
    GET_COMMUNITY_ITEM("/getBuildings.do",CommunityDetailResult.class), //获取楼 ，层 ，牌号
    GET_CITYS("/getCitys.do",CityListResult.class), //获取楼 ，层 ，牌号
    SET_CITY("/setCity.do",BaseResult.class), //获取楼 ，层 ，牌号
    ADD_COMMUNITY("/addCommunity.do",CommunityResult.class), //添加小区
    SERVICE_POLICY("/servicePolicy.do",StringResult.class), //服务条款
    XIEYI_POLICY("/userProtocol.do",StringResult.class), //协议条款
    APPLY_AUTH("/applyAuth.do",AuthResult.class),//授权 申请授权
    CANCEL_APPLYAUTH("/cancelApplyauth.do",AuthResult.class),//授权 取消申请授权
    GIVE_AUTH("/giveauth.do",AuthResult.class),//授权 授权
    GIVE_AUTH2("/giveauth2.do",AuthResult.class),//授权 申请授权
    CANCEL_AUTH("/cancelauth.do",AuthResult.class),//授权 取消授权
    GETAPPLYAUTHS("/getApplyauths.do",AuthListResult.class),//申请列表授权
    GET_AUTHS("/getAuths.do",AuthListResult.class),//授权列表
    GET_CONTACT("/getContacts.do",ContactListResult.class), //获取通讯录联系人
    GETAUTHGATES("/getAuthgates.do",LockInfoResult.class), //
    OPENGATE("/opengate.do",BaseResult.class), //获取通讯录联系人
    UPDATE_MY_PROTRAIT("/updateMyPortrait.do",UpdateMyPortraitResult.class,ServiceMap.NET_TASKTYPE_FILE), //更新头像
    ;

    private final String mType;
    private final Class<? extends BaseResult> mClazz;
    private final int mTaskType;
    private final static int NET_TASK_START = 0;
    public final static int NET_TASKTYPE_CONTROL = NET_TASK_START;
    public final static int NET_TASKTYPE_FILE = NET_TASKTYPE_CONTROL + 1;
    public final static int NET_TASKTYPE_ALL = NET_TASKTYPE_FILE + 1;

    ServiceMap(String type, Class<? extends BaseResult> clazz) {
        this(type, clazz, NET_TASKTYPE_CONTROL);
    }

    ServiceMap(String type, Class<? extends BaseResult> clazz, int taskType) {
        this.mType = type;
        this.mClazz = clazz;
        this.mTaskType = taskType;
    }

    /**
     * 创建接口对应的resp的Result的对象
     * 
     * @return AbsResult或其子类的对象
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseResult> T newResult() throws IllegalAccessException, InstantiationException {
        return (T) getClazz().newInstance();
    }

    @Override
    public String getDesc() {
        return mType;
    }

    public Class<? extends BaseResult> getClazz() {
        return mClazz;
    }

    @Override
    public int getCode() {
        return mTaskType;
    }
}

package com.haolb.client.domain.response;


import com.haolb.client.domain.Community;

import java.util.List;

/**
 * 登陆结果
 * <p/>
 * Integer userId 用户id
 * String token 登陆标志
 */
public class LoginResult extends BaseResult {
    private static final long serialVersionUID = 1L;

    public LoginData data;

    public static class LoginData implements BaseData {
        private static final long serialVersionUID = 1L;
        public Integer userId;
        public String token;
        public String nickname; //昵称
        public String phone; //手机号
        public String portrait;
        public Community  defCommunity;
        public List<Community> communityList;
        public String password;
        public String cityid;
        public String cityname;
    }

}

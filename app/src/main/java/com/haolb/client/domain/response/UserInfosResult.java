package com.haolb.client.domain.response;

import com.easemob.domain.User;

import java.util.List;


public class UserInfosResult extends BaseResult {
    private static final long serialVersionUID = 1L;

    public UserInfosData data;

    public static class UserInfosData implements BaseData {
        private static final long serialVersionUID = 1L;
        public List<User> userList;
    }
}

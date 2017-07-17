package com.haolb.client.domain.response;

import com.easemob.domain.User;

import java.util.List;

/**
 * 手机联系人服务端校验
 */
public class ContactListResult extends BaseResult {
    private static final long serialVersionUID = 1L;

    public ContactData data;

    public static class ContactData implements BaseData {
        public final static String TAG ="contactData";
        private static final long serialVersionUID = 1L;
        public List<User> contacts;
    }
}
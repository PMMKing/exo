package com.haolb.client.domain.response;

import com.haolb.client.domain.Community;

import java.util.List;


public class CommunityListResult extends BaseResult {
    private static final long serialVersionUID = 1L;

    public CommunityListData data;

    public static class CommunityListData implements BaseData {
        private static final long serialVersionUID = 1L;
        public List<Community> messages;
        public int number;
    }
}

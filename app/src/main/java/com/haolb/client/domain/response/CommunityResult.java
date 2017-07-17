package com.haolb.client.domain.response;

import com.haolb.client.domain.Community;


public class CommunityResult extends BaseResult {
    private static final long serialVersionUID = 1L;

    public CommunityData data;

    public static class CommunityData implements BaseData {
        private static final long serialVersionUID = 1L;
        public Community community;
    }
}
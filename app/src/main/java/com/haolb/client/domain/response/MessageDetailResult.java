package com.haolb.client.domain.response;

import java.io.Serializable;


public class MessageDetailResult extends BaseResult {
    private static final long serialVersionUID = 1L;
    
    public MessageDetailData data;
    public static class MessageDetailData implements Serializable {
		private static final long serialVersionUID = 1L;
        public static final String TAG = "Message";
        public String id;
        public String title;
        public String createtime;
        public String url ;
        public String content ;
        public String community;
    }
 


}
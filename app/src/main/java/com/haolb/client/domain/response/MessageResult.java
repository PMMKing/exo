package com.haolb.client.domain.response;

import java.io.Serializable;
import java.util.List;


public class MessageResult extends BaseResult {
    private static final long serialVersionUID = 1L;
    
    public MessageData data;
//    {"bstatus":{"code":0,"dess":"获取成功!"},"data"
//    :{"totalNum":2,"messages":[{"id":"4","title":"qwe",
//    "content":"qwe","createtime":"2015-03-01 01:13","":null},
//    {"id":"3","title":"asf","content":"asdf","createtime":"2015-03-01 01:04","":null}]}}
    public static class MessageData implements Serializable {
		private static final long serialVersionUID = 1L;
    	public int totalNum;
    	public int newnum;
        public List<Message> messages;
    }
 
    public static class Message implements Serializable {
    	private static final long serialVersionUID = 1L;
		public static final String TAG = "Message";
    	public String id;
        public int type;
    	public String title;
    	public String content;
    	public String createtime;
    	public String community;
        public String url ;
        public boolean isnew;

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Message)) {
                return false;
            }
            return id.equals(((Message) o).id);
        }
    }

}
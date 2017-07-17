package com.haolb.client.domain.response;

import java.util.List;


public class CommunityDetailResult extends BaseResult {
    private static final long serialVersionUID = 1L;

    public CommunityDetailData data;

    public static class CommunityDetailData implements BaseData {
        private static final long serialVersionUID = 1L;
        public List<CommunityItem> buildings;
        public int type ; //0 1 2 3
//        public boolean hasChild;

        public String getTitleByType(){
            if(type == 0){
                return "请选择栋号";
            }else if(type == 1){
                return "请选择楼号";

            }else if(type == 2){
                return "请选择层号";
            }else if(type == 3){
                return "请选择门牌号";
            }
            return "";
        }
    }

    public static class CommunityItem implements BaseData {
        private static final long serialVersionUID = 1L;
        public static final String TAG = "CommunityItem";
        public String id ;
        public String name;
    }
}

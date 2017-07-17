package com.haolb.client.domain.param;

import android.text.TextUtils;

/**
 * Created by chenxi.cui on 2015/6/24.
 */
public class Community extends  BaseParam {
    private static final long serialVersionUID = 1L;
    public static final String TAG = "Community";
    public String id;
    public boolean hasAuth;
    public String  title;//小区名
    public String  city;
    public String  address;
    public String building;//栋号号
    public String lou;//楼号
    public String floor;//层号
    public String number;//牌号
    public boolean isdefault;

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Community)) {
            return false;
        }
        return number.equals(((Community) o).number);
    }

    @Override
    public String toString() {

        StringBuffer sb =new StringBuffer();
        if(!TextUtils.isEmpty( title)){
            sb.append(title);
        }
        if(!TextUtils.isEmpty( building)){
            sb.append("->");
            sb.append(building);
        }
        if(!TextUtils.isEmpty( lou)){
            sb.append("->");
            sb.append(lou);
        }
        if(!TextUtils.isEmpty( floor)){
            sb.append("->");
            sb.append(floor);
        }
        if(!TextUtils.isEmpty( number)){
            sb.append("->");
            sb.append(number);
        }

        return sb.toString();
    }
    public String toString2() {

        StringBuffer sb =new StringBuffer();
        if(!TextUtils.isEmpty( title)){
            sb.append(title);
        }
        if(!TextUtils.isEmpty( building)){
            sb.append(" ");
            sb.append(building);
        }
        if(!TextUtils.isEmpty( lou)){
            sb.append(" ");
            sb.append(lou);
        }
        if(!TextUtils.isEmpty( floor)){
            sb.append(" ");
            sb.append(floor);
        }
        if(!TextUtils.isEmpty( number)){
            sb.append(" ");
            sb.append(number);
        }

        return sb.toString();
    }
}

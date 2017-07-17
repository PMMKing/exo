package com.haolb.client.domain.response;

import java.util.List;

/**
 * 手机联系人服务端校验
 */
public class CityListResult extends BaseResult {
    private static final long serialVersionUID = 1L;

    public ContactData data;

    public static class ContactData implements BaseData {
        public final static String TAG ="contactData";
        private static final long serialVersionUID = 1L;
        public List<City> citys;
    }
    public static class City implements BaseData {
        public final static String TAG ="City";
        private static final long serialVersionUID = 1L;
        public String id;
        public String cityname;

        @Override
        public String toString() {
            return cityname;
        }
    }
}
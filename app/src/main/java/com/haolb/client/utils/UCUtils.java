package com.haolb.client.utils;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.haolb.client.app.MainApplication;
import com.haolb.client.domain.Community;
import com.haolb.client.domain.response.LoginResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.haolb.client.domain.response.CityListResult.City;


/**
 * 用户工具类
 * 
 * @author zexu
 */
public class UCUtils {

    public final static String TAG = "UCUtils";
    private int CLOCK = 60;
    private static UCUtils singleInstance = null;
    public static final int TOTAL_CLOCK = 60;

    public static UCUtils getInstance() {
        if (singleInstance == null) {
            singleInstance = new UCUtils();
        }

        return singleInstance;
    }

    public int subtractClock() {
        this.CLOCK--;
        return this.CLOCK;
    }

    public void initClock() {
        this.CLOCK = TOTAL_CLOCK;
    }

    public int getCurrentClock() {
        return this.CLOCK;
    }

    public void saveCookie(LoginResult loginResult) {
        this.removeCookie(); // 清理旧数据
        if (loginResult != null && loginResult.data != null) {
            if (!TextUtils.isEmpty(loginResult.data.token)) {
                saveToken(loginResult.data.token);
            }
            if (loginResult.data.userId > 0) {
                QLog.i(TAG, loginResult.data.userId + "---------=======");
                saveUserid(loginResult.data.userId + "");
            }
            if (!TextUtils.isEmpty(loginResult.data.phone)) {
                QLog.i(TAG, loginResult.data.phone + "---------=======");
                saveAccount(loginResult.data.phone + "");
            }
            if (!TextUtils.isEmpty(loginResult.data.token)) {
                QLog.i(TAG, loginResult.data.token + "---------=======");
                saveToken(loginResult.data.token + "");
            }
            if (!TextUtils.isEmpty(loginResult.data.portrait)) {
                QLog.i(TAG, loginResult.data.portrait + "---------=======");
                saveUserPortrait(loginResult.data.portrait + "");
            }
            if (!TextUtils.isEmpty(loginResult.data.password)) {
                QLog.i(TAG, loginResult.data.password + "---------=======");
                savePassword(loginResult.data.password + "");
                MainApplication.getInstance().setPassword(loginResult.data.password);
            }
            if (!TextUtils.isEmpty(loginResult.data.nickname)) {
                QLog.i(TAG, loginResult.data.nickname + "---------=======");
                saveUsername(loginResult.data.nickname + "");
            }
            if (!QArrays.isEmpty(loginResult.data.communityList)) {
                saveCommunityList(loginResult.data.communityList);
            }
            if (loginResult.data.defCommunity != null) {
//                saveDefCommunity(loginResult.data.defCommunity);
            }
            if (!TextUtils.isEmpty(loginResult.data.cityid )) {
                City city = new City();
                city.cityname =loginResult.data.cityname;
                city.id =loginResult.data.cityid;
                saveCity(city);
            }
        }
    }


//	public void saveDefCommunity(Community community) {
//        DataUtils.putPreferences("defcommubity", JSON.toJSONString(community)); };
//    public String getNickName(){
//        return DataUtils.getPreferences("nickname" ,"");
//    }
//    public void saveNickName(String nickName){
//        DataUtils.putPreferences("nickname" ,nickName);
//    }

    public String getUsername() {
        return DataUtils.getPreferences("username", "");
    }

    public void saveUsername(String username) {
        DataUtils.putPreferences("username", username);
    }

    public String getPassword() {
        return DataUtils.getPreferences("password", "");
    }

    public void savePassword(String password) {
        DataUtils.putPreferences("password", password);
    }

    public String getToken() {
        return DataUtils.getPreferences("token", "");
    }

    public void saveToken(String token) {
        DataUtils.putPreferences("token", token);
    }

    public String getAccount() {
        return DataUtils.getPreferences("phone", "");
    }

    public void saveAccount(String account) {
        DataUtils.putPreferences("phone", account);
    }

    public void saveUserid(String userId) {
        DataUtils.putPreferences("userId", userId);
    }

    public String getUserid() {
        return DataUtils.getPreferences("userId", "0");
    }

    public void saveUserPortrait(String portrait) {
        DataUtils.putPreferences("portrait", portrait);
    }

    public String getUserPortrait() {
        return DataUtils.getPreferences("portrait", "");
    }


    public boolean userValidate() {
        return TextUtils.isEmpty(getToken());
    }

    public void removeCookie() {
        saveToken("");
        saveUserid("");
        saveAccount("");
        saveUserPortrait("");
        savePassword("");
        saveUsername("");
        saveCommunityList(null);
    }

    public Community getDefCommunity() {
        List<Community> communities = getCommunitys();
        if (QArrays.isEmpty(communities)) {
            return null;
        }
        for (Community community : communities) {
            if (community.isdefault) {
                return community;
            }
        }
        return null;
    }

    public void saveCommunity(Community com) {
        List<Community> communityList = getCommunitys();
        if (!QArrays.isEmpty(communityList)) {
            boolean hasEq = false;
            for (Community community : communityList) {
                community.isdefault = false;
                if (community.equals(com)) {
                    hasEq = true;
                    community.isdefault = com.isdefault;
                }
            }
            if (!hasEq) {
                communityList.add(com);
            }
            if (communityList.size() > 10) {
                communityList.remove(10);
            }
        } else {
            communityList = new ArrayList<Community>();
            communityList.add(com);
        }
        DataUtils.putPreferences(Community.TAG, JSON.toJSONString(communityList));
    }

    public void saveCommunityList(List<Community> communityList) {
        DataUtils.putPreferences(Community.TAG, JSON.toJSONString(communityList));
    }

    public void delCommunity(Community community) {
        List<Community> communities = getCommunitys();
        if (!QArrays.isEmpty(communities)) {
            for (Community community1 : communities) {
                if (!TextUtils.isEmpty(community1.title) && community1.title.equalsIgnoreCase(community.title)) {
                    communities.remove(community1);
                    DataUtils.putPreferences(Community.TAG, JSON.toJSONString(communities));
                    return;
                }
            }
        }
        DataUtils.putPreferences(Community.TAG, JSON.toJSONString(communities));
    }

    public  List<Community> getCommunitys() {
        String josn = DataUtils.getPreferences(Community.TAG, "");
        if (!TextUtils.isEmpty(josn)&&josn!=null) {
            return JSON.parseArray(josn, Community.class);
        }
        return null;
    }
    public int  getCommunitySize() {
        List<Community> communities = getCommunitys();
        int lenght =0 ;
        Map<String ,Community> map = new HashMap<String, Community>() ;
        if(QArrays.isEmpty(communities)){
            return  0;
        }
        for(Community community :communities){
            map.put(community.title ,community);
        }
        return map.size();
    }

    public void saveCity(City city) {
        DataUtils.putPreferences(City.TAG, JSON.toJSONString(city));
    }

    public City getCity() {
        String josn = DataUtils.getPreferences(City.TAG, "");
        if (!TextUtils.isEmpty(josn)) {
            City city = JSON.parseObject(josn, City.class);
            if("1".equals(city.id)){
                city.cityname ="北京";
            }
            return city;
        }else {
            City city =new City();
            city.id = "1";
            city.cityname = "北京";
            return city;
        }

    }

}

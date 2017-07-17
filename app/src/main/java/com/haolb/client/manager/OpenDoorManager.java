package com.haolb.client.manager;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.haolb.client.utils.DataUtils;
import com.haolb.client.utils.QArrays;

import java.util.ArrayList;
import java.util.List;

import static com.haolb.client.domain.LockInfoResult.LockInfo;

/**
 * Created by chenxi.cui on 2015/7/2.
 */
public class OpenDoorManager {
    public static final String TAG = "OpenDoorManger";
    private static final double MIN_DISTANCE = 20;

    public static void addAll(List<LockInfo> lockInfos) {
        synchronized (OpenDoorManager.class) {
            DataUtils.putPreferences(TAG, JSON.toJSONString(lockInfos));
        }
    }

    public static void updateAll(List<LockInfo> lockInfos) {


    }

    public static List<LockInfo>  getAll() {
        synchronized (OpenDoorManager.class){
            String josn = DataUtils.getPreferences( TAG, "");
            if (!TextUtils.isEmpty(josn)) {
                return JSON.parseArray(josn, LockInfo.class);
            }
            return null;
        }
    }

    public static LockInfo findKeyLockId(long lockId) {
        List<LockInfo> lockInfos = getAll();
        if(QArrays.isEmpty(lockInfos)){
            return null;
        }
        for(LockInfo l :lockInfos){
            if(l.gateid==lockId){
                return l;
            }
        }
        return null;
    }

    public static List<LockInfo> findKeyLockId(double lon ,double lan) {
        List<LockInfo> lockInfos = getAll();
        if(QArrays.isEmpty(lockInfos)){
            return null;
        }
        List<LockInfo> lockInfoList = null;
        for(LockInfo l :lockInfos){
            double distance = getDistance(l.lat ,l.lon , lan , lon);
            if(distance < MIN_DISTANCE){
                lockInfoList =new ArrayList<LockInfo>();
                lockInfoList.add(l);
            }
        }
        return lockInfoList;
    }

    public static NearByInfo nearbyLock(double lon ,double lan) {
        List<LockInfo> lockInfos = getAll();
        if(QArrays.isEmpty(lockInfos)){
            return null;
        }
        NearByInfo nearByInfo = new NearByInfo();
         nearByInfo.douNearby = -1 ;
        for(LockInfo l :lockInfos){
            if(l.lat==0 && l.lon==0){

            }else {
                double distance = getDistance(l.lat ,l.lon , lan , lon);
                if(nearByInfo.douNearby > distance || nearByInfo.douNearby ==-1){
                    nearByInfo.douNearby = distance;
                    nearByInfo.lan = l.lat;
                    nearByInfo.lon = l.lon;
                }
            }
        }

        return nearByInfo;
    }
    public static class  NearByInfo{
        public double douNearby ;
        public double lan ;
        public double lon ;
    }

    private static double EARTH_RADIUS = 6378137;
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

}

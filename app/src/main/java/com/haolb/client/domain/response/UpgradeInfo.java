package com.haolb.client.domain.response;

import java.io.Serializable;

public class UpgradeInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public Update[] upgradeAddress ;
    public String upgradeNote;// 升级提示语
    //    public boolean force;// 强制升级标识
//    public int upgradeFlag;//升级标示
    public String nversion;//新版的版本号
    public String versionCode;//新版的版本号
    public static class Update implements Serializable{
        public String url;//新版的版本号
        public String source;//新版的版本号
    }
    
}
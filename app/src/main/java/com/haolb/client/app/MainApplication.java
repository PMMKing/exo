/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haolb.client.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.easemob.EMCallBack;
import com.easemob.MainHXSDKHelper;
import com.easemob.domain.User;
import com.haolb.client.utils.DataUtils;
import com.haolb.client.utils.QArrays;
import com.haolb.client.utils.UCUtils;

import java.util.List;
import java.util.Map;

import static com.haolb.client.domain.response.MessageResult.Message;

public class MainApplication extends Application {

    public static Context applicationContext;
    private static MainApplication instance;
    // login user name
    public final String PREF_USERNAME = "username";

    /**
     * 当前用户nickname,为了苹果推送不是userid而是昵称
     */
    public static String currentUserNick = "";
    public static MainHXSDKHelper hxSDKHelper = new MainHXSDKHelper();
    public String versionName;
    public int versionCode;
    public List<Message> messageList;
    public double lan;
    public double lon;
//    private boolean hasNewMessage;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        instance = this;
        try {
            String pkName = this.getPackageName();
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(
                    pkName, 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        /**
         * this function will initialize the HuanXin SDK
         *
         * @return boolean true if caller can continue to call HuanXin related APIs after calling onInit, otherwise false.
         *
         * 环信初始化SDK帮助函数
         * 返回true如果正确初始化，否则false，如果返回为false，请在后续的调用中不要调用任何和环信相关的代码
         *
         * for example:
         * 例子：
         *
         * public class DemoHXSDKHelper extends HXSDKHelper
         *
         * HXHelper = new DemoHXSDKHelper();
         * if(HXHelper.onInit(context)){
         *     // do HuanXin related work
         * }
         */
        hxSDKHelper.onInit(applicationContext);
    }

    public static MainApplication getInstance() {
        return instance;
    }

    /**
     * 获取内存中好友user list
     *
     * @return
     */
    public Map<String, User> getContactList() {
        return hxSDKHelper.getContactList();
    }

    /**
     * 设置好友user list到内存中
     *
     * @param contactList
     */
    public void setContactList(Map<String, User> contactList) {
        Map<String, User> userMap = hxSDKHelper.getContactList();
        userMap.putAll(contactList);
        hxSDKHelper.setContactList(userMap);
    }

    /**
     * 获取当前登陆用户名
     *
     * @return
     */
    public String getUserName() {
        return hxSDKHelper.getHXId();
    }

    /**
     * 获取密码
     *
     * @return
     */
    public String getPassword() {
        return hxSDKHelper.getPassword();
    }

    /**
     * 设置用户名
     */
    public void setUserName(String username) {
        hxSDKHelper.setHXId(username);
    }

    /**
     * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
     * 内部的自动登录需要的密码，已经加密存储了
     *
     * @param pwd
     */
    public void setPassword(String pwd) {
        hxSDKHelper.setPassword(pwd);
    }

    /**
     * 退出登录,清空数据
     */
    public void logout(final EMCallBack emCallBack) {
        // 先调用sdk logout，在清理app中自己的数据
        hxSDKHelper.logout(emCallBack);
        UCUtils.getInstance().removeCookie();
    }

    public void updateMessage(Message message) {
        List<Message> messages = getMessage();
        if (!QArrays.isEmpty(messages)) {
            for (Message com : messages) {
                if (message.equals(com)) {
                     com =message;
                }
            }
        }
        saveMessage();
    }

    public void saveMessage() {
        DataUtils.putPreferences(Message.TAG, JSON.toJSONString(messageList));
    }

    public void initMessage() {
        String josn = DataUtils.getPreferences(Message.TAG, "");
        if (!TextUtils.isEmpty(josn)) {
            messageList = JSON.parseArray(josn, Message.class);
        }
    }

    public void setMessage(List<Message> messages) {
//        hasNewMessage = getHasNewMessage();
//        if (!QArrays.isEmpty(messages) && !QArrays.isEmpty(this.messageList)) {
//            if (messages.size() == this.messageList.size()) {
//                    hasNewMessage = false;
//            } else {
////                int length = messageList.size();
////                for (int i = 0; i < length; i++) {
////                    messages.remove(messages.size()-1-i);
////                }
////                messages.addAll(messageList);
//                messageList = messages;
//                hasNewMessage = true;
//            }
//        } else if (QArrays.isEmpty(messages)) {
            messageList = messages;
//            hasNewMessage = false;
//        } else {
//            messageList = messages;
//            hasNewMessage = true;
//        }
    }

    public List<Message> getMessage() {
        return messageList;
    }

    public boolean getHasNewMessage() {
        if(!QArrays.isEmpty(messageList)){
            for (Message message :messageList){
                if(message.isnew){
                    return true;
                }
            }
        }
        return false;
    }

//    public void setHasNewMessage(boolean hasNewMessage) {
//        this.hasNewMessage = hasNewMessage;
//    }


}

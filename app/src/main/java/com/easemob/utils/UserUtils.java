package com.easemob.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.easemob.domain.User;
import com.haolb.client.R;
import com.haolb.client.app.MainApplication;
import com.haolb.client.utils.cache.ImageLoader;

public class UserUtils {
    /**
     * 根据username获取相应user，由于demo没有真实的用户数据，这里给的模拟的数据；
     * @param username
     * @return
     */
    public static User getUserInfo(String username){
        User user = MainApplication.getInstance().getContactList().get(username);
        if(user == null){
            user = new User(username);
        }
            
        if(user != null){
            //demo没有这些数据，临时填充
            if(TextUtils.isEmpty(user.getNick())){
                if(TextUtils.isEmpty(user.getContactName())){
                    user.setNick(username);
                }else {
                    user.setNick(user.getContactName());
                }

            }
//            user.setAvatar("http://downloads.easemob.com/downloads/57.png");
        }
        return user;
    }
    
    /**
     * 设置用户头像
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView){
        User user = getUserInfo(username);
        if(user != null){
            ImageLoader.getInstance(context).loadImage(user.getAvatar(), imageView, R.drawable.default_avatar);
        }
    }
    
}

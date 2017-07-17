package com.haolb.client.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.PopupWindow;

/**
 * 
 * @author zexu
 */
public interface IBaseActFrag {

    void qStartActivity(Intent intent);

    void qStartActivity(Class<? extends Activity> cls);

    /** 打开新的Activity */
    void qStartActivity(Class<? extends Activity> cls, Bundle bundle);

    void qStartShareActivity(String title, String shareContent);

    void qStartImageShare(String shareContent, Uri uri);

    /** 打开新的Activity for result */
    void qStartActivityForResult(Class<? extends Activity> cls, Bundle bundle, int requestCode);

    /** 带结果返回上一个activity， 配合qStartActivityForResult使用 */
    void qBackForResult(int resultCode, Bundle bundle);

    /** 回到之前的Activity */
    void qBackToActivity(Class<? extends Activity> cls, Bundle bundle);

    PopupWindow showTipView(View view);

    void processAgentPhoneCall(String phoneNum);

    Context getContext();

    Handler getHandler();

	void qShowAlertMessage(String title, String message);
    void showToast(String message) ;
}

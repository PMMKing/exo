package com.haolb.client.manager;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import com.haolb.client.R;
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;

import java.io.Serializable;


/**
 * Created by chenxi.cui on 2015/7/2.
 */
public class AuthManager {


    /**
     * 申请授权
     *
     * @param authParam
     * @param mHandler
     */
    public static void applyAuth(Context context ,final ApplyAuthParam authParam, final Serializable ext, final Handler mHandler) {
        new android.app.AlertDialog.Builder(context).setTitle("提示")
                .setMessage("申请授权")
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Request.startRequest(authParam, ext,ServiceMap.APPLY_AUTH , mHandler,"申请授权中...", Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }

    /**
     * 取消授权申请
     *
     * @param authParam
     * @param mHandler
     */
    public static void cancelApplyauth(Context context ,final ApplyCancelAuthParam authParam, final Serializable ext, final Handler mHandler) {
        new android.app.AlertDialog.Builder(context).setTitle("提示")
                .setMessage("取消授权申请")
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Request.startRequest(authParam, ext,ServiceMap.CANCEL_APPLYAUTH , mHandler, "取消授权中...",Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }
    /**
     * 授权
     *
     * @param authParam
     * @param mHandler
     */
    public static void giveAuth(Context context ,final AuthParam authParam, final Serializable ext, final Handler mHandler) {
        new android.app.AlertDialog.Builder(context).setTitle("提示")
                .setMessage("授权")
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Request.startRequest(authParam, ext,ServiceMap.GIVE_AUTH , mHandler, "授权中...",Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }
    /**
     * 主动授权
     *
     * @param authParam
     * @param mHandler
     */
    public static void giveAuth2(Context context ,final Auth2Param authParam, final Serializable ext, final Handler mHandler) {
        new android.app.AlertDialog.Builder(context).setTitle("提示")
                .setMessage("授权")
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Request.startRequest(authParam, ext,ServiceMap.GIVE_AUTH2 , mHandler, "授权中...",Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }
    /**
     * 取消授权
     *
     * @param authParam
     * @param mHandler
     */
    public static void cancelAuth(Context context, final CancelAuthParam authParam,final Serializable ext, final Handler mHandler) {
        new android.app.AlertDialog.Builder(context).setTitle("提示")
                .setMessage("取消授权")
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Request.startRequest(authParam, ext,ServiceMap.CANCEL_AUTH , mHandler,"取消授权中...", Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }
    /**
     * 授权
     */
    public static class Auth2Param extends BaseParam {
        public String touser;
        public String communityId;
        public long expiretype;
    }

    /**
     * 授权
     */
    public static class AuthParam extends BaseParam {
        public String id;
        public long expiretype;
    }
    /**
     * 取消授权
     */
    public static class CancelAuthParam extends BaseParam {
        public String applyAuthId;
    }

    /**
     * 申请授权
     */
    public static class ApplyAuthParam extends BaseParam {
        public int expiretype;
        public String communityId;
        public String touser;
    }
    /**
     * 取消授权申请
     */
    public static class ApplyCancelAuthParam extends BaseParam {
        public String applyAuthId;
    }




}

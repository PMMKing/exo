package com.haolb.client.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haolb.client.R;
import com.haolb.client.app.MainApplication;
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.domain.response.CheckVersionResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.inject.From;

import static com.haolb.client.net.Request.RequestFeature;

public class AboutAct extends SwipeBackActivity {

    @From(R.id.tv_ver)
    private TextView tvVar;
    @From(R.id.tv_update)
    private TextView tvUpdate;
    @From(R.id.ll_tk)
    private LinearLayout llTk;
    @From(R.id.ll_xy)
    private LinearLayout llXy;
    @From(R.id.ll_update)
    private LinearLayout llUpdate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitleBar("关于我们", true);
        tvVar.setText( MainApplication.getInstance().versionName+"正式版");
        llTk.setOnClickListener(this);
        llXy.setOnClickListener(this);
        llUpdate.setOnClickListener(this);
        BaseParam param = new BaseParam();
        Request.startRequest(param,  ServiceMap.CHECK_VERSION, mHandler, RequestFeature.CANCELABLE);
    }

    @Override
    protected void onResume() {
        tvUpdate.setText("已是最新版本");
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_tk) {
            qStartActivity(ServiceAct.class);
        } else if (v.getId() == R.id.ll_xy) {
            qStartActivity(XieYiAct.class);
        } else if (v.getId() == R.id.ll_update) {
            BaseParam param = new BaseParam();
            isUpdate = true ;
            Request.startRequest(param, "" ,ServiceMap.CHECK_VERSION, mHandler,RequestFeature.BLOCK, RequestFeature.CANCELABLE);
        }

        //	点击事件
    }
    public boolean isUpdate = false ;

    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        boolean parentExecuted = super.onMsgSearchComplete(param);
        if (parentExecuted) {
            // 父类已经处理了
            return true;
        }
        switch (param.key) {
            case CHECK_VERSION: //http://www.96we.com/az.apk
                final CheckVersionResult checkVersionResult = (CheckVersionResult) param.result;
                if (checkVersionResult.bstatus.code == 0) {
                    if (checkVersionResult.data != null
                            && checkVersionResult.data.upgradeInfo != null) {
                        if(!isUpdate){
                            tvUpdate.setText("点击更新最新版本");
                        }else{
                            AlertDialog.Builder dialog = null;
                            if (dialog == null) {
                                dialog = new AlertDialog.Builder(this);
                                dialog.setTitle("更新提示");
                                dialog.setMessage("最新版本："
                                        + checkVersionResult.data.upgradeInfo.nversion
                                        + "/n"
                                        + checkVersionResult.data.upgradeInfo.upgradeNote);
                                dialog.setPositiveButton("更新", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Uri uri = Uri
                                                .parse(checkVersionResult.data.upgradeInfo.upgradeAddress[0].url);
                                        Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(it);

                                    }

                                });
                            }
                            dialog.show();
                        }
                    }

                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            default:
                break;
        }
        return false;
    }
}
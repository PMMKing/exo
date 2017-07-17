package com.haolb.client.activity;

import android.os.Bundle;
import android.webkit.WebView;

import com.haolb.client.R;
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.domain.response.StringResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.inject.From;


public class XieYiAct extends SwipeBackActivity {

    @From(R.id.webview)
    private WebView webview;
    private StringResult result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_service);
        setTitleBar("用户协议", true);
        super.onCreate(savedInstanceState);
        BaseParam param = new BaseParam();
        Request.startRequest(param, ServiceMap.XIEYI_POLICY, mHandler,
                Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
    }

    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        boolean parentExecuted = super.onMsgSearchComplete(param);
        if (parentExecuted) {
            // 父类已经处理了
            return true;
        }
        switch (param.key) {
            case XIEYI_POLICY:
                switch (param.result.bstatus.code) {
                    case 0:
                        result = (StringResult) param.result;
                        webview.loadDataWithBaseURL(null, result.data, "text/html",
                                "utf-8", null);
                        break;
                    default:
                        showToast(param.result.bstatus.des);
                        break;
                }
                break;
            default:
                break;
        }
        return false;
    }
}
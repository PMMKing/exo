package com.haolb.client.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.haolb.client.R;
import com.haolb.client.domain.param.FeedBackParam;
import com.haolb.client.domain.response.BaseResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;

public class FeedBackAct extends SwipeBackActivity {

    private EditText content, phone;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        setTitleBar("意见反馈", true);
        init();
    }

    private void init() {
        content = (EditText) findViewById(R.id.content);
        // phone = (EditText) findViewById(R.id.phone);
        findViewById(R.id.feedback).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.feedback:
                String s = content.getText().toString().trim();
//			String c = phone.getText().toString().trim();
                if (!TextUtils.isEmpty(s)) {
                    FeedBackParam param = new FeedBackParam();
                    // param.phone = c;
                    param.content = s;
                    Request.startRequest(param, ServiceMap.FEED_BACK, mHandler,
                            "发送中...", Request.RequestFeature.BLOCK,
                            Request.RequestFeature.CANCELABLE);
                } else {
                    showToast("意见内容不能为空");
                }

                break;

            default:
                break;
        }
    }

    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        if (super.onMsgSearchComplete(param)) {
            // 父类已经处理了
            return true;
        }
        switch (param.key) {
            case FEED_BACK:
                BaseResult feedBackResult = (BaseResult) param.result;
                if (feedBackResult.bstatus.code == 0) {
                    showToast("发送成功");
                    finish();
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
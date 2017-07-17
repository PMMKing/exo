package com.haolb.client.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.haolb.client.R;
import com.haolb.client.domain.param.CommunityDetailParam;
import com.haolb.client.domain.response.MessageDetailResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.cache.ImageLoader;
import com.haolb.client.utils.inject.From;

import static com.haolb.client.domain.response.MessageDetailResult.MessageDetailData;
import static com.haolb.client.domain.response.MessageResult.Message;
import static com.haolb.client.net.Request.RequestFeature.BLOCK;
import static com.haolb.client.net.Request.RequestFeature.CANCELABLE;

/**
 * Created by chenxi.cui on 2015/6/18.
 */
public class MessageDetailAct extends SwipeBackActivity {
    @From(R.id.tv_title)
    private TextView tvTitle;
    @From(R.id.tv_name)
    private TextView tvName;
    @From(R.id.tv_date)
    private TextView tvDate;
    @From(R.id.tv_content)
    private WebView tvContent;
    @From(R.id.image_url)
    private ImageView imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_detail);
        setTitleBar("详情", true);
        Message message = (Message) myBundle.getSerializable(Message.TAG);
        if (message == null) {
            finish();
        }
        CommunityDetailParam param = new CommunityDetailParam();
        param.id = message.id;
        Request.startRequest(param, ServiceMap.GET_MESSAGE_DETAIL, mHandler,BLOCK ,CANCELABLE);

    }
    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        boolean parentExecuted = super.onMsgSearchComplete(param);
        if (parentExecuted) {
            // 父类已经处理了
            return true;
        }
        switch (param.key) {
            case GET_MESSAGE_DETAIL:
                if (param.result.bstatus.code == 0) {
                    MessageDetailResult messageResult =(MessageDetailResult) param.result;
                    MessageDetailData message = messageResult.data;
                    tvTitle.setText(message.title);
                    tvName.setText(message.community);
                    tvDate.setText(message.createtime);
                    if(!TextUtils.isEmpty(message.content)){
                        tvContent.loadData( message.content,"text/html; charset=UTF-8", null);
                        tvContent.setVisibility(View.VISIBLE);
                    }else {
                        tvContent.setVisibility(View.GONE);
                    }
                    if(TextUtils.isEmpty(message.url)){
                        imageUrl.setVisibility(View.GONE);
                    }else {
                        ImageLoader.getInstance(this).loadImage(message.url, imageUrl, R.drawable.empty_photo);
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

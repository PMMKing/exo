package com.haolb.client.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.easemob.Constant;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.haolb.client.R;
import com.haolb.client.adapter.utils.QSimpleAdapter;
import com.haolb.client.domain.AuthInfo;
import com.haolb.client.domain.LockInfoResult;
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.domain.response.AuthListResult;
import com.haolb.client.domain.response.BaseResult;
import com.haolb.client.manager.AuthManager;
import com.haolb.client.manager.OpenDoorManager;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.DateTimeUtils;
import com.haolb.client.utils.QArrays;
import com.haolb.client.utils.QLog;
import com.haolb.client.utils.inject.From;

import java.util.Calendar;
import java.util.List;

import static com.haolb.client.domain.response.AuthListResult.MyAuthInfo;

public class MyAuthRecAct extends SwipeBackActivity {
    @From(R.id.listSend)
    private ListView listSend;
    @From(R.id.listRes)
    private ListView listRes;
    @From(R.id.atom_flight_special_rg)
    private RadioGroup rg;

    private MyResListAdp mResAdapter;
    private int index=0;
    private MyAuthListAdp mSendAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_auth_list);
        setTitleBar("授权列表", true);
        mSendAdapter = new MyAuthListAdp(this);
        mResAdapter = new MyResListAdp(this);
        listRes.setAdapter(mResAdapter);
        listSend.setAdapter(mSendAdapter);
        listSend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                final MyAuthInfo authInfo = (MyAuthInfo) parent.getAdapter().getItem(position);
                if (authInfo.status == 1) { //未过期取消授权
                    AuthManager.CancelAuthParam cancelAuthParam = new AuthManager.CancelAuthParam();
                    cancelAuthParam.applyAuthId = authInfo.id;
                    AuthManager.cancelAuth(MyAuthRecAct.this, cancelAuthParam, position, mHandler);
                }else if (authInfo.status == 0) { //授权
                    AuthManager.AuthParam authParam = new AuthManager.AuthParam();
                    authParam.id = authInfo.id;
                    authParam.expiretype = authInfo.expiretype;
                    AuthManager.giveAuth(MyAuthRecAct.this, authParam, position, mHandler);
                }
            }
        });
         if(index==0){
             rg.check(R.id.atom_flight_rb_dom);
             listSend.setVisibility(View.VISIBLE);
             listRes.setVisibility(View.GONE);
             Request.startRequest(new BaseParam(), ServiceMap.GET_AUTHS, mHandler, Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
         }else{
             listSend.setVisibility(View.GONE);
             listRes.setVisibility(View.VISIBLE);
             Request.startRequest(new BaseParam(), ServiceMap.GETAPPLYAUTHS, mHandler, Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
             rg.check(R.id.atom_flight_rb_inter);
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if(checkedId==R.id.atom_flight_rb_dom){
                    listSend.setVisibility(View.VISIBLE);
                    listRes.setVisibility(View.GONE);
                    Request.startRequest(new BaseParam(), ServiceMap.GET_AUTHS, mHandler, Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
                }else if(checkedId==R.id.atom_flight_rb_inter){
                    listSend.setVisibility(View.GONE);
                    listRes.setVisibility(View.VISIBLE);
                    Request.startRequest(new BaseParam(), ServiceMap.GETAPPLYAUTHS, mHandler, Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
                }
            }
        });
    }




    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        boolean parentExecuted = super.onMsgSearchComplete(param);
        if (parentExecuted) {
            // 父类已经处理了
            return true;
        }
        switch (param.key) {
            case GET_AUTHS:
                if (param.result.bstatus.code == 0) {
                    mSendAdapter.notifyDataSetChanged();
                    AuthListResult authListResult = (AuthListResult) param.result;
                    mSendAdapter.setData(authListResult.data.applys);
                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            case CANCEL_AUTH:
                if (param.result.bstatus.code == 0) {
                    int position = (Integer) param.ext;
                    MyAuthInfo authInfo =   mSendAdapter.getItem(position);
                    authInfo.status = 2;
                    mSendAdapter.notifyDataSetChanged();
                }
                showToast(param.result.bstatus.des);
                break;
            case GIVE_AUTH:
                if (param.result.bstatus.code == 0) {
                    int position = (Integer) param.ext;
                    MyAuthInfo authInfo = mSendAdapter.getItem(position);
                    authInfo.status = 1;
                    mSendAdapter.notifyDataSetChanged();
                }
                showToast(param.result.bstatus.des);
                break;
            case GETAUTHGATES:
                if (param.result.bstatus.code == 0) {
                    LockInfoResult lockInfoResult = (LockInfoResult) param.result;
                    OpenDoorManager.addAll(lockInfoResult.data.gateAuths);
                }

                break;

            case GETAPPLYAUTHS:
                if (param.result.bstatus.code == 0) {
                    mResAdapter.notifyDataSetChanged();
                    AuthListResult authListResult = (AuthListResult) param.result;
                    mResAdapter.setData(authListResult.data.applys);
                } else {
                    showToast(param.result.bstatus.des);
                }
//                BaseParam params = new BaseParam();
//                Request.startRequest(params, ServiceMap.GETAUTHGATES, mHandler);
                break;
            case CANCEL_APPLYAUTH:
                BaseResult authResult = (BaseResult) param.result;
                if (authResult.bstatus.code == 0) {
                    int position = (Integer) param.ext;
                    MyAuthInfo authInfo = (MyAuthInfo) mResAdapter.getItem(position);
                    authInfo.status = 2;
                    mResAdapter.notifyDataSetChanged();
//                    86450
                    EMConversation conversation = EMChatManager.getInstance().getConversationByType("15811508404", EMConversation.EMConversationType.Chat);
                    List<EMMessage> cns = conversation.getAllMessages();
                    if (!QArrays.isEmpty(cns)) {
                        for (EMMessage emessage : cns) {
                            if (emessage.getType() == EMMessage.Type.TXT && emessage.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_AUTH, false)) {
                                TextMessageBody txtBody = (TextMessageBody) emessage.getBody();
                                String msg = txtBody.getMessage();
                                final AuthInfo authMassageBody = JSON.parseObject(msg, AuthInfo.class);
                                if (authInfo.id.equals(authMassageBody.authId)) {


                                    authMassageBody.authType = 3;
                                    String content = JSON.toJSONString(authMassageBody);
                                    if (content.length() > 0) {
                                        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                                        message.setAttribute(Constant.MESSAGE_ATTR_IS_AUTH, true);

                                        TextMessageBody retxtBody = new TextMessageBody(content);
                                        // 设置消息body
                                        message.addBody(retxtBody);
                                        // 设置要发给谁,用户username或者群聊groupid
                                        message.setReceipt(authInfo.touser);
                                        // 把messgage加到conversation中
                                        conversation.addMessage(message);
                                        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {
                                            @Override
                                            public void onSuccess() {
                                                QLog.v("test", "onSuccess");
                                            }

                                            @Override
                                            public void onError(int i, String s) {

                                                QLog.v("test", "onError");
                                            }

                                            @Override
                                            public void onProgress(int i, String s) {

                                                QLog.v("test", "onProgress");
                                            }
                                        });

                                    }
                                }
                            }
                        }
                    }
                }
                showToast(param.result.bstatus.des);
                break;
            default:
                break;
        }
        return false;
    }

    private class MyResListAdp extends QSimpleAdapter<MyAuthInfo> {

        String[] arr;

        public MyResListAdp(Context context) {
            super(context);
            arr = getResources().getStringArray(R.array.spingarr);
        }

        @Override
        protected View newView(Context context, ViewGroup parent) {
            return inflate(R.layout.activity_my_auth_item, null, false);
        }

        @Override
        protected void bindView(View view, Context context, MyAuthInfo item, int position) {
            TextView tvName = (TextView) view.findViewById(R.id.tv_name);
            TextView tvCommunity = (TextView) view.findViewById(R.id.tv_community);
            TextView tvEff = (TextView) view.findViewById(R.id.tv_eff);
            TextView tvSend = (TextView) view.findViewById(R.id.tv_send);
//            ImageView imageAuth = (ImageView) view.findViewById(R.id.image_auth);
            tvName.setText(item.touser + "授权");
            tvCommunity.setText(item.communityname);
            boolean isEff =false ;
            try {
                Calendar endCalendar = DateTimeUtils.getCalendar(item.endtime);
                Calendar curCalendar = DateTimeUtils.getCalendar(System.currentTimeMillis());

                if (endCalendar.before(curCalendar)) {
                    tvEff.setText("开始:" + item.authtime + "\n周期:" + arr[item.expiretype]);
                      isEff = true;
                } else {
                    tvEff.setText(item.endtime + "到期");
                }

            } catch (Exception e) {
                tvEff.setText("授权时间:" + arr[item.expiretype]);
            }
            if (isEff) {
                tvSend.setTextColor(context.getResources().getColor(R.color.common_color_gray));
                tvSend.setVisibility(View.VISIBLE);
                tvSend.setText("已过期");
            }else if (item.status == 1) {
                tvSend.setTextColor(context.getResources().getColor(R.color.common_color_gray));
                tvSend.setText("已授权");
            } else if (item.status == 2) {
                tvSend.setTextColor(context.getResources().getColor(R.color.common_color_gray));
                tvSend.setText("已取消申请");
            } else if (item.status == 3) {
                tvSend.setTextColor(context.getResources().getColor(R.color.common_color_gray));
                tvSend.setText("已取消");
            } else {
                tvSend.setTextColor(context.getResources().getColor(R.color.common_color_orange));
                tvSend.setText("等待授权");
            }
        }
    }
    private class MyAuthListAdp extends QSimpleAdapter<MyAuthInfo> {
        String [] arr ;
        public MyAuthListAdp(Context context) {
            super(context);
            arr = getResources().getStringArray(R.array.spingarr);
        }

        @Override
        protected View newView(Context context, ViewGroup parent) {
            return inflate(R.layout.activity_my_auth_item, null, false);
        }

        @Override
        protected void bindView(View view, Context context, MyAuthInfo item, int position) {
            TextView tvName = (TextView) view.findViewById(R.id.tv_name);
            TextView tvCommunity = (TextView) view.findViewById(R.id.tv_community);
            TextView tvEff = (TextView) view.findViewById(R.id.tv_eff);
            TextView tvSend = (TextView) view.findViewById(R.id.tv_send);
//            ImageView imageAuth = (ImageView) view.findViewById(R.id.image_auth);
            tvName.setText("授权" + item.touser);
            tvCommunity.setText(item.communityname);
           boolean  isEff   =false ;
            try {
                Calendar endCalendar = DateTimeUtils.getCalendar(item.endtime);
                Calendar curCalendar = DateTimeUtils.getCalendar(System.currentTimeMillis());

                if(endCalendar.before(curCalendar)){
                    tvEff.setText("开始:"+item.authtime+"\n周期:"+arr[item.expiretype]);
                    isEff =true;
                }else{
                    tvEff.setText(item.endtime +"到期");
                }

            }catch (Exception e){
                tvEff.setText("有效时间:"+arr[item.expiretype]);
            }

            if (isEff) {
                tvSend.setTextColor(context.getResources().getColor(R.color.common_color_gray));
                tvSend.setVisibility(View.VISIBLE);
                tvSend.setText("已过期");
            }else if (item.status== 1) {
                tvSend.setVisibility(View.VISIBLE);
                tvSend.setText("取消");
                tvSend.setTextColor(context.getResources().getColor(R.color.common_color_orange));
            }else if (item.status==0) {
                tvSend.setVisibility(View.VISIBLE);
                tvSend.setText("授权");
                tvSend.setTextColor(context.getResources().getColor(R.color.common_color_orange));
            } else {
                tvSend.setText("已取消");
                tvSend.setTextColor(context.getResources().getColor(R.color.common_color_gray));
                tvSend.setVisibility(View.VISIBLE);
            }
        }
    }
}
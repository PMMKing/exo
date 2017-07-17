package com.haolb.client.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.haolb.client.R;
import com.haolb.client.domain.AuthInfo;
import com.haolb.client.domain.Community;
import com.haolb.client.domain.response.AuthResult;
import com.haolb.client.manager.AuthManager;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.UCUtils;
import com.haolb.client.utils.inject.From;

import static com.haolb.client.manager.AuthManager.ApplyAuthParam;

public class AuthAct extends SwipeBackActivity {
    @From(R.id.tv_name)
    private TextView tvName;
    @From(R.id.tv_community)
    private TextView tvCommunity;
    @From(R.id.tv_time)
    private TextView tvTime;
    @From(R.id.tv_phone)
    private TextView tvPhone;
    @From(R.id.btn_send)
    private Button btnSend;
    @From(R.id.spinner)
    private Spinner spinner;
    private int type;
    private String username;
    private Community community;
//    30分钟、1小时、3小时、12小时、24小时
    long[] tiems = new long[]{1800000,3600000,33600000 ,123600000 ,24*3600000};
    private int selectTime=0;
    private String nick;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_detail);
        type =myBundle.getInt("type" ,1);
        username =myBundle.getString("username");
        nick =myBundle.getString("nick");

        if(type == 1){
            setTitleBar("申请授权", true);
            btnSend.setText("申请授权");
            tvPhone.setText(username);
            tvName.setText(nick);
        }else {
            btnSend.setText("发放授权");
            tvPhone.setText(username);
            tvName.setText(nick);
            setTitleBar("发放授权", true);
        }
//        community = UCUtils.getInstance().getDefCommunity();
//        if(community!=null){
//            tvCommunity.setText(community.title);
//        }
        spinner.setSelection(2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getAdapter().getItem(i);
                tvTime.setText(item);
                selectTime = i;

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        tvCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle =new Bundle();
                if(type == 1) {
                    bundle.putString("username", username);
                }else {
                    bundle.putString("username", UCUtils.getInstance().getAccount());
                }
                qStartActivityForResult(CommunityAuthAct.class, bundle, CommunityAuthAct.REQUEST_CODE_COMMUNITY);
            }
        });
        btnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(btnSend.equals(v)){
            if(type ==1){ // 申请授权
                if(community==null||TextUtils.isEmpty(community.id)){
                    showToast("选择小区");
                    return;
                }
                ApplyAuthParam param = new ApplyAuthParam();
                param.communityId = community.id;
                param.touser = username;
                param.expiretype =selectTime;
                AuthManager.applyAuth(AuthAct.this,param ,0 ,mHandler);
            }else { //授权
                if(community==null||TextUtils.isEmpty(community.id)){
                    showToast("选择小区");
                    return;
                }
                if(type==0 &&!community.hasAuth){
                    showToast("对不起！该小区你没有权限授权");
                    return;
                }
                AuthManager.Auth2Param param = new AuthManager.Auth2Param();
                param.touser = username;
                param.communityId = community.id;
                param.expiretype = selectTime;
                AuthManager.giveAuth2(AuthAct.this, param ,0 ,mHandler);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == CommunityAuthAct.REQUEST_CODE_COMMUNITY){
                community =(Community) data.getExtras().getSerializable(Community.TAG);
                if(community!=null){
                   tvCommunity.setText(community.title);
                }
            }
        }
    }

    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        boolean parentExecuted = super.onMsgSearchComplete(param);
        if (parentExecuted) {
            // 父类已经处理了
            return true;
        }
        switch (param.key) {
            case GIVE_AUTH2:
                if (param.result.bstatus.code == 0) {
                    AuthResult authResult =(AuthResult)param.result;
                    AuthInfo authInfo = new AuthInfo();
                    authInfo.authId = authResult.data.id;
                    authInfo.authType = 0;
                    authInfo.communit=community.title;
                    authInfo.sendName = UCUtils.getInstance().getUsername() ;
                    authInfo.acceptName = nick;
                    authInfo.startTime = System.currentTimeMillis();
                    authInfo.effTime = tiems[selectTime];
                    authInfo.expiretype =  selectTime ;
                    authInfo.strTime = tvTime.getText().toString();
                    Bundle bundle =   new Bundle();
                    bundle.putSerializable(AuthInfo.TAG, authInfo);
                    qBackForResult(RESULT_OK, bundle);
                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            case APPLY_AUTH:
                if (param.result.bstatus.code == 0) {
                    AuthResult authResult =(AuthResult)param.result;
                    AuthInfo authInfo =new AuthInfo();
                    authInfo.authId = authResult.data.id;
                    authInfo.authType = 1;
                    authInfo.communit=community.title;
                    authInfo.sendName = nick;
                    authInfo.acceptName = UCUtils.getInstance().getUsername();
                    authInfo.startTime = System.currentTimeMillis();
                    authInfo.effTime = tiems[selectTime];
                    authInfo.expiretype =  selectTime ;
                    authInfo.strTime = tvTime.getText().toString();
                    Bundle bundle =   new Bundle();
                    bundle.putSerializable(AuthInfo.TAG, authInfo);
                    qBackForResult(RESULT_OK, bundle);
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
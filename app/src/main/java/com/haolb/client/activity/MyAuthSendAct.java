package com.haolb.client.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.haolb.client.R;
import com.haolb.client.adapter.utils.QSimpleAdapter;
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.domain.response.AuthListResult;
import com.haolb.client.manager.AuthManager;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.DateTimeUtils;
import com.haolb.client.utils.inject.From;

import java.util.Calendar;

import static com.haolb.client.domain.response.AuthListResult.MyAuthInfo;
import static com.haolb.client.manager.AuthManager.AuthParam;
import static com.haolb.client.manager.AuthManager.CancelAuthParam;

public class MyAuthSendAct extends SwipeBackActivity {
    @From(R.id.list)
    private ListView list;

    private MyAuthListAdp adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_auth_list);
        setTitleBar("管理授权", true ,"发送授权", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qStartActivity(AuthListAct.class);
            }
        });
        adapter = new MyAuthListAdp(this);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        Request.startRequest(new BaseParam(), ServiceMap.GET_AUTHS, mHandler, Request.RequestFeature.BLOCK, Request.RequestFeature.CANCELABLE);
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final MyAuthInfo authInfo = (MyAuthInfo) parent.getAdapter().getItem(position);
        if (authInfo.status == 1) { //未过期取消授权
            CancelAuthParam cancelAuthParam = new CancelAuthParam();
            cancelAuthParam.applyAuthId = authInfo.id;
            AuthManager.cancelAuth(this, cancelAuthParam, position, mHandler);
        }else if (authInfo.status == 0) { //授权
            AuthParam authParam = new AuthParam();
            authParam.id = authInfo.id;
            authParam.expiretype = authInfo.expiretype;
            AuthManager.giveAuth(this, authParam, position, mHandler);
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
            case GET_AUTHS:
                if (param.result.bstatus.code == 0) {
                    adapter.notifyDataSetChanged();
                    AuthListResult authListResult = (AuthListResult) param.result;
                    adapter.setData(authListResult.data.applys);
                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            case CANCEL_AUTH:
                if (param.result.bstatus.code == 0) {
                    int position = (Integer) param.ext;
                    MyAuthInfo authInfo =   adapter.getItem(position);
                    authInfo.status = 2;
                    adapter.notifyDataSetChanged();
                }
                showToast(param.result.bstatus.des);
                break;
            case GIVE_AUTH:
                if (param.result.bstatus.code == 0) {
                    int position = (Integer) param.ext;
                    MyAuthInfo authInfo = adapter.getItem(position);
                    authInfo.status = 1;
                    adapter.notifyDataSetChanged();
                }
                showToast(param.result.bstatus.des);
                break;
            default:
                break;
        }
        return false;
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
            try {
                Calendar endCalendar = DateTimeUtils.getCalendar(item.endtime);
                Calendar curCalendar = DateTimeUtils.getCalendar(System.currentTimeMillis());

                if(endCalendar.before(curCalendar)){
                    tvEff.setText("开始:"+item.authtime+"\n周期:"+arr[item.expiretype]);
//                    tvEff.setText("已到期");
                }else{
                    tvEff.setText(item.endtime +"到期");
                }

            }catch (Exception e){
                tvEff.setText("有效时间:"+arr[item.expiretype]);
            }
//            if (item.status==1) {
//
//            } else if (item.status==2) {
//                tvEff.setText("已取消授权");
//            }else {
//                tvEff.setText("授权申请中");
//            }
            if (item.status== 1) {
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
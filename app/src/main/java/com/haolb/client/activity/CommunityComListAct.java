package com.haolb.client.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.haolb.client.R;
import com.haolb.client.adapter.utils.QSimpleAdapter;
import com.haolb.client.domain.Community;
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.domain.param.CommunityParam;
import com.haolb.client.domain.param.SetDefCommunityParam;
import com.haolb.client.domain.response.CommunityListResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.UCUtils;
import com.haolb.client.utils.inject.From;

import java.util.List;

import static com.haolb.client.net.Request.RequestFeature.BLOCK;
import static com.haolb.client.net.Request.RequestFeature.CANCELABLE;

public class CommunityComListAct extends SwipeBackActivity {
    @From(R.id.list)
    private ListView list;
    @From(R.id.ll_add)
    private LinearLayout llAdd;
    private Adapter adpter;
    private List<Community> communityList;
    boolean isAddDef ;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com);
        isAddDef = myBundle.getBoolean("isAddDef",false);

        setTitleBar("常用小区", true );
        adpter = new Adapter(this);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);
        list.setAdapter(adpter);
        llAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

            Bundle bundle =new Bundle();
            bundle.putInt("auth", 0);
            qStartActivityForResult(CommunityListAct.class, bundle, CommunityListAct.REQUEST_CODE_COMMUNITY);

            }
        });
//        communityList = UCUtils.getInstance().getCommunitys();
//        adpter.setData(communityList);

        getCommunity();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Community community = (Community) intent.getSerializableExtra(Community.TAG);
        if (community != null) {
            communityList=   UCUtils.getInstance().getCommunitys();
            adpter.setData(communityList);
            if(!isAddDef){
                Bundle bundle = new Bundle();
                bundle.putSerializable(Community.TAG, community);
                qBackForResult(RESULT_OK, bundle);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CommunityListAct.REQUEST_CODE_COMMUNITY) {
                Community community = (Community) data.getExtras().getSerializable(Community.TAG);
                if (community != null) {
                    communityList=   UCUtils.getInstance().getCommunitys();
                    adpter.setData(communityList);
                    if(!isAddDef){
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Community.TAG, community);
                        qBackForResult(RESULT_OK, bundle);
                    }
                }
            }
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

//        final Community community = (Community) parent.getAdapter().getItem(position);
//        if(isAddDef&&!community.isdefault){
//            new AlertDialog.Builder(this).setTitle("提示").setMessage("将"+community.title+"设置为默认小区")
//                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//                            addDefCommunity(community);
//                        }
//                    }).setNegativeButton("取消", null).show();
//
//            return;
//        }
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(Community.TAG, community);
//        qBackForResult(RESULT_OK, bundle);
    }

    public void addDefCommunity(Community community){
        SetDefCommunityParam param = new SetDefCommunityParam();
        param.communityId = community.id ;
        Request.startRequest(param ,community, ServiceMap.SETDEFAULTCOMMUNITY, mHandler, BLOCK,CANCELABLE);
    }
    public void getCommunity( ){
        Request.startRequest(new BaseParam(), ServiceMap.GET_MY_COMMUNITY, mHandler,BLOCK,CANCELABLE);
    }
    public void delCommunity(Community community){
        CommunityParam deleteCommunityParam = new CommunityParam();
        deleteCommunityParam.communityId = community.id ;
        Request.startRequest(deleteCommunityParam ,community, ServiceMap.DEL_COMMUNITY, mHandler, "删除中...", BLOCK,CANCELABLE);
    }
    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {

        new AlertDialog.Builder(this).setTitle("提示").setMessage("确定删除？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Community item = (Community)parent.getAdapter().getItem(position);
                delCommunity(item);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
        return true;
    }

    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        boolean parentExecuted = super.onMsgSearchComplete(param);
        if (parentExecuted) {
            // 父类已经处理了
            return true;
        }
        switch (param.key) {
            
            case GET_MY_COMMUNITY:
                if (param.result.bstatus.code == 0) {
                    CommunityListResult communityListResult =(CommunityListResult) param.result;
                    UCUtils.getInstance().saveCommunityList(communityListResult.data.messages);
                    adpter.setData(communityListResult.data.messages);
                }else {
                    showToast(param.result.bstatus.des);
                }

                break;
            case SETDEFAULTCOMMUNITY:
                if (param.result.bstatus.code == 0) {
                    Community community =(Community)param.ext;
                    community.isdefault = true;
                    UCUtils.getInstance().saveCommunity(community);
                }
                List<Community> communities = UCUtils.getInstance().getCommunitys();
                adpter.setData(communities);
                showToast(param.result.bstatus.des);
                break;
            case DEL_COMMUNITY:
                if (param.result.bstatus.code == 0) {
                    Community community =(Community)param.ext;
                    adpter.remove(community);
                    UCUtils.getInstance().delCommunity(community);
                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            default:
                break;
        }
        return false;
    }
    public class Adapter extends QSimpleAdapter<Community> {

        public Adapter(Context context) {
            super(context);
        }
        @Override
        protected View newView(Context context, ViewGroup parent) {
            return inflate(R.layout.activity_com_item, null, false);
        }

        @Override
        protected void bindView(View view, Context context, Community item,
                                int position) {
            ImageView iamgeview = (ImageView) view.findViewById(R.id.iamge_defcommunity);
            TextView text1 = (TextView) view.findViewById(R.id.text1);
            TextView text2 = (TextView) view.findViewById(R.id.text2);
            TextView text3 = (TextView) view.findViewById(R.id.text3);
            StringBuffer stringBuffer = new StringBuffer(item.title);
            if(!TextUtils.isEmpty(item.building)){
                stringBuffer.append("-").append(item.building);
            }
            if(!TextUtils.isEmpty(item.lou)){
                stringBuffer.append("-").append(item.lou);
            }
            if(!TextUtils.isEmpty(item.floor)){
                stringBuffer.append("-").append(item.floor);
            }
            if(!TextUtils.isEmpty(item.number)){
                stringBuffer.append("-").append(item.number);
            }
            text1.setText(stringBuffer.toString());
            text2.setText(item.address);
            if(item.hasAuth){
                text3.setText("已认证");
            }else {
                text3.setText("未认证");
            }
//            if(item.isdefault){
//                iamgeview.setVisibility(View.VISIBLE);
//            }else {
                iamgeview.setVisibility(View.GONE);
//            }
        }

    }
}
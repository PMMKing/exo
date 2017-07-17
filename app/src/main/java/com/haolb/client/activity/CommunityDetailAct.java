package com.haolb.client.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.haolb.client.R;
import com.haolb.client.adapter.utils.QSimpleAdapter;
import com.haolb.client.domain.ChosecommunityParam;
import com.haolb.client.domain.Community;
import com.haolb.client.domain.param.CommunityBuildParam;
import com.haolb.client.domain.response.CommunityDetailResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.UCUtils;
import com.haolb.client.utils.inject.From;

import static com.haolb.client.domain.response.CommunityDetailResult.CommunityItem;
import static com.haolb.client.net.Request.RequestFeature.BLOCK;
import static com.haolb.client.net.Request.RequestFeature.CANCELABLE;

public class CommunityDetailAct extends SwipeBackActivity {
    @From(R.id.list)
    private ListView list;
    @From(R.id.tv_guide)
    private TextView tvGuide;
    private Adapter adapter;
    private CommunityDetailResult communityResult;
    private Community community;
    private CommunityItem communityItem;
    private ChosecommunityParam choseParam;
    private int auth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_detail);
        setTitleBar("设置小区" ,false);
        list.setOnItemClickListener(this);
        community = (Community) myBundle.getSerializable(Community.TAG);
        choseParam = (ChosecommunityParam) myBundle.getSerializable(ChosecommunityParam.TAG);
        community = (Community) myBundle.getSerializable(Community.TAG);
        communityItem = (CommunityItem) myBundle.getSerializable(CommunityItem.TAG);
        int type =   myBundle.getInt("type",-1);
          auth =   myBundle.getInt("auth",0);
        adapter = new Adapter(this);
        if(choseParam==null){
            choseParam = new ChosecommunityParam();
        }
        list.setAdapter(adapter);
        if(community!=null&&type==-1){
            choseParam.communityId=community.id;
            getCommunityItem(choseParam.communityId ,0);
        }else {
            getCommunityItem(communityItem.id ,type+1);
        }


        tvGuide.setText(community.toString());
    }

    public void addCommunity(ChosecommunityParam choseParam) {
        Request.startRequest(choseParam,community, ServiceMap.CHOOSE_COMMUNITY, mHandler, "保存至常用小区中...", BLOCK ,CANCELABLE);
    }

    public void getCommunityItem(String pId ,int type) {
        CommunityBuildParam param = new CommunityBuildParam();
        param.pid = pId;
        param.type = type;
        Request.startRequest(param, ServiceMap.GET_COMMUNITY_ITEM, mHandler,  BLOCK ,CANCELABLE);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CommunityItem communityItem = (CommunityItem) parent.getAdapter().getItem(position);
        if (communityResult.data.type == 0) {
            choseParam.buildingId=communityItem.id;
            community.building = communityItem.name;
        } else if (communityResult.data.type == 1) {
            choseParam.louId=communityItem.id;
            community.lou = communityItem.name;
        } else if (communityResult.data.type == 2) {
            choseParam.floorId=communityItem.id;
            community.floor = communityItem.name;
        } else if (communityResult.data.type == 3) {
            choseParam.numberId=communityItem.id;
            community.number = communityItem.name;
            if(auth==1){
                Bundle bundle = new Bundle();
                bundle.putSerializable(Community.TAG, community);
//                qStartActivity(CommunityComListAct.class ,bundle);
                qBackForResult(RESULT_OK, bundle);
            }else {
                addCommunity(choseParam);
            }
            return;
        }
        Bundle bundle =new Bundle();
        bundle.putSerializable(communityItem.TAG ,communityItem);
        bundle.putSerializable(ChosecommunityParam.TAG ,choseParam);
        bundle.putSerializable(Community.TAG ,community);
        bundle.putInt("type" ,communityResult.data.type);
        bundle.putInt("auth" ,auth);
        qStartActivityForResult(CommunityDetailAct.class, bundle, CommunityListAct.REQUEST_CODE_COMMUNITY);
//        qStartActivity(CommunityDetailAct.class ,bundle);
//        getCommunityItem(communityItem.id ,communityResul
// t.data.type+1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CommunityListAct.REQUEST_CODE_COMMUNITY) {
                Community community = (Community) data.getExtras().getSerializable(Community.TAG);
                if (community != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Community.TAG, community);
                    qBackForResult(RESULT_OK, bundle);
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
            case GET_COMMUNITY_ITEM:
                if (param.result.bstatus.code == 0) {
                    communityResult = (CommunityDetailResult) param.result;
                    setTitleBar(communityResult.data.getTitleByType(), true);
                    adapter.setData(communityResult.data.buildings);
                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            case CHOOSE_COMMUNITY:
                if (param.result.bstatus.code == 0) {
                     Community community1 =(Community) param.ext;
//                    CommunityResult communityResult = (CommunityResult) param.result;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Community.TAG, community1);
                    community1.isdefault = true;
                    UCUtils.getInstance().saveCommunity(community1);
//                    qStartActivity(CommunityComListAct.class ,bundle);
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

    public class Adapter extends QSimpleAdapter<CommunityItem> {


        public Adapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(Context context, ViewGroup parent) {
            return inflate(R.layout.activity_com_select_item, null, false);
        }

        @Override
        protected void bindView(View view, Context context, CommunityItem item, int position) {
            TextView text1 = (TextView) view.findViewById(R.id.text1);
            text1.setText(item.name);
        }

    }
}
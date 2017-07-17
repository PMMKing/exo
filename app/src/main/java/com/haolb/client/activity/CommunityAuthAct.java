package com.haolb.client.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.haolb.client.R;
import com.haolb.client.adapter.utils.QSimpleAdapter;
import com.haolb.client.domain.Community;
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.domain.response.CommunityListResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.inject.From;

import static com.haolb.client.net.Request.RequestFeature.BLOCK;
import static com.haolb.client.net.Request.RequestFeature.CANCELABLE;

public class CommunityAuthAct extends SwipeBackActivity {
    public static final int REQUEST_CODE_COMMUNITY = 1;
    @From(R.id.list)
    private ListView list;
    @From(R.id.query)
    private EditText query;
    @From(R.id.search_clear)
    private ImageButton clearSearch;
    private Adapter adapter;
    private String username;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_select);

        adapter = new Adapter(this);
        username =   myBundle.getString("username");
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        query.setHint(R.string.search);
        query.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                hideSoftInput();
            }
        });
        setTitleBar("选择小区", true);
        ComParam comParam =new ComParam();
        comParam.phone = username;
        Request.startRequest(comParam, ServiceMap.GETAUTHCOMMUNITIES, mHandler, BLOCK, CANCELABLE);
       }


    public class ComParam extends BaseParam{
        public String phone ;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Community community = (Community) parent.getAdapter().getItem(position);
        if (community != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Community.TAG, community);
            qBackForResult(RESULT_OK, bundle);
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
            case GETAUTHCOMMUNITIES:
                if (param.result.bstatus.code == 0) {
                    if (param.result.bstatus.code == 0) {
                        CommunityListResult communityListResult = (CommunityListResult) param.result;
                        adapter.setData(communityListResult.data.messages);
                    } else {
                        showToast(param.result.bstatus.des);
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

    public class Adapter extends QSimpleAdapter<Community> {

        public Adapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(Context context, ViewGroup parent) {
            return inflate(R.layout.activity_com_select_item, null, false);
        }

        @Override
        protected void bindView(View view, Context context, Community item,
                                int position) {
            TextView text1 = (TextView) view.findViewById(R.id.text1);
            text1.setText(item.title);
        }

    }
}
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
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.domain.response.CityListResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.UCUtils;
import com.haolb.client.utils.inject.From;

import static com.haolb.client.domain.response.CityListResult.City;
import static com.haolb.client.net.Request.RequestFeature.BLOCK;
import static com.haolb.client.net.Request.RequestFeature.CANCELABLE;

public class CityListAct extends SwipeBackActivity {
    @From(R.id.list)
    private ListView list;
    @From(R.id.query)
    private EditText query;
    @From(R.id.search_clear)
    private ImageButton clearSearch;
    private Adapter adapter;
    private CityListResult communitieResult;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        setTitleBar("城市选择", true);
        adapter = new   Adapter(this );
        list.setOnItemClickListener(this);
        list.setAdapter(adapter);
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

        Request.startRequest(new BaseParam(), ServiceMap.GET_CITYS, mHandler, BLOCK, CANCELABLE);

    }

    public static class CityParam extends BaseParam{
        private static final long serialVersionUID = 1L;
        public String cityId;
        public String cityname;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        City city = (City) parent.getAdapter().getItem( position);
        CityParam cityParam =new CityParam();
        cityParam.cityId = city.id;
        cityParam.cityname = city.cityname;
        Request.startRequest(cityParam, city ,ServiceMap.SET_CITY, mHandler, BLOCK, CANCELABLE);
    }


    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        boolean parentExecuted = super.onMsgSearchComplete(param);
        if (parentExecuted) {
            // 父类已经处理了
            return true;
        }
        switch (param.key) {
            case GET_CITYS:
                if (param.result.bstatus.code == 0) {
                    communitieResult = (CityListResult) param.result;
                    adapter.setData(communitieResult.data.citys);
                } else {
                    showToast(param.result.bstatus.des);
                }
              break;
            case SET_CITY:
                if (param.result.bstatus.code == 0) {
                    City city =(City)param.ext;
                    Bundle bundle = new Bundle();
                    UCUtils.getInstance().saveCity(city);
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
    public class Adapter extends QSimpleAdapter<City> {


        public Adapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(Context context, ViewGroup parent) {
            return inflate(R.layout.activity_com_select_item, null, false);
        }

        @Override
        protected void bindView(View view, Context context, City item, int position) {
            TextView text1 = (TextView) view.findViewById(R.id.text1);
            text1.setText(item.cityname);
        }

    }

}
/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haolb.client.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.easemob.Constant;
import com.haolb.client.R;
import com.haolb.client.adapter.MessageAdp;
import com.haolb.client.app.MainApplication;
import com.haolb.client.domain.response.MessageResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.utils.inject.From;

import java.util.ArrayList;
import java.util.List;

import static com.haolb.client.domain.response.MessageResult.Message;

/**
 * 小区
 *
 * @author Administrator
 */
public class MessageFragment extends BaseFragment {
    @From(R.id.list)
    public ListView listView;
    private ArrayList<Message> messages;
    private MessageAdp messageAdp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateViewWithTitleBar(inflater, container, R.layout.activity_msg);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleBar("好乐帮", false);
        listView.setOnItemClickListener(this);
        messageAdp = new MessageAdp(getActivity());
        listView.setAdapter(messageAdp);
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        MainApplication.getInstance().setMessages(null);
        super.onItemClick(parent, view, position, id);
       final Message message =(Message)parent.getAdapter().getItem(position);
        message.isnew =false;
        Bundle  bundle = new Bundle();
        bundle.putSerializable(Message.TAG ,message);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                MainApplication.getInstance().updateMessage(message);
//            }
//        }).start();
        messageAdp.notifyDataSetChanged();
        qStartActivity(MessageDetailAct.class ,bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (((MainActivity2) getActivity()).isConflict) {
            outState.putBoolean("isConflict", true);
        } else if (((MainActivity2) getActivity()).getCurrentAccountRemoved()) {
            outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
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
            case GET_MESSAGE:
                if (param.result.bstatus.code == 0) {
                    MessageResult messageResult =(MessageResult) param.result;
                    messageAdp.setData(messageResult.data.messages);
                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            default:
                break;
        }
        return false;
    }

    public void refresh() {
        List<Message> messages = MainApplication.getInstance().getMessage();
        messageAdp.setData( messages);
    }
}

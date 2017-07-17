package com.haolb.client.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.utils.UserUtils;
import com.haolb.client.R;
import com.haolb.client.adapter.HistoryAdapter;
import com.haolb.client.app.MainApplication;
import com.haolb.client.domain.Community;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.inject.From;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class AuthListAct extends SwipeBackActivity {
    @From(R.id.list)
    private ListView list;
    @From(R.id.query)
    private EditText query;
    @From(R.id.search_clear)
    private ImageButton clearSearch;
    @From(R.id.ll_contact)
    private LinearLayout llContact;
    private HistoryAdapter adapter;
    private List<EMConversation> conversationList = new ArrayList<EMConversation>();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_list);
        setTitleBar("管理授权", true);
        conversationList.addAll(loadConversationsWithRecentChat());
        adapter = new  HistoryAdapter(this, 1, conversationList);
        list.setAdapter(adapter);
        query.setHint(R.string.search);
        query.setInputType(InputType.TYPE_CLASS_PHONE);
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qStartActivity(SearchContactListAct.class);
            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                hideSoftInput();
            }
        });

        llContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qStartActivity(ContactListAct.class);
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EMConversation conversation = adapter.getItem(position);
                String username = conversation.getUserName();
                final String st2 = getResources().getString(R.string.Cant_chat_with_yourself);
                if (username.equals(MainApplication.getInstance().getUserName()))
                    showToast(st2);
                else {
                    // 进入聊天页面
                    Intent intent = new Intent(AuthListAct.this, ChatActivity.class);
                    intent.putExtra("userId", username);
                    intent.putExtra("nickname", UserUtils.getUserInfo(username).getNick());
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        refresh();
        super.onResume();
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        conversationList.clear();
        conversationList.addAll(loadConversationsWithRecentChat());
        if(adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Community addressItem = (Community) parent.getAdapter().getItem(
                position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("AddressItem", addressItem);
        qBackForResult(RESULT_OK, bundle);
    }
    /**
     * 获取所有会话
     *
     * @return
    +	 */
    private List<EMConversation> loadConversationsWithRecentChat() {
        // 获取所有会话，包括陌生人
        Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
        // 过滤掉messages size为0的conversation
        /**
         * 如果在排序过程中有新消息收到，lastMsgTime会发生变化
         * 影响排序过程，Collection.sort会产生异常
         * 保证Conversation在Sort过程中最后一条消息的时间不变
         * 避免并发问题
         */
        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    //if(conversation.getType() != EMConversationType.ChatRoom){
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
                    //}
                }
            }
        }
        try {
            // Internal is TimSort algorithm, has bug
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<EMConversation> list = new ArrayList<EMConversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
    }
    /**
     * 根据最后一条消息的时间排序
     *
     */
    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {

                if (con1.first == con2.first) {
                    return 0;
                } else if (con2.first > con1.first) {
                    return 1;
                } else {
                    return -1;
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
            case DEL_COMMUNITY:
                if (param.result.bstatus.code == 0) {
                    // communitieResult = (CommunitieResult) param.result;
                    // adpter.setData(communitieResult.data.messages);
                } else {
                    showToast(param.result.bstatus.des);
                }
            default:
                break;
        }
        return false;
    }

}
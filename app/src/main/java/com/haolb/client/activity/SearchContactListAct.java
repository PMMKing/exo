package com.haolb.client.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.db.UserDao;
import com.easemob.domain.User;
import com.easemob.util.HanziToPinyin;
import com.haolb.client.R;
import com.haolb.client.adapter.utils.QSimpleAdapter;
import com.haolb.client.app.MainApplication;
import com.haolb.client.domain.param.GetContactParam;
import com.haolb.client.domain.response.ContactListResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.swipeback.SwipeBackActivity;
import com.haolb.client.utils.BusinessUtils;
import com.haolb.client.utils.MUtil;
import com.haolb.client.utils.QArrays;
import com.haolb.client.utils.cache.ImageLoader;
import com.haolb.client.utils.inject.From;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.haolb.client.net.Request.RequestFeature.BLOCK;
import static com.haolb.client.net.Request.RequestFeature.CANCELABLE;


public class SearchContactListAct extends SwipeBackActivity {
    @From(R.id.list)
    private ListView listView;
    @From(R.id.query)
    private EditText query;
    @From(R.id.search)
    private Button search;
    @From(R.id.search_clear)
    private ImageButton clearSearch;
    private ContactAdp adapter;
    private List<User> userInfos;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        setTitleBar("搜索好友", true);
        adapter = new ContactAdp(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        query.setHint(R.string.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = query.getText().toString();
                if(BusinessUtils.checkPhoneNumber(phone)){
                    List<String> phoneList = new ArrayList<String>();
                    phoneList.add(phone);
                    checkContact2Server(phoneList);
                }
            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                hideSoftInput();
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏软键盘
                if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                    if (getCurrentFocus() != null)
                        hideSoftInput();
                }
                return false;
            }
        });


        userInfos = MUtil.getPhoneContacts(this);


    }

    public void checkContact2Server(List<String> phoneList) {
        GetContactParam param = new GetContactParam();
        param.phoneList = phoneList;
        Request.startRequest(param, ServiceMap.GET_CONTACT, mHandler, BLOCK, CANCELABLE);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User user = (User) parent.getAdapter().getItem(position);
        if (user.getStatus() != 1) {
            shareFriends(user, this, (Button) view.findViewById(R.id.btn_fuck));
            return;
        }
        final String st2 = getResources().getString(R.string.Cant_chat_with_yourself);
        if (user.getUsername().equals(MainApplication.getInstance().getUserName()))
            showToast(st2);
        else {
            // 进入聊天页面
            Bundle bundle = new Bundle();
            bundle.putString("userId", user.getUsername());
            bundle.putString("nickname", user.getNick());
            qStartActivity(ChatActivity.class, bundle);
            finish();
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
            case GET_CONTACT:
                if (param.result.bstatus.code == 0) {
                    ContactListResult contactListResult = (ContactListResult) param.result;
                    List<User> contacts = contactListResult.data.contacts;


                    if (!QArrays.isEmpty(contacts)) {
                        Map<String, User> userlist = new HashMap<String, User>();
                        for (User user : contacts) {
                            userlist.put(user.getUsername(), user);
                            if (!QArrays.isEmpty(userInfos)) {
                                for (User user1 : userInfos) {
                                    if (user.equals(user1)) {
                                        user.setContactName(user1.getContactName());
                                    }
                                }
                            }
                        }
                        // 存入内存
                        MainApplication.getInstance().setContactList(userlist);

                        // 存入db
                        UserDao dao = new UserDao(SearchContactListAct.this);
                        List<User> users = new ArrayList<User>(MainApplication.getInstance().getContactList().values());
                        dao.saveContactList(users);
                    }


                    adapter.setData(contacts);
                } else

                {
                    showToast(param.result.bstatus.des);
                }

                break;
            default:
                break;
        }
        return false;
    }

    public static class ViewHolder {
        ImageView avatar;
        TextView tvCommunity;
        TextView tvName;
        TextView tvHeader;
        Button btnFuck;
    }

    public class ContactAdp extends QSimpleAdapter<User> implements SectionIndexer {
        private static final String TAG = "ContactAdp";
        List<String> list;
        private SparseIntArray positionOfSection;
        private SparseIntArray sectionOfPosition;

        public ContactAdp(Context context) {
            super(context);
        }

        @Override
        protected View newView(Context context, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            View convertView = inflate(R.layout.activity_contact_item, parent, false);
            holder.avatar = (ImageView) convertView.findViewById(R.id.image_header);
            holder.tvCommunity = (TextView) convertView.findViewById(R.id.tv_community);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tvHeader = (TextView) convertView.findViewById(R.id.header);
            holder.btnFuck = (Button) convertView.findViewById(R.id.btn_fuck);
            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected void bindView(View view, final Context context, final User user, int position) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            String header = user.getHeader();
            if (position == 0 || !TextUtils.isEmpty(header) && !header.equals(getItem(position - 1).getHeader())) {
                if ("".equals(header)) {
                    holder.tvHeader.setVisibility(View.GONE);
                } else {
                    holder.tvHeader.setVisibility(View.VISIBLE);
                    holder.tvHeader.setText(header);
                }
            } else {
                holder.tvHeader.setVisibility(View.GONE);
            }
            if (TextUtils.isEmpty(user.getNick())) {
                holder.tvName.setText(user.getContactName());
            } else {
                holder.tvName.setText(user.getNick());
            }
            holder.tvCommunity.setText(user.getUsername());
            if (user.getStatus() == 1) {
                holder.btnFuck.setVisibility(View.GONE);
            } else if (user.getStatus() == 0) {
                holder.btnFuck.setVisibility(View.VISIBLE);
                holder.btnFuck.setText("邀请");
            } else {
                holder.btnFuck.setVisibility(View.VISIBLE);
                holder.btnFuck.setText("已邀请");
            }
            holder.btnFuck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareFriends(user, context, holder.btnFuck);
                }
            });
            //设置用户头像
            ImageLoader.getInstance(context).loadImage(user.getAvatar(), holder.avatar, R.drawable.default_avatar);

        }

        public int getPositionForSection(int section) {
            return positionOfSection.get(section);
        }

        public int getSectionForPosition(int position) {
            return sectionOfPosition.get(position);
        }

        @Override
        public void setData(List<User> notes) {
            if(QArrays.isEmpty(notes)){
                return;
            }
            for (User user : notes) {
                String nick = TextUtils.isEmpty(user.getNick()) ? user.getContactName() : user.getNick();
                user.setHeader(HanziToPinyin.getInstance().get(nick.substring(0, 1)).get(0).target.substring(0, 1).toUpperCase());
                char header = user.getHeader().toLowerCase().charAt(0);
                if (header < 'a' || header > 'z') {
                    user.setHeader("#");
                }

            }
            Collections.sort(notes, new Comparator<User>() {

                @Override
                public int compare(User lhs, User rhs) {
                    return lhs.getHeader().compareTo(rhs.getHeader());
                }
            });
            super.setData(notes);
        }

        @Override
        public Object[] getSections() {
            positionOfSection = new SparseIntArray();
            sectionOfPosition = new SparseIntArray();
            int count = getCount();
            list = new ArrayList<String>();
            list.add(getContext().getString(R.string.search_header));
            positionOfSection.put(0, 0);
            sectionOfPosition.put(0, 0);
            for (int i = 1; i < count; i++) {
                String letter = getItem(i).getHeader();
                int section = list.size() - 1;
                if (list.get(section) != null && !list.get(section).equals(letter)) {
                    list.add(letter);
                    section++;
                    positionOfSection.put(section, i);
                }
                sectionOfPosition.put(i, section);
            }
            return list.toArray(new String[list.size()]);
        }
    }

    private void shareFriends(final User user, final Context context, final Button btnFuck) {
        if (user.getStatus() == 0) {
            final String sms_content = context.getString(R.string.share_content);
            new AlertDialog.Builder(this).setTitle("发送短信").setMessage(sms_content).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ;
                    String phone_number = user.getUsername();
                    if (!BusinessUtils.checkPhoneNumber(phone_number)) {
                        Toast.makeText(SearchContactListAct.this, "联系人号码有误，确认后再发", Toast.LENGTH_LONG).show();
                    } else {
                        SmsManager smsManager = SmsManager.getDefault();
                        if (sms_content.length() > 70) {
                            List<String> contents = smsManager.divideMessage(sms_content);
                            for (String sms : contents) {
                                smsManager.sendTextMessage(phone_number, null, sms, null, null);
                            }
                        } else {
                            smsManager.sendTextMessage(phone_number, null, sms_content, null, null);
                        }
                        btnFuck.setVisibility(View.VISIBLE);
                        btnFuck.setText("已邀请");
                        user.setStatus(2);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(SearchContactListAct.this, "已发送邀请", Toast.LENGTH_LONG).show();
                    }
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();

        }
    }
}
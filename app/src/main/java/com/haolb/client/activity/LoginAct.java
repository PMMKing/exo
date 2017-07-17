package com.haolb.client.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.Constant;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.db.UserDao;
import com.easemob.domain.User;
import com.easemob.util.HanziToPinyin;
import com.easemob.utils.CommonUtils;
import com.haolb.client.R;
import com.haolb.client.app.MainApplication;
import com.haolb.client.domain.param.GetCodeParam;
import com.haolb.client.domain.param.GetUserInfosParam;
import com.haolb.client.domain.param.LoginParam;
import com.haolb.client.domain.response.BaseResult;
import com.haolb.client.domain.response.ContactListResult;
import com.haolb.client.domain.response.LoginResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.Request.RequestFeature;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.utils.BusinessUtils;
import com.haolb.client.utils.HandlerCallbacks;
import com.haolb.client.utils.QArrays;
import com.haolb.client.utils.UCUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.haolb.client.net.Request.RequestFeature.CANCELABLE;

public class LoginAct extends BaseActivity implements TextWatcher {
	private EditText usernameEditText;
	private EditText codeEditText;
	private Button loginBtn;
	private TextView loginVerifyTv;
    final String msg = "正在登录中..." ;
	private final Handler clockHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == HandlerCallbacks.MESSAGE_CLOCK) {
				if (UCUtils.getInstance().getCurrentClock() > 0
						&& UCUtils.getInstance().getCurrentClock() <= UCUtils.TOTAL_CLOCK) {
					String temp = getString(R.string.uc_sendagain_seconds);
					String repeatBtnStr = String.format(temp, UCUtils
							.getInstance().getCurrentClock());
					loginVerifyTv.setText(repeatBtnStr);
					loginVerifyTv.setEnabled(false);
					sendEmptyMessageDelayed(HandlerCallbacks.MESSAGE_CLOCK,
							1000);
					UCUtils.getInstance().subtractClock();
				} else {
					loginVerifyTv.setText("重新获取");
					loginVerifyTv.setEnabled(true);
					UCUtils.getInstance().initClock();
				}

			}

		}

	};
    private LoginResult loginResult;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		setTitleBar("用户登录", false);
		initView();
	}

	private void initView() {
		usernameEditText = (EditText) findViewById(R.id.username_edit);
		codeEditText = (EditText) findViewById(R.id.password_code);
		loginVerifyTv = (TextView) findViewById(R.id.login_verify_tv);
		loginBtn = (Button) findViewById(R.id.login_btn);
		loginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
//                startActivity(new Intent(LoginAct.this, MainActivity2.class));

				LoginParam loginParam = new LoginParam();
				loginParam.phone = usernameEditText.getText().toString();
				loginParam.code = codeEditText.getText().toString();
				Request.startRequest(loginParam, ServiceMap.LOGIN, mHandler,msg, RequestFeature.BLOCK,
						RequestFeature.CANCELABLE);
			}
		});
		loginVerifyTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String phone = usernameEditText.getText().toString().trim();
				if (!BusinessUtils.checkPhoneNumber(phone)) {
					showToast("请输入正确的手机号");
					return;
				}
				GetCodeParam codeParam = new GetCodeParam();
				codeParam.phone = phone;
				Request.startRequest(codeParam, ServiceMap.GET_VERIFICATION_CODE,
						mHandler, "正在发送中......", RequestFeature.BLOCK,
						RequestFeature.CANCELABLE);
			}
		});
		loginBtn.setEnabled(false);
		usernameEditText.addTextChangedListener(this);
		codeEditText.addTextChangedListener(this);

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

		if (!TextUtils.isEmpty(usernameEditText.getText().toString().trim())
				&& !TextUtils.isEmpty(codeEditText.getText().toString().trim()) ) {
			loginBtn.setEnabled(true);
		} else {
			loginBtn.setEnabled(false);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

    /**
     * 登录
     *
     */
    public void loginEm(final String currentUsername , final String currentPassword) {
        if (!CommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(currentUsername)||TextUtils.isEmpty(currentPassword)) {
            showToast("登录失败！");
            return;
        }
        onShowProgress(msg ,true , new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });
        // 调用sdk登陆方法登陆聊天服务器
        EMChatManager.getInstance().login(currentUsername, currentPassword, new EMCallBack() {

            @Override
            public void onSuccess() {
                // 登陆成功，保存用户名密码
                MainApplication.getInstance().setUserName(currentUsername);
                MainApplication.getInstance().setPassword(currentPassword);

                try {
                    // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
                    // ** manually load all local groups and
                    EMChatManager.getInstance().loadAllConversations();
                    // 处理好友和群组
                    List<String> usernames = getAllConverUsername();
//                    List<String> usernames = EMContactManager.getInstance().getContactUserNames();
                    if(QArrays.isEmpty(usernames)){
                        UCUtils.getInstance().saveCookie(loginResult);
                        // 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
                        boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(UCUtils.getInstance().getUsername());
                        if (!updatenick) {
                            Log.e("LoginActivity", "update current user nick fail");
                        }
                        if (!LoginAct.this.isFinishing())
                            onCloseProgress(msg);
                        // 进入主页面
                        startActivity(new Intent(LoginAct.this, MainActivity2.class));
                        finish();
                        return;
                    }
                    getUserInfos(usernames);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 取好友或者群聊失败，不让进入主页面
                    runOnUiThread(new Runnable() {
                        public void run() {
                            onCloseProgress(msg);
                            MainApplication.getInstance().logout(null);
                            Toast.makeText(getApplicationContext(), R.string.login_failure_failed, Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(final int code, final String message) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        onCloseProgress(msg);
                        showToast(getString(R.string.Login_failed));
                    }
                });
            }
        });
    }

    @Override
    public void onNetError(NetworkParam param, int errCode) {
        super.onNetError(param, errCode);
        onCloseProgress(msg);
        showToast(getString(R.string.Login_failed));
    }

    private void getUserInfos( List<String> usernames){
        GetUserInfosParam userInfosParam =new GetUserInfosParam();
        userInfosParam.phoneList = usernames;
        Request.startRequest(userInfosParam, ServiceMap.GET_CONTACT, mHandler, CANCELABLE);
    }



	@Override
	public boolean onMsgSearchComplete(NetworkParam param) {
		boolean parentExecuted = super.onMsgSearchComplete(param);
		if (parentExecuted) {
			// 父类已经处理了
			return true;
		}
		switch (param.key) {
		case LOGIN:
            if (param.result.bstatus.code == 0) {
                loginResult = (LoginResult) param.result;
                if(loginResult.data==null){
                    showToast("服务器异常!");
                    break;
                }else {
                    UCUtils.getInstance().saveCookie(loginResult);
                    loginEm(loginResult.data.phone , loginResult.data.password);
                }
			} else {
				showToast(param.result.bstatus.des);
			}
			break;
		case GET_VERIFICATION_CODE:
			BaseResult baseResult = (BaseResult) param.result;
			if (baseResult.bstatus.code == 0) {

				clockHandler.sendEmptyMessage(HandlerCallbacks.MESSAGE_CLOCK);
			} else {
				showToast(param.result.bstatus.des);
			}
			break;
		case GET_CONTACT:
            ContactListResult userInfosResult = (ContactListResult) param.result;
			if (userInfosResult.bstatus.code == 0) {
                Map<String, User> userlist = new HashMap<String, User>();
                for (User user : userInfosResult.data.contacts) {
                    setUserHearder(user.getUsername(), user);
                    userlist.put(user.getUsername(), user);
                }
                // 存入内存
                MainApplication.getInstance().setContactList(userlist);
                System.out.println("----------------" + userlist.values().toString());
                // 存入db
                UserDao dao = new UserDao(LoginAct.this);
                List<User> users = new ArrayList<User>(MainApplication.getInstance().getContactList().values());
                dao.saveContactList(users);

                // 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
                boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
                        MainApplication.currentUserNick.trim());
                if (!updatenick) {
                    Log.e("LoginActivity", "update current user nick fail");
                }
                if (!LoginAct.this.isFinishing())
                    onCloseProgress(msg);
                // 进入主页面
                startActivity(new Intent(LoginAct.this, MainActivity2.class));
                finish();

			} else {

				showToast(param.result.bstatus.des);
			}
			break;

		default:
			break;
		}
		return false;
	}
    /**
     * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
     *
     * @param username
     * @param user
     */
    protected void setUserHearder(String username, User user) {
        String headerName = null;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
            user.setHeader("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            user.setHeader("#");
        } else {
            user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1)
                    .toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
        }
    }

    /**
     * 获取所有会话
     *
     * @return
    +	 */
    private List<String> getAllConverUsername() {
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
//            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> list = new ArrayList<String>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second.getUserName());
        }
        return list;
    }
}

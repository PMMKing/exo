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

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.easemob.Constant;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMGroupChangeListener;
import com.easemob.EMNotifierEvent;
import com.easemob.MainHXSDKHelper;
import com.easemob.applib.controller.HXSDKHelper;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMConversation.EMConversationType;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.TextMessageBody;
import com.easemob.db.InviteMessgeDao;
import com.easemob.db.UserDao;
import com.easemob.domain.InviteMessage;
import com.easemob.domain.InviteMessage.InviteMesageStatus;
import com.easemob.domain.User;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;
import com.easemob.utils.CommonUtils;
import com.haolb.client.R;
import com.haolb.client.activity.easemob.ChatAllHistoryFragment;
import com.haolb.client.activity.easemob.ContactlistFragment;
import com.haolb.client.activity.easemob.GroupsActivity;
import com.haolb.client.app.MainApplication;
import com.haolb.client.domain.LockInfoResult;
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.domain.response.CheckVersionResult;
import com.haolb.client.manager.OpenDoorManager;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.utils.UCUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.haolb.client.manager.OpenDoorManager.NearByInfo;

public class MainActivity2 extends BaseActivity implements EMEventListener {

	protected static final String TAG = "MainActivity";
	// 未读消息textview
	private TextView unreadLabel;
	// 未读通讯录textview
	private TextView unreadAddressLable;

	private Button[] mTabs;
	private ContactlistFragment contactListFragment;
	// private ChatHistoryFragment chatHistoryFragment;
	private ChatAllHistoryFragment chatHistoryFragment;
	private MyFragment myFragment;
	private DoorFragment doorFragment;
	private MessageFragment msgFragment;
	private Fragment[] fragments;
	private int index;
	// 当前fragment的index
	private int currentTabIndex;
	// 账号在别处登录
	public boolean isConflict = false;
	// 账号被移除
	private boolean isCurrentAccountRemoved = false;
    private AudioManager audio;
    private int currentVolume;
    private EMChatOptions chatOptions;
    public LocationClient mLocationClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    /**
	 * 检查当前用户是否被删除
	 */
	public boolean getCurrentAccountRemoved() {
		return isCurrentAccountRemoved;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null && savedInstanceState.getBoolean(Constant.ACCOUNT_REMOVED, false)) {
			// 防止被移除后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
			// 三个fragment里加的判断同理
			MainApplication.getInstance().logout(null);
            UCUtils.getInstance().removeCookie();
			finish();
			startActivity(new Intent(this, LoginAct.class));
			return;
		} else if (savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false)) {
			// 防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
			// 三个fragment里加的判断同理
            UCUtils.getInstance().removeCookie();
			finish();
			startActivity(new Intent(this, LoginAct.class));
			return;
		}
		setContentView(R.layout.activity_main);
        audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

		initView();
        MainApplication.getInstance().initMessage();
        MobclickAgent.setDebugMode(true);
//      SDK在统计Fragment时，需要关闭Activity自带的页面统计，
//		然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
        MobclickAgent.openActivityDurationTrack(false);
//		MobclickAgent.setAutoLocation(true);
//		MobclickAgent.setSessionContinueMillis(1000);
        MobclickAgent.updateOnlineConfig(this);

		if (getIntent().getBooleanExtra("conflict", false) && !isConflictDialogShow) {
			showConflictDialog();
		} else if (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
			showAccountRemovedDialog();
		}

		inviteMessgeDao = new InviteMessgeDao(this);
		userDao = new UserDao(this);
		// 这个fragment只显示好友和群组的聊天记录
		// chatHistoryFragment = new ChatHistoryFragment();
		// 显示所有人消息记录的fragment
//		chatHistoryFragment = new ChatAllHistoryFragment();
//		contactListFragment = new ContactlistFragment();
		myFragment = new MyFragment();
        msgFragment = new MessageFragment();
        doorFragment = new DoorFragment();
//		fragments = new Fragment[] { doorFragment, contactListFragment, myFragment};
//        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, doorFragment)
//                .add(R.id.fragment_container, contactListFragment).hide(contactListFragment).show(doorFragment)
//                .commit();
		fragments = new Fragment[] { doorFragment, msgFragment, myFragment};
//		添加显示第一个fragment
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, doorFragment)
				.add(R.id.fragment_container, msgFragment).hide(msgFragment).show(doorFragment)
				.commit();

		// setContactListener监听联系人的变化等
		EMContactManager.getInstance().setContactListener(new MyContactListener());
		// 注册一个监听连接状态的listener
		EMChatManager.getInstance().addConnectionListener(new MyConnectionListener());
		// 注册群聊相关的listener
		EMGroupManager.getInstance().addGroupChangeListener(new MyGroupChangeListener());
		// 通知sdk，UI 已经初始化完毕，注册了相应的receiver和listener, 可以接受broadcast了
		EMChat.getInstance().setAppInited();
        chatOptions = EMChatManager.getInstance().getChatOptions();

        new Thread(new Runnable() {
            @Override
            public void run() {
                chatOptions.setNotificationEnable(false);
                EMChatManager.getInstance().setChatOptions(chatOptions);
                HXSDKHelper.getInstance().getModel().setSettingMsgNotification(false);
            }
        }).start();
        if(TextUtils.isEmpty(UCUtils.getInstance().getUsername())){
            qStartActivity(MyInfo.class);
        }

        Request.startRequest(new BaseParam(), "" ,ServiceMap.CHECK_VERSION, mHandler,  Request.RequestFeature.CANCELABLE);
        mHandler.postDelayed(runnable, 1000);
        timer.schedule(task, 1000*10, 1000*10); // 1s后执行task,经过1s再次执行
//        mLocationClient = new LocationClient( this );
//        mLocationClient.registerLocationListener( myListener );
//        setLocationOption();
//        mLocationClient.start();
    }

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                if(isActive){
                    mHandler.postDelayed(this, 1000*60);
                }else {
                    mHandler.postDelayed(this, 1000*60*60);
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        Request.startRequest(new BaseParam(), ServiceMap.GET_MESSAGE, mHandler);
                    }
                });
                System.out.println("do...");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("exception...");
            }
        }
    };

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                BaseParam param = new BaseParam();
                Request.startRequest(param,ServiceMap.GETAUTHGATES, mHandler);
            }
            super.handleMessage(msg);
        };
    };
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };
	/**
	 * 初始化组件
	 */
	private void initView() {
		unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
		unreadAddressLable = (TextView) findViewById(R.id.unread_address_number);
		mTabs = new Button[3];
		mTabs[0] = (Button) findViewById(R.id.btn_conversation);
		mTabs[1] = (Button) findViewById(R.id.btn_address_list);
		mTabs[2] = (Button) findViewById(R.id.btn_setting);
		// 把第一个tab设为选中状态
		mTabs[0].setSelected(true);

	}

    
	/**
	 * button点击事件
	 * 
	 * @param view
	 */
	public void onTabClicked(View view) {
		switch (view.getId()) {
		case R.id.btn_conversation:
			index = 0;
			break;
		case R.id.btn_address_list:
			index = 1;
//            MainApplication.getInstance().setHasNewMessage(false);
            updateUnreadAddressLable();
            msgFragment.refresh();
			break;
		case R.id.btn_setting:
			index = 2;
			break;
		}
		if (currentTabIndex != index) {
			FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
			trx.hide(fragments[currentTabIndex]);
			if (!fragments[index].isAdded()) {
				trx.add(R.id.fragment_container, fragments[index]);
			}
			trx.show(fragments[index]).commit();
		}
		mTabs[currentTabIndex].setSelected(false);
		// 把当前tab设为选中状态
		mTabs[index].setSelected(true);
		currentTabIndex = index;
        MobclickAgent.onEvent(this,"mTabs"+index);
	}

	/**
	 * 监听事件
     */
	@Override
	public void onEvent(EMNotifierEvent event) {
		switch (event.getEvent()) {
		case EventNewMessage: // 普通消息
		{
			EMMessage message = (EMMessage) event.getData();

			// 提示新消息
			com.easemob.applib.controller.HXSDKHelper.getInstance().getNotifier().onNewMsg(message);

			refreshUI();
			break;
		}

		case EventOfflineMessage: {
			refreshUI();
			break;
		}

		case EventConversationListChanged: {
		    refreshUI();
		    break;
		}
		
		default:
			break;
		}
	}

	private void refreshUI() {
		runOnUiThread(new Runnable() {
			public void run() {
				// 刷新bottom bar消息未读数
				updateUnreadLabel();
				if (currentTabIndex == 0) {
					// 当前页面如果为聊天历史页面，刷新此页面
//					if (chatHistoryFragment != null) {
//						chatHistoryFragment.refresh();
//					}
				}
			}
		});
	}

    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;
           MainApplication.getInstance().lan = location.getLatitude();
           MainApplication.getInstance().lon = location.getLongitude();
           if(MainApplication.getInstance().lon!=0&&MainApplication.getInstance().lan!=0){
               NearByInfo nearByInfo = OpenDoorManager.nearbyLock( MainApplication.getInstance().lon , MainApplication.getInstance().lan );
              String strLoc = "当前 经度："+MainApplication.getInstance().lon
                       +"       纬度:"+MainApplication.getInstance().lan ;
              StringBuffer sb = new StringBuffer(strLoc);
              if(nearByInfo!=null&& nearByInfo.douNearby>=0){
                  sb.append("\n门锁 经度：");
                  sb.append(nearByInfo.lon);
                  sb.append("       纬度:");
                  sb.append(nearByInfo.lan);
                  sb.append("\n您离最近的门锁大约");
                  sb.append(formatString(nearByInfo.douNearby));
              }
              doorFragment.tvGps.setText(sb.toString());

           }else {
               doorFragment.tvGps.setText("定位中...");
           }

        }

        @Override
        public void onReceivePoi(BDLocation arg0) {
        }
    }

    public String formatString (double m){
        if(m<1000){
            return m+"米";
        }else {
            return m/1000+"千米";
        }
    }

    //设置相关参数
    private void setLocationOption(){
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(5000);
        mLocationClient.setLocOption(option);
    }

	@Override
	public void back(View view) {
		super.back(view);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
        mLocationClient.stop();
		if (conflictBuilder != null) {
			conflictBuilder.create().dismiss();
			conflictBuilder = null;
		}

	}

	/**
	 * 刷新未读消息数
	 */
	public void updateUnreadLabel() {
		int count = getUnreadMsgCountTotal();
		if (count > 0) {
			unreadLabel.setText(String.valueOf(count));
//			unreadLabel.setVisibility(View.VISIBLE);
            unreadLabel.setVisibility(View.INVISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
		}
	}

    @Override
    public void updateUnreadAddressLable() {
        super.updateUnreadAddressLable();
        runOnUiThread(new Runnable() {
            public void run() {

                if (MainApplication.getInstance().getHasNewMessage() ) {
                    // 提示新消息
                    unreadAddressLable.setText("");
                    unreadAddressLable.setVisibility(View.VISIBLE);
                } else {
                    unreadAddressLable.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

	/**
	 * 获取未读申请与通知消息
	 * 
	 * @return
	 */
	public int getUnreadAddressCountTotal() {
		int unreadAddressCountTotal = 0;
		if (MainApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME) != null)
			unreadAddressCountTotal = MainApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME)
					.getUnreadMsgCount();
		return unreadAddressCountTotal;
	}

	/**
	 * 获取未读消息数
	 * 
	 * @return
	 */
	public int getUnreadMsgCountTotal() {
		int unreadMsgCountTotal = 0;
		int chatroomUnreadMsgCount = 0;
		unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
		for(EMConversation conversation:EMChatManager.getInstance().getAllConversations().values()){
			if(conversation.getType() == EMConversationType.ChatRoom)
			chatroomUnreadMsgCount=chatroomUnreadMsgCount+conversation.getUnreadMsgCount();
		}
		return unreadMsgCountTotal-chatroomUnreadMsgCount;
	}

	private InviteMessgeDao inviteMessgeDao;
	private UserDao userDao;

	/***
	 * 好友变化listener
	 * 
	 */
	private class MyContactListener implements EMContactListener {

		@Override
		public void onContactAdded(List<String> usernameList) {
			// 保存增加的联系人
			Map<String, User> localUsers = MainApplication.getInstance().getContactList();
			Map<String, User> toAddUsers = new HashMap<String, User>();
			for (String username : usernameList) {
				User user = setUserHead(username);
				// 添加好友时可能会回调added方法两次
				if (!localUsers.containsKey(username)) {
					userDao.saveContact(user);
				}
				toAddUsers.put(username, user);
			}
			localUsers.putAll(toAddUsers);
			// 刷新ui
			if (currentTabIndex == 2)
				contactListFragment.refresh();

		}

		@Override
		public void onContactDeleted(final List<String> usernameList) {
			// 被删除
			Map<String, User> localUsers = MainApplication.getInstance().getContactList();
			for (String username : usernameList) {
				localUsers.remove(username);
				userDao.deleteContact(username);
				inviteMessgeDao.deleteMessage(username);
			}
			runOnUiThread(new Runnable() {
				public void run() {
					// 如果正在与此用户的聊天页面
					String st10 = getResources().getString(R.string.have_you_removed);
					if (ChatActivity.activityInstance != null
							&& usernameList.contains(ChatActivity.activityInstance.getToChatUsername())) {
						Toast.makeText(MainActivity2.this, ChatActivity.activityInstance.getToChatUsername() + st10, Toast.LENGTH_LONG)
								.show();
						ChatActivity.activityInstance.finish();
					}
					updateUnreadLabel();
					// 刷新ui
					contactListFragment.refresh();
//					chatHistoryFragment.refresh();
				}
			});

		}

		@Override
		public void onContactInvited(String username, String reason) {
			// 接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所以客户端不需要重复提醒
			List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();

			for (InviteMessage inviteMessage : msgs) {
				if (inviteMessage.getGroupId() == null && inviteMessage.getFrom().equals(username)) {
					inviteMessgeDao.deleteMessage(username);
				}
			}
			// 自己封装的javabean
			InviteMessage msg = new InviteMessage();
			msg.setFrom(username);
			msg.setTime(System.currentTimeMillis());
			msg.setReason(reason);
			Log.d(TAG, username + "请求加你为好友,reason: " + reason);
			// 设置相应status
			msg.setStatus(InviteMesageStatus.BEINVITEED);
//			notifyNewIviteMessage(msg);

		}

		@Override
		public void onContactAgreed(String username) {
			List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
			for (InviteMessage inviteMessage : msgs) {
				if (inviteMessage.getFrom().equals(username)) {
					return;
				}
			}
			// 自己封装的javabean
			InviteMessage msg = new InviteMessage();
			msg.setFrom(username);
			msg.setTime(System.currentTimeMillis());
			Log.d(TAG, username + "同意了你的好友请求");
			msg.setStatus(InviteMesageStatus.BEAGREED);
//			notifyNewIviteMessage(msg);

		}

		@Override
		public void onContactRefused(String username) {
			// 参考同意，被邀请实现此功能,demo未实现
			Log.d(username, username + "拒绝了你的好友请求");
		}

	}

	/**
	 * 连接监听listener
	 * 
	 */
	private class MyConnectionListener implements EMConnectionListener {

		@Override
		public void onConnected() {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
//					chatHistoryFragment.errorItem.setVisibility(View.GONE);
				}

			});
		}

		@Override
		public void onDisconnected(final int error) {
			final String st1 = getResources().getString(R.string.Less_than_chat_server_connection);
			final String st2 = getResources().getString(R.string.the_current_network);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (error == EMError.USER_REMOVED) {
						// 显示帐号已经被移除
						showAccountRemovedDialog();
					} else if (error == EMError.CONNECTION_CONFLICT) {
						// 显示帐号在其他设备登陆dialog
						showConflictDialog();
					} else {
//						chatHistoryFragment.errorItem.setVisibility(View.VISIBLE);
//						if (NetUtils.hasNetwork(MainActivity2.this))
//							chatHistoryFragment.errorText.setText(st1);
//						else
//							chatHistoryFragment.errorText.setText(st2);

					}
				}

			});
		}
	}

	/**
	 * MyGroupChangeListener
	 */
	private class MyGroupChangeListener implements EMGroupChangeListener {

		@Override
		public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
			boolean hasGroup = false;
			for (EMGroup group : EMGroupManager.getInstance().getAllGroups()) {
				if (group.getGroupId().equals(groupId)) {
					hasGroup = true;
					break;
				}
			}
			if (!hasGroup)
				return;

			// 被邀请
			String st3 = getResources().getString(R.string.Invite_you_to_join_a_group_chat);
			EMMessage msg = EMMessage.createReceiveMessage(Type.TXT);
			msg.setChatType(ChatType.GroupChat);
			msg.setFrom(inviter);
			msg.setTo(groupId);
			msg.setMsgId(UUID.randomUUID().toString());
			msg.addBody(new TextMessageBody(inviter + st3));
			// 保存邀请消息
			EMChatManager.getInstance().saveMessage(msg);
			// 提醒新消息
			com.easemob.applib.controller.HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(msg);

			runOnUiThread(new Runnable() {
				public void run() {
					updateUnreadLabel();
					// 刷新ui
					if (currentTabIndex == 0)
//						chatHistoryFragment.refresh();
					if (CommonUtils.getTopActivity(MainActivity2.this).equals(GroupsActivity.class.getName())) {
						GroupsActivity.instance.onResume();
					}
				}
			});

		}

		@Override
		public void onInvitationAccpted(String groupId, String inviter, String reason) {

		}

		@Override
		public void onInvitationDeclined(String groupId, String invitee, String reason) {

		}

		@Override
		public void onUserRemoved(String groupId, String groupName) {
			// 提示用户被T了，demo省略此步骤
			// 刷新ui
			runOnUiThread(new Runnable() {
				public void run() {
					try {
						updateUnreadLabel();
						if (currentTabIndex == 0)
//							chatHistoryFragment.refresh();
						if (CommonUtils.getTopActivity(MainActivity2.this).equals(GroupsActivity.class.getName())) {
							GroupsActivity.instance.onResume();
						}
					} catch (Exception e) {
						EMLog.e(TAG, "refresh exception " + e.getMessage());
					}
				}
			});
		}

		@Override
		public void onGroupDestroy(String groupId, String groupName) {
			// 群被解散
			// 提示用户群被解散,demo省略
			// 刷新ui
			runOnUiThread(new Runnable() {
				public void run() {
					updateUnreadLabel();
					if (currentTabIndex == 0)
//						chatHistoryFragment.refresh();
					if (CommonUtils.getTopActivity(MainActivity2.this).equals(GroupsActivity.class.getName())) {
						GroupsActivity.instance.onResume();
					}
				}
			});

		}

		@Override
		public void onApplicationReceived(String groupId, String groupName, String applyer, String reason) {
			// 用户申请加入群聊
			InviteMessage msg = new InviteMessage();
			msg.setFrom(applyer);
			msg.setTime(System.currentTimeMillis());
			msg.setGroupId(groupId);
			msg.setGroupName(groupName);
			msg.setReason(reason);
			Log.d(TAG, applyer + " 申请加入群聊：" + groupName);
			msg.setStatus(InviteMesageStatus.BEAPPLYED);
//			notifyNewIviteMessage(msg);
		}

		@Override
		public void onApplicationAccept(String groupId, String groupName, String accepter) {
			String st4 = getResources().getString(R.string.Agreed_to_your_group_chat_application);
			// 加群申请被同意
			EMMessage msg = EMMessage.createReceiveMessage(Type.TXT);
			msg.setChatType(ChatType.GroupChat);
			msg.setFrom(accepter);
			msg.setTo(groupId);
			msg.setMsgId(UUID.randomUUID().toString());
			msg.addBody(new TextMessageBody(accepter + st4));
			// 保存同意消息
			EMChatManager.getInstance().saveMessage(msg);
			// 提醒新消息
			com.easemob.applib.controller.HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(msg);

			runOnUiThread(new Runnable() {
				public void run() {
					updateUnreadLabel();
					// 刷新ui
					if (currentTabIndex == 0)
//						chatHistoryFragment.refresh();
					if (CommonUtils.getTopActivity(MainActivity2.this).equals(GroupsActivity.class.getName())) {
						GroupsActivity.instance.onResume();
					}
				}
			});
		}

		@Override
		public void onApplicationDeclined(String groupId, String groupName, String decliner, String reason) {
			// 加群申请被拒绝，demo未实现
		}

	}

	/**
	 * 保存提示新消息
	 * 
	 * @param msg
	 */
	private void notifyNewIviteMessage(InviteMessage msg) {
		saveInviteMsg(msg);
		// 提示有新消息
		com.easemob.applib.controller.HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(null);

		// 刷新bottom bar消息未读数
		updateUnreadAddressLable();
		// 刷新好友页面ui
		if (currentTabIndex == 1)
			contactListFragment.refresh();
	}

	/**
	 * 保存邀请等msg
	 * 
	 * @param msg
	 */
	private void saveInviteMsg(InviteMessage msg) {
		// 保存msg
		inviteMessgeDao.saveMessage(msg);
		// 未读数加1
		User user = MainApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME);
		if (user.getUnreadMsgCount() == 0)
			user.setUnreadMsgCount(user.getUnreadMsgCount() + 1);
	}

	/**
	 * set head
	 * 
	 * @param username
	 * @return
	 */
	User setUserHead(String username) {
		User user = new User();
		user.setUsername(username);
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
		return user;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isConflict && !isCurrentAccountRemoved) {
			updateUnreadLabel();
			updateUnreadAddressLable();
			EMChatManager.getInstance().activityResumed();
		}

		// unregister this event listener when this activity enters the
		// background
		MainHXSDKHelper sdkHelper = (MainHXSDKHelper) MainHXSDKHelper.getInstance();
		sdkHelper.pushActivity(this);

		// register the event listener when enter the foreground
		EMChatManager.getInstance().registerEventListener(this,
				new EMNotifierEvent.Event[] { EMNotifierEvent.Event.EventNewMessage ,EMNotifierEvent.Event.EventOfflineMessage, EMNotifierEvent.Event.EventConversationListChanged});
        currentVolume =audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }


	@Override
	protected void onStop() {
		EMChatManager.getInstance().unregisterEventListener(this);
		MainHXSDKHelper sdkHelper = (MainHXSDKHelper) MainHXSDKHelper.getInstance();
		sdkHelper.popActivity(this);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,0);
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("isConflict", isConflict);
		outState.putBoolean(Constant.ACCOUNT_REMOVED, isCurrentAccountRemoved);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(false);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private android.app.AlertDialog.Builder conflictBuilder;
	private android.app.AlertDialog.Builder accountRemovedBuilder;
	private boolean isConflictDialogShow;
	private boolean isAccountRemovedDialogShow;

	/**
	 * 显示帐号在别处登录dialog
	 */
	private void showConflictDialog() {
		isConflictDialogShow = true;
		MainApplication.getInstance().logout(null);
		String st = getResources().getString(R.string.Logoff_notification);
		if (!MainActivity2.this.isFinishing()) {
			// clear up global variables
			try {
				if (conflictBuilder == null)
					conflictBuilder = new android.app.AlertDialog.Builder(MainActivity2.this);
				conflictBuilder.setTitle(st);
				conflictBuilder.setMessage(R.string.connect_conflict);
				conflictBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						conflictBuilder = null;
						finish();
						startActivity(new Intent(MainActivity2.this, LoginAct.class));
					}
				});
				conflictBuilder.setCancelable(false);
				conflictBuilder.create().show();
				isConflict = true;
			} catch (Exception e) {
				EMLog.e(TAG, "---------color conflictBuilder error" + e.getMessage());
			}

		}

	}

	/**
	 * 帐号被移除的dialog
	 */
	private void showAccountRemovedDialog() {
		isAccountRemovedDialogShow = true;
		MainApplication.getInstance().logout(null);
		String st5 = getResources().getString(R.string.Remove_the_notification);
		if (!MainActivity2.this.isFinishing()) {
			// clear up global variables
			try {
				if (accountRemovedBuilder == null)
					accountRemovedBuilder = new android.app.AlertDialog.Builder(MainActivity2.this);
				accountRemovedBuilder.setTitle(st5);
				accountRemovedBuilder.setMessage(R.string.em_user_remove);
				accountRemovedBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						accountRemovedBuilder = null;
						finish();
						startActivity(new Intent(MainActivity2.this, LoginAct.class));
					}
				});
				accountRemovedBuilder.setCancelable(false);
				accountRemovedBuilder.create().show();
				isCurrentAccountRemoved = true;
			} catch (Exception e) {
				EMLog.e(TAG, "---------color userRemovedBuilder error" + e.getMessage());
			}

		}

	}
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (getIntent().getBooleanExtra("conflict", false) && !isConflictDialogShow) {
			showConflictDialog();
		} else if (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
			showAccountRemovedDialog();
		}
	}

    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        {
            boolean parentExecuted = super.onMsgSearchComplete(param);
            if (parentExecuted) {
                // 父类已经处理了
                return true;
            }
            switch (param.key) {
                case GET_MESSAGE:
                    if(param.result.bstatus.code==0){
                        msgFragment.refresh();
                    }
                    break;
                case GETAUTHGATES:
                    if(param.result.bstatus.code==0){
                        LockInfoResult  lockInfoResult =(LockInfoResult)param.result;
                        OpenDoorManager.addAll(lockInfoResult.data.gateAuths);
                    }
                    break;
                case CHECK_VERSION: //http://www.96we.com/az.apk
                    final CheckVersionResult checkVersionResult = (CheckVersionResult) param.result;
                    if (checkVersionResult.bstatus.code == 0) {
                        if (checkVersionResult.data != null
                                && checkVersionResult.data.upgradeInfo != null) {

                            AlertDialog.Builder dialog = null;
                            if (dialog == null) {
                                dialog = new AlertDialog.Builder(this);
                                dialog.setTitle("更新提示");
                                dialog.setMessage("最新版本："
                                        + checkVersionResult.data.upgradeInfo.nversion
                                        + "/n"
                                        + checkVersionResult.data.upgradeInfo.upgradeNote);
                                dialog.setPositiveButton("更新", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Uri uri = Uri
                                                .parse(checkVersionResult.data.upgradeInfo.upgradeAddress[0].url);
                                        Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(it);

                                    }

                                });
                            }
                            dialog.show();
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
    }
}

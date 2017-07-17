package com.haolb.client.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.applib.controller.HXSDKHelper;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.haolb.client.R;
import com.haolb.client.app.AppConstants;
import com.haolb.client.app.MainApplication;
import com.haolb.client.domain.response.MessageResult;
import com.haolb.client.net.NetworkListener;
import com.haolb.client.net.NetworkManager;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.utils.BusinessUtils;
import com.haolb.client.utils.Globals;
import com.haolb.client.utils.HandlerCallbacks;
import com.haolb.client.utils.IBaseActFrag;
import com.haolb.client.utils.QLog;
import com.haolb.client.utils.UCUtils;
import com.haolb.client.utils.inject.Injector;
import com.haolb.client.utils.tuski.Tuski;
import com.haolb.client.view.QProgressDialogFragment;
import com.haolb.client.view.SystemBarTintManager;
import com.haolb.client.view.TitleBar;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import static android.app.ActivityManager.RunningAppProcessInfo;


/**
 * 
 */
public abstract class BaseActivity extends FragmentActivity implements
        NetworkListener, OnClickListener, OnLongClickListener,
		OnItemClickListener, OnItemLongClickListener, IBaseActFrag {

	private static final int COLOR_7000 = 0x77000000;
	
	protected Handler mHandler;
	protected Bundle myBundle;

	protected ViewGroup mRoot;
	private FrameLayout mAndroidContent;
	private boolean blockTouch;
	protected boolean fromRestore = false;
	private boolean mIsFloat;
	private HandlerCallbacks.CommonCallback hcb;
	private TitleBar mTitleBar;
	protected QProgressDialogFragment progressDialog;

	public static final int REQUEST_CODE_LOGIN = 4096;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
                mHandler = new Handler(hcb = new HandlerCallbacks.ActivityCallback(this, genCallback()));
		if (savedInstanceState != null) {
			fromRestore = true;
		}
		myBundle = savedInstanceState == null ? getIntent().getExtras()
				: savedInstanceState;
		if (myBundle == null) {
			myBundle = new Bundle();
		}
		mIsFloat = myBundle.getBoolean("mIsFloat");
		blockTouch = myBundle.getBoolean("blockTouch");
    }

    private void setBarTint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.t_theme);
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    /**
     * 返回
     *
     * @param view
     */
    public void back(View view) {
        finish();
    }

	/**
	 * 设置rootview，默认是linearlayout，子类可以自定义类型，使titlebar可以显示不同的样式（如浮动）
	 * 
	 * @return
	 */
	public ViewGroup genRootView() {
		final LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		return linearLayout;
	}

	/**
	 * 设置realroot，默认是linearlayout，子类可以自定义类型
	 * 
	 * @return
	 */
	public ViewGroup genRealRootView() {
		final LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		return linearLayout;
	}

	public void setContentView(View view, boolean autoInject) {
		final ViewGroup realRoot = genRealRootView();
		mRoot = genRootView();
		mTitleBar = new TitleBar(this);
		mRoot.addView(mTitleBar, -1, -2);
		mRoot.addView(view, -1, -1);
		realRoot.addView(mRoot, -1, -1);
		super.setContentView(realRoot);
		mTitleBar.setVisibility(View.GONE);
		if (autoInject) {
			Injector.inject(this);
		}
	};

	public void setContentView(int layoutResID, boolean autoInject) {
		final View content = getLayoutInflater().inflate(layoutResID, null);
		setContentView(content, autoInject);
	}

	@Override
	public void setContentView(int layoutResID) {
		setContentView(layoutResID, true);
	}

	@Override
	public void setContentView(View view) {
		setContentView(view, true);
	}

	public void openSoftinput(final EditText editText) {
		editText.setFocusable(true);
		editText.setFocusableInTouchMode(true);
		editText.requestFocus();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) editText
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(editText, 0);
			}
		}, 498);
	}

	@Override
	public void showToast(String message) {
		// showToast(message, 2000);
		Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
		// Tuski.makeText(getContext(), message,
		// Appearance.DEFAULT_BOTTOM).show();
	}

	/**
	 * 这玩意会导致view leak 暂时不要使用这种方式
	 * 
	 * @param message
	 * @param delay
	 */
	@Deprecated
	public void showToast(String message, long delay) {
		if (isFinishing()) {
			return;
		}
		final TextView view = new TextView(getContext());
		view.setText(message);
		view.setTextColor(Color.WHITE);
		view.setBackgroundResource(android.R.drawable.toast_frame);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.TYPE_TOAST,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
						| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
		params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.format = PixelFormat.RGBA_8888;
		getWindowManager().addView(view, params);
		view.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (view.getParent() != null) {
					getWindowManager().removeView(view);
				}
			}
		}, delay);
	}

	@Override
	public void onNetEnd(NetworkParam param) {
		if (param.block) {
			onCloseProgress(param);
		}
	}

	@Override
	public void onNetError(final NetworkParam param, int errCode) {
		// showToast("error code :" + errCode + "\nparam:" + param.toString());
		if (param.block) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.notice)
					//
					.setMessage(R.string.net_network_error)
					//
					.setNegativeButton(R.string.retry,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									Request.startRequest(param, mHandler);
								}
							})
					.setPositiveButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
			onCloseProgress(param);
		}
	}
    public void updateUnreadAddressLable(){

    }
	@Override
	public boolean onMsgSearchComplete(final NetworkParam param) {
        if(param.key==ServiceMap.GET_MESSAGE){
            if(param.result.bstatus.code==0){
                MessageResult messageResult =(MessageResult) param.result;
                MainApplication.getInstance().setMessage(messageResult.data.messages);
                if (MainApplication.getInstance().getHasNewMessage()) {
                    // 提示新消息
                    EMMessage message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                    message.setFrom(getContext().getContext().getString(R.string.app_name));
                    message.addBody(new TextMessageBody("新公告"));
                    updateUnreadAddressLable();
                    com.easemob.applib.controller.HXSDKHelper.getInstance().getNotifier().sendMyNotification(message);
                }
            }
        }

		switch (param.result.bstatus.code) {
		case Globals.STATUS_CODE_NOT_LOGIN:
			UCUtils.getInstance().removeCookie();
			showToast(param.result.bstatus.des);

			Bundle bundle = new Bundle();
			bundle.putSerializable(NetworkParam.TAG, param);

			qStartActivityForResult(LoginAct.class, bundle, REQUEST_CODE_LOGIN);
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case REQUEST_CODE_LOGIN:
			NetworkParam networkParam = (NetworkParam) data.getSerializableExtra(NetworkParam.TAG);
			if(networkParam != null) {
				Request.startRequest(networkParam, mHandler);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onNetCancel(NetworkParam param) {

	}

	@Override
	public void onNetStart(final NetworkParam param) {
		if (param.block) {
			onShowProgress(param);
		}
	}

	public void onShowProgress(final NetworkParam networkParam) {
		progressDialog = (QProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(networkParam.toString());
		if (progressDialog == null) {
			progressDialog = QProgressDialogFragment.newInstance(
					networkParam.progressMessage, networkParam.cancelAble,
					new OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							NetworkManager.getInstance().cancelTaskByParam(
									networkParam);
							onNetCancel(networkParam);
						}
					});
			progressDialog.show(getSupportFragmentManager(),
					networkParam.toString());
		} else {
			progressDialog.setMessage(networkParam.progressMessage);
			progressDialog.setCancelable(networkParam.cancelAble);
		}
	}

	public void onShowProgress(String message, boolean cancelAble,
			OnCancelListener cancelListener) {
		progressDialog = QProgressDialogFragment.newInstance(message,
				cancelAble, cancelListener);
		progressDialog.show(getSupportFragmentManager(), message);
	}

	public void onCloseProgress(NetworkParam networkParam) {
		onCloseProgress(networkParam.toString());
	}

	public void onCloseProgress(String message) {
		progressDialog = (QProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(message);
		if (progressDialog != null) {
			try {
				progressDialog.dismissAllowingStateLoss();
			} catch (Exception e) {
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (myBundle != null) {
			outState.putAll(myBundle);
		}
		super.onSaveInstanceState(outState);
		outState.putBoolean("blockTouch", blockTouch);
		outState.putBoolean("mIsFloat", mIsFloat);

	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		blockTouch = true;
		super.startActivityForResult(intent, requestCode);
	}

	@Override
	public void startActivityFromFragment(Fragment fragment, Intent intent,
			int requestCode) {
		blockTouch = true;
		super.startActivityFromFragment(fragment, intent, requestCode);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (blockTouch) {
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}

    public boolean isActive ;
	@Override
	protected void onResume() {
		super.onResume();
        if (!isActive) {
//        app 从后台唤醒，进入前台

        isActive = true;
        }
        // onresume时，取消notification显示
        HXSDKHelper.getInstance().getNotifier().reset();

        // umeng
        MobclickAgent.onResume(this);

		mAndroidContent = (FrameLayout) getWindow().findViewById(
				Window.ID_ANDROID_CONTENT);
		mAndroidContent.setForegroundGravity(Gravity.FILL);
		if (fromRestore) {
			fromRestore = false;
			if (mIsFloat) {
				mAndroidContent.setForeground(new ColorDrawable(COLOR_7000));
			}
		} else {
			Drawable foreground = mAndroidContent.getForeground();
			if (foreground != null) {
				mAndroidContent.setForeground(null);
			}
		}
		blockTouch = false;
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		Application application = getApplication();
//		if (application instanceof MainApp) {
//			((MainApp) application).resetActiveContext(getClass());
//		}

		if (hcb != null) {
			hcb.removeListener();
		}
		Tuski.clearTuskiesForActivity(this);
		NetworkManager.getInstance().cancelTaskByHandler(mHandler);

	}

	@Override
	public void onClick(View v) {
	}

	@Override
	public boolean onLongClick(View v) {
		return false;
	}

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    /**
	 * 设置标题栏
	 * 
	 * @param title
	 * @param hasBackBtn
	 * @param rightIconResId
	 * @param rightListener
	 */
	public void setTitleBar(String title, boolean hasBackBtn,
			int rightIconResId, OnClickListener rightListener) {
		mTitleBar.setTitleBar(titleBarClickListener, hasBackBtn, title,
				rightIconResId, rightListener);
		mTitleBar.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置标题栏
	 * 
	 * @param title
	 * @param hasBackBtn
	 * @param rightText
	 * @param rightListener
	 */
	public void setTitleBar(String title, boolean hasBackBtn, String rightText,
			OnClickListener rightListener) {
		mTitleBar.setTitleBar(titleBarClickListener, hasBackBtn, title,
				rightText, rightListener);
		mTitleBar.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置标题栏
	 * 
	 * @param title
	 * @param hasBackBtn
	 */
	public void setTitleBar(String title, boolean hasBackBtn) {
		mTitleBar.setTitleBar(titleBarClickListener, hasBackBtn, title);
		mTitleBar.setVisibility(View.VISIBLE);
//        setBarTint();
    }

	public void setTitleText(String text) {
		mTitleBar.setTitle(text);
	}

	protected void setTabIcon(View view, String text, int icon) {
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		if (imageView != null) {
			imageView.setImageResource(icon);
		}
		TextView textView = (TextView) view.findViewById(R.id.textview);
		if (textView != null) {
			textView.setText(text);
		}
		// textView.setTextColor(Color.WHITE);
		// textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
	}

	/**
	 * 如果想处理,网络请求以外的消息,可以实现这个方法,返回自定义的callback即可
	 * 
	 * @return
	 */
	protected Callback genCallback() {
		return null;
	}

	public void shareText(String text) {
		shareText("好友推荐", text);
	}

	public void shareText(String subject, String text) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		// intent.setType("image/*");//
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(intent, "平台分享"));
	}



	@Override
	public void qStartActivity(Intent intent) {
		startActivity(intent);
	}

	@Override
	public void qStartActivity(Class<? extends Activity> cls) {
		qStartActivity(cls, null);
	}

	/* 打开新的Activity */
	@Override
	public void qStartActivity(Class<? extends Activity> cls, Bundle bundle) {
		Intent intent = new Intent();
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		intent.setClass(this, cls);
		startActivity(intent);
	}

	@Override
	public void qStartShareActivity(String title, String shareContent) {
//		Intent intent = new Intent(Intent.ACTION_SEND);
//		intent.setType("text/plain");
//		if (title != null) {
//			intent.putExtra(Intent.EXTRA_SUBJECT, title);
//		}
//		intent.putExtra(Intent.EXTRA_TEXT, shareContent);
//		startActivity(Intent.createChooser(intent,
//				getString(R.string.share_message)));
	}

	@Override
	public void qStartImageShare(String shareContent, Uri shareUri) {
//		Intent intent = new Intent(Intent.ACTION_SEND);
//		intent.setType("image/*");
//		if (shareUri == null) {
//			qStartShareActivity(null, shareContent);
//			return;
//		}
//		intent.putExtra(Intent.EXTRA_STREAM, shareUri);
//		intent.putExtra(Intent.EXTRA_TEXT, shareContent);
//		intent.putExtra("sms_body", shareContent);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(Intent.createChooser(intent,
//				getString(R.string.share_message)));
	}

	/* 打开新的Activity for result */
	@Override
	public void qStartActivityForResult(Class<? extends Activity> cls,
			Bundle bundle, int requestCode) {
		Intent intent = new Intent();
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		intent.setClass(this, cls);
		startActivityForResult(intent, requestCode);
	}

	/* 带结果返回上一个activity， 配合qStartActivityForResult使用 */
	@Override
	public void qBackForResult(int resultCode, Bundle bundle) {
		Intent intent = new Intent();
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		setResult(resultCode, intent);
		finish();
	}

	/* 根据url跳转Activity */
	public void qStartActivity(String url, Bundle bundle) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		if (bundle == null) {
			bundle = new Bundle();
		}
		bundle.putBoolean("noQuitConfirm", false);
		intent.putExtras(bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	@Override
	public void qShowAlertMessage(String title, String message) {
		try {
			new AlertDialog.Builder(this).setTitle(title).setMessage(message)
					.setNegativeButton(R.string.sure, null).show();
		} catch (IllegalStateException e) {
		}
	}
    public void qShowAlertMessage(  String message) {
        qShowAlertMessage(getString(R.string.notice), message);
    }
	public void qShowAlertMessage(int titleResId, String message) {
		qShowAlertMessage(getString(titleResId), message);
	}

	public void qShowAlertMessage(int titleResId, int msgResId) {
		qShowAlertMessage(getString(titleResId), getString(msgResId));
	}

	@Override
	public void onNewIntent(Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			int intentToCode = bundle.getInt(Globals.INTENT_TO_ACTIVITY);
			/*if (Globals.INTENT_TO_QUITAPP == intentToCode) {
				finish();
				MainApp.getContext().onClose();
			} else if(Globals.INTENT_TO_PAY_CENTER == intentToCode) {
				//跳到支付中心
				qStartActivity(HMVouchersActivity.class, bundle);
			}*/
		}
		super.onNewIntent(intent);
	}

	protected void quitApp() {
		NetworkManager.getInstance().destroy();
		Bundle bundle = new Bundle();
		bundle.putInt(Globals.INTENT_TO_ACTIVITY, Globals.INTENT_TO_QUITAPP);
		qBackToActivity(MainActivity2.class, bundle);
		if (AppConstants.KILL_PROGRESS_ON_QUIT) {
			ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			manager.killBackgroundProcesses(getPackageName());
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	/* 回到之前的Activity */
	@Override
	public void qBackToActivity(Class<? extends Activity> cls, Bundle bundle) {
		Intent intent = new Intent();
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		intent.setClass(this, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	/**
	 * 关掉软键盘
	 */
	public void hideSoftInput() {
		try {
			InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
		}
	}

	public void processAgentPhoneCall(String phoneNum) {
		try {
			startActivity(new Intent(Intent.ACTION_CALL,
					Uri.parse(BusinessUtils.formatPhoneNumber(phoneNum))));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PopupWindow showTipText(String text) {
		return showTipText(null, text);
	}

	public PopupWindow showTipText(String title, String text) {
		View view = LayoutInflater.from(this)
				.inflate(R.layout.tip_dialog, null);
		TextView textView = (TextView) view.findViewById(R.id.textview);// new
																		// TextView(this);
		textView.setText(text);
		TextView textView1 = (TextView) view.findViewById(R.id.textview1);// new
																			// TextView(this);
		if (TextUtils.isEmpty(title)) {
			textView1.setVisibility(View.GONE);
		} else {
			textView1.setVisibility(View.VISIBLE);
			textView1.setText(title);
		}
		return showTipView(view);
	}

	public PopupWindow showTipView(int id) {
		return showTipView(LayoutInflater.from(this).inflate(id, null));
	}

	@Override
	public PopupWindow showTipView(View view) {
		final PopupWindow popupWindow = new PopupWindow(view,
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources()));
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				popupWindow.showAtLocation(getWindow().getDecorView(),
						Gravity.CENTER, 0, 0);
			}
		}, 100);
		popupWindow.setTouchInterceptor(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (popupWindow != null && popupWindow.isShowing()) {
						popupWindow.dismiss();
					}
				}
				return false;
			}
		});
		return popupWindow;
	}

	/**
	 * 输入错误提示
	 * 
	 * @param editText
	 */
	protected void showErrorTip(final EditText editText, int msgResId) {
		showErrorTip(editText, getString(msgResId));
	}

	/**
	 * 输入错误提示
	 * 
	 * @param editText
	 * @param message
	 */
	protected void showErrorTip(final EditText editText, String message) {

        new AlertDialog.Builder(this).setTitle(getString(R.string.notice))
                .setMessage(message).setNegativeButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (editText != null) {
                    editText.requestFocus(View.FOCUS_RIGHT);
                    String txt = editText.getText().toString().trim();
                    editText.setText(txt);
                    editText.setSelection(txt.length());
                    InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(editText, 0);
                }
            }
        }).show();
	}

	@Override
	public BaseActivity getContext() {
		return this;
	}

	@Override
	public Handler getHandler() {
		return mHandler;
	}

	public boolean superDispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	private final OnClickListener titleBarClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			if (v != null && v.getId() == R.id.title_left_btn) {
				onBackPressed();
				hideSoftInput();
			}
		};
	};

	public void disableEnableControls(boolean enable, View vg) {
		if (vg instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) vg;
			for (int i = 0; i < group.getChildCount(); i++) {
				View child = group.getChildAt(i);
				if (child instanceof ViewGroup) {
					disableEnableControls(enable, child);
				} else {
					child.setEnabled(enable);
				}
			}
		}
		vg.setEnabled(enable);
	}

	public static void simulateKey(final int KeyCode) {
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyCode);
				} catch (Exception e) {
					QLog.e("Exception when sendKeyDownUpSync", e.toString());
				}
			}
		}.start();
	}
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();

        if (!isAppOnForeground()) {
            //app 进入后台

            isActive = false; //记录当前已经进入后台
        }
    }


    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
}
}

package com.haolb.client.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.support.v4.app.Fragment;
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
import android.view.WindowManager.BadTokenException;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.haolb.client.R;
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


/**
 * 
 * @author zexu
 */
public abstract class BaseFragment extends Fragment implements OnClickListener,
		OnLongClickListener, OnItemClickListener, OnItemLongClickListener,
        NetworkListener, IBaseActFrag {

	protected Handler mHandler;
	protected Bundle myBundle;
	private HandlerCallbacks.FragmentCallback hcb;
	protected QProgressDialogFragment progressDialog;
	protected TitleBar mTitleBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler(hcb = new HandlerCallbacks.FragmentCallback(this, genCallback()));
		myBundle = savedInstanceState == null ? getArguments()
				: savedInstanceState;
		if (myBundle == null) {
			myBundle = new Bundle();
		}

    }
    private void setBarTint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(this.getActivity());
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.t_theme);

    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getActivity().getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

	protected View onCreateViewWithTitleBar(LayoutInflater inflater,
			ViewGroup container, int resource) {
		final LinearLayout linearLayout = new LinearLayout(this.getContext());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		mTitleBar = new TitleBar(this.getContext());
		linearLayout.addView(mTitleBar, LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		View view = inflater.inflate(resource, null, false);
		linearLayout.addView(view, LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		mTitleBar.setVisibility(View.GONE);
		return linearLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Injector.inject(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (myBundle != null) {
			outState.putAll(myBundle);
		}
		super.onSaveInstanceState(outState);
	}
	/**
	 * 设置标题栏
	 * @param title
	 * @param hasBackBtn
	 * @param rightIconResId
	 * @param rightListener
	 */
	public void setTitleBar(String title, boolean hasBackBtn,int rightIconResId,OnClickListener rightListener) {
		mTitleBar.setTitleBar(titleBarClickListener, hasBackBtn, title, rightIconResId,rightListener);
		mTitleBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 设置标题栏
	 * @param title
	 * @param hasBackBtn
	 * @param rightText
	 * @param rightListener
	 */
	public void setTitleBar(String title, boolean hasBackBtn,String rightText,OnClickListener rightListener) {
		mTitleBar.setTitleBar(titleBarClickListener, hasBackBtn, title, rightText,rightListener);
		mTitleBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 设置标题栏
	 * @param title
	 * @param hasBackBtn
	 */
	public void setTitleBar(String title, boolean hasBackBtn) {
		mTitleBar.setTitleBar(titleBarClickListener, hasBackBtn, title);
		mTitleBar.setVisibility(View.VISIBLE);
//        setBarTint();
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
	
	public void shareText(String subject,String text) {
		Intent intent=new Intent(Intent.ACTION_SEND);
		//	intent.setType("image/*");//
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(intent, "平台分享"));
	}
	

    /*@Override
    public void qOpenWebView(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.WEBVIEW_URL, url);
        qStartActivity(WebActivity.class, bundle);
    }*/
    
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
		intent.setClass(getActivity(), cls);
		startActivity(intent);
	}

	@Override
	public void qStartShareActivity(String title, String shareContent) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		if (title != null) {
			intent.putExtra(Intent.EXTRA_TITLE, title);
		}
		intent.putExtra(Intent.EXTRA_TEXT, shareContent);
		startActivity(Intent.createChooser(intent,
				getString(R.string.share_message)));
	}

	@Override
	public void qStartImageShare(String shareContent, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		if (uri == null) {
			qStartShareActivity(null, shareContent);
			return;
		}
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.putExtra(Intent.EXTRA_TEXT, shareContent);
		intent.putExtra("sms_body", shareContent);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(intent,
				getString(R.string.share_message)));
	}

	/* 打开新的Activity for result */
	@Override
	public void qStartActivityForResult(Class<? extends Activity> cls,
			Bundle bundle, int requestCode) {
		Intent intent = new Intent();
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		intent.setClass(getActivity(), cls);
		startActivityForResult(intent, requestCode);
	}

	/* 带结果返回上一个activity， 配合qStartActivityForResult使用 */
	@Override
	public void qBackForResult(int resultCode, Bundle bundle) {
		Intent intent = new Intent();
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		getActivity().setResult(resultCode, intent);
		getActivity().finish();
	}

	/* 回到之前的Activity */
	@Override
	public void qBackToActivity(Class<? extends Activity> cls, Bundle bundle) {
		Intent intent = new Intent();
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		intent.setClass(getActivity(), cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	/* 根据url跳转Activity */
	public void qStartActivity(String url, Bundle bundle) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	public void qShowAlertMessage(String message) {
		qShowAlertMessage(getString(R.string.notice), message);
	}
	public void qShowAlertMessage(int titleResId, String message) {
		qShowAlertMessage(getString(titleResId), message);
	}

	@Override
	public void qShowAlertMessage(String title, String message) {

        try {
            new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(message).setNegativeButton(R.string.sure, null)
                    .show();
        } catch (Exception e) {
        }
	}

	@Override
	public void processAgentPhoneCall(String phoneNum) {
		try {
			startActivity(new Intent(Intent.ACTION_CALL,
					Uri.parse(BusinessUtils.formatPhoneNumber(phoneNum))));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showToast(String message) {
		Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
		// Tuski.makeText(getContext(), message,
		// Appearance.DEFAULT_BOTTOM).show();
	}

	@Override
	public BaseActivity getContext() {
		return (BaseActivity) getActivity();
	}

	@Override
	public Handler getHandler() {
		return mHandler;
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public boolean onLongClick(View v) {
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		return false;
	}

	@Override
	public void onDestroy() {
		NetworkManager.getInstance().cancelTaskByHandler(mHandler);
		if (hcb != null) {
			hcb.removeListener();
		}
		Tuski.clearTuskiesForActivity(getActivity());
		super.onDestroy();
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
            new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.notice))
                    .setMessage(getString(R.string.net_network_error))
                    .setNegativeButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Request.startRequest(param, mHandler);
                        }
                    })
                    .setPositiveButton(getString(R.string.cancel) ,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
			onCloseProgress(param);
		}
	}

	private final OnClickListener titleBarClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			if (v != null && v.getId() == R.id.title_left_btn) {
				getActivity().onBackPressed();
			}
		};
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case BaseActivity.REQUEST_CODE_LOGIN:
			NetworkParam networkParam = (NetworkParam) data.getSerializableExtra(NetworkParam.TAG);
			Request.startRequest(networkParam, mHandler);
			break;
		default:
			break;
		}
	}
    @Override
    public boolean onMsgSearchComplete(final NetworkParam param) {
        if(param.key== ServiceMap.GET_MESSAGE){
            if(param.result.bstatus.code==0){
                MessageResult messageResult =(MessageResult) param.result;
                MainApplication.getInstance().setMessage(messageResult.data.messages);
                if (MainApplication.getInstance().getHasNewMessage()) {
                    // 提示新消息
                    EMMessage message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                    message.setFrom(getContext().getContext().getString(R.string.app_name));
                    message.addBody(new TextMessageBody("新公告"));
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

                qStartActivityForResult(LoginAct.class, bundle, BaseActivity.REQUEST_CODE_LOGIN);
                return true;
            default:
                break;
        }
        return false;
    }

	@Override
	public void onNetCancel(NetworkParam param) {

	}

	@Override
	public void onNetStart(NetworkParam param) throws BadTokenException {
		if (param.block) {
			onShowProgress(param);
		}
	}

	public void onShowProgress(final NetworkParam networkParam) {
		progressDialog = (QProgressDialogFragment) getFragmentManager()
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
			progressDialog.show(getFragmentManager(), networkParam.toString());
		} else {
			progressDialog.setMessage(networkParam.progressMessage);
			progressDialog.setCancelable(networkParam.cancelAble);
		}
	}

	public void onCloseProgress(NetworkParam networkParam) {
		progressDialog = (QProgressDialogFragment) getFragmentManager()
				.findFragmentByTag(networkParam.toString());
		if (progressDialog != null) {
			try {
				progressDialog.dismissAllowingStateLoss();
			} catch (Exception e) {
				QLog.e("error", e.getLocalizedMessage(), e);
			}
		}
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
        new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.notice))
                .setMessage(message).setNegativeButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (editText != null) {
                    editText.requestFocus(View.FOCUS_RIGHT);
                    String txt = editText.getText().toString().trim();
                    editText.setText(txt);
                    editText.setSelection(txt.length());
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                            .getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(editText, 0);
                }
            }
        }).show();
	}

	public PopupWindow showTipText(String text) {
		return showTipText(null, text);
	}

	public PopupWindow showTipText(String title, String text) {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.tip_dialog, null);
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
		return showTipView(LayoutInflater.from(getContext()).inflate(id, null));
	}

	@Override
	public PopupWindow showTipView(View view) {
		final PopupWindow popupWindow = new PopupWindow(view,
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources()));
        popupWindow.setTouchable(true);
		popupWindow.showAtLocation(getContext().getWindow().getDecorView(),
				Gravity.CENTER, 0, 0);
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
}

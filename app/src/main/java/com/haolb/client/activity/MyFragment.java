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

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easemob.Constant;
import com.easemob.EMCallBack;
import com.haolb.client.R;
import com.haolb.client.app.MainApplication;
import com.haolb.client.domain.Community;
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.domain.param.UpdateMyPortraitParam;
import com.haolb.client.domain.response.BaseResult;
import com.haolb.client.domain.response.UpdateMyPortraitResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.utils.BitmapHelper;
import com.haolb.client.utils.MSystem;
import com.haolb.client.utils.QArrays;
import com.haolb.client.utils.UCUtils;
import com.haolb.client.utils.cache.ImageLoader;
import com.haolb.client.utils.inject.From;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.haolb.client.net.Request.RequestFeature.BLOCK;
import static com.haolb.client.net.Request.RequestFeature.CANCELABLE;

/**
 * 我的界面
 *
 * @author Administrator
 */
public class MyFragment extends BaseFragment  {
    @From(R.id.image_header)
    public ImageView imageHeader;
    @From(R.id.tv_name)
    public TextView tvName;
    @From(R.id.tv_phone)
    public TextView tvPhone;
    @From(R.id.ll_my)
    public LinearLayout llMy;

    @From(R.id.ll_address)
    public LinearLayout llAddress;
    @From(R.id.tv_address)
    public TextView tvAddress;
    @From(R.id.ll_shouquan)
    public LinearLayout llShouquan;
    @From(R.id.ll_jieshou)
    public LinearLayout llJieshou;
    @From(R.id.ll_feedback)
    public LinearLayout llFeedBack;
    @From(R.id.ll_about)
    public LinearLayout llAbout;
    @From(R.id.ll_share)
    public LinearLayout llShare;
    @From(R.id.btn_ext)
    public Button btnExt;
    private TextView tvCamera;
    private TextView tvAlbum;
    private File mCurrentPhotoFile;
    private String photoFilePath;
    private View view;
    private PopupWindow popupWindow;
    private Community community;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateViewWithTitleBar(inflater, container, R.layout.activity_my);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        view = LinearLayout.inflate(getActivity(), R.layout.view_charge_header, null);
        tvCamera = (TextView) view.findViewById(R.id.tv_camera);
        tvAlbum = (TextView) view.findViewById(R.id.tv_album);
        tvCamera.setOnClickListener(this);
        tvAlbum.setOnClickListener(this);
        llMy.setOnClickListener(this);
        llJieshou.setOnClickListener(this);
        llAddress.setOnClickListener(this);
        llFeedBack.setOnClickListener(this);
        llShare.setOnClickListener(this);
        llAbout.setOnClickListener(this);
        llShouquan.setOnClickListener(this);
        btnExt.setOnClickListener(this);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow!=null&&popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
//        intView();
    }

    public void intView(){
        tvName.setText(UCUtils.getInstance().getUsername());
        tvPhone.setText(UCUtils.getInstance().getAccount());
        ImageLoader.getInstance(getContext()).loadImage(UCUtils.getInstance().getUserPortrait(), imageHeader, R.drawable.logo_uidemo);
    }

    @Override
    public void onResume() {
        if(!QArrays.isEmpty(UCUtils.getInstance().getCommunitys())&&UCUtils.getInstance().getCommunitys().get(0)!=null){
            tvAddress.setText(UCUtils.getInstance().getCommunitys().get(0).title+(UCUtils.getInstance().getCommunitySize()>1?"等"+UCUtils.getInstance().getCommunitySize()+"个小区":""));
        }

        if(UCUtils.getInstance().getCity()!=null){
            setTitleBar("好乐帮", false/*,"访问授权",new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    qStartActivity(AuthListAct.class);
                }
            }*/);
//            setTitleBar("我", false ,UCUtils.getInstance().getCity().cityname ,new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    qStartActivity(CityListAct.class);
//                }
//            });
        }
        intView();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_camera:
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                doTakePhoto();
                break;
            case R.id.tv_album:
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                doPickPhotoFromGallery();
                break;
            case R.id.ll_my:
                if (popupWindow == null) {
                    popupWindow = new PopupWindow(view,
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
                    popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources()));
                    popupWindow.setTouchable(true);
                }
                if (!popupWindow.isShowing()) {
                    popupWindow.showAtLocation(getContext().getWindow().getDecorView(),
                            Gravity.CENTER, 0, 0);
                }
                break;
            case R.id.ll_address:
                Bundle bundle = new Bundle();
                bundle.putBoolean("isAddDef", true);
                qStartActivity(CommunityComListAct.class, bundle);
                break;
            case R.id.ll_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, "好乐帮"));
                break;
            case R.id.ll_feedback:
                qStartActivity(FeedBackAct.class);
                break;
            case R.id.ll_about:
                qStartActivity(AboutAct.class);
                break;
            case R.id.ll_shouquan:
                qStartActivity(AuthListAct.class);

                break;
            case R.id.ll_jieshou:
                qStartActivity(MyAuthRecAct.class);
                break;
            case R.id.btn_ext:
                   logout();
                break;
            default:
                break;
        }

    }
    ProgressDialog pd ;
    void logout() {
        pd = new ProgressDialog(getActivity());
        String st = getResources().getString(R.string.Are_logged_out);
        pd.setMessage(st);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        MainApplication.getInstance().logout(new EMCallBack() {

            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {

                        BaseParam baseParam=new BaseParam();
                        Request.startRequest(baseParam, ServiceMap.LOGOUT, mHandler ,  CANCELABLE);

                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {

            }
        });
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

    private static final int CAMERA_WITH_DATA = 3010;
    private static final int PHOTO_PICKED_WITH_DATA = 3011;
    private static final int CHANGEPORTRAIT_REQUESTCODE = 3012;// Portrait

    private void doTakePhoto() {
        try {
            File root = new File(MSystem.getStoragePath(getActivity()));
            mCurrentPhotoFile = new File(root, getPhotoFileName());
            photoFilePath = mCurrentPhotoFile.getAbsolutePath();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(mCurrentPhotoFile));
            startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG_'yyyyMMddHHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    private void doPickPhotoFromGallery() {
        try {
            File root = new File(MSystem.getStoragePath(getActivity()));
            mCurrentPhotoFile = new File(root, getPhotoFileName());
            photoFilePath = mCurrentPhotoFile.getAbsolutePath();
            if (!root.exists()) {
                root.mkdirs();
                mCurrentPhotoFile.createNewFile();
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 100);
            intent.putExtra("outputY", 100);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(mCurrentPhotoFile));
            intent.putExtra("outputFormat",
                    Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true); // no face detection
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void updateUserInfo(Community community){
//        UpdateUserInfoParam param = new UpdateUserInfoParam();
//        param.type = "address";
//        param.objValue = community;
//        Request.startRequest(param, ServiceMap.UPDATE_INFO, mHandler,  "更新中...", BLOCK,CANCELABLE);
//    }


    @Override
    public void onNetError(NetworkParam param, int errCode) {
        pd.dismiss();
        super.onNetError(param, errCode);
    }

    @Override
    public void onNetCancel(NetworkParam param) {
        pd.dismiss();
        super.onNetCancel(param);
    }

    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        if (super.onMsgSearchComplete(param)) {
            // 父类已经处理了
            return true;
        }
        switch (param.key) {
            case UPDATE_INFO:
                BaseResult baseResult = (BaseResult) param.result;
                if (baseResult.bstatus.code == 0) {
                    tvAddress.setText(community.title);
                    UCUtils.getInstance().saveCommunity(community);
                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            case UPDATE_MY_PROTRAIT:
                UpdateMyPortraitResult portraitResult = (UpdateMyPortraitResult) param.result;
                if (portraitResult.bstatus.code == 0) {
                    ImageLoader.getInstance(getActivity()).loadImage(portraitResult.data.portrait ,imageHeader ,R.drawable.default_avatar);
                    if(!TextUtils.isEmpty(portraitResult.data.portrait)){
                        UCUtils.getInstance().saveUserPortrait(portraitResult.data.portrait);
                    }
                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            case LOGOUT:
                if (param.result.bstatus.code == 0) {
                    pd.dismiss();
                    MainApplication.getInstance().saveMessage();
                    UCUtils.getInstance().removeCookie();
                    // 重新显示登陆页面
                    ((MainActivity2) getActivity()).finish();
                    startActivity(new Intent(getActivity(), LoginAct.class));
                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            default:
                break;
        }
        return super.onMsgSearchComplete(param);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != getActivity().RESULT_OK) {
            return;
        }

        switch (requestCode) {
//            case RESULT_SLELECT_COMMUNITY: {
//                  community = (Community)data.getExtras().getSerializable(Community.TAG);
//                if(community!=null){
//                    updateUserInfo(community);
//                }
//                break;
//            }
            case CAMERA_WITH_DATA: {
                File file = new File(photoFilePath);
                Uri uri = Uri.fromFile(file);
                doCropPhoto(uri);
                break;
            }
            case PHOTO_PICKED_WITH_DATA: {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap bitmap = extras.getParcelable("data");
                    saveAvatar(bitmap);
                }
                break;
            }
            case CHANGEPORTRAIT_REQUESTCODE:
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap bitmap = extras.getParcelable("data");
                    saveAvatar(bitmap);
                }
                break;
            default:
                break;
        }
    }

    private void doCropPhoto(Uri source/* ,Uri target */) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(source, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 100);
            intent.putExtra("outputY", 100);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            // intent.putExtra(MediaStore.EXTRA_OUTPUT, target);
            intent.putExtra("outputFormat",
                    Bitmap.CompressFormat.JPEG.toString());
            startActivityForResult(intent, CHANGEPORTRAIT_REQUESTCODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


        private void saveAvatar(final Bitmap bitmap) {
       Bitmap bt = BitmapHelper.compressImage(bitmap);
        imageHeader.setImageBitmap(bt);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mCurrentPhotoFile);
            bt.compress(Bitmap.CompressFormat.JPEG, 75, fos);
            fos.flush();
            fos.close();
            UpdateMyPortraitParam param = new UpdateMyPortraitParam();
            param.byteLength = mCurrentPhotoFile.length();
            param.ext = ".jpg";
            NetworkParam np = Request.getRequest(param,
                    ServiceMap.UPDATE_MY_PROTRAIT, new Request.RequestFeature[]{
                            BLOCK, CANCELABLE});
            np.progressMessage = "上传中......";
            np.filePath = mCurrentPhotoFile.getAbsolutePath();
            Request.startRequest(np, mHandler);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }

    }

    @Override
    public void onNetEnd(NetworkParam param) {
        super.onNetEnd(param);
        if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists()) {
            mCurrentPhotoFile.delete();
        }
    }
}

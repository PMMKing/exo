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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.haolb.client.R;
import com.haolb.client.domain.Community;
import com.haolb.client.domain.param.SetDefCommunityParam;
import com.haolb.client.domain.param.UpdateMyPortraitParam;
import com.haolb.client.domain.param.UpdateUserInfoParam;
import com.haolb.client.domain.response.BaseResult;
import com.haolb.client.domain.response.UpdateMyPortraitResult;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.utils.BitmapHelper;
import com.haolb.client.utils.MSystem;
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
public class MyInfo extends BaseActivity {

    @From(R.id.btn_ok)
    public Button btnOk;
    @From(R.id.iv_avatar)
    public ImageView imageHeader;
    @From(R.id.et_nickname)
    public EditText etNickName;
    @From(R.id.tv_community)
    public TextView tvCommunity;

    private TextView tvCamera;
    private TextView tvAlbum;
    private File mCurrentPhotoFile;
    private String photoFilePath;
    private View view;
    private PopupWindow popupWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privte_setting);
        setTitleBar("个人设置", false);

        view = LinearLayout.inflate(this, R.layout.view_charge_header, null);
        tvCamera = (TextView) view.findViewById(R.id.tv_camera);
        tvAlbum = (TextView) view.findViewById(R.id.tv_album);
        tvCamera.setOnClickListener(this);
        tvAlbum.setOnClickListener(this);
        imageHeader.setOnClickListener(this);
        tvCommunity.setOnClickListener(this);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname = etNickName.getText().toString();
                if (TextUtils.isEmpty(nickname)) {
                    showToast("请填写昵称");
                    return;
                }
                if (nickname.length() > 4) {
                    showToast("昵称不能多于4个字符");
                    return;
                }
                UpdateUserInfoParam param = new UpdateUserInfoParam();
//                param.type = "nickname" ;
                param.nickname = nickname;
                Request.startRequest(param, ServiceMap.UPDATE_INFO, mHandler, BLOCK, CANCELABLE);
            }
        });
        ImageLoader.getInstance(this).loadImage(UCUtils.getInstance().getUserPortrait(), imageHeader, R.drawable.default_avatar);


    }


    @Override
    public void onBackPressed() {

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
            case R.id.iv_avatar:
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
            case R.id.tv_community:
                qStartActivityForResult(CommunityListAct.class, null, CommunityListAct.REQUEST_CODE_COMMUNITY);
                break;

            default:
                break;
        }

    }


    private static final int CAMERA_WITH_DATA = 3010;
    private static final int PHOTO_PICKED_WITH_DATA = 3011;
    private static final int CHANGEPORTRAIT_REQUESTCODE = 3012;// Portrait

    private void doTakePhoto() {
        try {
            File root = new File(MSystem.getStoragePath(this));
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
            File root = new File(MSystem.getStoragePath(this));
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
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentPhotoFile));
//            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true); // no face detection
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onMsgSearchComplete(NetworkParam param) {
        if (super.onMsgSearchComplete(param)) {
            // 父类已经处理了
            return true;
        }
        switch (param.key) {
            case SETDEFAULTCOMMUNITY:
                if (param.result.bstatus.code == 0) {
                    Community community = (Community) param.ext;
                    UCUtils.getInstance().saveCommunity(community);
                }
//                showToast(param.result.bstatus.des);

                break;
            case UPDATE_INFO:
                BaseResult baseResult = (BaseResult) param.result;
                if (baseResult.bstatus.code == 0) {
                    UCUtils.getInstance().saveUsername(etNickName.getText().toString());
                    finish();
                } else {
                    showToast(param.result.bstatus.des);
                }
                break;
            case UPDATE_MY_PROTRAIT:
                UpdateMyPortraitResult portraitResult = (UpdateMyPortraitResult) param.result;
                if (portraitResult.bstatus.code == 0) {
                    ImageLoader.getInstance(this).loadImage(portraitResult.data.portrait, imageHeader, R.drawable.default_avatar);
                    if (!TextUtils.isEmpty(portraitResult.data.portrait)) {
                        UCUtils.getInstance().saveUserPortrait(portraitResult.data.portrait);
                    }
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
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case CommunityListAct.REQUEST_CODE_COMMUNITY: {
                Community community = (Community) data.getExtras().getSerializable(Community.TAG);
                if (community != null) {
                    SetDefCommunityParam param = new SetDefCommunityParam();
                    param.communityId = community.id;
                    Request.startRequest(param, community, ServiceMap.SETDEFAULTCOMMUNITY, mHandler, CANCELABLE);
                    tvCommunity.setText(community.toString2());
                }
                break;
            }
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
            bt.compress(Bitmap.CompressFormat.JPEG, 60, fos);
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

package net.lzzy.networkdisk.activitys;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.yalantis.ucrop.UCrop;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.models.DetailsUser;
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.BASE64Utils;
import net.lzzy.networkdisk.utils.ImgUtils;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.ToastUtils;
import net.lzzy.networkdisk.views.CustomPopWindow;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;

public class UserActivity extends BaseActivity {
    @BindView(R.id.activity_user_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_user_tv_name)
    TextView tv_name;
    @BindView(R.id.activity_user_tv_add)
    TextView tv_add;
    @BindView(R.id.activity_user_frame_img)
    FrameLayout frame_img;
    private DetailsUser detailsUser;


    @Override
    protected int setContentView() {
        return R.layout.activity_user;
    }

    @Override
    protected void init() {
        String[] perms = {"android.permission.CAMERA"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, 200);
        }


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (NetworkUtils.isConnected()) {
            getUserInfo();
        } else {
            SPUtils utils = new SPUtils(Constants.SP_USER);
            String json = utils.getString(Constants.JSON_SP_DETAILS);
            if (json != null && !json.equals("")) {
                json = BASE64Utils.decodeBase64(utils.getString(Constants.JSON_SP_DETAILS));
                detailsUser = JSON.parseObject(json, DetailsUser.class);
                tv_name.setText(detailsUser.getName());
                tv_add.setText(detailsUser.getRole());
            }
        }
    }


    private void getUserInfo() {
        final MaterialDialog dialog =
                new MaterialDialog.Builder(this)
                        .cancelable(false)
                        .title("温馨提示")
                        .content("正在获取信息！")
                        .progress(true, 0)
                        .progressIndeterminateStyle(false).show();
        SPUtils utils = new SPUtils(Constants.SP_USER);
        String email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));
        OkGo.<String>get(Constants.API_URL_GET_USER_INFO)
                .tag(Constants.API_URL_GET_USER_INFO)
                .params(Constants.API_EMAIL, email)
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        String res = response.body();
                        int code = 0;
                        String details = "";
                        try {
                            JSONObject object = new JSONObject(res);
                            code = (int) object.get(Constants.JSON_RESULTS_CODE);
                            details = object.getString(Constants.JSON_RESULTS_DETAILS_USER);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        switch (code) {
                            case 0:

                                break;
                            case -1:
                                ToastUtils.showShortToastSafe("登录已失效，请重新登录！");
                                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                                finish();
                                break;
                            case 1:
                                try {
                                    detailsUser = JSON.parseObject(details, DetailsUser.class);
                                    SPUtils spUtils = new SPUtils(Constants.SP_USER);
                                    spUtils.putString(Constants.JSON_SP_DETAILS, BASE64Utils.encodeBase64(JSON.toJSONString(detailsUser)));
                                    tv_name.setText(detailsUser.getName());
                                    tv_add.setText(detailsUser.getRole());
                                    setResult(87, new Intent().putExtra(Constants.INTENT_CAPACITY, "已用:" + AppUtils.getFormatSize(detailsUser.getWithSize()) + "/" + AppUtils.getFormatSize(detailsUser.getTotalSize())));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        dialog.dismiss();
                        Throwable throwable = response.getException();
                        if (throwable != null) {
                            if (throwable.getMessage().equals(Constants.ERROR_FAILED_TO_CONNECT)) {
                                ToastUtils.showShortToast("无法连接服务器");
                            } else if (throwable.getMessage().equals(Constants.ERROR_FAILED_TO_CONNECT_NO_RESPONSE)) {
                                ToastUtils.showShortToast("服务器未响应");
                            } else {
                                ToastUtils.showShortToast(response.getException().toString());
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 500);

                    }
                });
    }


    @OnClick({R.id.activity_user_frame_add, R.id.activity_user_frame_img, R.id.activity_user_frame_name, R.id.activity_user_frame_pass, R.id.activity_user_btn_exit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_user_frame_add:
                if (!detailsUser.getRole().equals("黄金会员")) {
                    addCapacity();
                } else {
                    ToastUtils.showShortToastSafe("暂未开放！");
                }
                break;
            case R.id.activity_user_frame_img:
                if (NetworkUtils.isConnected()) {

                    View parent = ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
                    @SuppressLint("InflateParams") View contentView = LayoutInflater.from(this).inflate(R.layout.pop_view, null);
                    CustomPopWindow pop = new CustomPopWindow.PopupWindowBuilder(this)
                            .setView(contentView)
                            .enableBackgroundDark(true) //弹出popWindow时，背景是否变暗
                            .setBgDarkAlpha(0.8f) // 控制亮度
                            .enableOutsideTouchableDissmiss(true)
                            .size(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)//显示大小
                            .create()
                            .showAtLocation(parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

                    onMenuClick(contentView, pop);
                } else {
                    ToastUtils.showShortToast(getString(R.string.network_none_hint));
                }
                break;
            case R.id.activity_user_frame_name:
                if (NetworkUtils.isConnected()) {
                    new MaterialDialog.Builder(this)
                            .stackingBehavior(
                                    StackingBehavior
                                            .ALWAYS)
                            .titleGravity(GravityEnum.CENTER)
                            .positiveColor(getResources().getColor(R.color.colorPrimary))
                            .btnStackedGravity(GravityEnum.CENTER)
                            .inputType(InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                                    | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                            .inputRange(1, 10)
                            .title("修改昵称")
                            .positiveText("确定")
                            .input("修改昵称", detailsUser.getName(),
                                    false, new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                            editName(input.toString());
                                        }
                                    })
                            .show();


                } else {
                    ToastUtils.showShortToast(getString(R.string.network_none_hint));
                }
                break;
            case R.id.activity_user_frame_pass:
                startActivity(new Intent(UserActivity.this, ModifyActivity.class));
                break;
            case R.id.activity_user_btn_exit:
                SPUtils utils = new SPUtils(Constants.SP_USER);
                String token = utils.getString(Constants.SP_TOKEN);
                String email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));
                if (NetworkUtils.isConnected()) {
                    OkGo.<String>post(Constants.API_URL_EXIT)
                            .tag(this + Constants.API_GET)
                            .headers(Constants.JSON_RESULTS_TOKEN, token)
                            .headers(Constants.API_EMAIL, email)
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(Response<String> response) {
                                    String res = response.body();
                                    if (!res.isEmpty() && !res.equals("null")) {
                                        int code = JSON.parseObject(res, new TypeReference<Map<String, Integer>>() {
                                        }).get(Constants.JSON_RESULTS_CODE);
                                        switch (code) {
                                            case 1:
                                                ToastUtils.showShortToast("服务器数据清除成功");
                                                break;
                                        }
                                    }

                                }

                            });
                }
                new SPUtils(Constants.SP_APP_STATE).clear();
                new SPUtils(Constants.SP_USER).clear();
                new SPUtils(Constants.SP_OK_GO_COOKIE).clear();
                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                break;
        }
    }

    private void onMenuClick(View contentView, final CustomPopWindow pop) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pop != null) {
                    pop.dissmiss();
                }
                switch (v.getId()) {
                    case R.id.pop_view_btn_camera:
                        if (Build.VERSION.SDK_INT >= 23) {//检查相机权限
                            ArrayList<String> permissions = new ArrayList<>();
                            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                permissions.add(Manifest.permission.CAMERA);
                            }
                            if (permissions.size() == 0) {//有权限,跳转
                                //打开相机-兼容7.0
                                ImgUtils.openCamera(UserActivity.this);
                            } else {
                                requestPermissions(permissions.toArray(new String[permissions.size()]), 200);
                            }
                        } else {
                            //打开相机-兼容7.0
                            ImgUtils.openCamera(UserActivity.this);
                        }
                        break;
                    case R.id.pop_view_btn_album:
                        ImgUtils.openAlbum(UserActivity.this);
                        break;
                    case R.id.pop_view_btn_close:
                        pop.dissmiss();
                        break;
                }
            }
        };
        contentView.findViewById(R.id.pop_view_btn_camera).setOnClickListener(listener);
        contentView.findViewById(R.id.pop_view_btn_album).setOnClickListener(listener);
        contentView.findViewById(R.id.pop_view_btn_close).setOnClickListener(listener);

    }


    private void editName(final String newName) {
        SPUtils spUtils = new SPUtils(Constants.SP_USER);
        String token = spUtils.getString(Constants.SP_TOKEN);
        String email = BASE64Utils.decodeBase64(spUtils.getString(Constants.SP_EMAIL));
        OkGo.<String>post(Constants.API_URL_EDIT_NAME)
                .tag(Constants.API_URL_EDIT_NAME)
                .headers(Constants.JSON_RESULTS_TOKEN, token)
                .headers(Constants.API_EMAIL, email)
                .params(Constants.API_NEW_NAME, newName)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        if (!NetworkUtils.isConnected()) {
                            ToastUtils.showShortToast(getString(R.string.network_none_hint));
                        }
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String res = response.body();
                        if (!res.isEmpty() && !res.equals("null")) {
                            int code = JSON.parseObject(res, new TypeReference<Map<String, Integer>>() {
                            }).get(Constants.JSON_RESULTS_CODE);
                            switch (code) {// 0失败，1成功,-1token不正确,
                                case 1:
                                    ToastUtils.showShortToastSafe("修改成功！");
                                    tv_name.setText(newName);

                                    SPUtils spUtils = new SPUtils(Constants.SP_USER);
                                    detailsUser.setName(newName);
                                    spUtils.putString(Constants.JSON_SP_DETAILS, BASE64Utils.encodeBase64(JSON.toJSONString(detailsUser)));
                                    setResult(88, new Intent().putExtra(Constants.INTENT_NAME, newName));
                                    break;
                                case 0:
                                case -1:
                                    ToastUtils.showShortToastSafe("修改失败！");
                                    break;
                            }
                        } else {
                            ToastUtils.showShortToastSafe("修改失败！");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);

                        Throwable throwable = response.getException();
                        if (throwable != null && throwable.getMessage().equals(Constants.ERROR_FAILED_TO_CONNECT)) {
                            ToastUtils.showShortToast("无法连接服务器");
                        } else if (throwable.getMessage().equals(Constants.ERROR_FAILED_TO_CONNECT_NO_RESPONSE)) {
                            ToastUtils.showShortToast("服务器未响应");
                        } else {
                            ToastUtils.showShortToast(response.getException().toString());
                        }

                    }

                });
    }

    private void addCapacity() {
        if (NetworkUtils.isConnected()) {
            SPUtils spUtils = new SPUtils(Constants.SP_USER);
            String token = spUtils.getString(Constants.SP_TOKEN);
            String email = BASE64Utils.decodeBase64(spUtils.getString(Constants.SP_EMAIL));
            OkGo.<String>post(Constants.API_URL_ADD_CAPACITY)
                    .tag(Constants.API_URL_ADD_CAPACITY)
                    .headers(Constants.JSON_RESULTS_TOKEN, token)
                    .headers(Constants.API_EMAIL, email)
                    .execute(new StringCallback() {
                        @Override
                        public void onStart(Request<String, ? extends Request> request) {
                            super.onStart(request);
                            if (!NetworkUtils.isConnected()) {
                                ToastUtils.showShortToastSafe("网络异常，请检查网络！");
                            }
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            String res = response.body();
                            if (!res.isEmpty() && !res.equals("null")) {
                                int code = JSON.parseObject(res, new TypeReference<Map<String, Integer>>() {
                                }).get(Constants.JSON_RESULTS_CODE);
                                switch (code) {// 0失败，1成功,-1token不正确,
                                    case 1:
                                        ToastUtils.showShortToastSafe("扩容成功！");

                                        getUserInfo();
                                        break;
                                    case 0:
                                    case -1:
                                        ToastUtils.showShortToastSafe("扩容失败！");
                                        break;
                                }
                            } else {
                                ToastUtils.showShortToastSafe("扩容失败！");
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);

                            Throwable throwable = response.getException();
                            if (throwable != null && throwable.getMessage().equals(Constants.ERROR_FAILED_TO_CONNECT)) {
                                ToastUtils.showShortToast("无法连接服务器");
                            } else if (throwable.getMessage().equals(Constants.ERROR_FAILED_TO_CONNECT_NO_RESPONSE)) {
                                ToastUtils.showShortToast("服务器未响应");
                            } else {
                                ToastUtils.showShortToast(response.getException().toString());
                            }

                        }

                    });
        } else {
            ToastUtils.showShortToastSafe("网络异常，请检查网络！");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ImgUtils.TAKE_PHOTO:
                    ImgUtils.startUCrop(UserActivity.this, ImgUtils.imageUri);
                    break;
                case ImgUtils.CHOOSE_PHOTO:
                    try {
                        if (data != null) {
                            Uri uri = data.getData();
                            //相册返回图片，调用裁剪的方法
                            ImgUtils.startUCrop(UserActivity.this, uri);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showShortToast("图片选择失败");
                    }

                    break;
                case UCrop.REQUEST_CROP:
                    try {
                        final Uri resultUri = UCrop.getOutput(data);

                        String downloadsDirectoryPath = AppUtils.getTmpPath();
                        SPUtils spUtils = new SPUtils(Constants.SP_USER);
                        String token = spUtils.getString(Constants.SP_TOKEN);
                        String email = BASE64Utils.decodeBase64(spUtils.getString(Constants.SP_EMAIL));
                        String filename = email + ".png";


                        final File saveFile = new File(downloadsDirectoryPath, filename);
                        FileInputStream inStream = new FileInputStream(new File(resultUri.getPath()));
                        FileOutputStream outStream = new FileOutputStream(saveFile);
                        FileChannel inChannel = inStream.getChannel();
                        FileChannel outChannel = outStream.getChannel();
                        inChannel.transferTo(0, inChannel.size(), outChannel);
                        inStream.close();
                        outStream.close();
                        UserFile userFile = new UserFile();
                        userFile.setName(saveFile.getName());
                        userFile.setPath(saveFile.getPath());


                        final MaterialDialog dialog =
                                new MaterialDialog.Builder(this)
                                        .cancelable(false)
                                        .title("提示")
                                        .content("正在上传")
                                        .progress(true, 0)
                                        .progressIndeterminateStyle(false).show();
                        OkGo.<String>post(Constants.API_URL_UPLOAD)
                                .tag(Constants.API_URL_UPLOAD)
                                .headers(Constants.JSON_RESULTS_TOKEN, token)
                                .headers(Constants.API_EMAIL, email)
                                .params(Constants.API_SHORT_PATH, "img\\")
                                .params("fileKey", saveFile)
                                .execute(new StringCallback() {
                                    @Override
                                    public void onStart(Request<String, ? extends Request> request) {
                                        super.onStart(request);
                                    }

                                    @Override
                                    public void onSuccess(Response<String> response) {
                                        String res = response.body();
                                        if (!res.isEmpty() && !res.equals("null")) {
                                            int code = JSON.parseObject(res, new TypeReference<Map<String, Integer>>() {
                                            }).get(Constants.JSON_RESULTS_CODE);
                                            switch (code) {// 0失败，1成功,-1token不正确,
                                                case 1:
                                                    ToastUtils.showShortToast("修改头像成功");
                                                    SPUtils spUtils = new SPUtils(Constants.SP_APP_STATE);
                                                    spUtils.putString(Constants.SP_IMG_KEY, UUID.randomUUID().toString());
                                                    Intent intent = new Intent();
                                                    intent.putExtra(Constants.INTENT_URI, resultUri);
                                                    intent.setData(resultUri);
                                                    setResult(86, intent);
                                                    break;
                                                case 0:
                                                    ToastUtils.showShortToast("修改头像失败");
                                                    break;
                                                case -1:
                                                    startActivity(new Intent(UserActivity.this, LoginActivity.class));
                                                    break;
                                            }
                                        } else {
                                            ToastUtils.showShortToast("修改头像失败");
                                        }

                                    }

                                    @Override
                                    public void onFinish() {
                                        super.onFinish();
                                        dialog.dismiss();
                                        saveFile.delete();
                                    }

                                    @Override
                                    public void onError(Response<String> response) {
                                        super.onError(response);
                                        dialog.dismiss();
                                        saveFile.delete();
                                    }
                                });


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
            }

        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {

                ToastUtils.showShortToast(cropError.getMessage());
            } else {
                ToastUtils.showShortToast("unexpected_error");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkGo.getInstance().cancelTag(Constants.API_URL_GET_USER_INFO);
        OkGo.getInstance().cancelTag(Constants.API_URL_EDIT_NAME);
        OkGo.getInstance().cancelTag(Constants.API_URL_ADD_CAPACITY);
        OkGo.getInstance().cancelTag(Constants.API_URL_UPLOAD);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {
            case 200:
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!cameraAccepted) {
                    ToastUtils.showShortToast("未获取拍照权限将无法打开照相机");
                }
                break;
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
}

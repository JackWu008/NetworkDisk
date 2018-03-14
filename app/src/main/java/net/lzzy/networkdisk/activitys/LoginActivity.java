package net.lzzy.networkdisk.activitys;


import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.BASE64Utils;
import net.lzzy.networkdisk.utils.EncryptUtils;
import net.lzzy.networkdisk.utils.KeyBoardUtil;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.activity_login_til_email)
    TextInputLayout til_email;
    @BindView(R.id.activity_login_til_password)
    TextInputLayout til_password;
    @BindView(R.id.activity_login_edt_email)
    EditText edt_email;
    @BindView(R.id.activity_login_btn_login)
    Button btn_login;
    @BindView(R.id.activity_login_edt_password)
    EditText edt_password;
    private boolean isEmail = false;
    private boolean isPassword = false;
    private long backPressed = 0;

    @Override
    protected int setContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void init() {
        String[] perms = {"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "android.permission.CHANGE_WIFI_STATE"};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, 201);
        }

        KeyBoardUtil.openKeybord(edt_email, this);
        edt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString().trim()).matches()) {
                    til_email.setError("请输入正确的邮箱");
                    isEmail = false;
                } else {
                    isEmail = true;
                    til_email.setError("");
                }
                check();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() < 6) {
                    til_password.setError("密码的最小长度为6");
                    isPassword = false;
                } else {
                    til_password.setError("");
                    isPassword = true;
                }
                check();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void check() {
        if (isEmail && isPassword) {
            btn_login.setEnabled(true);
            btn_login.setBackgroundResource(R.drawable.btn_bg_activate);
        } else {
            btn_login.setEnabled(false);
            btn_login.setBackgroundResource(R.drawable.btn_bg);
        }
    }


    @OnClick({R.id.activity_login_tv_find_password, R.id.activity_login_tv_add_account, R.id.activity_login_btn_login})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_login_tv_add_account:
                startActivity(new Intent(LoginActivity.this, AccountActivity.class));
                break;
            case R.id.activity_login_tv_find_password:
                startActivity(new Intent(LoginActivity.this, FindPasswordActivity.class));
                break;
            case R.id.activity_login_btn_login:
                btn_login.setEnabled(false);
                btn_login.setBackgroundResource(R.drawable.btn_bg);
                final MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                        .cancelable(false)
                        .contentGravity(GravityEnum.CENTER)
                        .content("登录中")
                        .progress(true, 0)
                        .progressIndeterminateStyle(true);
                final MaterialDialog dialog = builder.build();

                if (NetworkUtils.isConnected()) {
                    OkGo.<String>post(Constants.API_URL_LOGIN)
                            .tag(this)
                            .params(Constants.API_EMAIL, edt_email.getText().toString())
                            .params(Constants.API_PASSWORD, EncryptUtils.encryptMD5ToString(edt_password.getText().toString()))
                            .execute(new StringCallback() {
                                @Override
                                public void onStart(Request<String, ? extends Request> request) {
                                    super.onStart(request);
                                    KeyBoardUtil.closeKeybord(LoginActivity.this);
                                    dialog.show();
                                }

                                @Override
                                public void onSuccess(Response<String> response) {
                                    String res = response.body();
                                    if (!res.isEmpty() && !res.equals("null")) {
                                        int code = -2;
                                        String token = "";
                                        //String details = "";
                                        try {
                                            JSONObject jsonObject = new JSONObject(res);
                                            code = (int) jsonObject.get(Constants.JSON_RESULTS_CODE);
                                            token = (String) jsonObject.get(Constants.JSON_RESULTS_TOKEN);
                                            //details = jsonObject.getString(Constants.JSON_RESULTS_DETAILS_USER);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        switch (code) {// 0失败，1成功,2邮箱或密码不正确，
                                            case -2:
                                            case 0:
                                                btn_login.setEnabled(true);
                                                btn_login.setBackgroundResource(R.drawable.btn_bg_activate);
                                                dialog.dismiss();
                                                ToastUtils.showShortToast("登录失败");
                                                break;
                                            case 1:
                                                ToastUtils.showShortToast("登录成功");
                                                btn_login.setEnabled(false);
                                                Log.i("safsdffasdtoken", "onSuccess:dsf " + res);
                                                Log.i("safsdffasdtoken", "onSuccess: " + token);
                                                if (!token.isEmpty() && !token.equals("null")) {
                                                    SPUtils spUtils = new SPUtils(Constants.SP_USER);
                                                    spUtils.putString(Constants.SP_TOKEN, token);
                                                    spUtils.putString(Constants.SP_EMAIL, BASE64Utils.encodeBase64(edt_email.getText().toString()));
                                                }
                                                dialog.dismiss();
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                finish();
                                                break;
                                            case 2:
                                                btn_login.setEnabled(true);
                                                btn_login.setBackgroundResource(R.drawable.btn_bg_activate);
                                                dialog.dismiss();
                                                ToastUtils.showShortToast("邮箱或密码不正确");
                                                break;
                                        }
                                    } else {
                                        btn_login.setEnabled(true);
                                        btn_login.setBackgroundResource(R.drawable.btn_bg_activate);
                                        dialog.dismiss();
                                        ToastUtils.showShortToast("未知错误,请稍后再试");
                                    }
                                }


                                @Override
                                public void onError(Response<String> response) {
                                    super.onError(response);
                                    dialog.dismiss();
                                    btn_login.setEnabled(true);
                                    btn_login.setBackgroundResource(R.drawable.btn_bg_activate);
                                    Log.i("safsdffasdtoken", "onError:dsf " + response.getException());
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

                            });
                } else {
                    ToastUtils.showShortToast(getString(R.string.network_none_hint));
                    btn_login.setEnabled(true);
                    dialog.dismiss();
                    btn_login.setBackgroundResource(R.drawable.btn_bg_activate);
                }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - backPressed < 2000) {
            OkGo.getInstance().cancelTag(Constants.API_URL_LOGIN);
            ((AppUtils) getApplication()).allFinishActivity();
        } else {
            ToastUtils.showShortToastSafe("再按一次退出程序");
        }
        backPressed = currentTime;
    }

    @Override
    protected void onPause() {
        super.onPause();
        KeyBoardUtil.closeKeybord(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 201:
//                boolean cameraAccepted = grantResults[0]== PackageManager.IN;
//                if(cameraAccepted){
//                    //授权成功之后，调用系统相机进行拍照操作等
//                }else{
//                    //用户授权拒绝之后，友情提示一下就可以了
//                }
                break;
        }
    }
}

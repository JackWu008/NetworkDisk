package net.lzzy.networkdisk.activitys;


import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.utils.EncryptUtils;
import net.lzzy.networkdisk.utils.KeyBoardUtil;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.TimeCountUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class FindPasswordActivity extends BaseActivity {
    @BindView(R.id.activity_find_pass_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_find_pass_edt_new_one)
    TextInputEditText edt_new_one;
    @BindView(R.id.activity_find_pass_til_new_one)
    TextInputLayout til_new_one;
    @BindView(R.id.activity_find_pass_edt_new_two)
    TextInputEditText edt_new_two;
    @BindView(R.id.activity_find_pass_til_new_two)
    TextInputLayout til_new_two;
    @BindView(R.id.activity_find_pass_til_email)
    TextInputLayout til_email;
    @BindView(R.id.activity_find_pass_btn_get_check_code)
    Button btn_get_code;
    @BindView(R.id.activity_find_pass_edt_check_code)
    TextInputEditText  edt_check_code;
    @BindView(R.id.activity_find_pass_til_check_code)
    TextInputLayout til_check_code;
    @BindView(R.id.activity_find_pass_btn_modify)
    Button btn_modify;
    @BindView(R.id.activity_find_pass_edt_email)
    TextInputEditText edt_email;

    private boolean isEmail = false;
    private boolean isCode = false;

    private boolean isNewOne = false;
    private boolean isNewTwo = false;


    @Override
    protected int setContentView() {
        return R.layout.activity_find_pass;
    }

    @Override
    protected void init() {
        KeyBoardUtil.openKeybord(edt_new_one, this);
        edt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s.toString().trim()).matches()) {
                    til_email.setError("请输入正确的邮箱");
                    isEmail = false;
                    btn_get_code.setEnabled(false);
                    btn_get_code.setBackgroundResource(R.drawable.btn_bg);
                } else {
                    isEmail = true;
                    til_email.setError("");
                    btn_get_code.setEnabled(true);
                    btn_get_code.setBackgroundResource(R.drawable.btn_bg_activate);
                }
                check();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edt_check_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (s.length() == 4) {
                        til_check_code.setError("");
                        isCode = true;
                    } else {
                        til_check_code.setError("验证码输入不正确");
                        isCode = false;
                    }
                } else {
                    til_check_code.setError("验证码不能为空");
                    isCode = false;
                }
                check();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_new_one.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    til_new_one.setError("密码的最小长度为6");
                    isNewOne = false;
                } else {
                    til_new_one.setError("");
                    isNewOne = true;
                    String pass1 = edt_new_one.getText().toString();
                    String pass2 = edt_new_two.getText().toString();

                    if (!pass1.equals(pass2)) {
                        til_new_two.setError("两次输入的密码不一致");
                        isNewTwo = false;
                    }

                }
                check();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edt_new_two.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    til_new_two.setError("密码的最小长度为6");
                    isNewTwo = false;
                } else {
                    String pass1 = edt_new_one.getText().toString();
                    String pass2 = edt_new_two.getText().toString();

                    if (pass1.equals(pass2)) {
                        til_new_two.setError("");
                        isNewTwo = true;
                    } else {
                        til_new_two.setError("两次输入的密码不一致");
                        isNewTwo = false;
                    }
                }

                check();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }


    @OnClick({R.id.activity_find_pass_btn_get_check_code, R.id.activity_find_pass_btn_modify})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.activity_find_pass_btn_get_check_code:
                if (isEmail) {
                    btn_get_code.setEnabled(false);
                    btn_get_code.setBackgroundResource(R.drawable.btn_bg);
                    edt_email.setEnabled(false);
                    if (NetworkUtils.isConnected()) {
                        OkGo.<String>get(Constants.API_URL_SEND_EMAIl)
                                .tag(Constants.API_URL_SEND_EMAIl)
                                .params(Constants.API_EMAIL, edt_email.getText().toString())
                                .params(Constants.API_IS_MODIFY, true)
                                .execute(new StringCallback() {
                                    @Override
                                    public void onSuccess(Response<String> response) {
                                        String res = response.body();
                                        if (!res.isEmpty() && !res.equals("null")) {
                                            int code = JSON.parseObject(res, new TypeReference<Map<String, Integer>>() {
                                            }).get(Constants.JSON_RESULTS_CODE);
                                            switch (code) {
                                                case 0:
                                                    ToastUtils.showShortToast("发送失败");
                                                    btn_get_code.setEnabled(true);
                                                    btn_get_code.setBackgroundResource(R.drawable.btn_bg_activate);
                                                    edt_email.setEnabled(true);
                                                    break;
                                                case 1:
                                                    ToastUtils.showShortToast("发送成功");
                                                    new TimeCountUtils(30000, btn_get_code, edt_email).start();
                                                    break;
                                                case 3:
                                                    ToastUtils.showShortToast("此邮箱未注册");
                                                    btn_get_code.setEnabled(true);
                                                    edt_email.setEnabled(true);
                                                    btn_get_code.setBackgroundResource(R.drawable.btn_bg_activate);
                                                    break;
                                            }
                                        } else {
                                            ToastUtils.showShortToast("出错了");
                                            btn_get_code.setEnabled(true);
                                            btn_get_code.setBackgroundResource(R.drawable.btn_bg_activate);
                                        }
                                    }

                                    @Override
                                    public void onError(Response<String> response) {
                                        super.onError(response);
                                        btn_get_code.setEnabled(true);
                                        edt_email.setEnabled(true);
                                        btn_get_code.setBackgroundResource(R.drawable.btn_bg_activate);
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
                    } else{
                        btn_get_code.setEnabled(true);
                        btn_get_code.setBackgroundResource(R.drawable.btn_bg_activate);
                        ToastUtils.showShortToast("请开启网络后再试");
                    }

                } else
                    til_email.setError("请输入正确的邮箱");
                break;
            case R.id.activity_find_pass_btn_modify:
                if (isEmail && isCode && isNewOne && isNewTwo) {
                    final MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                            .cancelable(false)
                            .contentGravity(GravityEnum.CENTER)
                            .content("修改中")
                            .progress(true, 0)
                            .progressIndeterminateStyle(true);
                    final MaterialDialog dialog = builder.build();
                    if (NetworkUtils.isConnected()) {

                        OkGo.<String>post(Constants.API_URL_FIND_PASSWORD)
                                .tag(Constants.API_URL_FIND_PASSWORD)
                                .params(Constants.API_EMAIL, edt_email.getText().toString())
                                .params(Constants.API_CODE, edt_check_code.getText().toString())
                                .params(Constants.API_NEW_PASSWORD, EncryptUtils.encryptMD5ToString(edt_new_two.getText().toString()))
                                .execute(new StringCallback() {
                                    @Override
                                    public void onStart(Request<String, ? extends Request> request) {
                                        super.onStart(request);
                                        KeyBoardUtil.closeKeybord(FindPasswordActivity.this);
                                        dialog.show();
                                    }

                                    @Override
                                    public void onSuccess(Response<String> response) {
// 0失败，1成功,2验证码不正确,3旧密码错误
                                        String res = response.body();
                                        if (!res.isEmpty() && !res.equals("null")) {
                                            int code = JSON.parseObject(res, new TypeReference<Map<String, Integer>>() {
                                            }).get(Constants.JSON_RESULTS_CODE);
                                            switch (code) {
                                                case 0:
                                                    dialog.dismiss();
                                                    ToastUtils.showShortToast("修改密码失败");
                                                    break;
                                                case 1:
                                                    ToastUtils.showShortToast("修改密码成功");
                                                    new SPUtils(Constants.SP_USER).clear();
                                                    new SPUtils(Constants.SP_APP_STATE).clear();

                                                    startActivity(new Intent(FindPasswordActivity.this, LoginActivity.class));
                                                    finish();
                                                    break;
                                                case 2:
                                                    ToastUtils.showShortToast("验证码不正确");
                                                    dialog.dismiss();
                                                    break;
                                                case 3:
                                                    ToastUtils.showShortToast("旧密码错误");
                                                    dialog.dismiss();
                                                    break;
                                            }
                                        } else {
                                            dialog.dismiss();
                                            ToastUtils.showShortToast("未知错误,请稍后再试");
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
                                });
                    } else
                        ToastUtils.showShortToast(getString(R.string.network_none_hint));
                }
                break;
        }
    }

    private void check() {
        if (isEmail && isCode && isNewOne && isNewTwo) {
            btn_modify.setEnabled(true);
            btn_modify.setBackgroundResource(R.drawable.btn_bg_activate);
        } else {
            btn_modify.setEnabled(false);
            btn_modify.setBackgroundResource(R.drawable.btn_bg);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        KeyBoardUtil.closeKeybord(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkGo.getInstance().cancelTag(Constants.API_URL_MODIFY_PASSWORD);
        OkGo.getInstance().cancelTag(Constants.API_URL_SEND_EMAIl);
    }


}

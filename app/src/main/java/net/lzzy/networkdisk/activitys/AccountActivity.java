package net.lzzy.networkdisk.activitys;

import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import net.lzzy.networkdisk.utils.TimeCountUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class AccountActivity extends BaseActivity {
    @BindView(R.id.activity_account_edt_name)
    EditText edt_name;
    @BindView(R.id.activity_account_til_name)
    TextInputLayout til_name;
    @BindView(R.id.activity_account_edt_password)
    EditText edt_password;
    @BindView(R.id.activity_account_til_password)
    TextInputLayout til_password;
    @BindView(R.id.activity_account_edt_ok_password)
    EditText edt_ok_password;
    @BindView(R.id.activity_account_til_ok_password)
    TextInputLayout til_ok_password;
    @BindView(R.id.activity_account_edt_email)
    EditText edt_email;
    @BindView(R.id.activity_account_til_email)
    TextInputLayout til_email;
    @BindView(R.id.activity_account_btn_get_check_code)
    Button btn_get_check_code;
    @BindView(R.id.activity_account_edt_check_code)
    EditText edt_check_code;
    @BindView(R.id.activity_account_til_check_code)
    TextInputLayout til_check_code;
    @BindView(R.id.activity_account_btn_account)
    Button btn_account;
    private boolean isEmail = false;
    private boolean isPassword = false;
    private boolean isOkPassword = false;
    private boolean isName = false;
    private boolean isCode = false;
    @BindView(R.id.activity_account_toolbar)
    Toolbar toolbar;

    @Override
    protected int setContentView() {
        return R.layout.activity_account;
    }

    @Override
    protected void init() {

        KeyBoardUtil.openKeybord(edt_name, this);

        edt_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    til_name.setError("");
                    isName = true;
                } else {
                    til_name.setError("昵称不能为空");
                    isName = false;
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
                    String pass1 = edt_password.getText().toString();
                    String pass2 = edt_ok_password.getText().toString();
                    if (pass1.equals(pass2)) {
                        til_ok_password.setError("");
                        isOkPassword = true;
                    } else {
                        til_ok_password.setError("两次输入的密码不一致");
                        isOkPassword = false;
                    }
                }
                check();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edt_ok_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    til_ok_password.setError("密码的最小长度为6");
                    isOkPassword = false;
                } else {
                    String pass1 = edt_password.getText().toString();
                    String pass2 = edt_ok_password.getText().toString();
                    if (pass1.equals(pass2)) {
                        til_ok_password.setError("");
                        isOkPassword = true;
                    } else {
                        til_ok_password.setError("两次输入的密码不一致");
                        isOkPassword = false;
                    }
                }
                check();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString().trim()).matches()) {
                    til_email.setError("请输入正确的邮箱");
                    isEmail = false;
                    btn_get_check_code.setEnabled(false);
                    btn_get_check_code.setBackgroundResource(R.drawable.btn_bg);
                } else {
                    isEmail = true;
                    til_email.setError("");
                    btn_get_check_code.setEnabled(true);
                    btn_get_check_code.setBackgroundResource(R.drawable.btn_bg_activate);
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    @OnClick({R.id.activity_account_btn_get_check_code, R.id.activity_account_btn_account})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_account_btn_get_check_code:
                if (isEmail) {
                    btn_get_check_code.setEnabled(false);
                    btn_get_check_code.setBackgroundResource(R.drawable.btn_bg);
                    edt_email.setEnabled(false);

                    if (NetworkUtils.isConnected()) {
                        OkGo.<String>get(Constants.API_URL_SEND_EMAIl)
                                .tag(this+Constants.API_URL_SEND_EMAIl)
                                .params(Constants.API_EMAIL, edt_email.getText().toString())
                                .params(Constants.API_IS_MODIFY, false)
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
                                                    btn_get_check_code.setEnabled(true);
                                                    btn_get_check_code.setBackgroundResource(R.drawable.btn_bg_activate);
                                                    edt_email.setEnabled(true);
                                                    break;
                                                case 1:
                                                    ToastUtils.showShortToast("发送成功");
                                                    new TimeCountUtils(30000, btn_get_check_code, edt_email).start();
                                                    break;
                                                case 2:
                                                    ToastUtils.showShortToast("此邮箱已被注册");
                                                    btn_get_check_code.setEnabled(true);
                                                    edt_email.setEnabled(true);
                                                    btn_get_check_code.setBackgroundResource(R.drawable.btn_bg_activate);
                                                    break;
                                            }
                                        } else {
                                            btn_get_check_code.setEnabled(true);
                                            edt_email.setEnabled(true);
                                            btn_get_check_code.setBackgroundResource(R.drawable.btn_bg_activate);
                                            ToastUtils.showShortToast("未知错误,请稍后再试");
                                        }
                                    }

                                    @Override
                                    public void onError(Response<String> response) {
                                        super.onError(response);
                                        btn_get_check_code.setEnabled(true);
                                        edt_email.setEnabled(true);
                                        btn_get_check_code.setBackgroundResource(R.drawable.btn_bg_activate);
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
                        ToastUtils.showShortToast("请开启网络后再试");
                        btn_get_check_code.setEnabled(true);
                        edt_email.setEnabled(true);
                        btn_get_check_code.setBackgroundResource(R.drawable.btn_bg_activate);
                    }

                } else
                    til_email.setError("请输入正确的邮箱");
                break;
            case R.id.activity_account_btn_account:
                final MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                        .cancelable(false)
                        .contentGravity(GravityEnum.CENTER)
                        .content("注册中")
                        .progress(true, 0)
                        .progressIndeterminateStyle(true);
                final MaterialDialog dialog = builder.build();
                if (NetworkUtils.isConnected()) {
                    OkGo.<String>post(Constants.API_URL_REGISTERED)
                            .tag(this+Constants.API_URL_REGISTERED)
                            .params(Constants.API_NAME, edt_name.getText().toString())
                            .params(Constants.API_PASSWORD, EncryptUtils.encryptMD5ToString(edt_ok_password.getText().toString()))
                            .params(Constants.API_EMAIL, edt_email.getText().toString())
                            .params(Constants.API_CODE, edt_check_code.getText().toString())
                            .execute(new StringCallback() {
                                @Override
                                public void onStart(Request<String, ? extends Request> request) {
                                    super.onStart(request);
                                    KeyBoardUtil.closeKeybord(AccountActivity.this);
                                    dialog.show();
                                }

                                @Override
                                public void onSuccess(Response<String> response) {
                                    String res = response.body();
                                    if (!res.isEmpty() && !res.equals("null")) {
                                        int code = JSON.parseObject(res, new TypeReference<Map<String, Integer>>() {
                                        }).get(Constants.JSON_RESULTS_CODE);
                                        switch (code) {
                                            case 0:
                                                dialog.dismiss();
                                                ToastUtils.showShortToast("注册失败");
                                                break;
                                            case 1:
                                                dialog.dismiss();
                                                ToastUtils.showShortToast("注册成功");
                                                finish();
                                                break;
                                            case 2:
                                                dialog.dismiss();
                                                ToastUtils.showShortToast("验证码不正确");

                                                break;
                                            case 3:
                                                dialog.dismiss();
                                                ToastUtils.showShortToast("此邮箱已被注册");
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
                    ToastUtils.showShortToastSafe(getString(R.string.network_none_hint));
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        KeyBoardUtil.closeKeybord(this);
    }


    private void check() {
        if (isEmail && isPassword && isCode && isName && isOkPassword) {
            btn_account.setEnabled(true);
            btn_account.setBackgroundResource(R.drawable.btn_bg_activate);
        } else {
            btn_account.setEnabled(false);
            btn_account.setBackgroundResource(R.drawable.btn_bg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkGo.getInstance().cancelTag(this+Constants.API_URL_SEND_EMAIl);
        OkGo.getInstance().cancelTag(this+Constants.API_URL_REGISTERED);
    }
}

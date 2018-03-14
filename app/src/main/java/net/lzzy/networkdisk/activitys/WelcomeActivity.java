package net.lzzy.networkdisk.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.BASE64Utils;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import java.util.Map;

import butterknife.ButterKnife;


public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        ((AppUtils) getApplication()).addActivity(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final SPUtils utils = new SPUtils(Constants.SP_USER);
                String token = utils.getString(Constants.SP_TOKEN);
                String email = utils.getString(Constants.SP_EMAIL);
                Log.i("Jajfslddjf", "run: " + email);
                if (token != null && email != null && !token.isEmpty() && !token.equals("null") && !email.isEmpty() && !email.equals("null")) {
                    email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));
                    if (NetworkUtils.isConnected()) {
                        OkGo.<String>post(Constants.API_URL_LOGIN_TOKEN)
                                .tag(this)
                                .headers(Constants.JSON_RESULTS_TOKEN, token)
                                .headers(Constants.API_EMAIL, email)
                                .execute(new StringCallback() {
                                    @Override
                                    public void onSuccess(Response<String> response) {
                                        String res = response.body();
                                        if (!res.isEmpty() && !res.equals("null")) {
                                            int code = JSON.parseObject(res, new TypeReference<Map<String, Integer>>() {
                                            }).get(Constants.JSON_RESULTS_CODE);
                                            switch (code) {// 0失败，1成功,-1token不正确,
                                                case 1:
                                                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                                                    finish();
                                                    break;
                                                case 0:
                                                case -1:
                                                default:
                                                    startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                                                    clearSP();
                                                    finish();
                                                    break;
                                            }
                                        } else {
                                            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                                            clearSP();
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onError(Response<String> response) {
                                        super.onError(response);
                                        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                                        clearSP();
                                        finish();
                                    }
                                });
                    } else {
                        ToastUtils.showShortToast(getString(R.string.network_none_hint));
                        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                        clearSP();
                        finish();
                    }
                } else {
                    startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                    clearSP();
                    finish();
                }

            }
        }, 1000);
    }


    /**
     * 屏蔽物理返回按钮
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    private void clearSP() {
        new SPUtils(Constants.SP_USER).clear();
        new SPUtils(Constants.SP_APP_STATE).clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkGo.getInstance().cancelTag(this);
        ((AppUtils) getApplication()).removeActivity(this);
    }
}

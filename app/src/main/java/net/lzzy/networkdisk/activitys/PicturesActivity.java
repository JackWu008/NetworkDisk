package net.lzzy.networkdisk.activitys;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.base.Request;
import com.lzy.okserver.OkDownload;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.adapter.PicturesAdapter;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.log.LogDownloadListener;
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.BASE64Utils;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class PicturesActivity extends BaseActivity {
    @BindView(R.id.activity_pictures_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_pictures_vp)
    ViewPager viewPager;
    private List<UserFile> files = new ArrayList<>();
    private PagerAdapter adapter;
    private UserFile userFile;
    private String tag;

    @Override
    protected int setContentView() {
        return R.layout.activity_pictures;
    }

    @Override
    protected void init() {
        setSupportActionBar(toolbar);
        boolean isToPicture = getIntent().getBooleanExtra(Constants.INTENT_IS_PICTURE_TO, false);
        int pos = getIntent().getIntExtra(Constants.INTENT_PICTURE_POSITION, 0);
        if (isToPicture) {
            get();
        } else {
            String json = getIntent().getStringExtra(Constants.INTENT_PICTURE_USERFILE_JSON);
            userFile = JSON.parseObject(json, UserFile.class);
            files.add(userFile);
        }

        adapter = new PicturesAdapter(this, files);


        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(pos);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void get() {
        SPUtils utils = new SPUtils(Constants.SP_USER);
        String token = utils.getString(Constants.SP_TOKEN);
        String email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));
        OkGo.<String>get(Constants.API_URL_GET_TYPE)
                .tag(this + Constants.API_URL_GET_TYPE)
                .headers(Constants.JSON_RESULTS_TOKEN, token)
                .headers(Constants.API_EMAIL, email)
                .params(Constants.API_KEY, 0)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        networkDetection();
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        files.clear();
                        String res = response.body();
                        int code = 0;
                        try {
                            JSONObject object = new JSONObject(res);
                            code = (int) object.get(Constants.JSON_RESULTS_CODE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        switch (code) {
                            case 0:

                                ToastUtils.showShortToastSafe("获取失败");
                                break;
                            case 2:

                                //ToastUtils.showShortToastSafe("此类型的文件为空");
                                break;
                            case -1:
                                ToastUtils.showShortToastSafe("登录已失效，请重新登录！");
                                startActivity(new Intent(PicturesActivity.this, LoginActivity.class));

                                break;
                            case 1:
                                String json = "";
                                try {
                                    JSONArray array = (JSONArray) new JSONObject(res).get(Constants.JSON_FILE_INFO);
                                    json = array.toString();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                files.addAll(JSON.parseArray(json, UserFile.class));
                                break;
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();


                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);

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
    }

    private void networkDetection() {
        if (!NetworkUtils.isConnected()) {
            ToastUtils.showShortToast(getString(R.string.network_none_hint));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pictures, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pictures_dow:
                if (NetworkUtils.isConnected()) {
                    if (NetworkUtils.isWifiConnected()) {
                        download();
                    } else {
                        new MaterialDialog.Builder(PicturesActivity.this)
                                .titleGravity(GravityEnum.CENTER)
                                .positiveColor(getResources().getColor(R.color.colorPrimary))
                                .title("温馨提示")
                                .content(getString(R.string.network_non_wifi_hint))
                                .positiveText("确定")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        download();
                                    }
                                }).show();
                    }
                } else {
                    ToastUtils.showShortToastSafe(getString(R.string.network_none_hint));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void download() {
       // if (OkDownload.getInstance().getTask(userFile.getUrl()) == null) {
            long dowTotalSize = AppUtils.toLong(userFile.getFileSize());
            long phoneSurplusSize = AppUtils.toLong(AppUtils.getSDAvailableSize());
            if (dowTotalSize < phoneSurplusSize) {
                SPUtils spUtils = new SPUtils(Constants.SP_USER);
                String email = BASE64Utils.decodeBase64(spUtils.getString(Constants.SP_EMAIL));
                if (userFile.isFile()) {
                    tag = userFile.getUrl();
                    Log.i("LogDownloadListener", "onClick: url" + Constants.API_URL_DOWNLOAD + "//" + userFile.getPath());
                    Log.i("LogDownloadListener", "onClick: path" + userFile.getPath());
                    GetRequest<File> request = OkGo.<File>get(Constants.API_URL_DOWNLOAD)//
                            .tag(tag)
                            .params(Constants.API_SHORT_PATH, email.concat("\\" + userFile.getPath()));
                    Log.i("LogDownloadListener", "onClick: path" + userFile.getPath());
                    Log.i("LogDownloadListener", "onClick: urla" + Constants.API_URL_DOWNLOAD + "\\" + userFile.getPath());
                    Log.i("LogDownloadListener", "onClick: urln" + userFile.getUrl());
                    //这里第一个参数是tag，代表下载任务的唯一标识，传任意字符串都行，需要保证唯一,我这里用url作为了tag
                    OkDownload.request(tag, request)
                            .extra1(userFile)
                            .folder(Constants.FILE_DOWNLOAD_PATH)
                            .register(new LogDownloadListener())
                            .save()
                            .start();
                    ToastUtils.showShortToast("已添加下载");
                }
            } else
                ToastUtils.showShortToast("SD卡空间不足");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkGo.getInstance().cancelTag(this + Constants.API_URL_GET_TYPE);
    }
}

package net.lzzy.networkdisk.activitys;


import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.base.Request;
import com.lzy.okserver.OkDownload;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.log.LogDownloadListener;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.ClipboardUtils;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.activity_about_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_about_tv_version)
    TextView tv_version;


    @Override
    protected int setContentView() {
        return R.layout.activity_about;
    }

    @Override
    protected void init() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_version.setText(AppUtils.getVersionName());
    }


    private void shareApp() {
        Intent shareAppIntent = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.fromFile(new File(getPackageResourcePath()));
        shareAppIntent.setType("application/vnd.android.package-archive");
        shareAppIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareAppIntent, "分享APP到..."));
    }


    @OnClick({R.id.activity_about_frame_share, R.id.activity_about_frame_update})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_about_frame_share:
                shareApp();
                break;
            case R.id.activity_about_frame_update:
                if (NetworkUtils.isConnected()) {
                    final MaterialDialog dialog =
                            new MaterialDialog.Builder(this)
                                    .cancelable(false)
                                    .title("检查更新")
                                    .content("正在检查")
                                    .progress(true, 0)
                                    .progressIndeterminateStyle(false).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            OkGo.<String>get(Constants.API_URL_CHECK_UPDATE)
                                    .tag(this + Constants.API_URL_CHECK_UPDATE)
                                    .execute(new StringCallback() {
                                        @Override
                                        public void onStart(Request<String, ? extends Request> request) {
                                            super.onStart(request);
                                            if (!NetworkUtils.isConnected()) {
                                                ToastUtils.showShortToastSafe(getString(R.string.network_none_hint));
                                            }
                                        }

                                        @Override
                                        public void onSuccess(Response<String> response) {
                                            String json = response.body();
                                            try {
                                                String name = new JSONObject(json).getString(Constants.API_NAME);
                                                int newVersion = new JSONObject(json).getInt(Constants.JSON_VERSION);
                                                int nowVersion = AppUtils.getVersionCode();
                                                final String update_url = new JSONObject(json).getString(Constants.JSON_UPDATE_URL);
                                                String versionShort = new JSONObject(json).getString(Constants.JSON_VERSION_SHORT);
                                                String changelog = new JSONObject(json).getString(Constants.JSON_CHANGELOG);
                                                final String installUrl = new JSONObject(json).getString(Constants.JSON_INSTALL_URL);
                                                if (name != null && name.equals(getString(R.string.app_name))) {
                                                    if (newVersion > nowVersion) {
                                                        new MaterialDialog.Builder(AboutActivity.this)
                                                                .titleGravity(GravityEnum.CENTER)
                                                                .positiveColor(getResources().getColor(R.color.colorPrimary))
                                                                .title("新版本：V" + versionShort + "")
                                                                .content("更新日志:" + changelog)
                                                                .positiveText("下载")
                                                                .negativeText("复制")
                                                                .neutralText("浏览器下载")
                                                                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                                                        intent.setData(Uri.parse(update_url));
                                                                        startActivity(intent);
                                                                    }
                                                                })
                                                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                        ClipboardUtils.copyText(installUrl);
                                                                        ToastUtils.showShortToast("已复制");
                                                                    }
                                                                })
                                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                        try {
                                                                            GetRequest<File> request = OkGo.<File>get(installUrl)//
                                                                                    .tag(installUrl);
                                                                            //这里第一个参数是tag，代表下载任务的唯一标识，传任意字符串都行，需要保证唯一,我这里用url作为了tag
                                                                            OkDownload.request(installUrl, request)
                                                                                    .save()
                                                                                    .folder(AppUtils.getFileSavePath())
                                                                                    .register(new LogDownloadListener())
                                                                                    .start();
                                                                            ToastUtils.showShortToast("已添加1个下载任务");
                                                                        } catch (Exception ignored) {
                                                                            ToastUtils.showShortToast("出错了！");
                                                                        }
                                                                    }
                                                                }).show();
                                                    } else  {
                                                        ToastUtils.showShortToast("已是最新版！");
                                                    }

                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
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
                                            dialog.dismiss();
                                        }
                                    });


                        }
                    }, 1000);

                } else {
                    ToastUtils.showShortToastSafe(getString(R.string.network_none_hint));
                }
                break;
        }


    }

    // 根据文件后缀名获得对应的MIME类型。
    private static String getMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "*/*";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (Exception e) {
                return mime;
            }
        }
        return mime;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkGo.getInstance().cancelTag(this + Constants.API_URL_CHECK_UPDATE);
    }
}

package net.lzzy.networkdisk.activitys;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.OkDownload;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.log.LogDownloadListener;
import net.lzzy.networkdisk.models.Ext;
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.BASE64Utils;
import net.lzzy.networkdisk.utils.DateUtil;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import java.io.File;

import butterknife.BindView;


public class FileActivity extends BaseActivity {
    @BindView(R.id.activity_file_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_file_name)
    TextView tv_name;
    @BindView(R.id.activity_file_time)
    TextView tv_time;
    @BindView(R.id.activity_file_size)
    TextView tv_size;
    @BindView(R.id.activity_file_iv)
    ImageView iv;
    @BindView(R.id.activity_file_btn_dow)
    Button btn_dow;
    private UserFile userFile;
    private String tag;

    @Override
    protected int setContentView() {
        return R.layout.activity_file;
    }

    @Override
    protected void init() {


        setSupportActionBar(toolbar);
        String json = getIntent().getStringExtra(Constants.JSON_STRING);
        if (json != null && !json.equals("")) {
            userFile = JSON.parseObject(json, UserFile.class);
            iv.setImageResource(Ext.getIcon(userFile.getType()));
            tv_name.setText(userFile.getName());
            tv_size.setText(userFile.getFileSize());
            tv_time.setText(DateUtil.format(userFile.getCreateDate()));

            btn_dow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtils.isConnected()) {
                        if (NetworkUtils.isWifiConnected()) {
                            download();
                        } else {
                            new MaterialDialog.Builder(FileActivity.this)
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
                }
            });
            check();

        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void download() {
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
                check();
            }
        } else
            ToastUtils.showShortToast("SD卡空间不足");
    }

    private void check() {
        if (OkDownload.getInstance().getTask(userFile.getUrl()) != null) {
            btn_dow.setText("已在队列");
            btn_dow.setEnabled(false);
        } else {
            btn_dow.setText("下载");
            btn_dow.setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_file, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_file_detail:
                if (userFile != null) {
                    String content;
                    int icon;
                    if (userFile.isFile()) {
                        icon = Ext.getIcon(userFile.getType());
                        content = getString(R.string.detail_type, getString(R.string.file)) + getString(R.string.detail_size, userFile.getFileSize())
                                + getString(R.string.detail_ext, userFile.getType()) + getString(R.string.detail_create_time, DateUtil.format(userFile.getCreateDate()))
                                + getString(R.string.detail_end_time, DateUtil.format(userFile.getEndDate()));
                    } else {
                        icon = R.drawable.ic_folder;
                        content = getString(R.string.detail_type, getString(R.string.folder)) + getString(R.string.detail_size, "0.0B")
                                + getString(R.string.detail_ext, getString(R.string.folder)) + getString(R.string.detail_create_time, DateUtil.format(userFile.getCreateDate()))
                                + getString(R.string.detail_end_time, DateUtil.format(userFile.getEndDate()));
                    }
                    new MaterialDialog.Builder(FileActivity.this)
                            .iconRes(icon)
                            .limitIconToDefaultSize()
                            .title(userFile.getName())
                            .content(Html.fromHtml(content))
                            .show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (tag != null)
//            OkGo.getInstance().cancelTag(tag);
//    }
}

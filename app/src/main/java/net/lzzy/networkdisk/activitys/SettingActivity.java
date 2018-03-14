package net.lzzy.networkdisk.activitys;


import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
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
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.CacheUtils;
import net.lzzy.networkdisk.utils.ClipboardUtils;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnLongClick;

public class SettingActivity extends BaseActivity {
    @BindView(R.id.activity_setting_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_setting_tv_show_path)
    TextView tv_show_save_path;
    @BindView(R.id.activity_setting_tv_show_cacheSize)
    TextView tv_show_cacheSize;
    private CacheUtils glideCache;

    @Override
    protected int setContentView() {
        return R.layout.activity_setting;
    }

    @Override
    protected void init() {
        glideCache = CacheUtils.getInstance();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setShowSavePath(AppUtils.getFileSavePath());
        tv_show_cacheSize.setText(glideCache.getAllCacheSize(this));
    }

    private void setShowSavePath(String path) {
        String res = AppUtils.getFileSavePath();

        if (path != null && !path.isEmpty() && new File(path).exists()) {
            if (path.length() > 40) {
                res = "长按查看：.../" + path.substring(path.lastIndexOf("/") + 1, path.length());

            } else {
                res = path;
            }
        }
        if (res.contains(Environment.getExternalStorageDirectory().getPath() + "/"))
            res = res.replace(Environment.getExternalStorageDirectory().getPath() + "/", "");
        tv_show_save_path.setText(res);
    }

    @OnClick({R.id.activity_setting_frame_save_path, R.id.activity_setting_frame_cache})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_setting_frame_save_path:
                startActivityForResult(new Intent(this, SelectPathActivity.class), 101);
                break;
            case R.id.activity_setting_frame_cache:
                if (!tv_show_cacheSize.getText().toString().equals("0.0B")) {

                    final MaterialDialog dialog =
                            new MaterialDialog.Builder(this)
                                    .cancelable(false)
                                    .title("温馨提示")
                                    .content("正在清除")
                                    .progress(true, 0)
                                    .progressIndeterminateStyle(false).show();
                    glideCache.clearAllCache(this);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            ToastUtils.showShortToastSafe("清除成功");
                            tv_show_cacheSize.setText("0.0B");
                        }
                    }, 2000);
                } else
                    ToastUtils.showShortToastSafe("已经清空，不需要再清除");
                break;
        }

    }

    @OnLongClick(R.id.activity_setting_frame_save_path)
    public boolean onLongClick() {
        final String path = AppUtils.getFileSavePath();
        new MaterialDialog.Builder(this)
                .titleGravity(GravityEnum.CENTER)
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .title("下载文件保存路径")
                .content(path)
                .positiveText("复制")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ClipboardUtils.copyText(path);
                        ToastUtils.showShortToastSafe("已复制");
                    }
                }).show();
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 101) {
            String res = data.getStringExtra(Constants.API_PATH);
            setShowSavePath(res);
        }

    }


}

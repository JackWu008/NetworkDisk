package net.lzzy.networkdisk.activitys;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.request.PostRequest;
import com.lzy.okserver.OkUpload;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.adapter.SimpleGenericAdapter;
import net.lzzy.networkdisk.adapter.ViewHolder;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.models.DetailsUser;
import net.lzzy.networkdisk.models.Ext;
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.BASE64Utils;
import net.lzzy.networkdisk.utils.DateUtil;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class SelectActivity extends BaseActivity {
    private Stack<String> nowPathStack;
    private List<UserFile> userFiles;
    SimpleGenericAdapter adapter;
    File[] files;
    @BindView(R.id.activity_select_lv)
    ListView lv;
    @BindView(R.id.activity_select_tv_path)
    TextView tv_path;
    @BindView(R.id.activity_select_tv_upload)
    TextView tv_upload;
    @BindView(R.id.activity_select_upload_path)
    TextView tv_upload_path;
    @BindView(R.id.activity_select_toolbar)
    Toolbar toolbar;
    private String path;
    private String uploadRootpath;
    private String tag;

    @Override
    protected int setContentView() {
        return R.layout.activtiy_select;
    }

    @Override
    protected void init() {
        SPUtils sp_user = new SPUtils(Constants.SP_USER);
        uploadRootpath = BASE64Utils.decodeBase64(sp_user.getString(Constants.SP_EMAIL));
        path = uploadRootpath;
        userFiles = new ArrayList<>();
        final String rootpath = Environment.getExternalStorageDirectory().toString();
        nowPathStack = new Stack<>();
        files = Environment.getExternalStorageDirectory()
                .listFiles();
        //将根路径推入路径栈
        nowPathStack.push(rootpath);
        tv_path.setText(getPath());

        for (File f : files) {
            String name = f.getName();
            if (!name.substring(0, 1).equals(".")) {
                UserFile userFile = new UserFile();
                userFile.setName(name);
                userFile.setEndDate(f.lastModified());
                userFile.setPath(f.getPath());
                if (f.isFile()) {
                    userFile.setType(name.substring(name.lastIndexOf(".") + 1, name.length()));
                    userFile.setFileSize(AppUtils.getFormatSize(f.length()));
                } else
                    userFile.setType(Constants.CON_DIRECTORY);
                userFiles.add(userFile);
            }
        }
        adapter = new SimpleGenericAdapter(SelectActivity.this, R.layout.item_select_file, userFiles, true) {
            @Override
            public void populate(ViewHolder holder, UserFile userFile, View convertView, int position) {
                ImageView imageView = holder.getView(R.id.item_file_checkbox);
                if (!userFile.isFile()) {
                    imageView.setVisibility(View.GONE);
                    holder.getView(R.id.file_size).setVisibility(View.GONE);
                    holder.setImagView(R.id.file_image, R.drawable.ic_folder);
                } else {
                    imageView.setVisibility(View.VISIBLE);
                    holder.getView(R.id.file_size).setVisibility(View.VISIBLE);
                    holder.setTextView(R.id.file_size, userFile.getFileSize());
                    holder.setImagView(R.id.file_image, Ext.getIcon(userFile.getType()));
                    if (isSelected(position))
                        imageView.setImageResource(R.drawable.ic_checkbox_checked);
                    else
                        imageView.setImageResource(R.drawable.ic_checkbox_unchecked);
                }
                String name = userFile.getName();
                if (name.length() > 20)
                    holder.setTextView(R.id.file_name, name.substring(0, 20).concat("..."));
                else
                    holder.setTextView(R.id.file_name, name);
                holder.setTextView(R.id.file_name, userFile.getName());
                holder.setTextView(R.id.file_time, DateUtil.format(userFile.getEndDate()));

            }
        };
        lv.setAdapter(adapter);
        tv_upload.setText(getString(R.string.upload, 0 + ""));
        tv_upload_path.setText(getString(R.string.upload_path, "我的网盘"));


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    @OnItemClick(R.id.activity_select_lv)
    public void OnItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserFile userFile = userFiles.get(position);
        ImageView imageView = view.findViewById(R.id.item_file_checkbox);
        if (userFile.isFile()) {
            if (adapter.isSelected(position)) {
                Log.i("sfsfdsff", "onItemClick: 位置" + position + "：false");
                adapter.delete(position);
                imageView.setImageResource(R.drawable.ic_checkbox_unchecked);
            } else {
                Log.i("sfsfdsff", "onItemClick: 位置" + position + "：true");
                adapter.setIsSelected(position, true);
                imageView.setImageResource(R.drawable.ic_checkbox_checked);
            }
            if (adapter.getSelectedNum() == 0) {
                tv_upload.setBackgroundResource(R.drawable.shape_bt_send);
                tv_upload.setTextColor(getResources().getColor(R.color.md_grey));
            } else {
                tv_upload.setBackgroundResource(R.drawable.shape_bt_send_blue);
                tv_upload.setTextColor(getResources().getColor(R.color.md_white));
            }
            tv_upload.setText(getString(R.string.upload, adapter.getSelectedNum() + ""));
            adapter.notifyDataSetChanged();
            Log.i("dsfsdfsafsdfasa", "OnItemClick: " + adapter.getSelectedNum());
            // ToastUtils.showShortToastSafe(getPath() + "/" + userFile.getName());
        } else {
            nowPathStack.push("/" + userFile.getName());
            showChange(getPath());
            Log.i("fgasgdg", "onItemClick: " + getPath());
            Log.i("fgasgdg", "onItemClick: root" + Environment.getExternalStorageDirectory().getPath() + "/");
        }
    }


    //显示改变data之后的文件数据列表
    private void showChange(String path) {
        tv_upload.setText(getString(R.string.upload, 0 + ""));
        tv_upload.setBackgroundResource(R.drawable.shape_bt_send);
        tv_upload.setTextColor(getResources().getColor(R.color.md_grey));
        tv_path.setText(path);
        files = new File(path).listFiles();
        adapter.setFileData(files);
    }

    private String getPath() {
        Stack<String> temp = new Stack<>();
        temp.addAll(nowPathStack);
        String result = "";
        while (temp.size() != 0) {
            result = temp.pop().concat(result);
        }
        return result;
    }

    @OnClick({R.id.activity_select_upload_path, R.id.activity_select_tv_upload})
    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.activity_select_upload_path:
                startActivityForResult(new Intent(this, SelectUploadPathActivity.class), 99);
                break;
            case R.id.activity_select_tv_upload:
                if (NetworkUtils.isConnected()) {
                    if (NetworkUtils.isWifiConnected()) {
                        upload();
                    } else {
                        new MaterialDialog.Builder(SelectActivity.this)
                                .titleGravity(GravityEnum.CENTER)
                                .positiveColor(getResources().getColor(R.color.colorPrimary))
                                .title("温馨提示")
                                .content(getString(R.string.network_non_wifi_hint))
                                .positiveText("确定")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        upload();
                                    }
                                }).show();
                    }
                } else {
                    ToastUtils.showShortToast(getString(R.string.network_none_hint));
                }
                break;
        }

    }

    private void upload() {
        SPUtils utils = new SPUtils(Constants.SP_USER);
        String token = utils.getString(Constants.SP_TOKEN);
        String email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));
        long total = 0;
        if (adapter.getSelectedNum() > 0) {
            int[] pos = adapter.getSelectedPosition();
            for (int i : pos) {
                UserFile userFile = adapter.getItem(i);
                total += AppUtils.toLong(userFile.getFileSize());
            }
        }

        String json = utils.getString(Constants.JSON_SP_DETAILS);
        if (json != null && !json.equals("")) {
            json = BASE64Utils.decodeBase64(json);
            DetailsUser detailsUser = JSON.parseObject(json, DetailsUser.class);
            long num = detailsUser.getTotalSize() - detailsUser.getWithSize();
            if (total > num) {
                ToastUtils.showShortToast("云盘容量不足，请扩容后再试！");
            } else {
                int[] pos = adapter.getSelectedPosition();
                for (int i : pos) {
                    UserFile userFile = adapter.getItem(i);
                    tag = userFile.getPath();
                    PostRequest<String> postRequest = OkGo.<String>post(Constants.API_URL_UPLOAD)
                            .tag(tag)
                            .headers(Constants.JSON_RESULTS_TOKEN, token)
                            .headers(Constants.API_EMAIL, email)
                            .params(Constants.API_SHORT_PATH, path)
                            .params("fileKey" + i, new File(getPath() + "/" + userFile.getName()))//
                            .converter(new StringConvert());

                    OkUpload.request(tag, postRequest)
                            .extra1(userFile)
                            .save()
                            .start();
                }
                ToastUtils.showShortToast("已成功添加" + adapter.getSelectedNum() + "个上传任务");
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 99) {
            String res = data.getStringExtra(Constants.API_PATH);
            path = res.replace("我的网盘", uploadRootpath);
            if (!res.equals("我的网盘"))
                res = res.substring(res.lastIndexOf("\\") + 1, res.length());
            if (res.length() > 10) {
                res = res.substring(0, 10).concat("...");
            }
            tv_upload_path.setText(getString(R.string.upload_path, res));
        }

        Log.i("sfsadsdafsdffsdffsdf", "onActivityResult: " + path);
    }

    @Override
    public void onBackPressed() {
        if (nowPathStack.peek().equals(Environment.getExternalStorageDirectory().toString())) {
            finish();
        } else {
            nowPathStack.pop();
            showChange(getPath());
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        OkGo.getInstance().cancelTag(tag);
//    }
}

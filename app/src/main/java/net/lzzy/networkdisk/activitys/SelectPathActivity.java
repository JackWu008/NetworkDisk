package net.lzzy.networkdisk.activitys;


import android.content.Intent;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.adapter.SimpleGenericAdapter;
import net.lzzy.networkdisk.adapter.ViewHolder;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.models.Ext;
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.DateUtil;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class SelectPathActivity extends BaseActivity {
    private Stack<String> nowPathStack;
    private List<UserFile> userFiles;
    SimpleGenericAdapter adapter;
    File[] files;
    @BindView(R.id.activity_select_path_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_select_path_tv_show_path)
    TextView tv_show_path;
    @BindView(R.id.activity_select_path_lv)
    ListView lv;
    @BindView(R.id.activity_select_path_tv_save_path)
    TextView tv_save_path;


    @Override
    protected int setContentView() {
        return R.layout.activity_select_path;
    }

    @Override
    protected void init() {
        userFiles = new ArrayList<>();
        final String rootpath = Environment.getExternalStorageDirectory().toString();
        nowPathStack = new Stack<>();
        files = Environment.getExternalStorageDirectory()
                .listFiles();
        //将根路径推入路径栈
        nowPathStack.push(rootpath);
        tv_show_path.setText(getPath());
        for (File f : files) {
            String name = f.getName();
            if (!name.substring(0, 1).equals(".")) {
                if (!f.isFile()) {
                    UserFile userFile = new UserFile();
                    userFile.setName(name);
                    userFile.setEndDate(f.lastModified());
                    userFile.setPath(f.getPath());
                    userFiles.add(userFile);
                }
            }
        }
        adapter = new SimpleGenericAdapter(SelectPathActivity.this, R.layout.item_select_file, userFiles, false) {
            @Override
            public void populate(ViewHolder holder, UserFile userFile, View convertView, int position) {
                holder.getView(R.id.item_file_checkbox).setVisibility(View.GONE);
                if (!userFile.isFile()) {
                    holder.getView(R.id.file_size).setVisibility(View.GONE);
                    holder.setImagView(R.id.file_image, R.drawable.ic_folder);
                } else {
                    holder.getView(R.id.file_size).setVisibility(View.VISIBLE);
                    holder.setTextView(R.id.file_size, userFile.getFileSize());
                    holder.setImagView(R.id.file_image, Ext.getIcon(userFile.getType()));
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onBackPressed();
            }
        });
    }

    @OnItemClick(R.id.activity_select_path_lv)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserFile userFile = userFiles.get(position);
        if (!userFile.isFile()) {
            nowPathStack.push("/" + userFile.getName());
            files = new File(getPath()).listFiles();
            tv_show_path.setText(getPath());
            adapter.setFileData(files);
        }
    }

    @OnClick(R.id.activity_select_path_tv_save_path)
    public void onClick() {
        SPUtils spUtils = new SPUtils(Constants.SP_APP_STATE);
        spUtils.putString(Constants.SP_FILE_SAVE_PATH, getPath());
        Intent intent = new Intent();
        intent.putExtra(Constants.API_PATH, getPath());
        setResult(101, intent);
        finish();
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

    @Override
    public void onBackPressed() {
        if (nowPathStack.peek().equals(Environment.getExternalStorageDirectory().toString())) {
            finish();
        } else {
            nowPathStack.pop();
            files = new File(getPath()).listFiles();
            tv_show_path.setText(getPath());
            adapter.setFileData(files);
        }
    }
}

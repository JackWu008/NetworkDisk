package net.lzzy.networkdisk.activitys;


import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.adapter.SimpleGenericAdapter;
import net.lzzy.networkdisk.adapter.ViewHolder;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.models.Ext;
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.utils.BASE64Utils;
import net.lzzy.networkdisk.utils.DateUtil;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import butterknife.BindView;
import butterknife.OnClick;

public class SelectUploadPathActivity extends BaseActivity {
    @BindView(R.id.activity_select_path_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_select_path_tv_path)
    TextView tv_path;
    @BindView(R.id.activity_select_path_lv)
    ListView lv;
    @BindView(R.id.activity_select_path_upload_path)
    TextView upload_path;
    @BindView(R.id.activity_select_path_srl)
    SwipeRefreshLayout srl;
    @BindView(R.id.activity_select_path_tv_hint)
    TextView tv_hint;
    @BindView(R.id.activity_select_path_frameLayout)
    FrameLayout fl;
    @BindView(R.id.activity_select_path_btn_refresh)
    Button btn_refresh;
    private List<UserFile> userFiles = new ArrayList<>();
    private Stack<String> nowPathStack;
    private String rootpath;
    SimpleGenericAdapter adapter;
    private int state = 0;//0获取失败,1获取成功，2当前文件夹为空

    @Override
    protected int setContentView() {
        return R.layout.activity_select_upload_path;
    }

    @Override
    protected void init() {
        srl.setColorSchemeColors(getResources().getIntArray(R.array.refresh_colors));
        SPUtils sp_user = new SPUtils(Constants.SP_USER);
        rootpath = BASE64Utils.decodeBase64(sp_user.getString(Constants.SP_EMAIL));
        nowPathStack = new Stack<>();
        nowPathStack.push(rootpath);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get();
            }
        });
        if (userFiles.size() == 0) {
            get();
        }
        adapter = new SimpleGenericAdapter(this, R.layout.item_select_file, userFiles, true) {
            @Override
            public void populate(ViewHolder holder, UserFile userFile, View convertView, int position) {
                holder.setTextView(R.id.file_name, userFile.getName());
                holder.setTextView(R.id.file_time, DateUtil.format(userFile.getCreateDate()));
                String name = userFile.getName();
                if (name.length() > 16)
                    holder.setTextView(R.id.file_name, name.substring(0, 16).concat("..."));
                else
                    holder.setTextView(R.id.file_name, name);
                holder.getView(R.id.item_file_checkbox).setVisibility(View.GONE);
                if (!userFile.isFile()) {
                    holder.getView(R.id.file_size).setVisibility(View.GONE);
                    holder.setImagView(R.id.file_image, R.drawable.ic_folder);
                } else {
                    holder.getView(R.id.file_size).setVisibility(View.VISIBLE);
                    holder.setImagView(R.id.file_image, Ext.getIcon(userFile.getType()));
                }

            }
        };
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserFile userFile = userFiles.get(position);
                if (!userFile.isFile()) {
                    nowPathStack.push("\\" + userFile.getName());
                    get();
                    tv_path.setText(getShowPath(getPath()));
                }
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get();
            }
        });

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {//处理listView和SwipeRefreshLayout的滑动冲突
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {//滚动到顶部
                    View firstVisibleItemView = lv.getChildAt(0);
                    if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
                        srl.setEnabled(true);
                    }
                } else {//其它
                    srl.setEnabled(false);
                }
            }
        });
        tv_path.setText(getShowPath(getPath()));
    }

    @OnClick(R.id.activity_select_path_upload_path)
    public void OnClick() {
        Intent intent = new Intent();
        intent.putExtra(Constants.API_PATH, getShowPath(getPath()));
        setResult(99, intent);
        finish();
    }


    private void get() {
        SPUtils utils = new SPUtils(Constants.SP_USER);
        String token = utils.getString(Constants.SP_TOKEN);
        String email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));


        OkGo.<String>get(Constants.API_URL_GET)
                .tag(Constants.API_URL_GET)
                .headers(Constants.JSON_RESULTS_TOKEN, token)
                .headers(Constants.API_EMAIL, email)
                .params(Constants.API_PATH, getPath())
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        srl.setRefreshing(true);
                        if (!NetworkUtils.isConnected()) {
                            ToastUtils.showShortToast(getString(R.string.network_none_hint));
                        }
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        userFiles.clear();
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
                                state = 0;
                                // ToastUtils.showShortToastSafe("获取失败");
                                break;
                            case 2:
                                state = 2;
                                //  ToastUtils.showShortToastSafe("这里是空的哦，快点上传文件吧");
                                break;
                            case -1:
                                ToastUtils.showShortToastSafe("登录已失效，请重新登录！");
                                startActivity(new Intent(SelectUploadPathActivity.this, LoginActivity.class));
                                finish();
                                break;
                            case 1:
                                state = 1;
                                String json = "";
                                try {
                                    JSONArray array = (JSONArray) new JSONObject(res).get(Constants.JSON_FILE_INFO);
                                    json = array.toString();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                userFiles.addAll(JSON.parseArray(json, UserFile.class));

                                break;
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);

                        srl.setRefreshing(false);
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
                        srl.setRefreshing(false);
                        checkState();
                    }
                });

    }

    private void checkState() {
        switch (state) {
            case 0:
                lv.setVisibility(View.GONE);
                fl.setVisibility(View.VISIBLE);
                tv_hint.setText("获取失败");
                break;
            case 1:
                lv.setVisibility(View.VISIBLE);
                fl.setVisibility(View.GONE);
                break;
            case 2:
                lv.setVisibility(View.GONE);
                fl.setVisibility(View.VISIBLE);
                tv_hint.setText("这里是空的哦，快点上传文件吧");
                break;
            default:
                lv.setVisibility(View.VISIBLE);
                fl.setVisibility(View.GONE);
                break;
        }
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


    private String getShowPath(String path) {
        return path.replace(rootpath, "我的网盘");
    }

    @Override
    public void onBackPressed() {
        if (nowPathStack.peek().equals(rootpath)) {
            finish();
        } else {
            nowPathStack.pop();
            get();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkGo.getInstance().cancelTag(Constants.API_URL_GET);
    }
}

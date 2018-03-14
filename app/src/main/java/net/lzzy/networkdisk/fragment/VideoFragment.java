package net.lzzy.networkdisk.fragment;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.clans.fab.FloatingActionMenu;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.base.Request;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.OkUpload;
import com.lzy.okserver.task.XExecutor;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.activitys.FileActivity;
import net.lzzy.networkdisk.activitys.LoginActivity;
import net.lzzy.networkdisk.adapter.SimpleGenericAdapter;
import net.lzzy.networkdisk.adapter.ViewHolder;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.inter.IDeleteFileUpdateListener;
import net.lzzy.networkdisk.log.LogDownloadListener;
import net.lzzy.networkdisk.models.Ext;
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.sort.FileSortFactory;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.BASE64Utils;
import net.lzzy.networkdisk.utils.DateUtil;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class VideoFragment extends BaseFragment  {
    @BindView(R.id.include_select_item_delete)
    LinearLayout layout_delete;
    @BindView(R.id.include_select_item_dow)
    LinearLayout layout_dow;
    @BindView(R.id.include_select_item_edit)
    LinearLayout layout_deit;
    @BindView(R.id.include_select_item_detail)
    LinearLayout layout_detail;
    @BindView(R.id.include_layout)
    LinearLayout select_layout;
    @BindView(R.id.fragment_video_lv)
    ListView lv;
    @BindView(R.id.fragment_video_srl)
    SwipeRefreshLayout srl;
    @BindView(R.id.fragment_video_tv_num)
    TextView tv_num;
    @BindView(R.id.fragment_video_tv_sort_title)
    TextView tv_sort_title;
    @BindView(R.id.fragment_video_ll)
    LinearLayout sort_layout;
    @BindView(R.id.fragment_video_tv_hint)
    TextView tv_hint;
    @BindView(R.id.fragment_video_frameLayout)
    FrameLayout fl;
    @BindView(R.id.fragment_video_btn_refresh)
    Button btn_refresh;
    private List<UserFile> files = new ArrayList<>();
    private SimpleGenericAdapter adapter;
    private IDeleteFileUpdateListener listener;
    private boolean allSelected;
    private ActionMode actionMode;
    private FloatingActionMenu fam;

    private boolean isShowActionMode = false;
    private int state = 0;//0获取失败,1获取成功，2当前文件夹为空
    private String tag;

    @Override
    protected int setContentView() {
        return R.layout.fragment_video;
    }

    @Override
    protected void init() {
        srl.setColorSchemeColors(getResources().getIntArray(R.array.refresh_colors));

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ViewPager viewPager = null;
        if (activity != null) {
            fam = activity.findViewById(R.id.activity_main_fam);
            viewPager = activity.findViewById(R.id.activity_main_pager);
        }

        if (files.size() == 0) {
            get();
        }
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get();
            }
        });
        adapter = new SimpleGenericAdapter(getContext(), R.layout.item_video, files, false) {
            @Override
            public void populate(ViewHolder holder, UserFile userFile, View convertView, int position) {
                holder.setTextView(R.id.item_video_name, userFile.getName())
                        .setTextView(R.id.item_video_size, userFile.getFileSize())
                        .setTextView(R.id.item_video_time, DateUtil.format(userFile.getCreateDate()));
            }
        };
        lv.setAdapter(adapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(multiChoiceModeListener);


        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    View firstVisibleItemView = lv.getChildAt(0);
                    if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
                        if (!isShowActionMode)
                            srl.setEnabled(true);
                    }
                } else {
                    srl.setEnabled(false);
                }
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (isShowActionMode)
                    actionMode.finish();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        listener.onDelete(false);

        OkUpload.getInstance().addOnAllTaskEndListener(new XExecutor.OnAllTaskEndListener() {
            @Override
            public void onAllTaskEnd() {
                get();
            }
        });
    }

    private AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mode.setTitle("已选中" + lv.getCheckedItemCount() + "项");


            if (lv.getCheckedItemCount() == files.size()) {
                mode.getMenu().getItem(0).setTitle("取消");
                allSelected = true;
            } else {
                mode.getMenu().getItem(0).setTitle("全选");
                allSelected = false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            isShowActionMode = true;
            srl.setEnabled(false);
            sort_layout.setEnabled(false);
            select_layout.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.select_item_out));
            select_layout.setVisibility(View.VISIBLE);
            fam.setAnimated(true);
            fam.setVisibility(View.GONE);
            mode.getMenuInflater().inflate(R.menu.menu_actionbar, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            actionMode = mode;
            switch (item.getItemId()) {
                case R.id.menu_main_choose:
                    if (allSelected) {
                        for (int i = adapter.getCount() - 1; i >= 0; i--) {
                            lv.setItemChecked(i, false);
                        }
                    } else {
                        for (int i = adapter.getCount() - 1; i >= 0; i--) {
                            lv.setItemChecked(i, true);
                        }
                    }
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = mode;
            isShowActionMode = false;
            srl.setEnabled(true);
            sort_layout.setEnabled(true);
            select_layout.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.select_item_in));
            select_layout.setVisibility(View.GONE);
            fam.setAnimated(true);
            fam.setVisibility(View.VISIBLE);
        }
    };

    public void get() {
        SPUtils utils = new SPUtils(Constants.SP_USER);
        String token = utils.getString(Constants.SP_TOKEN);
        String email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));
        OkGo.<String>get(Constants.API_URL_GET_TYPE)
                .tag(this + Constants.API_URL_GET_TYPE)
                .headers(Constants.JSON_RESULTS_TOKEN, token)
                .headers(Constants.API_EMAIL, email)
                .params(Constants.API_KEY, 3)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        srl.setRefreshing(true);
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
                                state = 0;
                                ToastUtils.showShortToastSafe("获取失败");
                                break;
                            case 2:
                                state = 2;
                                // ToastUtils.showShortToastSafe("此类型的文件为空");
                                break;
                            case -1:
                                ToastUtils.showShortToastSafe("登录已失效，请重新登录！");
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
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
                                files.addAll(JSON.parseArray(json, UserFile.class));
                                break;
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        srl.setRefreshing(false);
                        tv_num.setText(String.valueOf(files.size()));
                        checkState();
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

    private void delete(UserFile userFile) {
        SPUtils utils = new SPUtils(Constants.SP_USER);
        String rootPath = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL, "-1"));
        String filePath = rootPath + "\\" + userFile.getPath();
        String token = utils.getString(Constants.SP_TOKEN);
        String email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));
        OkGo.<String>post(Constants.API_URL_DELETE)
                .tag(this + Constants.API_URL_DELETE)
                .headers(Constants.JSON_RESULTS_TOKEN, token)
                .headers(Constants.API_EMAIL, email)
                .params(Constants.API_PATH, filePath)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        networkDetection();
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String res = response.body();
                        int code = JSON.parseObject(res, new TypeReference<Map<String, Integer>>() {
                        }).get(Constants.JSON_RESULTS_CODE);
                        switch (code) {
                            case 0:
                                ToastUtils.showShortToastSafe("删除失败");
                                break;
                            case 1:
                                ToastUtils.showShortToastSafe("删除成功");
                                get();
                                listener.onDelete(true);
                                break;
                            case 2:
                                ToastUtils.showShortToastSafe("文件不存在");
                                get();
                                break;
                            case -1:
                                ToastUtils.showShortToastSafe("登录已失效，请重新登录！");
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
                                break;
                        }
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
                    }
                });
    }

    private void rename(String nowName, String newName, UserFile userFile) {
        SPUtils utils = new SPUtils(Constants.SP_USER);
        String rootPath = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL, "-1"));
        String filePath = rootPath + "\\" + userFile.getPath().replace(userFile.getName(), "");
        String token = utils.getString(Constants.SP_TOKEN);
        String email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));
        OkGo.<String>post(Constants.API_URL_RENAME)
                .tag(this + Constants.API_URL_RENAME)
                .headers(Constants.JSON_RESULTS_TOKEN, token)
                .headers(Constants.API_EMAIL, email)
                .params(Constants.API_NOWPATH, filePath + "\\" + nowName)
                .params(Constants.API_NEWPATH, filePath + "\\" + newName)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        networkDetection();
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String res = response.body();
                        int code = JSON.parseObject(res, new TypeReference<Map<String, Integer>>() {
                        }).get(Constants.JSON_RESULTS_CODE);
                        switch (code) {
                            case 0:
                                ToastUtils.showShortToastSafe("重命名失败");
                                break;
                            case 1:
                                ToastUtils.showShortToastSafe("重命名成功");
                                get();
                                break;
                            case 2:
                                ToastUtils.showShortToastSafe("重命名后的名称一样");
                                break;
                            case 3:
                                ToastUtils.showShortToastSafe("文件不存在");
                                get();
                                break;
                            case -1:
                                ToastUtils.showShortToastSafe("登录已失效，请重新登录！");
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
                                break;
                        }
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
                    }
                });
    }

    private void networkDetection() {
        if (!NetworkUtils.isConnected()) {
            srl.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShortToastSafe(getString(R.string.network_none_hint));
                    srl.setRefreshing(false);
                }
            }, 1000);
        }
    }

    @OnClick({R.id.fragment_video_ll, R.id.fragment_video_btn_refresh})
    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_video_ll:

                final SPUtils sp_app_state = new SPUtils(Constants.SP_APP_STATE);
                int i = sp_app_state.getInt(Constants.SP_VIDEO_SORT_STATE);
                if (i == -1) {
                    i = 0;
                    sp_app_state.putInt(Constants.SP_VIDEO_SORT_STATE, 0);
                }
                new MaterialDialog.Builder(getActivity())
                        .title("选择排序方式")
                        .items(R.array.select_sort)
                        .itemsCallbackSingleChoice(
                                i, new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        sp_app_state.putInt(Constants.SP_VIDEO_SORT_STATE, which);
                                        switch (which) {
                                            case 0:
                                                adapter.setSortWay(FileSortFactory.SORT_BY_FOLDER_AND_NAME);
                                                break;
                                            case 1:
                                                adapter.setSortWay(FileSortFactory.SORT_BY_FOLDER_AND_TIME);
                                                break;
                                        }
                                        dialog.dismiss();
                                        adapter.notifyDataSetChanged();
                                        tv_sort_title.setText(text.toString());
                                        return true;
                                    }
                                })
                        .show();
                break;
            case R.id.fragment_video_btn_refresh:
                get();
                break;
        }

    }


    @OnClick({R.id.include_select_item_delete, R.id.include_select_item_detail, R.id.include_select_item_dow, R.id.include_select_item_edit})
    public void onClicks(View view) {
        switch (view.getId()) {
            case R.id.include_select_item_delete:
                if (lv.getCheckedItemCount() > 0) {
                    new MaterialDialog.Builder(getActivity())
                            .stackingBehavior(
                                    StackingBehavior
                                            .ALWAYS)
                            .titleGravity(GravityEnum.CENTER)
                            .positiveColor(getResources().getColor(R.color.colorPrimary))
                            .btnStackedGravity(GravityEnum.CENTER)
                            .title("提示")
                            .content("是否删除" + lv.getCheckedItemCount() + "个文件(夹)？")
                            .positiveText("确定")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                        if (lv.isItemChecked(i)) {
                                            delete(adapter.getItem(i));
                                        }
                                    }
                                    actionMode.finish();
                                }
                            }).show();

                } else {
                    ToastUtils.showShortToastSafe("没有选中哦");
                }
                break;
            case R.id.include_select_item_detail:
                if (lv.getCheckedItemCount() == 1) {
                    UserFile userFile = adapter.getItem(getCheckedItemPosition(lv));
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
                        new MaterialDialog.Builder(getActivity())
                                .iconRes(icon)
                                .limitIconToDefaultSize()
                                .title(userFile.getName())
                                .content(Html.fromHtml(content))
                                .show();
                    }
                }
                break;
            case R.id.include_select_item_dow:
                if (NetworkUtils.isConnected()) {
                    if (NetworkUtils.isWifiConnected()) {
                        download();
                    } else {
                        new MaterialDialog.Builder(getActivity())
                                .titleGravity(GravityEnum.CENTER)
                                .positiveColor(getResources().getColor(R.color.colorPrimary))
                                .title("温馨提示")
                                .content(getString(R.string.network_non_wifi_hint))
                                .positiveText("确定")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        download();
                                        actionMode.finish();
                                    }
                                }).show();
                    }
                } else {
                    ToastUtils.showShortToastSafe(getString(R.string.network_none_hint));
                }

                break;
            case R.id.include_select_item_edit:
                if (lv.getCheckedItemCount() == 1) {
                    final UserFile us = adapter.getItem(getCheckedItemPosition(lv));
                    if (us != null) {
                        final String name = us.getName();
                        new MaterialDialog.Builder(getActivity())
                                .stackingBehavior(
                                        StackingBehavior
                                                .ALWAYS)
                                .titleGravity(GravityEnum.CENTER)
                                .positiveColor(getResources().getColor(R.color.colorPrimary))
                                .btnStackedGravity(GravityEnum.CENTER)
                                .inputType(InputType.TYPE_CLASS_TEXT
                                        | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                                        | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                                .title("重命名")
                                .positiveText("确定")
                                .input("重命名", name,
                                        false, new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                if (!Constants.CON_FOLDER_NAME_PATTERN_MATCHES.matcher(input.toString().trim()).matches()) {
                                                    ToastUtils.showLongToast(getString(R.string.pattern_matches_folder_name_hint));
                                                } else {
                                                    if (!name.equals(input.toString())) {
                                                        rename(name, input.toString(), us);
                                                    }
                                                }
                                                actionMode.finish();
                                            }
                                        })
                                .show();

                    }
                } else {
                    ToastUtils.showShortToast("暂不支持多个重命名");
                }
                break;
        }
    }

    private void download() {
        int checkedCount = 0;
        long dowTotalSize = 0;
        long phoneSurplusSize = AppUtils.toLong(AppUtils.getSDAvailableSize());
        for (int k = adapter.getCount() - 1; k >= 0; k--) {
            if (lv.isItemChecked(k)) {
                UserFile us = adapter.getItem(k);
                if (us.isFile()) {
                    dowTotalSize += AppUtils.toLong(us.getFileSize());
                }
            }
        }
        if (dowTotalSize < phoneSurplusSize) {
            SPUtils utils = new SPUtils(Constants.SP_USER);
            String rootPath = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL, "-1"));
            for (int i = adapter.getCount() - 1; i >= 0; i--) {
                if (lv.isItemChecked(i)) {
                    UserFile us = adapter.getItem(i);
                    if (us.isFile()) {
                        final String filePath = rootPath + "\\" + us.getPath();
                        tag = us.getUrl();
                        GetRequest<File> request = OkGo.<File>get(Constants.API_URL_DOWNLOAD)//
                                .tag(tag)
                                .params(Constants.API_SHORT_PATH, filePath);
                        //这里第一个参数是tag，代表下载任务的唯一标识，传任意字符串都行，需要保证唯一,我这里用url作为了tag
                        OkDownload.request(tag, request)
                                .extra1(us)
                                .save()
                                .folder(AppUtils.getFileSavePath())
                                .register(new LogDownloadListener())
                                .start();
                        checkedCount++;
                    } else
                        ToastUtils.showShortToast("暂不支持文件夹下载");
                }
            }
        } else
            ToastUtils.showShortToast("SD卡空间不足");
        if (checkedCount > 0)
            ToastUtils.showShortToast("已成功添加" + checkedCount + "个下载任务");
    }

    @OnItemClick(R.id.fragment_video_lv)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserFile userFile = files.get(position);
        if (userFile.isFile()) {
            String json = JSON.toJSONString(userFile);
            startActivity(new Intent(getActivity(), FileActivity.class).putExtra(Constants.JSON_STRING, json));
        }
    }


    private int getCheckedItemPosition(ListView lv) {
        int res = -1;
        SparseBooleanArray sba = lv.getCheckedItemPositions();
        for (int i = 0; i < sba.size(); i++) {
            int pos = sba.keyAt(i);
            if (sba.get(pos))
                res = pos;
        }
        return res;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (IDeleteFileUpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "必须实现IDeleteFileUpdateListener");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OkGo.getInstance().cancelTag(this + Constants.API_URL_GET_TYPE);
        OkGo.getInstance().cancelTag(this + Constants.API_URL_DELETE);
        OkGo.getInstance().cancelTag(this + Constants.API_URL_RENAME);
        OkGo.getInstance().cancelTag(tag);
    }


}

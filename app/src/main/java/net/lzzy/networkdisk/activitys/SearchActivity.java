package net.lzzy.networkdisk.activitys;

import android.content.Intent;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import net.lzzy.networkdisk.models.FileType;
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.utils.BASE64Utils;
import net.lzzy.networkdisk.utils.DateUtil;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;

public class SearchActivity extends BaseActivity {
    @BindView(R.id.activity_search_lv)
    ListView lv;
    @BindView(R.id.activity_search_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_search_sv)
    SearchView sv;
    private List<UserFile> files = new ArrayList<>();
    private SimpleGenericAdapter adapter;
    @BindView(R.id.activity_search_pb)
    ProgressBar pb;
    @BindView(R.id.activity_search_tv_hint)
    TextView tv_hint;

    @Override
    protected int setContentView() {
        return R.layout.activity_search;
    }

    @Override
    protected void init() {
       // KeyBoardUtil.openKeybord(edt_email, this);

        adapter = new SimpleGenericAdapter(this, R.layout.item_file, files, false) {
            @Override
            public void populate(ViewHolder holder, UserFile userFile, View convertView, int position) {
                if (userFile.isFile()) {
                    holder.setTextView(R.id.file_size, userFile.getFileSize());
                    holder.setImagView(R.id.file_image, Ext.getIcon(userFile.getType()));
                    String name = userFile.getName();
                    if (name.length() > 20)
                        holder.setTextView(R.id.file_name, name.substring(0, 20).concat("..."));
                    else
                        holder.setTextView(R.id.file_name, name);
                    holder.setTextView(R.id.file_time, DateUtil.format(userFile.getCreateDate()));
                }
            }
        };


        lv.setAdapter(adapter);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                files.clear();
//                if (query.trim().length() > 0)
//                    search(query);
//                else if (query.trim().length() == 0) {
//                    pb.setVisibility(View.GONE);
//                    tv_hint.setVisibility(View.GONE);
//                    lv.setVisibility(View.VISIBLE);
//                }
//                adapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                files.clear();
                if (newText.trim().length() > 0)
                    search(newText);
                else if (newText.trim().length() == 0) {
                    pb.setVisibility(View.GONE);
                    tv_hint.setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });

    }

    @OnItemClick(R.id.activity_search_lv)
    public void onItemClick( int position) {
        UserFile userFile = files.get(position);
        String json = JSON.toJSONString(userFile);
        if (FileType.getKeyByType(userFile.getType()) == 0)
            startActivity(new Intent(this, PicturesActivity.class).putExtra(Constants.INTENT_PICTURE_USERFILE_JSON, json).putExtra(Constants.INTENT_IS_PICTURE_TO, false));
        else
            startActivity(new Intent(this, FileActivity.class).putExtra(Constants.JSON_STRING, json));

    }


    private void search(String key) {
        SPUtils utils = new SPUtils( Constants.SP_USER);
        String token = utils.getString(Constants.SP_TOKEN);
        String email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));
        OkGo.<String>get(Constants.API_URL_SEARCH)
                .tag(Constants.API_URL_SEARCH)
                .headers(Constants.JSON_RESULTS_TOKEN, token)
                .headers(Constants.API_EMAIL, email)
                .params(Constants.API_KEY, key)
                .execute(new StringCallback() {

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        pb.setVisibility(View.VISIBLE);
                        lv.setVisibility(View.GONE);
                        pb.setVisibility(View.GONE);
                        tv_hint.setVisibility(View.GONE);
                    }

                    @Override
                    public void onSuccess(Response<String> response) {

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
                                // state = 0;
                                //  ToastUtils.showShortToastSafe("获取失败");
                                lv.setVisibility(View.VISIBLE);
                                pb.setVisibility(View.GONE);
                                tv_hint.setVisibility(View.GONE);
                                //  tv_hint.setText("获取失败");
                                break;
                            case 2:
                                lv.setVisibility(View.GONE);
                                pb.setVisibility(View.GONE);
                                tv_hint.setVisibility(View.VISIBLE);
                                tv_hint.setText("没有找到");
                                // state = 2;
                                // ToastUtils.showShortToastSafe("这里是空的哦，快点上传文件吧");
                                break;
                            case -1:
                                ToastUtils.showShortToastSafe("登录已失效，请重新登录！");
                                startActivity(new Intent(SearchActivity.this, LoginActivity.class));
                                finish();
                                break;
                            case 1:
                                lv.setVisibility(View.VISIBLE);
                                pb.setVisibility(View.GONE);
                                tv_hint.setVisibility(View.GONE);

                                String json = "";
                                try {
                                    JSONArray array = (JSONArray) new JSONObject(res).get(Constants.JSON_FILE_INFO);
                                    json = array.toString();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                files.addAll(JSON.parseArray(json, UserFile.class));
                                // state = 1;
                                break;
                        }

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        pb.setVisibility(View.GONE);

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

//        SearchView.SearchAutoComplete textView = ( SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
//        textView.setTextColor(Color.WHITE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkGo.getInstance().cancelTag(Constants.API_URL_SEARCH);
    }

}

package net.lzzy.networkdisk.activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.github.clans.fab.FloatingActionButton;
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
import net.lzzy.networkdisk.adapter.SimplePagerAdapter;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.fragment.AllFileFragment;
import net.lzzy.networkdisk.fragment.DocumentFragment;
import net.lzzy.networkdisk.fragment.MusicFragment;
import net.lzzy.networkdisk.fragment.PictureFragment;
import net.lzzy.networkdisk.fragment.VideoFragment;
import net.lzzy.networkdisk.inter.IDeleteFileUpdateListener;
import net.lzzy.networkdisk.log.LogDownloadListener;
import net.lzzy.networkdisk.models.DetailsUser;
import net.lzzy.networkdisk.models.Feedback;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.BASE64Utils;
import net.lzzy.networkdisk.utils.BackHandlerHelper;
import net.lzzy.networkdisk.utils.ClipboardUtils;
import net.lzzy.networkdisk.utils.GlideCircleTransform;
import net.lzzy.networkdisk.utils.NetworkUtils;
import net.lzzy.networkdisk.utils.SPUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;

public class MainActivity extends BaseActivity implements NavigationView.
        OnNavigationItemSelectedListener, IDeleteFileUpdateListener {
    private String[] tags;
    @BindView(R.id.activity_main_fam)
    FloatingActionMenu fam;
    @BindView(R.id.activity_main_fab_new_folder)
    FloatingActionButton new_folder;
    @BindView(R.id.activity_main_fab_upload_file)
    FloatingActionButton upload_file;
    @BindView(R.id.activity_main_drawer)
    DrawerLayout drawer;
    @BindView(R.id.activity_main_nv)
    NavigationView nv;
    @BindView(R.id.activity_main_pager)
    ViewPager viewPager;
    @BindView(R.id.activity_main_tab)
    TabLayout tab;
    @BindView(R.id.activity_main_toolbar)
    Toolbar toolbar;
    private long backPressed = 0;
    private ProgressBar progressBar;
    private TextView tv_capacity;
    private TextView tv_name;
    private ImageView iv;
    private PictureFragment pictureFragment;
    private MusicFragment musicFragment;
    private DocumentFragment documentFragment;
    private VideoFragment videoFragment;
    private AllFileFragment allFileFragment;

    @Override
    protected int setContentView() {
        return R.layout.activity_main;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void init() {

        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
        tags = getResources().getStringArray(R.array.main_tags);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, 220);
        }
        setSupportActionBar(toolbar);
        tab.setupWithViewPager(viewPager);
        tab.setTabMode(TabLayout.MODE_FIXED);
        fam.setClosedOnTouchOutside(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        //设置Toolbar和DrawerLayout实现动画和联动
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        nv.setNavigationItemSelectedListener(this);
        List<Fragment> fragments = new ArrayList<>();
        allFileFragment = new AllFileFragment();
        pictureFragment = new PictureFragment();
        musicFragment = new MusicFragment();
        documentFragment = new DocumentFragment();
        videoFragment = new VideoFragment();
//        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
//        //[2]开启事物
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        //[3]动态替换
//        transaction.replace(R.id.activity_main_pager,pictureFragment,"pictureFragment");
//      //  transaction.replace(R.id.ll2, new Fragment2(),"f2");
//
//        //[4]最后一步 记得commit
//        transaction.commit();


        fragments.add(allFileFragment);
        fragments.add(pictureFragment);
        fragments.add(musicFragment);
        fragments.add(documentFragment);
        fragments.add(videoFragment);
        SimplePagerAdapter adapter = new SimplePagerAdapter(getSupportFragmentManager(), fragments, tags);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (!tags[position].equals("全部")) {
                    toolbar.setSubtitle("");
                    toolbar.setTitle(tags[position]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        nv.setItemIconTintList(null);
        View headerLayout = nv.getHeaderView(0);
        iv = headerLayout.findViewById(R.id.header_navigation_cv);
        setImg(null);
        tv_name = headerLayout.findViewById(R.id.header_navigation_tv_name);
        tv_capacity = headerLayout.findViewById(R.id.header_navigation_tv_capacity);
        progressBar = headerLayout.findViewById(R.id.header_navigation_pb);
        FrameLayout frameLayout = headerLayout.findViewById(R.id.header_navigation_fl);
        if (NetworkUtils.isConnected())
            getUserInfo();
        else {
            SPUtils utils = new SPUtils(Constants.SP_USER);
            String json = utils.getString(Constants.JSON_SP_DETAILS);
            if (json != null && !json.equals("")) {
                json = BASE64Utils.decodeBase64(utils.getString(Constants.JSON_SP_DETAILS));
                DetailsUser detailsUser = JSON.parseObject(json, DetailsUser.class);
                nameCheck(detailsUser.getName());
                progressBar.setMax((int) detailsUser.getTotalSize());
                progressBar.setProgress((int) detailsUser.getWithSize());
                tv_capacity.setText("已用:" + AppUtils.getFormatSize(detailsUser.getWithSize()) + "/" + AppUtils.getFormatSize(detailsUser.getTotalSize()));
            }
        }


        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(nv)) {
                    drawer.closeDrawers();
                }
                startActivityForResult(new Intent(MainActivity.this, UserActivity.class), 109);

            }
        });


        checkUpdate();


        OkUpload.getInstance().addOnAllTaskEndListener(new XExecutor.OnAllTaskEndListener() {
            @Override
            public void onAllTaskEnd() {
                getUserInfo();
            }
        });


    }


    private void getUserInfo() {
        SPUtils utils = new SPUtils(Constants.SP_USER);
        String email = BASE64Utils.decodeBase64(utils.getString(Constants.SP_EMAIL));
        OkGo.<String>get(Constants.API_URL_GET_USER_INFO)
                .params(Constants.API_EMAIL, email)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        if (!NetworkUtils.isConnected())
                            ToastUtils.showShortToast(getString(R.string.network_none_hint));
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(Response<String> response) {
                        String res = response.body();
                        int code = 0;
                        String details = "";
                        try {
                            JSONObject object = new JSONObject(res);
                            code = (int) object.get(Constants.JSON_RESULTS_CODE);
                            details = object.getString(Constants.JSON_RESULTS_DETAILS_USER);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        switch (code) {
                            case 0:
                                //   state = 0;
                                // ToastUtils.showShortToastSafe("获取失败");
                                break;
                            case -1:
                                ToastUtils.showShortToastSafe("登录已失效，请重新登录！");
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                                break;
                            case 1:
                                try {
                                    DetailsUser detailsUser = JSON.parseObject(details, DetailsUser.class);
                                    SPUtils spUtils = new SPUtils(Constants.SP_USER);
                                    spUtils.putString(Constants.JSON_SP_DETAILS, BASE64Utils.encodeBase64(JSON.toJSONString(detailsUser)));
                                    nameCheck(detailsUser.getName());
                                    progressBar.setMax((int) detailsUser.getTotalSize());
                                    progressBar.setProgress((int) detailsUser.getWithSize());
                                    tv_capacity.setText("已用:" + AppUtils.getFormatSize(detailsUser.getWithSize()) + "/" + AppUtils.getFormatSize(detailsUser.getTotalSize()));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
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

                    @Override
                    public void onFinish() {
                        super.onFinish();

                    }
                });
    }

    private void nameCheck(String name) {
//        String regAll = "[\\u4e00-\\u9fa5]+";
//        String reg = "[\\u4e00-\\u9fa5]";
//        boolean result1All = name.matches(regAll);
//        boolean result1 = name.matches(reg);
//        if (result1All) {
//            if (name.length() > 5)
//                tv_name.setText(name.substring(0, 5).concat("..."));
//            else
//                tv_name.setText(name);
//        } else {
//            tv_name.setText(name);
//        }

        if (name.length() > 5)
            tv_name.setText(name.substring(0, 5).concat("..."));
        else
            tv_name.setText(name);

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_navigation_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
//                if (drawer.isDrawerOpen(nv)) {
//                    drawer.closeDrawers();
//                }
                break;
            case R.id.menu_navigation_setting:
                startActivity(new Intent(this, SettingActivity.class));
//                if (drawer.isDrawerOpen(nv)) {
//                    drawer.closeDrawers();
//                }
                break;
            case R.id.menu_navigation_exit:
                ((AppUtils) getApplication()).allFinishActivity();
                break;
            case R.id.menu_navigation_feedback:
                if (NetworkUtils.isConnected()) {
                    SPUtils spUtils = new SPUtils(Constants.SP_USER);
                    String userEmail = BASE64Utils.decodeBase64(spUtils.getString(Constants.SP_EMAIL));
                    String details = BASE64Utils.decodeBase64(spUtils.getString(Constants.JSON_SP_DETAILS));
                    details = details.substring(0, details.lastIndexOf("}")).concat(",\"email\":\"" + userEmail + "\" }");
                    new Feedback.Builder(this)
                            .withEmail("1586023871@qq.com")
                            .userInfo(details)
                            .withSystemInfo()
                            .build()
                            .start();

                } else
                    ToastUtils.showShortToast("请开启网络后再试");
                break;

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(nv)) {
            drawer.closeDrawers();
            return;
        }
        if (!BackHandlerHelper.handleBackPress(this)) {
            if (fam.isOpened()) {
                fam.close(true);
            } else {
                long currentTime = System.currentTimeMillis();
                if (currentTime - backPressed < 2000) {
                    OkGo.getInstance().cancelAll();
                    ((AppUtils) getApplication()).allFinishActivity();
                } else {
                    ToastUtils.showShortToastSafe("再按一次退出程序");
                }
                backPressed = currentTime;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OkUpload.getInstance().addOnAllTaskEndListener(new XExecutor.OnAllTaskEndListener() {
            @Override
            public void onAllTaskEnd() {
                getUserInfo();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                break;
            case R.id.menu_main_task_manage:
                startActivity(new Intent(this, TaskManageActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void checkUpdate() {
        if (NetworkUtils.isConnected()) {
            OkGo.<String>get(Constants.API_URL_CHECK_UPDATE)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            String json = response.body();
                            try {
                                final int newVersion = new JSONObject(json).getInt(Constants.JSON_VERSION);
                                int nowVersion = AppUtils.getVersionCode();
                                String versionShort = new JSONObject(json).getString(Constants.JSON_VERSION_SHORT);
                                String changelog = new JSONObject(json).getString(Constants.JSON_CHANGELOG);
                                final String installUrl = new JSONObject(json).getString(Constants.JSON_INSTALL_URL);
                                final String update_url = new JSONObject(json).getString(Constants.JSON_UPDATE_URL);
                                final SPUtils spUtils = new SPUtils(Constants.SP_APP_STATE);
                                int state = spUtils.getInt(Constants.SP_CHECK_UPLOAD + newVersion);
                                if (state == -1 || state == 1) {
                                    if (newVersion > nowVersion) {
                                        new MaterialDialog.Builder(MainActivity.this)
                                                .titleGravity(GravityEnum.CENTER)
                                                .positiveColor(getResources().getColor(R.color.colorPrimary))
                                                .title("新版本：V" + versionShort + "")
                                                .content("更新日志:" + changelog)
                                                .positiveText("下载")
                                                .negativeText("复制")
                                                .neutralText("浏览器下载")
                                                .checkBoxPrompt("此版本不再提醒", false, new CompoundButton.OnCheckedChangeListener() {
                                                    @Override
                                                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                                        if (b) {
                                                            spUtils.putInt(Constants.SP_CHECK_UPLOAD + newVersion, 0);
                                                        } else {
                                                            spUtils.putInt(Constants.SP_CHECK_UPLOAD + newVersion, 1);
                                                        }
                                                    }
                                                })
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
                                                            // ToastUtils.showShortToast("出错了！");
                                                        }
                                                    }
                                                }).show();
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

        }
    }

    private void setImg(Uri uri) {
        SPUtils sp_state = new SPUtils(Constants.SP_APP_STATE);
        SPUtils sp_user = new SPUtils(Constants.SP_USER);
        String email = BASE64Utils.decodeBase64(sp_user.getString(Constants.SP_EMAIL));
        String imgKey = sp_state.getString(Constants.SP_IMG_KEY);
        if (imgKey == null || imgKey.isEmpty()) {
            String uuid = UUID.randomUUID().toString();
            sp_state.putString(Constants.SP_IMG_KEY, uuid);
            imgKey = uuid;
        }
        Log.i("main  url", "setImg: " + Constants.API_URL_GET_IMG + email + ".png");
        if (uri != null) {
            Glide.with(this).load(uri)
                    .asBitmap().signature(new StringSignature(imgKey)).placeholder(R.drawable.ic_user)
                    .transform(new GlideCircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.RESULT).into(iv);
            // new File(uri.getPath()).delete();
        } else {
            Glide.with(this).load(Constants.API_URL_GET_IMG + email + ".png")
                    .asBitmap().signature(new StringSignature(imgKey)).placeholder(R.drawable.ic_user)
                    .transform(new GlideCircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.RESULT).into(iv);
        }

    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 109) {
            switch (resultCode) {
                case 87:
                    String capacity = data.getStringExtra(Constants.INTENT_CAPACITY);
                    tv_capacity.setText(capacity);
                    break;
                case 88:
                    String name = data.getStringExtra(Constants.INTENT_NAME);
                    nameCheck(name);
                    break;
                case 86:
                    Uri uri = data.getParcelableExtra(Constants.INTENT_URI);
                    setImg(uri);
                    break;
                default:
                    getUserInfo();
                    break;
            }
        }


    }


    @Override
    public void onDelete(boolean isDelete) {
        if (isDelete) {
            getUserInfo();
            if (allFileFragment.isAdded())
                allFileFragment.get();
            if (pictureFragment.isAdded())
                pictureFragment.get();
            if (documentFragment.isAdded())
                documentFragment.get();
            if (musicFragment.isAdded())
                musicFragment.get();
            if (videoFragment.isAdded())
                videoFragment.get();
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {
            case 220:
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!cameraAccepted) {
                    ToastUtils.showShortToast("未获取写入权限将无法下载");
                }
                break;
        }
    }


}

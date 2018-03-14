package net.lzzy.networkdisk.activitys;


import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.adapter.SimplePagerAdapter;
import net.lzzy.networkdisk.fragment.DownFinishFragment;
import net.lzzy.networkdisk.fragment.DownloadFragment;
import net.lzzy.networkdisk.fragment.UpFinishFragment;
import net.lzzy.networkdisk.fragment.UploadFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class TaskManageActivity extends BaseActivity {
    @BindView(R.id.activity_task_manage_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_task_manage_tab)
    TabLayout tab;
    @BindView(R.id.activity_task_manage_pager)
    ViewPager viewpager;

    @Override
    protected int setContentView() {
        return R.layout.activity_task_manage;
    }

    @Override
    protected void init() {
        String[] tabs = getResources().getStringArray(R.array.task_tags);
        setSupportActionBar(toolbar);
        tab.setupWithViewPager(viewpager);
        tab.setTabMode(TabLayout.MODE_FIXED);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new DownloadFragment());
        fragments.add(new UploadFragment());
        fragments.add(new DownFinishFragment());
        fragments.add(new UpFinishFragment());

        SimplePagerAdapter adapter = new SimplePagerAdapter(getSupportFragmentManager(), fragments, tabs);
        viewpager.setAdapter(adapter);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


}

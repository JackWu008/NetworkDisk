package net.lzzy.networkdisk.activitys;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.squareup.leakcanary.RefWatcher;

import net.lzzy.networkdisk.utils.AppUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public abstract class BaseActivity extends AppCompatActivity {
    public Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setContentView());
        ((AppUtils) getApplication()).addActivity(this);

        unbinder = ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = AppUtils.getRefWatcher(this);
        refWatcher.watch(this);
        ((AppUtils) getApplication()).removeActivity(this);
        if (unbinder != null)
            unbinder.unbind();
    }

    protected abstract int setContentView();

    protected abstract void init();
}

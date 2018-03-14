package net.lzzy.networkdisk.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.lzy.okserver.OkDownload;
import com.lzy.okserver.task.XExecutor;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.adapter.DownloadAdapter;
import net.lzzy.networkdisk.utils.ToastUtils;



import butterknife.BindView;


public class DownFinishFragment extends BaseFragment implements XExecutor.OnAllTaskEndListener{
    private DownloadAdapter adapter;
    private OkDownload okDownload;


    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @Override
    protected int setContentView() {
        return R.layout.fragment_finish_down;
    }



    @Override
    protected void init() {
        okDownload = OkDownload.getInstance();
        adapter = new DownloadAdapter(getActivity());
        adapter.updateData(DownloadAdapter.TYPE_FINISH);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        okDownload.addOnAllTaskEndListener(this);
    }

    @Override
    public void onAllTaskEnd() {
        ToastUtils.showShortToast("所有下载任务已结束");
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        okDownload.removeOnAllTaskEndListener(this);
        adapter.unRegister();
    }
}

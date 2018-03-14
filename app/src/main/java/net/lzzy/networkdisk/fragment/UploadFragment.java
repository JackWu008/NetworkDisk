package net.lzzy.networkdisk.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.lzy.okserver.OkUpload;
import com.lzy.okserver.task.XExecutor;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.adapter.UploadAdapter;
import net.lzzy.networkdisk.utils.ToastUtils;

import butterknife.BindView;



public class UploadFragment extends BaseFragment implements XExecutor.OnAllTaskEndListener {
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private OkUpload okUpload;
    private UploadAdapter adapter;


    @Override
    protected int setContentView() {
        return R.layout.frament_upload;
    }


    @Override
    protected void init() {
        okUpload = OkUpload.getInstance();
        okUpload.getThreadPool().setCorePoolSize(1);

        adapter = new UploadAdapter(getActivity());
        adapter.updateData(UploadAdapter.TYPE_ING);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        okUpload.addOnAllTaskEndListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        okUpload.removeOnAllTaskEndListener(this);
        adapter.unRegister();
    }


    @Override
    public void onAllTaskEnd() {
        ToastUtils.showShortToast("所有上传任务已结束");
    }
}

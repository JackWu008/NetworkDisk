package net.lzzy.networkdisk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.okgo.db.DownloadManager;
import com.lzy.okgo.model.Progress;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.lzy.okserver.download.DownloadTask;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.log.LogDownloadListener;
import net.lzzy.networkdisk.models.Ext;
import net.lzzy.networkdisk.utils.ApkUtils;
import net.lzzy.networkdisk.utils.AppUtils;
import net.lzzy.networkdisk.utils.FileUtils;
import net.lzzy.networkdisk.utils.ToastUtils;
import net.lzzy.networkdisk.views.NumberProgressBar;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

    private static final int TYPE_ALL = 0;
    public static final int TYPE_FINISH = 1;
    public static final int TYPE_ING = 2;

    private List<DownloadTask> values;
    private LayoutInflater inflater;
    private Context context;
    private int type;

    public DownloadAdapter(Context context) {
        this.context = context;
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(1);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateData(int type) {
        //这里是将数据库的数据恢复
        this.type = type;
        if (type == TYPE_ALL) values = OkDownload.restore(DownloadManager.getInstance().getAll());
        if (type == TYPE_FINISH)
            values = OkDownload.restore(DownloadManager.getInstance().getFinished());
        if (type == TYPE_ING)
            values = OkDownload.restore(DownloadManager.getInstance().getDownloading());
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_download_manager, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final DownloadTask task = values.get(position);
        String tag = createTag(task);
        task.register(new ListDownloadListener(tag, holder))//
                .register(new LogDownloadListener());
        holder.setTag(tag);
        holder.setTask(task);
        holder.bind();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.onClick();
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new MaterialDialog.Builder(context)
                        .items("删除", "重新下载")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                switch (position) {
                                    case 0:
                                        holder.remove();
                                        break;
                                    case 1:
                                        holder.restart();
                                        break;
                                }
                            }
                        })
                        .show();

                return true;
            }
        });
        holder.refresh(task.progress);
    }

    public void unRegister() {
        Map<String, DownloadTask> taskMap = OkDownload.getInstance().getTaskMap();
        for (DownloadTask task : taskMap.values()) {
            task.unRegister(createTag(task));
        }
    }

    private String createTag(DownloadTask task) {
        return type + "_" + task.progress.tag;
    }

    @Override
    public int getItemCount() {
        return values == null ? 0 : values.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_dow_manager_icon)
        ImageView icon;
        @BindView(R.id.item_dow_manager_name)
        TextView name;
        @BindView(R.id.item_dow_manager_downloadSize)
        TextView downloadSize;
        @BindView(R.id.item_dow_manager_netSpeed)
        TextView netSpeed;
        @BindView(R.id.item_dow_manager_pbProgress)
        NumberProgressBar pbProgress;
        @BindView(R.id.item_dow_manager_state)
        ImageView download;
        private DownloadTask task;
        private String tag;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setTask(DownloadTask task) {
            this.task = task;
        }

        void bind() {


            Progress progress = task.progress;
            String ext = progress.fileName.substring(progress.fileName.lastIndexOf(".") + 1, progress.fileName.length());
            icon.setImageResource(Ext.getIcon(ext));
            name.setText(progress.fileName);




        }

        @SuppressLint("SetTextI18n")
        void refresh(Progress progress) {

            String currentSize = Formatter.formatFileSize(context, progress.currentSize);
            String totalSize = Formatter.formatFileSize(context, progress.totalSize);
            downloadSize.setText(currentSize + "/" + totalSize);
            switch (progress.status) {
                case Progress.NONE:
                    netSpeed.setText("停止");
                    download.setImageResource(R.drawable.ic_stop);
                    break;
                case Progress.PAUSE:
                    netSpeed.setText("暂停中");
                    download.setImageResource(R.drawable.ic_stop);
                    break;
                case Progress.ERROR:
                    netSpeed.setText("下载出错");
                    download.setImageResource(R.drawable.ic_stop);
                    break;
                case Progress.WAITING:
                    netSpeed.setText("等待中");
                    download.setImageResource(R.drawable.ic_stop);
                    break;
                case Progress.FINISH:
                    netSpeed.setText("下载完成");
                    download.setVisibility(View.GONE);

                    break;
                case Progress.LOADING:
                    String speed = Formatter.formatFileSize(context, progress.speed);
                    netSpeed.setText(String.format("%s/s", speed));
                    download.setImageResource(R.drawable.ic_start);
                    break;
            }
            //    tvProgress.setText(numberFormat.format(progress.fraction));
            pbProgress.setMax(10000);
            pbProgress.setProgress((int) (progress.fraction * 10000));
        }


        public void onClick() {
            Progress progress = task.progress;
            switch (progress.status) {
                case Progress.PAUSE:
                    task.restart();
                    break;
                case Progress.NONE:
                case Progress.ERROR:
                    task.start();
                    break;
                case Progress.LOADING:
                    task.pause();
                    break;
                case Progress.FINISH:
                    if (new File(progress.filePath).exists()) {
                        FileUtils.openFile(context, new File(progress.filePath));
                    } else {
                        ToastUtils.showShortToast("文件不存在");
                    }


                    break;
            }
            Log.i("safdsdfsdfdsa", "onClick: " + progress.status);
            refresh(progress);
        }


        void remove() {
            task.remove(false);
            updateData(type);
        }


        void restart() {
            task.restart();
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }

    private class ListDownloadListener extends DownloadListener {

        private ViewHolder holder;

        ListDownloadListener(Object tag, ViewHolder holder) {
            super(tag);
            this.holder = holder;
        }

        @Override
        public void onStart(Progress progress) {

        }

        @Override
        public void onProgress(Progress progress) {
            if (tag == holder.getTag()) {
                holder.refresh(progress);
            }

        }

        @Override
        public void onError(Progress progress) {
            Throwable throwable = progress.exception;
            if (throwable != null) throwable.printStackTrace();
        }

        @Override
        public void onFinish(File file, Progress progress) {

            updateData(type);

        }

        @Override
        public void onRemove(Progress progress) {
        }
    }
}

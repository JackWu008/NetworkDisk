/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.lzzy.networkdisk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.db.UploadManager;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.PostRequest;
import com.lzy.okgo.request.base.Request;
import com.lzy.okserver.OkUpload;
import com.lzy.okserver.upload.UploadListener;
import com.lzy.okserver.upload.UploadTask;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.log.LogUploadListener;
import net.lzzy.networkdisk.models.Ext;
import net.lzzy.networkdisk.models.Upload;
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.utils.ToastUtils;
import net.lzzy.networkdisk.views.NumberProgressBar;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.ViewHolder> {

    public static final int TYPE_ALL = 0;
    public static final int TYPE_FINISH = 1;
    public static final int TYPE_ING = 2;

    private List<UploadTask<?>> values;
    private List<Upload> ups;
    private LayoutInflater inflater;
    private Context context;
    private int type = -1;

    public UploadAdapter(Context context) {
        this.context = context;
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(1);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateData(int type) {
        //这里是将数据库的数据恢复
        this.type = type;
        if (type == TYPE_ALL) values = OkUpload.restore(UploadManager.getInstance().getAll());
        if (type == TYPE_FINISH)
            values = OkUpload.restore(UploadManager.getInstance().getFinished());
        if (type == TYPE_ING) values = OkUpload.restore(UploadManager.getInstance().getUploading());

        //由于Converter是无法保存下来的，所以这里恢复任务的时候，需要额外传入Converter，否则就没法解析数据
        //至于数据类型，统一就行，不一定非要是String
        for (UploadTask<?> task : values) {
            //noinspection unchecked
            Request<String, ? extends Request> request = (Request<String, ? extends Request>) task.progress.request;
            request.converter(new StringConvert());
        }

        notifyDataSetChanged();
    }

    public List<UploadTask<?>> updateData(List<Upload> ups) {
        this.type = -1;
        this.ups = ups;
        values = new ArrayList<>();
        if (ups != null) {

            for (int i = 0; i < ups.size(); i++) {
                Upload up = ups.get(i);
                PostRequest<String> postRequest = OkGo.<String>post(Constants.API_URL_UPLOAD)//
                        .params("shortpath", up.getSavePath())//
                        .params("fileKey" + i, new File(up.getFilePath()))//
                        .converter(new StringConvert());

                UploadTask<String> task = OkUpload.request(up.getFilePath(), postRequest)//
                        .save();
                values.add(task);
            }
        }
        notifyDataSetChanged();
        return values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_upload_manager, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //noinspection unchecked
        UploadTask<String> task = (UploadTask<String>) values.get(position);
        String tag = createTag(task);
        task.register(new ListUploadListener(tag, holder))//
                .register(new LogUploadListener<String>());
        holder.setTag(tag);
        holder.setTask(task);
        holder.bind();
        holder.refresh(task.progress);
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
                        .items("删除", "重新上传")
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
    }

    public void unRegister() {
        Map<String, UploadTask<?>> taskMap = OkUpload.getInstance().getTaskMap();
        for (UploadTask<?> task : taskMap.values()) {
            task.unRegister(createTag(task));
        }
    }

    private String createTag(UploadTask task) {
        return type + "_" + task.progress.tag;
    }

    @Override
    public int getItemCount() {
        return values == null ? 0 : values.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_upload_manager_icon)
        ImageView icon;
        @BindView(R.id.item_upload_manager_name)
        TextView name;
        @BindView(R.id.item_upload_manager_size)
        TextView size;
        @BindView(R.id.item_upload_manager_netSpeed)
        TextView netSpeed;
        @BindView(R.id.item_upload_manager_pbProgress)
        NumberProgressBar pbProgress;
        @BindView(R.id.item_upload_manager_state)
        ImageView iv_state;
        private UploadTask<?> task;
        private String tag;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setTask(UploadTask<?> task) {
            this.task = task;
        }

        public void bind() {
            Progress progress = task.progress;
            UserFile item = (UserFile) progress.extra1;
            String n = item.getName();
            name.setText(n);
            String ext = n.substring(n.lastIndexOf(".") + 1, n.length());
            icon.setImageResource(Ext.getIcon(ext));
        }

        public void refresh(Progress progress) {


            String currentSize = Formatter.formatFileSize(context, progress.currentSize);
            String totalSize = Formatter.formatFileSize(context, progress.totalSize);
            size.setText(currentSize + "/" + totalSize);
            switch (progress.status) {
                case Progress.NONE:
                    netSpeed.setText("停止");
                    iv_state.setImageResource(R.drawable.ic_stop);
                    break;
                case Progress.PAUSE:
                    netSpeed.setText("暂停中");
                    iv_state.setImageResource(R.drawable.ic_stop);
                    break;
                case Progress.ERROR:
                    netSpeed.setText("上传出错");
                    iv_state.setImageResource(R.drawable.ic_stop);
                    break;
                case Progress.WAITING:
                    netSpeed.setText("等待中");
                    iv_state.setImageResource(R.drawable.ic_stop);
                    break;
                case Progress.FINISH:
                    iv_state.setVisibility(View.GONE);
                    netSpeed.setText("上传成功");
                    break;
                case Progress.LOADING:
                    String speed = Formatter.formatFileSize(context, progress.speed);
                    netSpeed.setText(String.format("%s/s", speed));
                    iv_state.setImageResource(R.drawable.ic_start);
                    break;
            }
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

                    break;
            }
            refresh(progress);
        }


        public void remove() {
            task.remove();
            if (type == -1) {
                int removeIndex = -1;
                for (int i = 0; i < ups.size(); i++) {
                    if (ups.get(i).getFilePath().equals(task.progress.tag)) {
                        removeIndex = i;
                        break;
                    }
                }
                if (removeIndex != -1) {
                    ups.remove(removeIndex);
                }
                updateData(ups);
            } else {
                updateData(type);
            }
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

    private class ListUploadListener extends UploadListener<String> {

        private ViewHolder holder;

        ListUploadListener(Object tag, ViewHolder holder) {
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
        public void onFinish(String s, Progress progress) {
          //  ToastUtils.showShortToast("上传完成:" + progress.fileName);
            if (type != -1) updateData(type);
        }

        @Override
        public void onRemove(Progress progress) {
        }
    }
}

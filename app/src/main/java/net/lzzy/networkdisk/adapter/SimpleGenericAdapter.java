package net.lzzy.networkdisk.adapter;

import android.content.Context;

import net.lzzy.networkdisk.constants.Constants;
import net.lzzy.networkdisk.models.UserFile;
import net.lzzy.networkdisk.sort.FileSortFactory;
import net.lzzy.networkdisk.utils.AppUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;


public abstract class SimpleGenericAdapter extends MyBaseAdapter<UserFile> {
    private int sortWay = FileSortFactory.SORT_BY_FOLDER_AND_NAME;

    public void setSortWay(int sortWay) {
        this.sortWay = sortWay;
    }


    protected SimpleGenericAdapter(Context context, int layout, List<UserFile> userFiles, boolean isMyMultipleChoice) {
        super(context, layout, userFiles,isMyMultipleChoice);
        sort();

    }

    /**
     * 将文件列表排序
     */
    private void sort() {
        Collections.sort(getItems(), FileSortFactory.getWebFileQueryMethod(sortWay));
    }

    @Override
    public void notifyDataSetChanged() {
        //重新排序
        sort();
        super.notifyDataSetChanged();
    }

    public void setFileData(File[] fs) {
        getItems().clear();
        for (File f : fs) {
            String name = f.getName();
            if (!name.substring(0, 1).equals(".")) {
                UserFile userFile = new UserFile();
                userFile.setName(name);
                userFile.setEndDate(f.lastModified());
                userFile.setPath(f.getPath());
                if (f.isFile()) {
                    userFile.setType(name.substring(name.lastIndexOf(".") + 1, name.length()));
                    userFile.setFileSize(AppUtils.getFormatSize(f.length()));
                } else
                    userFile.setType(Constants.CON_DIRECTORY);
                getItems().add(userFile);
            }
        }
        sort();
        this.notifyDataSetChanged();
    }
}
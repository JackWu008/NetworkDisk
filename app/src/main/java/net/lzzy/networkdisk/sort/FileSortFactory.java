package net.lzzy.networkdisk.sort;


import net.lzzy.networkdisk.models.UserFile;

import java.util.Comparator;

public class FileSortFactory {
    public static final int SORT_BY_FOLDER_AND_NAME = 1;//按文件夹和名称排序。
    public static final int SORT_BY_FOLDER_REVERSE_AND_NAME = 2;

    public static final int SORT_BY_FOLDER_AND_NAME_REVERSE = 3;
    public static final int SORT_BY_FOLDER_REVERSE_AND_NAME_REVERSE = 4;


    public static final int SORT_BY_FOLDER_AND_TIME = 9;
    public static final int SORT_BY_FOLDER_REVERSE_AND_TIME = 10;
    public static final int SORT_BY_FOLDER_AND_TIME_REVERSE = 11;
    public static final int SORT_BY_FOLDER_REVERSE_AND_TIME_REVERSE = 12;


    public static Comparator<UserFile> getWebFileQueryMethod(
            int method) {
        switch (method) {
            case SORT_BY_FOLDER_AND_NAME:
                return new SortByFolderAndName(true, true);
            case SORT_BY_FOLDER_REVERSE_AND_NAME:
                return new SortByFolderAndName(false, true);
            case SORT_BY_FOLDER_AND_NAME_REVERSE:
                return new SortByFolderAndName(true, false);
            case SORT_BY_FOLDER_REVERSE_AND_NAME_REVERSE:
                return new SortByFolderAndName(false, false);
            case SORT_BY_FOLDER_AND_TIME:
                return new SortByFolderAndTime(true, true);
            case SORT_BY_FOLDER_REVERSE_AND_TIME:
                return new SortByFolderAndTime(false, true);
            case SORT_BY_FOLDER_AND_TIME_REVERSE:
                return new SortByFolderAndTime(true, false);
            case SORT_BY_FOLDER_REVERSE_AND_TIME_REVERSE:
                return new SortByFolderAndTime(false, false);
            default:
                break;
        }
        return null;
    }

}

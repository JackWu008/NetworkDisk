package net.lzzy.networkdisk.sort;


import net.lzzy.networkdisk.models.UserFile;

import java.util.Comparator;

public class SortByFolderAndName implements Comparator<UserFile> {

    private boolean first;
    private boolean second;

    public SortByFolderAndName(boolean first, boolean second) {
        this.first = first;
        this.second = second;
    }


    @Override
    public int compare(UserFile lhs, UserFile rhs) {
        if (first) {
            if (!lhs.isFile() && rhs.isFile()) {
                return -1;
            }
            if (lhs.isFile() && !rhs.isFile()) {
                return 1;
            }
        } else {
            if (!lhs.isFile() && rhs.isFile()) {
                return 1;
            }
            if (lhs.isFile() && !rhs.isFile()) {
                return -1;
            }
        }

        if (second) {
            if (!(lhs.isFile() ^ rhs.isFile())) {
                return lhs.getName().compareTo(rhs.getName());
            }
        } else {
            if (!(lhs.isFile() ^ rhs.isFile())) {
                return -lhs.getName().compareTo(rhs.getName());
            }
        }
        return 0;
    }
}

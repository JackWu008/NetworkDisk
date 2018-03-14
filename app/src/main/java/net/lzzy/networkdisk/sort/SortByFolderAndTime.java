package net.lzzy.networkdisk.sort;


import net.lzzy.networkdisk.models.UserFile;

import java.util.Comparator;

public class SortByFolderAndTime implements Comparator<UserFile> {
	private boolean first;
	private boolean second;

	SortByFolderAndTime(boolean firstSequence,
						boolean secondSequence) {
		this.first = firstSequence;
		this.second = secondSequence;
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
			if (lhs.isFile() == rhs.isFile()) {
				return -Long.compare(lhs.getCreateDate(), rhs.getCreateDate());
			}
		} else {
			if (lhs.isFile() == rhs.isFile()) {
				return +Long.compare(lhs.getCreateDate(), rhs.getCreateDate());
			}
		}
		return 0;
	}
}

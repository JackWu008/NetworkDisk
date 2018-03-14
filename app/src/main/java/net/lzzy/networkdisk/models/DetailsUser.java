package net.lzzy.networkdisk.models;

public class DetailsUser {
	private String name;
	private String role;
	private long totalSize;
	private long withSize;

	public DetailsUser() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}


	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public long getWithSize() {
		return withSize;
	}

	public void setWithSize(long withSize) {
		this.withSize = withSize;
	}
}

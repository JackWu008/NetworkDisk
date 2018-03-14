package net.lzzy.networkdisk.models;

import com.alibaba.fastjson.annotation.JSONField;

import net.lzzy.networkdisk.constants.Constants;

import java.io.Serializable;

public class UserFile implements Serializable{
	private String name;// 文件名
	private String fileSize;// 大小
	private long endDate;// 最后修改时间
	private long createDate;// 创建时间
	private String type;// 类型（后缀）
	private String url;// 下载url
	private String path;// 文件路径
	@JSONField(serialize = false)
	private boolean isFile;

	public UserFile() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		isFile = !type.equals(Constants.CON_DIRECTORY);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@JSONField(serialize = false)
	public boolean isFile() {
		return isFile;
	}

	
	
	
}

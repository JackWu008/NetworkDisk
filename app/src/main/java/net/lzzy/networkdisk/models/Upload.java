package net.lzzy.networkdisk.models;

import java.io.Serializable;


public class Upload implements Serializable {
    private String savePath;
    private String filePath;
    private String name;       //图片的名字
    private long size;         //图片的大小
    private String createTime;

    public String getSavePath() {
        return savePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

package com.ycbjie.ycupdatelib.download.bean;

import android.support.annotation.IntRange;


import com.ycbjie.ycupdatelib.download.constant.DlStatus;
import com.ycbjie.ycupdatelib.download.utils.UpdateUtils;

import java.io.Serializable;


public class FileInfo implements Serializable{

    private String id;   //文件的唯一标识符 (url+文件存储路径)
    private String downloadUrl;   //下载的url
    private String filePath;  //文件存放的路径位置
    private long size;   //文件的总尺寸
    private long downloadLocation; // 下载的位置(就是当前已经下载过的size，也是断点的位置)

    @IntRange(from = DlStatus.WAIT, to = DlStatus.FAIL)
    private int downloadStatus = DlStatus.PAUSE;   //下载的状态信息



    public FileInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDownloadLocation() {
        return downloadLocation;
    }

    public void setDownloadLocation(long downloadLocation) {
        this.downloadLocation = downloadLocation;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id='" + id + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", filePath='" + filePath + '\'' +
                ", size=" + size +
                ", downloadLocation=" + downloadLocation +
                ", downloadStatus=" + UpdateUtils.getStatusDesc(downloadStatus) +
                '}';
    }
}

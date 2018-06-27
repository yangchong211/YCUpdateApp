package com.ycbjie.ycupdatelib.download.bean;


import android.support.annotation.IntRange;

import com.ycbjie.ycupdatelib.download.constant.DlConstant;
import com.ycbjie.ycupdatelib.download.utils.DebugUtils;

import java.io.Serializable;


public class RequestInfo implements Serializable{

    @IntRange(from = DlConstant.RequestCode.loading, to = DlConstant.RequestCode.pause)
    private int dictate;   //下载的控制状态

    private DownloadInfo downloadInfo;

    public RequestInfo() {
    }



    public int getDictate() {
        return dictate;
    }

    public void setDictate(int dictate) {
        this.dictate = dictate;
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    public void setDownloadInfo(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }


    @Override
    public String toString() {
        return "RequestInfo{" +
                "dictate=" + DebugUtils.getRequestDictateDesc(dictate) +
                ", downloadInfo=" + downloadInfo +
                '}';
    }
}

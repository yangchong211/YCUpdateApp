package com.ycbjie.ycupdatelib.download.execute;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.IntRange;
import android.util.Log;


import com.ycbjie.ycupdatelib.download.constant.DlStatus;
import com.ycbjie.ycupdatelib.download.bean.FileInfo;
import com.ycbjie.ycupdatelib.download.bean.DownloadInfo;
import com.ycbjie.ycupdatelib.download.constant.DlConstant;
import com.ycbjie.ycupdatelib.download.db.DbHolder;
import com.ycbjie.ycupdatelib.download.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadTask implements Runnable{
    
    private static final String TAG = "DownloadTask";
    private Context context;
    private DownloadInfo info;
    private FileInfo mFileInfo;
    private DbHolder dbHolder;
    private boolean isPause;



    public DownloadTask(Context context, DownloadInfo info, DbHolder dbHolder) {
        this.context = context;
        this.info = info;
        this.dbHolder = dbHolder;

        //初始化下载文件信息
        mFileInfo = new FileInfo();
        mFileInfo.setId(info.getUniqueId());
        mFileInfo.setDownloadUrl(info.getUrl());
        mFileInfo.setFilePath(info.getFile().getAbsolutePath());

        LogUtils.i(TAG, "构造函数 -> 初始化 mFileInfo=" + mFileInfo);

        FileInfo fileInfoFromDb = dbHolder.getFileInfo(info.getUniqueId());
        long location = 0;
        long fileSize = 0;
        if (null != fileInfoFromDb){
            location = fileInfoFromDb.getDownloadLocation();
            fileSize = fileInfoFromDb.getSize();

            if (location == 0){
                if (info.getFile().exists()){
                    info.getFile().delete();
                }
            }else {
                //因为未知的原因, 这个文件不存在了,(虽然数据库记录表明我们的确已经下载过了),所以我们要从头开始
                if (!info.getFile().exists()){
                    LogUtils.i(TAG, "file = " + info.getFile());
                    Log.i(TAG, "数据库记录表明我们下载过该文件, 但是现在该文件不存在,所以从头开始");
                    dbHolder.deleteFileInfo(info.getUniqueId());
                    location = 0;
                    fileSize = 0;
                }
            }
        }else {
            if (info.getFile().exists()){
                info.getFile().delete();
            }
        }

        mFileInfo.setSize(fileSize);
        mFileInfo.setDownloadLocation(location);

        LogUtils.i(TAG, "构造函数() -> 初始化完毕  mFileInfo=" + mFileInfo);
    }

    @Override
    public void run() {
        download();
    }

    public void pause(){
        isPause = true;
    }

    @IntRange(from = DlStatus.WAIT, to = DlStatus.FAIL)
    public int getStatus(){
        if (null != mFileInfo){
            return mFileInfo.getDownloadStatus();
        }
        return DlStatus.FAIL;
    }

    public void setFileStatus( @IntRange(from = DlStatus.WAIT, to = DlStatus.FAIL) int status){
        mFileInfo.setDownloadStatus(status);
    }

    public void sendBroadcast(Intent intent){
        context.sendBroadcast(intent);
    }

    public DownloadInfo getDownLoadInfo(){
        return info;
    }

    public FileInfo getFileInfo(){
        return mFileInfo;
    }

    private void download(){
        mFileInfo.setDownloadStatus(DlStatus.PREPARE);
        LogUtils.i(TAG, "准备开始下载");

        Intent intent = new Intent();
        intent.setAction(info.getAction());
        intent.putExtra(DlConstant.EXTRA_INTENT_DOWNLOAD, mFileInfo);
        context.sendBroadcast(intent);

        RandomAccessFile accessFile = null;
        HttpURLConnection http = null;
        InputStream inStream = null;

        try {
            URL sizeUrl = new URL(info.getUrl());
            HttpURLConnection sizeHttp = (HttpURLConnection)sizeUrl.openConnection();
            sizeHttp.setRequestMethod("GET");
            sizeHttp.connect();
            long totalSize = sizeHttp.getContentLength();
            sizeHttp.disconnect();

            if (totalSize <= 0){
                if (info.getFile().exists()){
                    info.getFile().delete();
                }
                dbHolder.deleteFileInfo(info.getUniqueId());
                LogUtils.e(TAG, "文件大小 = " + totalSize + "\t, 终止下载过程");
                return;
            }

            mFileInfo.setSize(totalSize);
            accessFile = new RandomAccessFile(info.getFile(), "rwd");

            URL url = new URL(info.getUrl());
            http = (HttpURLConnection)url.openConnection();
            http.setConnectTimeout(10000);
            http.setRequestProperty("Connection", "Keep-Alive");
            http.setReadTimeout(10000);
            http.setRequestProperty("Range", "bytes=" + mFileInfo.getDownloadLocation() + "-");
            http.connect();

            inStream = http.getInputStream();
            byte[] buffer = new byte[1024*8];
            int offset;

            accessFile.seek(mFileInfo.getDownloadLocation());
            long  millis = SystemClock.uptimeMillis();
            while ((offset = inStream.read(buffer)) != -1){
                if (isPause){
                    LogUtils.i(TAG, "下载过程 设置了 暂停");
                    mFileInfo.setDownloadStatus(DlStatus.PAUSE);
                    isPause = false;
                    dbHolder.saveFile(mFileInfo);
                    context.sendBroadcast(intent);

                    http.disconnect();
                    accessFile.close();
                    inStream.close();
                    return;
                }
                accessFile.write(buffer,0, offset);
                mFileInfo.setDownloadLocation( mFileInfo.getDownloadLocation()+offset );
                mFileInfo.setDownloadStatus(DlStatus.LOADING);

                if (SystemClock.uptimeMillis()-millis >= 1000){
                    millis = SystemClock.uptimeMillis();
                    dbHolder.saveFile(mFileInfo);
                    context.sendBroadcast(intent);
                }
            }
            mFileInfo.setDownloadStatus(DlStatus.COMPLETE);
            dbHolder.saveFile(mFileInfo);
            context.sendBroadcast(intent);
        } catch (Exception e){
            LogUtils.e(TAG, "下载过程发生失败"+e.getLocalizedMessage());
            mFileInfo.setDownloadStatus(DlStatus.FAIL);
            dbHolder.saveFile(mFileInfo);
            context.sendBroadcast(intent);
            e.printStackTrace();
        } finally {
            try {
                if (accessFile != null){
                    accessFile.close();
                }
                if (inStream != null){
                    inStream.close();
                }
                if (http != null){
                    http.disconnect();
                }
            }catch (IOException e){
                LogUtils.e(TAG, "finally 块  关闭文件过程中 发生异常");
                e.printStackTrace();
            }
        }
    }


}

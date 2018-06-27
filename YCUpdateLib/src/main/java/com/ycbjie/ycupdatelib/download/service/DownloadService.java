package com.ycbjie.ycupdatelib.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;


import com.ycbjie.ycupdatelib.download.constant.DlStatus;
import com.ycbjie.ycupdatelib.download.bean.FileInfo;
import com.ycbjie.ycupdatelib.download.bean.DownloadInfo;
import com.ycbjie.ycupdatelib.download.bean.RequestInfo;
import com.ycbjie.ycupdatelib.download.constant.DlConstant;
import com.ycbjie.ycupdatelib.download.db.DbHolder;
import com.ycbjie.ycupdatelib.download.execute.DownloadExecutor;
import com.ycbjie.ycupdatelib.download.execute.DownloadTask;
import com.ycbjie.ycupdatelib.download.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class DownloadService extends Service{

    public static final String TAG = "DownloadService";

    public static boolean canRequest = true;

    //关于线程池的一些配置
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(3, CPU_COUNT/2);
    private static final int MAX_POOL_SIZE =  CORE_POOL_SIZE * 2;
    private static final long KEEP_ALIVE_TIME  = 0L;

    private DownloadExecutor mExecutor = new DownloadExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
            KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());

    //存储任务
    private HashMap<String, DownloadTask> mTasks = new HashMap<>();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (canRequest){
            LogUtils.i(TAG, "onStartCommand() -> 启动了service服务 intent=" + intent + "\t this=" + this);
            canRequest = false;

            if (null!=intent && intent.hasExtra(DlConstant.Inner.SERVICE_INTENT_EXTRA)){
                try {
                    ArrayList<RequestInfo> requesetes =
                            (ArrayList<RequestInfo>)intent.getSerializableExtra(DlConstant.Inner.SERVICE_INTENT_EXTRA);
                    if (null != requesetes && requesetes.size()>0){
                        for (RequestInfo request : requesetes){
                            executeDownload(request);
                        }
                    }
                }catch (Exception e){
                    LogUtils.i(TAG, "onStartCommand()-> 接受数据,启动线程中发生异常");
                    e.printStackTrace();
                }
            }
            canRequest = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //Todo  除了简单的synchronized, 是否有更好的方式来进行加锁呢
    private synchronized void executeDownload(RequestInfo requestInfo){
        DownloadInfo mDownloadInfo = requestInfo.getDownloadInfo();

        //先看看在任务列表里，是否有这个任务
        DownloadTask task = mTasks.get(mDownloadInfo.getUniqueId());
        DbHolder dbHolder = new DbHolder(getBaseContext());
        FileInfo mFileInfo = dbHolder.getFileInfo(mDownloadInfo.getUniqueId());

        LogUtils.i(TAG, "executeDownload() -> task=" + task + "\t mFileInfo=" + mFileInfo);


        if (null == task){ //之前没有类似任务
            if (null != mFileInfo){
                if (mFileInfo.getDownloadStatus()== DlStatus.LOADING
                        || mFileInfo.getDownloadStatus()== DlStatus.PREPARE){
                    //修正文件状态
                    dbHolder.updateState(mFileInfo.getId(), DlStatus.PAUSE);
                }else if (mFileInfo.getDownloadStatus() == DlStatus.COMPLETE){
                    if (mDownloadInfo.getFile().exists()){
                        if (!TextUtils.isEmpty(mDownloadInfo.getAction())){
                            Intent intent = new Intent();
                            intent.setAction(mDownloadInfo.getAction());
                            intent.putExtra(DlConstant.EXTRA_INTENT_DOWNLOAD, mFileInfo);
                            sendBroadcast(intent);
                        }
                        return;
                    } else {
                        dbHolder.deleteFileInfo(mDownloadInfo.getUniqueId());
                    }
                }
            }//end of "  null != mFileInfo "

            //创建一个下载任务
            if (requestInfo.getDictate() == DlConstant.RequestCode.loading){
                task = new DownloadTask(this, mDownloadInfo, dbHolder);
                mTasks.put(mDownloadInfo.getUniqueId(), task);
            }
        }else {
            // 什么情况下, 可能存在这种这种状态
            if (task.getStatus()== DlStatus.COMPLETE || task.getStatus()== DlStatus.LOADING){
                if (!mDownloadInfo.getFile().exists()){
                    task.pause();
                    mTasks.remove(mDownloadInfo.getUniqueId());
                    LogUtils.i(TAG, " 状态标示完成，但是文件不存在，重新执行下载文件  ");
                    executeDownload(requestInfo);
                    return;
                }
            }
        }

        if (null != task){
            if (requestInfo.getDictate() == DlConstant.RequestCode.loading){
                mExecutor.executeTask(task);
            }else {
                task.pause();
            }
        }
    }
}

package com.ycbjie.ycupdatelib.download.execute;


import android.content.Intent;


import com.ycbjie.ycupdatelib.download.constant.DlStatus;
import com.ycbjie.ycupdatelib.download.constant.DlConstant;
import com.ycbjie.ycupdatelib.download.utils.LogUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class DownloadExecutor extends ThreadPoolExecutor{
    
    private static final String TAG = "DownloadExecutor";
    
    public DownloadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                            TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public void executeTask(DownloadTask task){
        int status = task.getStatus();
        if (status== DlStatus.PAUSE || status== DlStatus.FAIL){
            task.setFileStatus(DlStatus.WAIT);

            Intent intent = new Intent();
            intent.setAction(task.getDownLoadInfo().getAction());
            intent.putExtra(DlConstant.EXTRA_INTENT_DOWNLOAD, task.getFileInfo());
            task.sendBroadcast(intent);

            execute(task);
        }else {
            LogUtils.w(TAG, "文件状态不正确, 不进行下载 FileInfo=" + task.getFileInfo());
        }
    }
}

package com.ycbjie.ycupdatelib.download.help;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;


import com.ycbjie.ycupdatelib.dialog.UpdateFragment;
import com.ycbjie.ycupdatelib.download.bean.DownloadInfo;
import com.ycbjie.ycupdatelib.download.bean.RequestInfo;
import com.ycbjie.ycupdatelib.download.constant.DlConstant;
import com.ycbjie.ycupdatelib.download.service.DownloadService;
import com.ycbjie.ycupdatelib.download.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;


public class DownloadHelper {
    
    private static final String TAG = "DownloadHelper";
    private volatile static DownloadHelper SINGLE;
    private static ArrayList<RequestInfo> requests = new ArrayList<>();

    private DownloadHelper(){}

    public static DownloadHelper getInstance(){
        if (SINGLE == null){
            synchronized (DownloadHelper.class){
                if (SINGLE == null){
                    SINGLE = new DownloadHelper();
                }
            }
        }
        return SINGLE;
    }


    /**
     * 提交                           下载/暂停  等任务.(提交就意味着开始执行生效)
     * @param context                上下文
     */
    public synchronized void submit(Context context){
        if (requests.isEmpty()){
            LogUtils.w("没有下载任务可供执行");
            return;
        }
        //开启服务service
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DlConstant.Inner.SERVICE_INTENT_EXTRA, requests);
        context.startService(intent);
        requests.clear();
    }


    /**
     * 添加新的下载任务
     *
     * @param url                   下载的url
     * @param file                  存储在某个位置上的文件
     * @param action                下载过程会发出广播信息.该参数是广播的action
     * @return                      DownloadHelper自身 (方便链式调用)
     */
    public DownloadHelper addTask(String url, File file, @Nullable String action){
        RequestInfo requestInfo = createRequest(url, file, action, DlConstant.RequestCode.loading);
        LogUtils.i(TAG, "addTask() requestInfo=" + requestInfo);
        requests.add(requestInfo);
        return this;
    }


    /**
     * 暂停某个下载任务
     *
     * @param url                   下载的url
     * @param file                  存储在某个位置上的文件
     * @param action                下载过程会发出广播信息.该参数是广播的action
     * @return                      DownloadHelper自身 (方便链式调用)
     */
    public DownloadHelper pauseTask(String url, File file, @Nullable String action){
        RequestInfo requestInfo = createRequest(url, file, action, DlConstant.RequestCode.pause);
        LogUtils.i(TAG, "pauseTask() -> requestInfo=" + requestInfo);
        requests.add(requestInfo);
        return this;
    }


    /**
     * 设定该模块是否输出 debug信息
     */
    public DownloadHelper setDebug(boolean isDebug){
        LogUtils.setDebug(isDebug);
        return this;
    }


    private RequestInfo createRequest(String url, File file, String action,
                                      @DlConstant.RequestCode int dictate){
        RequestInfo request = new RequestInfo();
        request.setDictate(dictate);
        request.setDownloadInfo(new DownloadInfo(url, file, action));
        LogUtils.i(TAG, "createRequest=" + file);
        return request;
    }

}

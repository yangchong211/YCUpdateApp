package com.ycbjie.ycupdatelib.dialog;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ycbjie.ycupdatelib.R;
import com.ycbjie.ycupdatelib.download.help.DownloadHelper;
import com.ycbjie.ycupdatelib.download.constant.DlStatus;
import com.ycbjie.ycupdatelib.download.bean.FileInfo;
import com.ycbjie.ycupdatelib.download.constant.DlConstant;
import com.ycbjie.ycupdatelib.download.utils.LogUtils;
import com.ycbjie.ycupdatelib.download.utils.UpdateUtils;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@SuppressLint("ValidFragment")
public class UpdateFragment extends BaseDialogFragment implements View.OnClickListener {

    private ProgressBar mProgress;
    private TextView mTvCancel;
    private TextView mTvOk;

    private static final String DOWNLOAD_ACTION = "download_action";
    private File dlAppFile;
    private DownloadHelper mDownloadHelper;

    private boolean isForceUpdate;
    private String desc;
    private String url;
    private String apkFileName;
    private String packName;


    /**
     * 更新
     * @param isForceUpdate             是否强制更新
     * @param desc                      更新文案
     * @param url                       下载链接
     * @param apkFileName               apk下载文件路径名称
     * @param packName                  包名
     */
    @SuppressLint("ValidFragment")
    public UpdateFragment(boolean isForceUpdate,String desc,String url,String apkFileName,String packName) {
        this.isForceUpdate = isForceUpdate;
        this.desc = desc;
        this.url = url;
        this.apkFileName = apkFileName;
        this.packName = packName;
    }


    @Retention(RetentionPolicy.SOURCE)
    public @interface DlType {
        String START = "开始";
        String PAUSE = "暂停";
        String STOP = "停止";
        String ERROR = "错误，请重试";
        String COMPLETE = "下载完成";
    }

    /**
     * 通过广播形式发送消息来更新下载进度信息
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null && intent.getAction()!=null){
                String action = intent.getAction();
                switch (action){
                    case DOWNLOAD_ACTION:
                        FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(
                                DlConstant.EXTRA_INTENT_DOWNLOAD);
                        updateView(mProgress, fileInfo, mTvOk);
                        break;
                    default:
                        break;
                }
            }
        }
    };


    private void updateView(ProgressBar mProgress, FileInfo fileInfo, TextView mTvOk) {
        float pro = (float) (fileInfo.getDownloadLocation()*1.0 / fileInfo.getSize());
        int progress = (int)(pro * 100);
        String filePath = fileInfo.getFilePath();
        LogUtils.e("progress-------",progress+"-------"+filePath);
        int downloadStatus = fileInfo.getDownloadStatus();
        switch (downloadStatus){
            case DlStatus.FAIL:
                mTvOk.setText(DlType.ERROR);
                break;
            case DlStatus.COMPLETE:
                mTvOk.setText(DlType.COMPLETE);
                mProgress.setProgress(progress);
                break;
            case DlStatus.LOADING:
                if(progress>0){
                    mProgress.setVisibility(View.VISIBLE);
                }
                mProgress.setProgress(progress);
                break;
            case DlStatus.PAUSE:

                break;
            case DlStatus.PREPARE:

                break;
            case DlStatus.WAIT:

                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(receiver!=null){
            getContext().unregisterReceiver(receiver);
        }
        dismissDialog();
    }

    @Override
    protected boolean isCancel() {
        return !isForceUpdate;
    }


    @Override
    public int getLayoutRes() {
        return R.layout.fragment_update_app;
    }

    @Override
    public void bindView(View v) {
        initFindViewId(v);
        initListener();
        //initSetPath();
        initDownload();
    }

    private void initFindViewId(View v) {
        TextView mTvDesc = v.findViewById(R.id.tv_desc);
        mProgress = v.findViewById(R.id.progress);
        mTvCancel = v.findViewById(R.id.tv_cancel);
        mTvOk = v.findViewById(R.id.tv_ok);

        if(isForceUpdate){
            mTvOk.setVisibility(View.VISIBLE);
            mTvCancel.setVisibility(View.GONE);
        }else {
            mTvOk.setVisibility(View.VISIBLE);
            mTvCancel.setVisibility(View.VISIBLE);
        }
        mTvOk.setText(DlType.START);
        mTvDesc.setText(desc);
    }


    private void initSetPath(){
        String saveApkPath = UpdateUtils.getLocalApkDownSavePath("id");
        dlAppFile = new File(saveApkPath);
        if (dlAppFile.exists()) {
            //下载完成
            mTvOk.setVisibility(View.VISIBLE);
            mTvCancel.setVisibility(View.GONE);
            mTvOk.setText(DlType.COMPLETE);
        } else {
            dlAppFile.mkdirs();
            //即将下载
            mDownloadHelper = DownloadHelper.getInstance();
            mDownloadHelper.setDebug(true);
            IntentFilter filter = new IntentFilter();
            filter.addAction(DOWNLOAD_ACTION);
            getContext().registerReceiver(receiver, filter);
        }
    }


    private void initDownload() {
        File dir = new File(getContext().getExternalCacheDir(), "download");
        if (!dir.exists()){
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        dlAppFile = new File(dir, apkFileName);


        //测试由此抽象路径名表示的文件或目录是否存在
        if(dlAppFile.exists()){
            //下载完成
            mTvOk.setVisibility(View.VISIBLE);
            mTvCancel.setVisibility(View.GONE);
            mTvOk.setText(DlType.COMPLETE);
        }else {
            mDownloadHelper = DownloadHelper.getInstance();
            mDownloadHelper.setDebug(true);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(DOWNLOAD_ACTION);
        getContext().registerReceiver(receiver, filter);
    }

    private void initListener() {
        mTvOk.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
        if(getDialog()!=null){
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    switch (keyCode) {
                        // 返回键
                        case KeyEvent.KEYCODE_BACK:
                            if (isForceUpdate) {
                                return true;
                            }
                        default:
                            break;
                    }
                    return false;
                }
            });
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_ok) {
            String content = mTvOk.getText().toString().trim();
            switch (content){
                case DlType.START:
                    mDownloadHelper.addTask(url, dlAppFile, DOWNLOAD_ACTION).submit(getContext());
                    mTvOk.setText(DlType.PAUSE);
                    break;
                case DlType.PAUSE:
                    mDownloadHelper.pauseTask(url, dlAppFile, DOWNLOAD_ACTION).submit(getContext());
                    mTvOk.setText(DlType.START);
                    break;
                case DlType.COMPLETE:
                    if (dlAppFile.exists()) {
                        //检测是否有apk文件，如果有直接普通安装
                        String filePath = dlAppFile.getPath();
                        String absolutePath = dlAppFile.getAbsolutePath();
                        LogUtils.e("下载" , filePath+"-----"+absolutePath);
                        UpdateUtils.installNormal(getContext(), filePath , packName);
                    }
                    break;
            }
        }else if(i == R.id.tv_cancel){
            mDownloadHelper.pauseTask(url, dlAppFile, DOWNLOAD_ACTION).submit(getContext());
            dismissDialog();
        }
    }

    public String getFilePath(){
        if(dlAppFile!=null){
            return dlAppFile.getPath();
        }
        return null;
    }


}

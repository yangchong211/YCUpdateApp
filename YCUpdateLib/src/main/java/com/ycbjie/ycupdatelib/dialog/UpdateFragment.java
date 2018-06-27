package com.ycbjie.ycupdatelib.dialog;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ycbjie.ycupdatelib.R;
import com.ycbjie.ycupdatelib.download.help.DownloadHelper;
import com.ycbjie.ycupdatelib.download.constant.DlStatus;
import com.ycbjie.ycupdatelib.download.bean.FileInfo;
import com.ycbjie.ycupdatelib.download.constant.DlConstant;
import com.ycbjie.ycupdatelib.download.utils.UpdateUtils;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@SuppressLint("ValidFragment")
public class UpdateFragment extends BaseDialogFragment implements View.OnClickListener {

    private ProgressBar mProgress;
    private TextView mTvCancel;
    private TextView mTvOk;

    private static final String pathName = "com.paidian.hwmc";
    private static final String DOWNLOAD_ACTION = "download_action";
    private static final String url = "http://dynamic.12306.cn/otn/appDownload/androiddownload";
    private File dir;
    private static final String dlAppName = "yc.apk";
    private File dlAppFile;
    private DownloadHelper mDownloadHelper;
    private String filePath;
    private boolean isForceUpdate;

    @SuppressLint("ValidFragment")
    public UpdateFragment(boolean isForceUpdate) {
        this.isForceUpdate = isForceUpdate;
    }


    @Retention(RetentionPolicy.SOURCE)
    public @interface DlType {
        String START = "开始";
        String PAUSE = "暂停";
        String STOP = "停止";
        String ERROR = "错误";
        String COMPLETE = "下载完成";
    }



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
        filePath = fileInfo.getFilePath();
        Log.e("progress",progress+"");
        if(progress>0){
            mProgress.setVisibility(View.VISIBLE);
        }
        mProgress.setProgress(progress);
        if (fileInfo.getDownloadStatus() == DlStatus.COMPLETE){
            mTvOk.setText(DlType.COMPLETE);
            //自动安装apk文件
            UpdateUtils.installNormal(getContext(), filePath, pathName);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(receiver!=null){
            getContext().unregisterReceiver(receiver);
        }
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
        initDownload();
    }

    private void initFindViewId(View v) {
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
    }


    private void initDownload() {
        mDownloadHelper = DownloadHelper.getInstance();
        dlAppFile = new File(getDir(), dlAppName);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DOWNLOAD_ACTION);
        getContext().registerReceiver(receiver, filter);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File getDir(){
        if (dir !=null && dir.exists()){
            return dir;
        }
        dir = new File(getContext().getExternalCacheDir(), "download");
        if (!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }

    private void initListener() {
        mTvOk.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
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
                        UpdateUtils.installNormal(getContext(), filePath , pathName);
                    }
                    break;
            }
        }else if(i == R.id.tv_cancel){
            mDownloadHelper.pauseTask(url, dlAppFile, DOWNLOAD_ACTION).submit(getContext());
            dismissDialog();
        }
    }

}

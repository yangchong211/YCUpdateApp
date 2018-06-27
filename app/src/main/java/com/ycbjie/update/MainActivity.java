package com.ycbjie.update;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ycbjie.ycupdatelib.dialog.BaseDialogFragment;
import com.ycbjie.ycupdatelib.dialog.UpdateFragment;
import com.ycbjie.ycupdatelib.download.help.DownloadHelper;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String pathName = "com.ycbjie.update";
    private static final String firstUrl = "http://ucan.25pp.com/Wandoujia_web_seo_baidu_homepage.apk";
    private static final String secondUrl = "http://www.meituan.com/mobile/download/meituan/android/meituan?from=new";
    private static final String thirdUrl = "http://dynamic.12306.cn/otn/appDownload/androiddownload";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String  desc = getResources().getString(R.string.update_content_info);
                /*
                 * @param isForceUpdate             是否强制更新
                 * @param desc                      更新文案
                 * @param url                       下载链接
                 * @param apkFileName               apk下载文件路径名称
                 * @param packName                  包名
                 */
                UpdateFragment updateFragment = new UpdateFragment(false,desc,firstUrl,"apk1",pathName);
                updateFragment.show(MainActivity.this , getSupportFragmentManager());
                //获取下载文件的路径
                String filePath = updateFragment.getFilePath();
                //弹窗销毁监听listener
                updateFragment.setLoadFinishListener(new BaseDialogFragment.onLoadFinishListener() {
                    @Override
                    public void listener(boolean isSuccess) {

                    }
                });
            }
        });

        findViewById(R.id.tv_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String  desc = getResources().getString(R.string.update_content_info1);
                UpdateFragment updateFragment = new UpdateFragment(
                        true,desc,secondUrl,"apk2",pathName);
                updateFragment.show(MainActivity.this , getSupportFragmentManager());
                updateFragment.setLoadFinishListener(new BaseDialogFragment.onLoadFinishListener() {
                    @Override
                    public void listener(boolean isSuccess) {

                    }
                });
            }
        });

        findViewById(R.id.tv_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String  desc = getResources().getString(R.string.update_content_info1);
                UpdateFragment updateFragment = new UpdateFragment(
                        false,desc,thirdUrl,"apk3",pathName);
                updateFragment.show(MainActivity.this , getSupportFragmentManager());
                updateFragment.setLoadFinishListener(new BaseDialogFragment.onLoadFinishListener() {
                    @Override
                    public void listener(boolean isSuccess) {

                    }
                });
            }
        });
    }



}

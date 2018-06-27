package com.ycbjie.ycupdatelib.download.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.IntRange;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.ycbjie.ycupdatelib.download.constant.DlStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class UpdateUtils {

    public static String getStatusDesc(@IntRange(from = DlStatus.WAIT, to = DlStatus.FAIL) int status){
        switch (status){
            case DlStatus.WAIT:
                return " wait ";
            case DlStatus.PREPARE:
                return " prepare ";
            case DlStatus.LOADING:
                return " loading ";
            case DlStatus.PAUSE:
                return " pause ";
            case DlStatus.COMPLETE:
                return " complete ";
            case DlStatus.FAIL:
                return " fail ";
            default:
                return "  错误的未知状态 ";
        }
    }

    public static final String APP_UPDATE_DOWN_APK_PATH = "yc" + File.separator + "downApk";
    public static String getLocalApkDownSavePath(String versionId){
        String saveApkPath= APP_UPDATE_DOWN_APK_PATH+ File.separator;
        String sdPath = getInnerSDCardPath();
        if (!isExistSDCard() || TextUtils.isEmpty(sdPath)) {
            ArrayList<String> sdPathList = getExtSDCardPath();
            if (sdPathList != null && sdPathList.size() > 0 && !TextUtils.isEmpty(sdPathList.get(0))) {
                sdPath = sdPathList.get(0);
            }
        }
        String saveApkDirs = sdPath+File.separator+saveApkPath;
        File file = new File(saveApkDirs);
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            file.mkdirs();
        }
        saveApkPath = saveApkDirs + versionId+".apk";
        return saveApkPath;
    }

    /**
     * 判断是否有sd卡
     * @return                      是否有sd
     */
    private static boolean isExistSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else{
            return false;
        }
    }


    /**
     * 获取内置SD卡路径
     * @return                      路径
     */
    private static String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 获取外置SD卡路径
     * @return 应该就一条记录或空
     */
    private static ArrayList<String> getExtSDCardPath() {
        ArrayList<String> lResult = new ArrayList<>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("extSdCard")) {
                    String[] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory()) {
                        lResult.add(path);
                    }
                }
            }
            isr.close();
        } catch (Exception ignored) {
        }
        return lResult;
    }



    /*
     注意配置清单文件
     参考博客：https://www.cnblogs.com/newjeremy/p/7294519.html
     <provider
     android:name="android.support.v4.content.FileProvider"
     android:authorities="你的包名.fileprovider"
     android:exported="false"
     android:grantUriPermissions="true">
     <meta-data
     android:name="android.support.FILE_PROVIDER_PATHS"
     android:resource="@xml/file_paths" />
     </provider>
     */
    /**
     * 关于在代码中安装 APK 文件，在 Android N 以后，为了安卓系统为了安全考虑，不能直接访问软件
     * 需要使用 fileProvider 机制来访问、打开 APK 文件。
     * 普通安装
     * @param context                   上下文
     * @param apkPath                   path
     * @param pathName                  你的包名
     */
    public static void installNormal(Context context, String apkPath , String pathName) {
        if(apkPath==null || pathName==null){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File apkFile = new File(apkPath);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //版本在7.0以上是不能直接通过uri访问的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(context, pathName+".fileProvider", apkFile);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            Uri uri = Uri.fromFile(apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }


}

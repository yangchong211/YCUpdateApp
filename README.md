# 自定义版本更新弹窗
#### 目录介绍
- 1.使用介绍
- 2.注意要点
- 3.效果展示
- 4.其他介绍


### 1.使用介绍
#### 1.1 关于库导入
- 在build.gradle中直接导入：compile 'cn.yc:YCUpdateLib:1.0.1'

#### 1.2 使用说明

```
/*
 * @param isForceUpdate             是否强制更新
 * @param desc                      更新文案
 * @param url                       下载链接
 * @param apkFileName               apk下载文件路径名称
 * @param packName                  包名
 */
UpdateFragment updateFragment = new UpdateFragment(false,desc,firstUrl,"apk1",pathName);
//弹出弹窗
updateFragment.show(MainActivity.this , getSupportFragmentManager());
//获取下载文件的路径
String filePath = updateFragment.getFilePath();
//弹窗销毁监听listener
updateFragment.setLoadFinishListener(new BaseDialogFragment.onLoadFinishListener() {
    @Override
    public void listener(boolean isSuccess) {

    }
});
```

#### 1.3 在代码中安装 APK 文件
- 直接调用工具类UpdateUtils.installNormal方法
- 关于在代码中安装 APK 文件，在 Android N 以后，为了安卓系统为了安全考虑，不能直接访问软件，需要使用 fileProvider 机制来访问、打开 APK 文件。里面if 语句，就是区分软件运行平台，来对 intent 设置不同的属性。

```
/**
 * 关于在代码中安装 APK 文件，在 Android N 以后，为了安卓系统为了安全考虑，不能直接访问软件
 * 需要使用 fileProvider 机制来访问、打开 APK 文件。
 * 普通安装
 * @param context                   上下文
 * @param apkPath                   path，文件路径
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
```

#### 1.4 清单文件
- 1.4.1 清单文件添加代码如下所示：

```
<application>
    <provider
        android:name="android.support.v4.content.FileProvider"
        android:authorities="你的包名.fileprovider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>
</application>    
```

- 1.4.2 在res/xml下增加文件：file_paths.xml

```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
        <external-path path="." name="external_files"/>
        <external-path path="." name="download"/>
</paths>
```



### 2.注意要点

### 3.效果展示


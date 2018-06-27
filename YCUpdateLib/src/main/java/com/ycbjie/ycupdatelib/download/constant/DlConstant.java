package com.ycbjie.ycupdatelib.download.constant;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DlConstant {

    //Db 数据库中用到的字段
    public static class Db {
        public static final String id = "id";
        public static final String downloadUrl = "downloadUrl";
        public static final String filePath = "filePath";
        public static final String size = "size";
        public static final String downloadLocation = "downloadLocation";
        public static final String downloadStatus = "downloadStatus";
        public static final String NAME_TABLE = "download_info";
        public static final String NAME_DB = "download.Db";
    }


    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestCode {
        //下载状态
        int loading = 10;
        //暂停状态
        int pause = 11;
        //停止状态
        int stop = 12;
        //错误状态
        int error = 13;
    }


    public static class Inner {
        public static final String SERVICE_INTENT_EXTRA = "service_intent_extra";
    }

    /**
     * 下载过程会通过发送广播, 广播通过intent携带文件数据的 信息。
     * intent 的key值就是该字段
     * eg : FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(DownloadConstant.EXTRA_INTENT_DOWNLOAD);
     */
    public static final String EXTRA_INTENT_DOWNLOAD = "yc_download_extra";

}

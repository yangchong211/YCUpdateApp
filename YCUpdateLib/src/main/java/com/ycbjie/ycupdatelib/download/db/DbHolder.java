package com.ycbjie.ycupdatelib.download.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.ycbjie.ycupdatelib.download.bean.FileInfo;
import com.ycbjie.ycupdatelib.download.constant.DlConstant;

import java.io.File;

/**
 * <pre>
 *     @author yangchong
 *     blog  :
 *     time  : 2017/8/9
 *     desc  : db
 *     revise:
 * </pre>
 */
public class DbHolder {

    private Context context;
    private SQLiteDatabase mDb;

    public DbHolder(Context context) {
        this.context = context;
        mDb = new DbOpenHelper(context).getWritableDatabase();
    }

    public void saveFile(FileInfo downloadFile){
        if (null == downloadFile){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DlConstant.Db.id, downloadFile.getId());
        values.put(DlConstant.Db.downloadUrl, downloadFile.getDownloadUrl());
        values.put(DlConstant.Db.filePath, downloadFile.getFilePath());
        values.put(DlConstant.Db.size, downloadFile.getSize());
        values.put(DlConstant.Db.downloadLocation, downloadFile.getDownloadLocation());
        values.put(DlConstant.Db.downloadStatus, downloadFile.getDownloadStatus());

        if (has(downloadFile.getId())){
            mDb.update(DlConstant.Db.NAME_TABLE, values, DlConstant.Db.id + " = ?", new String[]{downloadFile.getId()});
        }else {
            mDb.insert(DlConstant.Db.NAME_TABLE, null, values);
        }

    }//end of "saveFile(..."


    public void updateState(String id, int state){
        if (TextUtils.isEmpty(id)){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DlConstant.Db.downloadStatus, state);
        mDb.update(DlConstant.Db.NAME_TABLE, values, DlConstant.Db.id + " = ?", new String[]{id});
    }


    public FileInfo getFileInfo(String id){
        Cursor cursor = mDb.query(DlConstant.Db.NAME_TABLE, null,
                " " + DlConstant.Db.id + " = ? ", new String[]{id},
                null, null, null);
        FileInfo downloadFile = null;
        while (cursor.moveToNext()){
            downloadFile = new FileInfo();
            downloadFile.setId( cursor.getString(cursor.getColumnIndex( DlConstant.Db.id)) );
            downloadFile.setDownloadUrl( cursor.getString(cursor.getColumnIndex( DlConstant.Db.downloadUrl)) );
            downloadFile.setFilePath( cursor.getString(cursor.getColumnIndex( DlConstant.Db.filePath)) );
            downloadFile.setSize( cursor.getLong( cursor.getColumnIndex(DlConstant.Db.size)) );
            downloadFile.setDownloadLocation( cursor.getLong( cursor.getColumnIndex(DlConstant.Db.downloadLocation)));
            downloadFile.setDownloadStatus( cursor.getInt(cursor.getColumnIndex(DlConstant.Db.downloadStatus)) );

            File file = new File(downloadFile.getFilePath());
            if (!file.exists()){
                deleteFileInfo(id);
                return null;
            }
        }
        cursor.close();
        return downloadFile;
    }


    /**
     * 根据id 来删除对应的文件信息
     * @param id
     */
    public void deleteFileInfo(String id){
        if (has(id)){
            mDb.delete(DlConstant.Db.NAME_TABLE, DlConstant.Db.id + " = ?", new String[]{id});
        }
    }

    private boolean has(String id){
        Cursor cursor = mDb.query(DlConstant.Db.NAME_TABLE, null,  " " + DlConstant.Db.id + " = ? ", new String[]{id}, null, null, null);
        boolean has = cursor.moveToNext();
        cursor.close();
        return has;
    }
}

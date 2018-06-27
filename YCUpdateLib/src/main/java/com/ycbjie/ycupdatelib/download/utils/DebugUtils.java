package com.ycbjie.ycupdatelib.download.utils;


import android.support.annotation.IntRange;

import com.ycbjie.ycupdatelib.download.constant.DlStatus;
import com.ycbjie.ycupdatelib.download.constant.DlConstant;


public class DebugUtils {

    public static String getStatusDesc(@IntRange(from = DlStatus.WAIT,
            to = DlStatus.FAIL) int status){
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

    public static String getRequestDictateDesc(@IntRange(from = DlConstant.RequestCode.loading,
            to = DlConstant.RequestCode.pause) int dictate){
        switch (dictate){
            case DlConstant.RequestCode.loading:
                return " loading ";
            case DlConstant.RequestCode.pause:
                return " pause ";
            default:
                return " dictate描述错误  ";
        }
    }
}

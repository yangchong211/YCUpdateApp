package com.ycbjie.updatelib;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;


/**
 * ================================================
 * 作    者：杨充
 * 版    本：1.0
 * 创建日期：2017/8/9
 * 描    述：自定义布局弹窗
 * 修订历史：
 * ================================================
 */
public abstract class BaseDialogFragment extends DialogFragment {

    private static final String TAG = "base_dialog";
    private static final float DEFAULT_DIM = 0.2f;
    private Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.BottomDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialog = getDialog();
        if(dialog!=null){
            if(dialog.getWindow()!=null){
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            }
            dialog.setCanceledOnTouchOutside(isCancel());
            dialog.setCancelable(isCancel());
        }
        View v = inflater.inflate(getLayoutRes(), container, false);
        bindView(v);
        return v;
    }

    /**
     * 设置是否可以cancel
     * @return
     */
    protected abstract boolean isCancel();

    /**
     * 获取布局资源文件
     * @return              布局资源文件id值
     */
    @LayoutRes
    public abstract int getLayoutRes();

    /**
     * 绑定
     * @param v             view
     */
    public abstract void bindView(View v);

    /**
     * 开始展示
     */
    @Override
    public void onStart() {
        super.onStart();
        if(dialog==null){
            dialog = getDialog();
        }
        Window window = dialog.getWindow();
        if(window!=null){
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = getDimAmount();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            if (getHeight() > 0) {
                params.height = getHeight();
            } else {
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            }
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mListener!=null){
            mListener.listener(true);
        }
        dismissDialog();
    }

    /**
     * 获取弹窗高度
     * @return          int类型值
     */
    public int getHeight() {
        return -1;
    }

    public float getDimAmount() {
        return DEFAULT_DIM;
    }

    public boolean getCancelOutside() {
        return true;
    }

    public String getFragmentTag() {
        return TAG;
    }

    public void show(Context context , FragmentManager fragmentManager) {
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }
        if(fragmentManager!=null){
            show(fragmentManager, getFragmentTag());
        }else {
            Log.e("show","需要设置setFragmentManager");
        }
    }

    /**
     * 一定要销毁dialog，设置为null，防止内存泄漏
     * GitHub地址：https://github.com/yangchong211
     * 如果可以，欢迎star
     */
    public void dismissDialog(){
        if(dialog!=null && dialog.isShowing()){
            dialog.dismiss();
            dialog = null;
        }
    }

    public onLoadFinishListener mListener;
    public void setLoadFinishListener(onLoadFinishListener listener){
        mListener = listener;
    }

    public interface onLoadFinishListener{
        void listener(boolean isSuccess);
    }

}

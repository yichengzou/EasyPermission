package com.taobao.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

/**
 * Description:
 * 更简易的运行时权限检查方案
 * <p>
 * 使用
 * <p>
 * step1:
 * <p>
 * 通过{@link EasyPermission.Builder}构建EasyPermission实例(Callback/要申请的权限/相关文案)
 * <p>
 * step2:
 * <p>
 * 检查权限 {@link EasyPermission#check()},在{@link Activity#onActivityResult(int, int, Intent)}（如果在fragment中调用Fragment的onActivityResult即可）中
 * 调用{@link EasyPermission#handleResult(int, int, Intent)}分发结果
 * <p>
 * step3:
 * <p>
 * 在{@link PermissionResultCallback}中处理结果
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/3/21<br/>
 * Time: 上午10:10<br/>
 */
@SuppressWarnings("unused")
public class EasyPermission {
    private static final String TAG = "EasyPermission";
    private String rationalMessage;
    private String deniedMessage;
    private boolean showSettingBtn;
    private String deniedCloseBtnText;
    private String deniedSettingsBtnText;
    private String rationalConfirmBtnText;
    private String[] permissions;

    private PermissionResultCallback resultCallback;

    private Context context;

    private Fragment mSupportFragment;


    private EasyPermission(Builder builder) {
        this.rationalMessage = builder.rationalMessage;
        this.deniedMessage = builder.deniedMessage;
        this.showSettingBtn = builder.showSettingBtn;
        this.deniedCloseBtnText = builder.deniedCloseBtnText;
        if (TextUtils.isEmpty(deniedCloseBtnText)) {
            this.deniedCloseBtnText = "关闭";
        }
        this.deniedSettingsBtnText = builder.deniedSettingsBtnText;
        if (TextUtils.isEmpty(deniedSettingsBtnText)) {
            this.deniedSettingsBtnText = "去设置";
        }
        this.rationalConfirmBtnText = builder.rationalConfirmBtnText;
        if (TextUtils.isEmpty(rationalConfirmBtnText)) {
            this.rationalConfirmBtnText = "我知道了";
        }
        this.permissions = builder.permissions;
        if (this.permissions == null || this.permissions.length == 0) {
            throw new IllegalArgumentException("no permission found...");
        }

        this.resultCallback = builder.resultCallback;
        if (this.resultCallback == null) {
            throw new RuntimeException("callback is null...");
        }
        this.context = builder.context;
        this.mSupportFragment = builder.mSupportFragment;
    }


    /**
     * 检查权限,检查结果将通过{@link PermissionResultCallback}通知
     */
    public void check() {
        Intent intent = new Intent(context, PermissionController.class);
        intent.putExtra(PermissionController.EXTRA_PERMISSIONS, this.permissions);
        intent.putExtra(PermissionController.EXTRA_DENIED_CLOSE_BTN, this.deniedCloseBtnText);
        intent.putExtra(PermissionController.EXTRA_DENIED_MESSAGE, this.deniedMessage);
        intent.putExtra(PermissionController.EXTRA_DENIED_SETTINGS_BTN, this.deniedSettingsBtnText);
        intent.putExtra(PermissionController.EXTRA_RATIONAL_CONFIRM_BTN, this.rationalConfirmBtnText);
        intent.putExtra(PermissionController.EXTRA_SHOW_SETTINGS_BTN, this.showSettingBtn);
        intent.putExtra(PermissionController.EXTRA_RATIONAL_MESSAGE, this.rationalMessage);
        if(mSupportFragment != null){
            mSupportFragment.startActivityForResult(intent, PermissionController.REQ_CODE_GET_CALLBACK);
        }else if(context != null){
            ((Activity) context).startActivityForResult(intent, PermissionController.REQ_CODE_GET_CALLBACK);
        }else{
            Log.e(TAG,"you must specify a fragment or context...");
        }

    }

    /**
     * 处理权限检查的结果.
     * <p>
     * 你必须在{@link android.app.Activity#onActivityResult(int, int, Intent)}方法中调用此方法
     */
    public void handleResult(int requestCode, int resultCode, Intent intent) {
        if (intent == null || resultCallback == null || requestCode != PermissionController.REQ_CODE_GET_CALLBACK
                || resultCode != Activity.RESULT_OK) {
            return;
        }

        boolean result = intent.getBooleanExtra(PermissionController.REQ_RESULT, false);
        String[] permissionList = intent.getStringArrayExtra(PermissionController.RESULT_DATA);
        if (result) {
            this.resultCallback.onPermissionGranted(Arrays.asList(permissionList));
        } else {
            this.resultCallback.onPermissionDenied(Arrays.asList(permissionList));
        }

    }


    public static class Builder {
        private String rationalMessage;
        private String deniedMessage;
        private boolean showSettingBtn;
        private String deniedCloseBtnText;
        private String deniedSettingsBtnText;
        private String rationalConfirmBtnText;
        private String[] permissions;

        private PermissionResultCallback resultCallback;

        private Context context;

        private Fragment mSupportFragment;

        /**
         * 如果是在activity中检测权限，请务必调用此构造器
         * */
        public Builder(@NonNull Activity context, PermissionResultCallback callback) {
            this.resultCallback = callback;
            this.context = context;
        }

        /**
         * 如果是在fragment中检测权限，请务必调用此构造器
         * */
        public Builder(@NonNull Activity context,@NonNull Fragment fragment, PermissionResultCallback callback) {
            this.resultCallback = callback;
            this.context = context;
            this.mSupportFragment = fragment;
        }



        public Builder permission(String... permissions) {
            this.permissions = permissions;
            return this;
        }


        public Builder rationalMessage(String rationalMessage) {
            this.rationalMessage = rationalMessage;
            return this;
        }

        public Builder deniedMessage(String deniedMessage) {
            this.deniedMessage = deniedMessage;
            return this;
        }

        public Builder settingBtn(boolean showSettingBtn) {
            this.showSettingBtn = showSettingBtn;
            return this;
        }

        public Builder deniedCloseBtnText(String deniedCloseBtnText) {
            this.deniedCloseBtnText = deniedCloseBtnText;
            return this;
        }

        public Builder deniedSettingsBtnText(String deniedSettingsBtnText) {
            this.deniedSettingsBtnText = deniedSettingsBtnText;
            return this;
        }

        public Builder rationalConfirmBtnText(String rationalConfirmBtnText) {
            this.rationalConfirmBtnText = rationalConfirmBtnText;
            return this;
        }

        public EasyPermission build() {
            return new EasyPermission(this);
        }
    }

}
















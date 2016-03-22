package com.taobao.library;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class PermissionController extends AppCompatActivity {

    public static final String EXTRA_DENIED_MESSAGE = "extra_denied_message";
    public static final String EXTRA_SHOW_SETTINGS_BTN = "extra_show_settings_btn";
    public static final String EXTRA_DENIED_CLOSE_BTN = "extra_denied_close_btn";
    public static final String EXTRA_DENIED_SETTINGS_BTN = "extra_denied_settings_btn";
    public static final String EXTRA_RATIONAL_CONFIRM_BTN = "extra_rational_confirm_btn";
    public static final String EXTRA_PERMISSIONS = "extra_permissions";
    public static final String EXTRA_RATIONAL_MESSAGE = "extra_rational_message";

    public static final String EXTRA_SHOWN_RATIONAL = "extra_shown_rational";


    private String rationalMessage;
    private String deniedMessage;
    private boolean showSettingBtn;
    private String deniedCloseBtnText;
    private String deniedSettingsBtnText;
    private String rationalConfirmBtnText;
    private String[] permissions;


    public static final int REQ_CODE_GET_CALLBACK = 100;

    public static final String REQ_RESULT = "req_result";
    public static final String RESULT_DATA = "result_data";


    private boolean hasShownRational = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resolveParams(savedInstanceState);
        permissionCheck(false);
    }

    private void permissionCheck(boolean flag) {

        List<String> permissionList = new ArrayList<>(4);
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                permissionList.add(permission);
            }
        }

        if (permissionList.isEmpty()) {
           performResult(true,permissions);
            return;
        }
        if(flag){
            performResult(false,permissionList.toArray(new String[permissionList.size()]));
            return;
        }

        boolean shouldShowRational = false;
        for (String permission : permissionList) {
            shouldShowRational = shouldShowRational || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }

        if (shouldShowRational && !TextUtils.isEmpty(rationalMessage)) {
            //解释
            showRationalDialog(permissionList);
        } else {
            requestPermission(permissionList);
        }
    }

    private void performResult(boolean success,String[] permissionArray){
        Intent intent = new Intent();
        intent.putExtra(REQ_RESULT, success);
        intent.putExtra(RESULT_DATA, permissionArray);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showRationalDialog(final List<String> permissionList) {
        if(hasShownRational){
           return;
        }
        hasShownRational = true;

        new AlertDialog.Builder(this)
                .setMessage(rationalMessage)
                .setCancelable(false)
                .setNegativeButton(rationalConfirmBtnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermission(permissionList);
                    }
                })
                .show();
    }

    private void requestPermission(List<String> permissionList) {
        ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 100);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        List<String> grantedPermissions = new ArrayList<>();
        List<String> deniedPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                grantedPermissions.add(permissions[i]);
            }else{
                deniedPermissions.add(permissions[i]);
            }
        }

        if(!grantedPermissions.isEmpty() && deniedPermissions.isEmpty()){//全部通过才回调
            performResult(true,grantedPermissions.toArray(new String[grantedPermissions.size()]));
        }

        if(!deniedPermissions.isEmpty()){
            if (TextUtils.isEmpty(deniedMessage)) {
                performResult(false,deniedPermissions.toArray(new String[deniedPermissions.size()]));
                return;
            }
            showDeniedDialog(deniedPermissions);
        }

    }

    private void showDeniedDialog(final List<String> deniedPermissions) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(deniedMessage)
                .setCancelable(false)
                .setNegativeButton(deniedCloseBtnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        performResult(false,deniedPermissions.toArray(new String[deniedPermissions.size()]));
                    }
                });

        if (showSettingBtn) {
            builder.setPositiveButton(deniedSettingsBtnText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + PermissionController.this.getPackageName()));
                        startActivityForResult(intent, REQ_CODE_REQUEST_SETTING);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        startActivityForResult(intent, REQ_CODE_REQUEST_SETTING);
                    }

                }
            });

        }


        builder.show();
    }

    public static final int REQ_CODE_REQUEST_SETTING = 20;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQ_CODE_REQUEST_SETTING){
            permissionCheck(true);
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void resolveParams(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            rationalMessage = savedInstanceState.getString(EXTRA_RATIONAL_MESSAGE);
            deniedMessage = savedInstanceState.getString(EXTRA_DENIED_MESSAGE);
            showSettingBtn = savedInstanceState.getBoolean(EXTRA_SHOW_SETTINGS_BTN);
            deniedCloseBtnText = savedInstanceState.getString(EXTRA_DENIED_CLOSE_BTN);
            deniedSettingsBtnText = savedInstanceState.getString(EXTRA_DENIED_SETTINGS_BTN);
            rationalConfirmBtnText = savedInstanceState.getString(EXTRA_RATIONAL_CONFIRM_BTN);
            permissions = savedInstanceState.getStringArray(EXTRA_PERMISSIONS);
            hasShownRational = savedInstanceState.getBoolean(EXTRA_SHOWN_RATIONAL);
        } else {
            Intent from = getIntent();
            if(from == null){
                throw new RuntimeException("do not start this activity manually...");
            }

            rationalMessage = from.getStringExtra(EXTRA_RATIONAL_MESSAGE);
            deniedMessage = from.getStringExtra(EXTRA_DENIED_MESSAGE);
            showSettingBtn = from.getBooleanExtra(EXTRA_SHOW_SETTINGS_BTN, true);
            deniedCloseBtnText = from.getStringExtra(EXTRA_DENIED_CLOSE_BTN);
            deniedSettingsBtnText = from.getStringExtra(EXTRA_DENIED_SETTINGS_BTN);
            rationalConfirmBtnText = from.getStringExtra(EXTRA_RATIONAL_CONFIRM_BTN);
            permissions = from.getStringArrayExtra(EXTRA_PERMISSIONS);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_DENIED_MESSAGE, deniedMessage);
        outState.putBoolean(EXTRA_SHOW_SETTINGS_BTN, showSettingBtn);
        outState.putString(EXTRA_DENIED_CLOSE_BTN, deniedCloseBtnText);
        outState.putString(EXTRA_DENIED_SETTINGS_BTN, deniedSettingsBtnText);
        outState.putString(EXTRA_RATIONAL_CONFIRM_BTN, rationalConfirmBtnText);
        outState.putStringArray(EXTRA_PERMISSIONS, permissions);
        outState.putString(EXTRA_RATIONAL_MESSAGE, rationalMessage);
        outState.putBoolean(EXTRA_SHOWN_RATIONAL,hasShownRational);

        super.onSaveInstanceState(outState);
    }
}

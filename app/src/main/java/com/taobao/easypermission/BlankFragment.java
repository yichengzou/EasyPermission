package com.taobao.easypermission;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.taobao.library.EasyPermission;
import com.taobao.library.PermissionResultCallback;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class BlankFragment extends Fragment {

    public BlankFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);

        view.findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkByMe();
            }
        });

        return view;
    }



    private EasyPermission easyPermission;
    private void checkByMe() {
        if(easyPermission == null){
            easyPermission = new EasyPermission.Builder(this.getActivity(),this, new PermissionResultCallback() {
                @Override
                public void onPermissionGranted(List<String> grantedPermissions) {
                    Toast.makeText(BlankFragment.this.getActivity(), "Permission granted\n" + grantedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    writeFile();

                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                    Toast.makeText(BlankFragment.this.getActivity(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                }
            })
                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE)//申请那些权限
                    .rationalMessage("若想使用此功能，必须给我权限")//想用户解释为什么需要这些权限
                    .deniedMessage("您没有授予我权限，功能将不能正常使用。你可以去设置页面重新授予权限")//用户仍然拒绝，引导用户去设置页面打开
                    .settingBtn(true)
                    .build();

        }

        easyPermission.check();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(easyPermission != null){
            easyPermission.handleResult(requestCode,resultCode,data);
        }
    }


    private void writeFile() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return;
        }
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
            File file = new File(dir,"sample.txt");
            FileWriter writer = new FileWriter(file);
            writer.write("hello world");
            writer.flush();
            writer.close();
            Toast.makeText(this.getActivity(),"写文件成功",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}

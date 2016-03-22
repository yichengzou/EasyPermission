package com.taobao.library;

import java.util.List;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/3/21<br/>
 * Time: 上午10:34<br/>
 */
@SuppressWarnings("unused")
public interface PermissionResultCallback {
    public void onPermissionGranted(List<String> grantedPermissions);
    public void onPermissionDenied(List<String> deniedPermissions);
}

## EasyPermission

> an easy way to handle android runtime permissions 

### why?

逻辑太复杂，API太多!
checkSelfPermission(), requestPermissions(), onRequestPermissionsResult(),shouldShowRequestPermissionRationale....


以写文件举例

首先我们需要判断是否已有权限，如果没有的话，再去根据用户之前是否拒绝过此权限来判断是否要向用户解释.

```
    private void checkPermissionAndWriteFile(){
        //1. 检查我们是否有权限
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //2. 是否应该向用户解释（用户之前拒绝过此权限）
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
//                Toast.makeText(this,"你需要此权限去写文件",Toast.LENGTH_SHORT).show();
                //解释完之后再去请求权限  弹dialog，如果dialog同意就重新请求权限
                //如果是fragment，直接使用requestPermissions
                new AlertDialog.Builder(MainActivity.this).setMessage("你需要此权限去写文件").setPositiveButton("申请权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

            }else{
                //3. 请求权限
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
            }
        }else{
            writeFile();
        }
    }
```


然后需要在`onRequestPermissionsResult`中处理回调结果,如果仍然拒绝，还要通知用户或者引导用户去设置页面中重新授权.


```
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 100://request code 不超过255
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    writeFile();

                } else {
                    //弹dialog，让用户去设置页面打开权限
                    Toast.makeText(this,"写文件失败,没有权限23333",Toast.LENGTH_SHORT).show();

                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                    //省略....

                }
                return;
        }

    }

```


如果每次检查权限都要重复这些代码那实在太痛苦了!

参考:[http://inthecheesefactory.com/blog/things-you-need-to-know-about-android-m-permission-developer-edition/en](http://inthecheesefactory.com/blog/things-you-need-to-know-about-android-m-permission-developer-edition/en)

### usage

`EasyPermission`简化了这一流程.

只需3步:

- 添加依赖

Add it in your root build.gradle at the end of repositories:

```

	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```

Add the dependency

```
	dependencies {
	        compile 'com.github.Rowandjj:EasyPermission:1.0'
	}
```


- 构建`EasyPermission`实例
    
```
 private EasyPermission easyPermission;
    private void checkByMe() {
        if(easyPermission == null){
            easyPermission = new EasyPermission.Builder(this.getActivity(),this, new PermissionResultCallback() {
                @Override
                public void onPermissionGranted(List<String> grantedPermissions) {
                    writeFile();

                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                }
            })
                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE)//申请那些权限
                    .rationalMessage("若想使用此功能，必须给我权限")//想用户解释为什么需要这些权限
                    .deniedMessage("您没有授予我权限，功能将不能正常使用。你可以去设置页面重新授予权限")//用户仍然拒绝，引导用户去设置页面打开
                    .settingBtn(true)
                    .build();

        }

        easyPermission.check();//检查
    }
```
- 调用`EasyPermission#handleResult`分发结果

```
 @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(easyPermission != null){
            easyPermission.handleResult(requestCode,resultCode,data);
        }
    }
```


复杂的交互逻辑全部不用管，只需在`handleResult#onPermissionGranted`中增加原有逻辑即可。



### demo效果

![https://github.com/Rowandjj/EasyPermission/blob/master/art.gif](https://github.com/Rowandjj/EasyPermission/blob/master/art.gif)

### License

     Copyright 2016 Rowandjj
    
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.





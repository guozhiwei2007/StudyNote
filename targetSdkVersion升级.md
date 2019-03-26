targetSdkVersion升级
targetSdkVersion升级纪事
目标
将优信二手车app的targetSdkVersion由22升级到26

1.集成权限框架EasyPermissions
集成方法（两种方式）
使用gradle引用
dependencies {

    // For developers using AndroidX in their applications
    implementation 'pub.devrel:easypermissions:3.0.0'

    // For developers using the Android Support Library
    implementation 'pub.devrel:easypermissions:2.0.1'
}
将源码直接拷进工程（推荐，也是二手车c端采用的方式，附github地址）
https://github.com/googlesamples/easypermissions
2.增加动态申请权限
逐一查找每个页面需要动态权限的地方，然后添加权限判断和申请，确保没有遗漏的地方

示例
比如在详情页

VehicleDetailsActivity
中有个方法

public void realCall(){
    Uri uri =  Uri.parse("tel:10086");
    startActivity(new Intent(Intent.ACTION_CALL, uri));
}
这个方法由于是直接拨打电话，所以需要权限 Manifest.permission.CALL_PHONE
下面通过调用这个拨打电话的方法realCall()来讲解EasyPermissions的使用过程

1. 让VehicleDetailsActivity继承EasyPermissions.PermissionCallbacks接口，实现2个方法
//当用户在系统的权限弹框上选择"允许"时的回调
void onPermissionsGranted(int requestCode, @NonNull List<String> perms);
//当用户在系统的权限弹框上选择"拒绝"时的回调
void onPermissionsDenied(int requestCode, @NonNull List<String> perms);
为了方便后面使用，我们先在VehicleDetailsActivity里定义一个int型的成员变量

private final int PERMISSION_CALL = 1;
2.在调用的地方加权限判断，如果没有权限就使用EasyPermissions.requestPermissions(Activity host,String rationale,int requestCode,String… perms)方法申请系统权限
String[] params={Manifest.permission.CALL_PHONE};
if(EasyPermissions.hasPermissions(this,params)){
    realCall();
}else {
    EasyPermissions.requestPermissions(this,"拨打电话",PERMISSION_CALL,params);
}
3.为了让框架能接收权限申请的结果（允许或拒绝），需要重写Activity或Fragment的onRequestPermissionsResult(int,String[] permissions,int[])方法，并将框架的回调写进去，此处的写法比较固定
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
}
4.为了当用户在系统的权限弹框选择”允许”后能继续拨打电话，我们有两种方式去实现他：
1) 在realCall()上加注解@AfterPermissionGranted(PERMISSION_CALL)
@AfterPermissionGranted(PERMISSION_CALL)
public void realCall(){
}
这样当用户选择”允许”后，框架会自动调用被@AfterPermissionGranted标记了的方法

2)在onPermissionsGranted回调中手动调用realCall()
@Override
public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    if(requestCode == PERMISSION_CALL){
        realCall();
    }
}
5.为了在用户选择了”拒绝”或者选中了”不再提醒”后给一个友好的提示，我们可以把相应的代码写在onPermissionsDenied里
@Override
public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    if (requestCode== PERMISSION_CALL){
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            //选择了不再提醒并拒绝
        }else{
            //选择了拒绝
        }
    }
}
注意
被@AfterPermissionGranted()注解的方法一定要是无参的，并且是public

3.ReactNative动态申请权限
把RN里需要权限的地方用java来写，java调用相应api前执行判断和申请权限逻辑，然后在RN里回调此方法

4.启动时必需的权限申请
在启动页增加必需权限（如获取设备信息）申请，成功获取权限后正常进入应用，如没有获得此项权限，则给用户提示去设置里手动授予权限

5.第三方sdk升级
因为升级targetSdkVersion后可能会对工程引用的第三方库产生影响，所以要对有些第三方库进行升级。

首先列举出项目中所有引用的第三方库，查看官方文档，看新版本是否兼容了targetSdk26，来确定是否要升级这个三方库；

附上优信二手车升级的三方库供参考
https://shimo.im/sheet/XwGD9BoxEBIZZdYw/Axn13

*遇到过的问题
1.问题描述
项目最先使用的是PermissionsDispatcher框架，但这个框架有两个问题：1)这个框架是使用注解的方式申请权限，会严重增加整个工程的编译时间 2) 如果有个抽象的BaseActivity，那么此注解框架无法在这里面使用

解决方案
使用EasyPermissions框架替代

2.问题描述
EasyPermissions会在用户设置权限拒绝并不再提醒后显示一个框架内部的弹框，这个弹框无法去掉，也无法定制UI

解决方案
将原来的gradle引用方式改为直接将代码拷进工程，然后修改相应代码去掉弹框

3.问题描述
由于小米等手机对低targetSdk的应用也有权限限制，之前对小米单独做过权限判断并弹框提示，集成新的权限框架后没有删除这段代码，导致当用户拒绝权限后出现了这个弹框，点击弹框应用崩溃

解决方案
删除旧的权限判断和提示，并全局搜索类似的关键词，保证旧的权限逻辑被清理干净

4.问题描述
进入应用后，打开其他页面按home切到后台，在手机系统设置里关闭某项必需的权限（如获取设备信息），再切回到应用，系统恢复原来的Activity，重走onCreate回调，此时由于缺少必需的权限，应用崩溃

解决方案
在BaseActivity中监测到Activity的恢复时（onCreate参数savedInstanceState不为空），在其onCreate函数中直接跳转到APP启动页面（跳转启动页时需要设置Intent.FLAG_ACTIVITY_NEW_TASK 和 Intent.FLAG_ACTIVITY_CLEAR_TASK两个标记，用于创建新的TASK和清空TASK中的Activity），然后调用android.os.Process.killProcess(android.os.Process.myPid()杀掉原有进程，重新走app启动时的权限申请逻辑

5.问题描述
对于面向 Android 7.0 的应用，Android 框架执行的 StrictMode API 政策禁止在应用外部公开 file:// URI ， 如果一项包含文件 URI 的 intent 离开应用，则应用出现故障，并出现 FileUriExposedException 异常。目前优信二手车中获取照片文件时需要使用FileProvider实现应用间共享文件，实现获取照片文件

解决方案(实现FileProvider的方法)
1.在AndroidManifest中定义FileProvider 例如：

<provider
          android:name="android.support.v4.content.FileProvider"
          android:authorities="com.uxin.usedcar.fileprovider"
          android:exported="false"
          android:grantUriPermissions="true">
          <meta-data
              android:name="android.support.FILE_PROVIDER_PATHS"
              android:resource="@xml/file_paths"/>
</provider>
2.在xml中创建file_paths文件指定可分享的文件路径 例如：

<!--<files-path/>代表的根目录： Context.getFilesDir()
<external-path/>代表的根目录: Environment.getExternalStorageDirectory()
<cache-path/>代表的根目录: getCacheDir()-->
<!--上述代码中path=""，是有特殊意义的，它代码根目录，
也就是说你可以向其它的应用共享根目录及其子目录下任何一个文件了。-->
<resources>
    <paths>
        <external-path
            name="providerfile"
            path=""/>
    </paths>
</resources>
3.获取相关文件Uri 例如：

if (Build.VERSION.SDK_INT >= 24) {
    Uri uriForFile = FileProvider.getUriForFile(activity,
    activity.getPackageName() + ".fileprovider", imageFile);
    //do something
}

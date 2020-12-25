# AppUpdate

  
  此文看不到图片，详细请移步博客  [ Android 关于使用 azhon/AppUpdate 的一点想法 ](http://yuqianglianshou.com/2020/12/android_appupdate/)  

[本demo地址 https://github.com/yuqianglianshou/AppUpdate](https://github.com/yuqianglianshou/AppUpdate)  

最近使用了一个不错的版本更新库，[azhon/AppUpdate](https://github.com/azhon/AppUpdate) ，使用中关于强制更新遇到了两个偏激的需求，大概描述为  
1:检测到有新版本需要更新时弹出更新提示窗，正常情况用户看到弹窗点击更新就可以了，我要实现的效果是弹出更新自动开始下载，去掉用户点击更新的操作，实现方法是代码模拟点击事件代替。   
2:有2个应用，我们称为A应用和B应用，A运行后检测手机中安装的B是否有新版，如果有，则强制更新B，这个可以实现，但是会有一个问题，更新完B以后A中的更新弹窗是不会消失的，而此时又是强制更新，所以A应用界面卡在这里了。原因是按照正常的逻辑A应该更新A，更新后A重新打开，所以并不需要也不应该在A没有更新之前关闭弹窗，而此时我们是用A更新的B，所以缺少了一步 B更新后关闭A中更新弹窗的步骤。实现思路是广播监听B的安装或更新，回调dialog的关闭方法。


运行如下图所示：  
检测到更新后
<br/>
<br/>
![](/images/posts/android_appupdate/1.jpeg){:width="40%" height="40%"}  
<br/>
<br/>
<br/>
安装了B之后A的日志
<br/>
![](/images/posts/android_appupdate/2.png)
<br/> 
<br/> 

关键代码：
<br/> 
<br/> 
```
class MainActivity : AppCompatActivity() {

    //此url 是 ES文件浏览器 apk的下载地址，包名 为 com.estrongs.android.pop
    private val url =
        "https://imtt.dd.qq.com/16891/apk/FA48766BA12A41A1D619CB4B152889C6.apk?fsname=com.estrongs.android.pop_4.2.3.3_10089.apk&csr=1bbd"

    lateinit var receiver: AppInstallReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //需动态注册广播接受者，Android8.0静态注册失效
        receiver = AppInstallReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");

        registerReceiver(receiver, filter)


        //当安装了之后 AppInstallReceiver 收到广播，在receiver中调用关闭 强制更新窗口
        button.setOnClickListener {
            AppUpdateUtils.startUpdate(
                this,
                url,
                "ES文件浏览器.apk",
                "2.0",
                2,
                "有新版啦\n强制更新\n问世间情为何物\n直教人生死相许",
                true
            )
        }
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

}
```

<br/> 
<br/> 

```
object AppUpdateUtils {


    //记录 弹窗实例 对象，用于主动关闭
    lateinit var updateDialog: UpdateDialog

    /**
     * 检查需要更新后调用
     * 强制升级
     */
    fun startUpdate(
        mContext: Context,
        updateUrl: String,
        apkName: String,
        apkVersionName: String,
        apkVersionCode: Int,
        desMsg: String,
        isForce: Boolean
    ) {
        /*
         * 整个库允许配置的内容
         * 非必选
         */
        val configuration = UpdateConfiguration()
            //输出错误日志
            .setEnableLog(true)
            //下载完成自动跳动安装页:
            .setJumpInstallPage(true)
            //设置按钮的文字颜色
            .setDialogButtonTextColor(Color.WHITE)
            //设置是否显示通知栏进度
            .setShowNotification(!isForce)
            //设置是否提示后台下载toast
            .setShowBgdToast(!isForce)
            //设置强制更新
            .setForcedUpgrade(isForce)

        var manager = DownloadManager.getInstance(mContext)
        manager.setApkName(apkName)
            .setApkUrl(updateUrl)
            .setShowNewerToast(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setConfiguration(configuration)
            .setApkVersionCode(apkVersionCode)
            .setApkVersionName(apkVersionName)
            .setApkDescription(desMsg)
            .download()

        //记录 弹窗实例 对象，用于主动关闭
        updateDialog = manager.defaultDialog

        if (isForce) {
            //拿到更新弹窗中的 button，模拟一次手动点击
            manager.defaultDialog.findViewById<Button>(com.azhon.appupdate.R.id.btn_update)
                .performClick()
        }

    }


    /**
     * 主动关闭 更新弹窗
     */
    fun dismissUpdateDialog() {
        if (updateDialog != null && updateDialog.isShowing) {
            updateDialog.cancel()
            Log.i("lq_", "dismissDialog:  关闭 ")
        } else {
            Log.i("lq_", "dismissDialog:  null ")
        }
    }

}

```
<br/> 
<br/> 

```

public class AppInstallReceiver extends BroadcastReceiver {

    public static final String TAG = "lq_AppUnInstallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        String packageName = intent.getData().getSchemeSpecificPart();

        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {

            Toast.makeText(context, "有应用被添加", Toast.LENGTH_LONG).show();

            //com.estrongs.android.pop 为 ES文件浏览器的包名
            if (("com.estrongs.android.pop").equals(packageName)) {
                //监测到ES文件浏览器 安装了版本
                Log.i(TAG, "onReceive: 监测到ES文件浏览器 安装了版本  ");
                AppUpdateUtils.INSTANCE.dismissUpdateDialog();
            }

        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {

//            Toast.makeText(context, "有应用被替换", Toast.LENGTH_LONG).show();
//
//            if (("com.estrongs.android.pop").equals(packageName)) {
//                //监测到ES文件浏览器 安装了新的版本
//                Log.i(TAG, "onReceive: 监测到ES文件浏览器 安装了新的版本  ");
//                AppUpdateUtils.INSTANCE.dismissUpdateDialog();
//            }
        }
    }

}
```

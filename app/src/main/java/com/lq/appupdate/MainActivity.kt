package com.lq.appupdate

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


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
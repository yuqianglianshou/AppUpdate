package com.lq.appupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * 注意：需要在 AndroidManifest.xml 中注册
 * 应用程序安装卸载的广播接受者
 */

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

            Toast.makeText(context, "有应用被替换", Toast.LENGTH_LONG).show();

            if (("com.estrongs.android.pop").equals(packageName)) {
                //监测到ES文件浏览器 安装了新的版本
                Log.i(TAG, "onReceive: 监测到ES文件浏览器 安装了新的版本  ");
                AppUpdateUtils.INSTANCE.dismissUpdateDialog();
            }
        }
    }

}

package com.lq.appupdate

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.Button
import com.azhon.appupdate.config.UpdateConfiguration
import com.azhon.appupdate.dialog.UpdateDialog
import com.azhon.appupdate.manager.DownloadManager

/**
 *
 *@author : lq
 *@date   : 2020/12/21
 *@desc   : app 更新安装
 *
 */
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
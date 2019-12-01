package dev.kourosh.metamorphosis

import android.app.Activity
import android.app.DownloadManager
import android.os.Environment

data class Builder(
    val activity: Activity,
    val versionCheckerUrl: String,
    var apkName: String = "new version.apk",
    val dir: String = Environment.getExternalStorageDirectory()!!.absolutePath + "/",
    val timeOut: Long = 10000L,
    val notificationConfig: NotificationConfig = NotificationConfig(
        apkName.replace(".apk", ""),
        "downloading",
        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
    )
)

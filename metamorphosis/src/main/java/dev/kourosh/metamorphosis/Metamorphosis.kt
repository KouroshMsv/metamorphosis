package dev.kourosh.metamorphosis

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

class Metamorphosis(private val builder: Builder) {


    var downloadListener: OnDownloadListener? = null
    private var downloadingListener: OnDownloadingListener? = null

    fun setOnDownloadingListener(percent: (percent: Int) -> Unit) {
        downloadingListener = object : OnDownloadingListener {
            override fun downloading(percent: Int) {
                percent(percent)
            }
        }
    }

    fun checkVersion(checkVersionListener: OnCheckVersionListener) {
        GlobalScope.launch(Dispatchers.IO) {
            when (val res = CheckVersion(builder.versionCheckerUrl).execute()
                .get(builder.timeOut, TimeUnit.MILLISECONDS).await()) {
                is Result.Success -> {
                    launch(Dispatchers.Main) {
                        checkVersionListener.onSucceed(res.data)
                    }
                }
                is Result.Error -> {
                    launch(Dispatchers.Main) {
                        checkVersionListener.onFailed(res.message, res.code)
                    }
                }
                else -> {
                    launch(Dispatchers.Main) {
                        checkVersionListener.onFailed("Time out", null)

                    }
                }
            }
        }
    }

    fun startDownload(apkUrl: String) {
        try {
            GlobalScope.launch(Dispatchers.IO) {

                builder.run {
                    if (!apkName.contains(".apk")) {
                        apkName += ".apk"
                    }
                    val file = File(dir, apkName)
                    if (file.exists())
                        file.delete()
                    val request = DownloadManager.Request(Uri.parse(apkUrl))
                        .setTitle(notificationConfig.title)
                        .setDescription(notificationConfig.description)
                        .setNotificationVisibility(notificationConfig.notificationVisibility)
                        .setDestinationUri(Uri.fromFile(file))

                    val downloadManager =
                        activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                    val downloadId = downloadManager!!.enqueue(request)
                    var downloading = true
                    var lastPercent = -1
                    while (downloading) {
                        val q = DownloadManager.Query()
                        q.setFilterById(downloadId)
                        val cursor = downloadManager.query(q)
                        cursor.moveToFirst()
                        val bytesDownloaded =
                            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val bytesTotal =
                            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val percent = ((bytesDownloaded * 100L) / bytesTotal).toInt()
                        if (percent != lastPercent) {
                            downloadingListener?.downloading(percent)
                            lastPercent = percent
                        }
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false
                            downloadListener?.onFinished(file)
                        }
                        cursor.close()
                    }
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, e)
            downloadListener?.onFailed("fail to download", null)
        }
    }


    fun installAPK(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(
            builder.activity,
            builder.activity.applicationContext.packageName.toString() + ".metamorphosisProvider",
            file
        )
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        builder.activity.startActivity(intent)
    }


}


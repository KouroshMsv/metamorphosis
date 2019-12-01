/*
package dev.kourosh.metamorphosis

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit



class Metamorphosis2(private val url: String) {

    fun check(onCheckedListener: OnCheckedListener) {
        GlobalScope.launch {
            when (val res = CheckVersion(url).execute().get(10, TimeUnit.SECONDS).await()) {
                is Result.Success -> {
                    launch(Dispatchers.Main) {
                        onCheckedListener.onSuccess(res.data)
                    }
                }
                is Result.Error -> {
                    launch(Dispatchers.Main) {
                        onCheckedListener.onFailed(res.message, res.code)
                    }
                }
                else -> {
                    launch(Dispatchers.Main) {
                        onCheckedListener.onFailed("Time out", null)

                    }
                }
            }
        }

    }

    fun startDownload(
        context: Context,
        apkUrl: String, apkName: String,
        dir: String = context.getExternalFilesDir(null)!!.absolutePath + "/",
        onDownloadListener: OnDownloadListener
    ): Long {
        return try {
            val file = File(dir, apkName)
            if (file.exists())
                file.delete()
            file.createNewFile()
            val request = DownloadManager.Request(Uri.parse(apkUrl))
                .addRequestHeader("Content-Encoding", "gzip")
                .setTitle(apkName.replace(".apk", ""))
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(file))
            val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?

            val downloadId = downloadManager!!.enqueue(request)

            var downloading = true
            var lastPercent = -1
            while (downloading) {
                val q = DownloadManager.Query()
                q.setFilterById(downloadId)
                val cursor = downloadManager.query(q)
                cursor.moveToFirst()
                val bytes_downloaded: Int = cursor.getInt(
                    cursor
                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                )
                val bytes_total: Int =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val percent = ((bytes_downloaded * 100L) / bytes_total).toInt()
                if (percent != lastPercent){
                    lastPercent=percent
                }
                cursor.close()
            }



            downloadManager.enqueue(request)
        } catch (e: Exception) {
            onDownloadListener.onFailed("fail to download", null)
            -1
        }
    }

    fun install(context: Activity, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(
            context, context.applicationContext.packageName.toString() + ".provider", file
        )
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }


}


class APKDownloader(
    private val apkUrl: String,
    private val apkName: String,
    private val dir: String,
    private val onDownloadListener: OnDownloadListener
) : AsyncTask<Unit, Unit, Unit>() {
    private var client = OkHttpClient()
    override fun doInBackground(vararg p0: Unit?) {
        try {

            val request: Request = Request.Builder().addHeader("Accept-Encoding", "identity")
                .url(apkUrl)
                .build()
            client.newCall(request).execute().use { response ->
                Log.i(TAG, response.toString())
                when (response.code) {
                    in 200..299 -> {
                        var inputStream: InputStream? = null
                        try {
//                            onDownloadListener.inProgress(0)
                            inputStream = response.body!!.byteStream()
                            val buff = ByteArray(1024 * 4)
                            var downloaded = 0L
                            val target = response.body!!.contentLength()
                            if (target == -1L) {
                                onDownloadListener.onFailed(
                                    "content length is null;\nset contentLength to header",
                                    null
                                )
                                return
                            }
                            while (true) {
                                val read: Int = inputStream.read(buff)
                                if (read == -1) {
                                    break
                                }
                                //write buff
                                downloaded += read
//                                onDownloadListener.inProgress(((downloaded + 1) * 100 / target).toInt())
                            }
                            if (target == downloaded) {

                                try {

                                    val file = File(dir, apkName)
                                    if (file.exists())
                                        file.delete()
                                    file.createNewFile()
                                    val fos = FileOutputStream(file)
                                    fos.write(response.body!!.bytes())
                                    fos.close()
                                    onDownloadListener.onFinished(file)
                                } catch (e: Exception) {
                                    onDownloadListener.onFailed("fail to download", null)
                                }


                            } else {
                                onDownloadListener.onFailed("fail to download", null)
                            }
                        } catch (e: IOException) {
                            onDownloadListener.onFailed(
                                response.body?.string() ?: response.code.toString(), null
                            )
                        } finally {
                            if (inputStream != null) {

                                inputStream.close()
                            } else {
                                onDownloadListener.onFailed("fail to download", null)
                            }
                        }


                    }
                    else -> {
                        Result.Error(
                            response.body?.string() ?: response.code.toString(),
                            response.code
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, e)
            Result.Error(e.message ?: e.localizedMessage ?: e.cause.toString(), null)
        }

    }

}

class APKDownloader2(
    private val apkUrl: String,
    private val apkName: String,
    private val dir: String,
    private val onDownloadListener: OnDownloadListener
) : AsyncTask<Unit, Unit, Unit>() {
    private var client = OkHttpClient()
    override fun doInBackground(vararg p0: Unit?) {
        try {

            val request: Request = Request.Builder()
                .url(apkUrl)
                .build()
            client.newCall(request).execute().use { response ->
                Log.i(TAG, response.toString())
                when (response.code) {
                    in 200..299 -> {
                        try {

                            val file = File(dir, apkName)
                            if (file.exists())
                                file.delete()
//                            file.mkdirs()
                            file.createNewFile()
                            val fos = FileOutputStream(file)
                            fos.write(response.body!!.bytes())
                            fos.close()
                            Result.Success(file)
                        } catch (e: Exception) {
                            Result.Error(e.toString(), null)
                        }
                    }
                    else -> {
                        Result.Error(
                            response.body?.string() ?: response.code.toString(),
                            response.code
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, e)
            Result.Error(e.message ?: e.localizedMessage ?: e.cause.toString(), null)
        }

    }

}

interface OnCheckedListener {
    fun onSuccess(data: String)
    fun onFailed(message: String, code: Int?)

}



*/

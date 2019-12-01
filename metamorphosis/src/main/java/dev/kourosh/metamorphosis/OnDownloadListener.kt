package dev.kourosh.metamorphosis

import java.io.File

interface OnDownloadListener {
    fun onFinished(file:File)
    fun onFailed(message: String, code: Int?)
}

package dev.kourosh.metamorphosis

import android.app.DownloadManager

data class NotificationConfig (var title:String, var description:String?=null, var notificationVisibility:Int= DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

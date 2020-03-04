package dev.kourosh.metamorphosis_sample

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.kourosh.metamorphosis.Builder
import dev.kourosh.metamorphosis.Metamorphosis
import dev.kourosh.metamorphosis.OnCheckVersionListener
import dev.kourosh.metamorphosis.OnDownloadListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val m = Metamorphosis(
            Builder(
                this,
                "http://ecourier.mahex.com/app-checkver-ecdelivery.json"
            )
        )
        val json = Json(JsonConfiguration.Stable)
        m.checkVersion(object : OnCheckVersionListener {
            override fun onSucceed(data: String) {
                m.startDownload(json.parse(Data.serializer(), data).url)
            }

            override fun onFailed(message: String, code: Int?) {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        })
        m.setOnDownloadingListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(it, true)
            } else {
                progressBar.progress = it
            }
        }

        m.downloadListener = object : OnDownloadListener {
            override fun onFinished(file: File) {
                m.installAPK(file)
            }

            override fun onFailed(message: String, code: Int?) {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

            }
        }
    }
}


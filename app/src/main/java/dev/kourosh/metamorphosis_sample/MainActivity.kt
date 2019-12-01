package dev.kourosh.metamorphosis_sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.kourosh.metamorphosis.Builder
import dev.kourosh.metamorphosis.Metamorphosis
import dev.kourosh.metamorphosis.OnCheckVersionListener
import dev.kourosh.metamorphosis.OnDownloadListener
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val m = Metamorphosis(Builder(this, "http://192.168.0.35:5151/v2/app/android/ecourier/checkversion"))
        val json = Json(JsonConfiguration.Stable)
        m.checkVersion(object : OnCheckVersionListener {
            override fun onSucceed(data: String) {
                m.startDownload(json.parse(Data.serializer(), data).url)
            }

            override fun onFailed(message: String, code: Int?) {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        })

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


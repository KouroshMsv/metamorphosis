package dev.kourosh.metamorphosis

import android.os.AsyncTask
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import okhttp3.*
import java.io.IOException

internal class CheckVersion(private val url: String) : AsyncTask<Unit, Unit, Deferred<Result<String>>>() {
    private var client = OkHttpClient()
    override fun doInBackground(vararg p0: Unit?): Deferred<Result<String>> {
        val deferred = CompletableDeferred<Result<String>>()
        try {
            val request: Request = Request.Builder()
                .url(url)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    deferred.complete(
                        Result.Error(
                            e.message ?: e.localizedMessage ?: e.cause.toString(), null
                        )
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.i(TAG, response.toString())
                    when (response.code) {
                        in 200..299 -> {
                            deferred.complete(Result.Success(response.body!!.string()))
                        }
                        else -> {
                            deferred.complete(
                                Result.Error(
                                    response.body?.string() ?: response.code.toString(),
                                    response.code
                                )
                            )
                        }
                    }
                    response.body?.close()
                }
            })


        } catch (e: Exception) {
            Log.w(TAG, e)
            deferred.complete(
                Result.Error(
                    e.message ?: e.localizedMessage ?: e.cause.toString(),
                    null
                )
            )
        }
        return deferred

    }

}

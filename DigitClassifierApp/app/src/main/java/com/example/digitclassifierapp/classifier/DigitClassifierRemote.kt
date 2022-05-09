package com.example.digitclassifierapp.classifier

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

class DigitClassifierRemote {
    private val url =
        "http://192.168.0.9:8080/DigitRecognitionServlet_war_exploded/digit-recognition-servlet"
    private val TAG = DigitClassifierRemote::class.java.simpleName
    private val MEDIA_TYPE_PLAINTEXT = MediaType.parse("text/plain; charset=utf-8")

    fun recognizeDigit(digit: Bitmap, callback: Callback) {
        val stream = ByteArrayOutputStream()
        digit.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        val request: Request? = Request.Builder()
            .url(url)
            .post(RequestBody.create(MEDIA_TYPE_PLAINTEXT, byteArray))
            .build()

        val okHttpClient: OkHttpClient =
            OkHttpClient.Builder()
                .readTimeout(1, TimeUnit.HOURS)
                .writeTimeout(1, TimeUnit.HOURS)
                .connectTimeout(1, TimeUnit.HOURS)
                .build()
        okHttpClient.newCall(request).enqueue(callback)
    }
}
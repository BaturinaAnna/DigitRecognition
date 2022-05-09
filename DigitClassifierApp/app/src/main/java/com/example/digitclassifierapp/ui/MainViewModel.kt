package com.example.digitclassifierapp.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.digitclassifierapp.classifier.DigitClassifierLocal
import com.example.digitclassifierapp.classifier.DigitClassifierRemote
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainViewModel : ViewModel() {
    private val TAG = MainViewModel::class.simpleName
    val recognizedDigit: MutableLiveData<String> = MutableLiveData("Draw a digit to recognize")

    // Local Classifier
    private val digitClassifierLocal = DigitClassifierLocal()

    // ML Kit Classifier
    private lateinit var ink: Ink
    private lateinit var recognizer: DigitalInkRecognizer

    // Remote Classifier
    private lateinit var digitClassifierRemote: DigitClassifierRemote

    fun recognizeDigit(runType: ClassifierRunType, digit: Any) {
        when (runType) {
            ClassifierRunType.LOCAL -> recognizeUsingDCLocal(digit as Bitmap)
            ClassifierRunType.MLKIT -> recognizeUsingDCMLKit(digit as Ink.Builder)
            ClassifierRunType.REMOTE -> recognizeUsingDCRemote(digit as Bitmap)
        }
    }

    fun initialize(context: Context) {
        initLocalClassifier(context)
        initMLKitClassifier()
        digitClassifierRemote = DigitClassifierRemote()
    }

    fun destroy() {
        digitClassifierLocal.close()
    }

    private fun recognizeUsingDCLocal(digit: Bitmap) {
        if (digitClassifierLocal.isInitialized) {
            digitClassifierLocal.classifyAsync(digit)
                .addOnSuccessListener { resultText ->
                    recognizeSuccessListener(resultText)
                }
                .addOnFailureListener { e ->
                    recognizeFailureListener(e)
                }
        }
    }

    private fun recognizeUsingDCRemote(digit: Bitmap) {
        val resizedImage = Bitmap.createScaledBitmap(
            digit,
            digitClassifierLocal.inputImageWidth,
            digitClassifierLocal.inputImageHeight,
            true
        )
        digitClassifierRemote.recognizeDigit(resizedImage,
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    recognizeFailureListener(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body()?.let { recognizeSuccessListener(it.string()) }
                }
            })
    }

    private fun recognizeUsingDCMLKit(inkBuilder: Ink.Builder) {
        ink = inkBuilder.build()
        recognizer
            .recognize(ink)
            .addOnSuccessListener { result: RecognitionResult ->
                recognizeSuccessListener(result.candidates[0].text)
            }
            .addOnFailureListener { e: Exception ->
                recognizeFailureListener(e)
            }
    }

    private fun initLocalClassifier(context: Context) {
        digitClassifierLocal
            .initialize(context)
            .addOnFailureListener { e ->
                Log.e(TAG, "Error to setting up digit classifier.", e)
            }
    }

    private fun initMLKitClassifier() {
        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US")
        val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()

        val remoteModelManager = RemoteModelManager.getInstance()

        remoteModelManager.download(model, DownloadConditions.Builder().build())
            .addOnSuccessListener {
                Log.d(TAG, "Model downloaded")
            }
            .addOnFailureListener { e: Exception ->
                Log.e(TAG, "Error while downloading a model: $e")
            }

        recognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(model).build()
        )
    }

    private fun recognizeSuccessListener(digit: String) {
        recognizedDigit.postValue(digit)
        Log.d(TAG, "Recognized $digit")
    }

    private fun recognizeFailureListener(e: Exception) {
        recognizedDigit.postValue(e.localizedMessage)
        Log.e(TAG, "Error classifying drawing.", e)
    }
}
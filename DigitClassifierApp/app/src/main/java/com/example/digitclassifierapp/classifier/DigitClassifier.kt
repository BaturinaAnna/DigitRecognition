package com.example.digitclassifierapp.classifier

import android.content.Context

interface DigitClassifier {
    fun classify(digit: Any, context: Context)
}
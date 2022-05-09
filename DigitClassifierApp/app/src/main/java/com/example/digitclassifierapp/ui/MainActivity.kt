package com.example.digitclassifierapp.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.divyanshu.draw.widget.DrawView
import com.example.digitclassifierapp.R
import com.google.mlkit.vision.digitalink.Ink

enum class ClassifierRunType {
    LOCAL, MLKIT, REMOTE
}

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.simpleName

    private lateinit var drawView: DrawView
    private lateinit var clearButton: Button
    private lateinit var recognizeButton: Button
    private lateinit var predictedTextView: TextView
    private val viewModel: MainViewModel by viewModels()
    private lateinit var strokeBuilder: Ink.Stroke.Builder
    private lateinit var inkBuilder: Ink.Builder
    private lateinit var classifierRunType: ClassifierRunType

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawView = findViewById(R.id.draw_view)
        drawView.setStrokeWidth(70.0f)
        drawView.setColor(Color.WHITE)

        clearButton = findViewById(R.id.button_clear)
        recognizeButton = findViewById(R.id.button_recognize)
        predictedTextView = findViewById(R.id.textView)
        predictedTextView.text = "Draw a digit to recognize"

        viewModel.initialize(applicationContext)
        inkBuilder = Ink.builder()

        classifierRunType = ClassifierRunType.LOCAL
        recognizeButton.text = "Recognize Local"

        initListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.classifier_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        drawView.clearCanvas()
        predictedTextView.text = "Draw a digit to recognize"

        return when (item.itemId) {
            R.id.local -> {
                classifierRunType = ClassifierRunType.LOCAL
                recognizeButton.text = "Recognize Local"
                true
            }
            R.id.mlkit -> {
                classifierRunType = ClassifierRunType.MLKIT
                recognizeButton.text = "Recognize ML Kit"
                true
            }
            R.id.remote -> {
                classifierRunType = ClassifierRunType.REMOTE
                recognizeButton.text = "Recognize Remote"
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        clearButton.setOnClickListener {
            inkBuilder = Ink.builder()
            drawView.clearCanvas()
            predictedTextView.text = "Draw a digit to recognize"
        }

        recognizeButton.setOnClickListener {
            when(classifierRunType) {
                ClassifierRunType.LOCAL -> viewModel.recognizeDigit(ClassifierRunType.LOCAL, drawView.getBitmap())
                ClassifierRunType.MLKIT -> viewModel.recognizeDigit(ClassifierRunType.MLKIT, inkBuilder)
                ClassifierRunType.REMOTE -> viewModel.recognizeDigit(ClassifierRunType.REMOTE, drawView.getBitmap())
            }
        }

        viewModel.recognizedDigit.observe(this, { digit ->
            predictedTextView.text = digit
        })

        drawView.setOnTouchListener { _, event ->
            drawView.onTouchEvent(event)

            val action = event.actionMasked
            val x = event.x
            val y = event.y
            val t = System.currentTimeMillis()

            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    strokeBuilder = Ink.Stroke.builder()
                    strokeBuilder.addPoint(Ink.Point.create(x, y, t))
                }
                MotionEvent.ACTION_MOVE -> strokeBuilder.addPoint(Ink.Point.create(x, y, t))
                MotionEvent.ACTION_UP -> {
                    strokeBuilder.addPoint(Ink.Point.create(x, y, t))
                    inkBuilder.addStroke(strokeBuilder.build())
                }
                else -> {
                    // Action not relevant for ink construction
                }
            }
            true
        }
    }

}
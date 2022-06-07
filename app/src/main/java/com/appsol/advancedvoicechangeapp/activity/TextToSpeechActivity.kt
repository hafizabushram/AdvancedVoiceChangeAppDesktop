package com.appsol.advancedvoicechangeapp.activity

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.databinding.ActivityTextToSpeechBinding
import java.util.*

class TextToSpeechActivity : AppCompatActivity() {

    private lateinit var textToSpeechBinding: ActivityTextToSpeechBinding
    var t1: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textToSpeechBinding = DataBindingUtil.setContentView(this, R.layout.activity_text_to_speech)

        init()

    }

    private fun init() {
        textToSpeechBinding.etEnglishText.requestFocus()
        textToSpeechBinding.etEnglishText.isCursorVisible = true
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        t1 = TextToSpeech(this) { status: Int ->
            if (status != TextToSpeech.ERROR) {
                t1?.language = Locale.US
            }
        }

        textToSpeechBinding.appBarMain.also { layout ->
            layout.findViewById<ImageButton>(R.id.menuButton).also {
                it.setOnClickListener {
                    onBackPressed()
                }
                it.setImageResource(R.drawable.ic_back_black)
            }

            layout.findViewById<TextView>(R.id.toolbar_title).text =
                resources.getString(R.string.textToVoice)


        }

        clickListener()

        textChangeListener()
    }

    private fun textChangeListener() {
        textToSpeechBinding.etEnglishText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                textToSpeechBinding.ivClearAll.visibility = View.VISIBLE
            }

        })
    }

    private fun clickListener() {
        textToSpeechBinding.ivListen.setOnClickListener {
            if (textToSpeechBinding.etEnglishText.text.isNotEmpty()) {
                t1!!.speak(
                    textToSpeechBinding.etEnglishText.text.toString(),
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            } else {
                textToSpeechBinding.etEnglishText.error = "Enter Text First"
            }
        }

        textToSpeechBinding.ivClearAll.setOnClickListener {
            textToSpeechBinding.etEnglishText.setText("")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        t1?.stop()
        val imm = getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.hideSoftInputFromWindow(textToSpeechBinding.etEnglishText.windowToken, 0)
    }
}
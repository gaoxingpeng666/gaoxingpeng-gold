package com.goldmonitor.alert

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingMessages = mutableListOf<String>()

    init {
        initializeTts()
    }

    private fun initializeTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINA)
                isInitialized = result != TextToSpeech.LANG_MISSING_DATA && 
                                result != TextToSpeech.LANG_NOT_SUPPORTED

                if (isInitialized) {
                    tts?.setSpeechRate(1.0f)
                    tts?.setPitch(1.0f)
                    
                    // Speak any pending messages
                    pendingMessages.forEach { speakInternal(it) }
                    pendingMessages.clear()
                }
            }
        }
    }

    fun speak(message: String) {
        if (isInitialized) {
            speakInternal(message)
        } else {
            pendingMessages.add(message)
            // Try to reinitialize if not initialized
            if (tts == null) {
                initializeTts()
            }
        }
    }

    private fun speakInternal(message: String) {
        tts?.speak(
            message,
            TextToSpeech.QUEUE_ADD,
            null,
            "utterance_${System.currentTimeMillis()}"
        )
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    fun setOnSpeechListener(listener: SpeechListener) {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                listener.onStart()
            }

            override fun onDone(utteranceId: String?) {
                listener.onDone()
            }

            @Deprecated("Deprecated in API level 21")
            override fun onError(utteranceId: String?) {
                listener.onError()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                listener.onError()
            }
        })
    }

    interface SpeechListener {
        fun onStart()
        fun onDone()
        fun onError()
    }
}

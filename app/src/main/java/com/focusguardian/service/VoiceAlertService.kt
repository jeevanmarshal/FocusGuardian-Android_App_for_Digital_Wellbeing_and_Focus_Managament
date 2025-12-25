package com.focusguardian.service

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object VoiceAlertService {

    const val TONE_GENTLE = 0
    const val TONE_REMINDER = 1
    const val TONE_STRICT = 2

    private var tts: TextToSpeech? = null

    private fun init(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) {
                tts?.language = Locale.US
            }
        }
    }

    fun speak(context: Context, text: String, tone: Int) {
        init(context)

        when (tone) {
            TONE_GENTLE -> tts?.setSpeechRate(1.0f)
            TONE_REMINDER -> tts?.setSpeechRate(0.95f)
            TONE_STRICT -> tts?.setSpeechRate(0.85f)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}

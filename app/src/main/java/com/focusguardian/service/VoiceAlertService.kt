package com.focusguardian.service

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import com.focusguardian.util.UserPrefs

object VoiceAlertService {

    const val TONE_GENTLE = 0
    const val TONE_REMINDER = 1
    const val TONE_STRICT = 2

    private var tts: TextToSpeech? = null

    fun init(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                }
            }
        }
    }

    fun speak(context: Context, text: String, tone: Int) {
        init(context)

        val voiceType = UserPrefs.getAlertVoiceType(context) // "MALE" or "FEMALE"
        val targetGender = if (voiceType == "MALE") "male" else "female"
        
        // Attempt to find a matching voice
        var voiceFound = false
        try {
            val voices = tts?.voices
            if (voices != null) {
                // heuristic: looking for "male" or "female" in voice name
                // This is not guaranteed as Voice object properties are limited, but commonly names contain hints
                // or we can check for locale variants. 
                // However, Android 'Voice' object doesn't strictly expose gender property in API < 21 (and here we are minSdk 24+ supposedly)
                // Let's try to match by name.
                for (voice in voices) {
                    if (voice.name.lowercase(Locale.ROOT).contains(targetGender)) {
                        tts?.voice = voice
                        voiceFound = true
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback or Enhancement: Pitch
        // If we didn't find a specific voice, OR even if we did, pitch helps emphasize.
        val pitch = if (voiceType == "MALE") 0.6f else 1.0f 
        tts?.setPitch(pitch)

        when (tone) {
            TONE_GENTLE -> tts?.setSpeechRate(1.0f)
            TONE_REMINDER -> tts?.setSpeechRate(0.95f)
            TONE_STRICT -> tts?.setSpeechRate(0.85f)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}

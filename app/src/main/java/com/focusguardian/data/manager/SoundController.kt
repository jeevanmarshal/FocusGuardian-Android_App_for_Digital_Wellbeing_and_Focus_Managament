package com.focusguardian.data.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale
import com.focusguardian.util.UserPrefs

class SoundController(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Log error
            } else {
                isTtsReady = true
                updateVoicePreferences()
            }
        }
    }

    fun updateVoicePreferences() {
        if (!isTtsReady) return
        
        val voiceType = UserPrefs.getAlertVoiceType(context) // "MALE" or "FEMALE"
        val voices = tts?.voices
        if (!voices.isNullOrEmpty()) {
            val targetName = if (voiceType == "MALE") "male" else "female"
            // Try to find a voice that matches
            val selectedVoice = voices.find { 
                it.name.contains(targetName, ignoreCase = true) && !it.name.contains("network", ignoreCase = true)
            } ?: voices.find { it.name.contains(targetName, ignoreCase = true) }
            
            if (selectedVoice != null) {
                tts?.voice = selectedVoice
            } else {
                // Fallback pitch manipulation if specific voice not found
                if (voiceType == "MALE") {
                    tts?.setPitch(0.7f)
                } else {
                    tts?.setPitch(1.3f)
                }
            }
        } else {
            // Fallback pitch if voices not available
             if (voiceType == "MALE") {
                    tts?.setPitch(0.7f)
                } else {
                    tts?.setPitch(1.3f)
                }
        }
    }

    fun playAlertSound(isStrict: Boolean, durationMs: Long = 3000L) {
        try {
            val notification: Uri = if (isStrict) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) 
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            val r = RingtoneManager.getRingtone(context, notification)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                r.isLooping = false // Ensure no looping if possible
            }

            r.play()
            
            if (isStrict) {
                // Stop after duration
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (r.isPlaying) {
                        r.stop()
                    }
                }, durationMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun speak(message: String) {
        if (isTtsReady && UserPrefs.isVoiceAlertsEnabled(context)) {
            updateVoicePreferences() // Ensure latest pref is used
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "ALERT_ID")
        }
    }
    
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}

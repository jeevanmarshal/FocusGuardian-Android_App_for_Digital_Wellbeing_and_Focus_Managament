package com.focusguardian.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.focusguardian.util.UserPrefs

/**
 * Monitors phone calls to automatically pause focus monitoring
 * so users aren't interrupted during important conversations.
 */
class CallMonitorService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private val callStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when (state) {
                TelephonyManager.CALL_STATE_RINGING,
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    // Call active, pause monitoring
                    UserPrefs.setManualPause(this@CallMonitorService, true)
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    // Call ended, resume monitoring after a short delay (optional)
                    UserPrefs.setManualPause(this@CallMonitorService, false)
                }
            }
        }
    }

    override fun onDestroy() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE)
        super.onDestroy()
    }
}

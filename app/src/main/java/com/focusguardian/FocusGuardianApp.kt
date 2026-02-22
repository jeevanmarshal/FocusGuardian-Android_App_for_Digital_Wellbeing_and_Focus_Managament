package com.focusguardian

import android.app.Application
class FocusGuardianApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization code can go here
        ServiceLocator.initialize(this)
    }
}

package com.focusguardian.domain.strict

class BypassProtectionLayer {

    private val restrictedPackages = setOf(
        "com.android.settings",
        "com.google.android.packageinstaller",
        "com.android.packageinstaller"
    )

    fun isRestrictedSystemApp(packageName: String): Boolean {
        // Blocks access to Settings or Uninstaller to prevent bypass
        return restrictedPackages.contains(packageName)
    }

    fun validateUninstallAttempt() {
        // Hook for DeviceAdmin receiver implementation (future)
    }
}

package com.focusguardian.util

import android.content.Context
import android.content.pm.ApplicationInfo
import com.focusguardian.model.AppCategory

object AppCategoryResolver {

    fun resolve(context: Context, packageName: String): AppCategory {
        val pm = context.packageManager
        try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            
            // Android 26+ has category info
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                when (appInfo.category) {
                    ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAME
                    ApplicationInfo.CATEGORY_VIDEO -> return AppCategory.VIDEO
                    ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
                    ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
                    // Add others as needed
                }
            }
            
            // Fallback: Simple heuristic or generic
            if (packageName.contains("facebook") || packageName.contains("instagram") || packageName.contains("twitter")) {
                return AppCategory.SOCIAL
            }
            if (packageName.contains("youtube") || packageName.contains("tiktok")) {
                return AppCategory.VIDEO
            }
            if (packageName.contains("chrome") || packageName.contains("browser")) {
                return AppCategory.BROWSING
            }

            return AppCategory.OTHER
            
        } catch (e: Exception) {
            return AppCategory.OTHER
        }
    }
}

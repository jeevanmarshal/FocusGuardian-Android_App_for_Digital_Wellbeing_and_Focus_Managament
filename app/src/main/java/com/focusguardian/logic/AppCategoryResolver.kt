package com.focusguardian.logic

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build

enum class AppCategory {
    MEDIA, SOCIAL, WORK, RELAX, OTHER
}

object AppCategoryResolver {

    fun resolve(context: Context, packageName: String): AppCategory {
        val pm = context.packageManager
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            
            // 1. Check by Android System Category (API 26+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (appInfo.category) {
                    ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
                    ApplicationInfo.CATEGORY_GAME -> return AppCategory.RELAX
                    ApplicationInfo.CATEGORY_AUDIO,
                    ApplicationInfo.CATEGORY_VIDEO,
                    ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
                    ApplicationInfo.CATEGORY_PRODUCTIVITY,
                    ApplicationInfo.CATEGORY_MAPS,
                    ApplicationInfo.CATEGORY_NEWS -> return AppCategory.WORK
                }
            }
            // Fallback for undefined or old Android
            return AppCategory.OTHER
        } catch (e: Exception) {
            AppCategory.OTHER
        }
    }

}

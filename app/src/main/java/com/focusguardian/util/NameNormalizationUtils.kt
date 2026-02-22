package com.focusguardian.util

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import java.net.URI

/**
 * COMPONENT 1 — APP NAME & SYSTEM LABEL NORMALIZATION
 * Purpose: Ensure human-friendly names everywhere.
 */
object NameNormalizationUtils {

    // Cache for app names: PackageName -> HumanName
    private val appNameCache = LruCache<String, String>(100)
    
    // Cache for site names: URL -> SiteName
    private val siteNameCache = LruCache<String, String>(100)

    /**
     * Converts package name to user-friendly App Name.
     */
    fun getAppName(context: Context, packageName: String): String {
        // 1. Check Cache
        appNameCache.get(packageName)?.let { return it }

        // 2. Resolve via PackageManager
        val pm = context.packageManager
        val name = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // Fallback to package name if not found
        }

        // 3. Store in Cache
        appNameCache.put(packageName, name)
        return name
    }

    /**
     * Converts URL to Site Name (Domain -> Brand).
     */
    fun getSiteName(url: String): String {
        // 1. Check Cache
        siteNameCache.get(url)?.let { return it }

        // 2. Extract Domain/Host
        val resolvedName = try {
            val uri = URI(if (!url.startsWith("http")) "https://$url" else url)
            var host = uri.host ?: url
            if (host.startsWith("www.")) {
                host = host.substring(4)
            }
            // Capitalize first letter of domain parts for better look
            host.split(".").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: host
        } catch (e: Exception) {
            url
        }

        // 3. Store in Cache
        siteNameCache.put(url, resolvedName)
        return resolvedName
    }
    
    /**
     * Clears the name cache.
     */
    fun clearCache() {
        appNameCache.evictAll()
        siteNameCache.evictAll()
    }
}

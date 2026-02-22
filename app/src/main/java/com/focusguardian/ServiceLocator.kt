package com.focusguardian

import android.content.Context
import androidx.room.Room
import com.focusguardian.data.local.AppDatabase
import com.focusguardian.data.local.LocalStorageManager
import com.focusguardian.data.remote.RetrofitClient
import com.focusguardian.data.repository.AiInsightsRepository
import com.focusguardian.data.repository.DataRepository
import com.focusguardian.domain.logic.EnforcementExecutor
import com.focusguardian.domain.logic.InsightProcessor
import com.focusguardian.domain.logic.ModeManager
import com.focusguardian.domain.logic.OverlayController
import com.focusguardian.domain.logic.RuleManager

object ServiceLocator {

    lateinit var applicationContext: Context
    private var database: AppDatabase? = null

    fun initialize(context: Context) {
        if (!::applicationContext.isInitialized) {
            applicationContext = context.applicationContext
            
             if (database == null) {
                database = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "focus_guardian_db"
                )
                .fallbackToDestructiveMigration()
                .build()
            }
        }
    }

    // Storage
    private val localStorageManager: LocalStorageManager by lazy {
        LocalStorageManager(applicationContext)
    }

    val dataRepository: DataRepository by lazy {
        val db = getDatabase()
        DataRepository(
            localStorageManager = localStorageManager,
            scheduleDao = db.blockingScheduleDao(),
            sessionDao = db.sessionDao(),
            usageDao = db.usageDao()
        )
    }

    // Core Logic Managers
    val modeManager: ModeManager by lazy {
        ModeManager(applicationContext)
    }

    val overlayController: OverlayController by lazy {
        OverlayController()
    }

    val ruleManager: RuleManager by lazy {
        RuleManager(applicationContext, modeManager)
    }

    val enforcementExecutor: EnforcementExecutor by lazy {
        EnforcementExecutor(applicationContext)
    }

    // Insights & AI
    val insightProcessor: InsightProcessor by lazy {
        InsightProcessor(dataRepository)
    }

    val aiInsightsRepository: AiInsightsRepository by lazy {
        AiInsightsRepository(RetrofitClient.apiService)
    }
    
    // Work & Cleanup
    val cleanupScheduler: com.focusguardian.data.manager.CleanupScheduler by lazy {
        com.focusguardian.data.manager.CleanupScheduler(dataRepository)
    }

    fun getDatabase(): AppDatabase {
        return database ?: throw IllegalStateException("Database not initialized")
    }
}

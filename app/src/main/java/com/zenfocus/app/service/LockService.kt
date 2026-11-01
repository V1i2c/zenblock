package com.zenfocus.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.zenfocus.app.R
import com.zenfocus.app.data.repository.BlockLogRepository
import com.zenfocus.app.data.repository.ProfileRepository
import com.zenfocus.app.domain.usecase.BlockDecision
import com.zenfocus.app.domain.usecase.BlockingDecisionMaker
import com.zenfocus.app.ui.LockActivity
import com.zenfocus.app.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LockService : LifecycleService() {

    private val decisionMaker: BlockingDecisionMaker by inject()
    private val blockLogRepository: BlockLogRepository by inject()
    private val profileRepository: ProfileRepository by inject()

    private var lastForegroundPackage: String = ""
    private var lastTickTime: Long = System.currentTimeMillis()
    private var isLoopRunning: Boolean = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        startPollingLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (!isLoopRunning) {
            startPollingLoop()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun startPollingLoop() {
        isLoopRunning = true
        lastTickTime = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.Default) {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            while (isActive) {
                try {
                    val foregroundPackage = getForegroundPackage(usageStatsManager)
                    if (!foregroundPackage.isNullOrBlank() && foregroundPackage != packageName) {
                        val now = System.currentTimeMillis()
                        val isNewLaunch = foregroundPackage != lastForegroundPackage
                        val elapsedSeconds = ((now - lastTickTime) / 1000).coerceAtLeast(0)
                        lastTickTime = now
                        lastForegroundPackage = foregroundPackage

                        // Record usage quota (launch count or time spent)
                        profileRepository.recordAppUsage(
                            packageName = foregroundPackage,
                            isNewLaunch = isNewLaunch,
                            elapsedSeconds = if (isNewLaunch) 0 else elapsedSeconds
                        )

                        val decision = decisionMaker.evaluateApp(foregroundPackage)
                        if (decision is BlockDecision.Block) {
                            handleBlock(decision)
                        }
                    } else {
                        lastTickTime = System.currentTimeMillis()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(300) // Poll every 300ms
            }
        }
    }

    private fun getForegroundPackage(usageStatsManager: UsageStatsManager?): String? {
        if (usageStatsManager == null) return null
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000 // 10 seconds window

        val events = usageStatsManager.queryEvents(startTime, endTime)
        var foregroundPkg: String? = null
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                foregroundPkg = event.packageName
            }
        }
        if (!foregroundPkg.isNullOrBlank()) return foregroundPkg

        // Fallback: queryUsageStats for most recent active package
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            endTime - 60000,
            endTime
        )
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }

    private fun handleBlock(decision: BlockDecision.Block) {
        lifecycleScope.launch(Dispatchers.Main) {
            // Log block attempt
            blockLogRepository.logBlockSession(
                profileId = decision.profileId,
                packageName = decision.target,
                blockType = decision.blockType
            )

            if (Settings.canDrawOverlays(this@LockService)) {
                // Show WindowManager Compose overlay
                val overlayIntent = Intent(this@LockService, OverlayService::class.java).apply {
                    putExtra(OverlayService.EXTRA_PROFILE_NAME, decision.profileName)
                    putExtra(OverlayService.EXTRA_BLOCKED_TARGET, decision.target)
                    putExtra(OverlayService.EXTRA_BLOCK_REASON, decision.reason)
                    putExtra(OverlayService.EXTRA_BLOCK_TYPE, decision.blockType)
                }
                startService(overlayIntent)
            } else {
                // Fallback to fullscreen activity
                val lockIntent = Intent(this@LockService, LockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    putExtra(LockActivity.EXTRA_PROFILE_NAME, decision.profileName)
                    putExtra(LockActivity.EXTRA_BLOCKED_TARGET, decision.target)
                    putExtra(LockActivity.EXTRA_BLOCK_REASON, decision.reason)
                }
                startActivity(lockIntent)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_service_title))
            .setContentText(getString(R.string.foreground_service_description))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "zenfocus_protection_channel"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, LockService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LockService::class.java)
            context.stopService(intent)
        }
    }
}

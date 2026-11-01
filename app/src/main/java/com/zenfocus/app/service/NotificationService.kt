package com.zenfocus.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.zenfocus.app.data.repository.BlockLogRepository
import com.zenfocus.app.data.repository.ProfileRepository
import com.zenfocus.app.domain.model.DayFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class NotificationService : NotificationListenerService() {

    private val profileRepository: ProfileRepository by inject()
    private val blockLogRepository: BlockLogRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        if (packageName == applicationContext.packageName) return

        serviceScope.launch {
            val activeProfiles = profileRepository.getAllActiveProfilesWithDetails()
            val now = java.util.Calendar.getInstance()
            val currentTime = System.currentTimeMillis()

            val shouldBlock = activeProfiles.any { details ->
                if (!details.profile.notificationBlockingEnabled) return@any false
                val isTimeActive = if (details.profile.isQuickBlock) {
                    currentTime < details.profile.quickBlockExpirationTime
                } else {
                    if (details.intervals.isEmpty()) {
                        true
                    } else {
                        details.intervals.any { interval ->
                            DayFlags.isCurrentlyActive(
                                interval.daysBitmask,
                                interval.startMinutes,
                                interval.endMinutes,
                                now
                            )
                        }
                    }
                }
                isTimeActive && details.applications.any { it.packageName == packageName }
            }

            if (shouldBlock) {
                // Cancel notification from status bar
                cancelNotification(sbn.key)

                // Persist notification so user can read it safely later
                val extras = sbn.notification.extras
                val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

                val packageManager = applicationContext.packageManager
                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    packageName
                }

                blockLogRepository.logBlockedNotification(
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text
                )
            }
        }
    }
}

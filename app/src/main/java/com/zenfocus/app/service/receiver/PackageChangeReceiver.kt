package com.zenfocus.app.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenfocus.app.data.db.entity.ApplicationProfileRelation
import com.zenfocus.app.data.repository.ProfileRepository
import com.zenfocus.app.domain.model.StrictModeOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PackageChangeReceiver : BroadcastReceiver(), KoinComponent {

    private val profileRepository: ProfileRepository by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == Intent.ACTION_PACKAGE_ADDED || intent.action == Intent.ACTION_PACKAGE_REPLACED) {
            val packageName = intent.data?.schemeSpecificPart ?: return

            CoroutineScope(Dispatchers.IO).launch {
                val activeProfiles = profileRepository.getAllActiveProfilesWithDetails()
                for (details in activeProfiles) {
                    val profile = details.profile
                    // Auto-block reinstalled apps if Strict Mode option is enabled
                    if (StrictModeOption.hasOption(profile.strictModeOptionsBitmask, StrictModeOption.BLOCK_REINSTALLED_APPS)) {
                        // Check if previously in application list or add to strict profile
                        val alreadyBlocked = details.applications.any { it.packageName == packageName }
                        if (!alreadyBlocked && details.applications.isNotEmpty()) {
                            val newApps = details.applications.toMutableList().apply {
                                add(
                                    ApplicationProfileRelation(
                                        profileId = profile.id,
                                        packageName = packageName,
                                        appName = packageName
                                    )
                                )
                            }
                            profileRepository.saveProfile(
                                profile = profile,
                                apps = newApps,
                                websites = details.websites,
                                subApps = details.subApps,
                                intervals = details.intervals,
                                limits = details.usageLimits
                            )
                        }
                    }
                }
            }
        }
    }
}

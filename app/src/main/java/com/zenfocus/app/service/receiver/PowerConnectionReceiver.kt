package com.zenfocus.app.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.zenfocus.app.data.repository.ProfileRepository
import com.zenfocus.app.domain.model.StrictModeAccessMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PowerConnectionReceiver : BroadcastReceiver(), KoinComponent {

    private val profileRepository: ProfileRepository by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == Intent.ACTION_POWER_CONNECTED) {
            CoroutineScope(Dispatchers.IO).launch {
                val activeProfiles = profileRepository.getAllActiveProfilesWithDetails()
                var unlockedCount = 0

                for (details in activeProfiles) {
                    val profile = details.profile
                    if (profile.isStrictModeActive &&
                        StrictModeAccessMethod.hasMethod(profile.strictModeAccessMethod, StrictModeAccessMethod.CHARGER)
                    ) {
                        profileRepository.setStrictModeState(profile.id, active = false, deactivationTime = 0L)
                        unlockedCount++
                    }
                }

                if (unlockedCount > 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            "Charger detected: Strict Mode unlocked for $unlockedCount profile(s).",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}

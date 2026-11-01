package com.zenfocus.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenfocus.app.data.db.entity.Profile
import com.zenfocus.app.data.repository.BlockLogRepository
import com.zenfocus.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val profileRepository: ProfileRepository,
    private val blockLogRepository: BlockLogRepository
) : ViewModel() {

    val allProfiles: StateFlow<List<Profile>> = profileRepository.getAllProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBlockCount: StateFlow<Int> = blockLogRepository.getTotalBlockCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleProfile(profile: Profile) {
        viewModelScope.launch {
            // Cannot toggle off if strict mode is active
            if (profile.isActive && profile.isStrictModeActive) return@launch
            profileRepository.setProfileActive(profile.id, !profile.isActive)
        }
    }

    fun startQuickBlock(minutes: Int) {
        viewModelScope.launch {
            // Distracting social apps and websites by default for Quick Block
            val quickApps = listOf(
                "com.google.android.youtube",
                "com.instagram.android",
                "com.facebook.katana",
                "com.twitter.android",
                "com.zhiliaoapp.musically" // TikTok
            )
            val quickWebs = listOf("reddit.com", "twitter.com", "instagram.com", "tiktok.com")
            profileRepository.startQuickBlock(minutes, quickApps, quickWebs)
        }
    }
}

package com.zenfocus.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenfocus.app.data.db.entity.Profile
import com.zenfocus.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfilesViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val profiles: StateFlow<List<Profile>> = profileRepository.getAllProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleProfile(profile: Profile) {
        viewModelScope.launch {
            if (profile.isActive && profile.isStrictModeActive) return@launch
            profileRepository.setProfileActive(profile.id, !profile.isActive)
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            if (profile.isStrictModeActive) return@launch
            profileRepository.deleteProfile(profile.id)
        }
    }
}

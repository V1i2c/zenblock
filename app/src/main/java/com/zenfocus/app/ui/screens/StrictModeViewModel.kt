package com.zenfocus.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenfocus.app.data.db.entity.Profile
import com.zenfocus.app.data.repository.ProfileRepository
import com.zenfocus.app.domain.model.StrictModeAccessMethod
import com.zenfocus.app.domain.model.StrictModeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StrictModeConfigState(
    val selectedOptionsBitmask: Int = StrictModeOption.ALL_ENABLED,
    val selectedDeactivationMethod: Int = StrictModeAccessMethod.CHARGER.mask or StrictModeAccessMethod.TIMER.mask,
    val isDeviceAdminActive: Boolean = false
)

class StrictModeViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val activeProfiles: StateFlow<List<Profile>> = profileRepository.getActiveProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _configState = MutableStateFlow(StrictModeConfigState())
    val configState: StateFlow<StrictModeConfigState> = _configState.asStateFlow()

    fun toggleOption(option: StrictModeOption) {
        _configState.update { current ->
            val updated = if (StrictModeOption.hasOption(current.selectedOptionsBitmask, option)) {
                current.selectedOptionsBitmask and option.mask.inv()
            } else {
                current.selectedOptionsBitmask or option.mask
            }
            current.copy(selectedOptionsBitmask = updated)
        }
    }

    fun toggleMethod(method: StrictModeAccessMethod) {
        _configState.update { current ->
            val updated = if (StrictModeAccessMethod.hasMethod(current.selectedDeactivationMethod, method)) {
                current.selectedDeactivationMethod and method.mask.inv()
            } else {
                current.selectedDeactivationMethod or method.mask
            }
            current.copy(selectedDeactivationMethod = updated)
        }
    }

    fun applyStrictModeToProfile(profile: Profile, durationMinutes: Int = 60) {
        viewModelScope.launch {
            profileRepository.enableStrictMode(
                profileId = profile.id,
                optionsBitmask = _configState.value.selectedOptionsBitmask,
                accessMethod = _configState.value.selectedDeactivationMethod,
                durationMinutes = durationMinutes
            )
        }
    }
}

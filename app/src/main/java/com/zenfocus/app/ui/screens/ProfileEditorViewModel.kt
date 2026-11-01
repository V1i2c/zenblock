package com.zenfocus.app.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenfocus.app.data.db.entity.ApplicationProfileRelation
import com.zenfocus.app.data.db.entity.Interval
import com.zenfocus.app.data.db.entity.Profile
import com.zenfocus.app.data.db.entity.SubAppRelation
import com.zenfocus.app.data.db.entity.WebsiteProfileRelation
import com.zenfocus.app.data.repository.ProfileRepository
import com.zenfocus.app.domain.model.DayFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String
)

data class ProfileEditorState(
    val profileId: Long = 0L,
    val name: String = "",
    val colorHex: String = "#00A896",
    val startMinutes: Int = 540, // 09:00
    val endMinutes: Int = 1020,  // 17:00
    val daysBitmask: Int = DayFlags.ALL_DAYS,
    val selectedApps: Set<String> = emptySet(),
    val selectedWebsites: Set<String> = emptySet(),
    val selectedSubApps: Set<String> = emptySet(),
    val isStrictModeEnabled: Boolean = false,
    val strictModeOptionsBitmask: Int = 3,
    val strictModeAccessMethod: Int = 1,
    val notificationBlockingEnabled: Boolean = true,
    val installedApps: List<InstalledAppItem> = emptyList(),
    val isSaved: Boolean = false
)

class ProfileEditorViewModel(
    private val profileRepository: ProfileRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditorState())
    val uiState: StateFlow<ProfileEditorState> = _uiState.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val installed = pm.getInstalledApplications(0)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName.contains("chrome") || it.packageName.contains("youtube") }
                .map {
                    InstalledAppItem(
                        packageName = it.packageName,
                        appName = pm.getApplicationLabel(it).toString()
                    )
                }
                .sortedBy { it.appName.lowercase() }

            _uiState.update { it.copy(installedApps = installed) }
        }
    }

    fun loadProfile(profileId: Long) {
        if (profileId == 0L) {
            _uiState.update {
                it.copy(
                    profileId = 0L,
                    name = "New Focus Profile",
                    selectedSubApps = setOf("yt_shorts", "ig_reels")
                )
            }
            return
        }

        viewModelScope.launch {
            val details = profileRepository.getProfileWithDetails(profileId) ?: return@launch
            val interval = details.intervals.firstOrNull() ?: Interval(profileId = profileId)

            _uiState.update {
                it.copy(
                    profileId = details.profile.id,
                    name = details.profile.name,
                    colorHex = details.profile.colorHex,
                    startMinutes = interval.startMinutes,
                    endMinutes = interval.endMinutes,
                    daysBitmask = interval.daysBitmask,
                    selectedApps = details.applications.map { a -> a.packageName }.toSet(),
                    selectedWebsites = details.websites.map { w -> w.domain }.toSet(),
                    selectedSubApps = details.subApps.map { s -> s.subAppTag }.toSet(),
                    isStrictModeEnabled = details.profile.isStrictModeActive,
                    strictModeOptionsBitmask = details.profile.strictModeOptionsBitmask,
                    strictModeAccessMethod = details.profile.strictModeAccessMethod,
                    notificationBlockingEnabled = details.profile.notificationBlockingEnabled
                )
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateTimeRange(start: Int, end: Int) = _uiState.update { it.copy(startMinutes = start, endMinutes = end) }

    fun toggleDay(day: DayFlags) {
        _uiState.update { current ->
            val updatedMask = if (DayFlags.hasFlag(current.daysBitmask, day)) {
                current.daysBitmask and day.flag.inv()
            } else {
                current.daysBitmask or day.flag
            }
            current.copy(daysBitmask = updatedMask)
        }
    }

    fun toggleApp(packageName: String) {
        _uiState.update { current ->
            val updated = if (current.selectedApps.contains(packageName)) {
                current.selectedApps - packageName
            } else {
                current.selectedApps + packageName
            }
            current.copy(selectedApps = updated)
        }
    }

    fun addWebsite(domain: String) {
        val trimmed = domain.trim().lowercase()
        if (trimmed.isNotBlank()) {
            _uiState.update { it.copy(selectedWebsites = it.selectedWebsites + trimmed) }
        }
    }

    fun removeWebsite(domain: String) {
        _uiState.update { it.copy(selectedWebsites = it.selectedWebsites - domain) }
    }

    fun toggleSubApp(tag: String) {
        _uiState.update { current ->
            val updated = if (current.selectedSubApps.contains(tag)) {
                current.selectedSubApps - tag
            } else {
                current.selectedSubApps + tag
            }
            current.copy(selectedSubApps = updated)
        }
    }

    fun toggleStrictMode(enabled: Boolean) = _uiState.update { it.copy(isStrictModeEnabled = enabled) }
    fun toggleNotificationBlocking(enabled: Boolean) = _uiState.update { it.copy(notificationBlockingEnabled = enabled) }

    fun saveProfile(onComplete: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            val profile = Profile(
                id = state.profileId,
                name = state.name.ifBlank { "Focus Profile" },
                colorHex = state.colorHex,
                isActive = true,
                isStrictModeActive = state.isStrictModeEnabled,
                strictModeOptionsBitmask = state.strictModeOptionsBitmask,
                strictModeAccessMethod = state.strictModeAccessMethod,
                notificationBlockingEnabled = state.notificationBlockingEnabled
            )

            val apps = state.selectedApps.map { pkg ->
                val label = state.installedApps.firstOrNull { it.packageName == pkg }?.appName ?: pkg
                ApplicationProfileRelation(profileId = state.profileId, packageName = pkg, appName = label)
            }

            val websites = state.selectedWebsites.map { domain ->
                WebsiteProfileRelation(profileId = state.profileId, domain = domain)
            }

            val subApps = state.selectedSubApps.map { tag ->
                SubAppRelation(profileId = state.profileId, subAppTag = tag)
            }

            val intervals = listOf(
                Interval(
                    profileId = state.profileId,
                    daysBitmask = state.daysBitmask,
                    startMinutes = state.startMinutes,
                    endMinutes = state.endMinutes
                )
            )

            profileRepository.saveProfile(
                profile = profile,
                apps = apps,
                websites = websites,
                subApps = subApps,
                intervals = intervals,
                limits = emptyList()
            )

            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}

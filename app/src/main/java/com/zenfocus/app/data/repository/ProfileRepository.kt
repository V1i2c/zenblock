package com.zenfocus.app.data.repository

import com.zenfocus.app.data.db.ZenFocusDatabase
import com.zenfocus.app.data.db.entity.ApplicationProfileRelation
import com.zenfocus.app.data.db.entity.Interval
import com.zenfocus.app.data.db.entity.Profile
import com.zenfocus.app.data.db.entity.SubAppRelation
import com.zenfocus.app.data.db.entity.UsageLimit
import com.zenfocus.app.data.db.entity.WebsiteProfileRelation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class ProfileWithDetails(
    val profile: Profile,
    val applications: List<ApplicationProfileRelation>,
    val websites: List<WebsiteProfileRelation>,
    val subApps: List<SubAppRelation>,
    val intervals: List<Interval>,
    val usageLimits: List<UsageLimit>
)

class ProfileRepository(private val database: ZenFocusDatabase) {

    private val profileDao = database.profileDao()
    private val appDao = database.applicationProfileRelationDao()
    private val webDao = database.websiteProfileRelationDao()
    private val subAppDao = database.subAppRelationDao()
    private val intervalDao = database.intervalDao()
    private val limitDao = database.usageLimitDao()

    fun getAllProfilesFlow(): Flow<List<Profile>> = profileDao.getAllProfiles()
    fun getActiveProfilesFlow(): Flow<List<Profile>> = profileDao.getActiveProfilesFlow()

    suspend fun getProfileById(id: Long): Profile? = withContext(Dispatchers.IO) {
        profileDao.getProfileById(id)
    }

    suspend fun getProfileWithDetails(profileId: Long): ProfileWithDetails? = withContext(Dispatchers.IO) {
        val profile = profileDao.getProfileById(profileId) ?: return@withContext null
        val apps = appDao.getRelationsForProfileSync(profileId)
        val webs = webDao.getWebsitesForProfileSync(profileId)
        val subApps = subAppDao.getSubAppsForProfileSync(profileId)
        val intervals = intervalDao.getIntervalsForProfileSync(profileId)
        val limits = limitDao.getUsageLimitsForProfileSync(profileId)

        ProfileWithDetails(
            profile = profile,
            applications = apps,
            websites = webs,
            subApps = subApps,
            intervals = intervals,
            usageLimits = limits
        )
    }

    suspend fun getAllActiveProfilesWithDetails(): List<ProfileWithDetails> = withContext(Dispatchers.IO) {
        val activeProfiles = profileDao.getActiveProfiles()
        activeProfiles.mapNotNull { getProfileWithDetails(it.id) }
    }

    suspend fun saveProfile(
        profile: Profile,
        apps: List<ApplicationProfileRelation>,
        websites: List<WebsiteProfileRelation>,
        subApps: List<SubAppRelation>,
        intervals: List<Interval>,
        limits: List<UsageLimit>
    ): Long = withContext(Dispatchers.IO) {
        val profileId = if (profile.id == 0L) {
            profileDao.insert(profile)
        } else {
            profileDao.update(profile)
            profile.id
        }

        // Replace relations
        appDao.deleteByProfileId(profileId)
        appDao.insertAll(apps.map { it.copy(profileId = profileId) })

        webDao.deleteByProfileId(profileId)
        webDao.insertAll(websites.map { it.copy(profileId = profileId) })

        subAppDao.deleteByProfileId(profileId)
        subAppDao.insertAll(subApps.map { it.copy(profileId = profileId) })

        intervalDao.deleteByProfileId(profileId)
        intervalDao.insertAll(intervals.map { it.copy(profileId = profileId) })

        limitDao.deleteByProfileId(profileId)
        limitDao.insertAll(limits.map { it.copy(profileId = profileId) })

        profileId
    }

    suspend fun setProfileActive(profileId: Long, active: Boolean) = withContext(Dispatchers.IO) {
        profileDao.setProfileActive(profileId, active)
    }

    suspend fun setStrictModeState(profileId: Long, active: Boolean, deactivationTime: Long = 0L) = withContext(Dispatchers.IO) {
        profileDao.setStrictModeState(profileId, active, deactivationTime)
    }

    suspend fun enableStrictMode(
        profileId: Long,
        optionsBitmask: Int,
        accessMethod: Int,
        durationMinutes: Int
    ) = withContext(Dispatchers.IO) {
        val deactivationTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        profileDao.updateStrictModeConfig(
            id = profileId,
            active = true,
            deactivationTime = deactivationTime,
            options = optionsBitmask,
            method = accessMethod
        )
    }

    suspend fun deactivateAllStrictMode() = withContext(Dispatchers.IO) {
        profileDao.deactivateAllStrictMode()
    }

    suspend fun recordAppUsage(packageName: String, isNewLaunch: Boolean, elapsedSeconds: Long) = withContext(Dispatchers.IO) {
        val limits = limitDao.getActiveUsageLimitsForPackage(packageName)
        if (limits.isEmpty()) return@withContext

        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)

        for (limit in limits) {
            val limitCal = java.util.Calendar.getInstance().apply { timeInMillis = limit.lastResetTimestamp }
            val lastDay = limitCal.get(java.util.Calendar.DAY_OF_YEAR)

            if (limit.periodType == 1 && currentDay != lastDay) {
                limitDao.resetUsage(limit.id, now)
                continue
            }

            if (limit.limitType == 1 && isNewLaunch) {
                limitDao.incrementUsage(limit.id, 1L)
            } else if (limit.limitType == 0 && elapsedSeconds > 0) {
                limitDao.incrementUsage(limit.id, elapsedSeconds)
            }
        }
    }

    suspend fun deleteProfile(profileId: Long) = withContext(Dispatchers.IO) {
        profileDao.deleteById(profileId)
    }

    suspend fun startQuickBlock(durationMinutes: Int, appsToBlock: List<String>, domainsToBlock: List<String>) = withContext(Dispatchers.IO) {
        val expiration = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        val quickProfile = Profile(
            name = "Quick Focus ($durationMinutes min)",
            iconResName = "ic_timer",
            colorHex = "#FF5964",
            isActive = true,
            isQuickBlock = true,
            quickBlockExpirationTime = expiration
        )
        val profileId = profileDao.insert(quickProfile)

        val appRelations = appsToBlock.map { pkg ->
            ApplicationProfileRelation(profileId = profileId, packageName = pkg, appName = pkg)
        }
        appDao.insertAll(appRelations)

        val webRelations = domainsToBlock.map { domain ->
            WebsiteProfileRelation(profileId = profileId, domain = domain)
        }
        webDao.insertAll(webRelations)

        profileId
    }
}

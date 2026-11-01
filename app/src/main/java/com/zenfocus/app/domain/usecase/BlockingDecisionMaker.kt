package com.zenfocus.app.domain.usecase

import com.zenfocus.app.data.db.dao.AdultContentWebsiteDao
import com.zenfocus.app.data.repository.ProfileRepository
import com.zenfocus.app.data.repository.ProfileWithDetails
import com.zenfocus.app.domain.model.DayFlags
import com.zenfocus.app.domain.model.StrictModeOption

sealed class BlockDecision {
    data object Allow : BlockDecision()
    data class Block(
        val profileId: Long,
        val profileName: String,
        val reason: String,
        val target: String,
        val blockType: String // "APP", "WEBSITE", "SUB_APP", "SETTINGS"
    ) : BlockDecision()
}

class BlockingDecisionMaker(
    private val profileRepository: ProfileRepository,
    private val detectBrowserUrlUseCase: DetectBrowserUrlUseCase,
    private val adultContentWebsiteDao: AdultContentWebsiteDao
) {

    private val exemptPackages = setOf(
        "com.zenfocus.app",
        "com.google.android.packageinstaller",
        "com.android.packageinstaller"
    )

    suspend fun evaluateApp(packageName: String): BlockDecision {
        if (exemptPackages.contains(packageName)) return BlockDecision.Allow

        val activeProfiles = profileRepository.getAllActiveProfilesWithDetails()
        val now = java.util.Calendar.getInstance()
        val currentTime = System.currentTimeMillis()

        for (item in activeProfiles) {
            val profile = item.profile

            // Check if profile is temporally active
            val isTimeActive = if (profile.isQuickBlock) {
                currentTime < profile.quickBlockExpirationTime
            } else {
                if (item.intervals.isEmpty()) {
                    true // If no intervals specified, active 24/7 while enabled
                } else {
                    item.intervals.any { interval ->
                        DayFlags.isCurrentlyActive(
                            interval.daysBitmask,
                            interval.startMinutes,
                            interval.endMinutes,
                            now
                        )
                    }
                }
            }

            if (!isTimeActive) continue

            // Strict Mode: Block Android Settings
            if (StrictModeOption.hasOption(profile.strictModeOptionsBitmask, StrictModeOption.BLOCK_SETTINGS) &&
                (packageName == "com.android.settings" || packageName.contains("settings", ignoreCase = true))
            ) {
                return BlockDecision.Block(
                    profileId = profile.id,
                    profileName = profile.name,
                    reason = "Android Settings is blocked while Strict Mode is active.",
                    target = packageName,
                    blockType = "SETTINGS"
                )
            }

            // Check blocked applications
            val appRelation = item.applications.firstOrNull { it.packageName == packageName }
            if (appRelation != null) {
                if (appRelation.blockingType == 0) { // Full Block
                    return BlockDecision.Block(
                        profileId = profile.id,
                        profileName = profile.name,
                        reason = "${appRelation.appName} is blocked by profile ${profile.name}.",
                        target = packageName,
                        blockType = "APP"
                    )
                } else {
                    // Usage limit check
                    val limit = item.usageLimits.firstOrNull { it.packageName == packageName }
                    if (limit != null && limit.currentUsage >= limit.limitValue) {
                        return BlockDecision.Block(
                            profileId = profile.id,
                            profileName = profile.name,
                            reason = "Usage limit of ${limit.limitValue} exceeded for ${appRelation.appName}.",
                            target = packageName,
                            blockType = "APP"
                        )
                    }
                }
            }
        }

        return BlockDecision.Allow
    }

    suspend fun evaluateWebsite(domain: String, isAdultBlacklistEnabled: Boolean = true): BlockDecision {
        if (domain.isBlank()) return BlockDecision.Allow

        val cleanedDomain = detectBrowserUrlUseCase.cleanAndExtractDomain(domain)
        if (cleanedDomain.isBlank()) return BlockDecision.Allow

        // Check global Adult / NSFW Content Filter
        if (isAdultBlacklistEnabled) {
            try {
                if (adultContentWebsiteDao.isDomainAdult(cleanedDomain)) {
                    return BlockDecision.Block(
                        profileId = -1L,
                        profileName = "Adult Content Filter",
                        reason = "Website $cleanedDomain is blocked by Adult / NSFW Content Protection.",
                        target = cleanedDomain,
                        blockType = "WEBSITE"
                    )
                }
            } catch (_: Exception) {}
        }

        val activeProfiles = profileRepository.getAllActiveProfilesWithDetails()
        val now = java.util.Calendar.getInstance()
        val currentTime = System.currentTimeMillis()

        for (item in activeProfiles) {
            val profile = item.profile
            val isTimeActive = if (profile.isQuickBlock) {
                currentTime < profile.quickBlockExpirationTime
            } else {
                if (item.intervals.isEmpty()) {
                    true
                } else {
                    item.intervals.any { interval ->
                        DayFlags.isCurrentlyActive(
                            interval.daysBitmask,
                            interval.startMinutes,
                            interval.endMinutes,
                            now
                        )
                    }
                }
            }

            if (!isTimeActive) continue

            // Check if domain matches any blocked website for this profile
            val matchedRelation = item.websites.firstOrNull { relation ->
                val blocked = relation.domain.lowercase()
                cleanedDomain == blocked || cleanedDomain.endsWith(".$blocked")
            }

            if (matchedRelation != null) {
                return BlockDecision.Block(
                    profileId = profile.id,
                    profileName = profile.name,
                    reason = "Website $cleanedDomain is blocked by profile ${profile.name}.",
                    target = cleanedDomain,
                    blockType = "WEBSITE"
                )
            }
        }

        return BlockDecision.Allow
    }

    suspend fun evaluateSubApp(subAppTag: String): BlockDecision {
        val activeProfiles = profileRepository.getAllActiveProfilesWithDetails()
        val now = java.util.Calendar.getInstance()
        val currentTime = System.currentTimeMillis()

        for (item in activeProfiles) {
            val profile = item.profile
            val isTimeActive = if (profile.isQuickBlock) {
                currentTime < profile.quickBlockExpirationTime
            } else {
                if (item.intervals.isEmpty()) {
                    true
                } else {
                    item.intervals.any { interval ->
                        DayFlags.isCurrentlyActive(
                            interval.daysBitmask,
                            interval.startMinutes,
                            interval.endMinutes,
                            now
                        )
                    }
                }
            }

            if (!isTimeActive) continue

            val match = item.subApps.firstOrNull { it.subAppTag == subAppTag }
            if (match != null) {
                return BlockDecision.Block(
                    profileId = profile.id,
                    profileName = profile.name,
                    reason = "Sub-feature $subAppTag is blocked by profile ${profile.name}.",
                    target = subAppTag,
                    blockType = "SUB_APP"
                )
            }
        }

        return BlockDecision.Allow
    }
}

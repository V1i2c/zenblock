package com.zenfocus.app

import android.app.Application
import com.zenfocus.app.data.db.entity.AdultContentWebsite
import com.zenfocus.app.data.db.entity.ApplicationProfileRelation
import com.zenfocus.app.data.db.entity.Interval
import com.zenfocus.app.data.db.entity.Profile
import com.zenfocus.app.data.db.entity.SubAppRelation
import com.zenfocus.app.data.db.entity.WebsiteProfileRelation
import com.zenfocus.app.data.repository.ProfileRepository
import com.zenfocus.app.di.appModule
import com.zenfocus.app.domain.model.DayFlags
import com.zenfocus.app.service.LockService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ZenFocusApp : Application() {

    private val profileRepository: ProfileRepository by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ZenFocusApp)
            modules(appModule)
        }

        // Initialize default profiles and adult domains on IO thread
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialData()
        }

        // Launch LockService
        try {
            LockService.start(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun seedInitialData() {
        // Pre-seed default adult website blocklist
        val adultWebsites = listOf(
            "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com",
            "redtube.com", "youporn.com", "onlyfans.com", "chaturbate.com"
        ).map { AdultContentWebsite(domain = it) }

        try {
            val adultDao = org.koin.java.KoinJavaComponent.get<com.zenfocus.app.data.db.dao.AdultContentWebsiteDao>(com.zenfocus.app.data.db.dao.AdultContentWebsiteDao::class.java)
            if (adultDao.getCount() == 0) {
                adultDao.insertAll(adultWebsites)
            }
        } catch (_: Exception) {}

        // Seed a default "Deep Work" profile if DB is completely empty
        val profileDao = org.koin.java.KoinJavaComponent.get<com.zenfocus.app.data.db.dao.ProfileDao>(com.zenfocus.app.data.db.dao.ProfileDao::class.java)
        val active = profileDao.getActiveProfiles()
        if (active.isEmpty()) {
            val defaultProfile = Profile(
                name = "Deep Work",
                iconResName = "ic_shield",
                colorHex = "#00A896",
                isActive = true,
                notificationBlockingEnabled = true
            )
            profileRepository.saveProfile(
                profile = defaultProfile,
                apps = listOf(
                    ApplicationProfileRelation(profileId = 0, packageName = "com.google.android.youtube", appName = "YouTube"),
                    ApplicationProfileRelation(profileId = 0, packageName = "com.instagram.android", appName = "Instagram"),
                    ApplicationProfileRelation(profileId = 0, packageName = "com.facebook.katana", appName = "Facebook"),
                    ApplicationProfileRelation(profileId = 0, packageName = "com.twitter.android", appName = "X / Twitter")
                ),
                websites = listOf(
                    WebsiteProfileRelation(profileId = 0, domain = "reddit.com"),
                    WebsiteProfileRelation(profileId = 0, domain = "twitter.com"),
                    WebsiteProfileRelation(profileId = 0, domain = "x.com"),
                    WebsiteProfileRelation(profileId = 0, domain = "tiktok.com")
                ),
                subApps = listOf(
                    SubAppRelation(profileId = 0, subAppTag = "yt_shorts"),
                    SubAppRelation(profileId = 0, subAppTag = "ig_reels")
                ),
                intervals = listOf(
                    Interval(profileId = 0, daysBitmask = DayFlags.WEEKDAYS, startMinutes = 540, endMinutes = 1020) // 9:00 AM - 5:00 PM
                ),
                limits = emptyList()
            )
        }
    }
}

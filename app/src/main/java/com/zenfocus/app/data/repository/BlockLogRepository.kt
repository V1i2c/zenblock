package com.zenfocus.app.data.repository

import com.zenfocus.app.data.db.ZenFocusDatabase
import com.zenfocus.app.data.db.entity.AdultContentWebsite
import com.zenfocus.app.data.db.entity.BlockSession
import com.zenfocus.app.data.db.entity.BlockedNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BlockLogRepository(private val database: ZenFocusDatabase) {

    private val sessionDao = database.blockSessionDao()
    private val notificationDao = database.blockedNotificationDao()
    private val adultDao = database.adultContentWebsiteDao()

    fun getAllSessionsFlow(): Flow<List<BlockSession>> = sessionDao.getAllSessions()
    fun getRecentSessionsFlow(limit: Int = 100): Flow<List<BlockSession>> = sessionDao.getRecentSessions(limit)
    fun getTotalBlockCountFlow(): Flow<Int> = sessionDao.getTotalBlockCount()
    fun getBlockCountSinceFlow(since: Long): Flow<Int> = sessionDao.getBlockCountSince(since)

    fun getBlockedNotificationsFlow(limit: Int = 50): Flow<List<BlockedNotification>> =
        notificationDao.getRecentNotifications(limit)

    suspend fun logBlockSession(profileId: Long, packageName: String, domain: String = "", blockType: String) =
        withContext(Dispatchers.IO) {
            sessionDao.insert(
                BlockSession(
                    profileId = profileId,
                    packageName = packageName,
                    domain = domain,
                    blockType = blockType
                )
            )
        }

    suspend fun logBlockedNotification(packageName: String, appName: String, title: String, text: String) =
        withContext(Dispatchers.IO) {
            notificationDao.insert(
                BlockedNotification(
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text
                )
            )
        }

    suspend fun isDomainAdult(domain: String): Boolean = withContext(Dispatchers.IO) {
        adultDao.isDomainAdult(domain)
    }

    suspend fun seedAdultDomains(domains: List<String>) = withContext(Dispatchers.IO) {
        val existingCount = adultDao.getCount()
        if (existingCount == 0) {
            val entities = domains.map { AdultContentWebsite(domain = it) }
            adultDao.insertAll(entities)
        }
    }
}

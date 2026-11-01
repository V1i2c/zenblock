package com.zenfocus.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenfocus.app.data.db.entity.UsageLimit
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageLimitDao {
    @Query("SELECT * FROM usage_limits WHERE profileId = :profileId")
    fun getUsageLimitsForProfile(profileId: Long): Flow<List<UsageLimit>>

    @Query("SELECT * FROM usage_limits WHERE profileId = :profileId")
    suspend fun getUsageLimitsForProfileSync(profileId: Long): List<UsageLimit>

    @Query("SELECT * FROM usage_limits WHERE profileId = :profileId AND packageName = :packageName LIMIT 1")
    suspend fun getUsageLimit(profileId: Long, packageName: String): UsageLimit?

    @Query("SELECT u.* FROM usage_limits u INNER JOIN profiles p ON u.profileId = p.id WHERE p.isActive = 1")
    suspend fun getAllActiveUsageLimits(): List<UsageLimit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usageLimit: UsageLimit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usageLimits: List<UsageLimit>)

    @Update
    suspend fun update(usageLimit: UsageLimit)

    @Query("DELETE FROM usage_limits WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: Long)

    @Query("SELECT u.* FROM usage_limits u INNER JOIN profiles p ON u.profileId = p.id WHERE p.isActive = 1 AND u.packageName = :packageName")
    suspend fun getActiveUsageLimitsForPackage(packageName: String): List<UsageLimit>

    @Query("UPDATE usage_limits SET currentUsage = currentUsage + :delta WHERE id = :id")
    suspend fun incrementUsage(id: Long, delta: Long)

    @Query("UPDATE usage_limits SET currentUsage = 0, lastResetTimestamp = :timestamp WHERE id = :id")
    suspend fun resetUsage(id: Long, timestamp: Long)

    @Delete
    suspend fun delete(usageLimit: UsageLimit)
}

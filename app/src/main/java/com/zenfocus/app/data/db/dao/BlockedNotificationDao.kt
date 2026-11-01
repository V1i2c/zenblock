package com.zenfocus.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenfocus.app.data.db.entity.BlockedNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNotificationDao {
    @Query("SELECT * FROM blocked_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<BlockedNotification>>

    @Query("SELECT * FROM blocked_notifications ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentNotifications(limit: Int = 50): Flow<List<BlockedNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: BlockedNotification): Long

    @Query("DELETE FROM blocked_notifications")
    suspend fun clearAll()
}

package com.zenfocus.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenfocus.app.data.db.entity.BlockSession
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockSessionDao {
    @Query("SELECT * FROM block_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<BlockSession>>

    @Query("SELECT * FROM block_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 100): Flow<List<BlockSession>>

    @Query("SELECT COUNT(*) FROM block_sessions")
    fun getTotalBlockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM block_sessions WHERE timestamp >= :sinceTimestamp")
    fun getBlockCountSince(sinceTimestamp: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: BlockSession): Long

    @Query("DELETE FROM block_sessions")
    suspend fun clearAll()
}

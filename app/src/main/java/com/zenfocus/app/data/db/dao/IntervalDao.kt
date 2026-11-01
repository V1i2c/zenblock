package com.zenfocus.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenfocus.app.data.db.entity.Interval
import kotlinx.coroutines.flow.Flow

@Dao
interface IntervalDao {
    @Query("SELECT * FROM intervals WHERE profileId = :profileId")
    fun getIntervalsForProfile(profileId: Long): Flow<List<Interval>>

    @Query("SELECT * FROM intervals WHERE profileId = :profileId")
    suspend fun getIntervalsForProfileSync(profileId: Long): List<Interval>

    @Query("SELECT i.* FROM intervals i INNER JOIN profiles p ON i.profileId = p.id WHERE p.isActive = 1")
    suspend fun getAllActiveIntervals(): List<Interval>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(interval: Interval): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(intervals: List<Interval>)

    @Query("DELETE FROM intervals WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: Long)

    @Delete
    suspend fun delete(interval: Interval)
}

package com.zenfocus.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenfocus.app.data.db.entity.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): Profile?

    @Query("SELECT * FROM profiles WHERE isActive = 1")
    suspend fun getActiveProfiles(): List<Profile>

    @Query("SELECT * FROM profiles WHERE isActive = 1")
    fun getActiveProfilesFlow(): Flow<List<Profile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: Profile): Long

    @Update
    suspend fun update(profile: Profile)

    @Delete
    suspend fun delete(profile: Profile)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE profiles SET isActive = :active WHERE id = :id")
    suspend fun setProfileActive(id: Long, active: Boolean)

    @Query("UPDATE profiles SET isStrictModeActive = :active, strictModeDeactivationTime = :deactivationTime WHERE id = :id")
    suspend fun setStrictModeState(id: Long, active: Boolean, deactivationTime: Long)

    @Query("UPDATE profiles SET isStrictModeActive = :active, strictModeDeactivationTime = :deactivationTime, strictModeOptionsBitmask = :options, strictModeAccessMethod = :method WHERE id = :id")
    suspend fun updateStrictModeConfig(id: Long, active: Boolean, deactivationTime: Long, options: Int, method: Int)

    @Query("UPDATE profiles SET isStrictModeActive = 0, strictModeDeactivationTime = 0 WHERE isStrictModeActive = 1")
    suspend fun deactivateAllStrictMode()
}

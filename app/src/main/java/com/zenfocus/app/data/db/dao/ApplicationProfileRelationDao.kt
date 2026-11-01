package com.zenfocus.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenfocus.app.data.db.entity.ApplicationProfileRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationProfileRelationDao {
    @Query("SELECT * FROM app_profile_relations WHERE profileId = :profileId")
    fun getRelationsForProfile(profileId: Long): Flow<List<ApplicationProfileRelation>>

    @Query("SELECT * FROM app_profile_relations WHERE profileId = :profileId")
    suspend fun getRelationsForProfileSync(profileId: Long): List<ApplicationProfileRelation>

    @Query("SELECT r.* FROM app_profile_relations r INNER JOIN profiles p ON r.profileId = p.id WHERE p.isActive = 1")
    suspend fun getAllActiveAppRelations(): List<ApplicationProfileRelation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(relation: ApplicationProfileRelation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(relations: List<ApplicationProfileRelation>)

    @Query("DELETE FROM app_profile_relations WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: Long)

    @Delete
    suspend fun delete(relation: ApplicationProfileRelation)
}

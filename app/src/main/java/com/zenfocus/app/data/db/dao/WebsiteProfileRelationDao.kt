package com.zenfocus.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenfocus.app.data.db.entity.WebsiteProfileRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface WebsiteProfileRelationDao {
    @Query("SELECT * FROM web_profile_relations WHERE profileId = :profileId")
    fun getWebsitesForProfile(profileId: Long): Flow<List<WebsiteProfileRelation>>

    @Query("SELECT * FROM web_profile_relations WHERE profileId = :profileId")
    suspend fun getWebsitesForProfileSync(profileId: Long): List<WebsiteProfileRelation>

    @Query("SELECT w.* FROM web_profile_relations w INNER JOIN profiles p ON w.profileId = p.id WHERE p.isActive = 1")
    suspend fun getAllActiveWebsiteRelations(): List<WebsiteProfileRelation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(relation: WebsiteProfileRelation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(relations: List<WebsiteProfileRelation>)

    @Query("DELETE FROM web_profile_relations WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: Long)

    @Delete
    suspend fun delete(relation: WebsiteProfileRelation)
}

package com.zenfocus.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenfocus.app.data.db.entity.SubAppRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface SubAppRelationDao {
    @Query("SELECT * FROM sub_app_relations WHERE profileId = :profileId")
    fun getSubAppsForProfile(profileId: Long): Flow<List<SubAppRelation>>

    @Query("SELECT * FROM sub_app_relations WHERE profileId = :profileId")
    suspend fun getSubAppsForProfileSync(profileId: Long): List<SubAppRelation>

    @Query("SELECT s.* FROM sub_app_relations s INNER JOIN profiles p ON s.profileId = p.id WHERE p.isActive = 1")
    suspend fun getAllActiveSubAppRelations(): List<SubAppRelation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(relation: SubAppRelation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(relations: List<SubAppRelation>)

    @Query("DELETE FROM sub_app_relations WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: Long)

    @Delete
    suspend fun delete(relation: SubAppRelation)
}

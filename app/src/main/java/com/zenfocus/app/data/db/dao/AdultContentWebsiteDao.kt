package com.zenfocus.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenfocus.app.data.db.entity.AdultContentWebsite

@Dao
interface AdultContentWebsiteDao {
    @Query("SELECT * FROM adult_content_websites")
    suspend fun getAll(): List<AdultContentWebsite>

    @Query("SELECT COUNT(*) > 0 FROM adult_content_websites WHERE domain = :domain OR :domain LIKE ('%.' || domain) LIMIT 1")
    suspend fun isDomainAdult(domain: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(domains: List<AdultContentWebsite>)

    @Query("SELECT COUNT(*) FROM adult_content_websites")
    suspend fun getCount(): Int
}

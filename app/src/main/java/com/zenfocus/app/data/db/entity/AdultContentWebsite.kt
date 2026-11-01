package com.zenfocus.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "adult_content_websites",
    indices = [Index(value = ["domain"], unique = true)]
)
data class AdultContentWebsite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val domain: String,
    val category: String = "adult"
)

package com.zenfocus.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_profile_relations",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "packageName"], unique = true)]
)
data class ApplicationProfileRelation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val packageName: String,
    val appName: String,
    val blockingType: Int = 0, // 0 = FULL_BLOCK, 1 = USAGE_LIMIT
    val isStrictModeEnabled: Boolean = false
)

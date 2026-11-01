package com.zenfocus.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_limits",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId", "packageName"])]
)
data class UsageLimit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val packageName: String = "",
    val limitType: Int = 0, // 0 = TIME (seconds), 1 = LAUNCH_COUNT
    val limitValue: Long = 1800, // Default 30 min (1800s) or 5 launches
    val periodType: Int = 1, // 0 = HOURLY, 1 = DAILY, 2 = WEEKLY
    val currentUsage: Long = 0,
    val lastResetTimestamp: Long = System.currentTimeMillis()
)

package com.zenfocus.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_sessions")
data class BlockSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val packageName: String,
    val domain: String = "",
    val blockType: String, // "APP", "WEBSITE", "SUB_APP", "SETTINGS"
    val timestamp: Long = System.currentTimeMillis()
)

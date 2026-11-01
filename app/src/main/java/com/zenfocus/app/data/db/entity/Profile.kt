package com.zenfocus.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconResName: String = "ic_shield",
    val colorHex: String = "#00A896",
    val isActive: Boolean = true,
    val isStrictModeActive: Boolean = false,
    val strictModeDeactivationTime: Long = 0L,
    val strictModeAccessMethod: Int = 1,
    val strictModeOptionsBitmask: Int = 3, // BlockEditing | BlockSettings
    val notificationBlockingEnabled: Boolean = false,
    val isQuickBlock: Boolean = false,
    val quickBlockExpirationTime: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

package com.zenfocus.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zenfocus.app.data.db.dao.AdultContentWebsiteDao
import com.zenfocus.app.data.db.dao.ApplicationProfileRelationDao
import com.zenfocus.app.data.db.dao.BlockSessionDao
import com.zenfocus.app.data.db.dao.BlockedNotificationDao
import com.zenfocus.app.data.db.dao.IntervalDao
import com.zenfocus.app.data.db.dao.ProfileDao
import com.zenfocus.app.data.db.dao.SubAppRelationDao
import com.zenfocus.app.data.db.dao.UsageLimitDao
import com.zenfocus.app.data.db.dao.WebsiteProfileRelationDao
import com.zenfocus.app.data.db.entity.AdultContentWebsite
import com.zenfocus.app.data.db.entity.ApplicationProfileRelation
import com.zenfocus.app.data.db.entity.BlockSession
import com.zenfocus.app.data.db.entity.BlockedNotification
import com.zenfocus.app.data.db.entity.Interval
import com.zenfocus.app.data.db.entity.Profile
import com.zenfocus.app.data.db.entity.SubAppRelation
import com.zenfocus.app.data.db.entity.UsageLimit
import com.zenfocus.app.data.db.entity.WebsiteProfileRelation

@Database(
    entities = [
        Profile::class,
        ApplicationProfileRelation::class,
        WebsiteProfileRelation::class,
        SubAppRelation::class,
        Interval::class,
        UsageLimit::class,
        BlockedNotification::class,
        BlockSession::class,
        AdultContentWebsite::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ZenFocusDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun applicationProfileRelationDao(): ApplicationProfileRelationDao
    abstract fun websiteProfileRelationDao(): WebsiteProfileRelationDao
    abstract fun subAppRelationDao(): SubAppRelationDao
    abstract fun intervalDao(): IntervalDao
    abstract fun usageLimitDao(): UsageLimitDao
    abstract fun blockedNotificationDao(): BlockedNotificationDao
    abstract fun blockSessionDao(): BlockSessionDao
    abstract fun adultContentWebsiteDao(): AdultContentWebsiteDao

    companion object {
        private const val DB_NAME = "zenfocus.db"

        @Volatile
        private var INSTANCE: ZenFocusDatabase? = null

        fun getInstance(context: Context): ZenFocusDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ZenFocusDatabase::class.java,
                    DB_NAME
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

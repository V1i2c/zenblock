package com.zenfocus.app

import com.zenfocus.app.domain.model.DayFlags
import com.zenfocus.app.domain.model.Limit
import com.zenfocus.app.domain.model.StrictModeAccessMethod
import com.zenfocus.app.domain.model.StrictModeOption
import com.zenfocus.app.domain.model.SubAppsCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DomainLogicTest {

    @Test
    fun testDayFlagsBitmaskOperations() {
        val weekdays = DayFlags.WEEKDAYS
        assertTrue(DayFlags.hasFlag(weekdays, DayFlags.MONDAY))
        assertTrue(DayFlags.hasFlag(weekdays, DayFlags.FRIDAY))
        assertFalse(DayFlags.hasFlag(weekdays, DayFlags.SATURDAY))
        assertFalse(DayFlags.hasFlag(weekdays, DayFlags.SUNDAY))

        val weekends = DayFlags.WEEKENDS
        assertFalse(DayFlags.hasFlag(weekends, DayFlags.MONDAY))
        assertTrue(DayFlags.hasFlag(weekends, DayFlags.SATURDAY))
        assertTrue(DayFlags.hasFlag(weekends, DayFlags.SUNDAY))
    }

    @Test
    fun testDaytimeScheduleInterval() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)

        // 09:00 (540 min) to 17:00 (1020 min)
        // Set time to 12:30 (750 min) -> should be active
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 30)
        assertTrue(DayFlags.isCurrentlyActive(DayFlags.WEEKDAYS, 540, 1020, cal))

        // Set time to 18:00 (1080 min) -> should be inactive
        cal.set(Calendar.HOUR_OF_DAY, 18)
        cal.set(Calendar.MINUTE, 0)
        assertFalse(DayFlags.isCurrentlyActive(DayFlags.WEEKDAYS, 540, 1020, cal))

        // Set day to Sunday -> should be inactive for weekdays
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        assertFalse(DayFlags.isCurrentlyActive(DayFlags.WEEKDAYS, 540, 1020, cal))
    }

    @Test
    fun testOvernightScheduleInterval() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        // Overnight interval: 22:00 (1320 min) to 06:00 (360 min)
        // 23:30 -> should be active
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 30)
        assertTrue(DayFlags.isCurrentlyActive(DayFlags.ALL_DAYS, 1320, 360, cal))

        // 04:15 -> should be active
        cal.set(Calendar.HOUR_OF_DAY, 4)
        cal.set(Calendar.MINUTE, 15)
        assertTrue(DayFlags.isCurrentlyActive(DayFlags.ALL_DAYS, 1320, 360, cal))

        // 14:00 -> should be inactive
        cal.set(Calendar.HOUR_OF_DAY, 14)
        cal.set(Calendar.MINUTE, 0)
        assertFalse(DayFlags.isCurrentlyActive(DayFlags.ALL_DAYS, 1320, 360, cal))
    }

    @Test
    fun testSubAppsCatalogIntegrity() {
        val catalog = SubAppsCatalog.SUB_APPS
        assertEquals(7, catalog.size)

        val ytShorts = SubAppsCatalog.findByTag("yt_shorts")
        assertNotNull(ytShorts)
        assertEquals("com.google.android.youtube", ytShorts?.hostPackage)
        assertTrue(ytShorts!!.targetViewIds.contains(":id/reel_player_page_container"))

        val igReels = SubAppsCatalog.findByTag("ig_reels")
        assertNotNull(igReels)
        assertEquals("com.instagram.android", igReels?.hostPackage)
        assertTrue(igReels!!.targetViewIds.contains(":id/comment_count"))
    }

    @Test
    fun testStrictModeBitmasks() {
        val fullProtection = StrictModeOption.ALL_ENABLED
        assertTrue(StrictModeOption.hasOption(fullProtection, StrictModeOption.BLOCK_SETTINGS))
        assertTrue(StrictModeOption.hasOption(fullProtection, StrictModeOption.BLOCK_UNINSTALLING))
        assertTrue(StrictModeOption.hasOption(fullProtection, StrictModeOption.BLOCK_EDITING))
        assertTrue(StrictModeOption.hasOption(fullProtection, StrictModeOption.BLOCK_REINSTALLED_APPS))

        val chargerMethod = StrictModeAccessMethod.CHARGER.mask
        assertTrue(StrictModeAccessMethod.hasMethod(chargerMethod, StrictModeAccessMethod.CHARGER))
        assertFalse(StrictModeAccessMethod.hasMethod(chargerMethod, StrictModeAccessMethod.PIN_CODE))
    }

    @Test
    fun testSingleDayOvernightScheduleInterval() {
        val cal = Calendar.getInstance()

        // Schedule: Friday only (DayFlags.FRIDAY.flag = 16), 22:00 (1320 min) to 06:00 (360 min)
        val fridayOnly = DayFlags.FRIDAY.flag

        // 1. Friday at 23:00 -> Should be ACTIVE (started Friday evening)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 0)
        assertTrue(DayFlags.isCurrentlyActive(fridayOnly, 1320, 360, cal))

        // 2. Saturday morning at 03:30 AM -> Should be ACTIVE (continuation of Friday night)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        cal.set(Calendar.HOUR_OF_DAY, 3)
        cal.set(Calendar.MINUTE, 30)
        assertTrue(DayFlags.isCurrentlyActive(fridayOnly, 1320, 360, cal))

        // 3. Saturday morning at 07:00 AM -> Should be INACTIVE (concluded at 06:00 AM)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        cal.set(Calendar.HOUR_OF_DAY, 7)
        cal.set(Calendar.MINUTE, 0)
        assertFalse(DayFlags.isCurrentlyActive(fridayOnly, 1320, 360, cal))

        // 4. Friday morning at 03:30 AM -> Should be INACTIVE (that was Thursday night, not Friday night)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        cal.set(Calendar.HOUR_OF_DAY, 3)
        cal.set(Calendar.MINUTE, 30)
        assertFalse(DayFlags.isCurrentlyActive(fridayOnly, 1320, 360, cal))

        // 5. Sunday morning at 03:30 AM -> Should be INACTIVE
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        cal.set(Calendar.HOUR_OF_DAY, 3)
        cal.set(Calendar.MINUTE, 30)
        assertFalse(DayFlags.isCurrentlyActive(fridayOnly, 1320, 360, cal))
    }

    @Test
    fun testFeatureLimitsPermanentlyUnlocked() {
        // All limits must be unlocked without restrictions
        Limit.entries.forEach { limit ->
            assertTrue(Limit.isFeatureUnlocked(limit))
            assertTrue(limit.limitValue > 0)
        }
        assertEquals(Int.MAX_VALUE, Limit.PROFILE_LIMIT.limitValue)
        assertEquals(Int.MAX_VALUE, Limit.APP_LIMIT.limitValue)
        assertEquals(Int.MAX_VALUE, Limit.WEBSITE_LIMIT.limitValue)
    }
}

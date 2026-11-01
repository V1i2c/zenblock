package com.zenfocus.app.domain.model

/**
 * 100% Unlocked Feature Matrix:
 * Zero paywalls, zero subscription checks. All limits set to Int.MAX_VALUE.
 */
enum class Limit(val id: Int, val limitValue: Int) {
    PROFILE_LIMIT(0, Int.MAX_VALUE),
    APP_LIMIT(1, Int.MAX_VALUE),
    WEBSITE_LIMIT(2, Int.MAX_VALUE),
    APP_WEB_LIMIT_QUICK_BLOCK(3, Int.MAX_VALUE),
    TIMES_LIMIT(4, Int.MAX_VALUE),
    NOTIFICATION_LIMIT(5, Int.MAX_VALUE),
    USAGE_LIMIT(6, Int.MAX_VALUE),
    LAUNCH_COUNT(7, Int.MAX_VALUE),
    LOCK_TIME_LIMIT(8, Int.MAX_VALUE),
    STRICT_MODE_LIMIT(9, Int.MAX_VALUE),
    STATISTICS_LIMIT(10, Int.MAX_VALUE),
    ADD_NEW_APPS(11, 1),
    ALLOWLIST(12, 1),
    BLOCK_UNSUPPORTED_BROWSERS(13, 1),
    ADULT_CONTENT_BLOCKING(14, 1),
    IN_APP_PURCHASES_BLOCKING(15, 1),
    DUPLICATE_SCHEDULES(16, 1),
    TIME_SCHEDULE_DAY_LIMIT(17, Int.MAX_VALUE),
    SUB_APPS_LIMIT(18, 1);

    companion object {
        fun isFeatureUnlocked(limit: Limit): Boolean = true
        fun getLimit(limit: Limit): Int = limit.limitValue
    }
}

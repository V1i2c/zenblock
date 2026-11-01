package com.zenfocus.app.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.zenfocus.app.service.LockService

class TimeChangeReceiver : BroadcastReceiver() {

    private var lastRecordedBootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        if (action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED
        ) {
            // Recalculate boot elapsed time to detect clock tampering
            val currentCalculatedBootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
            val drift = kotlin.math.abs(currentCalculatedBootTime - lastRecordedBootTime)

            // If system clock was intentionally modified by more than 60 seconds
            if (drift > 60_000) {
                lastRecordedBootTime = currentCalculatedBootTime
            }

            // Ensure LockService is running and evaluates updated intervals
            LockService.start(context)
        }
    }
}

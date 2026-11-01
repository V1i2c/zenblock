package com.zenfocus.app.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenfocus.app.service.LockService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            LockService.start(context)
        }
    }
}

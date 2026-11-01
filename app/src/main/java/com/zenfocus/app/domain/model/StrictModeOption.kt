package com.zenfocus.app.domain.model

enum class StrictModeOption(val mask: Int, val title: String, val description: String) {
    BLOCK_EDITING(1, "Block Editing", "Disables editing, pausing, or deleting active focus profiles"),
    BLOCK_SETTINGS(2, "Block Settings", "Blocks Android Settings to prevent clearing data or force stopping"),
    BLOCK_UNINSTALLING(4, "Prevent Uninstallation", "Enforces Device Admin API to lock uninstallation of ZenFocus"),
    BLOCK_RECENT_APPS(8, "Block Recent Apps", "Prevents viewing blocked app previews through system task switcher"),
    BLOCK_SPLIT_SCREEN(16, "Block Split Screen", "Disables multi-window and split screen bypasses"),
    BLOCK_REINSTALLED_APPS(32, "Auto-Block Reinstalled", "Immediately re-applies blocking if a blocked app is uninstalled and reinstalled");

    companion object {
        const val ALL_ENABLED = 1 or 2 or 4 or 8 or 16 or 32

        fun hasOption(bitmask: Int, option: StrictModeOption): Boolean {
            return (bitmask and option.mask) != 0
        }
    }
}

enum class StrictModeAccessMethod(val mask: Int, val title: String, val description: String) {
    TIMER(1, "Timer Expiration", "Profile stays locked until countdown finishes"),
    FOLLOW_SCHEDULES(2, "Follow Schedules", "Profile disengages automatically when scheduled interval ends"),
    APPROVAL(4, "Accountability Code", "Requires entering a partner emergency passcode"),
    CHARGER(8, "Charging Cable", "Disengages ONLY when device is physically plugged into a power source"),
    COOLDOWN(16, "5-Minute Cooldown", "Mandatory waiting countdown before any changes take effect"),
    PIN_CODE(32, "Passcode / PIN", "Requires entering a secure 4-digit PIN");

    companion object {
        fun hasMethod(bitmask: Int, method: StrictModeAccessMethod): Boolean {
            return (bitmask and method.mask) != 0
        }
    }
}

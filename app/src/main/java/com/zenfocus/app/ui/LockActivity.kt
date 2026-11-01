package com.zenfocus.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import com.zenfocus.app.ui.screens.OverlayLockScreen
import com.zenfocus.app.ui.theme.ZenFocusTheme

class LockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileName = intent.getStringExtra(EXTRA_PROFILE_NAME) ?: "Focus Profile"
        val blockedTarget = intent.getStringExtra(EXTRA_BLOCKED_TARGET) ?: "Application"
        val blockReason = intent.getStringExtra(EXTRA_BLOCK_REASON) ?: "This target is currently blocked."

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToHomeScreen()
            }
        })

        setContent {
            ZenFocusTheme {
                OverlayLockScreen(
                    profileName = profileName,
                    blockedTarget = blockedTarget,
                    blockReason = blockReason,
                    blockType = "APP",
                    onCloseApp = { goToHomeScreen() },
                    onEmergencyUnblock = {
                        val emergencyIntent = Intent(this@LockActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(MainActivity.EXTRA_NAVIGATE_TO, "emergency_unblock")
                        }
                        startActivity(emergencyIntent)
                        finish()
                    }
                )
            }
        }
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_PROFILE_NAME = "extra_profile_name"
        const val EXTRA_BLOCKED_TARGET = "extra_blocked_target"
        const val EXTRA_BLOCK_REASON = "extra_block_reason"
    }
}

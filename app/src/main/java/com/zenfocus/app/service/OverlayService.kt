package com.zenfocus.app.service

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.zenfocus.app.ui.MainActivity
import com.zenfocus.app.ui.screens.OverlayLockScreen
import com.zenfocus.app.ui.theme.ZenFocusTheme

class OverlayService : LifecycleService(), SavedStateRegistryOwner, ViewModelStoreOwner {

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val appViewModelStore = ViewModelStore()
    private var windowManager: WindowManager? = null
    private var overlayComposeView: ComposeView? = null

    private var profileName: String = ""
    private var blockedTarget: String = ""
    private var blockReason: String = ""
    private var blockType: String = "APP"

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore

    init {
        savedStateRegistryController.performAttach()
    }

    override fun onCreate() {
        savedStateRegistryController.performRestore(null)
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.let {
            profileName = it.getStringExtra(EXTRA_PROFILE_NAME) ?: "Focus Profile"
            blockedTarget = it.getStringExtra(EXTRA_BLOCKED_TARGET) ?: "Application"
            blockReason = it.getStringExtra(EXTRA_BLOCK_REASON) ?: "This target is currently blocked."
            blockType = it.getStringExtra(EXTRA_BLOCK_TYPE) ?: "APP"
        }

        showOverlay()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun showOverlay() {
        if (overlayComposeView != null) {
            removeOverlay()
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        overlayComposeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            setContent {
                ZenFocusTheme {
                    OverlayLockScreen(
                        profileName = profileName,
                        blockedTarget = blockedTarget,
                        blockReason = blockReason,
                        blockType = blockType,
                        onCloseApp = {
                            removeOverlay()
                            goToHomeScreen()
                            stopSelf()
                        },
                        onEmergencyUnblock = {
                            removeOverlay()
                            openEmergencyUnblockScreen()
                            stopSelf()
                        }
                    )
                }
            }
        }

        try {
            windowManager?.addView(overlayComposeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun removeOverlay() {
        overlayComposeView?.let {
            try {
                it.disposeComposition()
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayComposeView = null
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    private fun openEmergencyUnblockScreen() {
        val emergencyIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_NAVIGATE_TO, "emergency_unblock")
        }
        startActivity(emergencyIntent)
    }

    override fun onDestroy() {
        removeOverlay()
        appViewModelStore.clear()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PROFILE_NAME = "extra_profile_name"
        const val EXTRA_BLOCKED_TARGET = "extra_blocked_target"
        const val EXTRA_BLOCK_REASON = "extra_block_reason"
        const val EXTRA_BLOCK_TYPE = "extra_block_type"
    }
}

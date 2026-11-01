package com.zenfocus.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.zenfocus.app.data.repository.BlockLogRepository
import com.zenfocus.app.domain.model.SubAppsCatalog
import com.zenfocus.app.domain.usecase.BlockDecision
import com.zenfocus.app.domain.usecase.BlockingDecisionMaker
import com.zenfocus.app.domain.usecase.DetectBrowserUrlUseCase
import com.zenfocus.app.ui.LockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

class LockAccessibilityService : AccessibilityService() {

    private val decisionMaker: BlockingDecisionMaker by inject()
    private val detectBrowserUrlUseCase: DetectBrowserUrlUseCase by inject()
    private val blockLogRepository: BlockLogRepository by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val nodeQueue = LinkedBlockingQueue<Pair<String, AccessibilityNodeInfo>>()
    private val workerExecutor = Executors.newSingleThreadExecutor()

    @Volatile
    private var isRunning = true
    private var lastSubAppBlockTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        startNodeConsumerWorker()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isRunning) return

        val packageName = event.packageName?.toString() ?: return
        val rootNode = rootInActiveWindow ?: return

        // Anti-Tamper: Android 13+ Foreground Task Manager Protection
        if (packageName == "com.android.systemui" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSystemUiTaskManager(rootNode)
            rootNode.recycle()
            return
        }

        // Anti-Tamper: Phone Settings Blocking
        if (packageName == "com.android.settings" || packageName.contains("settings", ignoreCase = true)) {
            serviceScope.launch {
                val decision = decisionMaker.evaluateApp(packageName)
                if (decision is BlockDecision.Block) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    blockLogRepository.logBlockSession(
                        profileId = decision.profileId,
                        packageName = packageName,
                        blockType = "SETTINGS"
                    )
                }
            }
            rootNode.recycle()
            return
        }

        // Decoupled Producer: Push node to BlockingQueue; worker will inspect and recycle it
        nodeQueue.offer(Pair(packageName, rootNode))
    }

    private fun startNodeConsumerWorker() {
        workerExecutor.execute {
            while (isRunning) {
                try {
                    val pair = nodeQueue.take()
                    val packageName = pair.first
                    val node = pair.second

                    inspectNode(packageName, node)
                    node.recycle()
                } catch (_: InterruptedException) {
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun inspectNode(packageName: String, rootNode: AccessibilityNodeInfo) {
        // 1. Browser Address Bar Inspection
        if (detectBrowserUrlUseCase.isSupportedBrowser(packageName)) {
            val domain = detectBrowserUrlUseCase.extractUrl(rootNode, packageName)
            if (!domain.isNullOrBlank()) {
                serviceScope.launch {
                    val decision = decisionMaker.evaluateWebsite(domain)
                    if (decision is BlockDecision.Block) {
                        handleWebsiteBlock(packageName, domain, decision)
                    }
                }
            }
        }

        // 2. Sub-App Interception (YouTube Shorts, Instagram Reels, etc.)
        val candidateSubApps = SubAppsCatalog.findByPackage(packageName)
        for (subApp in candidateSubApps) {
            val matched = subApp.targetViewIds.any { viewId ->
                val fullId = if (viewId.startsWith(":id/")) "$packageName$viewId" else viewId
                val nodes = rootNode.findAccessibilityNodeInfosByViewId(fullId)
                val exists = !nodes.isNullOrEmpty()
                nodes?.forEach { it.recycle() }
                exists
            }

            if (matched) {
                serviceScope.launch {
                    val decision = decisionMaker.evaluateSubApp(subApp.tag)
                    if (decision is BlockDecision.Block) {
                        handleSubAppBlock(packageName, subApp.tag, decision)
                    }
                }
                break
            }
        }
    }

    private fun handleWebsiteBlock(browserPackage: String, domain: String, decision: BlockDecision.Block) {
        serviceScope.launch(Dispatchers.Main) {
            // Redirect browser tab to about:blank
            try {
                val redirectIntent = Intent(Intent.ACTION_VIEW, Uri.parse("about:blank")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                    setPackage(browserPackage)
                }
                startActivity(redirectIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Log block event
            blockLogRepository.logBlockSession(
                profileId = decision.profileId,
                packageName = browserPackage,
                domain = domain,
                blockType = "WEBSITE"
            )

            // Trigger overlay
            showOverlay(decision.profileName, domain, decision.reason, "WEBSITE")
        }
    }

    private fun handleSubAppBlock(hostPackage: String, subAppTag: String, decision: BlockDecision.Block) {
        val now = System.currentTimeMillis()
        if (now - lastSubAppBlockTime < 1000L) return
        lastSubAppBlockTime = now

        serviceScope.launch(Dispatchers.Main) {
            // Injects Back action to navigate out of the addictive view
            performGlobalAction(GLOBAL_ACTION_BACK)

            blockLogRepository.logBlockSession(
                profileId = decision.profileId,
                packageName = hostPackage,
                domain = subAppTag,
                blockType = "SUB_APP"
            )
        }
    }

    private fun checkSystemUiTaskManager(rootNode: AccessibilityNodeInfo) {
        // Inspects SystemUI notification shade for Foreground Services "Stop" action on ZenFocus
        val stopButtons = rootNode.findAccessibilityNodeInfosByText("Stop")
        if (!stopButtons.isNullOrEmpty()) {
            val hasZenFocus = rootNode.findAccessibilityNodeInfosByText("ZenFocus").isNotEmpty()
            if (hasZenFocus) {
                // Instantly collapse shade to protect foreground service
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            stopButtons.forEach { it.recycle() }
        }
    }

    private fun showOverlay(profileName: String, target: String, reason: String, blockType: String) {
        if (Settings.canDrawOverlays(this)) {
            val overlayIntent = Intent(this, OverlayService::class.java).apply {
                putExtra(OverlayService.EXTRA_PROFILE_NAME, profileName)
                putExtra(OverlayService.EXTRA_BLOCKED_TARGET, target)
                putExtra(OverlayService.EXTRA_BLOCK_REASON, reason)
                putExtra(OverlayService.EXTRA_BLOCK_TYPE, blockType)
            }
            startService(overlayIntent)
        } else {
            val lockIntent = Intent(this, LockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                putExtra(LockActivity.EXTRA_PROFILE_NAME, profileName)
                putExtra(LockActivity.EXTRA_BLOCKED_TARGET, target)
                putExtra(LockActivity.EXTRA_BLOCK_REASON, reason)
            }
            startActivity(lockIntent)
        }
    }

    override fun onInterrupt() {
        isRunning = false
    }

    override fun onDestroy() {
        isRunning = false
        workerExecutor.shutdownNow()
        super.onDestroy()
    }
}

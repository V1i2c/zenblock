package com.zenfocus.app.domain.usecase

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.zenfocus.app.domain.model.BrowserConfig
import com.zenfocus.app.domain.model.DetectionMethod
import com.zenfocus.app.domain.model.ExtractionMethod
import org.json.JSONArray
import java.net.URI
import java.util.Locale
import java.util.regex.Pattern

class DetectBrowserUrlUseCase(private val context: Context) {

    private val browserConfigs = mutableListOf<BrowserConfig>()
    private val incognitoViewIds = mutableMapOf<String, String>()

    private val domainPattern = Pattern.compile(
        "^(?:https?://)?(?:www\\.)?([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})(?:/.*)?$",
        Pattern.CASE_INSENSITIVE
    )

    init {
        loadBrowserViewIds()
        loadIncognitoViewIds()
    }

    private fun loadBrowserViewIds() {
        try {
            val jsonString = context.assets.open("browser_view_ids.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val pkg = obj.getString("packageName")
                val viewId = obj.getString("viewId")
                val detectionMethod = if (obj.optString("detectionMethod") == "COMPOSE_TEST_TAG") {
                    DetectionMethod.COMPOSE_TEST_TAG
                } else {
                    DetectionMethod.VIEW_ID
                }
                val extractionMethod = if (obj.optString("extractionMethod") == "CONTENT_DESC") {
                    ExtractionMethod.CONTENT_DESC
                } else {
                    ExtractionMethod.TEXT
                }
                val clearUrl = obj.optBoolean("clearUrl", true)
                val showLocalHtml = obj.optBoolean("showLocalHtml", false)

                browserConfigs.add(
                    BrowserConfig(
                        packageName = pkg,
                        viewId = viewId,
                        detectionMethod = detectionMethod,
                        extractionMethod = extractionMethod,
                        clearUrl = clearUrl,
                        showLocalHtml = showLocalHtml
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadIncognitoViewIds() {
        try {
            val jsonString = context.assets.open("incognito_view_ids.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                incognitoViewIds[obj.getString("packageName")] = obj.getString("viewId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isSupportedBrowser(packageName: String): Boolean {
        return browserConfigs.any { it.packageName == packageName }
    }

    fun extractUrl(rootNode: AccessibilityNodeInfo, packageName: String): String? {
        val configs = browserConfigs.filter { it.packageName == packageName }
        for (config in configs) {
            val node = findMatchingNode(rootNode, config)
            if (node != null) {
                val rawText = when (config.extractionMethod) {
                    ExtractionMethod.TEXT -> node.text?.toString()
                    ExtractionMethod.CONTENT_DESC -> node.contentDescription?.toString()
                }
                node.recycle()

                if (!rawText.isNullOrBlank()) {
                    val cleaned = cleanAndExtractDomain(rawText)
                    if (cleaned.isNotBlank()) {
                        return cleaned
                    }
                }
            }
        }
        return null
    }

    fun isIncognitoMode(rootNode: AccessibilityNodeInfo, packageName: String): Boolean {
        val viewId = incognitoViewIds[packageName] ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
        val isIncognito = !nodes.isNullOrEmpty()
        nodes?.forEach { it.recycle() }
        return isIncognito
    }

    private fun findMatchingNode(rootNode: AccessibilityNodeInfo, config: BrowserConfig): AccessibilityNodeInfo? {
        return when (config.detectionMethod) {
            DetectionMethod.VIEW_ID -> {
                val fullId = if (config.viewId.startsWith(":id/")) {
                    "${config.packageName}${config.viewId}"
                } else {
                    config.viewId
                }
                val nodes = rootNode.findAccessibilityNodeInfosByViewId(fullId)
                val match = nodes?.firstOrNull()
                nodes?.filter { it != match }?.forEach { it.recycle() }
                match
            }
            DetectionMethod.COMPOSE_TEST_TAG -> {
                // Search child nodes for matching text or content description
                findNodeByTagRecursive(rootNode, config.viewId)
            }
        }
    }

    private fun findNodeByTagRecursive(node: AccessibilityNodeInfo, tag: String): AccessibilityNodeInfo? {
        val contentDesc = node.contentDescription?.toString()
        if (contentDesc != null && contentDesc.contains(tag, ignoreCase = true)) {
            return AccessibilityNodeInfo.obtain(node)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByTagRecursive(child, tag)
            if (found != null) {
                return found
            }
            child.recycle()
        }
        return null
    }

    fun cleanAndExtractDomain(rawInput: String): String {
        val trimmed = rawInput.trim().lowercase(Locale.ROOT)
        // Discard blank strings, search query sentences containing spaces, or single words without a domain extension dot
        if (trimmed.isBlank() || trimmed.contains(" ") || !trimmed.contains(".")) {
            return ""
        }

        try {
            val withScheme = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else {
                trimmed
            }
            val uri = URI(withScheme)
            val host = uri.host
            if (!host.isNullOrBlank() && host.contains(".")) {
                return host.removePrefix("www.")
            }
        } catch (_: Exception) {}

        val matcher = domainPattern.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)?.removePrefix("www.") ?: ""
        }
        val fallback = trimmed.substringBefore("/").substringBefore("?").removePrefix("www.")
        return if (fallback.contains(".") && !fallback.contains(" ")) fallback else ""
    }
}

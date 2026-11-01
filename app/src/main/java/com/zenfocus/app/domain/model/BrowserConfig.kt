package com.zenfocus.app.domain.model

enum class DetectionMethod {
    VIEW_ID,
    COMPOSE_TEST_TAG
}

enum class ExtractionMethod {
    TEXT,
    CONTENT_DESC
}

enum class ViewType {
    URL_BAR,
    INCOGNITO_BADGE
}

data class BrowserConfig(
    val packageName: String,
    val viewId: String,
    val detectionMethod: DetectionMethod = DetectionMethod.VIEW_ID,
    val extractionMethod: ExtractionMethod = ExtractionMethod.TEXT,
    val clearUrl: Boolean = true,
    val showLocalHtml: Boolean = false,
    val viewType: ViewType = ViewType.URL_BAR
)

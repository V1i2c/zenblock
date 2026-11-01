package com.zenfocus.app.domain.model

data class SubApp(
    val id: String,
    val name: String,
    val hostPackage: String,
    val tag: String,
    val targetViewIds: List<String>,
    val description: String = ""
)

object SubAppsCatalog {
    val SUB_APPS: List<SubApp> = listOf(
        SubApp(
            id = "yt_shorts",
            name = "YouTube Shorts",
            hostPackage = "com.google.android.youtube",
            tag = "yt_shorts",
            targetViewIds = listOf(":id/reel_player_page_container"),
            description = "Blocks short-form algorithmic vertical videos on YouTube"
        ),
        SubApp(
            id = "ig_reels",
            name = "Instagram Reels",
            hostPackage = "com.instagram.android",
            tag = "ig_reels",
            targetViewIds = listOf(":id/comment_count"),
            description = "Blocks continuous Reels feed inside Instagram"
        ),
        SubApp(
            id = "ig_stories",
            name = "Instagram Stories",
            hostPackage = "com.instagram.android",
            tag = "ig_stories",
            targetViewIds = listOf(":id/reel_viewer_timestamp"),
            description = "Blocks 24-hour disappearing Stories viewer"
        ),
        SubApp(
            id = "snap_stories",
            name = "Snapchat Stories",
            hostPackage = "com.snapchat.android",
            tag = "snap_stories",
            targetViewIds = listOf(":id/df_large_story"),
            description = "Blocks public story carousels on Snapchat"
        ),
        SubApp(
            id = "snap_spotlight",
            name = "Snapchat Spotlight",
            hostPackage = "com.snapchat.android",
            tag = "snap_spotlight",
            targetViewIds = listOf(":id/spotlight_attribution_title", ":id/spotlight_container"),
            description = "Blocks Spotlight infinite vertical feed"
        ),
        SubApp(
            id = "whatsapp_status",
            name = "WhatsApp Status",
            hostPackage = "com.whatsapp",
            tag = "whatsapp_status",
            targetViewIds = listOf(":id/status_playback_image", ":id/status_playback_video"),
            description = "Blocks status updates tab playback"
        ),
        SubApp(
            id = "whatsapp_channels",
            name = "WhatsApp Channels",
            hostPackage = "com.whatsapp",
            tag = "whatsapp_channels",
            targetViewIds = listOf(":id/newsletter_quick_forwarding_pill_container_key", ":id/follow_channel_button"),
            description = "Blocks broadcast channels stream"
        )
    )

    fun findByPackage(packageName: String): List<SubApp> {
        return SUB_APPS.filter { it.hostPackage == packageName }
    }

    fun findByTag(tag: String): SubApp? {
        return SUB_APPS.firstOrNull { it.tag == tag }
    }
}

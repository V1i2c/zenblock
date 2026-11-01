package com.zenfocus.app.ui.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

/**
 * Custom TextToolbar implementation that completely suppresses copy/paste menus
 * in Jetpack Compose, forcing manual character-by-character typing.
 */
class NoOpTextToolbar : TextToolbar {
    override val status: TextToolbarStatus
        get() = TextToolbarStatus.Hidden

    override fun hide() {
        // No-op: toolbar is permanently hidden
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        // No-op: do not show any contextual actions menu
    }
}

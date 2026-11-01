package com.zenfocus.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenfocus.app.data.db.entity.BlockSession
import com.zenfocus.app.data.db.entity.BlockedNotification
import com.zenfocus.app.data.repository.BlockLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    blockLogRepository: BlockLogRepository
) : ViewModel() {

    val totalBlocks: StateFlow<Int> = blockLogRepository.getTotalBlockCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentSessions: StateFlow<List<BlockSession>> = blockLogRepository.getRecentSessionsFlow(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedNotifications: StateFlow<List<BlockedNotification>> = blockLogRepository.getBlockedNotificationsFlow(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

package com.zenfocus.app.di

import com.zenfocus.app.data.db.ZenFocusDatabase
import com.zenfocus.app.data.repository.BlockLogRepository
import com.zenfocus.app.data.repository.ProfileRepository
import com.zenfocus.app.domain.usecase.BlockingDecisionMaker
import com.zenfocus.app.domain.usecase.DetectBrowserUrlUseCase
import com.zenfocus.app.ui.screens.DashboardViewModel
import com.zenfocus.app.ui.screens.ProfileEditorViewModel
import com.zenfocus.app.ui.screens.ProfilesViewModel
import com.zenfocus.app.ui.screens.StatisticsViewModel
import com.zenfocus.app.ui.screens.StrictModeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single { ZenFocusDatabase.getInstance(androidContext()) }
    single { get<ZenFocusDatabase>().profileDao() }
    single { get<ZenFocusDatabase>().applicationProfileRelationDao() }
    single { get<ZenFocusDatabase>().websiteProfileRelationDao() }
    single { get<ZenFocusDatabase>().subAppRelationDao() }
    single { get<ZenFocusDatabase>().intervalDao() }
    single { get<ZenFocusDatabase>().usageLimitDao() }
    single { get<ZenFocusDatabase>().blockedNotificationDao() }
    single { get<ZenFocusDatabase>().blockSessionDao() }
    single { get<ZenFocusDatabase>().adultContentWebsiteDao() }

    // Repositories
    single { ProfileRepository(get()) }
    single { BlockLogRepository(get()) }

    // UseCases
    single { DetectBrowserUrlUseCase(androidContext()) }
    single { BlockingDecisionMaker(get(), get(), get()) }

    // ViewModels
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { ProfilesViewModel(get()) }
    viewModel { ProfileEditorViewModel(get(), androidContext()) }
    viewModel { StrictModeViewModel(get()) }
    viewModel { StatisticsViewModel(get()) }
}

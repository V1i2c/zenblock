package com.zenfocus.app.ui

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.zenfocus.app.ui.screens.DashboardScreen
import com.zenfocus.app.ui.screens.EmergencyUnblockScreen
import com.zenfocus.app.ui.screens.PermissionWizardScreen
import com.zenfocus.app.ui.screens.ProfileEditorScreen
import com.zenfocus.app.ui.screens.ProfilesScreen
import com.zenfocus.app.ui.screens.StatisticsScreen
import com.zenfocus.app.ui.screens.StrictModeScreen
import com.zenfocus.app.ui.theme.DarkBackground
import com.zenfocus.app.ui.theme.DarkSurface
import com.zenfocus.app.ui.theme.TealPrimary
import com.zenfocus.app.ui.theme.TextPrimary
import com.zenfocus.app.ui.theme.TextSecondary
import com.zenfocus.app.ui.theme.ZenFocusTheme
import androidx.lifecycle.lifecycleScope
import com.zenfocus.app.data.repository.ProfileRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

sealed class Screen {
    data object Dashboard : Screen()
    data object Profiles : Screen()
    data object StrictMode : Screen()
    data object Statistics : Screen()
    data class ProfileEditor(val profileId: Long) : Screen()
    data object EmergencyUnblock : Screen()
    data object Permissions : Screen()
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    PROFILES("Profiles", Icons.Default.Layers),
    STRICT_MODE("Strict", Icons.Default.Lock),
    STATS("Stats", Icons.Default.BarChart)
}

class MainActivity : ComponentActivity() {

    private val profileRepository: ProfileRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val navigateToExtra = intent.getStringExtra(EXTRA_NAVIGATE_TO)

        setContent {
            ZenFocusTheme {
                var currentScreen by remember {
                    mutableStateOf<Screen>(
                        if (navigateToExtra == "emergency_unblock") Screen.EmergencyUnblock else Screen.Dashboard
                    )
                }
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    bottomBar = {
                        if (currentScreen is Screen.Dashboard ||
                            currentScreen is Screen.Profiles ||
                            currentScreen is Screen.StrictMode ||
                            currentScreen is Screen.Statistics
                        ) {
                            NavigationBar(
                                containerColor = DarkSurface,
                                contentColor = TealPrimary,
                                tonalElevation = 8.dp
                            ) {
                                NavigationTab.entries.forEachIndexed { index, tab ->
                                    NavigationBarItem(
                                        selected = selectedTab == index,
                                        onClick = {
                                            selectedTab = index
                                            currentScreen = when (index) {
                                                0 -> Screen.Dashboard
                                                1 -> Screen.Profiles
                                                2 -> Screen.StrictMode
                                                3 -> Screen.Statistics
                                                else -> Screen.Dashboard
                                            }
                                        },
                                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                        label = { Text(text = tab.title) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = TealPrimary,
                                            indicatorColor = TealPrimary,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    },
                    containerColor = DarkBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "MainScreenNavigation"
                        ) { screen ->
                            when (screen) {
                                is Screen.Dashboard -> DashboardScreen(
                                    onNavigateToProfileEditor = { profileId ->
                                        currentScreen = Screen.ProfileEditor(profileId)
                                    },
                                    onNavigateToPermissions = {
                                        currentScreen = Screen.Permissions
                                    }
                                )
                                is Screen.Profiles -> ProfilesScreen(
                                    onNavigateToEditor = { profileId ->
                                        currentScreen = Screen.ProfileEditor(profileId)
                                    }
                                )
                                is Screen.StrictMode -> StrictModeScreen(
                                    onNavigateToEmergency = {
                                        currentScreen = Screen.EmergencyUnblock
                                    }
                                )
                                is Screen.Statistics -> StatisticsScreen()
                                is Screen.ProfileEditor -> ProfileEditorScreen(
                                    profileId = screen.profileId,
                                    onNavigateBack = {
                                        currentScreen = Screen.Profiles
                                    }
                                )
                                 is Screen.EmergencyUnblock -> EmergencyUnblockScreen(
                                    onDismiss = {
                                        currentScreen = Screen.Dashboard
                                    },
                                    onUnblockConfirmed = {
                                        lifecycleScope.launch {
                                            profileRepository.deactivateAllStrictMode()
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Emergency Unblock: Strict Mode restrictions released.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        currentScreen = Screen.Dashboard
                                    }
                                )
                                is Screen.Permissions -> PermissionWizardScreen(
                                    onNavigateBack = {
                                        currentScreen = Screen.Dashboard
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Runtime launcher icon switching: Enables target alias (1-19) or MainActivity (0)
     * and disables other components without killing the app process.
     */
    fun switchAppIcon(aliasIndex: Int) {
        val pm = packageManager
        // If aliasIndex == 0, enable MainActivity default launcher entry
        val mainComponent = ComponentName(this, MainActivity::class.java)
        pm.setComponentEnabledSetting(
            mainComponent,
            if (aliasIndex == 0) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        for (i in 1..19) {
            val component = ComponentName(this, "com.zenfocus.app.ui.MainActivityAlias$i")
            val newState = if (i == aliasIndex) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pm.setComponentEnabledSetting(
                component,
                newState,
                PackageManager.DONT_KILL_APP
            )
        }
        Toast.makeText(this, "App icon updated. Check your home screen.", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_NAVIGATE_TO = "extra_navigate_to"
    }
}

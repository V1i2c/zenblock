package com.zenfocus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenfocus.app.domain.model.DayFlags
import com.zenfocus.app.domain.model.SubAppsCatalog
import com.zenfocus.app.ui.theme.CardBorder
import com.zenfocus.app.ui.theme.CoralAccent
import com.zenfocus.app.ui.theme.DarkBackground
import com.zenfocus.app.ui.theme.DarkSurface
import com.zenfocus.app.ui.theme.DarkSurfaceVariant
import com.zenfocus.app.ui.theme.TealPrimary
import com.zenfocus.app.ui.theme.TextPrimary
import com.zenfocus.app.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    profileId: Long,
    viewModel: ProfileEditorViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(profileId) {
        viewModel.loadProfile(profileId)
    }

    val state by viewModel.uiState.collectAsState()
    var newWebsiteInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (profileId == 0L) "New Profile" else "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(16.dp)
            ) {
                val isLocked = state.isStrictModeEnabled && state.profileId != 0L
                Button(
                    onClick = { viewModel.saveProfile(onNavigateBack) },
                    enabled = !isLocked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        disabledContainerColor = DarkSurfaceVariant
                    )
                ) {
                    Text(
                        text = if (isLocked) "Locked by Strict Mode (Read-Only)" else "Save Focus Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) TextSecondary else Color.White
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Name
            item {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Profile Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    )
                )
            }

            // Days of the Week Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Days", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DayFlags.entries.forEach { day ->
                                val isSelected = DayFlags.hasFlag(state.daysBitmask, day)
                                val label = day.name.take(3)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (isSelected) TealPrimary else DarkSurfaceVariant,
                                            CircleShape
                                        )
                                        .clickable { viewModel.toggleDay(day) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Time Interval
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Schedule Time Range", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        val startH = state.startMinutes / 60
                        val startM = state.startMinutes % 60
                        val endH = state.endMinutes / 60
                        val endM = state.endMinutes % 60
                        Text(
                            text = String.format("%02d:%02d - %02d:%02d", startH, startM, endH, endM),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.updateTimeRange(540, 1020) }, // 9am - 5pm
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Text("Workday (9-5)", fontSize = 12.sp, color = TextPrimary)
                            }
                            Button(
                                onClick = { viewModel.updateTimeRange(1320, 420) }, // 10pm - 7am
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Text("Night (10pm-7am)", fontSize = 12.sp, color = TextPrimary)
                            }
                            Button(
                                onClick = { viewModel.updateTimeRange(0, 1439) }, // All day
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Text("24/7", fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Sub-App Granular Blocking (Shorts, Reels, Stories)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Granular Sub-App Blocking",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Intercept addictive feeds while keeping the main app usable:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SubAppsCatalog.SUB_APPS.forEach { subApp ->
                            val isChecked = state.selectedSubApps.contains(subApp.tag)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleSubApp(subApp.tag) }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = subApp.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Text(text = subApp.description, fontSize = 11.sp, color = TextSecondary)
                                }
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleSubApp(subApp.tag) },
                                    colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                                )
                            }
                        }
                    }
                }
            }

            // Blocked Websites Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Blocked Websites", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                        Text("Inspects address bars across 25+ mobile web browsers:", fontSize = 12.sp, color = TextSecondary)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newWebsiteInput,
                                onValueChange = { newWebsiteInput = it },
                                placeholder = { Text("e.g. reddit.com", fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = TealPrimary,
                                    unfocusedBorderColor = CardBorder,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    viewModel.addWebsite(newWebsiteInput)
                                    newWebsiteInput = ""
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = TealPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        state.selectedWebsites.forEach { domain ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = domain, fontSize = 14.sp, color = TextPrimary)
                                IconButton(onClick = { viewModel.removeWebsite(domain) }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Anti-Tamper & Notifications Toggles
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Engage Strict Mode", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                                Text("Locks profile from editing or disabling", fontSize = 12.sp, color = CoralAccent)
                            }
                            Switch(
                                checked = state.isStrictModeEnabled,
                                onCheckedChange = { viewModel.toggleStrictMode(it) },
                                enabled = state.profileId == 0L || !state.isStrictModeEnabled,
                                colors = SwitchDefaults.colors(checkedTrackColor = CoralAccent)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Suppress Notifications", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                                Text("Silently logs blocked notifications safely", fontSize = 12.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = state.notificationBlockingEnabled,
                                onCheckedChange = { viewModel.toggleNotificationBlocking(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = TealPrimary)
                            )
                        }
                    }
                }
            }

            // Blocked Applications List
            item {
                Text(
                    text = "Blocked Applications (${state.selectedApps.size} selected)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }

            items(state.installedApps) { app ->
                val isSelected = state.selectedApps.contains(app.packageName)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleApp(app.packageName) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.appName,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = TextPrimary
                        )
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleApp(app.packageName) },
                            colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

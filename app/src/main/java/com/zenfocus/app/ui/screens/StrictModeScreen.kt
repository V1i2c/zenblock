package com.zenfocus.app.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenfocus.app.domain.model.StrictModeAccessMethod
import com.zenfocus.app.domain.model.StrictModeOption
import com.zenfocus.app.service.receiver.DeviceAdminReceiverImpl
import com.zenfocus.app.ui.theme.CoralAccent
import com.zenfocus.app.ui.theme.DarkBackground
import com.zenfocus.app.ui.theme.DarkSurface
import com.zenfocus.app.ui.theme.TealPrimary
import com.zenfocus.app.ui.theme.TextPrimary
import com.zenfocus.app.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@Composable
fun StrictModeScreen(
    viewModel: StrictModeViewModel = koinViewModel(),
    onNavigateToEmergency: () -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.configState.collectAsState()
    val profiles by viewModel.activeProfiles.collectAsState()

    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
    val adminComponent = ComponentName(context, DeviceAdminReceiverImpl::class.java)
    val isAdminActive = dpm?.isAdminActive(adminComponent) == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(CoralAccent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Strict Mode", tint = CoralAccent)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Strict Mode",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Zero-Bypass Anti-Tamper Protection",
                        fontSize = 13.sp,
                        color = CoralAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Device Admin Anti-Uninstall Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = "Admin", tint = TealPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Device Administrator",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = if (isAdminActive) "PROTECTED" else "UNPROTECTED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAdminActive) TealPrimary else CoralAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Prevents uninstalling ZenFocus while focus sessions are active. Android blocks app deletion while Device Admin is enabled.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isAdminActive) {
                        Button(
                            onClick = {
                                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                    putExtra(
                                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                        "Required to enforce Strict Mode and prevent impulsive uninstallation during focus sessions."
                                    )
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text("Activate Anti-Uninstall Protection", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Anti-Tamper Safeguards Options
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Anti-Tampering Enforcement",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    StrictModeOption.entries.forEach { option ->
                        val isEnabled = StrictModeOption.hasOption(config.selectedOptionsBitmask, option)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = option.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(text = option.description, fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { viewModel.toggleOption(option) },
                                colors = SwitchDefaults.colors(checkedTrackColor = CoralAccent)
                            )
                        }
                    }
                }
            }
        }

        // Deactivation Safeguards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Power, contentDescription = "Charger", tint = TealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deactivation Conditions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose the requirements that must be met to unlock your profiles:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StrictModeAccessMethod.entries.forEach { method ->
                        val isChecked = StrictModeAccessMethod.hasMethod(config.selectedDeactivationMethod, method)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = method.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(text = method.description, fontSize = 11.sp, color = TextSecondary)
                            }
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.toggleMethod(method) },
                                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                            )
                        }
                    }
                }
            }
        }

        // Lock Active Profiles Section
        item {
            Text(
                text = "Engage Strict Mode on Profiles (${profiles.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        if (profiles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Text(
                        text = "No active profiles available to lock. Enable a profile in the Profiles tab first.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(profiles) { profile ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (profile.isStrictModeActive) "LOCKED with Strict Mode" else "Ready to Lock",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (profile.isStrictModeActive) CoralAccent else TealPrimary
                            )
                        }

                        if (!profile.isStrictModeActive) {
                            Button(
                                onClick = { viewModel.applyStrictModeToProfile(profile, 60) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CoralAccent)
                            ) {
                                Text("Engage (1h)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(CoralAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("LOCKED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoralAccent)
                            }
                        }
                    }
                }
            }
        }

        // Emergency Unblock Backdoor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Emergency Unblock Flow",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "10-step psychological reflection questionnaire with disabled clipboard copy/paste, designed to break impulsive bypasses.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToEmergency,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralAccent)
                    ) {
                        Text("Preview Emergency Unblock Flow", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

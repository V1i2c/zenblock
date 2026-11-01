package com.zenfocus.app.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenfocus.app.ui.components.NoOpTextToolbar
import com.zenfocus.app.ui.theme.CardBorder
import com.zenfocus.app.ui.theme.CoralAccent
import com.zenfocus.app.ui.theme.DarkBackground
import com.zenfocus.app.ui.theme.DarkSurface
import com.zenfocus.app.ui.theme.TealPrimary
import com.zenfocus.app.ui.theme.TextPrimary
import com.zenfocus.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyUnblockScreen(
    onDismiss: () -> Unit,
    onUnblockConfirmed: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 8

    val targetPhrase = "I choose to break my focus and accept full responsibility"
    var typedPhrase by remember { mutableStateOf("") }
    var urgeLevel by remember { mutableStateOf(7f) }
    var cooldownSeconds by remember { mutableIntStateOf(15) }

    val noOpToolbar = remember { NoOpTextToolbar() }

    LaunchedEffect(step) {
        if (step == 7) {
            while (cooldownSeconds > 0) {
                delay(1000)
                cooldownSeconds--
            }
            step = 8
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Reflection Gate", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { step.toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = CoralAccent,
                trackColor = DarkSurface
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Step $step of $totalSteps",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(targetState = step, label = "ReflectionSteps") { currentStep ->
                when (currentStep) {
                    1 -> StepView(
                        title = "Breaking Your Commitment",
                        subtitle = "You are requesting to bypass your focus restrictions. Remember that urge states are temporary, but deep focus creates long-term success.",
                        buttonText = "I Understand, Continue",
                        onNext = { step = 2 }
                    )
                    2 -> StepView(
                        title = "Why Did You Block This?",
                        subtitle = "You originally locked these distractions because they were stealing hours of your life without giving value back. Has anything fundamentally changed right now?",
                        buttonText = "Continue Reflection",
                        onNext = { step = 3 }
                    )
                    3 -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Rate Your Current Urge", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Acknowledge the physical sensation of the urge without reacting impulsively.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Text(
                            text = "${urgeLevel.toInt()} / 10",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralAccent
                        )
                        Slider(
                            value = urgeLevel,
                            onValueChange = { urgeLevel = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = CoralAccent,
                                activeTrackColor = CoralAccent,
                                inactiveTrackColor = DarkSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Button(
                            onClick = { step = 4 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text("Next Step")
                        }
                    }
                    4 -> StepView(
                        title = "Visualizing the Outcome",
                        subtitle = "How will you feel 30 minutes from now if you give in to this distraction? Will you feel refreshed, or will you feel regret for breaking your momentum?",
                        buttonText = "I Accept the Risk",
                        onNext = { step = 5 }
                    )
                    5 -> StepView(
                        title = "Pause & Reset",
                        subtitle = "Before continuing, close your eyes and take 3 deep breaths. Many dopamine cravings subside within 90 seconds if you pause.",
                        buttonText = "Proceed to Typing Gate",
                        onNext = { step = 6 }
                    )
                    6 -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Manual Commitment Gate",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To eliminate impulsive taps, copy-paste is completely disabled. Type the exact sentence below character-by-character:",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Text(
                                text = targetPhrase,
                                modifier = Modifier.padding(14.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CoralAccent,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Crucial: LocalTextToolbar provided with NoOpTextToolbar to strictly block clipboard/paste
                        CompositionLocalProvider(LocalTextToolbar provides noOpToolbar) {
                            OutlinedTextField(
                                value = typedPhrase,
                                onValueChange = { typedPhrase = it },
                                placeholder = { Text("Type phrase here...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = if (typedPhrase.trim() == targetPhrase) TealPrimary else CardBorder,
                                    unfocusedBorderColor = CardBorder,
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val isMatch = typedPhrase.trim() == targetPhrase
                        Button(
                            onClick = { if (isMatch) step = 7 },
                            enabled = isMatch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralAccent)
                        ) {
                            Text(if (isMatch) "Verified: Proceed to Cooldown" else "Type Sentence to Unlock")
                        }
                    }
                    7 -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cooldown Waiting Period",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Mandatory cooldown countdown. Take this time to reflect before access is granted.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(DarkSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${cooldownSeconds}s",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralAccent
                            )
                        }
                    }
                    8 -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Released",
                            tint = CoralAccent,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Emergency Unblock Complete",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Strict Mode has been paused. Remember your core commitments and resume focus when ready.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = onUnblockConfirmed,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralAccent)
                        ) {
                            Text("Release Focus Restrictions")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (step < 7) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Stay Committed (Return to Focus)", color = TealPrimary)
                }
            }
        }
    }
}

@Composable
private fun StepView(
    title: String,
    subtitle: String,
    buttonText: String,
    onNext: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            Text(buttonText, fontWeight = FontWeight.SemiBold)
        }
    }
}

package com.zenfocus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenfocus.app.ui.theme.CoralAccent
import com.zenfocus.app.ui.theme.DarkBackground
import com.zenfocus.app.ui.theme.DarkSurface
import com.zenfocus.app.ui.theme.TealPrimary
import com.zenfocus.app.ui.theme.TextPrimary
import com.zenfocus.app.ui.theme.TextSecondary

@Composable
fun OverlayLockScreen(
    profileName: String,
    blockedTarget: String,
    blockReason: String,
    blockType: String,
    onCloseApp: () -> Unit,
    onEmergencyUnblock: () -> Unit
) {
    val quotes = remember {
        listOf(
            "\"Focus is a muscle. Every time you resist distraction, you build mental strength.\"",
            "\"Do not give up what you want most for what you want now.\"",
            "\"The secret of change is to focus all your energy not on fighting the old, but on building the new.\"",
            "\"Your attention is your most precious asset. Guard it ruthlessly.\"",
            "\"Take a breath. You are stronger than a brief urge.\""
        )
    }
    val randomQuote = remember { quotes.random() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE60A0E14)), // Translucent serene dark backdrop
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Focus Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(TealPrimary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = TealPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Stay Focused",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$blockedTarget is restricted by \"$profileName\"",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TealPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Inspirational Quote Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = randomQuote,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Close App Action
                Button(
                    onClick = onCloseApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(
                        text = "Return to Focus",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Emergency Unblock
                OutlinedButton(
                    onClick = onEmergencyUnblock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAccent)
                ) {
                    Text(
                        text = "Emergency Unblock",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

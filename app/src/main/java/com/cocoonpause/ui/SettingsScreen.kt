package com.cocoonpause.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocoonpause.service.PauseOverlayService
import com.cocoonpause.tools.PrefsManager
import com.cocoonpause.ui.theme.*

@Composable
fun SettingsScreen(
    capturedKeys: Set<Int>,
    isCapturing: Boolean,
    onStartCapture: () -> Unit,
    onClearBinding: () -> Unit,
    onSaveBinding: () -> Unit,
) {
    val context = LocalContext.current
    val serviceRunning = PauseOverlayService.instance != null
    val currentBinding = remember(capturedKeys) {
        PrefsManager.formatKeycodes(
            if (capturedKeys.isNotEmpty()) capturedKeys
            else PrefsManager.getTriggerKeycodes(context),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CocoonBlack)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {

        // ── App title ──────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Gamepad,
                contentDescription = null,
                tint = CocoonTextPrimary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "CocoonPause",
                    color = CocoonTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "System-wide pause overlay for Odin 2",
                    color = CocoonTextSecondary,
                    fontSize = 12.sp,
                )
            }
        }

        // ── Service status card ────────────────────────────────────────
        SettingsCard(title = "Accessibility Service") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val icon  = if (serviceRunning) Icons.Filled.CheckCircle else Icons.Filled.Error
                val tint  = if (serviceRunning) Color(0xFF4CAF50) else CocoonDanger
                val label = if (serviceRunning) "Running" else "Not enabled"

                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, color = CocoonTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    if (!serviceRunning) {
                        Text(
                            "Enable CocoonPause in Accessibility settings",
                            color = CocoonTextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
                if (!serviceRunning) {
                    Spacer(Modifier.width(12.dp))
                    TextButton(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        },
                    ) {
                        Text("Open Settings", color = CocoonAccent, fontSize = 13.sp)
                    }
                }
            }
        }

        // ── Trigger button card ────────────────────────────────────────
        SettingsCard(title = "Trigger Button") {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Current binding display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Current binding",
                            color = CocoonTextSecondary,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = if (isCapturing && capturedKeys.isNotEmpty())
                                PrefsManager.formatKeycodes(capturedKeys) + "  (press more or tap Save)"
                            else if (isCapturing)
                                "Press a button on your Odin 2…"
                            else
                                currentBinding,
                            color = CocoonTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                // Capture instruction
                if (isCapturing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CocoonSurface2)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CocoonAccent,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (capturedKeys.isEmpty())
                                "Waiting for button press…"
                            else
                                "Hold combo or tap Save",
                            color = CocoonTextSecondary,
                            fontSize = 13.sp,
                        )
                    }
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isCapturing) {
                        OutlinedButton(
                            onClick = onStartCapture,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CocoonAccent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CocoonDivider),
                        ) {
                            Text("Configure", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = onClearBinding,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CocoonDanger),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CocoonDivider),
                        ) {
                            Text("Clear", fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = onSaveBinding,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CocoonAccent, contentColor = CocoonBlack),
                            enabled = capturedKeys.isNotEmpty(),
                        ) {
                            Text("Save", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onClearBinding, // acts as cancel when capturing
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CocoonTextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CocoonDivider),
                        ) {
                            Text("Cancel", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ── How it works hint ──────────────────────────────────────────
        SettingsCard(title = "How it works") {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HintRow("1.", "Enable the accessibility service above.")
                HintRow("2.", "Configure your trigger button (single or combo).")
                HintRow("3.", "Press it from any game or app to open the overlay.")
                HintRow("4.", "Exit Game force-stops the current app and goes home to Cocoon.")
                HintRow("💡", "Requires the Odin 2 firmware PServer (same as OdinTools).")
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CocoonSurface),
    ) {
        Text(
            text = title.uppercase(),
            color = CocoonTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 2.dp),
        )
        HorizontalDivider(color = CocoonDivider)
        content()
    }
}

@Composable
private fun HintRow(prefix: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(prefix, color = CocoonTextSecondary, fontSize = 12.sp, modifier = Modifier.width(22.dp))
        Text(text, color = CocoonTextSecondary, fontSize = 12.sp)
    }
}

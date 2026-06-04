package com.cocoonpause.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocoonpause.models.ControllerStyle
import com.cocoonpause.models.FanMode
import com.cocoonpause.models.L2R2Style
import com.cocoonpause.models.PerfMode
import com.cocoonpause.tools.ShellExecutor
import com.cocoonpause.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PauseMenuContent(
    executor: ShellExecutor,
    foregroundPackage: String,
    onDismiss: () -> Unit,
    onExitGame: () -> Unit,
    onScreenshot: () -> Unit,
) {
    // ── State ───────────────────────────────────────────────────────────
    var controllerStyle by remember { mutableStateOf<ControllerStyle>(ControllerStyle.Unknown) }
    var l2r2Style       by remember { mutableStateOf<L2R2Style>(L2R2Style.Unknown) }
    var perfMode        by remember { mutableStateOf<PerfMode>(PerfMode.Unknown) }
    var fanMode         by remember { mutableStateOf<FanMode>(FanMode.Unknown) }
    var loading         by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    // Load current device values once on first composition
    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            controllerStyle = ControllerStyle.getStyle(executor)
            l2r2Style       = L2R2Style.getStyle(executor)
            perfMode        = PerfMode.getMode(executor)
            fanMode         = FanMode.getMode(executor)
        }
        loading = false
    }

    // ── Full-screen scrim (tap outside card to dismiss) ────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        // ── Card ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CocoonSurface)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { /* absorb taps so they don't fall through to scrim */ }
        ) {

            // ── Header ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CocoonSurface2)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.SportsEsports,
                    contentDescription = null,
                    tint = CocoonTextPrimary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "PAUSED",
                    color = CocoonTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = CocoonTextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            HorizontalDivider(color = CocoonDivider)

            // ── Screenshot ─────────────────────────────────────────────
            ActionRow(
                icon  = Icons.Filled.CameraAlt,
                label = "Screenshot",
                onClick = onScreenshot,
            )

            HorizontalDivider(color = CocoonDivider)

            // ── Options header ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = null,
                    tint = CocoonTextSecondary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Options",
                    color = CocoonTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                )
            }

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = CocoonTextSecondary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                // Controller Mode
                ModeRow(
                    label        = "Controller Mode",
                    currentValue = controllerStyle.displayName,
                    onPrev = {
                        scope.launch(Dispatchers.IO) {
                            val next = ControllerStyle.prev(controllerStyle)
                            next.enable(executor)
                            controllerStyle = next
                        }
                    },
                    onNext = {
                        scope.launch(Dispatchers.IO) {
                            val next = ControllerStyle.next(controllerStyle)
                            next.enable(executor)
                            controllerStyle = next
                        }
                    },
                )

                // L2/R2 Mode
                ModeRow(
                    label        = "L2/R2 Mode",
                    currentValue = l2r2Style.displayName,
                    onPrev = {
                        scope.launch(Dispatchers.IO) {
                            val next = L2R2Style.prev(l2r2Style)
                            next.enable(executor)
                            l2r2Style = next
                        }
                    },
                    onNext = {
                        scope.launch(Dispatchers.IO) {
                            val next = L2R2Style.next(l2r2Style)
                            next.enable(executor)
                            l2r2Style = next
                        }
                    },
                )

                // Performance Mode
                ModeRow(
                    label        = "Performance",
                    currentValue = perfMode.displayName,
                    onPrev = {
                        scope.launch(Dispatchers.IO) {
                            val next = PerfMode.prev(perfMode)
                            next.enable(executor)
                            perfMode = next
                        }
                    },
                    onNext = {
                        scope.launch(Dispatchers.IO) {
                            val next = PerfMode.next(perfMode)
                            next.enable(executor)
                            perfMode = next
                        }
                    },
                )

                // Fan Mode
                ModeRow(
                    label        = "Fan Mode",
                    currentValue = fanMode.displayName,
                    onPrev = {
                        scope.launch(Dispatchers.IO) {
                            val next = FanMode.prev(fanMode)
                            next.enable(executor)
                            fanMode = next
                        }
                    },
                    onNext = {
                        scope.launch(Dispatchers.IO) {
                            val next = FanMode.next(fanMode)
                            next.enable(executor)
                            fanMode = next
                        }
                    },
                )
            }

            HorizontalDivider(color = CocoonDivider)

            // ── Exit Game ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExitGame() }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = CocoonDanger,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text  = "Exit Game",
                    color = CocoonDanger,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

// ── Reusable row components ────────────────────────────────────────────────

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CocoonTextPrimary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text  = label,
            color = CocoonTextPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun ModeRow(
    label: String,
    currentValue: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = label,
            color    = CocoonTextPrimary,
            fontSize = 14.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )

        // ◀  Value  ▶
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onPrev,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "Previous",
                    tint = CocoonTextSecondary,
                )
            }

            Text(
                text      = currentValue,
                color     = CocoonTextSecondary,
                fontSize  = 13.sp,
                textAlign = TextAlign.Center,
                modifier  = Modifier.widthIn(min = 76.dp),
            )

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Next",
                    tint = CocoonTextSecondary,
                )
            }
        }
    }
}

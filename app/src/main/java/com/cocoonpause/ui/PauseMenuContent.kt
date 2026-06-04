package com.cocoonpause.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocoonpause.models.*
import com.cocoonpause.tools.ShellExecutor
import com.cocoonpause.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PauseMenuContent(
    executor: ShellExecutor,
    foregroundPackage: String,
    onDismiss: () -> Unit,
    onExitGame: () -> Unit,
    onScreenshot: () -> Unit,
) {
    var controllerStyle by remember { mutableStateOf<ControllerStyle>(ControllerStyle.Unknown) }
    var l2r2Style       by remember { mutableStateOf<L2R2Style>(L2R2Style.Unknown) }
    var perfMode        by remember { mutableStateOf<PerfMode>(PerfMode.Unknown) }
    var fanMode         by remember { mutableStateOf<FanMode>(FanMode.Unknown) }
    var loading         by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val firstFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            controllerStyle = ControllerStyle.getStyle(executor)
            l2r2Style       = L2R2Style.getStyle(executor)
            perfMode        = PerfMode.getMode(executor)
            fanMode         = FanMode.getMode(executor)
        }
        loading = false
        delay(150)
        runCatching { firstFocus.requestFocus() }
    }

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
        Column(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CocoonSurface)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(CocoonSurface2)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.SportsEsports, null, tint = CocoonTextPrimary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text("PAUSED", color = CocoonTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, "Dismiss", tint = CocoonTextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            HorizontalDivider(color = CocoonDivider)

            ActionRow(Icons.Filled.CameraAlt, "Screenshot", onScreenshot, firstFocus)

            HorizontalDivider(color = CocoonDivider)

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Tune, null, tint = CocoonTextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Options", color = CocoonTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            }

            if (loading) {
                Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), Alignment.Center) {
                    CircularProgressIndicator(color = CocoonTextSecondary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else {
                ModeRow("Controller Mode", controllerStyle.displayName,
                    onPrev = { scope.launch(Dispatchers.IO) { val n = ControllerStyle.prev(controllerStyle); n.enable(executor); controllerStyle = n } },
                    onNext = { scope.launch(Dispatchers.IO) { val n = ControllerStyle.next(controllerStyle); n.enable(executor); controllerStyle = n } })
                ModeRow("L2/R2 Mode", l2r2Style.displayName,
                    onPrev = { scope.launch(Dispatchers.IO) { val n = L2R2Style.prev(l2r2Style); n.enable(executor); l2r2Style = n } },
                    onNext = { scope.launch(Dispatchers.IO) { val n = L2R2Style.next(l2r2Style); n.enable(executor); l2r2Style = n } })
                ModeRow("Performance", perfMode.displayName,
                    onPrev = { scope.launch(Dispatchers.IO) { val n = PerfMode.prev(perfMode); n.enable(executor); perfMode = n } },
                    onNext = { scope.launch(Dispatchers.IO) { val n = PerfMode.next(perfMode); n.enable(executor); perfMode = n } })
                ModeRow("Fan Mode", fanMode.displayName,
                    onPrev = { scope.launch(Dispatchers.IO) { val n = FanMode.prev(fanMode); n.enable(executor); fanMode = n } },
                    onNext = { scope.launch(Dispatchers.IO) { val n = FanMode.next(fanMode); n.enable(executor); fanMode = n } })
            }

            HorizontalDivider(color = CocoonDivider)

            ActionRow(Icons.Filled.Close, "Exit Game", onExitGame, labelColor = CocoonDanger)
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null,
    labelColor: Color = CocoonTextPrimary,
) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .background(if (focused) CocoonSurface2 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = labelColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = labelColor, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
private fun ModeRow(
    label: String,
    currentValue: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .onPreviewKeyEvent { e ->
                when {
                    e.key == Key.DirectionLeft  && e.type == KeyEventType.KeyDown -> { onPrev(); true }
                    e.key == Key.DirectionRight && e.type == KeyEventType.KeyDown -> { onNext(); true }
                    else -> false
                }
            }
            .background(if (focused) CocoonSurface2 else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = CocoonTextPrimary, fontSize = 14.sp,
            modifier = Modifier.weight(1f).padding(start = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronLeft, "Previous", tint = CocoonTextSecondary)
            }
            Text(currentValue, color = CocoonTextSecondary, fontSize = 13.sp,
                textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 76.dp))
            IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronRight, "Next", tint = CocoonTextSecondary)
            }
        }
    }
}

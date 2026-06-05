package com.cocoonpause.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocoonpause.service.PauseOverlayManager
import com.cocoonpause.ui.theme.*

@Composable
fun PauseMenuContent(
    overlayManager: PauseOverlayManager,
    onDismiss: () -> Unit,
    onExitGame: () -> Unit,
) {
    val sel = overlayManager.selectedIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
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
                Text("PAUSED", color = CocoonTextPrimary, fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, "Dismiss", tint = CocoonTextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            HorizontalDivider(color = CocoonDivider)

            // Options label
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Tune, null, tint = CocoonTextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Options", color = CocoonTextSecondary, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            }

            if (!overlayManager.modesLoaded) {
                Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), Alignment.Center) {
                    CircularProgressIndicator(color = CocoonTextSecondary,
                        modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else {
                ModeRow("Controller Mode", overlayManager.controllerStyle.displayName, sel == 0,
                    { overlayManager.prevForRow(0) }, { overlayManager.nextForRow(0) })
                ModeRow("L2/R2 Mode", overlayManager.l2r2Style.displayName, sel == 1,
                    { overlayManager.prevForRow(1) }, { overlayManager.nextForRow(1) })
                ModeRow("Performance", overlayManager.perfMode.displayName, sel == 2,
                    { overlayManager.prevForRow(2) }, { overlayManager.nextForRow(2) })
                ModeRow("Fan Mode", overlayManager.fanMode.displayName, sel == 3,
                    { overlayManager.prevForRow(3) }, { overlayManager.nextForRow(3) })
            }

            HorizontalDivider(color = CocoonDivider)

            // Exit Game
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (sel == 4) CocoonSurface2 else Color.Transparent)
                    .clickable { onExitGame() }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Close, null, tint = CocoonDanger, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Text("Exit Game", color = CocoonDanger, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun ModeRow(
    label: String, currentValue: String, selected: Boolean,
    onPrev: () -> Unit, onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) CocoonSurface2 else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = CocoonTextPrimary, fontSize = 14.sp,
            modifier = Modifier.weight(1f).padding(start = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronLeft, "Prev", tint = CocoonTextSecondary)
            }
            Text(currentValue, color = CocoonTextSecondary, fontSize = 13.sp,
                textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 76.dp))
            IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronRight, "Next", tint = CocoonTextSecondary)
            }
        }
    }
}

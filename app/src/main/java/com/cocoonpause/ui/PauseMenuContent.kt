package com.cocoonpause.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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

// Material Symbols Rounded codepoints
private const val IC_GAMEPAD  = "\uE30F"  // sports_esports
private const val IC_TUNE     = "\uE429"  // tune
private const val IC_BOLT     = "\uE3E7"  // flash_on
private const val IC_FAN      = "\uF168"  // mode_fan
private const val IC_POWER    = "\uE8AC"  // power_settings_new
private const val IC_CHEV_L   = "\uE5CB"  // chevron_left
private const val IC_CHEV_R   = "\uE5CC"  // chevron_right

@Composable
fun PauseMenuContent(
    overlayManager: PauseOverlayManager,
    onDismiss: () -> Unit,
    onExitGame: () -> Unit,
) {
    val sel    = overlayManager.selectedIndex
    val loaded = overlayManager.modesLoaded

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(500.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CocoonSurface)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {}
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (!loaded) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = CocoonTextSecondary,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                ModeRow(IC_GAMEPAD, "Controller Mode",
                    overlayManager.controllerStyle.displayName, sel == 0,
                    { overlayManager.prevForRow(0) }, { overlayManager.nextForRow(0) })
                ModeRow(IC_TUNE, "L2/R2 Mode",
                    overlayManager.l2r2Style.displayName, sel == 1,
                    { overlayManager.prevForRow(1) }, { overlayManager.nextForRow(1) })
                ModeRow(IC_BOLT, "Performance",
                    overlayManager.perfMode.displayName, sel == 2,
                    { overlayManager.prevForRow(2) }, { overlayManager.nextForRow(2) })
                ModeRow(IC_FAN, "Fan Mode",
                    overlayManager.fanMode.displayName, sel == 3,
                    { overlayManager.prevForRow(3) }, { overlayManager.nextForRow(3) })
                ActionRow(IC_POWER, "Exit Game", sel == 4, CocoonDanger, onExitGame)
            }
        }
    }
}

@Composable
private fun ModeRow(
    icon: String, label: String, value: String, selected: Boolean,
    onPrev: () -> Unit, onNext: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) CocoonSurface2 else Color.Transparent, shape)
            .border(BorderStroke(2.dp, if (selected) Color.White else Color.Transparent), shape)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon,
            fontFamily = MaterialSymbolsRounded,
            fontSize = 26.sp, lineHeight = 26.sp,
            color = CocoonTextPrimary)
        Spacer(Modifier.width(20.dp))
        Text(label,
            color = CocoonTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) {
                Text(IC_CHEV_L,
                    fontFamily = MaterialSymbolsRounded,
                    fontSize = 22.sp, lineHeight = 22.sp,
                    color = CocoonTextSecondary)
            }
            Text(value,
                color = CocoonTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 72.dp))
            IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
                Text(IC_CHEV_R,
                    fontFamily = MaterialSymbolsRounded,
                    fontSize = 22.sp, lineHeight = 22.sp,
                    color = CocoonTextSecondary)
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: String, label: String, selected: Boolean,
    labelColor: Color = CocoonTextPrimary, onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) CocoonSurface2 else Color.Transparent, shape)
            .border(BorderStroke(2.dp, if (selected) Color.White else Color.Transparent), shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon,
            fontFamily = MaterialSymbolsRounded,
            fontSize = 26.sp, lineHeight = 26.sp,
            color = labelColor)
        Spacer(Modifier.width(20.dp))
        Text(label,
            color = labelColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal)
    }
}

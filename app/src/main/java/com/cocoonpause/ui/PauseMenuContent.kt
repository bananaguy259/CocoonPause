package com.cocoonpause.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocoonpause.service.PauseOverlayManager
import com.cocoonpause.ui.theme.*

private val CardBg      = Color(0xFF131313)
private val RowSelected = Color(0xFF222222)
private val DividerClr  = Color(0x14FAFAFA)
private val CocoonWhite = Color(0xFFFAFAFA)
private val GlowHalo    = Color(0x55FAFAFA)
private val ScrimClr    = Color(0xB3000000)

private const val IC_GAMEPAD = "\uE30F"
private const val IC_TUNE    = "\uE429"
private const val IC_BOLT    = "\uE3E7"
private const val IC_FAN     = "\uF168"
private const val IC_POWER   = "\uE8AC"
private const val IC_CHEV_L  = "\uE5CB"
private const val IC_CHEV_R  = "\uE5CC"

@Composable
fun PauseMenuContent(
    overlayManager: PauseOverlayManager,
    onDismiss: () -> Unit,
    onExitGame: () -> Unit,
) {
    val sel       = overlayManager.selectedIndex
    val loaded    = overlayManager.modesLoaded
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScrimClr)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(500.dp)
                .clip(cardShape)
                .background(CardBg)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {},
        ) {
            if (!loaded) {
                Box(Modifier.fillMaxWidth().padding(vertical = 52.dp), Alignment.Center) {
                    CircularProgressIndicator(color = CocoonWhite.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    ModeRow(IC_GAMEPAD, "Controller Mode",
                        overlayManager.controllerStyle.displayName, sel == 0,
                        { overlayManager.prevForRow(0) }, { overlayManager.nextForRow(0) })
                    RowDivider()
                    ModeRow(IC_TUNE, "L2/R2 Mode",
                        overlayManager.l2r2Style.displayName, sel == 1,
                        { overlayManager.prevForRow(1) }, { overlayManager.nextForRow(1) })
                    RowDivider()
                    ModeRow(IC_BOLT, "Performance",
                        overlayManager.perfMode.displayName, sel == 2,
                        { overlayManager.prevForRow(2) }, { overlayManager.nextForRow(2) })
                    RowDivider()
                    ModeRow(IC_FAN, "Fan Mode",
                        overlayManager.fanMode.displayName, sel == 3,
                        { overlayManager.prevForRow(3) }, { overlayManager.nextForRow(3) })
                    RowDivider()
                    ActionRow(IC_POWER, "Exit Game", sel == 4, CocoonDanger, onExitGame)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(28.dp).align(Alignment.TopCenter)

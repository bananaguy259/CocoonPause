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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
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
    val sel    = overlayManager.selectedIndex
    val loaded = overlayManager.modesLoaded
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScrimClr)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        // Outer card — shadow gives white glow around the whole menu
        Box(
            modifier = Modifier
                .width(500.dp)
                .shadow(
                    elevation     = 28.dp,
                    shape         = cardShape,
                    clip          = false,
                    spotColor     = CocoonWhite.copy(alpha = 0.35f),
                    ambientColor  = CocoonWhite.copy(alpha = 0.12f),
                )
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
                // fadingEdges uses BlendMode.DstIn — alpha-masks the content at
                // top/bottom so it fades to transparent (shows CardBg behind it)
                // with NO colour added, just opacity reduction.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadingEdges(edgeFraction = 0.11f),
                ) {
                    Spacer(Modifier.height(6.dp))
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
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

// ── Rows ──────────────────────────────────────────────────────────────────────

@Composable
private fun ModeRow(
    icon: String, label: String, value: String, selected: Boolean,
    onPrev: () -> Unit, onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // No horizontal padding — selector runs edge-to-edge.
            // Card's clip(RoundedCornerShape(20dp)) handles the corners.
            .padding(vertical = 1.dp)
            .whiteGlow(selected, RectangleShape)
            .background(if (selected) RowSelected else Color.Transparent)
            .border(BorderStroke(4.5.dp, if (selected) CocoonWhite else Color.Transparent))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Symbol(icon, 26.dp)
        Spacer(Modifier.width(18.dp))
        Text(label, color = CocoonWhite, fontSize = 17.sp,
            fontWeight = FontWeight.Normal, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) {
                Symbol(IC_CHEV_L, 20.dp, CocoonWhite.copy(alpha = 0.45f))
            }
            Text(value, color = CocoonWhite.copy(alpha = 0.55f), fontSize = 13.sp,
                textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 70.dp))
            IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
                Symbol(IC_CHEV_R, 20.dp, CocoonWhite.copy(alpha = 0.45f))
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: String, label: String, selected: Boolean,
    labelColor: Color = CocoonWhite, onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .whiteGlow(selected, RectangleShape)
            .background(if (selected) RowSelected else Color.Transparent)
            .border(BorderStroke(4.5.dp, if (selected) CocoonWhite else Color.Transparent))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Symbol(icon, 26.dp, labelColor)
        Spacer(Modifier.width(18.dp))
        Text(label, color = labelColor, fontSize = 17.sp, fontWeight = FontWeight.Normal)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun RowDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp, color = DividerClr)
}

@Composable
private fun Symbol(cp: String, size: Dp, color: Color = CocoonWhite) {
    Text(cp, fontFamily = MaterialSymbolsRounded,
        fontSize = size.value.sp, lineHeight = size.value.sp, color = color)
}

// White glow via Android's coloured shadow system (API 28+)
private fun Modifier.whiteGlow(selected: Boolean, shape: Shape): Modifier =
    if (!selected) this
    else shadow(elevation = 22.dp, shape = shape, clip = false,
        spotColor = CocoonWhite, ambientColor = GlowHalo)

// Fading edges using BlendMode.DstIn — multiplies content alpha by the mask.
// No colour is added; only opacity at top/bottom is reduced.
// CompositingStrategy.Offscreen is required for DstIn to work correctly.
private fun Modifier.fadingEdges(edgeFraction: Float = 0.10f): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    0f           to Color.Transparent,
                    edgeFraction to Color.White,
                    (1f - edgeFraction) to Color.White,
                    1f           to Color.Transparent,
                ),
                blendMode = BlendMode.DstIn,
            )
        }

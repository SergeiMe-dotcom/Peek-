package com.fsag.peek.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsag.peek.storage.HudElementConfig
import com.fsag.peek.storage.HudElementId
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Landscape In-Game HUD for "PEEK".
 * Positions, scales, and opacities of all 5 HUD widgets are dynamically driven by [hudConfigs]
 * persisted via [com.fsag.peek.storage.GameStorage].
 */
@Composable
fun GameHud(
    levelNumber: Int,
    levelTitle: String,
    score: Int,
    starsCollected: Int,
    lives: Int,
    maxLives: Int,
    canGrabRope: Boolean,
    isSwinging: Boolean,
    hintText: String,
    hudConfigs: Map<HudElementId, HudElementConfig>,
    onMoveInputChanged: (dx: Float, dy: Float) -> Unit,
    onJumpStateChanged: (pressedThisFrame: Boolean, held: Boolean) -> Unit,
    onGrabStateChanged: (held: Boolean) -> Unit,
    onPauseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_hud_root")
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // 1. Top-Left Pause Button
        val pauseCfg = hudConfigs[HudElementId.PAUSE_BTN]
        if (pauseCfg != null) {
            val baseSize = 52.dp
            val sizePx = with(density) { baseSize.toPx() }
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            ((pauseCfg.normX * widthPx) - sizePx * 0.5f).roundToInt(),
                            ((pauseCfg.normY * heightPx) - sizePx * 0.5f).roundToInt()
                        )
                    }
                    .scale(pauseCfg.scale)
                    .alpha(pauseCfg.opacity)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xCC0F172A),
                    modifier = Modifier
                        .size(baseSize)
                        .border(1.5.dp, Color(0xFF00F0FF).copy(alpha = 0.7f), CircleShape)
                ) {
                    IconButton(
                        onClick = onPauseClicked,
                        modifier = Modifier.testTag("hud_pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = "Pause Game",
                            tint = Color(0xFF00F0FF)
                        )
                    }
                }
            }
        }

        // 2. Top-Center Level Tracker, Live Score, Stars & Health Pill
        val statusCfg = hudConfigs[HudElementId.STATUS_BAR]
        if (statusCfg != null) {
            val pillW = 410.dp
            val pillH = 54.dp
            val pillWPx = with(density) { pillW.toPx() }
            val pillHPx = with(density) { pillH.toPx() }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            ((statusCfg.normX * widthPx) - pillWPx * 0.5f).roundToInt(),
                            ((statusCfg.normY * heightPx) - pillHPx * 0.5f).roundToInt()
                        )
                    }
                    .scale(statusCfg.scale)
                    .alpha(statusCfg.opacity)
                    .testTag("hud_status_pill")
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xD90B1224),
                    modifier = Modifier
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF00F0FF), Color(0xFFFFB800))
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Level X / 20
                        Column {
                            Text(
                                text = "LEVEL $levelNumber / 20",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF00F0FF)
                            )
                            Text(
                                text = levelTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // 3 Collectible Stars
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (s in 1..3) {
                                Icon(
                                    imageVector = if (s <= starsCollected) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "Star $s",
                                    tint = if (s <= starsCollected) Color(0xFFFFB800) else Color(0xFF475569),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Health / Lives
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (h in 1..maxLives) {
                                Icon(
                                    imageVector = if (h <= lives) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Life $h",
                                    tint = if (h <= lives) Color(0xFFFF3366) else Color(0xFF475569),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Live Score
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "SCORE",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                            Text(
                                text = score.toString().padStart(5, '0'),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFFB800),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Subtle stage hint banner at top-right
        if (hintText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 20.dp)
                    .background(Color(0x990B1224), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x4400F0FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp
                )
            }
        }

        // 3. Bottom-Left Floating Directional D-Pad / Joystick
        val dpadCfg = hudConfigs[HudElementId.DPAD]
        if (dpadCfg != null) {
            val dpadDiameter = 144.dp
            val dpadPx = with(density) { dpadDiameter.toPx() }
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            ((dpadCfg.normX * widthPx) - dpadPx * 0.5f).roundToInt(),
                            ((dpadCfg.normY * heightPx) - dpadPx * 0.5f).roundToInt()
                        )
                    }
                    .scale(dpadCfg.scale)
                    .alpha(dpadCfg.opacity)
            ) {
                VirtualDpadJoystick(
                    diameter = dpadDiameter,
                    onDirectionChanged = onMoveInputChanged
                )
            }
        }

        // 4. Bottom-Right Secondary "GRAB / ROPE" Interaction Button
        val ropeCfg = hudConfigs[HudElementId.ROPE_BTN]
        if (ropeCfg != null) {
            val ropeBtnSize = 84.dp
            val ropePx = with(density) { ropeBtnSize.toPx() }
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            ((ropeCfg.normX * widthPx) - ropePx * 0.5f).roundToInt(),
                            ((ropeCfg.normY * heightPx) - ropePx * 0.5f).roundToInt()
                        )
                    }
                    .scale(ropeCfg.scale)
                    .alpha(ropeCfg.opacity)
            ) {
                RopeGrabActionButton(
                    sizeDp = ropeBtnSize,
                    isHighlighted = canGrabRope || isSwinging,
                    isSwinging = isSwinging,
                    onHoldChanged = onGrabStateChanged
                )
            }
        }

        // 5. Bottom-Right Prominent "JUMP" Action Button
        val jumpCfg = hudConfigs[HudElementId.JUMP_BTN]
        if (jumpCfg != null) {
            val jumpBtnSize = 98.dp
            val jumpPx = with(density) { jumpBtnSize.toPx() }
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            ((jumpCfg.normX * widthPx) - jumpPx * 0.5f).roundToInt(),
                            ((jumpCfg.normY * heightPx) - jumpPx * 0.5f).roundToInt()
                        )
                    }
                    .scale(jumpCfg.scale)
                    .alpha(jumpCfg.opacity)
            ) {
                JumpActionButton(
                    sizeDp = jumpBtnSize,
                    onJumpStateChanged = onJumpStateChanged
                )
            }
        }
    }
}

@Composable
private fun VirtualDpadJoystick(
    diameter: androidx.compose.ui.unit.Dp,
    onDirectionChanged: (dx: Float, dy: Float) -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var isActive by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(diameter)
            .testTag("hud_dpad_joystick")
            .pointerInput(Unit) {
                val radius = size.width * 0.5f
                val maxStickTravel = radius * 0.62f
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isActive = true
                    fun updateStick(pos: Offset) {
                        val rawDx = pos.x - radius
                        val rawDy = pos.y - radius
                        val dist = hypot(rawDx, rawDy)
                        val clampedDist = dist.coerceAtMost(maxStickTravel)
                        val angle = kotlin.math.atan2(rawDy, rawDx)
                        val cx = kotlin.math.cos(angle) * clampedDist
                        val cy = kotlin.math.sin(angle) * clampedDist
                        knobOffset = Offset(cx, cy)
                        val normX = (cx / maxStickTravel).coerceIn(-1f, 1f)
                        val normY = (cy / maxStickTravel).coerceIn(-1f, 1f)
                        onDirectionChanged(normX, normY)
                    }
                    updateStick(down.position)
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            change.consume()
                            updateStick(change.position)
                        }
                    } while (event.changes.any { it.pressed })

                    isActive = false
                    knobOffset = Offset.Zero
                    onDirectionChanged(0f, 0f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.5f)
            val outerR = size.width * 0.47f

            // Outer ring
            drawCircle(
                color = Color(0xAA0B1224),
                radius = outerR,
                center = center
            )
            drawCircle(
                color = if (isActive) Color(0xFF00F0FF) else Color(0x8800F0FF),
                radius = outerR,
                center = center,
                style = Stroke(width = if (isActive) 5f else 3f)
            )

            // Directional crosshairs
            drawLine(
                color = Color(0x4400F0FF),
                start = Offset(center.x - outerR * 0.72f, center.y),
                end = Offset(center.x + outerR * 0.72f, center.y),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color(0x4400F0FF),
                start = Offset(center.x, center.y - outerR * 0.72f),
                end = Offset(center.x, center.y + outerR * 0.72f),
                strokeWidth = 2.5f
            )

            // Thumbstick knob
            val knobCenter = center + knobOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00F0FF), Color(0xFF007799)),
                    center = knobCenter,
                    radius = outerR * 0.36f
                ),
                radius = outerR * 0.34f,
                center = knobCenter
            )
            drawCircle(
                color = Color.White,
                radius = outerR * 0.12f,
                center = knobCenter
            )
        }
    }
}

@Composable
private fun JumpActionButton(
    sizeDp: androidx.compose.ui.unit.Dp,
    onJumpStateChanged: (pressedThisFrame: Boolean, held: Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(sizeDp)
            .scale(if (isPressed) 0.92f else 1.0f)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = if (isPressed) {
                        listOf(Color(0xFF00F0FF), Color(0xFF006680))
                    } else {
                        listOf(Color(0xDD0F2442), Color(0xDD091529))
                    }
                )
            )
            .border(
                width = if (isPressed) 3.5.dp else 2.5.dp,
                color = Color(0xFF00F0FF),
                shape = CircleShape
            )
            .testTag("hud_jump_button")
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    onJumpStateChanged(true, true)
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })
                    isPressed = false
                    onJumpStateChanged(false, false)
                }
            }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "JUMP",
                style = MaterialTheme.typography.labelLarge,
                color = if (isPressed) Color(0xFF070B19) else Color(0xFF00F0FF),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "HOLD+",
                style = MaterialTheme.typography.labelMedium,
                color = if (isPressed) Color(0xFF070B19) else Color(0xFF94A3B8),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun RopeGrabActionButton(
    sizeDp: androidx.compose.ui.unit.Dp,
    isHighlighted: Boolean,
    isSwinging: Boolean,
    onHoldChanged: (held: Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "ropePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isHighlighted) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "pulseScale"
    )

    val ringColor = if (isHighlighted || isSwinging) Color(0xFFFFB800) else Color(0xFF38BDF8)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(sizeDp)
            .scale(if (isPressed) 0.92f else pulseScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = if (isPressed || isSwinging) {
                        listOf(Color(0xFFFFB800), Color(0xFF995C00))
                    } else {
                        listOf(Color(0xDD1E2238), Color(0xDD0D1120))
                    }
                )
            )
            .border(
                width = if (isHighlighted || isSwinging) 3.dp else 2.dp,
                color = ringColor,
                shape = CircleShape
            )
            .testTag("hud_rope_button")
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    onHoldChanged(true)
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })
                    isPressed = false
                    onHoldChanged(false)
                }
            }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isSwinging) "SWING" else "GRAB",
                style = MaterialTheme.typography.labelLarge,
                color = if (isPressed || isSwinging) Color(0xFF070B19) else ringColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = "ROPE",
                style = MaterialTheme.typography.labelMedium,
                color = if (isPressed || isSwinging) Color(0xFF070B19) else Color(0xFF94A3B8),
                fontSize = 9.sp
            )
        }
    }
}

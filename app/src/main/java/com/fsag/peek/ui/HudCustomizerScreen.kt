package com.fsag.peek.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsag.peek.storage.GameStorage
import com.fsag.peek.storage.HudElementConfig
import com.fsag.peek.storage.HudElementId
import kotlin.math.roundToInt

/**
 * Interactive Drag-and-Drop Landscape HUD Customizer (`HudCustomizerScreen.kt`).
 * Allows players to drag any HUD element across a visual coordinate grid, adjust its scale
 * (60% - 160%) and opacity (25% - 100%), toggle grid snapping, reset to defaults, and save to [GameStorage].
 */
@Composable
fun HudCustomizerScreen(
    initialConfigs: Map<HudElementId, HudElementConfig>,
    onSaveConfigs: (Map<HudElementId, HudElementConfig>) -> Unit,
    onResetDefaults: () -> Map<HudElementId, HudElementConfig>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val editableConfigs = remember(initialConfigs) {
        mutableStateMapOf<HudElementId, HudElementConfig>().apply {
            putAll(initialConfigs)
        }
    }
    var selectedId by remember { mutableStateOf(HudElementId.DPAD) }
    var snapToGrid by remember { mutableStateOf(true) }
    var savedBannerVisible by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B19))
            .testTag("hud_customizer_screen")
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        // 1. Precision Alignment Grid Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cols = 20
            val rows = 12
            val stepX = size.width / cols
            val stepY = size.height / rows

            for (c in 1 until cols) {
                val x = c * stepX
                drawLine(
                    color = if (c == cols / 2) Color(0x4400F0FF) else Color(0x1A94A3B8),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = if (c == cols / 2) 2f else 1f
                )
            }
            for (r in 1 until rows) {
                val y = r * stepY
                drawLine(
                    color = if (r == rows / 2) Color(0x4400F0FF) else Color(0x1A94A3B8),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (r == rows / 2) 2f else 1f
                )
            }
        }

        // 2. Draggable HUD Elements
        for (id in HudElementId.entries) {
            val cfg = editableConfigs[id] ?: continue
            val isSelected = selectedId == id
            val baseSizeDp: Dp = when (id) {
                HudElementId.DPAD -> 140.dp
                HudElementId.JUMP_BTN -> 96.dp
                HudElementId.ROPE_BTN -> 82.dp
                HudElementId.PAUSE_BTN -> 52.dp
                HudElementId.STATUS_BAR -> 280.dp
            }
            val baseHeightDp: Dp = if (id == HudElementId.STATUS_BAR) 50.dp else baseSizeDp
            val itemW = with(density) { baseSizeDp.toPx() }
            val itemH = with(density) { baseHeightDp.toPx() }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            ((cfg.normX * screenWidthPx) - itemW * 0.5f).roundToInt(),
                            ((cfg.normY * screenHeightPx) - itemH * 0.5f).roundToInt()
                        )
                    }
                    .size(width = baseSizeDp, height = baseHeightDp)
                    .scale(cfg.scale)
                    .alpha(cfg.opacity)
                    .clickable { selectedId = id }
                    .pointerInput(id, snapToGrid, screenWidthPx, screenHeightPx) {
                        detectDragGestures(
                            onDragStart = {
                                selectedId = id
                                savedBannerVisible = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val current = editableConfigs[id] ?: return@detectDragGestures
                                var newNormX = (current.normX + dragAmount.x / screenWidthPx)
                                    .coerceIn(0.05f, 0.95f)
                                var newNormY = (current.normY + dragAmount.y / screenHeightPx)
                                    .coerceIn(0.08f, 0.92f)
                                if (snapToGrid) {
                                    newNormX = (newNormX * 40f).roundToInt() / 40f
                                    newNormY = (newNormY * 24f).roundToInt() / 24f
                                }
                                editableConfigs[id] = current.copy(normX = newNormX, normY = newNormY)
                            }
                        )
                    }
                    .testTag("customizer_item_${id.name}")
            ) {
                DraggableHudPreviewWidget(id = id, isSelected = isSelected)
            }
        }

        // 3. Compact Floating Inspector Panel (Center-Top/Mid so it stays out of corner button zones)
        val activeConfig = editableConfigs[selectedId] ?: GameStorage.defaultHudConfigs().getValue(selectedId)

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xEB0F172A),
            modifier = Modifier
                .align(Alignment.Center)
                .width(450.dp)
                .border(1.5.dp, Color(0xFF00F0FF).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("hud_customizer_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF00F0FF)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "HUD LAYOUT EDITOR",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF00F0FF)
                            )
                            Text(
                                text = "Drag any control on screen • Selected: ${selectedId.displayName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    FilterChip(
                        selected = snapToGrid,
                        onClick = { snapToGrid = !snapToGrid },
                        label = { Text("Snap Grid", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.GridOn,
                                contentDescription = "Snap to Grid",
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0x3300F0FF),
                            selectedLabelColor = Color(0xFF00F0FF)
                        )
                    )
                }

                // Scale Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Scale: ${(activeConfig.scale * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.width(100.dp)
                    )
                    Slider(
                        value = activeConfig.scale,
                        onValueChange = { newScale ->
                            editableConfigs[selectedId] = activeConfig.copy(scale = newScale)
                            savedBannerVisible = false
                        },
                        valueRange = 0.65f..1.50f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00F0FF),
                            activeTrackColor = Color(0xFF00F0FF)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Opacity Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Opacity: ${(activeConfig.opacity * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.width(100.dp)
                    )
                    Slider(
                        value = activeConfig.opacity,
                        onValueChange = { newAlpha ->
                            editableConfigs[selectedId] = activeConfig.copy(opacity = newAlpha)
                            savedBannerVisible = false
                        },
                        valueRange = 0.30f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFB800),
                            activeTrackColor = Color(0xFFFFB800)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Coordinates & Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (savedBannerVisible) {
                            "✓ Layout Saved!"
                        } else {
                            "Pos: X=${(activeConfig.normX * 100).roundToInt()}%  Y=${(activeConfig.normY * 100).roundToInt()}%"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (savedBannerVisible) Color(0xFF00FFA3) else Color(0xFF94A3B8)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                val defaults = onResetDefaults()
                                editableConfigs.clear()
                                editableConfigs.putAll(defaults)
                                savedBannerVisible = true
                            },
                            modifier = Modifier.testTag("hud_reset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset HUD",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RESET", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onSaveConfigs(editableConfigs.toMap())
                                savedBannerVisible = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00F0FF),
                                contentColor = Color(0xFF070B19)
                            ),
                            modifier = Modifier.testTag("hud_save_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Save HUD",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SAVE HUD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableHudPreviewWidget(
    id: HudElementId,
    isSelected: Boolean
) {
    val highlightColor = if (isSelected) Color(0xFFFFB800) else Color(0xFF00F0FF)
    val borderWidth = if (isSelected) 3.dp else 1.5.dp

    when (id) {
        HudElementId.DPAD -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xBB0F172A), CircleShape)
                    .border(borderWidth, highlightColor, CircleShape)
            ) {
                Text(
                    text = "D-PAD\nDRAG ME",
                    style = MaterialTheme.typography.labelLarge,
                    color = highlightColor,
                    fontSize = 11.sp
                )
            }
        }
        HudElementId.JUMP_BTN -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xBB0F2442), CircleShape)
                    .border(borderWidth, highlightColor, CircleShape)
            ) {
                Text(
                    text = "JUMP",
                    style = MaterialTheme.typography.labelLarge,
                    color = highlightColor
                )
            }
        }
        HudElementId.ROPE_BTN -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xBB1E2238), CircleShape)
                    .border(borderWidth, highlightColor, CircleShape)
            ) {
                Text(
                    text = "GRAB\nROPE",
                    style = MaterialTheme.typography.labelMedium,
                    color = highlightColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        HudElementId.PAUSE_BTN -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xBB0F172A), CircleShape)
                    .border(borderWidth, highlightColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = "Pause Widget",
                    tint = highlightColor
                )
            }
        }
        HudElementId.STATUS_BAR -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xBB0B1224), RoundedCornerShape(24.dp))
                    .border(borderWidth, highlightColor, RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = "LEVEL 1 / 20  •  ★★★  •  SCORE",
                    style = MaterialTheme.typography.labelLarge,
                    color = highlightColor,
                    fontSize = 11.sp
                )
            }
        }
    }
}

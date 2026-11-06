package com.fsag.peek.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsag.peek.levels.LevelRegistry
import com.fsag.peek.model.LevelData
import com.fsag.peek.storage.LevelProgressRecord

/**
 * Landscape Level Select Screen (`LevelSelectScreen.kt`) displaying all 20 hand-crafted stages
 * with biome filter tabs, lock/unlock states, 3-star ratings, and high scores.
 */
@Composable
fun LevelSelectScreen(
    levelRecords: Map<Int, LevelProgressRecord>,
    sandboxUnlockAll: Boolean,
    onToggleSandboxUnlockAll: (Boolean) -> Unit,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    // 0 = All 20 Stages, 1..4 = Biome Worlds I..IV
    var selectedWorldFilter by remember { mutableIntStateOf(0) }
    val allLevels = remember { LevelRegistry.allLevels }

    val filteredLevels = remember(selectedWorldFilter, allLevels) {
        when (selectedWorldFilter) {
            1 -> allLevels.filter { it.levelNumber in 1..5 }
            2 -> allLevels.filter { it.levelNumber in 6..10 }
            3 -> allLevels.filter { it.levelNumber in 11..15 }
            4 -> allLevels.filter { it.levelNumber in 16..20 }
            else -> allLevels
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF070B19), Color(0xFF0F1B33))
                )
            )
            .testTag("level_select_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Top Bar: Back Button, Title, World Filter Tabs, and Sandbox Unlock Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("level_select_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Menu",
                            tint = Color(0xFF00F0FF)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "SELECT STAGE (1 - 20)",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF00F0FF)
                        )
                        Text(
                            text = "20 Hand-Crafted Sequential Stages across 4 Worlds",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Quick Sandbox Unlock All Chip so players can jump to any of the 20 stages anytime
                FilterChip(
                    selected = sandboxUnlockAll,
                    onClick = { onToggleSandboxUnlockAll(!sandboxUnlockAll) },
                    label = {
                        Text(
                            text = if (sandboxUnlockAll) "All 20 Unlocked" else "Unlock All 20",
                            fontSize = 11.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (sandboxUnlockAll) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x33FFB800),
                        selectedLabelColor = Color(0xFFFFB800)
                    ),
                    modifier = Modifier.testTag("unlock_all_levels_chip")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // World Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf(
                    0 to "All 20 Stages",
                    1 to "I: Ruins (1-5)",
                    2 to "II: Sanctum (6-10)",
                    3 to "III: Abyss (11-15)",
                    4 to "IV: Citadel (16-20)"
                )
                for ((worldIdx, label) in tabs) {
                    FilterChip(
                        selected = selectedWorldFilter == worldIdx,
                        onClick = { selectedWorldFilter = worldIdx },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0x3300F0FF),
                            selectedLabelColor = Color(0xFF00F0FF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5-Column Landscape Grid of Stages
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("level_select_grid")
            ) {
                items(filteredLevels, key = { it.levelNumber }) { level ->
                    val record = levelRecords[level.levelNumber]
                    val isUnlocked = sandboxUnlockAll || (record?.unlocked == true) || level.levelNumber == 1
                    LevelStageCard(
                        level = level,
                        record = record,
                        isUnlocked = isUnlocked,
                        onClick = {
                            if (isUnlocked) {
                                onLevelSelected(level.levelNumber)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelStageCard(
    level: LevelData,
    record: LevelProgressRecord?,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val accent = level.biome.trimAccentColor
    val stars = record?.starsEarned ?: 0
    val highScore = record?.highScore ?: 0

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isUnlocked) Color(0xE6111C35) else Color(0x990B1120),
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .alpha(if (isUnlocked) 1f else 0.58f)
            .border(
                width = if (isUnlocked) 1.5.dp else 1.dp,
                color = if (isUnlocked) accent.copy(alpha = 0.75f) else Color(0xFF334155),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_card_${level.levelNumber}")
            .padding(10.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "#${level.levelNumber.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isUnlocked) accent else Color(0xFF64748B),
                    fontWeight = FontWeight.Bold
                )

                if (isUnlocked) {
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        for (s in 1..3) {
                            Icon(
                                imageVector = if (s <= stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = null,
                                tint = if (s <= stars) Color(0xFFFFB800) else Color(0xFF475569),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Column {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp
                )
                Text(
                    text = if (highScore > 0) "Best: $highScore" else "${level.ropes.size} Ropes • ${level.platforms.size} Plats",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (highScore > 0) Color(0xFFFFB800) else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUnlocked) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play Level ${level.levelNumber}",
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

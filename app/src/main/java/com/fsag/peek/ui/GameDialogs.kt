package com.fsag.peek.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun PauseDialog(
    levelNumber: Int,
    levelTitle: String,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onCustomizeHud: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA040814))
            .testTag("pause_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF20F172A),
            modifier = Modifier
                .width(420.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF00F0FF), Color(0xFFFFB800))),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "GAME PAUSED",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF00F0FF)
                )
                Text(
                    text = "Stage $levelNumber / 20 — $levelTitle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00F0FF),
                        contentColor = Color(0xFF070B19)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_resume_button")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("RESUME STAGE", fontWeight = FontWeight.Bold)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_retry_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("RETRY")
                    }

                    OutlinedButton(
                        onClick = onCustomizeHud,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_hud_button")
                    ) {
                        Icon(Icons.Filled.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("HUD LAYOUT")
                    }
                }

                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_main_menu_button")
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("MAIN MENU")
                }
            }
        }
    }
}

@Composable
fun LevelClearDialog(
    levelNumber: Int,
    levelTitle: String,
    score: Int,
    starsEarned: Int,
    elapsedSeconds: Float,
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB8040814))
            .testTag("level_clear_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF50F172A),
            modifier = Modifier
                .width(450.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF00FFA3), Color(0xFFFFB800))),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (levelNumber >= 20) "CITADEL CONQUERED!" else "STAGE $levelNumber CLEARED!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF00FFA3)
                )
                Text(
                    text = levelTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )

                // 3-Star Showcase
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        Icon(
                            imageVector = if (i <= starsEarned) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Star $i",
                            tint = if (i <= starsEarned) Color(0xFFFFB800) else Color(0xFF475569),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Metrics Row
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070B19), RoundedCornerShape(14.dp))
                        .padding(vertical = 10.dp, horizontal = 16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SCORE", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFFB800)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CLEAR TIME", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                        Text(
                            text = "${(elapsedSeconds * 10f).roundToInt() / 10f}s",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF00F0FF)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_retry_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("RETRY")
                    }

                    if (levelNumber < 20) {
                        Button(
                            onClick = onNextLevel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FFA3),
                                contentColor = Color(0xFF070B19)
                            ),
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("clear_next_button")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("STAGE ${levelNumber + 1}", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("BACK TO MENU")
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    levelNumber: Int,
    levelTitle: String,
    score: Int,
    onRetry: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB8040814))
            .testTag("game_over_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF50F172A),
            modifier = Modifier
                .width(410.dp)
                .border(2.dp, Color(0xFFFF3366), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CUBE SHATTERED",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFF3366)
                )
                Text(
                    text = "Stage $levelNumber / 20 — $levelTitle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFFB800)
                )

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF3366),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gameover_retry_button")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("TRY AGAIN", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("MAIN MENU")
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    musicVolume: Float,
    sfxVolume: Float,
    hapticsEnabled: Boolean,
    sandboxUnlockAll: Boolean,
    onAudioChanged: (music: Float, sfx: Float, haptics: Boolean) -> Unit,
    onSandboxUnlockAllChanged: (Boolean) -> Unit,
    onOpenHudCustomizer: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB8040814))
            .testTag("settings_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF50F172A),
            modifier = Modifier
                .width(460.dp)
                .border(1.5.dp, Color(0xFF00F0FF), RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "GAME SETTINGS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF00F0FF)
                )

                // Music Volume
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Music: ${(musicVolume * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(110.dp)
                    )
                    Slider(
                        value = musicVolume,
                        onValueChange = { onAudioChanged(it, sfxVolume, hapticsEnabled) },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00F0FF),
                            activeTrackColor = Color(0xFF00F0FF)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // SFX Volume
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SFX: ${(sfxVolume * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(110.dp)
                    )
                    Slider(
                        value = sfxVolume,
                        onValueChange = { onAudioChanged(musicVolume, it, hapticsEnabled) },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFB800),
                            activeTrackColor = Color(0xFFFFB800)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Haptics & Unlock All toggles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tactile Vibration / Haptics", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { onAudioChanged(musicVolume, sfxVolume, it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Unlock All 20 Stages", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Practice any stage in Level Select",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = sandboxUnlockAll,
                        onCheckedChange = onSandboxUnlockAllChanged,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFB800)),
                        modifier = Modifier.testTag("settings_unlock_all_switch")
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenHudCustomizer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CUSTOMIZE HUD")
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00F0FF),
                            contentColor = Color(0xFF070B19)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_close_button")
                    ) {
                        Text("DONE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

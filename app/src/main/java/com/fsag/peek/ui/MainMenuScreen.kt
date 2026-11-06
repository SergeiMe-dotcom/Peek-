package com.fsag.peek.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Landscape Entry Menu (`MainMenuScreen.kt`) for "PEEK".
 * Features atmospheric hero artwork, animated 3D "The Cube" peeking showcase, campaign progress
 * summary, and "Play", "Level Select", "HUD Layout", and "Settings" actions.
 */
@Composable
fun MainMenuScreen(
    highestUnlockedLevel: Int,
    totalStarsEarned: Int,
    totalScoreEarned: Int,
    onPlayClicked: () -> Unit,
    onLevelSelectClicked: () -> Unit,
    onHudCustomizerClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menuCubeAnim")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "hoverOffset"
    )
    val eyePeekShift by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "eyePeekShift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B19))
            .testTag("main_menu_screen")
    ) {
        // Generated Hero Environment Artwork Background
        Image(
            painter = painterResource(id = R.drawable.img_peek_hero_bg_1793887811838),
            contentDescription = "PEEK Atmospheric Chasm Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.42f)
        )

        // Vignette overlay for crisp contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xE6060A17),
                            Color(0x99060A17),
                            Color(0xE6060A17)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 32.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Brand Identity + Animated 3D Cube Showcase
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Live Animated 3D "The Cube" Peeking Canvas
                    Canvas(modifier = Modifier.size(110.dp)) {
                        val cx = size.width * 0.5f
                        val cy = size.height * 0.52f + hoverOffset
                        val s = 62f
                        val x = cx - s * 0.5f
                        val y = cy - s * 0.5f
                        val dx = 14f
                        val dy = -11f

                        // Glow Halo
                        drawCircle(
                            color = Color(0xFF00F0FF).copy(alpha = 0.25f),
                            radius = s * 0.95f,
                            center = Offset(cx, cy)
                        )

                        // Top 3D Face
                        val topPath = Path().apply {
                            moveTo(x, y)
                            lineTo(x + dx, y + dy)
                            lineTo(x + s + dx, y + dy)
                            lineTo(x + s, y)
                            close()
                        }
                        drawPath(topPath, color = Color(0xFF5CE1E6))

                        // Right 3D Face
                        val sidePath = Path().apply {
                            moveTo(x + s, y)
                            lineTo(x + s + dx, y + dy)
                            lineTo(x + s + dx, y + s + dy)
                            lineTo(x + s, y + s)
                            close()
                        }
                        drawPath(sidePath, color = Color(0xFF0A4D68))

                        // Front Face
                        drawRoundRect(
                            color = Color(0xFF10203B),
                            topLeft = Offset(x, y),
                            size = Size(s, s),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF00F0FF), Color(0xFFFFB800))
                            ),
                            topLeft = Offset(x, y),
                            size = Size(s, s),
                            cornerRadius = CornerRadius(12f, 12f),
                            style = Stroke(width = 4f)
                        )

                        // Visor & Expressive Eyes
                        drawRoundRect(
                            color = Color(0xFF060B16),
                            topLeft = Offset(x + 9f, y + 9f),
                            size = Size(s - 18f, s - 18f),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(cx - 16f + eyePeekShift, cy - 6f),
                            size = Size(12f, 13f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(cx + 4f + eyePeekShift, cy - 6f),
                            size = Size(12f, 13f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawCircle(
                            color = Color(0xFF00F0FF),
                            radius = 3f,
                            center = Offset(cx - 9f + eyePeekShift * 1.2f, cy)
                        )
                        drawCircle(
                            color = Color(0xFF00F0FF),
                            radius = 3f,
                            center = Offset(cx + 11f + eyePeekShift * 1.2f, cy)
                        )
                    }

                    Column {
                        Text(
                            text = "PEEK",
                            style = MaterialTheme.typography.displayLarge,
                            color = Color(0xFF00F0FF),
                            fontSize = 52.sp
                        )
                        Text(
                            text = "2.5D MONOLITH & PENDULUM ODYSSEY",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFFFB800),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Guide The Cube across 20 hand-crafted stages of drifting monoliths, wall-climb shafts, and physics-driven pendulum ropes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.padding(end = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Badges
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xCC0F172A),
                        modifier = Modifier.border(1.dp, Color(0x5500F0FF), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalStarsEarned / 60 STARS",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xCC0F172A),
                        modifier = Modifier.border(1.dp, Color(0x55FFB800), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "TOTAL SCORE: $totalScoreEarned",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFFFB800),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Right Column: Navigation Command Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xD90F172A),
                modifier = Modifier
                    .width(340.dp)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(listOf(Color(0xFF00F0FF), Color(0xFFFFB800))),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(22.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onPlayClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00F0FF),
                            contentColor = Color(0xFF070B19)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("menu_play_button")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (highestUnlockedLevel > 1) "PLAY (STAGE $highestUnlockedLevel)" else "PLAY CAMPAIGN",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onLevelSelectClicked,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("menu_level_select_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LEVEL SELECT (1 - 20)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onHudCustomizerClicked,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("menu_hud_customizer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Build,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HUD",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFFFB800),
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onSettingsClicked,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("menu_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SETTINGS",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

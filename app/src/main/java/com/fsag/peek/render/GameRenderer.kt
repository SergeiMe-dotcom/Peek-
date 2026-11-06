package com.fsag.peek.render

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.fsag.peek.model.BiomeTheme
import com.fsag.peek.model.CubeActivityState
import com.fsag.peek.model.CubeEyeExpression
import com.fsag.peek.model.HazardData
import com.fsag.peek.model.HazardType
import com.fsag.peek.model.LevelData
import com.fsag.peek.model.ParticleEffect
import com.fsag.peek.model.PlatformData
import com.fsag.peek.model.PlatformType
import com.fsag.peek.model.PlayerState
import com.fsag.peek.model.RopeSwingData
import com.fsag.peek.model.StarCrystalData
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2.5D / 3D-Styled Graphics Engine for "PEEK".
 * Renders:
 * - Multi-layered parallax backgrounds with distant 3D monoliths, volumetric god-rays, and depth-of-field bokeh
 * - 3D-extruded perspective platforms (Front, Top, and Side 3D faces with directional lighting & neon trim)
 * - Pendulum ropes with segmented tension nodes and interactive grab-radius aura
 * - Spinning energy saws, laser beams, and crystalline spike strips
 * - Rotating 3D Star Crystals and Goal Portal vortex
 * - "The Cube": 3D beveled cube character with squash-and-stretch physics and expressive animated eyes
 */
class GameRenderer {

    private var cameraX: Float = 0f
    private var cameraY: Float = 0f

    fun resetCamera(player: PlayerState) {
        cameraX = (player.centerX - 360f).coerceAtLeast(0f)
        cameraY = (player.centerY - 320f).coerceIn(-120f, 180f)
    }

    fun DrawScope.renderFrame(
        level: LevelData,
        player: PlayerState,
        particles: List<ParticleEffect>,
        timeSeconds: Float
    ) {
        // Virtual viewport height is normalized to 640 world units; scale uniformly to fit wide landscape screen
        val virtualHeight = 640f
        val scale = size.height / virtualHeight
        val virtualWidth = size.width / scale

        // Smooth camera follow with look-ahead in movement direction
        val lookAheadX = if (player.facingRight) 110f else -70f
        val targetCamX = (player.centerX + lookAheadX - virtualWidth * 0.42f)
            .coerceIn(0f, (level.goalX + 420f - virtualWidth).coerceAtLeast(0f))
        val targetCamY = (player.centerY - virtualHeight * 0.54f).coerceIn(-140f, 140f)

        cameraX += (targetCamX - cameraX) * 0.12f
        cameraY += (targetCamY - cameraY) * 0.10f

        val biome = level.biome

        // 1. Sky Gradient & Volumetric Light Beams
        drawSkyAndGodRays(biome, timeSeconds)

        // 2. Parallax Distant Monoliths & Depth-of-Field Bokeh (Screen Space scaled)
        withTransform({
            scale(scale, scale, Offset.Zero)
        }) {
            drawParallaxLayers(biome, cameraX, cameraY, virtualWidth, virtualHeight, timeSeconds)

            // 3. World Space Transformation
            withTransform({
                translate(left = -cameraX, top = -cameraY)
            }) {
                // Render 3D Extruded Platforms
                for (plat in level.platforms) {
                    val px = plat.currentX(timeSeconds)
                    if (px + plat.width >= cameraX - 120f && px <= cameraX + virtualWidth + 120f) {
                        draw3DPlatform(plat, biome, timeSeconds)
                    }
                }

                // Render Swinging Pendulum Ropes
                for (rope in level.ropes) {
                    if (rope.anchorX >= cameraX - 260f && rope.anchorX <= cameraX + virtualWidth + 260f) {
                        drawPendulumRope(rope, player, biome, timeSeconds)
                    }
                }

                // Render Collectible 3D Star Crystals
                for (star in level.stars) {
                    if (star.id !in player.collectedStarIds) {
                        drawStarCrystal(star, timeSeconds)
                    }
                }

                // Render Hazards
                for (hazard in level.hazards) {
                    drawHazard(hazard, timeSeconds)
                }

                // Render 3D Goal Portal
                drawGoalPortal(level.goalX, level.goalY, biome, timeSeconds)

                // Render Particles
                for (p in particles) {
                    drawRoundRect(
                        color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                        topLeft = Offset(p.x - p.size * 0.5f, p.y - p.size * 0.5f),
                        size = Size(p.size, p.size),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }

                // Render "The Cube" (Player)
                drawTheCube(player, biome, timeSeconds)
            }

            // 4. Foreground Depth-of-Field Atmospheric Fog & Bokeh
            drawForegroundAtmosphere(biome, cameraX, virtualWidth, virtualHeight, timeSeconds)
        }
    }

    private fun DrawScope.drawSkyAndGodRays(biome: BiomeTheme, timeSeconds: Float) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(biome.skyTopColor, biome.skyBottomColor)
            ),
            size = size
        )

        // Volumetric diagonal light beams (God Rays)
        for (i in 0..3) {
            val drift = sin(timeSeconds * 0.35f + i * 1.4f) * 45f
            val rayX = size.width * (0.18f + i * 0.24f) + drift
            val rayPath = Path().apply {
                moveTo(rayX, 0f)
                lineTo(rayX + size.width * 0.11f, 0f)
                lineTo(rayX - size.width * 0.08f, size.height)
                lineTo(rayX - size.width * 0.22f, size.height)
                close()
            }
            drawPath(
                path = rayPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        biome.trimAccentColor.copy(alpha = 0.09f),
                        biome.fogColor.copy(alpha = 0.02f),
                        Color.Transparent
                    )
                )
            )
        }
    }

    private fun DrawScope.drawParallaxLayers(
        biome: BiomeTheme,
        camX: Float,
        camY: Float,
        vw: Float,
        vh: Float,
        timeSeconds: Float
    ) {
        // Far background geometric monolith silhouettes (Parallax factor 0.20)
        val farOffset = -(camX * 0.20f) % 320f
        for (i in -1..((vw / 320f).toInt() + 2)) {
            val bx = i * 320f + farOffset
            val h = 260f + ((i * 73) and 127)
            val topY = vh - h - (camY * 0.1f)
            // Front face
            drawRect(
                color = biome.monolithColor.copy(alpha = 0.45f),
                topLeft = Offset(bx, topY),
                size = Size(130f, h + 120f)
            )
            // 3D side extrusion
            val sidePath = Path().apply {
                moveTo(bx + 130f, topY)
                lineTo(bx + 162f, topY - 22f)
                lineTo(bx + 162f, topY + h + 120f)
                lineTo(bx + 130f, topY + h + 120f)
                close()
            }
            drawPath(sidePath, color = biome.platformSideColor.copy(alpha = 0.45f))
            // Glowing ancient glyph window on monolith
            drawRoundRect(
                color = biome.trimAccentColor.copy(alpha = 0.16f + 0.06f * sin(timeSeconds * 1.5f + i)),
                topLeft = Offset(bx + 54f, topY + 48f),
                size = Size(22f, 64f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }

        // Midground floating ruins (Parallax factor 0.42)
        val midOffset = -(camX * 0.42f) % 440f
        for (i in -1..((vw / 440f).toInt() + 2)) {
            val mx = i * 440f + midOffset + 90f
            val bob = sin(timeSeconds * 0.9f + i * 1.7f) * 12f
            val my = 180f + ((i * 53) and 95) + bob - camY * 0.2f
            drawRect(
                color = biome.monolithColor.copy(alpha = 0.65f),
                topLeft = Offset(mx, my),
                size = Size(85f, 38f)
            )
            drawLine(
                color = biome.trimAccentColor.copy(alpha = 0.35f),
                start = Offset(mx, my),
                end = Offset(mx + 85f, my),
                strokeWidth = 2.5f
            )
        }
    }

    private fun DrawScope.draw3DPlatform(
        plat: PlatformData,
        biome: BiomeTheme,
        timeSeconds: Float
    ) {
        val px = plat.currentX(timeSeconds)
        val py = plat.currentY(timeSeconds)
        val w = plat.width
        val h = plat.height
        val dx = plat.depth3D * 0.65f
        val dy = -plat.depth3D * 0.50f

        val accentColor = when (plat.type) {
            PlatformType.BOUNCY -> Color(0xFFFFB800)
            PlatformType.MOVING_HORIZONTAL, PlatformType.MOVING_VERTICAL -> Color(0xFF00FFA3)
            PlatformType.WALL_CLIMBABLE -> Color(0xFF38BDF8)
            PlatformType.STATIC -> biome.trimAccentColor
        }

        // 1. 3D Top Face (Perspective Extrusion)
        val topFace = Path().apply {
            moveTo(px, py)
            lineTo(px + dx, py + dy)
            lineTo(px + w + dx, py + dy)
            lineTo(px + w, py)
            close()
        }
        drawPath(
            path = topFace,
            brush = Brush.verticalGradient(
                colors = listOf(biome.platformTopColor, biome.platformTopColor.copy(alpha = 0.88f)),
                startY = py + dy,
                endY = py
            )
        )

        // 2. 3D Right Side Face (Perspective Extrusion)
        val rightFace = Path().apply {
            moveTo(px + w, py)
            lineTo(px + w + dx, py + dy)
            lineTo(px + w + dx, py + h + dy)
            lineTo(px + w, py + h)
            close()
        }
        drawPath(path = rightFace, color = biome.platformSideColor)

        // 3. Front Face
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(biome.platformFrontColor, biome.platformSideColor),
                startY = py,
                endY = py + h
            ),
            topLeft = Offset(px, py),
            size = Size(w, h),
            cornerRadius = CornerRadius(5f, 5f)
        )

        // 4. Specular Top Edge Trim (Bioluminescent Neon Strip)
        drawLine(
            color = accentColor,
            start = Offset(px + 2f, py + 1.5f),
            end = Offset(px + w - 2f, py + 1.5f),
            strokeWidth = if (plat.type == PlatformType.BOUNCY) 5f else 3.2f,
            cap = StrokeCap.Round
        )

        // 5. Special details for Bouncy, Moving, or Wall-Climbable platforms
        if (plat.type == PlatformType.BOUNCY) {
            val pulse = 0.5f + 0.5f * sin(timeSeconds * 6f)
            drawRoundRect(
                color = Color(0xFFFFB800).copy(alpha = 0.25f + 0.25f * pulse),
                topLeft = Offset(px + 12f, py - 6f),
                size = Size(w - 24f, 6f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        } else if (plat.type == PlatformType.WALL_CLIMBABLE) {
            // Grip chevrons along left and right sides of the climbable monolith
            var gy = py + 24f
            while (gy < py + h - 20f) {
                drawLine(
                    color = accentColor.copy(alpha = 0.65f),
                    start = Offset(px + 3f, gy),
                    end = Offset(px + 12f, gy),
                    strokeWidth = 3f
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.65f),
                    start = Offset(px + w - 12f, gy),
                    end = Offset(px + w - 3f, gy),
                    strokeWidth = 3f
                )
                gy += 26f
            }
        }
    }

    private fun DrawScope.drawPendulumRope(
        rope: RopeSwingData,
        player: PlayerState,
        biome: BiomeTheme,
        timeSeconds: Float
    ) {
        val isAttached = player.attachedRopeId == rope.id
        val isTargetable = player.nearestRopeId == rope.id && player.canGrabNearestRope

        val angle = if (isAttached) {
            player.ropeAngleRad
        } else {
            rope.initialAngleRad * cos(timeSeconds * 2.1f + rope.id)
        }

        val currentLen = if (isAttached) player.ropeGrabLength else rope.length
        val tipX = rope.anchorX + sin(angle) * currentLen
        val tipY = rope.anchorY + cos(angle) * currentLen

        // Grab-Range Halo when player is close enough to grab
        if (isTargetable && !isAttached) {
            val pulseRadius = 28f + 6f * sin(timeSeconds * 8f)
            drawCircle(
                color = Color(0xFFFFB800).copy(alpha = 0.28f),
                radius = pulseRadius,
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = Color(0xFFFFB800),
                radius = pulseRadius,
                center = Offset(tipX, tipY),
                style = Stroke(width = 2.5f)
            )
        }

        // Anchor Mount in 3D space
        drawCircle(
            color = biome.platformTopColor,
            radius = 14f,
            center = Offset(rope.anchorX, rope.anchorY)
        )
        drawCircle(
            color = biome.trimAccentColor,
            radius = 7f,
            center = Offset(rope.anchorX, rope.anchorY)
        )

        // Glowing Tether Line + Segmented Energy Nodes
        val ropeColor = if (isAttached) Color(0xFFFFB800) else biome.trimAccentColor
        drawLine(
            color = ropeColor.copy(alpha = 0.32f),
            start = Offset(rope.anchorX, rope.anchorY),
            end = Offset(tipX, tipY),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = ropeColor,
            start = Offset(rope.anchorX, rope.anchorY),
            end = Offset(tipX, tipY),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        val segments = 6
        for (s in 1 until segments) {
            val frac = s.toFloat() / segments
            val nx = rope.anchorX + (tipX - rope.anchorX) * frac
            val ny = rope.anchorY + (tipY - rope.anchorY) * frac
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = 3.2f,
                center = Offset(nx, ny)
            )
        }

        // Glowing Handle Orb at tip
        drawCircle(
            color = ropeColor,
            radius = 11f,
            center = Offset(tipX, tipY)
        )
        drawCircle(
            color = Color.White,
            radius = 5f,
            center = Offset(tipX, tipY)
        )
    }

    private fun DrawScope.drawStarCrystal(star: StarCrystalData, timeSeconds: Float) {
        val bobY = star.y + sin(timeSeconds * 3.6f + star.id) * 7f
        val rot = (timeSeconds * 75f + star.id * 30f) % 360f

        // Outer aura
        drawCircle(
            color = Color(0xFFFFB800).copy(alpha = 0.22f),
            radius = 24f,
            center = Offset(star.x, bobY)
        )

        rotate(degrees = rot, pivot = Offset(star.x, bobY)) {
            val path = Path().apply {
                moveTo(star.x, bobY - 16f)
                lineTo(star.x + 12f, bobY)
                lineTo(star.x, bobY + 16f)
                lineTo(star.x - 12f, bobY)
                close()
            }
            drawPath(path, color = Color(0xFFFFB800))
            drawPath(path, color = Color.White, style = Stroke(width = 2f))
        }
    }

    private fun DrawScope.drawHazard(hazard: HazardData, timeSeconds: Float) {
        val hx = hazard.currentX(timeSeconds)
        val hy = hazard.currentY(timeSeconds)

        when (hazard.type) {
            HazardType.SPIKE_STRIP -> {
                val spikeWidth = 18f
                val count = (hazard.width / spikeWidth).toInt().coerceAtLeast(1)
                for (i in 0 until count) {
                    val sx = hx + i * spikeWidth
                    val spikePath = Path().apply {
                        moveTo(sx, hy + hazard.height)
                        lineTo(sx + spikeWidth * 0.5f, hy)
                        lineTo(sx + spikeWidth, hy + hazard.height)
                        close()
                    }
                    drawPath(spikePath, color = Color(0xFFFF3366))
                    drawPath(spikePath, color = Color(0xFFFF99B3), style = Stroke(width = 1.5f))
                }
            }
            HazardType.ENERGY_SAW -> {
                val cx = hx + hazard.width * 0.5f
                val cy = hy + hazard.height * 0.5f
                val radius = hazard.width * 0.5f
                drawCircle(
                    color = Color(0xFFFF3366).copy(alpha = 0.25f),
                    radius = radius * 1.35f,
                    center = Offset(cx, cy)
                )
                rotate(degrees = (timeSeconds * 320f) % 360f, pivot = Offset(cx, cy)) {
                    for (tooth in 0 until 8) {
                        val a = tooth * 45f
                        rotate(degrees = a, pivot = Offset(cx, cy)) {
                            val blade = Path().apply {
                                moveTo(cx - 6f, cy)
                                lineTo(cx, cy - radius * 1.15f)
                                lineTo(cx + 6f, cy)
                                close()
                            }
                            drawPath(blade, color = Color(0xFFFF3366))
                        }
                    }
                    drawCircle(color = Color(0xFF1E1B2E), radius = radius * 0.55f, center = Offset(cx, cy))
                    drawCircle(color = Color.White, radius = radius * 0.22f, center = Offset(cx, cy))
                }
            }
            HazardType.LASER_PULSE -> {
                val cx = hx + hazard.width * 0.5f
                val pulse = 0.65f + 0.35f * sin(timeSeconds * 12f)
                drawLine(
                    color = Color(0xFFFF3366).copy(alpha = 0.35f * pulse),
                    start = Offset(cx, hy),
                    end = Offset(cx, hy + hazard.height),
                    strokeWidth = hazard.width * 0.85f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFFFF99B3),
                    start = Offset(cx, hy),
                    end = Offset(cx, hy + hazard.height),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    private fun DrawScope.drawGoalPortal(
        goalX: Float,
        goalY: Float,
        biome: BiomeTheme,
        timeSeconds: Float
    ) {
        val pulse = 1f + 0.08f * sin(timeSeconds * 4f)
        // Outer energy vortex
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00FFA3).copy(alpha = 0.55f),
                    biome.trimAccentColor.copy(alpha = 0.20f),
                    Color.Transparent
                ),
                center = Offset(goalX, goalY),
                radius = 68f * pulse
            ),
            radius = 68f * pulse,
            center = Offset(goalX, goalY)
        )

        // Rotating 3D diamond portal frames
        for (ring in 0..1) {
            val dir = if (ring == 0) 1f else -1f
            val r = if (ring == 0) 42f else 28f
            rotate(degrees = (timeSeconds * 65f * dir) % 360f, pivot = Offset(goalX, goalY)) {
                drawRoundRect(
                    color = if (ring == 0) Color(0xFF00FFA3) else Color.White,
                    topLeft = Offset(goalX - r, goalY - r),
                    size = Size(r * 2f, r * 2f),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 3.5f)
                )
            }
        }
    }

    /**
     * Renders "The Cube":
     * - 3D beveled cube with Top, Side, and Front shaded faces
     * - Squash-and-stretch deformation around its bottom/center anchor
     * - Expressive animated eyes reacting to peeking, jumping, swinging, clinging, and hazards
     */
    private fun DrawScope.drawTheCube(
        player: PlayerState,
        biome: BiomeTheme,
        timeSeconds: Float
    ) {
        // Flash alpha during invulnerability frames
        if (player.invulnerableTimer > 0f && ((timeSeconds * 16f).toInt() % 2 == 0)) {
            return
        }

        val cx = player.centerX
        val bottomY = player.y + player.size

        withTransform({
            // Squash and stretch anchored near bottom-center when grounded, or center when airborne
            val pivotY = if (player.activityState == CubeActivityState.GROUNDED) bottomY else player.centerY
            scale(player.scaleX, player.scaleY, pivot = Offset(cx, pivotY))
            rotate(player.tiltAngleDeg, pivot = Offset(cx, player.centerY))
        }) {
            val s = player.size
            val x = player.x
            val y = player.y
            val depthDx = 11f
            val depthDy = -9f

            // Soft ground/ambient glow under The Cube
            drawCircle(
                color = Color(0xFF00F0FF).copy(alpha = 0.24f),
                radius = s * 0.85f,
                center = Offset(cx, y + s * 0.55f)
            )

            // 1. 3D Top Face of The Cube
            val topPath = Path().apply {
                moveTo(x + 2f, y)
                lineTo(x + depthDx, y + depthDy)
                lineTo(x + s + depthDx - 2f, y + depthDy)
                lineTo(x + s - 2f, y)
                close()
            }
            drawPath(topPath, color = Color(0xFF5CE1E6))

            // 2. 3D Side Face of The Cube
            val sidePath = Path().apply {
                moveTo(x + s, y + 2f)
                lineTo(x + s + depthDx, y + depthDy + 2f)
                lineTo(x + s + depthDx, y + s + depthDy - 2f)
                lineTo(x + s, y + s - 2f)
                close()
            }
            drawPath(sidePath, color = Color(0xFF0A4D68))

            // 3. Outer Beveled Body of The Cube (Front Face)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF14284B), Color(0xFF0B162C)),
                    start = Offset(x, y),
                    end = Offset(x + s, y + s)
                ),
                topLeft = Offset(x, y),
                size = Size(s, s),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // 4. Glowing Neon Edge Frame (Cyan + Amber bevel)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00F0FF), Color(0xFFFFB800)),
                    start = Offset(x, y),
                    end = Offset(x + s, y + s)
                ),
                topLeft = Offset(x + 1.5f, y + 1.5f),
                size = Size(s - 3f, s - 3f),
                cornerRadius = CornerRadius(9f, 9f),
                style = Stroke(width = 3.2f)
            )

            // 5. Inner Dark Visor Faceplate where the eyes sit
            val visorMargin = 7f
            drawRoundRect(
                color = Color(0xFF060B16),
                topLeft = Offset(x + visorMargin, y + visorMargin),
                size = Size(s - visorMargin * 2f, s - visorMargin * 2f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // 6. Expressive Animated Eyes
            val lookShiftX = player.eyeLookX * 4.5f
            val lookShiftY = player.eyeLookY * 3.2f
            val leftEyeX = cx - 9f + lookShiftX
            val rightEyeX = cx + 9f + lookShiftX
            val eyeY = y + s * 0.48f + lookShiftY

            if (player.isBlinking) {
                // Blink line
                drawLine(
                    color = Color(0xFF00F0FF),
                    start = Offset(leftEyeX - 5f, eyeY),
                    end = Offset(leftEyeX + 5f, eyeY),
                    strokeWidth = 2.6f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF00F0FF),
                    start = Offset(rightEyeX - 5f, eyeY),
                    end = Offset(rightEyeX + 5f, eyeY),
                    strokeWidth = 2.6f,
                    cap = StrokeCap.Round
                )
            } else {
                when (player.eyeExpression) {
                    CubeEyeExpression.WIDE_EXCITED -> {
                        drawOval(
                            color = Color.White,
                            topLeft = Offset(leftEyeX - 5.5f, eyeY - 7f),
                            size = Size(11f, 14f)
                        )
                        drawOval(
                            color = Color.White,
                            topLeft = Offset(rightEyeX - 5.5f, eyeY - 7f),
                            size = Size(11f, 14f)
                        )
                        // Cyan pupils
                        drawCircle(
                            color = Color(0xFF00F0FF),
                            radius = 2.5f,
                            center = Offset(leftEyeX + player.eyeLookX * 1.5f, eyeY)
                        )
                        drawCircle(
                            color = Color(0xFF00F0FF),
                            radius = 2.5f,
                            center = Offset(rightEyeX + player.eyeLookX * 1.5f, eyeY)
                        )
                    }
                    CubeEyeExpression.FOCUSED_SQUINT, CubeEyeExpression.PEEKING -> {
                        // Expressive narrowed peeking eyes
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(leftEyeX - 5.5f, eyeY - 3.5f),
                            size = Size(11f, 7f),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(rightEyeX - 5.5f, eyeY - 3.5f),
                            size = Size(11f, 7f),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                    CubeEyeExpression.STARTLED -> {
                        drawCircle(color = Color(0xFFFFB800), radius = 6f, center = Offset(leftEyeX, eyeY))
                        drawCircle(color = Color(0xFFFFB800), radius = 6f, center = Offset(rightEyeX, eyeY))
                        drawCircle(color = Color(0xFF060B16), radius = 2.2f, center = Offset(leftEyeX, eyeY))
                        drawCircle(color = Color(0xFF060B16), radius = 2.2f, center = Offset(rightEyeX, eyeY))
                    }
                    else -> {
                        // Crisp glowing eyes with subtle highlight
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(leftEyeX - 5f, eyeY - 5.5f),
                            size = Size(10f, 11f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(rightEyeX - 5f, eyeY - 5.5f),
                            size = Size(10f, 11f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawCircle(
                            color = Color(0xFF00F0FF),
                            radius = 2.2f,
                            center = Offset(leftEyeX + player.eyeLookX * 1.4f, eyeY + player.eyeLookY * 1.2f)
                        )
                        drawCircle(
                            color = Color(0xFF00F0FF),
                            radius = 2.2f,
                            center = Offset(rightEyeX + player.eyeLookX * 1.4f, eyeY + player.eyeLookY * 1.2f)
                        )
                    }
                }
            }
        }
    }

    private fun DrawScope.drawForegroundAtmosphere(
        biome: BiomeTheme,
        camX: Float,
        vw: Float,
        vh: Float,
        timeSeconds: Float
    ) {
        // Out-of-focus foreground bokeh particles (Parallax 1.35x for depth of field)
        val fgOffset = -(camX * 1.35f) % 280f
        for (i in 0..6) {
            val bx = (i * 280f + fgOffset + vw) % (vw + 280f) - 140f
            val by = (vh * 0.25f) + ((i * 97) % 320) + sin(timeSeconds * 0.8f + i) * 24f
            val radius = 16f + (i % 3) * 10f
            drawCircle(
                color = biome.trimAccentColor.copy(alpha = 0.08f),
                radius = radius,
                center = Offset(bx, by)
            )
        }
    }
}

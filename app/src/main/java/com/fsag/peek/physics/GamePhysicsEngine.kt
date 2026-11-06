package com.fsag.peek.physics

import androidx.compose.ui.graphics.Color
import com.fsag.peek.audio.SoundEngine
import com.fsag.peek.model.CubeActivityState
import com.fsag.peek.model.CubeEyeExpression
import com.fsag.peek.model.LevelData
import com.fsag.peek.model.ParticleEffect
import com.fsag.peek.model.PlatformType
import com.fsag.peek.model.PlayerState
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * Real-time 2.5D Physics Engine for "PEEK".
 * Handles:
 * - Horizontal momentum, air control, and surface friction
 * - Variable jump heights (hold Jump for sustained lift), Coyote Time, and Jump Buffering
 * - Moving platform carry-velocity (horizontal & vertical oscillation)
 * - Surface wall-clinging & directional wall-jumps
 * - Pendulum physics-based rope swinging (angular acceleration, pumping, climb up/down, tangential launch boost)
 * - Squash-and-stretch 3D spring deformation & expressive eye tracking
 * - Particle emitter simulation
 */
class GamePhysicsEngine(
    private val soundEngine: SoundEngine
) {
    val particles = mutableListOf<ParticleEffect>()

    fun resetPlayerForLevel(player: PlayerState, level: LevelData) {
        player.x = level.startX
        player.y = level.startY
        player.vx = 0f
        player.vy = 0f
        player.scaleX = 1f
        player.scaleY = 1f
        player.tiltAngleDeg = 0f
        player.facingRight = true
        player.activityState = CubeActivityState.GROUNDED
        player.eyeExpression = CubeEyeExpression.PEEKING
        player.coyoteTimeLeft = 0f
        player.jumpBufferLeft = 0f
        player.isHoldingJump = false
        player.jumpHoldTime = 0f
        player.wallClingSide = 0
        player.groundedPlatformId = -1
        player.attachedRopeId = -1
        player.ropeAngleRad = 0f
        player.ropeAngularVel = 0f
        player.ropeCooldown = 0f
        player.nearestRopeId = -1
        player.canGrabNearestRope = false
        player.lives = 3
        player.maxLives = 3
        player.invulnerableTimer = 0f
        player.score = 0
        player.collectedStarIds.clear()
        player.elapsedSeconds = 0f
        player.checkpointX = level.startX
        player.checkpointY = level.startY
        particles.clear()
    }

    /**
     * Advances the physics simulation by [dt] seconds (clamped for stability).
     */
    fun update(
        dtRaw: Float,
        player: PlayerState,
        level: LevelData,
        moveInputX: Float, // -1f..+1f
        moveInputY: Float, // -1f..+1f (up/down on rope)
        jumpPressedThisFrame: Boolean,
        jumpHeld: Boolean,
        grabHeld: Boolean
    ) {
        if (player.activityState == CubeActivityState.VICTORY ||
            player.activityState == CubeActivityState.SHATTERED
        ) {
            updateParticles(dtRaw.coerceIn(0.001f, 0.033f))
            return
        }

        val dt = dtRaw.coerceIn(0.001f, 0.033f)
        player.elapsedSeconds += dt
        val t = player.elapsedSeconds

        if (player.invulnerableTimer > 0f) {
            player.invulnerableTimer = (player.invulnerableTimer - dt).coerceAtLeast(0f)
        }
        if (player.ropeCooldown > 0f) {
            player.ropeCooldown = (player.ropeCooldown - dt).coerceAtLeast(0f)
        }
        if (jumpPressedThisFrame) {
            player.jumpBufferLeft = JUMP_BUFFER_SECONDS
        } else if (player.jumpBufferLeft > 0f) {
            player.jumpBufferLeft = (player.jumpBufferLeft - dt).coerceAtLeast(0f)
        }

        // Update facing direction
        if (moveInputX > 0.15f) player.facingRight = true
        else if (moveInputX < -0.15f) player.facingRight = false

        // 1. Check proximity to swinging ropes
        updateNearestRopeDetection(player, level)

        // 2. Check if player initiates a rope grab
        if (player.attachedRopeId == -1 && grabHeld && player.canGrabNearestRope && player.ropeCooldown <= 0f) {
            val rope = level.ropes.firstOrNull { it.id == player.nearestRopeId }
            if (rope != null) {
                attachToRope(player, rope)
            }
        }

        // 3. Handle either Pendulum Rope Swing OR Free/Platform/Wall Physics
        if (player.attachedRopeId != -1) {
            val rope = level.ropes.firstOrNull { it.id == player.attachedRopeId }
            if (rope == null || !grabHeld || jumpPressedThisFrame) {
                releaseFromRope(player, jumpBoost = jumpPressedThisFrame, level = level)
            } else {
                updateRopeSwingPhysics(dt, player, rope, moveInputX, moveInputY)
            }
        } else {
            updateFreeMovementAndCollisions(
                dt = dt,
                timeSeconds = t,
                player = player,
                level = level,
                moveInputX = moveInputX,
                jumpHeld = jumpHeld
            )
        }

        // 4. Collect Star Crystals
        checkStarCollection(player, level)

        // 5. Check Hazards & Death Pit
        checkHazardsAndBounds(t, player, level)

        // 6. Check Goal Portal
        checkGoalPortal(player, level)

        // 7. Update Squash-and-Stretch spring, tilt, and Expressive Eye Animations
        updateSquashStretchAndEyes(dt, player, level, moveInputX)

        // 8. Update visual particles
        updateParticles(dt)
    }

    private fun updateNearestRopeDetection(player: PlayerState, level: LevelData) {
        var bestId = -1
        var bestDist = Float.MAX_VALUE
        var canGrab = false
        for (rope in level.ropes) {
            // Compute idle or current rope tip
            val tipX = rope.anchorX + sin(rope.initialAngleRad * cos(player.elapsedSeconds * 2.1f)) * rope.length
            val tipY = rope.anchorY + cos(rope.initialAngleRad * cos(player.elapsedSeconds * 2.1f)) * rope.length
            val distToTip = hypot(player.centerX - tipX, player.centerY - tipY)
            val distToAnchor = hypot(player.centerX - rope.anchorX, player.centerY - rope.anchorY)
            // Player can grab anywhere along the lower 75% of the rope or near the tip
            val effectiveDist = minOf(distToTip, abs(distToAnchor - rope.length * 0.8f))
            if (effectiveDist < bestDist) {
                bestDist = effectiveDist
                bestId = rope.id
                canGrab = (distToAnchor <= rope.length + rope.grabRadius) &&
                        (effectiveDist <= rope.grabRadius * 1.35f) &&
                        (player.centerY >= rope.anchorY - 20f)
            }
        }
        player.nearestRopeId = bestId
        player.canGrabNearestRope = canGrab && player.ropeCooldown <= 0f
    }

    private fun attachToRope(player: PlayerState, rope: com.fsag.peek.model.RopeSwingData) {
        player.attachedRopeId = rope.id
        player.activityState = CubeActivityState.SWINGING
        player.groundedPlatformId = -1
        player.wallClingSide = 0

        val dx = player.centerX - rope.anchorX
        val dy = (player.centerY - rope.anchorY).coerceAtLeast(45f)
        player.ropeAngleRad = atan2(dx, dy).coerceIn(-1.35f, 1.35f)
        player.ropeGrabLength = hypot(dx, dy).coerceIn(rope.length * 0.48f, rope.length)

        // Convert linear velocity into tangential angular velocity
        val tangentX = cos(player.ropeAngleRad)
        val tangentY = -sin(player.ropeAngleRad)
        val tangentialSpeed = player.vx * tangentX + player.vy * tangentY
        player.ropeAngularVel = (tangentialSpeed / player.ropeGrabLength).coerceIn(-4.8f, 4.8f)

        // Minimum initial swing impulse so grabbing from standstill still feels lively
        if (abs(player.ropeAngularVel) < 0.85f) {
            val dir = if (player.facingRight) 1f else -1f
            player.ropeAngularVel += dir * 1.35f
        }

        // Squash-stretch feedback on grab
        player.scaleX = 0.82f
        player.scaleY = 1.24f
        soundEngine.playRopeGrab()
        spawnBurstParticles(player.centerX, player.centerY, Color(0xFF00F0FF), count = 12)
    }

    private fun updateRopeSwingPhysics(
        dt: Float,
        player: PlayerState,
        rope: com.fsag.peek.model.RopeSwingData,
        moveInputX: Float,
        moveInputY: Float
    ) {
        // Pendulum angular acceleration: alpha = -(g / L) * sin(theta) + player pump torque
        val gravityTorque = -(ROPE_GRAVITY / player.ropeGrabLength) * sin(player.ropeAngleRad)
        val pumpTorque = moveInputX * ROPE_PUMP_ACCEL * cos(player.ropeAngleRad * 0.6f)
        player.ropeAngularVel += (gravityTorque + pumpTorque) * dt
        player.ropeAngularVel *= (1f - 0.12f * dt) // very light air damping
        player.ropeAngularVel = player.ropeAngularVel.coerceIn(-5.6f, 5.6f)
        player.ropeAngleRad += player.ropeAngularVel * dt

        // Soft angle limits
        if (player.ropeAngleRad > 1.42f) {
            player.ropeAngleRad = 1.42f
            player.ropeAngularVel = -abs(player.ropeAngularVel) * 0.5f
        } else if (player.ropeAngleRad < -1.42f) {
            player.ropeAngleRad = -1.42f
            player.ropeAngularVel = abs(player.ropeAngularVel) * 0.5f
        }

        // Climb up / slide down rope with vertical input
        if (abs(moveInputY) > 0.25f) {
            player.ropeGrabLength = (player.ropeGrabLength + moveInputY * 140f * dt)
                .coerceIn(rope.length * 0.42f, rope.length * 1.05f)
        }

        val newCenterX = rope.anchorX + sin(player.ropeAngleRad) * player.ropeGrabLength
        val newCenterY = rope.anchorY + cos(player.ropeAngleRad) * player.ropeGrabLength
        player.x = newCenterX - player.size * 0.5f
        player.y = newCenterY - player.size * 0.5f

        // Instantaneous tangential velocity
        val tangentialSpeed = player.ropeAngularVel * player.ropeGrabLength
        player.vx = cos(player.ropeAngleRad) * tangentialSpeed
        player.vy = -sin(player.ropeAngleRad) * tangentialSpeed
    }

    private fun releaseFromRope(player: PlayerState, jumpBoost: Boolean, level: LevelData) {
        player.attachedRopeId = -1
        player.ropeCooldown = 0.28f
        player.activityState = CubeActivityState.JUMPING

        // Apply launch boost in tangential direction + upward kick
        player.vx = (player.vx * 1.22f).coerceIn(-680f, 680f)
        player.vy = if (jumpBoost) {
            (player.vy - 360f).coerceIn(-760f, -340f)
        } else {
            (player.vy - 180f).coerceIn(-680f, 200f)
        }

        player.scaleX = 0.76f
        player.scaleY = 1.32f
        soundEngine.playRopeReleaseBoost()
        spawnBurstParticles(player.centerX, player.centerY, level.biome.trimAccentColor, count = 14)
    }

    private fun updateFreeMovementAndCollisions(
        dt: Float,
        timeSeconds: Float,
        player: PlayerState,
        level: LevelData,
        moveInputX: Float,
        jumpHeld: Boolean
    ) {
        val wasGrounded = player.activityState == CubeActivityState.GROUNDED

        // Carry velocity from moving platform if grounded on one
        var platformVx = 0f
        var platformVy = 0f
        if (wasGrounded && player.groundedPlatformId != -1) {
            val plat = level.platforms.firstOrNull { it.id == player.groundedPlatformId }
            if (plat != null) {
                platformVx = plat.velocityX(timeSeconds)
                platformVy = plat.velocityY(timeSeconds)
                player.x += platformVx * dt
                player.y += platformVy * dt
            }
        }

        // Horizontal acceleration & friction
        val targetVx = moveInputX * MAX_RUN_SPEED
        val accel = if (wasGrounded) GROUND_ACCEL else AIR_ACCEL
        if (abs(moveInputX) > 0.08f) {
            val diff = targetVx - player.vx
            val step = accel * dt
            player.vx += diff.coerceIn(-step, step)
        } else {
            val friction = if (wasGrounded) GROUND_FRICTION else AIR_DRAG
            val drop = friction * dt
            player.vx = when {
                player.vx > drop -> player.vx - drop
                player.vx < -drop -> player.vx + drop
                else -> 0f
            }
        }

        // Coyote time update
        if (wasGrounded) {
            player.coyoteTimeLeft = COYOTE_TIME_SECONDS
        } else if (player.coyoteTimeLeft > 0f) {
            player.coyoteTimeLeft = (player.coyoteTimeLeft - dt).coerceAtLeast(0f)
        }

        // Check Jump Execution (Normal Jump or Wall Jump)
        if (player.jumpBufferLeft > 0f) {
            if (player.coyoteTimeLeft > 0f) {
                // Standard / Coyote Jump
                player.vy = -JUMP_INITIAL_VELOCITY
                player.isHoldingJump = true
                player.jumpHoldTime = 0f
                player.coyoteTimeLeft = 0f
                player.jumpBufferLeft = 0f
                player.groundedPlatformId = -1
                player.activityState = CubeActivityState.JUMPING
                player.scaleX = 0.75f
                player.scaleY = 1.32f
                soundEngine.playJump(isWallJump = false)
                spawnBurstParticles(player.centerX, player.y + player.size, level.biome.trimAccentColor, count = 10)
            } else if (player.activityState == CubeActivityState.WALL_CLINGING && player.wallClingSide != 0) {
                // Wall Jump away from wall!
                val launchDir = -player.wallClingSide.toFloat()
                player.vx = launchDir * WALL_JUMP_HORIZONTAL_SPEED
                player.vy = -WALL_JUMP_VERTICAL_SPEED
                player.facingRight = launchDir > 0f
                player.isHoldingJump = true
                player.jumpHoldTime = 0f
                player.jumpBufferLeft = 0f
                player.wallClingSide = 0
                player.activityState = CubeActivityState.JUMPING
                player.scaleX = 0.74f
                player.scaleY = 1.34f
                soundEngine.playJump(isWallJump = true)
                spawnBurstParticles(player.centerX, player.centerY, Color(0xFFFFB800), count = 12)
            }
        }

        // Variable Jump Height sustain while holding Jump button
        if (player.isHoldingJump && jumpHeld && player.vy < 0f && player.jumpHoldTime < MAX_JUMP_HOLD_SECONDS) {
            player.vy -= JUMP_HOLD_BOOST_ACCEL * dt
            player.jumpHoldTime += dt
        } else if (!jumpHeld) {
            if (player.isHoldingJump && player.vy < -220f) {
                // Cut upward momentum slightly on early release for crisp short hops
                player.vy *= 0.68f
            }
            player.isHoldingJump = false
        }

        // Apply Gravity (reduced when wall-clinging)
        val gravity = if (player.activityState == CubeActivityState.WALL_CLINGING && player.vy > 0f) {
            WALL_SLIDE_GRAVITY
        } else {
            GRAVITY_ACCEL
        }
        player.vy = (player.vy + gravity * dt).coerceAtMost(MAX_FALL_SPEED)

        // Move Horizontally and Resolve X Collisions / Wall Cling
        val prevX = player.x
        player.x += player.vx * dt
        player.wallClingSide = 0

        for (plat in level.platforms) {
            val px = plat.currentX(timeSeconds)
            val py = plat.currentY(timeSeconds)
            if (rectsOverlap(player.x, player.y, player.size, player.size, px, py, plat.width, plat.height)) {
                // Only horizontal collision if vertically overlapping beyond step margin
                val verticalOverlap = minOf(player.y + player.size, py + plat.height) - maxOf(player.y, py)
                if (verticalOverlap > 10f) {
                    if (prevX + player.size <= px + 10f && player.vx >= 0f) {
                        player.x = px - player.size
                        player.vx = 0f
                        if (!wasGrounded && (plat.type == PlatformType.WALL_CLIMBABLE || plat.height >= 120f) && moveInputX > 0.1f) {
                            enterWallCling(player, side = 1)
                        }
                    } else if (prevX >= px + plat.width - 10f && player.vx <= 0f) {
                        player.x = px + plat.width
                        player.vx = 0f
                        if (!wasGrounded && (plat.type == PlatformType.WALL_CLIMBABLE || plat.height >= 120f) && moveInputX < -0.1f) {
                            enterWallCling(player, side = -1)
                        }
                    }
                }
            }
        }

        // Move Vertically and Resolve Y Collisions
        val prevY = player.y
        player.y += player.vy * dt
        var landedThisFrame = false
        var landedBounce = false

        for (plat in level.platforms) {
            val px = plat.currentX(timeSeconds)
            val py = plat.currentY(timeSeconds)
            if (rectsOverlap(player.x, player.y, player.size, player.size, px, py, plat.width, plat.height)) {
                val horizontalOverlap = minOf(player.x + player.size, px + plat.width) - maxOf(player.x, px)
                if (horizontalOverlap > 8f) {
                    if (prevY + player.size <= py + 18f && player.vy >= 0f) {
                        // Landing on top of platform
                        player.y = py - player.size
                        if (plat.type == PlatformType.BOUNCY) {
                            player.vy = -JUMP_INITIAL_VELOCITY * plat.bounceMultiplier
                            player.activityState = CubeActivityState.JUMPING
                            player.groundedPlatformId = -1
                            player.scaleX = 1.38f
                            player.scaleY = 0.64f
                            landedBounce = true
                            soundEngine.playJump(isWallJump = true)
                            spawnBurstParticles(player.centerX, py, Color(0xFFFFB800), count = 16)
                        } else {
                            if (!wasGrounded && player.vy > 180f) {
                                // Squash on landing impact
                                val impactRatio = (player.vy / MAX_FALL_SPEED).coerceIn(0.15f, 1f)
                                player.scaleX = 1f + 0.34f * impactRatio
                                player.scaleY = 1f - 0.28f * impactRatio
                                spawnBurstParticles(player.centerX, py, level.biome.trimAccentColor, count = 8)
                            }
                            player.vy = 0f
                            player.activityState = CubeActivityState.GROUNDED
                            player.groundedPlatformId = plat.id
                            landedThisFrame = true
                            // Update checkpoint on safe static platforms
                            if (plat.type == PlatformType.STATIC && plat.width >= 200f) {
                                player.checkpointX = px + plat.width * 0.35f
                                player.checkpointY = py - player.size - 4f
                            }
                        }
                    } else if (prevY >= py + plat.height - 16f && player.vy < 0f) {
                        // Head bump on underside of platform
                        player.y = py + plat.height
                        player.vy = 20f
                        player.isHoldingJump = false
                        player.scaleX = 1.22f
                        player.scaleY = 0.80f
                    }
                }
            }
        }

        if (!landedThisFrame && !landedBounce) {
            player.groundedPlatformId = -1
            if (player.wallClingSide == 0) {
                player.activityState = if (player.vy < 0f) CubeActivityState.JUMPING else CubeActivityState.FALLING
            }
        }
    }

    private fun enterWallCling(player: PlayerState, side: Int) {
        if (player.activityState != CubeActivityState.WALL_CLINGING) {
            soundEngine.playWallCling()
        }
        player.wallClingSide = side
        player.activityState = CubeActivityState.WALL_CLINGING
        if (player.vy > 95f) {
            player.vy = 95f
        }
    }

    private fun checkStarCollection(player: PlayerState, level: LevelData) {
        for (star in level.stars) {
            if (star.id !in player.collectedStarIds) {
                val dist = hypot(player.centerX - star.x, player.centerY - star.y)
                if (dist <= player.size * 0.78f + 18f) {
                    player.collectedStarIds.add(star.id)
                    player.score += star.bonusPoints
                    player.scaleX = 1.24f
                    player.scaleY = 1.24f
                    soundEngine.playStarCollect(player.collectedStarIds.size)
                    spawnBurstParticles(star.x, star.y, Color(0xFFFFB800), count = 20)
                }
            }
        }
    }

    private fun checkHazardsAndBounds(timeSeconds: Float, player: PlayerState, level: LevelData) {
        // Check pit fall
        if (player.y > level.deathPitY) {
            inflictHazardDamage(player, level)
            return
        }

        if (player.invulnerableTimer > 0f) return

        for (hazard in level.hazards) {
            val hx = hazard.currentX(timeSeconds)
            val hy = hazard.currentY(timeSeconds)
            if (rectsOverlap(
                    player.x + 6f,
                    player.y + 6f,
                    player.size - 12f,
                    player.size - 12f,
                    hx,
                    hy,
                    hazard.width,
                    hazard.height
                )
            ) {
                inflictHazardDamage(player, level)
                return
            }
        }
    }

    private fun inflictHazardDamage(player: PlayerState, level: LevelData) {
        soundEngine.playHazardHit()
        spawnBurstParticles(player.centerX, player.centerY, Color(0xFFFF3366), count = 26)
        player.attachedRopeId = -1
        player.lives -= 1

        if (player.lives <= 0) {
            player.activityState = CubeActivityState.SHATTERED
            player.vx = 0f
            player.vy = 0f
        } else {
            // Respawn at last safe checkpoint with brief invulnerability
            player.x = player.checkpointX
            player.y = player.checkpointY
            player.vx = 0f
            player.vy = -120f
            player.invulnerableTimer = 1.6f
            player.activityState = CubeActivityState.JUMPING
            player.eyeExpression = CubeEyeExpression.STARTLED
            player.scaleX = 0.72f
            player.scaleY = 1.32f
        }
    }

    private fun checkGoalPortal(player: PlayerState, level: LevelData) {
        val dist = hypot(player.centerX - level.goalX, player.centerY - level.goalY)
        if (dist <= 64f) {
            player.activityState = CubeActivityState.VICTORY
            player.attachedRopeId = -1
            player.eyeExpression = CubeEyeExpression.HAPPY_ARC
            // Time bonus calculation
            val timeRemaining = (level.targetTimeSeconds - player.elapsedSeconds).coerceAtLeast(0f)
            val timeBonus = (timeRemaining * 25f).toInt()
            val lifeBonus = player.lives * 250
            player.score += 500 + timeBonus + lifeBonus
            soundEngine.playLevelClear()
            spawnBurstParticles(level.goalX, level.goalY, Color(0xFF00FFA3), count = 34)
        }
    }

    private fun updateSquashStretchAndEyes(
        dt: Float,
        player: PlayerState,
        level: LevelData,
        moveInputX: Float
    ) {
        // Smoothly spring scaleX & scaleY back toward 1.0f
        val springRate = 11.5f * dt
        player.scaleX += (1.0f - player.scaleX) * springRate
        player.scaleY += (1.0f - player.scaleY) * springRate

        // Dynamic velocity tilt
        val targetTilt = when (player.activityState) {
            CubeActivityState.SWINGING -> (-player.ropeAngleRad * 32f).coerceIn(-28f, 28f)
            CubeActivityState.WALL_CLINGING -> player.wallClingSide * -8f
            else -> (player.vx / MAX_RUN_SPEED) * 11f
        }
        player.tiltAngleDeg += (targetTilt - player.tiltAngleDeg) * (10f * dt)

        // Blinking timer
        player.blinkTimer += dt
        if (!player.isBlinking && player.blinkTimer > 3.2f) {
            player.isBlinking = true
            player.blinkTimer = 0f
        } else if (player.isBlinking && player.blinkTimer > 0.13f) {
            player.isBlinking = false
            player.blinkTimer = Random.nextFloat() * 0.8f
        }

        // Expressive eye state based on gameplay events
        player.eyeExpression = when {
            player.invulnerableTimer > 0.8f -> CubeEyeExpression.STARTLED
            player.activityState == CubeActivityState.SWINGING -> CubeEyeExpression.WIDE_EXCITED
            player.activityState == CubeActivityState.WALL_CLINGING -> CubeEyeExpression.WALL_LOOK
            abs(player.vy) > 420f -> CubeEyeExpression.WIDE_EXCITED
            abs(player.vx) > MAX_RUN_SPEED * 0.82f -> CubeEyeExpression.FOCUSED_SQUINT
            abs(moveInputX) < 0.05f && player.activityState == CubeActivityState.GROUNDED -> {
                player.peekTimer += dt
                if (player.peekTimer > 1.0f) CubeEyeExpression.PEEKING else CubeEyeExpression.NEUTRAL
            }
            else -> {
                player.peekTimer = 0f
                CubeEyeExpression.NEUTRAL
            }
        }

        // Eye look vector tracks nearest uncollected star, active rope, or goal portal
        val uncollectedStar = level.stars
            .filter { it.id !in player.collectedStarIds }
            .minByOrNull { hypot(it.x - player.centerX, it.y - player.centerY) }

        val targetX: Float
        val targetY: Float
        if (player.activityState == CubeActivityState.WALL_CLINGING) {
            targetX = player.centerX - player.wallClingSide * 120f
            targetY = player.centerY - 60f
        } else if (uncollectedStar != null && hypot(uncollectedStar.x - player.centerX, uncollectedStar.y - player.centerY) < 420f) {
            targetX = uncollectedStar.x
            targetY = uncollectedStar.y
        } else {
            targetX = level.goalX
            targetY = level.goalY
        }

        val dx = targetX - player.centerX
        val dy = targetY - player.centerY
        val dist = hypot(dx, dy).coerceAtLeast(1f)
        val desiredLookX = (dx / dist).coerceIn(-1f, 1f)
        val desiredLookY = (dy / dist).coerceIn(-1f, 1f)
        player.eyeLookX += (desiredLookX - player.eyeLookX) * (9f * dt)
        player.eyeLookY += (desiredLookY - player.eyeLookY) * (9f * dt)

        // Trail particles when moving fast or swinging
        if (hypot(player.vx, player.vy) > 310f && Random.nextFloat() < 0.45f) {
            particles.add(
                ParticleEffect(
                    x = player.centerX + (Random.nextFloat() - 0.5f) * 18f,
                    y = player.centerY + (Random.nextFloat() - 0.5f) * 18f,
                    vx = -player.vx * 0.15f,
                    vy = -player.vy * 0.15f,
                    size = Random.nextFloat() * 6f + 4f,
                    alpha = 0.75f,
                    decayRate = 1.9f,
                    color = level.biome.trimAccentColor
                )
            )
        }
    }

    private fun spawnBurstParticles(x: Float, y: Float, color: Color, count: Int) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * (Math.PI.toFloat() * 2f)
            val speed = Random.nextFloat() * 220f + 50f
            particles.add(
                ParticleEffect(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 40f,
                    size = Random.nextFloat() * 7f + 4f,
                    alpha = 1.0f,
                    decayRate = Random.nextFloat() * 1.4f + 1.2f,
                    color = color
                )
            )
        }
    }

    private fun updateParticles(dt: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 240f * dt
            p.alpha -= p.decayRate * dt
            if (p.alpha <= 0.02f) {
                iterator.remove()
            }
        }
    }

    private fun rectsOverlap(
        ax: Float,
        ay: Float,
        aw: Float,
        ah: Float,
        bx: Float,
        by: Float,
        bw: Float,
        bh: Float
    ): Boolean {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by
    }

    companion object {
        const val MAX_RUN_SPEED = 370f
        const val GROUND_ACCEL = 2200f
        const val AIR_ACCEL = 1550f
        const val GROUND_FRICTION = 2100f
        const val AIR_DRAG = 320f

        const val GRAVITY_ACCEL = 1380f
        const val WALL_SLIDE_GRAVITY = 340f
        const val MAX_FALL_SPEED = 780f

        const val JUMP_INITIAL_VELOCITY = 540f
        const val JUMP_HOLD_BOOST_ACCEL = 820f
        const val MAX_JUMP_HOLD_SECONDS = 0.24f
        const val COYOTE_TIME_SECONDS = 0.12f
        const val JUMP_BUFFER_SECONDS = 0.13f

        const val WALL_JUMP_HORIZONTAL_SPEED = 410f
        const val WALL_JUMP_VERTICAL_SPEED = 530f

        const val ROPE_GRAVITY = 1280f
        const val ROPE_PUMP_ACCEL = 3.6f
    }
}

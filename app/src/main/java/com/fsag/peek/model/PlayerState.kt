package com.fsag.peek.model

import androidx.compose.ui.graphics.Color

enum class CubeActivityState {
    GROUNDED,
    JUMPING,
    FALLING,
    WALL_CLINGING,
    SWINGING,
    VICTORY,
    SHATTERED
}

enum class CubeEyeExpression {
    NEUTRAL,
    PEEKING,
    WIDE_EXCITED,
    FOCUSED_SQUINT,
    WALL_LOOK,
    STARTLED,
    HAPPY_ARC
}

data class ParticleEffect(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    var decayRate: Float,
    val color: Color,
    val isSquare: Boolean = true
)

/**
 * Tracks "The Cube" player position, velocity, squash-and-stretch 3D scale,
 * expressive eye animations, rope pendulum attachment, health/lives, and score.
 */
data class PlayerState(
    var x: Float = 120f,
    var y: Float = 420f,
    val size: Float = 48f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var tiltAngleDeg: Float = 0f,
    var facingRight: Boolean = true,
    var activityState: CubeActivityState = CubeActivityState.GROUNDED,
    var eyeExpression: CubeEyeExpression = CubeEyeExpression.NEUTRAL,
    var eyeLookX: Float = 1f,
    var eyeLookY: Float = 0f,
    var blinkTimer: Float = 0f,
    var isBlinking: Boolean = false,
    var peekTimer: Float = 0f,
    // Jump & Wall Cling physics timers
    var coyoteTimeLeft: Float = 0f,
    var jumpBufferLeft: Float = 0f,
    var isHoldingJump: Boolean = false,
    var jumpHoldTime: Float = 0f,
    var wallClingSide: Int = 0, // -1 clinging to wall on left, +1 clinging to wall on right, 0 none
    var groundedPlatformId: Int = -1,
    // Rope swinging state
    var attachedRopeId: Int = -1,
    var ropeAngleRad: Float = 0f,
    var ropeAngularVel: Float = 0f,
    var ropeGrabLength: Float = 140f,
    var ropeCooldown: Float = 0f,
    var nearestRopeId: Int = -1,
    var canGrabNearestRope: Boolean = false,
    // Health, Score, Stars, Level timers
    var lives: Int = 3,
    var maxLives: Int = 3,
    var invulnerableTimer: Float = 0f,
    var score: Int = 0,
    var collectedStarIds: MutableSet<Int> = mutableSetOf(),
    var elapsedSeconds: Float = 0f,
    var checkpointX: Float = 120f,
    var checkpointY: Float = 420f
) {
    val centerX: Float get() = x + size * 0.5f
    val centerY: Float get() = y + size * 0.5f
}

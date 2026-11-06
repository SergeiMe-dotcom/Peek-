package com.fsag.peek.model

import androidx.compose.ui.graphics.Color

/**
 * Atmospheric biome themes across the 20 hand-crafted levels of PEEK.
 */
enum class BiomeTheme(
    val displayName: String,
    val skyTopColor: Color,
    val skyBottomColor: Color,
    val monolithColor: Color,
    val platformTopColor: Color,
    val platformFrontColor: Color,
    val platformSideColor: Color,
    val trimAccentColor: Color,
    val fogColor: Color
) {
    LUMINOUS_RUINS(
        displayName = "World I: Luminous Ruins",
        skyTopColor = Color(0xFF060B18),
        skyBottomColor = Color(0xFF10243E),
        monolithColor = Color(0xFF132A46),
        platformTopColor = Color(0xFF203E5F),
        platformFrontColor = Color(0xFF12263C),
        platformSideColor = Color(0xFF0A1727),
        trimAccentColor = Color(0xFF00F0FF),
        fogColor = Color(0xFF00E5FF)
    ),
    CLOCKWORK_CHASM(
        displayName = "World II: Amber Sanctum",
        skyTopColor = Color(0xFF140B06),
        skyBottomColor = Color(0xFF2E1A0D),
        monolithColor = Color(0xFF382112),
        platformTopColor = Color(0xFF4F311C),
        platformFrontColor = Color(0xFF331E10),
        platformSideColor = Color(0xFF1F1108),
        trimAccentColor = Color(0xFFFFB800),
        fogColor = Color(0xFFFF9900)
    ),
    CRYSTAL_ABYSS(
        displayName = "World III: Crystal Abyss",
        skyTopColor = Color(0xFF0E061C),
        skyBottomColor = Color(0xFF231145),
        monolithColor = Color(0xFF2D1754),
        platformTopColor = Color(0xFF422375),
        platformFrontColor = Color(0xFF28134A),
        platformSideColor = Color(0xFF190A30),
        trimAccentColor = Color(0xFFE056FD),
        fogColor = Color(0xFFB53471)
    ),
    ASTRAL_CITADEL(
        displayName = "World IV: Astral Citadel",
        skyTopColor = Color(0xFF041414),
        skyBottomColor = Color(0xFF0A2E2E),
        monolithColor = Color(0xFF103B3B),
        platformTopColor = Color(0xFF195454),
        platformFrontColor = Color(0xFF0E3434),
        platformSideColor = Color(0xFF071F1F),
        trimAccentColor = Color(0xFF00FFA3),
        fogColor = Color(0xFF00F5D4)
    )
}

enum class PlatformType {
    STATIC,
    MOVING_HORIZONTAL,
    MOVING_VERTICAL,
    BOUNCY,
    WALL_CLIMBABLE
}

/**
 * 3D-extruded platform block in world space (units in game pixels, Y+ is downward, ground ~ 600f).
 */
data class PlatformData(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: PlatformType = PlatformType.STATIC,
    val moveRange: Float = 0f,
    val moveSpeed: Float = 1.6f,
    val phaseOffset: Float = 0f,
    val bounceMultiplier: Float = 1.45f,
    val depth3D: Float = 28f
) {
    fun currentX(timeSeconds: Float): Float {
        return if (type == PlatformType.MOVING_HORIZONTAL) {
            x + kotlin.math.sin(timeSeconds * moveSpeed + phaseOffset) * moveRange
        } else {
            x
        }
    }

    fun currentY(timeSeconds: Float): Float {
        return if (type == PlatformType.MOVING_VERTICAL) {
            y + kotlin.math.sin(timeSeconds * moveSpeed + phaseOffset) * moveRange
        } else {
            y
        }
    }

    fun velocityX(timeSeconds: Float): Float {
        return if (type == PlatformType.MOVING_HORIZONTAL) {
            kotlin.math.cos(timeSeconds * moveSpeed + phaseOffset) * moveRange * moveSpeed
        } else {
            0f
        }
    }

    fun velocityY(timeSeconds: Float): Float {
        return if (type == PlatformType.MOVING_VERTICAL) {
            kotlin.math.cos(timeSeconds * moveSpeed + phaseOffset) * moveRange * moveSpeed
        } else {
            0f
        }
    }
}

/**
 * Interactive swinging rope anchor for pendulum physics traversal across long chasms.
 */
data class RopeSwingData(
    val id: Int,
    val anchorX: Float,
    val anchorY: Float,
    val length: Float,
    val initialAngleRad: Float = 0.25f,
    val grabRadius: Float = 84f
)

enum class HazardType {
    SPIKE_STRIP,
    ENERGY_SAW,
    LASER_PULSE
}

/**
 * Hazard obstacles in world coordinates.
 */
data class HazardData(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: HazardType = HazardType.SPIKE_STRIP,
    val moveRangeX: Float = 0f,
    val moveRangeY: Float = 0f,
    val moveSpeed: Float = 2.0f,
    val phaseOffset: Float = 0f
) {
    fun currentX(timeSeconds: Float): Float =
        if (moveRangeX != 0f) x + kotlin.math.sin(timeSeconds * moveSpeed + phaseOffset) * moveRangeX else x

    fun currentY(timeSeconds: Float): Float =
        if (moveRangeY != 0f) y + kotlin.math.sin(timeSeconds * moveSpeed + phaseOffset) * moveRangeY else y
}

/**
 * Collectible star crystal (3 per level) that boosts score and star rating.
 */
data class StarCrystalData(
    val id: Int,
    val x: Float,
    val y: Float,
    val bonusPoints: Int = 500
)

/**
 * Complete definition for one of the 20 sequential hand-crafted stages.
 */
data class LevelData(
    val levelNumber: Int,
    val title: String,
    val subtitle: String,
    val biome: BiomeTheme,
    val startX: Float,
    val startY: Float,
    val goalX: Float,
    val goalY: Float,
    val deathPitY: Float = 940f,
    val targetTimeSeconds: Float = 45f,
    val twoStarScoreThreshold: Int = 1200,
    val threeStarScoreThreshold: Int = 2200,
    val platforms: List<PlatformData>,
    val ropes: List<RopeSwingData>,
    val hazards: List<HazardData>,
    val stars: List<StarCrystalData>,
    val hintText: String = ""
)

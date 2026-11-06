package com.fsag.peek.levels

import com.fsag.peek.model.BiomeTheme
import com.fsag.peek.model.HazardData
import com.fsag.peek.model.HazardType
import com.fsag.peek.model.LevelData
import com.fsag.peek.model.PlatformData
import com.fsag.peek.model.PlatformType
import com.fsag.peek.model.RopeSwingData
import com.fsag.peek.model.StarCrystalData

/**
 * Hand-crafted non-procedural registry of all 20 sequential levels in PEEK.
 * Divided into 4 atmospheric biomes (5 stages per biome):
 * - Levels 1..5: World I - Luminous Ruins (Fundamentals: variable jump, moving platforms, first rope swings)
 * - Levels 6..10: World II - Amber Sanctum (Wall clinging, chained pendulum ropes, oscillating hazards)
 * - Levels 11..15: World III - Crystal Abyss (Bounce monoliths, vertical shafts, precision pendulum transfers)
 * - Levels 16..20: World IV - Astral Citadel (Mastery gauntlet combining all traversal mechanics)
 */
object LevelRegistry {

    val allLevels: List<LevelData> by lazy {
        listOf(
            level1(),
            level2(),
            level3(),
            level4(),
            level5(),
            level6(),
            level7(),
            level8(),
            level9(),
            level10(),
            level11(),
            level12(),
            level13(),
            level14(),
            level15(),
            level16(),
            level17(),
            level18(),
            level19(),
            level20()
        )
    }

    fun getLevel(levelNumber: Int): LevelData {
        return allLevels.getOrElse((levelNumber - 1).coerceIn(0, 19)) { level1() }
    }

    // =========================================================================
    // WORLD I: LUMINOUS RUINS (Levels 1 - 5)
    // =========================================================================

    private fun level1() = LevelData(
        levelNumber = 1,
        title = "First Peek",
        subtitle = "Awakening in the Ruins",
        biome = BiomeTheme.LUMINOUS_RUINS,
        startX = 100f,
        startY = 470f,
        goalX = 1920f,
        goalY = 390f,
        hintText = "Hold D-Pad to move. Hold JUMP longer for higher leaps!",
        platforms = listOf(
            PlatformData(1, 40f, 540f, 420f, 60f),
            PlatformData(2, 540f, 490f, 280f, 55f),
            PlatformData(3, 910f, 430f, 300f, 55f),
            PlatformData(4, 1300f, 480f, 280f, 55f),
            PlatformData(5, 1660f, 450f, 380f, 65f)
        ),
        ropes = emptyList(),
        hazards = emptyList(),
        stars = listOf(
            StarCrystalData(1, 680f, 410f),
            StarCrystalData(2, 1060f, 340f),
            StarCrystalData(3, 1440f, 395f)
        )
    )

    private fun level2() = LevelData(
        levelNumber = 2,
        title = "Tethered Light",
        subtitle = "Introduction to Pendulum Ropes",
        biome = BiomeTheme.LUMINOUS_RUINS,
        startX = 90f,
        startY = 460f,
        goalX = 2150f,
        goalY = 380f,
        hintText = "Tap or Hold GRAB near a glowing tether to swing across the chasm!",
        platforms = listOf(
            PlatformData(1, 40f, 530f, 380f, 60f),
            PlatformData(2, 920f, 510f, 340f, 55f),
            PlatformData(3, 1760f, 440f, 500f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 670f, anchorY = 180f, length = 260f, grabRadius = 110f),
            RopeSwingData(2, anchorX = 1510f, anchorY = 160f, length = 270f, grabRadius = 110f)
        ),
        hazards = listOf(
            HazardData(1, 1040f, 492f, 90f, 18f, HazardType.SPIKE_STRIP)
        ),
        stars = listOf(
            StarCrystalData(1, 670f, 420f),
            StarCrystalData(2, 1090f, 410f),
            StarCrystalData(3, 1510f, 400f)
        )
    )

    private fun level3() = LevelData(
        levelNumber = 3,
        title = "Drifting Monoliths",
        subtitle = "Harmonic Moving Stones",
        biome = BiomeTheme.LUMINOUS_RUINS,
        startX = 90f,
        startY = 470f,
        goalX = 2340f,
        goalY = 350f,
        hintText = "Time your jumps with the drifting stone monoliths.",
        platforms = listOf(
            PlatformData(1, 40f, 540f, 340f, 60f),
            PlatformData(2, 540f, 500f, 220f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 120f, moveSpeed = 1.6f),
            PlatformData(3, 980f, 460f, 260f, 55f),
            PlatformData(4, 1340f, 420f, 200f, 44f, PlatformType.MOVING_VERTICAL, moveRange = 110f, moveSpeed = 1.8f),
            PlatformData(5, 1660f, 380f, 230f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 130f, moveSpeed = 1.9f, phaseOffset = 1.5f),
            PlatformData(6, 2080f, 410f, 380f, 65f)
        ),
        ropes = emptyList(),
        hazards = listOf(
            HazardData(1, 1070f, 442f, 80f, 18f, HazardType.SPIKE_STRIP)
        ),
        stars = listOf(
            StarCrystalData(1, 650f, 410f),
            StarCrystalData(2, 1440f, 260f),
            StarCrystalData(3, 1780f, 290f)
        )
    )

    private fun level4() = LevelData(
        levelNumber = 4,
        title = "Twin Pendulums",
        subtitle = "Momentum Chain",
        biome = BiomeTheme.LUMINOUS_RUINS,
        startX = 90f,
        startY = 450f,
        goalX = 2480f,
        goalY = 360f,
        hintText = "Chain two rope swings in mid-air by pressing JUMP at peak swing!",
        platforms = listOf(
            PlatformData(1, 40f, 520f, 340f, 60f),
            PlatformData(2, 1180f, 520f, 280f, 55f),
            PlatformData(3, 2220f, 420f, 380f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 560f, anchorY = 170f, length = 260f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 890f, anchorY = 160f, length = 260f, grabRadius = 115f),
            RopeSwingData(3, anchorX = 1680f, anchorY = 165f, length = 265f, grabRadius = 115f),
            RopeSwingData(4, anchorX = 1990f, anchorY = 145f, length = 255f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 1280f, 502f, 80f, 18f, HazardType.SPIKE_STRIP)
        ),
        stars = listOf(
            StarCrystalData(1, 725f, 340f),
            StarCrystalData(2, 1320f, 410f),
            StarCrystalData(3, 1840f, 320f)
        )
    )

    private fun level5() = LevelData(
        levelNumber = 5,
        title = "Sentinel Gate",
        subtitle = "Guardian of the Ruins",
        biome = BiomeTheme.LUMINOUS_RUINS,
        startX = 90f,
        startY = 470f,
        goalX = 2660f,
        goalY = 320f,
        hintText = "Combine bounce pads, moving platforms, and rope swings to clear World I!",
        platforms = listOf(
            PlatformData(1, 40f, 540f, 320f, 60f),
            PlatformData(2, 450f, 540f, 160f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.55f),
            PlatformData(3, 740f, 340f, 280f, 50f),
            PlatformData(4, 1460f, 460f, 250f, 46f, PlatformType.MOVING_HORIZONTAL, moveRange = 140f, moveSpeed = 2.0f),
            PlatformData(5, 1920f, 520f, 180f, 46f, PlatformType.BOUNCY, bounceMultiplier = 1.6f),
            PlatformData(6, 2380f, 380f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 1210f, anchorY = 130f, length = 250f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 2210f, anchorY = 120f, length = 240f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 840f, 322f, 80f, 18f, HazardType.SPIKE_STRIP),
            HazardData(2, 1580f, 390f, 42f, 42f, HazardType.ENERGY_SAW, moveRangeX = 90f, moveSpeed = 2.4f)
        ),
        stars = listOf(
            StarCrystalData(1, 530f, 280f),
            StarCrystalData(2, 1210f, 370f),
            StarCrystalData(3, 2010f, 250f)
        )
    )

    // =========================================================================
    // WORLD II: AMBER SANCTUM (Levels 6 - 10)
    // =========================================================================

    private fun level6() = LevelData(
        levelNumber = 6,
        title = "Vertical Cling",
        subtitle = "Scaling the Amber Towers",
        biome = BiomeTheme.CLOCKWORK_CHASM,
        startX = 90f,
        startY = 480f,
        goalX = 2420f,
        goalY = 300f,
        hintText = "Push against tall pillars while airborne to Wall-Cling, then tap JUMP to Wall-Jump!",
        platforms = listOf(
            PlatformData(1, 40f, 550f, 340f, 60f),
            PlatformData(2, 480f, 260f, 70f, 320f, PlatformType.WALL_CLIMBABLE),
            PlatformData(3, 720f, 200f, 70f, 340f, PlatformType.WALL_CLIMBABLE),
            PlatformData(4, 920f, 260f, 320f, 55f),
            PlatformData(5, 1680f, 240f, 75f, 340f, PlatformType.WALL_CLIMBABLE),
            PlatformData(6, 2140f, 360f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 1440f, anchorY = 110f, length = 250f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 1940f, anchorY = 120f, length = 240f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 1040f, 242f, 80f, 18f, HazardType.SPIKE_STRIP)
        ),
        stars = listOf(
            StarCrystalData(1, 600f, 230f),
            StarCrystalData(2, 1440f, 350f),
            StarCrystalData(3, 1940f, 340f)
        )
    )

    private fun level7() = LevelData(
        levelNumber = 7,
        title = "Sawblade Pendulum",
        subtitle = "Precision Timing",
        biome = BiomeTheme.CLOCKWORK_CHASM,
        startX = 90f,
        startY = 460f,
        goalX = 2560f,
        goalY = 380f,
        hintText = "Watch the oscillating energy saws while swinging across the amber gulf.",
        platforms = listOf(
            PlatformData(1, 40f, 530f, 320f, 60f),
            PlatformData(2, 920f, 490f, 280f, 55f),
            PlatformData(3, 1740f, 470f, 260f, 55f),
            PlatformData(4, 2300f, 440f, 380f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 630f, anchorY = 150f, length = 260f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 1460f, anchorY = 140f, length = 265f, grabRadius = 115f),
            RopeSwingData(3, anchorX = 2120f, anchorY = 150f, length = 250f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 630f, 450f, 46f, 46f, HazardType.ENERGY_SAW, moveRangeX = 120f, moveSpeed = 2.2f),
            HazardData(2, 1460f, 440f, 46f, 46f, HazardType.ENERGY_SAW, moveRangeY = 90f, moveSpeed = 2.5f),
            HazardData(3, 1850f, 452f, 70f, 18f, HazardType.SPIKE_STRIP)
        ),
        stars = listOf(
            StarCrystalData(1, 630f, 330f),
            StarCrystalData(2, 1060f, 390f),
            StarCrystalData(3, 2120f, 320f)
        )
    )

    private fun level8() = LevelData(
        levelNumber = 8,
        title = "Clockwork Elevators",
        subtitle = "Synchronized Ascent",
        biome = BiomeTheme.CLOCKWORK_CHASM,
        startX = 90f,
        startY = 480f,
        goalX = 2520f,
        goalY = 280f,
        hintText = "Ride the vertical elevators and leap when they reach top velocity.",
        platforms = listOf(
            PlatformData(1, 40f, 550f, 300f, 60f),
            PlatformData(2, 440f, 440f, 200f, 44f, PlatformType.MOVING_VERTICAL, moveRange = 130f, moveSpeed = 2.0f),
            PlatformData(3, 760f, 360f, 200f, 44f, PlatformType.MOVING_VERTICAL, moveRange = 130f, moveSpeed = 2.0f, phaseOffset = 2.2f),
            PlatformData(4, 1080f, 320f, 320f, 55f),
            PlatformData(5, 1520f, 380f, 220f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 150f, moveSpeed = 2.3f),
            PlatformData(6, 1940f, 440f, 160f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.6f),
            PlatformData(7, 2240f, 340f, 400f, 65f)
        ),
        ropes = emptyList(),
        hazards = listOf(
            HazardData(1, 1200f, 302f, 90f, 18f, HazardType.SPIKE_STRIP),
            HazardData(2, 1630f, 310f, 44f, 44f, HazardType.ENERGY_SAW, moveRangeY = 85f, moveSpeed = 2.6f)
        ),
        stars = listOf(
            StarCrystalData(1, 540f, 250f),
            StarCrystalData(2, 860f, 180f),
            StarCrystalData(3, 2020f, 200f)
        )
    )

    private fun level9() = LevelData(
        levelNumber = 9,
        title = "Canyon of Three Tethers",
        subtitle = "High-Altitude Swinging",
        biome = BiomeTheme.CLOCKWORK_CHASM,
        startX = 90f,
        startY = 420f,
        goalX = 2680f,
        goalY = 340f,
        hintText = "Pump the D-Pad left and right while hanging on a rope to build massive swing arcs!",
        platforms = listOf(
            PlatformData(1, 40f, 490f, 320f, 60f),
            PlatformData(2, 1420f, 520f, 240f, 55f),
            PlatformData(3, 2400f, 400f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 560f, anchorY = 140f, length = 265f, grabRadius = 120f),
            RopeSwingData(2, anchorX = 890f, anchorY = 130f, length = 265f, grabRadius = 120f),
            RopeSwingData(3, anchorX = 1200f, anchorY = 140f, length = 265f, grabRadius = 120f),
            RopeSwingData(4, anchorX = 1860f, anchorY = 140f, length = 265f, grabRadius = 120f),
            RopeSwingData(5, anchorX = 2180f, anchorY = 130f, length = 255f, grabRadius = 120f)
        ),
        hazards = listOf(
            HazardData(1, 1040f, 430f, 46f, 46f, HazardType.ENERGY_SAW, moveRangeY = 100f, moveSpeed = 2.4f),
            HazardData(2, 2020f, 420f, 46f, 46f, HazardType.ENERGY_SAW, moveRangeY = 100f, moveSpeed = 2.6f)
        ),
        stars = listOf(
            StarCrystalData(1, 725f, 310f),
            StarCrystalData(2, 1540f, 420f),
            StarCrystalData(3, 2020f, 290f)
        )
    )

    private fun level10() = LevelData(
        levelNumber = 10,
        title = "Forge of Momentum",
        subtitle = "Amber Sanctum Finale",
        biome = BiomeTheme.CLOCKWORK_CHASM,
        startX = 90f,
        startY = 470f,
        goalX = 2820f,
        goalY = 300f,
        hintText = "Use wall jumps and bounce monoliths to reach the high tether!",
        platforms = listOf(
            PlatformData(1, 40f, 540f, 300f, 60f),
            PlatformData(2, 430f, 540f, 150f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.6f),
            PlatformData(3, 700f, 220f, 70f, 340f, PlatformType.WALL_CLIMBABLE),
            PlatformData(4, 920f, 280f, 280f, 50f),
            PlatformData(5, 1680f, 460f, 240f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 160f, moveSpeed = 2.4f),
            PlatformData(6, 2120f, 240f, 70f, 330f, PlatformType.WALL_CLIMBABLE),
            PlatformData(7, 2540f, 360f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 1420f, anchorY = 110f, length = 250f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 2360f, anchorY = 110f, length = 250f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 1020f, 262f, 80f, 18f, HazardType.SPIKE_STRIP),
            HazardData(2, 1420f, 420f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeX = 110f, moveSpeed = 2.7f),
            HazardData(3, 1950f, 380f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeY = 110f, moveSpeed = 2.7f)
        ),
        stars = listOf(
            StarCrystalData(1, 505f, 260f),
            StarCrystalData(2, 1420f, 290f),
            StarCrystalData(3, 2360f, 280f)
        )
    )

    // =========================================================================
    // WORLD III: CRYSTAL ABYSS (Levels 11 - 15)
    // =========================================================================

    private fun level11() = LevelData(
        levelNumber = 11,
        title = "Shard Ricochet",
        subtitle = "Entering the Crystal Abyss",
        biome = BiomeTheme.CRYSTAL_ABYSS,
        startX = 90f,
        startY = 470f,
        goalX = 2620f,
        goalY = 320f,
        hintText = "Chain crystal bounce pads while steering around laser pulses.",
        platforms = listOf(
            PlatformData(1, 40f, 540f, 300f, 60f),
            PlatformData(2, 430f, 520f, 160f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.6f),
            PlatformData(3, 740f, 480f, 160f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.65f),
            PlatformData(4, 1060f, 360f, 280f, 52f),
            PlatformData(5, 1820f, 500f, 170f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.65f),
            PlatformData(6, 2340f, 380f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 1560f, anchorY = 120f, length = 260f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 2120f, anchorY = 115f, length = 250f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 600f, 340f, 32f, 120f, HazardType.LASER_PULSE, moveRangeY = 80f, moveSpeed = 2.4f),
            HazardData(2, 1160f, 342f, 80f, 18f, HazardType.SPIKE_STRIP),
            HazardData(3, 1980f, 320f, 46f, 46f, HazardType.ENERGY_SAW, moveRangeY = 95f, moveSpeed = 2.8f)
        ),
        stars = listOf(
            StarCrystalData(1, 510f, 250f),
            StarCrystalData(2, 820f, 210f),
            StarCrystalData(3, 1560f, 310f)
        )
    )

    private fun level12() = LevelData(
        levelNumber = 12,
        title = "Prism Chimney",
        subtitle = "Twin Wall Shafts",
        biome = BiomeTheme.CRYSTAL_ABYSS,
        startX = 90f,
        startY = 480f,
        goalX = 2580f,
        goalY = 260f,
        hintText = "Bounce between opposing crystal walls to ascend the vertical chimney!",
        platforms = listOf(
            PlatformData(1, 40f, 550f, 300f, 60f),
            PlatformData(2, 430f, 180f, 68f, 390f, PlatformType.WALL_CLIMBABLE),
            PlatformData(3, 660f, 140f, 68f, 390f, PlatformType.WALL_CLIMBABLE),
            PlatformData(4, 820f, 220f, 280f, 52f),
            PlatformData(5, 1560f, 200f, 68f, 380f, PlatformType.WALL_CLIMBABLE),
            PlatformData(6, 1790f, 150f, 68f, 380f, PlatformType.WALL_CLIMBABLE),
            PlatformData(7, 2300f, 320f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 1310f, anchorY = 95f, length = 250f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 2080f, anchorY = 100f, length = 250f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 545f, 350f, 42f, 42f, HazardType.ENERGY_SAW, moveRangeY = 110f, moveSpeed = 2.3f),
            HazardData(2, 920f, 202f, 75f, 18f, HazardType.SPIKE_STRIP),
            HazardData(3, 1675f, 330f, 42f, 42f, HazardType.ENERGY_SAW, moveRangeY = 110f, moveSpeed = 2.6f)
        ),
        stars = listOf(
            StarCrystalData(1, 545f, 180f),
            StarCrystalData(2, 1310f, 290f),
            StarCrystalData(3, 1675f, 170f)
        )
    )

    private fun level13() = LevelData(
        levelNumber = 13,
        title = "Violet Vortex",
        subtitle = "Moving Bridges & Ropes",
        biome = BiomeTheme.CRYSTAL_ABYSS,
        startX = 90f,
        startY = 460f,
        goalX = 2760f,
        goalY = 340f,
        hintText = "Release your rope swing directly onto the moving crystal bridges.",
        platforms = listOf(
            PlatformData(1, 40f, 530f, 280f, 60f),
            PlatformData(2, 820f, 480f, 210f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 140f, moveSpeed = 2.4f),
            PlatformData(3, 1580f, 440f, 200f, 44f, PlatformType.MOVING_VERTICAL, moveRange = 130f, moveSpeed = 2.5f),
            PlatformData(4, 2040f, 470f, 210f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 150f, moveSpeed = 2.5f, phaseOffset = 1.8f),
            PlatformData(5, 2480f, 400f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 540f, anchorY = 140f, length = 260f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 1280f, anchorY = 130f, length = 260f, grabRadius = 115f),
            RopeSwingData(3, anchorX = 1840f, anchorY = 120f, length = 250f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 540f, 440f, 46f, 46f, HazardType.ENERGY_SAW, moveRangeX = 100f, moveSpeed = 2.6f),
            HazardData(2, 1280f, 420f, 32f, 110f, HazardType.LASER_PULSE, moveRangeX = 90f, moveSpeed = 2.5f),
            HazardData(3, 2320f, 360f, 46f, 46f, HazardType.ENERGY_SAW, moveRangeY = 100f, moveSpeed = 2.9f)
        ),
        stars = listOf(
            StarCrystalData(1, 540f, 310f),
            StarCrystalData(2, 1280f, 290f),
            StarCrystalData(3, 1840f, 290f)
        )
    )

    private fun level14() = LevelData(
        levelNumber = 14,
        title = "Suspended Skylines",
        subtitle = "Four-Rope Aerial Crossing",
        biome = BiomeTheme.CRYSTAL_ABYSS,
        startX = 90f,
        startY = 400f,
        goalX = 2820f,
        goalY = 320f,
        hintText = "Stay airborne across four sequential pendulum ropes over the abyss!",
        platforms = listOf(
            PlatformData(1, 40f, 470f, 280f, 60f),
            PlatformData(2, 1620f, 520f, 170f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.65f),
            PlatformData(3, 2540f, 380f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 520f, anchorY = 120f, length = 265f, grabRadius = 120f),
            RopeSwingData(2, anchorX = 860f, anchorY = 115f, length = 265f, grabRadius = 120f),
            RopeSwingData(3, anchorX = 1200f, anchorY = 110f, length = 265f, grabRadius = 120f),
            RopeSwingData(4, anchorX = 1980f, anchorY = 120f, length = 260f, grabRadius = 120f),
            RopeSwingData(5, anchorX = 2300f, anchorY = 115f, length = 255f, grabRadius = 120f)
        ),
        hazards = listOf(
            HazardData(1, 690f, 390f, 46f, 46f, HazardType.ENERGY_SAW, moveRangeY = 110f, moveSpeed = 2.6f),
            HazardData(2, 1030f, 390f, 46f, 46f, HazardType.ENERGY_SAW, moveRangeY = 110f, moveSpeed = 2.6f, phaseOffset = 1.5f),
            HazardData(3, 2140f, 380f, 34f, 120f, HazardType.LASER_PULSE, moveRangeY = 90f, moveSpeed = 2.8f)
        ),
        stars = listOf(
            StarCrystalData(1, 690f, 250f),
            StarCrystalData(2, 1705f, 260f),
            StarCrystalData(3, 2140f, 240f)
        )
    )

    private fun level15() = LevelData(
        levelNumber = 15,
        title = "Heart of the Abyss",
        subtitle = "Crystal Abyss Finale",
        biome = BiomeTheme.CRYSTAL_ABYSS,
        startX = 90f,
        startY = 470f,
        goalX = 2940f,
        goalY = 280f,
        hintText = "Master crystal bounce pads, wall-clinging, and pendulum launches!",
        platforms = listOf(
            PlatformData(1, 40f, 540f, 280f, 60f),
            PlatformData(2, 420f, 520f, 160f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.65f),
            PlatformData(3, 720f, 200f, 70f, 350f, PlatformType.WALL_CLIMBABLE),
            PlatformData(4, 1340f, 450f, 220f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 150f, moveSpeed = 2.6f),
            PlatformData(5, 1820f, 500f, 160f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.65f),
            PlatformData(6, 2140f, 180f, 70f, 360f, PlatformType.WALL_CLIMBABLE),
            PlatformData(7, 2660f, 340f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 1040f, anchorY = 105f, length = 255f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 2420f, anchorY = 100f, length = 250f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 1040f, 410f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeX = 120f, moveSpeed = 2.8f),
            HazardData(2, 1620f, 360f, 34f, 120f, HazardType.LASER_PULSE, moveRangeY = 100f, moveSpeed = 2.9f),
            HazardData(3, 2420f, 390f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeX = 110f, moveSpeed = 3.0f)
        ),
        stars = listOf(
            StarCrystalData(1, 500f, 240f),
            StarCrystalData(2, 1040f, 270f),
            StarCrystalData(3, 2420f, 260f)
        )
    )

    // =========================================================================
    // WORLD IV: ASTRAL CITADEL (Levels 16 - 20)
    // =========================================================================

    private fun level16() = LevelData(
        levelNumber = 16,
        title = "Emerald Conduit",
        subtitle = "Gates of the Citadel",
        biome = BiomeTheme.ASTRAL_CITADEL,
        startX = 90f,
        startY = 470f,
        goalX = 2860f,
        goalY = 300f,
        hintText = "High-speed moving conduits require crisp jump release timing.",
        platforms = listOf(
            PlatformData(1, 40f, 540f, 280f, 60f),
            PlatformData(2, 460f, 480f, 200f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 160f, moveSpeed = 2.7f),
            PlatformData(3, 920f, 400f, 200f, 44f, PlatformType.MOVING_VERTICAL, moveRange = 140f, moveSpeed = 2.7f),
            PlatformData(4, 1620f, 440f, 200f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 160f, moveSpeed = 2.8f, phaseOffset = 1.4f),
            PlatformData(5, 2080f, 500f, 160f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.65f),
            PlatformData(6, 2580f, 360f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 1280f, anchorY = 110f, length = 255f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 2360f, anchorY = 105f, length = 250f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 740f, 400f, 34f, 120f, HazardType.LASER_PULSE, moveRangeY = 90f, moveSpeed = 2.8f),
            HazardData(2, 1280f, 410f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeX = 120f, moveSpeed = 2.9f),
            HazardData(3, 1920f, 380f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeY = 110f, moveSpeed = 3.0f)
        ),
        stars = listOf(
            StarCrystalData(1, 560f, 370f),
            StarCrystalData(2, 1280f, 270f),
            StarCrystalData(3, 2160f, 230f)
        )
    )

    private fun level17() = LevelData(
        levelNumber = 17,
        title = "Astral Pendulum",
        subtitle = "Zero-Floor Chasm",
        biome = BiomeTheme.ASTRAL_CITADEL,
        startX = 90f,
        startY = 410f,
        goalX = 2940f,
        goalY = 300f,
        hintText = "Swing from rope to wall pillar and back to rope without touching the ground!",
        platforms = listOf(
            PlatformData(1, 40f, 480f, 270f, 60f),
            PlatformData(2, 880f, 180f, 68f, 350f, PlatformType.WALL_CLIMBABLE),
            PlatformData(3, 1580f, 180f, 68f, 350f, PlatformType.WALL_CLIMBABLE),
            PlatformData(4, 2240f, 180f, 68f, 350f, PlatformType.WALL_CLIMBABLE),
            PlatformData(5, 2660f, 360f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 560f, anchorY = 110f, length = 260f, grabRadius = 120f),
            RopeSwingData(2, anchorX = 1230f, anchorY = 110f, length = 260f, grabRadius = 120f),
            RopeSwingData(3, anchorX = 1910f, anchorY = 110f, length = 260f, grabRadius = 120f),
            RopeSwingData(4, anchorX = 2480f, anchorY = 110f, length = 245f, grabRadius = 120f)
        ),
        hazards = listOf(
            HazardData(1, 560f, 420f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeX = 110f, moveSpeed = 2.8f),
            HazardData(2, 1230f, 420f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeX = 110f, moveSpeed = 2.9f),
            HazardData(3, 1910f, 420f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeX = 110f, moveSpeed = 3.0f)
        ),
        stars = listOf(
            StarCrystalData(1, 560f, 270f),
            StarCrystalData(2, 1230f, 270f),
            StarCrystalData(3, 1910f, 270f)
        )
    )

    private fun level18() = LevelData(
        levelNumber = 18,
        title = "Laser Labyrinth",
        subtitle = "Pulse Synchronization",
        biome = BiomeTheme.ASTRAL_CITADEL,
        startX = 90f,
        startY = 470f,
        goalX = 2960f,
        goalY = 280f,
        hintText = "Thread between the oscillating laser barriers using short, controlled hops.",
        platforms = listOf(
            PlatformData(1, 40f, 540f, 270f, 60f),
            PlatformData(2, 410f, 500f, 220f, 50f),
            PlatformData(3, 740f, 450f, 220f, 50f),
            PlatformData(4, 1080f, 490f, 160f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.65f),
            PlatformData(5, 1400f, 300f, 260f, 50f),
            PlatformData(6, 2120f, 440f, 210f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 160f, moveSpeed = 2.8f),
            PlatformData(7, 2680f, 340f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 1840f, anchorY = 100f, length = 250f, grabRadius = 115f),
            RopeSwingData(2, anchorX = 2480f, anchorY = 105f, length = 245f, grabRadius = 115f)
        ),
        hazards = listOf(
            HazardData(1, 510f, 340f, 30f, 130f, HazardType.LASER_PULSE, moveRangeY = 90f, moveSpeed = 2.8f),
            HazardData(2, 840f, 290f, 30f, 130f, HazardType.LASER_PULSE, moveRangeY = 90f, moveSpeed = 2.8f, phaseOffset = 1.6f),
            HazardData(3, 1510f, 282f, 80f, 18f, HazardType.SPIKE_STRIP),
            HazardData(4, 2220f, 360f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeX = 120f, moveSpeed = 3.1f)
        ),
        stars = listOf(
            StarCrystalData(1, 520f, 410f),
            StarCrystalData(2, 1160f, 230f),
            StarCrystalData(3, 1840f, 270f)
        )
    )

    private fun level19() = LevelData(
        levelNumber = 19,
        title = "Crown of Monoliths",
        subtitle = "Ascent to the Spire",
        biome = BiomeTheme.ASTRAL_CITADEL,
        startX = 90f,
        startY = 480f,
        goalX = 3060f,
        goalY = 240f,
        hintText = "One penultimate gauntlet of vertical shafts, bounce pads, and dual ropes!",
        platforms = listOf(
            PlatformData(1, 40f, 550f, 260f, 60f),
            PlatformData(2, 400f, 180f, 68f, 380f, PlatformType.WALL_CLIMBABLE),
            PlatformData(3, 620f, 140f, 68f, 380f, PlatformType.WALL_CLIMBABLE),
            PlatformData(4, 1380f, 480f, 165f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.7f),
            PlatformData(5, 1740f, 320f, 220f, 44f, PlatformType.MOVING_VERTICAL, moveRange = 140f, moveSpeed = 2.8f),
            PlatformData(6, 2520f, 460f, 165f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.7f),
            PlatformData(7, 2780f, 300f, 400f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 920f, anchorY = 95f, length = 255f, grabRadius = 120f),
            RopeSwingData(2, anchorX = 1180f, anchorY = 105f, length = 255f, grabRadius = 120f),
            RopeSwingData(3, anchorX = 2160f, anchorY = 100f, length = 255f, grabRadius = 120f)
        ),
        hazards = listOf(
            HazardData(1, 510f, 330f, 44f, 44f, HazardType.ENERGY_SAW, moveRangeY = 120f, moveSpeed = 2.8f),
            HazardData(2, 1050f, 380f, 32f, 120f, HazardType.LASER_PULSE, moveRangeY = 95f, moveSpeed = 3.0f),
            HazardData(3, 2160f, 390f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeX = 130f, moveSpeed = 3.1f)
        ),
        stars = listOf(
            StarCrystalData(1, 510f, 160f),
            StarCrystalData(2, 1050f, 240f),
            StarCrystalData(3, 2160f, 260f)
        )
    )

    private fun level20() = LevelData(
        levelNumber = 20,
        title = "The Final Peek",
        subtitle = "Apex of the Astral Citadel",
        biome = BiomeTheme.ASTRAL_CITADEL,
        startX = 90f,
        startY = 470f,
        goalX = 3280f,
        goalY = 260f,
        targetTimeSeconds = 55f,
        twoStarScoreThreshold = 1500,
        threeStarScoreThreshold = 2500,
        hintText = "Stage 20 Finale: Unleash every skill of The Cube to reach the Apex Portal!",
        platforms = listOf(
            PlatformData(1, 40f, 540f, 260f, 60f),
            PlatformData(2, 390f, 520f, 160f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.65f),
            PlatformData(3, 680f, 180f, 68f, 360f, PlatformType.WALL_CLIMBABLE),
            PlatformData(4, 1360f, 440f, 200f, 44f, PlatformType.MOVING_HORIZONTAL, moveRange = 160f, moveSpeed = 2.9f),
            PlatformData(5, 1820f, 180f, 68f, 360f, PlatformType.WALL_CLIMBABLE),
            PlatformData(6, 2040f, 140f, 68f, 360f, PlatformType.WALL_CLIMBABLE),
            PlatformData(7, 2740f, 480f, 170f, 45f, PlatformType.BOUNCY, bounceMultiplier = 1.72f),
            PlatformData(8, 3000f, 320f, 420f, 65f)
        ),
        ropes = listOf(
            RopeSwingData(1, anchorX = 1020f, anchorY = 100f, length = 255f, grabRadius = 120f),
            RopeSwingData(2, anchorX = 2320f, anchorY = 95f, length = 255f, grabRadius = 120f),
            RopeSwingData(3, anchorX = 2560f, anchorY = 95f, length = 255f, grabRadius = 120f)
        ),
        hazards = listOf(
            HazardData(1, 1020f, 400f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeX = 120f, moveSpeed = 3.0f),
            HazardData(2, 1460f, 350f, 32f, 110f, HazardType.LASER_PULSE, moveRangeY = 95f, moveSpeed = 3.0f),
            HazardData(3, 1930f, 320f, 44f, 44f, HazardType.ENERGY_SAW, moveRangeY = 120f, moveSpeed = 3.0f),
            HazardData(4, 2440f, 380f, 48f, 48f, HazardType.ENERGY_SAW, moveRangeY = 110f, moveSpeed = 3.2f)
        ),
        stars = listOf(
            StarCrystalData(1, 470f, 230f),
            StarCrystalData(2, 1020f, 260f),
            StarCrystalData(3, 2440f, 240f)
        )
    )
}

package com.example

import com.fsag.peek.levels.LevelRegistry
import com.fsag.peek.model.CubeActivityState
import com.fsag.peek.model.PlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun levelRegistry_containsExactly20ValidLevels() {
        val levels = LevelRegistry.allLevels
        assertEquals(20, levels.size)
        levels.forEachIndexed { index, level ->
            assertEquals(index + 1, level.levelNumber)
            assertTrue(level.platforms.isNotEmpty())
            assertEquals(3, level.stars.size)
            assertTrue(level.goalX > level.startX)
        }
    }

    @Test
    fun playerState_centersCorrectly() {
        val player = PlayerState(x = 100f, y = 200f, size = 48f)
        assertEquals(124f, player.centerX, 0.01f)
        assertEquals(224f, player.centerY, 0.01f)
        assertEquals(CubeActivityState.GROUNDED, player.activityState)
    }
}

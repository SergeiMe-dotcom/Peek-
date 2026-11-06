package com.example

import com.fsag.peek.levels.LevelRegistry
import com.fsag.peek.model.CubeActivityState
import com.fsag.peek.model.PlayerState
import com.fsag.peek.storage.GameStorage
import com.fsag.peek.storage.HudElementId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun hudDefaults_andAll20Levels_areValid() {
        val defaults = GameStorage.defaultHudConfigs()
        assertEquals(5, defaults.size)
        assertNotNull(defaults[HudElementId.DPAD])
        assertNotNull(defaults[HudElementId.JUMP_BTN])
        assertNotNull(defaults[HudElementId.ROPE_BTN])
        assertNotNull(defaults[HudElementId.PAUSE_BTN])
        assertNotNull(defaults[HudElementId.STATUS_BAR])

        val level2 = LevelRegistry.getLevel(2)
        assertTrue(level2.ropes.isNotEmpty())
        val player = PlayerState(x = level2.startX, y = level2.startY)
        assertEquals(CubeActivityState.GROUNDED, player.activityState)
    }
}

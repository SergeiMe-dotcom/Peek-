package com.fsag.peek.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Identifies each customizable HUD control widget in landscape layout space.
 */
enum class HudElementId(val displayName: String) {
    DPAD("Directional D-Pad / Stick"),
    JUMP_BTN("Jump Action Button"),
    ROPE_BTN("Grab / Rope Button"),
    PAUSE_BTN("Pause Button"),
    STATUS_BAR("Level & Score Pill")
}

/**
 * Normalized landscape screen coordinates (0.0..1.0), scale (0.6..1.6), and opacity (0.25..1.0).
 */
data class HudElementConfig(
    val id: HudElementId,
    val normX: Float,
    val normY: Float,
    val scale: Float = 1.0f,
    val opacity: Float = 0.90f
)

data class LevelProgressRecord(
    val levelNumber: Int,
    val unlocked: Boolean,
    val starsEarned: Int = 0,
    val highScore: Int = 0,
    val bestTimeSeconds: Float = 0f
)

/**
 * Persists unlocked levels (1-20), star ratings, high scores, audio settings,
 * and customized landscape HUD button layouts using SharedPreferences + reactive StateFlows.
 */
class GameStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _highestUnlockedLevel = MutableStateFlow(loadHighestUnlockedLevel())
    val highestUnlockedLevel: StateFlow<Int> = _highestUnlockedLevel.asStateFlow()

    private val _sandboxUnlockAll = MutableStateFlow(prefs.getBoolean(KEY_SANDBOX_UNLOCK_ALL, false))
    val sandboxUnlockAll: StateFlow<Boolean> = _sandboxUnlockAll.asStateFlow()

    private val _levelRecords = MutableStateFlow(loadAllLevelRecords())
    val levelRecords: StateFlow<Map<Int, LevelProgressRecord>> = _levelRecords.asStateFlow()

    private val _hudConfigs = MutableStateFlow(loadHudConfigs())
    val hudConfigs: StateFlow<Map<HudElementId, HudElementConfig>> = _hudConfigs.asStateFlow()

    private val _musicVolume = MutableStateFlow(prefs.getFloat(KEY_MUSIC_VOL, 0.55f))
    val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    private val _sfxVolume = MutableStateFlow(prefs.getFloat(KEY_SFX_VOL, 0.80f))
    val sfxVolume: StateFlow<Float> = _sfxVolume.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTICS, true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    fun isLevelUnlocked(levelNumber: Int): Boolean {
        if (levelNumber <= 1) return true
        if (_sandboxUnlockAll.value) return true
        return levelNumber <= _highestUnlockedLevel.value
    }

    fun recordLevelVictory(
        levelNumber: Int,
        score: Int,
        stars: Int,
        elapsedSeconds: Float
    ) {
        val currentHighest = _highestUnlockedLevel.value
        val nextLevel = (levelNumber + 1).coerceAtMost(20)
        val newHighest = maxOf(currentHighest, nextLevel)

        val existing = _levelRecords.value[levelNumber]
        val bestStars = maxOf(existing?.starsEarned ?: 0, stars.coerceIn(1, 3))
        val bestScore = maxOf(existing?.highScore ?: 0, score)
        val bestTime = if (existing == null || existing.bestTimeSeconds <= 0f) {
            elapsedSeconds
        } else {
            minOf(existing.bestTimeSeconds, elapsedSeconds)
        }

        prefs.edit()
            .putInt(KEY_HIGHEST_LEVEL, newHighest)
            .putInt("level_${levelNumber}_stars", bestStars)
            .putInt("level_${levelNumber}_score", bestScore)
            .putFloat("level_${levelNumber}_time", bestTime)
            .apply()

        _highestUnlockedLevel.value = newHighest
        _levelRecords.value = loadAllLevelRecords()
    }

    fun setSandboxUnlockAll(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SANDBOX_UNLOCK_ALL, enabled).apply()
        _sandboxUnlockAll.value = enabled
        _levelRecords.value = loadAllLevelRecords()
    }

    fun saveAudioSettings(musicVol: Float, sfxVol: Float, haptics: Boolean) {
        prefs.edit()
            .putFloat(KEY_MUSIC_VOL, musicVol.coerceIn(0f, 1f))
            .putFloat(KEY_SFX_VOL, sfxVol.coerceIn(0f, 1f))
            .putBoolean(KEY_HAPTICS, haptics)
            .apply()
        _musicVolume.value = musicVol.coerceIn(0f, 1f)
        _sfxVolume.value = sfxVol.coerceIn(0f, 1f)
        _hapticsEnabled.value = haptics
    }

    fun saveHudConfigs(configs: Map<HudElementId, HudElementConfig>) {
        val editor = prefs.edit()
        for ((id, cfg) in configs) {
            editor.putFloat("hud_${id.name}_x", cfg.normX.coerceIn(0.04f, 0.96f))
            editor.putFloat("hud_${id.name}_y", cfg.normY.coerceIn(0.06f, 0.94f))
            editor.putFloat("hud_${id.name}_scale", cfg.scale.coerceIn(0.6f, 1.6f))
            editor.putFloat("hud_${id.name}_opacity", cfg.opacity.coerceIn(0.25f, 1.0f))
        }
        editor.apply()
        _hudConfigs.value = loadHudConfigs()
    }

    fun resetHudConfigsToDefault(): Map<HudElementId, HudElementConfig> {
        val defaults = defaultHudConfigs()
        saveHudConfigs(defaults)
        return defaults
    }

    fun totalStarsEarned(): Int = _levelRecords.value.values.sumOf { it.starsEarned }

    fun totalScoreEarned(): Int = _levelRecords.value.values.sumOf { it.highScore }

    private fun loadHighestUnlockedLevel(): Int {
        return prefs.getInt(KEY_HIGHEST_LEVEL, 1).coerceIn(1, 20)
    }

    private fun loadAllLevelRecords(): Map<Int, LevelProgressRecord> {
        val highest = prefs.getInt(KEY_HIGHEST_LEVEL, 1).coerceIn(1, 20)
        val unlockAll = prefs.getBoolean(KEY_SANDBOX_UNLOCK_ALL, false)
        val map = mutableMapOf<Int, LevelProgressRecord>()
        for (lvl in 1..20) {
            map[lvl] = LevelProgressRecord(
                levelNumber = lvl,
                unlocked = unlockAll || lvl <= highest,
                starsEarned = prefs.getInt("level_${lvl}_stars", 0),
                highScore = prefs.getInt("level_${lvl}_score", 0),
                bestTimeSeconds = prefs.getFloat("level_${lvl}_time", 0f)
            )
        }
        return map
    }

    private fun loadHudConfigs(): Map<HudElementId, HudElementConfig> {
        val defaults = defaultHudConfigs()
        val result = mutableMapOf<HudElementId, HudElementConfig>()
        for ((id, def) in defaults) {
            result[id] = HudElementConfig(
                id = id,
                normX = prefs.getFloat("hud_${id.name}_x", def.normX).coerceIn(0.04f, 0.96f),
                normY = prefs.getFloat("hud_${id.name}_y", def.normY).coerceIn(0.06f, 0.94f),
                scale = prefs.getFloat("hud_${id.name}_scale", def.scale).coerceIn(0.6f, 1.6f),
                opacity = prefs.getFloat("hud_${id.name}_opacity", def.opacity).coerceIn(0.25f, 1.0f)
            )
        }
        return result
    }

    companion object {
        private const val PREFS_NAME = "peek_game_storage_v1"
        private const val KEY_HIGHEST_LEVEL = "highest_unlocked_level"
        private const val KEY_SANDBOX_UNLOCK_ALL = "sandbox_unlock_all"
        private const val KEY_MUSIC_VOL = "music_volume"
        private const val KEY_SFX_VOL = "sfx_volume"
        private const val KEY_HAPTICS = "haptics_enabled"

        fun defaultHudConfigs(): Map<HudElementId, HudElementConfig> = mapOf(
            HudElementId.DPAD to HudElementConfig(
                id = HudElementId.DPAD,
                normX = 0.14f,
                normY = 0.78f,
                scale = 1.0f,
                opacity = 0.92f
            ),
            HudElementId.JUMP_BTN to HudElementConfig(
                id = HudElementId.JUMP_BTN,
                normX = 0.88f,
                normY = 0.79f,
                scale = 1.05f,
                opacity = 0.95f
            ),
            HudElementId.ROPE_BTN to HudElementConfig(
                id = HudElementId.ROPE_BTN,
                normX = 0.74f,
                normY = 0.82f,
                scale = 1.0f,
                opacity = 0.92f
            ),
            HudElementId.PAUSE_BTN to HudElementConfig(
                id = HudElementId.PAUSE_BTN,
                normX = 0.07f,
                normY = 0.12f,
                scale = 1.0f,
                opacity = 0.92f
            ),
            HudElementId.STATUS_BAR to HudElementConfig(
                id = HudElementId.STATUS_BAR,
                normX = 0.50f,
                normY = 0.11f,
                scale = 1.0f,
                opacity = 0.94f
            )
        )
    }
}

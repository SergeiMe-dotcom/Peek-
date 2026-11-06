package com.fsag.peek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.theme.MyApplicationTheme
import com.fsag.peek.audio.SoundEngine
import com.fsag.peek.storage.GameStorage
import com.fsag.peek.ui.GameScreen
import com.fsag.peek.ui.HudCustomizerScreen
import com.fsag.peek.ui.LevelSelectScreen
import com.fsag.peek.ui.MainMenuScreen
import com.fsag.peek.ui.SettingsDialog

enum class PeekScreenDestination {
    MAIN_MENU,
    LEVEL_SELECT,
    GAMEPLAY,
    HUD_CUSTOMIZER
}

/**
 * Core Entry Point (`com.fsag.peek.MainActivity`) initializing the landscape immersive game window,
 * `SoundEngine`, `GameStorage`, and screen transitions.
 */
open class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureImmersiveLandscape()

        setContent {
            MyApplicationTheme {
                PeekAppRoot()
            }
        }
    }

    private fun configureImmersiveLandscape() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun PeekAppRoot() {
    val context = LocalContext.current
    val gameStorage = remember { GameStorage(context) }
    val soundEngine = remember { SoundEngine(context) }

    val highestUnlockedLevel by gameStorage.highestUnlockedLevel.collectAsState()
    val sandboxUnlockAll by gameStorage.sandboxUnlockAll.collectAsState()
    val levelRecords by gameStorage.levelRecords.collectAsState()
    val hudConfigs by gameStorage.hudConfigs.collectAsState()
    val musicVolume by gameStorage.musicVolume.collectAsState()
    val sfxVolume by gameStorage.sfxVolume.collectAsState()
    val hapticsEnabled by gameStorage.hapticsEnabled.collectAsState()

    var currentScreen by remember { mutableStateOf(PeekScreenDestination.MAIN_MENU) }
    var previousScreenBeforeHud by remember { mutableStateOf(PeekScreenDestination.MAIN_MENU) }
    var activeLevelNumber by remember { mutableIntStateOf(1) }
    var showSettingsModal by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        soundEngine.musicVolume = musicVolume
        soundEngine.sfxVolume = sfxVolume
        soundEngine.hapticsEnabled = hapticsEnabled
        soundEngine.startBackgroundMusic()
        onDispose {
            soundEngine.stopBackgroundMusic()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            PeekScreenDestination.MAIN_MENU -> {
                MainMenuScreen(
                    highestUnlockedLevel = highestUnlockedLevel,
                    totalStarsEarned = gameStorage.totalStarsEarned(),
                    totalScoreEarned = gameStorage.totalScoreEarned(),
                    onPlayClicked = {
                        soundEngine.playButtonClick()
                        activeLevelNumber = highestUnlockedLevel.coerceIn(1, 20)
                        currentScreen = PeekScreenDestination.GAMEPLAY
                    },
                    onLevelSelectClicked = {
                        soundEngine.playButtonClick()
                        currentScreen = PeekScreenDestination.LEVEL_SELECT
                    },
                    onHudCustomizerClicked = {
                        soundEngine.playButtonClick()
                        previousScreenBeforeHud = PeekScreenDestination.MAIN_MENU
                        currentScreen = PeekScreenDestination.HUD_CUSTOMIZER
                    },
                    onSettingsClicked = {
                        soundEngine.playButtonClick()
                        showSettingsModal = true
                    }
                )
            }

            PeekScreenDestination.LEVEL_SELECT -> {
                LevelSelectScreen(
                    levelRecords = levelRecords,
                    sandboxUnlockAll = sandboxUnlockAll,
                    onToggleSandboxUnlockAll = { enabled ->
                        soundEngine.playButtonClick()
                        gameStorage.setSandboxUnlockAll(enabled)
                    },
                    onLevelSelected = { levelNum ->
                        soundEngine.playButtonClick()
                        activeLevelNumber = levelNum.coerceIn(1, 20)
                        currentScreen = PeekScreenDestination.GAMEPLAY
                    },
                    onBack = {
                        soundEngine.playButtonClick()
                        currentScreen = PeekScreenDestination.MAIN_MENU
                    }
                )
            }

            PeekScreenDestination.GAMEPLAY -> {
                GameScreen(
                    levelNumber = activeLevelNumber,
                    soundEngine = soundEngine,
                    hudConfigs = hudConfigs,
                    onLevelVictory = { lvl, score, stars, timeSec ->
                        gameStorage.recordLevelVictory(lvl, score, stars, timeSec)
                    },
                    onNavigateToNextLevel = { nextLevel ->
                        activeLevelNumber = nextLevel.coerceIn(1, 20)
                    },
                    onOpenHudCustomizer = {
                        previousScreenBeforeHud = PeekScreenDestination.GAMEPLAY
                        currentScreen = PeekScreenDestination.HUD_CUSTOMIZER
                    },
                    onExitToMainMenu = {
                        currentScreen = PeekScreenDestination.MAIN_MENU
                    }
                )
            }

            PeekScreenDestination.HUD_CUSTOMIZER -> {
                HudCustomizerScreen(
                    initialConfigs = hudConfigs,
                    onSaveConfigs = { updated ->
                        soundEngine.playButtonClick()
                        gameStorage.saveHudConfigs(updated)
                    },
                    onResetDefaults = {
                        soundEngine.playButtonClick()
                        gameStorage.resetHudConfigsToDefault()
                    },
                    onBack = {
                        soundEngine.playButtonClick()
                        currentScreen = previousScreenBeforeHud
                    }
                )
            }
        }

        if (showSettingsModal) {
            SettingsDialog(
                musicVolume = musicVolume,
                sfxVolume = sfxVolume,
                hapticsEnabled = hapticsEnabled,
                sandboxUnlockAll = sandboxUnlockAll,
                onAudioChanged = { newMusic, newSfx, newHaptics ->
                    soundEngine.musicVolume = newMusic
                    soundEngine.sfxVolume = newSfx
                    soundEngine.hapticsEnabled = newHaptics
                    gameStorage.saveAudioSettings(newMusic, newSfx, newHaptics)
                },
                onSandboxUnlockAllChanged = { unlockAll ->
                    soundEngine.playButtonClick()
                    gameStorage.setSandboxUnlockAll(unlockAll)
                },
                onOpenHudCustomizer = {
                    soundEngine.playButtonClick()
                    showSettingsModal = false
                    previousScreenBeforeHud = currentScreen
                    currentScreen = PeekScreenDestination.HUD_CUSTOMIZER
                },
                onDismiss = {
                    soundEngine.playButtonClick()
                    showSettingsModal = false
                }
            )
        }
    }
}

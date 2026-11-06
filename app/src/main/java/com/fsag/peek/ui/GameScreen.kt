package com.fsag.peek.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import com.fsag.peek.audio.SoundEngine
import com.fsag.peek.levels.LevelRegistry
import com.fsag.peek.model.CubeActivityState
import com.fsag.peek.model.PlayerState
import com.fsag.peek.physics.GamePhysicsEngine
import com.fsag.peek.render.GameRenderer
import com.fsag.peek.storage.HudElementConfig
import com.fsag.peek.storage.HudElementId

/**
 * Active Landscape Gameplay Screen (`GameScreen.kt`) integrating:
 * - 60fps `withFrameNanos` game loop
 * - `GamePhysicsEngine` (variable jump, moving platforms, wall cling, pendulum rope swings)
 * - `GameRenderer` (2.5D/3D-styled rendering, parallax layers, lighting, expressive Cube)
 * - `GameHud` (customizable touch controls)
 * - `GameDialogs` (Pause, Stage Clear, Game Over)
 */
@Composable
fun GameScreen(
    levelNumber: Int,
    soundEngine: SoundEngine,
    hudConfigs: Map<HudElementId, HudElementConfig>,
    onLevelVictory: (levelNumber: Int, score: Int, stars: Int, elapsedSeconds: Float) -> Unit,
    onNavigateToNextLevel: (Int) -> Unit,
    onOpenHudCustomizer: () -> Unit,
    onExitToMainMenu: () -> Unit
) {
    val level = remember(levelNumber) { LevelRegistry.getLevel(levelNumber) }
    val physicsEngine = remember { GamePhysicsEngine(soundEngine) }
    val renderer = remember { GameRenderer() }
    val player = remember(levelNumber) {
        PlayerState().also {
            physicsEngine.resetPlayerForLevel(it, level)
            renderer.resetCamera(it)
        }
    }

    var isPaused by remember { mutableStateOf(false) }
    var frameTick by remember { mutableIntStateOf(0) }
    var victoryRecorded by remember(levelNumber) { mutableStateOf(false) }

    // Touch & Keyboard Input States
    var touchMoveX by remember { mutableFloatStateOf(0f) }
    var touchMoveY by remember { mutableFloatStateOf(0f) }
    var keyLeftHeld by remember { mutableStateOf(false) }
    var keyRightHeld by remember { mutableStateOf(false) }
    var keyUpHeld by remember { mutableStateOf(false) }
    var keyDownHeld by remember { mutableStateOf(false) }

    var jumpPressedPending by remember { mutableStateOf(false) }
    var jumpHeld by remember { mutableStateOf(false) }
    var grabHeld by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    BackHandler {
        soundEngine.playButtonClick()
        isPaused = !isPaused
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun restartCurrentStage() {
        physicsEngine.resetPlayerForLevel(player, level)
        renderer.resetCamera(player)
        victoryRecorded = false
        isPaused = false
        jumpPressedPending = false
        jumpHeld = false
        grabHeld = false
        frameTick++
    }

    // 60FPS Real-Time Game Loop
    LaunchedEffect(levelNumber, isPaused) {
        var lastFrameNanos = 0L
        while (true) {
            withFrameNanos { nowNanos ->
                val dt = if (lastFrameNanos == 0L) {
                    0.016f
                } else {
                    ((nowNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                }
                lastFrameNanos = nowNanos

                if (!isPaused) {
                    val effectiveMoveX = when {
                        keyLeftHeld && !keyRightHeld -> -1f
                        keyRightHeld && !keyLeftHeld -> 1f
                        else -> touchMoveX
                    }
                    val effectiveMoveY = when {
                        keyUpHeld && !keyDownHeld -> -1f
                        keyDownHeld && !keyUpHeld -> 1f
                        else -> touchMoveY
                    }

                    physicsEngine.update(
                        dtRaw = dt,
                        player = player,
                        level = level,
                        moveInputX = effectiveMoveX,
                        moveInputY = effectiveMoveY,
                        jumpPressedThisFrame = jumpPressedPending,
                        jumpHeld = jumpHeld,
                        grabHeld = grabHeld
                    )
                    jumpPressedPending = false

                    if (player.activityState == CubeActivityState.VICTORY && !victoryRecorded) {
                        victoryRecorded = true
                        val starsEarned = calculateStarsEarned(
                            collectedStars = player.collectedStarIds.size,
                            score = player.score,
                            twoStarThreshold = level.twoStarScoreThreshold,
                            threeStarThreshold = level.threeStarScoreThreshold
                        )
                        onLevelVictory(level.levelNumber, player.score, starsEarned, player.elapsedSeconds)
                    }

                    frameTick++
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                val isDown = event.type == KeyEventType.KeyDown
                when (event.key) {
                    Key.DirectionLeft, Key.A -> {
                        keyLeftHeld = isDown
                        true
                    }
                    Key.DirectionRight, Key.D -> {
                        keyRightHeld = isDown
                        true
                    }
                    Key.DirectionUp, Key.W -> {
                        keyUpHeld = isDown
                        true
                    }
                    Key.DirectionDown, Key.S -> {
                        keyDownHeld = isDown
                        true
                    }
                    Key.Spacebar -> {
                        if (isDown && !jumpHeld) jumpPressedPending = true
                        jumpHeld = isDown
                        true
                    }
                    Key.E, Key.ShiftLeft, Key.ShiftRight -> {
                        grabHeld = isDown
                        true
                    }
                    else -> false
                }
            }
            .testTag("game_screen")
    ) {
        // 1. 2.5D Game World Renderer Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_renderer_canvas")
        ) {
            // Read frameTick so Compose redraws every frame
            val tick = frameTick
            if (tick >= 0) {
                with(renderer) {
                    renderFrame(
                        level = level,
                        player = player,
                        particles = physicsEngine.particles,
                        timeSeconds = player.elapsedSeconds
                    )
                }
            }
        }

        // 2. Customizable Landscape Touch HUD
        GameHud(
            levelNumber = level.levelNumber,
            levelTitle = level.title,
            score = player.score,
            starsCollected = player.collectedStarIds.size,
            lives = player.lives,
            maxLives = player.maxLives,
            canGrabRope = player.canGrabNearestRope,
            isSwinging = player.activityState == CubeActivityState.SWINGING,
            hintText = level.hintText,
            hudConfigs = hudConfigs,
            onMoveInputChanged = { dx, dy ->
                touchMoveX = dx
                touchMoveY = dy
            },
            onJumpStateChanged = { pressedThisFrame, held ->
                if (pressedThisFrame) {
                    jumpPressedPending = true
                }
                jumpHeld = held
            },
            onGrabStateChanged = { held ->
                grabHeld = held
            },
            onPauseClicked = {
                soundEngine.playButtonClick()
                isPaused = true
            }
        )

        // 3. Modal Game Dialogs (Pause, Stage Clear, Game Over)
        if (isPaused) {
            PauseDialog(
                levelNumber = level.levelNumber,
                levelTitle = level.title,
                onResume = {
                    soundEngine.playButtonClick()
                    isPaused = false
                },
                onRetry = {
                    soundEngine.playButtonClick()
                    restartCurrentStage()
                },
                onCustomizeHud = {
                    soundEngine.playButtonClick()
                    onOpenHudCustomizer()
                },
                onMainMenu = {
                    soundEngine.playButtonClick()
                    onExitToMainMenu()
                }
            )
        } else if (player.activityState == CubeActivityState.VICTORY) {
            val starsEarned = calculateStarsEarned(
                collectedStars = player.collectedStarIds.size,
                score = player.score,
                twoStarThreshold = level.twoStarScoreThreshold,
                threeStarThreshold = level.threeStarScoreThreshold
            )
            LevelClearDialog(
                levelNumber = level.levelNumber,
                levelTitle = level.title,
                score = player.score,
                starsEarned = starsEarned,
                elapsedSeconds = player.elapsedSeconds,
                onNextLevel = {
                    soundEngine.playButtonClick()
                    val next = (level.levelNumber + 1).coerceAtMost(20)
                    onNavigateToNextLevel(next)
                },
                onRetry = {
                    soundEngine.playButtonClick()
                    restartCurrentStage()
                },
                onMainMenu = {
                    soundEngine.playButtonClick()
                    onExitToMainMenu()
                }
            )
        } else if (player.activityState == CubeActivityState.SHATTERED) {
            GameOverDialog(
                levelNumber = level.levelNumber,
                levelTitle = level.title,
                score = player.score,
                onRetry = {
                    soundEngine.playButtonClick()
                    restartCurrentStage()
                },
                onMainMenu = {
                    soundEngine.playButtonClick()
                    onExitToMainMenu()
                }
            )
        }
    }
}

private fun calculateStarsEarned(
    collectedStars: Int,
    score: Int,
    twoStarThreshold: Int,
    threeStarThreshold: Int
): Int {
    return when {
        collectedStars >= 3 || score >= threeStarThreshold -> 3
        collectedStars >= 2 || score >= twoStarThreshold -> 2
        else -> 1
    }
}

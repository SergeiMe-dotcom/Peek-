package com.fsag.peek.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Synthesized polyphonic AudioEngine for "PEEK".
 * Generates real-time crystal-clear retro-atmospheric SFX (jump, rope grab, wall cling,
 * star chime, hazard hit, stage clear, UI click) and ambient background arpeggios using Android AudioTrack,
 * plus optional tactile haptics.
 */
class SoundEngine(context: Context) {

    private val appContext = context.applicationContext
    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var musicVolume: Float = 0.55f
    var sfxVolume: Float = 0.80f
    var hapticsEnabled: Boolean = true
    private var musicJob: Job? = null
    private var isMusicPlaying = false

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun startBackgroundMusic() {
        if (isMusicPlaying) return
        isMusicPlaying = true
        musicJob?.cancel()
        musicJob = audioScope.launch {
            // Atmospheric minor pentatonic arpeggio sequence (C4, Eb4, F4, G4, Bb4, C5, G4, Eb4)
            val notesHz = floatArrayOf(
                261.63f, 311.13f, 349.23f, 392.00f,
                466.16f, 523.25f, 392.00f, 311.13f,
                233.08f, 293.66f, 349.23f, 440.00f,
                392.00f, 349.23f, 311.13f, 261.63f
            )
            var step = 0
            while (isActive && isMusicPlaying) {
                if (musicVolume > 0.02f) {
                    val freq = notesHz[step % notesHz.size]
                    playAmbientPluck(freq, durationMs = 340, volume = musicVolume * 0.22f)
                }
                step++
                delay(380L)
            }
        }
    }

    fun stopBackgroundMusic() {
        isMusicPlaying = false
        musicJob?.cancel()
        musicJob = null
    }

    fun playJump(isWallJump: Boolean = false) {
        if (sfxVolume <= 0.01f) return
        triggerHaptic(18L)
        audioScope.launch {
            val startHz = if (isWallJump) 320f else 250f
            val endHz = if (isWallJump) 690f else 560f
            playSweepTone(startHz, endHz, durationMs = 135, volume = sfxVolume * 0.45f)
        }
    }

    fun playRopeGrab() {
        if (sfxVolume <= 0.01f) return
        triggerHaptic(28L)
        audioScope.launch {
            playSweepTone(420f, 780f, durationMs = 110, volume = sfxVolume * 0.50f)
        }
    }

    fun playRopeReleaseBoost() {
        if (sfxVolume <= 0.01f) return
        triggerHaptic(22L)
        audioScope.launch {
            playSweepTone(380f, 880f, durationMs = 160, volume = sfxVolume * 0.48f)
        }
    }

    fun playWallCling() {
        if (sfxVolume <= 0.01f) return
        triggerHaptic(12L)
        audioScope.launch {
            playSweepTone(190f, 260f, durationMs = 70, volume = sfxVolume * 0.30f)
        }
    }

    fun playStarCollect(starCount: Int) {
        if (sfxVolume <= 0.01f) return
        triggerHaptic(30L)
        audioScope.launch {
            val base = when (starCount) {
                1 -> 523.25f
                2 -> 659.25f
                else -> 783.99f
            }
            playChordArp(floatArrayOf(base, base * 1.25f, base * 1.5f), noteMs = 75, volume = sfxVolume * 0.55f)
        }
    }

    fun playHazardHit() {
        if (sfxVolume <= 0.01f) return
        triggerHaptic(75L)
        audioScope.launch {
            playSweepTone(240f, 75f, durationMs = 260, volume = sfxVolume * 0.60f, addHarmonicGrit = true)
        }
    }

    fun playLevelClear() {
        if (sfxVolume <= 0.01f) return
        triggerHaptic(55L)
        audioScope.launch {
            playChordArp(
                floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f),
                noteMs = 115,
                volume = sfxVolume * 0.60f
            )
        }
    }

    fun playButtonClick() {
        if (sfxVolume <= 0.01f) return
        triggerHaptic(10L)
        audioScope.launch {
            playSweepTone(640f, 920f, durationMs = 45, volume = sfxVolume * 0.35f)
        }
    }

    private fun triggerHaptic(durationMs: Long) {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore in environments without physical vibrator hardware
        }
    }

    private fun playAmbientPluck(freqHz: Float, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            val pcm = ShortArray(numSamples)
            var phase = 0.0
            val phaseInc = 2.0 * PI * freqHz / sampleRate
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val env = sin(PI * (t.coerceAtMost(0.12f) / 0.12f)).toFloat() * exp(-3.5 * t).toFloat()
                val sample = (sin(phase) * 0.75 + sin(phase * 2.0) * 0.25) * env * volume
                pcm[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                phase += phaseInc
            }
            writeAudioTrack(pcm, sampleRate)
        } catch (_: Exception) {
        }
    }

    private fun playSweepTone(
        startHz: Float,
        endHz: Float,
        durationMs: Int,
        volume: Float,
        addHarmonicGrit: Boolean = false
    ) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            val pcm = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val currentHz = startHz + (endHz - startHz) * progress
                phase += 2.0 * PI * currentHz / sampleRate
                val env = (1f - progress) * (if (progress < 0.08f) progress / 0.08f else 1f)
                val wave = if (addHarmonicGrit) {
                    (sin(phase) * 0.6 + sin(phase * 3.7) * 0.4)
                } else {
                    (sin(phase) * 0.8 + sin(phase * 2.0) * 0.2)
                }
                val sample = wave * env * volume
                pcm[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            }
            writeAudioTrack(pcm, sampleRate)
        } catch (_: Exception) {
        }
    }

    private fun playChordArp(freqs: FloatArray, noteMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val samplesPerNote = (sampleRate * noteMs) / 1000
            val totalSamples = samplesPerNote * freqs.size
            val pcm = ShortArray(totalSamples)
            var phase = 0.0
            for (n in freqs.indices) {
                val hz = freqs[n]
                val inc = 2.0 * PI * hz / sampleRate
                val offset = n * samplesPerNote
                for (i in 0 until samplesPerNote) {
                    val progress = i.toFloat() / samplesPerNote
                    val env = (1f - progress * 0.7f)
                    val sample = (sin(phase) * 0.8 + sin(phase * 2.0) * 0.2) * env * volume
                    pcm[offset + i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                    phase += inc
                }
            }
            writeAudioTrack(pcm, sampleRate)
        } catch (_: Exception) {
        }
    }

    private fun writeAudioTrack(pcm: ShortArray, sampleRate: Int) {
        val byteSize = pcm.size * 2
        if (byteSize <= 0) return
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(byteSize.coerceAtLeast(AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(pcm, 0, pcm.size)
        track.play()
        Thread.sleep(((pcm.size * 1000L) / sampleRate) + 40L)
        track.release()
    }
}

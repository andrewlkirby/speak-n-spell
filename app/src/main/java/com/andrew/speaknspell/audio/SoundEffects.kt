package com.andrew.speaknspell.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.andrew.speaknspell.R

/** Short correct/wrong/celebrate cues: a tone from [SoundPool] plus a matching haptic buzz. */
class SoundEffects(context: Context) {
    private val appContext = context.applicationContext

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val correctSoundId = soundPool.load(appContext, R.raw.correct, 1)
    private val wrongSoundId = soundPool.load(appContext, R.raw.wrong, 1)
    private val celebrateSoundId = soundPool.load(appContext, R.raw.celebrate, 1)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playCorrect(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) soundPool.play(correctSoundId, 1f, 1f, 1, 0, 1f)
        if (vibrationEnabled) buzz(durationMs = 20, amplitude = 90)
    }

    fun playWrong(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) soundPool.play(wrongSoundId, 1f, 1f, 1, 0, 1f)
        if (vibrationEnabled) buzz(durationMs = 45, amplitude = 70)
    }

    fun playCelebrate(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) soundPool.play(celebrateSoundId, 1f, 1f, 1, 0, 1f)
        if (vibrationEnabled) {
            val timings = longArrayOf(0, 60, 60, 60, 60, 120)
            val amplitudes = intArrayOf(0, 120, 0, 150, 0, 200)
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        }
    }

    private fun buzz(durationMs: Long, amplitude: Int) {
        vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
    }

    fun release() {
        soundPool.release()
    }
}

package com.andrew.speaknspell.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

const val DEFAULT_SPEECH_RATE = 0.9f

data class Progress(
    val lastLevel: Level = Level.FIRST_WORDS,
    val starsByLevel: Map<Level, Int> = emptyMap(),
    val phonicsEnabled: Boolean = true,
    val correctSoundEnabled: Boolean = true,
    val wrongSoundEnabled: Boolean = true,
    val celebrateSoundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
    val speechRate: Float = DEFAULT_SPEECH_RATE,
    val darkModeEnabled: Boolean = false,
)

/** Persists across app restarts: last level played, stars earned per level, settings. */
class ProgressStore(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    val progress: Flow<Progress> = dataStore.data.map { prefs ->
        Progress(
            lastLevel = prefs[LAST_LEVEL]?.let { ordinal -> Level.entries.getOrNull(ordinal) }
                ?: Level.FIRST_WORDS,
            starsByLevel = Level.entries.associateWith { level -> prefs[starsKey(level)] ?: 0 },
            phonicsEnabled = prefs[PHONICS_ENABLED] ?: true,
            correctSoundEnabled = prefs[CORRECT_SOUND_ENABLED] ?: true,
            wrongSoundEnabled = prefs[WRONG_SOUND_ENABLED] ?: true,
            celebrateSoundEnabled = prefs[CELEBRATE_SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[VIBRATION_ENABLED] ?: true,
            ttsEnabled = prefs[TTS_ENABLED] ?: true,
            speechRate = prefs[SPEECH_RATE] ?: DEFAULT_SPEECH_RATE,
            darkModeEnabled = prefs[DARK_MODE_ENABLED] ?: false,
        )
    }

    suspend fun setLastLevel(level: Level) {
        dataStore.edit { it[LAST_LEVEL] = level.ordinal }
    }

    suspend fun addStar(level: Level) {
        dataStore.edit { prefs ->
            val key = starsKey(level)
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    suspend fun setPhonicsEnabled(enabled: Boolean) {
        dataStore.edit { it[PHONICS_ENABLED] = enabled }
    }

    suspend fun setCorrectSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[CORRECT_SOUND_ENABLED] = enabled }
    }

    suspend fun setWrongSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[WRONG_SOUND_ENABLED] = enabled }
    }

    suspend fun setCelebrateSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[CELEBRATE_SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[VIBRATION_ENABLED] = enabled }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        dataStore.edit { it[TTS_ENABLED] = enabled }
    }

    suspend fun setSpeechRate(rate: Float) {
        dataStore.edit { it[SPEECH_RATE] = rate }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { it[DARK_MODE_ENABLED] = enabled }
    }

    suspend fun reset() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val LAST_LEVEL = intPreferencesKey("last_level")
        val PHONICS_ENABLED = booleanPreferencesKey("phonics_enabled")
        val CORRECT_SOUND_ENABLED = booleanPreferencesKey("correct_sound_enabled")
        val WRONG_SOUND_ENABLED = booleanPreferencesKey("wrong_sound_enabled")
        val CELEBRATE_SOUND_ENABLED = booleanPreferencesKey("celebrate_sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        fun starsKey(level: Level) = intPreferencesKey("stars_${level.name}")
    }
}

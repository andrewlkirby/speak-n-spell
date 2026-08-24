package com.andrew.speaknspell.tts

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

enum class SpeechStatus {
    INITIALIZING,
    READY,
    /** Engine failed to init, or the language's voice data isn't installed. */
    UNAVAILABLE,
}

/**
 * Thin wrapper around [TextToSpeech]. Owns the engine instance; callers must
 * invoke [stop] when backgrounding and [shutdown] when done (e.g. Activity
 * onStop/onDestroy) so the engine doesn't keep talking or leak.
 */
class SpeechService(context: Context) {
    private val appContext = context.applicationContext

    private val _status = MutableStateFlow(SpeechStatus.INITIALIZING)
    val status: StateFlow<SpeechStatus> = _status.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(appContext) { result -> onInit(result) }.also { engine ->
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java", ReplaceWith(""))
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    private fun onInit(result: Int) {
        if (result != TextToSpeech.SUCCESS) {
            _status.value = SpeechStatus.UNAVAILABLE
            return
        }
        val langResult = tts?.setLanguage(Locale.US)
        _status.value = when (langResult) {
            TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> SpeechStatus.UNAVAILABLE
            else -> {
                tts?.setSpeechRate(0.9f) // slightly slower — easier for a beginning reader to follow
                SpeechStatus.READY
            }
        }
    }

    /**
     * Speaks [text]. No-ops if not READY. When [interrupt] is true (default) this cuts off
     * anything currently playing; when false it queues after what's already speaking, so e.g.
     * quick letter callouts don't chop each other off mid-word.
     */
    fun speak(text: String, interrupt: Boolean = true) {
        if (_status.value != SpeechStatus.READY) return
        val queueMode = if (interrupt) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, UUID.randomUUID().toString())
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    companion object {
        /** Intent to send the user to install missing TTS voice data. */
        fun installVoiceDataIntent(): Intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
    }
}

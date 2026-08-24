package com.andrew.speaknspell

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.andrew.speaknspell.audio.SoundEffects
import com.andrew.speaknspell.data.ProgressStore
import com.andrew.speaknspell.data.WordRepository
import com.andrew.speaknspell.tts.SpeechService

class MainActivity : ComponentActivity() {
    private lateinit var speechService: SpeechService
    private lateinit var soundEffects: SoundEffects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // this app is the whole point of the screen being on
        speechService = SpeechService(applicationContext)
        soundEffects = SoundEffects(applicationContext)
        val wordRepository = WordRepository()
        val progressStore = ProgressStore(applicationContext)

        setContent {
            SpeakNSpellApp(speechService, wordRepository, progressStore, soundEffects)
        }
    }

    override fun onStop() {
        super.onStop()
        speechService.stop()
    }

    override fun onDestroy() {
        speechService.shutdown()
        soundEffects.release()
        super.onDestroy()
    }
}

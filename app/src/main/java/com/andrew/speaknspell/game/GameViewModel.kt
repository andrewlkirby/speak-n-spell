package com.andrew.speaknspell.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrew.speaknspell.audio.SoundEffects
import com.andrew.speaknspell.data.Level
import com.andrew.speaknspell.data.Progress
import com.andrew.speaknspell.data.ProgressStore
import com.andrew.speaknspell.data.Word
import com.andrew.speaknspell.data.WordRepository
import com.andrew.speaknspell.tts.SpeechService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val level: Level,
    private val wordRepository: WordRepository,
    private val speechService: SpeechService,
    private val progressStore: ProgressStore,
    private val soundEffects: SoundEffects,
) : ViewModel() {

    private val sessionWords: List<Word> = wordRepository.shuffledWordsFor(level)
    private var wordIndex = 0
    private var nextTileId = 0
    private var settings = Progress()

    private val _uiState = MutableStateFlow(buildRoundState(sessionWords[wordIndex]))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        speakPrompt(sessionWords[wordIndex])
        viewModelScope.launch {
            progressStore.progress.collect { settings = it }
        }
    }

    fun repeatWord() {
        speak(_uiState.value.word.text)
    }

    fun onTileTapped(tileId: Int) {
        val previous = _uiState.value
        if (previous.isWordComplete || tileId in previous.placedTileIds) return
        val tappedTile = previous.tiles.firstOrNull { it.id == tileId } ?: return

        val updated = evaluateTap(previous, tileId)
        _uiState.value = updated

        // Phonics is a separate toggle from the master Voice switch — letter callouts only.
        // Queued, not flushed, so it doesn't chop off a letter still being announced.
        if (settings.phonicsEnabled) speak(tappedTile.letter.toString(), interrupt = false)

        if (updated.wrongTileId != null) {
            soundEffects.playWrong(settings.wrongSoundEnabled, settings.vibrationEnabled)
        } else {
            soundEffects.playCorrect(settings.correctSoundEnabled, settings.vibrationEnabled)
        }

        if (updated.isWordComplete && !previous.isWordComplete) {
            soundEffects.playCelebrate(settings.celebrateSoundEnabled, settings.vibrationEnabled)
            onWordCompleted(updated.word)
        }
    }

    /** Called by the UI once the shake animation for a wrong tap has finished playing. */
    fun clearWrongTile() {
        _uiState.value = _uiState.value.copy(wrongTileId = null)
    }

    private fun buildRoundState(word: Word): GameUiState {
        val letters = wordRepository.tileLetters(word, level)
        val tiles = letters.map { letter -> LetterTile(id = nextTileId++, letter = letter) }
        return GameUiState(level = level, word = word, tiles = tiles)
    }

    private fun speakPrompt(word: Word, interrupt: Boolean = true) {
        speak("Spell ${word.text}", interrupt = interrupt)
    }

    private fun onWordCompleted(word: Word) {
        viewModelScope.launch {
            progressStore.addStar(level)
            speak("You spelled ${word.text}!", interrupt = false)
            delay(WORD_COMPLETE_PAUSE_MS)
            advanceToNextWord()
        }
    }

    private fun speak(text: String, interrupt: Boolean = true) {
        if (settings.ttsEnabled) speechService.speak(text, interrupt)
    }

    private fun advanceToNextWord() {
        wordIndex = (wordIndex + 1) % sessionWords.size
        val nextWord = sessionWords[wordIndex]
        _uiState.value = buildRoundState(nextWord)
        // Queued, not flushed — never cut off the "You spelled ...!" praise if it's still playing.
        speakPrompt(nextWord, interrupt = false)
    }

    companion object {
        private const val WORD_COMPLETE_PAUSE_MS = 1800L

        /**
         * Pure tap evaluation, no side effects — safe to unit test without a ViewModel/Android
         * runtime. Ignores taps on an already-placed tile or after the word is already complete.
         */
        internal fun evaluateTap(state: GameUiState, tileId: Int): GameUiState {
            if (state.isWordComplete || tileId in state.placedTileIds) return state
            val tappedTile = state.tiles.firstOrNull { it.id == tileId } ?: return state

            val nextRequiredLetter = state.word.letters.getOrNull(state.placedTileIds.size)
            return if (tappedTile.letter == nextRequiredLetter) {
                val placed = state.placedTileIds + tileId
                state.copy(
                    placedTileIds = placed,
                    wrongTileId = null,
                    isWordComplete = placed.size == state.word.letters.size,
                )
            } else {
                state.copy(wrongTileId = tileId)
            }
        }
    }
}

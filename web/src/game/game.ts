import { type Level, type Word } from '../data/levels';
import { type ProgressStore } from '../data/progressStore';
import { shuffledWordsFor, tileLetters } from '../data/wordRepository';
import { type SoundEffects } from '../sound';
import { type SpeechService } from '../speech';
import { evaluateTap, type GameState } from './gameState';

const WORD_COMPLETE_PAUSE_MS = 1800;

/** One play session on one level — the web port of the Android GameViewModel. */
export class Game {
  private readonly sessionWords: Word[];
  private wordIndex = 0;
  private nextTileId = 0;
  private advanceTimer: number | undefined;
  private listener: ((state: GameState) => void) | null = null;
  private _state: GameState;

  constructor(
    private readonly level: Level,
    private readonly speech: SpeechService,
    private readonly progressStore: ProgressStore,
    private readonly sounds: SoundEffects,
  ) {
    this.sessionWords = shuffledWordsFor(level);
    this._state = this.buildRoundState(this.sessionWords[0]);
    this.speakPrompt(this._state.word);
  }

  get state(): GameState {
    return this._state;
  }

  onChange(listener: (state: GameState) => void): void {
    this.listener = listener;
  }

  repeatWord(): void {
    this.speak(this._state.word.text);
  }

  onTileTapped(tileId: number): void {
    const previous = this._state;
    if (previous.isWordComplete || previous.placedTileIds.includes(tileId)) return;
    const tappedTile = previous.tiles.find((tile) => tile.id === tileId);
    if (!tappedTile) return;

    const updated = evaluateTap(previous, tileId);
    this.setState(updated);
    const settings = this.progressStore.progress;

    // Phonics is a separate toggle from the master Voice switch — letter callouts only.
    // Queued, not flushed, so it doesn't chop off a letter still being announced.
    if (settings.phonicsEnabled) this.speak(tappedTile.letter, false);

    if (updated.wrongTileId !== null) {
      this.sounds.playWrong(settings.wrongSoundEnabled, settings.vibrationEnabled);
    } else {
      this.sounds.playCorrect(settings.correctSoundEnabled, settings.vibrationEnabled);
    }

    if (updated.isWordComplete && !previous.isWordComplete) {
      this.sounds.playCelebrate(settings.celebrateSoundEnabled, settings.vibrationEnabled);
      this.onWordCompleted(updated.word);
    }
  }

  /** Called by the UI once the shake animation for a wrong tap has finished playing. */
  clearWrongTile(): void {
    this.setState({ ...this._state, wrongTileId: null });
  }

  dispose(): void {
    window.clearTimeout(this.advanceTimer);
    this.listener = null;
  }

  private setState(state: GameState): void {
    this._state = state;
    this.listener?.(state);
  }

  private buildRoundState(word: Word): GameState {
    const tiles = tileLetters(word, this.level).map((letter) => ({ id: this.nextTileId++, letter }));
    return { level: this.level, word, tiles, placedTileIds: [], wrongTileId: null, isWordComplete: false };
  }

  private speakPrompt(word: Word, interrupt = true): void {
    this.speak(`Spell ${word.text}`, interrupt);
  }

  private onWordCompleted(word: Word): void {
    this.progressStore.addStar(this.level.id);
    this.speak(`You spelled ${word.text}!`, false);
    this.advanceTimer = window.setTimeout(() => this.advanceToNextWord(), WORD_COMPLETE_PAUSE_MS);
  }

  private speak(text: string, interrupt = true): void {
    if (this.progressStore.progress.ttsEnabled) this.speech.speak(text, interrupt);
  }

  private advanceToNextWord(): void {
    this.wordIndex = (this.wordIndex + 1) % this.sessionWords.length;
    const nextWord = this.sessionWords[this.wordIndex];
    this.setState(this.buildRoundState(nextWord));
    // Queued, not flushed — never cut off the "You spelled ...!" praise if it's still playing.
    this.speakPrompt(nextWord, false);
  }
}

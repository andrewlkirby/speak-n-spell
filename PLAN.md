# Speak & Spell — Android Spelling App for Kids

## Context

Build a native Android app inspired by the classic Speak & Spell toy that helps
kids learn to spell, with graduated difficulty from absolute-beginner (3-year-old)
words up to advanced spelling. The core loop: the app **speaks a word**, the child
**assembles it from letter tiles**, and gets **immediate, forgiving, celebratory
feedback**. Greenfield project (empty directory), personal use (no Play Store /
ads / accounts), fully offline.

**Decisions locked in:** Kotlin + Jetpack Compose · Android built-in `TextToSpeech`
(free, offline) · personal deployment · developer is comfortable coding.

### Key UX principle: usable by a 3-year-old
A 3yo can't read menus or type on a keyboard. So:
- **No reading required to navigate** — every screen/button self-narrates via TTS on
  appearance and on tap; navigation is big colorful icons (emoji), not text.
- **Tap letter tiles, never a keyboard** — big tiles, tap-to-place in order.
- **No fail state** — wrong tile gently shakes + says the letter; word can't be
  "lost." Endless gentle retry.
- **Emoji as word imagery** — `🐱`/`🐶`/`☀️` shown with each word. Zero image
  licensing/asset work, instantly recognizable, colorful.
- **Parent gate** — a hold-to-confirm / simple-math gate protects Settings & exit so
  the kid stays in the app.

---

## Tech stack & baseline

- **Language/UI:** Kotlin, Jetpack Compose (Material 3), single-Activity.
- **Min SDK:** 26 (Android 8.0) · **Target SDK:** current (35/36).
- **Arch:** `ViewModel` + `StateFlow`, unidirectional data flow. No network layer.
- **Async:** Coroutines.
- **Persistence:** Jetpack **DataStore (Preferences)** for progress/settings (last
  level, stars, phonics on/off).
- **Speech:** `android.speech.tts.TextToSpeech` wrapped in a small service class with
  `UtteranceProgressListener`.
- **SFX:** `SoundPool` for short correct/wrong/celebrate cues (bundled `.ogg` in
  `res/raw`), plus haptics via `HapticFeedback`.
- **Animation:** Compose animation APIs; lightweight confetti on success (hand-rolled
  `Canvas` particles — no third-party dep needed).
- **Build:** Gradle Kotlin DSL, version catalog (`libs.versions.toml`).
- **Testing:** JUnit for the word/game logic; Compose UI test for the game screen.

---

## Gameplay model

### Difficulty levels (bundled word lists)
| Level | Name | Audience | Word shape | Hints shown | Distractor tiles |
|-------|------|----------|-----------|-------------|------------------|
| 1 | First Words | ~3 yo | 3-letter CVC (cat, dog, sun, bed, pig) | emoji **+ word text** + audio | 0–1 |
| 2 | Growing | 4–5 yo | 4-letter (fish, star, milk, frog) | emoji + audio, word text fades after first listen | 2–3 |
| 3 | Speller | 5–7 yo | 5–7 letters (rabbit, yellow, garden) | audio only (classic mode) | full A–Z-ish set |
| 4 | Challenge | 7+ | tricky/irregular (because, friend, enough) | audio only, letter-name hint on request | full set |

Each level is a `List<Word>`; `Word(text, emoji, phonics?)`. Lists live as a typed
Kotlin object (or `assets/words.json` parsed once) — small enough to bundle, no DB.

### The round loop (one word)
1. TTS: *"Spell… **cat**"* (word repeated once, tap the ear icon to hear again).
2. Show emoji; show/hide word text per level; render shuffled letter tiles
   (correct letters + level-appropriate distractors) in a big responsive grid.
3. Child taps tiles left→right into answer slots.
   - **Correct next letter:** tile snaps into slot, optional phonics sound
     (letter name or phoneme), happy blip.
   - **Wrong tile:** tile shakes, buzzes softly, TTS says its letter; nothing is
     consumed. No penalty.
4. Word complete → confetti + celebrate SFX + TTS praise (*"You spelled cat!"*),
   award a star, auto-advance after a beat.
5. Progress (stars, current index) persisted to DataStore.

### Modes (phase 2, optional)
- **Learn** (word text shown, guided) vs **Quiz** (audio only) toggle — Level
  determines the default.

---

## Project structure

```
app/src/main/java/com/andrew/speaknspell/
├── MainActivity.kt                 // single Activity, sets Compose content
├── SpeakNSpellApp.kt               // NavHost + top-level theme
├── tts/
│   └── SpeechService.kt            // TextToSpeech wrapper (init, speak, queue, release)
├── audio/
│   └── SoundEffects.kt             // SoundPool cues + haptics
├── data/
│   ├── Word.kt                     // data class
│   ├── WordRepository.kt           // level -> List<Word>, shuffle, distractor gen
│   ├── Levels.kt                   // bundled word lists (or JSON loader)
│   └── ProgressStore.kt            // DataStore: stars, last level, settings
├── game/
│   ├── GameViewModel.kt            // round state machine, tile validation, scoring
│   ├── GameUiState.kt              // immutable UI state
│   └── LetterTile.kt               // tile model (char, id, placed?)
├── ui/
│   ├── HomeScreen.kt               // level picker (big emoji buttons, self-narrating)
│   ├── GameScreen.kt               // emoji, answer slots, tile grid, ear/repeat button
│   ├── Celebration.kt              // confetti Canvas + star burst
│   ├── ParentGate.kt              // hold / simple-math gate for settings & exit
│   ├── SettingsScreen.kt           // phonics on/off, TTS voice/speed, reset progress
│   └── theme/                      // Color, Type, Theme (large fonts, high contrast)
└── res/
    ├── raw/                        // correct.ogg, wrong.ogg, celebrate.ogg
    └── ...
```

Reused/standard building blocks (no need to hand-roll): `TextToSpeech` +
`UtteranceProgressListener`, `SoundPool.Builder`, `LazyVerticalGrid` for tiles,
`Preferences DataStore`, Compose `animateFloatAsState` / `Animatable` for shake &
confetti.

---

## Build order (milestones)

1. **Scaffold** — Android Studio project (Empty Compose Activity), Gradle version
   catalog, package, theme with oversized fonts + high-contrast kid palette.
2. **SpeechService** — init `TextToSpeech`, `speak(text)`, "speak again," graceful
   handling when TTS engine/voice missing (prompt to install). Verify on device.
3. **Data layer** — `Word`, `Levels` (start with Level 1 ~20 words), `WordRepository`
   (shuffle + distractor generation), `ProgressStore`.
4. **GameViewModel + GameScreen (Level 1)** — full round loop with tiles, slots,
   correct/wrong handling, TTS prompt, no-fail retry. This is the core — nail it first.
5. **Feedback layer** — `SoundEffects` (SoundPool + haptics), `Celebration` confetti,
   praise TTS.
6. **HomeScreen** — self-narrating level picker; wire navigation.
7. **Levels 2–4** — add word lists; per-level hint/distractor rules; word-text fade.
8. **ParentGate + SettingsScreen** — phonics toggle, TTS speed/voice, reset progress,
   gated exit.
9. **Polish** — animations, star rewards persistence, edge cases (TTS not ready,
   rotation, backgrounding stops speech).

Levels 1 + core loop (milestones 1–5) already produce a usable app for the 3yo; the
rest is breadth and polish.

---

## Key implementation notes / gotchas

- **TTS lifecycle:** init is async — gate the first prompt on the `onInit` callback;
  `shutdown()` in `onCleared`/`onStop`; stop speech when app backgrounds.
- **TTS availability:** device may lack a voice/engine — detect and show a friendly
  "install voice data" path; never crash silently.
- **Distractor generation:** pull extra letters that aren't already needed, cap tile
  count per level so Level 1 stays tiny.
- **All controls self-narrate** so a pre-reader can operate the app; keep on-screen
  text minimal.
- **No dark patterns / no fail:** wrong taps are always recoverable and encouraging.
- **Screen-on / immersive:** keep screen awake during a round; use immersive full
  screen so kids don't hit system bars.

---

## Verification

- **Unit tests** (`WordRepository`, `GameViewModel`): correct-letter acceptance,
  wrong-tile rejection (no state change), word completion → star awarded, level
  distractor counts, progress persisted.
- **Compose UI test** (`GameScreen`): tap correct tiles in order → celebration shown;
  tap wrong tile → tile still available, no advance.
- **Manual on-device (primary):** run on a physical Android phone/tablet.
  1. Launch → home narrates → pick Level 1.
  2. Confirm word is spoken; tap ear to repeat.
  3. Spell correctly → confetti + praise + star; auto-advance.
  4. Tap wrong tile → shake/buzz + letter spoken, no penalty.
  5. Rotate + background/foreground mid-round → no crash, speech stops on background.
  6. Parent gate blocks a 3yo from exiting to Settings.
  7. Kill/relaunch → progress (stars, level) restored.
- **Real-kid test:** hand it to the target child; watch whether they can start a
  word and complete it **without adult help** — the true success metric.

---

## Open / deferred (not in initial build)
- Pre-recorded human voice (chose device TTS; can add hybrid later).
- Google Play publishing + COPPA/Kids-policy assets (personal use for now).
- Extra modes (timed, spelling-bee), custom parent-defined word lists, multi-profile.

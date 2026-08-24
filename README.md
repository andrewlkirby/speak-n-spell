# Speak & Spell

A native Android spelling game for kids, inspired by the classic Speak & Spell
toy. The app **speaks a word**, the child **assembles it from letter tiles**,
and gets immediate, forgiving, celebratory feedback. Built for a pre-reader —
no login, no ads, no network, fully offline.

## Screenshots

| Home | Spelling | Celebration |
|------|----------|-------------|
| ![Home screen](docs/screenshots/home.png) | ![Game screen](docs/screenshots/game.png) | ![Celebration screen](docs/screenshots/celebration.png) |

| Parent gate | Settings |
|-------------|----------|
| ![Parent gate](docs/screenshots/parent-gate.png) | ![Settings screen](docs/screenshots/settings.png) |

## How it plays

1. The app speaks a word aloud (tap **Hear it again** to repeat it).
2. An emoji shows what the word means; the child taps letter tiles in order
   to spell it.
3. A wrong tile just shakes and says its letter — nothing is lost, no fail
   state, endless gentle retry.
4. Spelling the word correctly triggers confetti, a celebration sound, and a
   star, then auto-advances to the next word.

### Levels

| Level | Word shape | Hints shown |
|-------|-----------|-------------|
| 🌱 First Words | 3-letter (cat, dog, sun) | word text + emoji + audio |
| 🌻 Growing | 4-letter (fish, star, milk) | emoji + audio |
| ⭐ Speller | 5–7 letters (rabbit, yellow, garden) | audio only |
| 🏆 Challenge | long/tricky words (butterfly, elephant) | audio only |

Every word is paired with an emoji, so the game never requires the child to
already be able to read.

Settings (behind a parent gate — a simple math question) let a parent toggle
voice, phonics, sound effects, vibration, dark mode, and speech speed.

## Tech stack

- Kotlin + Jetpack Compose (Material 3), single-Activity
- `ViewModel` + `StateFlow`, unidirectional data flow, no network layer
- Android's built-in `TextToSpeech` for speech (free, offline)
- `SoundPool` for sound effects, haptics for feedback
- Jetpack DataStore (Preferences) for progress and settings persistence
- Min SDK 26, target SDK 35

## Building

```bash
./gradlew assembleDebug
```

The debug APK is installed for personal use only — there's no Play Store
listing, accounts, or ads.

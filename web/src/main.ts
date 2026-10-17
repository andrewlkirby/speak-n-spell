import { registerSW } from 'virtual:pwa-register';
import { type Level } from './data/levels';
import { ProgressStore } from './data/progressStore';
import { Game } from './game/game';
import { SoundEffects } from './sound';
import { SpeechService } from './speech';
import { h } from './ui/dom';
import { renderGame } from './ui/game';
import { renderHome } from './ui/home';
import { captureInstallPrompt, renderInstallHint } from './ui/installHint';
import { showParentGate } from './ui/parentGate';
import { showSettings } from './ui/settings';
import './styles.css';

registerSW({ immediate: true });
captureInstallPrompt();
void navigator.storage?.persist?.(); // ask the browser not to evict saved stars/settings

const app = document.getElementById('app')!;
const store = new ProgressStore();
const speech = new SpeechService();
const sounds = new SoundEffects();
let disposeScreen: (() => void) | null = null;

function applySettings(): void {
  const { darkModeEnabled, speechRate } = store.progress;
  document.documentElement.dataset.theme = darkModeEnabled ? 'dark' : 'light';
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', darkModeEnabled ? '#14141C' : '#FF6F3C');
  speech.setSpeechRate(speechRate);
}
store.subscribe(applySettings);
applySettings();

function speak(text: string): void {
  if (store.progress.ttsEnabled) speech.speak(text);
}

function show(screen: HTMLElement, dispose: (() => void) | null = null): void {
  disposeScreen?.();
  disposeScreen = dispose;
  app.replaceChildren(
    screen,
    h(
      'button',
      {
        class: 'icon-button settings-button',
        'aria-label': 'Settings',
        onclick: () => showParentGate(() => showSettings(store, speech)),
      },
      '⚙️',
    ),
  );
}

// Screens are swapped directly inside tap handlers (not on a later event) so that speech and
// audio stay inside the user gesture iOS requires. History entries let Android's back button
// (and the browser's) return from a game to the level picker instead of closing the app.
function showHome(): void {
  const home = renderHome(store.progress, speak, startGame);
  const hint = renderInstallHint();
  if (hint) home.append(hint);
  show(home);
}

function startGame(level: Level): void {
  store.setLastLevel(level.id);
  const { element, dispose } = renderGame(new Game(level, speech, store, sounds), () => history.back());
  show(element, dispose);
  history.pushState({ screen: 'game' }, '');
}

window.addEventListener('popstate', () => {
  speech.stop();
  showHome();
});

// Keep the screen awake while playing, and stop talking when the app is backgrounded.
let wakeLock: WakeLockSentinel | null = null;
async function keepAwake(): Promise<void> {
  try {
    wakeLock = (await navigator.wakeLock?.request('screen')) ?? null;
  } catch {
    wakeLock = null; // not supported, or denied (e.g. low battery) — not essential
  }
}
document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'hidden') {
    speech.stop();
  } else if (!wakeLock || wakeLock.released) {
    void keepAwake();
  }
});

// iOS keeps speech and audio locked until the first tap, so the app opens on a big play button.
function showSplash(): void {
  app.replaceChildren(
    h(
      'main',
      { class: 'splash' },
      h('h1', { class: 'home-title' }, '🐱 Speak & Spell 🐶'),
      h(
        'button',
        {
          class: 'play-button',
          'aria-label': 'Tap to play',
          onclick: () => {
            sounds.unlock();
            void keepAwake();
            showHome();
          },
        },
        '▶',
      ),
      speech.status === 'UNAVAILABLE' &&
        h('p', { class: 'hint error' }, "This browser can't speak. Try Safari on iPhone or Chrome on Android."),
    ),
  );
}

showSplash();

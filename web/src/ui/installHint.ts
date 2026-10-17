import { isAndroid, isIos, isStandalone } from '../platform';
import { h } from './dom';

const DISMISSED_KEY = 'speaknspell.installHintDismissed';

interface BeforeInstallPromptEvent extends Event {
  prompt(): Promise<void>;
}

// Chrome on Android fires this once, early — capture it at startup so the Install button works.
let installPrompt: BeforeInstallPromptEvent | null = null;
export function captureInstallPrompt(): void {
  window.addEventListener('beforeinstallprompt', (event) => {
    event.preventDefault();
    installPrompt = event as BeforeInstallPromptEvent;
  });
}

function dismissed(): boolean {
  try {
    return localStorage.getItem(DISMISSED_KEY) === '1';
  } catch {
    return false;
  }
}

/**
 * A small banner, shown only when the app is open in a browser tab, coaching a parent through
 * adding it to the home screen (iPhone has no install prompt, so it must be explained).
 */
export function renderInstallHint(): HTMLElement | null {
  if (isStandalone() || dismissed()) return null;

  let message: (Node | string)[];
  if (isIos) {
    message = [
      'Install: tap the ',
      h('strong', {}, 'Share'),
      ' button ',
      h('span', { class: 'share-glyph', 'aria-hidden': 'true' }, '⬆️'),
      ', then ',
      h('strong', {}, 'Add to Home Screen'),
      '.',
    ];
  } else if (installPrompt) {
    const prompt = installPrompt;
    message = [
      h(
        'button',
        {
          class: 'pill-button small',
          onclick: () => {
            void prompt.prompt();
            banner.remove();
          },
        },
        '📲 Install app',
      ),
      ' to play offline from your home screen.',
    ];
  } else {
    message = [
      `Install: open the browser menu${isAndroid ? ' ⋮' : ''} and choose `,
      h('strong', {}, 'Add to Home screen'),
      ' or ',
      h('strong', {}, 'Install app'),
      '.',
    ];
  }

  const banner = h(
    'aside',
    { class: 'install-hint' },
    h('span', {}, ...message),
    h(
      'button',
      {
        class: 'icon-button small',
        'aria-label': 'Dismiss',
        onclick: () => {
          try {
            localStorage.setItem(DISMISSED_KEY, '1');
          } catch {
            // ignore — it'll just show again next time
          }
          banner.remove();
        },
      },
      '✕',
    ),
  );
  return banner;
}

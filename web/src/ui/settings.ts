import { type ProgressStore, type Settings } from '../data/progressStore';
import { isIos } from '../platform';
import { canVibrate } from '../sound';
import { type SpeechService } from '../speech';
import { h, showDialog } from './dom';

const SPEECH_RATE_OPTIONS: [number, string][] = [
  [0.6, '🐢 Slow'],
  [0.9, '🙂 Normal'],
  [1.2, '🐇 Fast'],
];

type BooleanSetting = { [K in keyof Settings]: Settings[K] extends boolean ? K : never }[keyof Settings];

export function showSettings(store: ProgressStore, speech: SpeechService): void {
  const body = h('div', { class: 'settings-body' });
  const dialog = h(
    'dialog',
    { class: 'dialog' },
    h('h2', {}, 'Settings'),
    body,
    h('div', { class: 'dialog-actions' }, h('button', { class: 'text-button', onclick: () => dialog.close() }, 'Done')),
  );
  let confirmingReset = false;

  function switchRow(label: string, key: BooleanSetting): HTMLElement {
    const input = h('input', {
      type: 'checkbox',
      role: 'switch',
      checked: store.progress[key],
      onchange: (event) => store.setSetting(key, (event.target as HTMLInputElement).checked),
    });
    return h('label', { class: 'switch-row' }, h('span', {}, label), input);
  }

  function render(): void {
    const progress = store.progress;
    body.replaceChildren(
      switchRow('🔊 Voice', 'ttsEnabled'),
      switchRow('🔤 Phonics (say letters)', 'phonicsEnabled'),
      switchRow('✅ Correct sound', 'correctSoundEnabled'),
      switchRow('❌ Wrong sound', 'wrongSoundEnabled'),
      switchRow('🎉 Celebrate sound', 'celebrateSoundEnabled'),
      // iPhone browsers have no vibration support, so don't offer a switch that does nothing.
      canVibrate ? switchRow('📳 Vibration', 'vibrationEnabled') : '',
      switchRow('🌙 Dark mode', 'darkModeEnabled'),
      h('hr'),
      h('p', {}, 'Speech speed'),
      h(
        'div',
        { class: 'button-row' },
        ...SPEECH_RATE_OPTIONS.map(([rate, label]) =>
          h(
            'button',
            {
              class: progress.speechRate === rate ? 'pill-button' : 'pill-button outlined',
              'aria-pressed': String(progress.speechRate === rate),
              onclick: () => {
                store.setSetting('speechRate', rate);
                speech.setSpeechRate(rate);
                speech.speak('Spell cat');
              },
            },
            label,
          ),
        ),
      ),
      h(
        'p',
        { class: 'hint' },
        speech.status === 'UNAVAILABLE'
          ? "This browser can't speak. Try Safari on iPhone or Chrome on Android."
          : `Voice: ${speech.voiceName ?? 'system default'}. ` +
              (isIos
                ? 'For a clearer voice: Settings → Accessibility → Spoken Content → Voices → English, then download an “Enhanced” voice.'
                : 'For a clearer voice: Settings → Text-to-speech, choose Google and install English voice data.'),
      ),
      h('hr'),
      confirmingReset
        ? h(
            'div',
            { class: 'reset-confirm' },
            h('p', { class: 'error' }, "Reset all stars and settings? This can't be undone."),
            h(
              'div',
              { class: 'button-row' },
              h('button', { class: 'pill-button', onclick: () => { confirmingReset = false; store.reset(); } }, 'Yes, reset'),
              h('button', { class: 'pill-button outlined', onclick: () => { confirmingReset = false; render(); } }, 'Cancel'),
            ),
          )
        : h('button', { class: 'pill-button outlined', onclick: () => { confirmingReset = true; render(); } }, '🗑️ Reset Progress'),
    );
  }

  const unsubscribe = store.subscribe(render);
  dialog.addEventListener('close', unsubscribe);
  render();
  showDialog(dialog);
}

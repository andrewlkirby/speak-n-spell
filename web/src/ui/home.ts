import { levels, type Level } from '../data/levels';
import { type Progress } from '../data/progressStore';
import { h } from './dom';

/** The level picker — the app's entry point. No reading required: level cards are icon + color led. */
export function renderHome(
  progress: Progress,
  onSpeak: (text: string) => void,
  onLevelSelected: (level: Level) => void,
): HTMLElement {
  onSpeak('Pick a level to play!');

  return h(
    'main',
    { class: 'home' },
    h('h1', { class: 'home-title' }, '🐱 Speak & Spell 🐶'),
    h(
      'div',
      { class: 'level-grid' },
      ...levels.map((level) => {
        const stars = progress.starsByLevel[level.id] ?? 0;
        return h(
          'button',
          {
            class: `level-card level-${level.id}`,
            'aria-label': level.displayName,
            onclick: () => {
              onSpeak(level.displayName);
              onLevelSelected(level);
            },
          },
          h('span', { class: 'level-icon', 'aria-hidden': 'true' }, level.icon),
          h('span', { class: 'level-name' }, level.displayName),
          stars > 0 && h('span', { class: 'level-stars' }, `⭐ ${stars}`),
        );
      }),
    ),
  );
}

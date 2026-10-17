import { shuffled } from '../data/random';
import { h, showDialog } from './dom';

function randomInt(from: number, untilExclusive: number): number {
  return from + Math.floor(Math.random() * (untilExclusive - from));
}

/**
 * A quick addition problem — easy for an adult, unsolvable for a toddler — gating access to
 * Settings so a young child can't wander in and change/reset things.
 */
export function showParentGate(onUnlocked: () => void): void {
  const a = randomInt(2, 9);
  const b = randomInt(2, 9);
  const answer = a + b;
  const wrong = new Set<number>();
  while (wrong.size < 2) {
    const option = randomInt(4, 17);
    if (option !== answer) wrong.add(option);
  }

  const dialog = h(
    'dialog',
    { class: 'dialog' },
    h('h2', {}, 'Parents Only'),
    h('p', {}, `What is ${a} + ${b}?`),
    h(
      'div',
      { class: 'button-row' },
      ...shuffled([...wrong, answer]).map((option) =>
        h(
          'button',
          {
            class: 'pill-button',
            onclick: () => {
              if (option !== answer) return;
              dialog.close();
              onUnlocked();
            },
          },
          String(option),
        ),
      ),
    ),
    h('div', { class: 'dialog-actions' }, h('button', { class: 'text-button', onclick: () => dialog.close() }, 'Cancel')),
  );
  showDialog(dialog);
}

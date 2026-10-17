import { images, letters, type Word } from '../data/levels';
import { type Game } from '../game/game';
import { type GameState } from '../game/gameState';
import { playConfetti } from './celebration';
import { h } from './dom';

const SLOT_GAP = 6;
const SLOT_MAX = 56;
const TILE_GAP = 6;
const TILE_MAX = 72;

/** Shows the word's custom illustration if it has one, otherwise its emoji. */
function wordPicture(word: Word): HTMLElement {
  const picture = h('div', { class: 'word-picture', 'aria-hidden': 'true' });
  if (word.image && images[word.image]) {
    picture.innerHTML = images[word.image]; // generated at build time from our own drawables
  } else {
    picture.textContent = word.emoji;
  }
  return picture;
}

/**
 * Every tile fits on screen at once, no scrolling — the column count and tile size are picked to
 * maximize tile size within the available area, since Speller/Challenge can hand us the (nearly)
 * full alphabet as one round's tile pool. Layout uses the full fixed tile set for the round, so a
 * placed tile just fades out in place instead of the rest of the grid reflowing around the gap.
 */
function bestGrid(count: number, width: number, height: number): { columns: number; cell: number } {
  let best = { columns: 1, cell: 0 };
  for (let columns = 1; columns <= count; columns++) {
    const rows = Math.ceil(count / columns);
    const cell = Math.min((width - TILE_GAP * (columns - 1)) / columns, (height - TILE_GAP * (rows - 1)) / rows);
    if (cell > best.cell) best = { columns, cell };
  }
  // Only ever shrink from the ideal, never grow past it.
  return { columns: best.columns, cell: Math.max(0, Math.min(best.cell, TILE_MAX)) };
}

export function renderGame(game: Game, onHome: () => void): { element: HTMLElement; dispose: () => void } {
  const pictureSlot = h('div', { class: 'picture-slot' });
  const wordText = h('div', { class: 'word-text' });
  const slots = h('div', { class: 'slots', 'aria-live': 'polite' });
  const tileGrid = h('div', { class: 'tile-grid' });
  const doneMessage = h('div', { class: 'done-message', hidden: true });
  const playArea = h('div', { class: 'play-area' }, tileGrid, doneMessage);
  const confetti = h('canvas', { class: 'confetti', 'aria-hidden': 'true' });

  const element = h(
    'main',
    { class: 'game' },
    h('button', { class: 'icon-button home-button', 'aria-label': 'Home', onclick: onHome }, '🏠'),
    pictureSlot,
    wordText,
    h('button', { class: 'pill-button hear-button', onclick: () => game.repeatWord() }, '🔊 Hear it again'),
    slots,
    playArea,
    confetti,
  );

  let tileButtons = new Map<number, HTMLButtonElement>();
  let slotElements: HTMLElement[] = [];
  let previous: GameState | null = null;

  function layout(): void {
    const state = game.state;
    const count = letters(state.word).length;
    const slotSize = Math.min(SLOT_MAX, (slots.clientWidth - SLOT_GAP * (count - 1)) / count);
    slots.style.setProperty('--slot-size', `${slotSize}px`);

    const { columns, cell } = bestGrid(state.tiles.length, playArea.clientWidth, playArea.clientHeight);
    tileGrid.style.setProperty('--columns', String(columns));
    tileGrid.style.setProperty('--cell', `${cell}px`);
  }

  function buildRound(state: GameState): void {
    pictureSlot.replaceChildren(wordPicture(state.word));
    wordText.textContent = state.level.showWordText ? state.word.text : '';
    wordText.hidden = !state.level.showWordText;

    slotElements = letters(state.word).map(() => h('div', { class: 'slot' }));
    slots.replaceChildren(...slotElements);

    tileButtons = new Map(
      state.tiles.map((tile) => [
        tile.id,
        h(
          'button',
          {
            class: 'tile',
            'aria-label': tile.letter,
            onclick: () => game.onTileTapped(tile.id),
            onanimationend: () => game.clearWrongTile(),
          },
          tile.letter,
        ),
      ]),
    );
    tileGrid.replaceChildren(...tileButtons.values());
    tileGrid.hidden = false;
    doneMessage.hidden = true;
    layout();
  }

  function update(state: GameState): void {
    if (!previous || state.tiles !== previous.tiles) buildRound(state);

    const tilesById = new Map(state.tiles.map((tile) => [tile.id, tile]));
    slotElements.forEach((slot, index) => {
      const placedId = state.placedTileIds[index];
      const letter = placedId === undefined ? '' : tilesById.get(placedId)!.letter;
      if (slot.textContent !== letter) {
        slot.textContent = letter;
        slot.classList.toggle('filled', letter !== '');
      }
    });

    for (const [id, button] of tileButtons) {
      const placed = state.placedTileIds.includes(id);
      button.classList.toggle('placed', placed);
      button.disabled = placed;
      if (id !== state.wrongTileId) {
        button.classList.remove('shake');
      } else if (state !== previous) {
        // Restart the shake even if this same tile was already mid-shake.
        button.classList.remove('shake');
        void button.offsetWidth;
        button.classList.add('shake');
      }
    }

    if (state.isWordComplete && !previous?.isWordComplete) {
      tileGrid.hidden = true;
      doneMessage.hidden = false;
      doneMessage.textContent = `🎉 ${state.word.text}! 🎉`;
      playConfetti(confetti);
    }
    previous = state;
  }

  game.onChange(update);
  const resizeObserver = new ResizeObserver(layout);
  resizeObserver.observe(playArea);
  queueMicrotask(() => update(game.state));

  return {
    element,
    dispose: () => {
      resizeObserver.disconnect();
      game.dispose();
    },
  };
}

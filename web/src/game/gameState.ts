import { letters, type Level, type Word } from '../data/levels';

export interface LetterTile {
  id: number;
  letter: string;
}

export interface GameState {
  level: Level;
  word: Word;
  tiles: LetterTile[];
  /** Tile ids in the order the child placed them — the spelling attempt so far. */
  placedTileIds: number[];
  /** The tile last tapped out of order; drives the shake animation until consumed. */
  wrongTileId: number | null;
  isWordComplete: boolean;
}

/**
 * Pure tap evaluation, no side effects — safe to unit test without a DOM. Ignores taps on an
 * already-placed tile or after the word is already complete (returns the same state object).
 */
export function evaluateTap(state: GameState, tileId: number): GameState {
  if (state.isWordComplete || state.placedTileIds.includes(tileId)) return state;
  const tappedTile = state.tiles.find((tile) => tile.id === tileId);
  if (!tappedTile) return state;

  const wordLetters = letters(state.word);
  const nextRequiredLetter = wordLetters[state.placedTileIds.length];
  if (tappedTile.letter === nextRequiredLetter) {
    const placed = [...state.placedTileIds, tileId];
    return { ...state, placedTileIds: placed, wrongTileId: null, isWordComplete: placed.length === wordLetters.length };
  }
  return { ...state, wrongTileId: tileId };
}

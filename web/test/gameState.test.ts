import { describe, expect, it } from 'vitest';
import { levelById } from '../src/data/levels';
import { evaluateTap, type GameState } from '../src/game/gameState';

// Mirrors app/src/test/.../GameViewModelTest.kt.
// "cat" -> C(0), A(1), T(2), plus one distractor 'Z'(3), in shuffled tile order.
const tileC = { id: 0, letter: 'C' };
const tileA = { id: 1, letter: 'A' };
const tileT = { id: 2, letter: 'T' };
const tileZ = { id: 3, letter: 'Z' };
const initialState: GameState = {
  level: levelById('FIRST_WORDS'),
  word: { text: 'cat', emoji: '🐱' },
  tiles: [tileC, tileA, tileT, tileZ],
  placedTileIds: [],
  wrongTileId: null,
  isWordComplete: false,
};

function spellCat(): GameState {
  return [tileC, tileA, tileT].reduce((state, tile) => evaluateTap(state, tile.id), initialState);
}

describe('evaluateTap', () => {
  it('places the correct next letter in order', () => {
    const result = evaluateTap(initialState, tileC.id);
    expect(result.placedTileIds).toEqual([tileC.id]);
    expect(result.wrongTileId).toBeNull();
    expect(result.isWordComplete).toBe(false);
  });

  it('sets wrongTileId for a wrong letter without changing placed letters', () => {
    const result = evaluateTap(initialState, tileZ.id);
    expect(result.placedTileIds).toEqual([]);
    expect(result.wrongTileId).toBe(tileZ.id);
  });

  it('rejects a correct letter tapped out of order', () => {
    // 'T' is a real letter in "cat" but it's not next — 'C' is.
    const result = evaluateTap(initialState, tileT.id);
    expect(result.placedTileIds).toEqual([]);
    expect(result.wrongTileId).toBe(tileT.id);
  });

  it('treats tapping an already-placed tile as a no-op', () => {
    const afterC = evaluateTap(initialState, tileC.id);
    expect(evaluateTap(afterC, tileC.id)).toBe(afterC);
  });

  it('marks the word complete once fully spelled', () => {
    const state = spellCat();
    expect(state.isWordComplete).toBe(true);
    expect(state.placedTileIds).toEqual([tileC.id, tileA.id, tileT.id]);
  });

  it('ignores taps after word completion', () => {
    const completed = spellCat();
    expect(evaluateTap(completed, tileZ.id)).toBe(completed);
  });
});

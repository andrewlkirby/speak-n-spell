import { describe, expect, it } from 'vitest';
import { letters, levelById, levels, type Word } from '../src/data/levels';
import { seededRandom } from '../src/data/random';
import { shuffledWordsFor, tileLetters } from '../src/data/wordRepository';

// Mirrors app/src/test/.../WordRepositoryTest.kt.
function counts(items: string[]): Map<string, number> {
  const result = new Map<string, number>();
  for (const item of items) result.set(item, (result.get(item) ?? 0) + 1);
  return result;
}

const FIRST_WORDS = levelById('FIRST_WORDS');
const SPELLER = levelById('SPELLER');

describe('wordRepository', () => {
  it('every level has words', () => {
    for (const level of levels) expect(level.words.length).toBeGreaterThan(0);
  });

  it('tileLetters contains every required letter including duplicates', () => {
    const word: Word = { text: 'bee', emoji: '🐝' }; // repeated letter
    const tileCounts = counts(tileLetters(word, FIRST_WORDS, seededRandom(1)));
    for (const [letter, count] of counts(letters(word))) {
      expect(tileCounts.get(letter) ?? 0).toBeGreaterThanOrEqual(count);
    }
  });

  it('fixed distractor levels add exactly distractorCount extra tiles', () => {
    const word: Word = { text: 'cat', emoji: '🐱' };
    const tiles = tileLetters(word, FIRST_WORDS, seededRandom(1));
    expect(tiles).toHaveLength(3 + FIRST_WORDS.distractorCount!);
  });

  it('full-set levels fill every unused letter of the alphabet', () => {
    const word: Word = { text: 'rabbit', emoji: '🐰' }; // 5 unique letters: r,a,b,i,t
    const tiles = tileLetters(word, SPELLER, seededRandom(1));
    expect(tiles).toHaveLength(6 + (26 - 5));
  });

  it('distractors never add an extra copy of a required letter', () => {
    const word: Word = { text: 'dragon', emoji: '🐉' };
    const tileCounts = counts(tileLetters(word, SPELLER, seededRandom(1)));
    for (const [letter, count] of counts(letters(word))) {
      expect(tileCounts.get(letter), `letter '${letter}'`).toBe(count);
    }
  });

  it('shuffledWordsFor returns the same words in a different order', () => {
    const level = levelById('GROWING');
    const shuffled = shuffledWordsFor(level, seededRandom(42));
    expect(shuffled).toHaveLength(level.words.length);
    expect(new Set(shuffled)).toEqual(new Set(level.words));
    expect(shuffled).not.toEqual(level.words);
  });
});

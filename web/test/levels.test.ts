import { describe, expect, it } from 'vitest';
import { images, levels, type LevelId } from '../src/data/levels';

// Mirrors app/src/test/.../LevelsTest.kt, run against the word list extracted from Levels.kt.
const letterCountRanges: Record<LevelId, [number, number]> = {
  FIRST_WORDS: [3, 3],
  GROWING: [4, 4],
  SPELLER: [5, 7],
  CHALLENGE: [7, 11],
};

function duplicates(values: string[]): string[] {
  return values.filter((value, index) => values.indexOf(value) !== index);
}

describe('levels', () => {
  it('has the four levels in order', () => {
    expect(levels.map((l) => l.id)).toEqual(['FIRST_WORDS', 'GROWING', 'SPELLER', 'CHALLENGE']);
  });

  it.each(levels)("every $id word is within the level's letter-count range", (level) => {
    const [min, max] = letterCountRanges[level.id];
    for (const word of level.words) {
      expect(word.text.length, `${level.id}: '${word.text}'`).toBeGreaterThanOrEqual(min);
      expect(word.text.length, `${level.id}: '${word.text}'`).toBeLessThanOrEqual(max);
    }
  });

  it.each(levels)('$id has no duplicate words or emoji', (level) => {
    expect(duplicates(level.words.map((w) => w.text))).toEqual([]);
    expect(duplicates(level.words.map((w) => w.emoji))).toEqual([]);
  });

  it.each(levels)('$id has 100 words', (level) => {
    expect(level.words).toHaveLength(100);
  });

  it('every custom illustration was converted to SVG', () => {
    for (const word of levels.flatMap((l) => l.words).filter((w) => w.image)) {
      expect(images[word.image!], word.image).toMatch(/^<svg [^>]*viewBox=.*<\/svg>$/);
    }
  });
});

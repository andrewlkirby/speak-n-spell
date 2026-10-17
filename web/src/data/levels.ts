import data from '../generated/words.json';

// Generated from the Android app's Levels.kt by scripts/extract-words.mjs — edit word lists there.

export interface Word {
  text: string;
  emoji: string;
  /** Optional custom illustration (key into [images]) shown instead of [emoji] when set —
   * for words where no single emoji unambiguously depicts the word. */
  image?: string;
}

export type LevelId = 'FIRST_WORDS' | 'GROWING' | 'SPELLER' | 'CHALLENGE';

export interface Level {
  id: LevelId;
  displayName: string;
  icon: string;
  showWordText: boolean;
  /** Extra wrong-letter tiles mixed in with the correct ones; null = fill the full alphabet. */
  distractorCount: number | null;
  words: Word[];
}

export const levels: Level[] = data.levels as Level[];

/** Inline SVG markup for custom illustrations, keyed by Android drawable name. */
export const images: Record<string, string> = data.images;

export function levelById(id: string | undefined): Level {
  return levels.find((level) => level.id === id) ?? levels[0];
}

export function letters(word: Word): string[] {
  return [...word.text.toUpperCase()];
}

import { letters, type Level, type Word } from './levels';
import { shuffled, type Random } from './random';

const ALPHABET = [...'ABCDEFGHIJKLMNOPQRSTUVWXYZ'];

/** A play-session word order: same words, shuffled. */
export function shuffledWordsFor(level: Level, random: Random = Math.random): Word[] {
  return shuffled(level.words, random);
}

/**
 * The letter tiles for one round: [word]'s own letters (in their required multiplicity)
 * plus wrong-letter distractors per [Level.distractorCount], all shuffled together.
 * A null distractorCount ("full set") fills every unused letter of the alphabet.
 */
export function tileLetters(word: Word, level: Level, random: Random = Math.random): string[] {
  const required = letters(word);
  const distractorPool = shuffled(
    ALPHABET.filter((letter) => !required.includes(letter)),
    random,
  );
  const distractorCount = Math.min(level.distractorCount ?? distractorPool.length, distractorPool.length);
  return shuffled([...required, ...distractorPool.slice(0, distractorCount)], random);
}

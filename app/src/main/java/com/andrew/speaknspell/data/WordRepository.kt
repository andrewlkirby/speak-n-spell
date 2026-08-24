package com.andrew.speaknspell.data

import kotlin.random.Random

class WordRepository {
    fun wordsFor(level: Level): List<Word> = wordsByLevel.getValue(level)

    /** A play-session word order: same words, shuffled. */
    fun shuffledWordsFor(level: Level, random: Random = Random.Default): List<Word> =
        wordsFor(level).shuffled(random)

    /**
     * The letter tiles for one round: [word]'s own letters (in their required multiplicity)
     * plus wrong-letter distractors per [Level.distractorCount], all shuffled together.
     * A null distractorCount ("full set") fills every unused letter of the alphabet.
     */
    fun tileLetters(word: Word, level: Level, random: Random = Random.Default): List<Char> {
        val required = word.letters
        val distractorPool = ('A'..'Z').filterNot { it in required }.shuffled(random)

        val distractorCount = level.distractorCount?.coerceAtMost(distractorPool.size)
            ?: distractorPool.size

        return (required + distractorPool.take(distractorCount)).shuffled(random)
    }
}

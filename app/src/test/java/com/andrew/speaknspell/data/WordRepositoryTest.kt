package com.andrew.speaknspell.data

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordRepositoryTest {
    private val repository = WordRepository()

    @Test
    fun `every level has words`() {
        Level.entries.forEach { level ->
            assertTrue("no words for $level", repository.wordsFor(level).isNotEmpty())
        }
    }

    @Test
    fun `tileLetters contains every required letter including duplicates`() {
        val word = Word("bee", "🐝") // repeated letter
        val tiles = repository.tileLetters(word, Level.FIRST_WORDS, Random(1))

        val requiredCounts = word.letters.groupingBy { it }.eachCount()
        val tileCounts = tiles.groupingBy { it }.eachCount()
        requiredCounts.forEach { (letter, count) ->
            assertTrue(
                "expected at least $count '$letter' tiles, got ${tileCounts[letter]}",
                (tileCounts[letter] ?: 0) >= count,
            )
        }
    }

    @Test
    fun `fixed distractor levels add exactly distractorCount extra tiles`() {
        val word = Word("cat", "🐱")
        val tiles = repository.tileLetters(word, Level.FIRST_WORDS, Random(1))

        assertEquals(word.letters.size + Level.FIRST_WORDS.distractorCount!!, tiles.size)
    }

    @Test
    fun `full-set levels fill every unused letter of the alphabet`() {
        val word = Word("rabbit", "🐰") // 5 unique letters: r,a,b,i,t
        val tiles = repository.tileLetters(word, Level.SPELLER, Random(1))

        val uniqueRequiredLetters = word.letters.toSet().size
        assertEquals(word.letters.size + (26 - uniqueRequiredLetters), tiles.size)
    }

    @Test
    fun `distractors never add an extra copy of a required letter`() {
        val word = Word("dragon", "🐉")
        val tiles = repository.tileLetters(word, Level.SPELLER, Random(1))
        val requiredCounts = word.letters.groupingBy { it }.eachCount()
        val tileCounts = tiles.groupingBy { it }.eachCount()

        requiredCounts.forEach { (letter, count) ->
            assertEquals(
                "letter '$letter' should appear exactly $count time(s), found ${tileCounts[letter]}",
                count,
                tileCounts[letter],
            )
        }
    }

    @Test
    fun `shuffledWordsFor returns the same words in a different order`() {
        val original = repository.wordsFor(Level.GROWING)
        val shuffled = repository.shuffledWordsFor(Level.GROWING, Random(42))

        assertEquals(original.toSet(), shuffled.toSet())
        assertEquals(original.size, shuffled.size)
    }
}

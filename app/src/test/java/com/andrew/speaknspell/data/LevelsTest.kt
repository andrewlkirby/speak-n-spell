package com.andrew.speaknspell.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelsTest {
    private val letterCountRanges = mapOf(
        Level.FIRST_WORDS to 3..3,
        Level.GROWING to 4..4,
        Level.SPELLER to 5..7,
        Level.CHALLENGE to 7..11,
    )

    @Test
    fun `every word is within its level's letter-count range`() {
        wordsByLevel.forEach { (level, words) ->
            val range = letterCountRanges.getValue(level)
            words.forEach { word ->
                assertTrue(
                    "${level.name}: '${word.text}' has ${word.text.length} letters, expected $range",
                    word.text.length in range,
                )
            }
        }
    }

    @Test
    fun `no duplicate words within a level`() {
        wordsByLevel.forEach { (level, words) ->
            val duplicates = words.groupingBy { it.text }.eachCount().filterValues { it > 1 }
            assertTrue("${level.name}: duplicate words $duplicates", duplicates.isEmpty())
        }
    }

    @Test
    fun `no duplicate emoji within a level`() {
        wordsByLevel.forEach { (level, words) ->
            val duplicates = words.groupingBy { it.emoji }.eachCount().filterValues { it > 1 }
            assertTrue("${level.name}: duplicate emoji $duplicates", duplicates.isEmpty())
        }
    }

    @Test
    fun `every level has 100 words`() {
        wordsByLevel.forEach { (level, words) ->
            assertEquals("${level.name} word count", 100, words.size)
        }
    }
}

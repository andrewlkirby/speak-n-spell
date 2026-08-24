package com.andrew.speaknspell.game

import com.andrew.speaknspell.data.Level
import com.andrew.speaknspell.data.Word
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {
    // "cat" -> C(0), A(1), T(2), plus one distractor 'Z'(3), in shuffled tile order.
    private val word = Word("cat", "🐱")
    private val tileC = LetterTile(0, 'C')
    private val tileA = LetterTile(1, 'A')
    private val tileT = LetterTile(2, 'T')
    private val tileZ = LetterTile(3, 'Z')
    private val initialState = GameUiState(
        level = Level.FIRST_WORDS,
        word = word,
        tiles = listOf(tileC, tileA, tileT, tileZ),
    )

    @Test
    fun `correct next letter is placed in order`() {
        val result = GameViewModel.evaluateTap(initialState, tileC.id)

        assertEquals(listOf(tileC.id), result.placedTileIds)
        assertNull(result.wrongTileId)
        assertTrue(!result.isWordComplete)
    }

    @Test
    fun `wrong letter tile sets wrongTileId without changing placed letters`() {
        val result = GameViewModel.evaluateTap(initialState, tileZ.id)

        assertEquals(emptyList<Int>(), result.placedTileIds)
        assertEquals(tileZ.id, result.wrongTileId)
    }

    @Test
    fun `correct letter tapped out of order is rejected`() {
        // 'T' is a real letter in "cat" but it's not next — 'C' is.
        val result = GameViewModel.evaluateTap(initialState, tileT.id)

        assertEquals(emptyList<Int>(), result.placedTileIds)
        assertEquals(tileT.id, result.wrongTileId)
    }

    @Test
    fun `tapping an already-placed tile is a no-op`() {
        val afterC = GameViewModel.evaluateTap(initialState, tileC.id)
        val tappedAgain = GameViewModel.evaluateTap(afterC, tileC.id)

        assertEquals(afterC, tappedAgain)
    }

    @Test
    fun `spelling the full word marks it complete`() {
        var state = initialState
        state = GameViewModel.evaluateTap(state, tileC.id)
        state = GameViewModel.evaluateTap(state, tileA.id)
        state = GameViewModel.evaluateTap(state, tileT.id)

        assertTrue(state.isWordComplete)
        assertEquals(listOf(tileC.id, tileA.id, tileT.id), state.placedTileIds)
    }

    @Test
    fun `taps after word completion are ignored`() {
        var completed = initialState
        completed = GameViewModel.evaluateTap(completed, tileC.id)
        completed = GameViewModel.evaluateTap(completed, tileA.id)
        completed = GameViewModel.evaluateTap(completed, tileT.id)

        val result = GameViewModel.evaluateTap(completed, tileZ.id)

        assertEquals(completed, result)
    }
}

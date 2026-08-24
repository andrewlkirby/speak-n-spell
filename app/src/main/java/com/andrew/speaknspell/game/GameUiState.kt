package com.andrew.speaknspell.game

import com.andrew.speaknspell.data.Level
import com.andrew.speaknspell.data.Word

data class GameUiState(
    val level: Level,
    val word: Word,
    val tiles: List<LetterTile>,
    /** Tile ids in the order the child placed them — the spelling attempt so far. */
    val placedTileIds: List<Int> = emptyList(),
    /** The tile last tapped out of order; drives the shake animation until consumed. */
    val wrongTileId: Int? = null,
    val isWordComplete: Boolean = false,
)

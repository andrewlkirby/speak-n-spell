package com.andrew.speaknspell.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andrew.speaknspell.data.Word
import com.andrew.speaknspell.game.GameViewModel
import com.andrew.speaknspell.game.LetterTile
import kotlin.math.roundToInt

@Composable
fun GameScreen(viewModel: GameViewModel, onHome: () -> Unit, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WordPicture(word = uiState.word, size = 96.dp)

            if (uiState.level.showWordText) {
                Text(
                    text = uiState.word.text,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(Modifier.height(6.dp))

            Button(onClick = viewModel::repeatWord) {
                Text("🔊 Hear it again", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(10.dp))

            AnswerSlots(word = uiState.word, tiles = uiState.tiles, placedTileIds = uiState.placedTileIds)

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isWordComplete) {
                    Text(
                        text = "🎉 ${uiState.word.text}! 🎉",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    LetterTileGrid(
                        tiles = uiState.tiles,
                        placedTileIds = uiState.placedTileIds,
                        wrongTileId = uiState.wrongTileId,
                        onTileClick = viewModel::onTileTapped,
                        onWrongConsumed = viewModel::clearWrongTile,
                    )
                }
            }
        }

        ConfettiOverlay(trigger = uiState.isWordComplete, modifier = Modifier.fillMaxSize())

        IconButton(
            onClick = onHome,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        ) {
            Text("🏠", fontSize = 28.sp)
        }
    }
}

/** Shows the word's custom illustration if it has one, otherwise its emoji. */
@Composable
private fun WordPicture(word: Word, size: Dp) {
    val imageRes = word.imageRes
    if (imageRes != null) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size),
        )
    } else {
        Text(text = word.emoji, fontSize = size.value.sp)
    }
}

private val AnswerSlotSpacing = 6.dp

@Composable
private fun AnswerSlots(word: Word, tiles: List<LetterTile>, placedTileIds: List<Int>) {
    val tilesById = remember(tiles) { tiles.associateBy { it.id } }
    val placedLetters = placedTileIds.map { id -> tilesById.getValue(id).letter }
    val count = word.letters.size

    // Always a single row, however long the word — shrink to fit rather than wrap.
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val slotSize = ((maxWidth - AnswerSlotSpacing * (count - 1)) / count).coerceAtMost(56.dp)
        val letterFontSize = (slotSize.value * 0.55f).sp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AnswerSlotSpacing, Alignment.CenterHorizontally),
        ) {
            word.letters.indices.forEach { index ->
                val letter = placedLetters.getOrNull(index)
                Surface(
                    modifier = Modifier.size(slotSize),
                    shape = RoundedCornerShape(8.dp),
                    color = if (letter != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                ) {
                    val popProgress by animateFloatAsState(
                        targetValue = if (letter != null) 1f else 0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "letterPop",
                    )
                    Box(contentAlignment = Alignment.Center) {
                        if (letter != null) {
                            Text(
                                text = letter.toString(),
                                fontSize = letterFontSize,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier
                                    .scale(popProgress)
                                    .alpha(popProgress),
                            )
                        }
                    }
                }
            }
        }
    }
}

private val TileGridSpacing = 6.dp

/**
 * Every tile fits on screen at once, no scrolling — the column count and tile size are picked
 * to maximize tile size within the available area, since Speller/Challenge can hand us the
 * (nearly) full alphabet as one round's tile pool.
 *
 * The layout is computed from the full, fixed tile set for the round (not just the remaining
 * ones), so a tile's position never changes — placed tiles just fade out in place instead of
 * the rest of the grid reflowing around the gap.
 */
@Composable
private fun LetterTileGrid(
    tiles: List<LetterTile>,
    placedTileIds: List<Int>,
    wrongTileId: Int?,
    onTileClick: (Int) -> Unit,
    onWrongConsumed: () -> Unit,
) {
    if (tiles.isEmpty()) return

    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        var bestColumns = 1
        var bestCellSize = 0.dp
        for (columns in 1..tiles.size) {
            val rows = (tiles.size + columns - 1) / columns
            val cellFromWidth = (maxWidth - TileGridSpacing * (columns - 1)) / columns
            val cellFromHeight = (maxHeight - TileGridSpacing * (rows - 1)) / rows
            val cellSize = minOf(cellFromWidth, cellFromHeight)
            if (cellSize > bestCellSize) {
                bestCellSize = cellSize
                bestColumns = columns
            }
        }
        // Only ever shrink from the ideal, never grow past it — a forced minimum here could
        // make the grid overflow the available space (e.g. the short height of landscape).
        val cellSize = bestCellSize.coerceAtMost(72.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(TileGridSpacing, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            tiles.chunked(bestColumns).forEach { rowTiles ->
                Row(horizontalArrangement = Arrangement.spacedBy(TileGridSpacing, Alignment.CenterHorizontally)) {
                    rowTiles.forEach { tile ->
                        LetterTileButton(
                            tile = tile,
                            size = cellSize,
                            placed = tile.id in placedTileIds,
                            isWrong = tile.id == wrongTileId,
                            onWrongConsumed = onWrongConsumed,
                            onClick = { onTileClick(tile.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterTileButton(
    tile: LetterTile,
    size: Dp,
    placed: Boolean,
    isWrong: Boolean,
    onWrongConsumed: () -> Unit,
    onClick: () -> Unit,
) {
    val shakeOffset = remember { Animatable(0f) }
    val alpha by animateFloatAsState(targetValue = if (placed) 0f else 1f, label = "tileFade")

    LaunchedEffect(isWrong) {
        if (isWrong) {
            shakeOffset.animateTo(-16f, tween(60))
            shakeOffset.animateTo(16f, tween(80))
            shakeOffset.animateTo(-12f, tween(80))
            shakeOffset.animateTo(0f, tween(60))
            onWrongConsumed()
        }
    }

    Surface(
        onClick = onClick,
        enabled = !placed,
        modifier = Modifier
            .size(size)
            .padding(2.dp)
            .alpha(alpha)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
        tonalElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = tile.letter.toString(),
                fontSize = (size.value * 0.5f).sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

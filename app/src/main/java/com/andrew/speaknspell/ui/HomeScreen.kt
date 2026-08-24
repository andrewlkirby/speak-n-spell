package com.andrew.speaknspell.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrew.speaknspell.data.Level
import com.andrew.speaknspell.data.Progress
import com.andrew.speaknspell.ui.theme.BerryRed
import com.andrew.speaknspell.ui.theme.GrapePurple
import com.andrew.speaknspell.ui.theme.SkyTeal
import com.andrew.speaknspell.ui.theme.SunOrange

private val Level.accentColor: Color
    get() = when (this) {
        Level.FIRST_WORDS -> SunOrange
        Level.GROWING -> SkyTeal
        Level.SPELLER -> GrapePurple
        Level.CHALLENGE -> BerryRed
    }

/** The level picker — the app's entry point. No reading required: level cards are icon + color led. */
@Composable
fun HomeScreen(
    progress: Progress,
    onSpeak: (String) -> Unit,
    onLevelSelected: (Level) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        onSpeak("Pick a level to play!")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "🐱 Speak & Spell 🐶",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(Level.entries) { level ->
                LevelCard(
                    level = level,
                    stars = progress.starsByLevel[level] ?: 0,
                    onClick = {
                        onSpeak(level.displayName)
                        onLevelSelected(level)
                    },
                )
            }
        }
    }
}

@Composable
private fun LevelCard(level: Level, stars: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(width = 160.dp, height = 180.dp),
        shape = RoundedCornerShape(20.dp),
        color = level.accentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = level.icon, fontSize = 64.sp)
            Text(
                text = level.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            if (stars > 0) {
                Text(
                    text = "⭐ $stars",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
            }
        }
    }
}

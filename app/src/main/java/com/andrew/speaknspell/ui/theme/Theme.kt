package com.andrew.speaknspell.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SpeakNSpellLightColorScheme = lightColorScheme(
    primary = SunOrange,
    onPrimary = CardWhite,
    secondary = SkyTeal,
    onSecondary = CardWhite,
    tertiary = GrapePurple,
    onTertiary = CardWhite,
    background = CreamBackground,
    onBackground = InkText,
    surface = CardWhite,
    onSurface = InkText,
    error = BerryRed,
    onError = CardWhite,
)

// Same vivid accent colors as the light theme — only the backdrop inverts — so tiles and
// buttons stay just as legible for a pre-reader in dark mode as they are in light mode.
private val SpeakNSpellDarkColorScheme = darkColorScheme(
    primary = SunOrange,
    onPrimary = NightBackground,
    secondary = SkyTeal,
    onSecondary = NightBackground,
    tertiary = GrapePurple,
    onTertiary = NightText,
    background = NightBackground,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    error = BerryRed,
    onError = NightBackground,
)

/**
 * Manually toggled via Settings (not tied to system dark mode) — a dimmed UI could hurt
 * legibility for a pre-reader, so it's always an explicit opt-in, never automatic.
 */
@Composable
fun SpeakNSpellTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) SpeakNSpellDarkColorScheme else SpeakNSpellLightColorScheme,
        typography = SpeakNSpellTypography,
        content = content,
    )
}

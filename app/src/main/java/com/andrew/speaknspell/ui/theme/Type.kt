package com.andrew.speaknspell.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Sizes scaled well above Material3 defaults: a 3-year-old (and their parent
// across the room) need to read every label at a glance.
val SpeakNSpellTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 96.sp, lineHeight = 104.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Black, fontSize = 72.sp, lineHeight = 80.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 56.sp, lineHeight = 64.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 52.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 28.sp),
)

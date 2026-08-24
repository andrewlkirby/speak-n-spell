package com.andrew.speaknspell.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.andrew.speaknspell.ui.theme.BerryRed
import com.andrew.speaknspell.ui.theme.GrapePurple
import com.andrew.speaknspell.ui.theme.LeafGreen
import com.andrew.speaknspell.ui.theme.LemonYellow
import com.andrew.speaknspell.ui.theme.SkyTeal
import com.andrew.speaknspell.ui.theme.SunOrange
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val ConfettiPalette = listOf(SunOrange, SkyTeal, GrapePurple, LemonYellow, LeafGreen, BerryRed)

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val spinsPerSecond: Float,
) {
    companion object {
        fun burst(count: Int, random: Random): List<ConfettiParticle> = List(count) {
            ConfettiParticle(
                angle = random.nextFloat() * (2f * Math.PI.toFloat()),
                speed = 260f + random.nextFloat() * 260f,
                color = ConfettiPalette.random(random),
                size = 10f + random.nextFloat() * 10f,
                spinsPerSecond = 1f + random.nextFloat() * 3f,
            )
        }
    }
}

/**
 * A one-shot confetti burst from the center of [modifier]'s bounds, replayed every time
 * [trigger] flips to true (word completed). Hand-rolled on Canvas — no animation library.
 */
@Composable
fun ConfettiOverlay(trigger: Boolean, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    var visible by remember { mutableStateOf(false) }
    val particles = remember { ConfettiParticle.burst(count = 28, random = Random.Default) }

    LaunchedEffect(trigger) {
        if (trigger) {
            visible = true
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 1100, easing = LinearEasing))
            visible = false
        }
    }

    if (visible) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val origin = Offset(size.width / 2f, size.height * 0.4f)
            val t = progress.value
            particles.forEach { particle -> drawParticle(particle, t, origin) }
        }
    }
}

private fun DrawScope.drawParticle(
    particle: ConfettiParticle,
    t: Float,
    origin: Offset,
) {
    if (t <= 0f) return
    val dx = cos(particle.angle) * particle.speed * t
    val gravity = 500f * t * t
    val dy = sin(particle.angle) * particle.speed * t + gravity
    val alpha = (1f - t).coerceIn(0f, 1f)

    withTransform({
        translate(origin.x + dx, origin.y + dy)
        rotate(degrees = particle.spinsPerSecond * t * 360f, pivot = Offset.Zero)
    }) {
        drawRect(
            color = particle.color.copy(alpha = alpha),
            topLeft = Offset(-particle.size / 2f, -particle.size / 2f),
            size = Size(particle.size, particle.size),
        )
    }
}

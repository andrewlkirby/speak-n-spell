package com.andrew.speaknspell.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private data class MathChallenge(val a: Int, val b: Int, val options: List<Int>) {
    val answer: Int get() = a + b
}

private fun randomChallenge(): MathChallenge {
    val a = Random.nextInt(2, 9)
    val b = Random.nextInt(2, 9)
    val answer = a + b
    val wrongOptions = generateSequence { Random.nextInt(4, 17) }
        .filter { it != answer }
        .distinct()
        .take(2)
        .toList()
    return MathChallenge(a, b, (wrongOptions + answer).shuffled())
}

/**
 * A quick addition problem — easy for an adult, unsolvable for a toddler — gating access to
 * Settings so a young child can't wander in and change/reset things.
 */
@Composable
fun ParentGateDialog(onUnlocked: () -> Unit, onDismiss: () -> Unit) {
    val challenge = remember { randomChallenge() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Parents Only", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "What is ${challenge.a} + ${challenge.b}?",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    challenge.options.forEach { option ->
                        Button(onClick = { if (option == challenge.answer) onUnlocked() }) {
                            Text(option.toString(), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

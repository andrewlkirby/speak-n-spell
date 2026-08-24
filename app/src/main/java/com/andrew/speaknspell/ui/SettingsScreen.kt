package com.andrew.speaknspell.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrew.speaknspell.data.Progress

private val SpeechRateOptions = listOf(0.6f to "🐢 Slow", 0.9f to "🙂 Normal", 1.2f to "🐇 Fast")

@Composable
fun SettingsDialog(
    progress: Progress,
    onDismiss: () -> Unit,
    onSetCorrectSound: (Boolean) -> Unit,
    onSetWrongSound: (Boolean) -> Unit,
    onSetCelebrateSound: (Boolean) -> Unit,
    onSetVibration: (Boolean) -> Unit,
    onSetTts: (Boolean) -> Unit,
    onSetPhonics: (Boolean) -> Unit,
    onSetSpeechRate: (Float) -> Unit,
    onSetDarkMode: (Boolean) -> Unit,
    onResetProgress: () -> Unit,
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingSwitchRow("🔊 Voice", progress.ttsEnabled, onSetTts)
                SettingSwitchRow("🔤 Phonics (say letters)", progress.phonicsEnabled, onSetPhonics)
                SettingSwitchRow("✅ Correct sound", progress.correctSoundEnabled, onSetCorrectSound)
                SettingSwitchRow("❌ Wrong sound", progress.wrongSoundEnabled, onSetWrongSound)
                SettingSwitchRow("🎉 Celebrate sound", progress.celebrateSoundEnabled, onSetCelebrateSound)
                SettingSwitchRow("📳 Vibration", progress.vibrationEnabled, onSetVibration)
                SettingSwitchRow("🌙 Dark mode", progress.darkModeEnabled, onSetDarkMode)

                HorizontalDivider()

                Text("Speech speed", style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpeechRateOptions.forEach { (rate, label) ->
                        val selected = progress.speechRate == rate
                        if (selected) {
                            Button(onClick = { onSetSpeechRate(rate) }) {
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            }
                        } else {
                            OutlinedButton(onClick = { onSetSpeechRate(rate) }) {
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                HorizontalDivider()

                if (showResetConfirm) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Reset all stars and settings? This can't be undone.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { showResetConfirm = false; onResetProgress() }) {
                                Text("Yes, reset", style = MaterialTheme.typography.labelLarge)
                            }
                            OutlinedButton(onClick = { showResetConfirm = false }) {
                                Text("Cancel", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                } else {
                    OutlinedButton(onClick = { showResetConfirm = true }) {
                        Text("🗑️ Reset Progress", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

@Composable
private fun SettingSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

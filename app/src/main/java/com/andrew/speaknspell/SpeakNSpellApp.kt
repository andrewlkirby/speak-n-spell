package com.andrew.speaknspell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.andrew.speaknspell.audio.SoundEffects
import com.andrew.speaknspell.data.Level
import com.andrew.speaknspell.data.Progress
import com.andrew.speaknspell.data.ProgressStore
import com.andrew.speaknspell.data.WordRepository
import com.andrew.speaknspell.game.GameViewModel
import com.andrew.speaknspell.tts.SpeechService
import com.andrew.speaknspell.tts.SpeechStatus
import com.andrew.speaknspell.ui.GameScreen
import com.andrew.speaknspell.ui.HomeScreen
import com.andrew.speaknspell.ui.ParentGateDialog
import com.andrew.speaknspell.ui.SettingsDialog
import com.andrew.speaknspell.ui.theme.SpeakNSpellTheme
import kotlinx.coroutines.launch

private const val ROUTE_HOME = "home"
private const val ROUTE_GAME = "game/{levelName}"

@Composable
fun SpeakNSpellApp(
    speechService: SpeechService,
    wordRepository: WordRepository,
    progressStore: ProgressStore,
    soundEffects: SoundEffects,
) {
    val status by speechService.status.collectAsState()
    val progress by progressStore.progress.collectAsState(initial = Progress())

    LaunchedEffect(progress.speechRate) {
        speechService.setSpeechRate(progress.speechRate)
    }

    SpeakNSpellTheme(darkTheme = progress.darkModeEnabled) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
        ) {
            when (status) {
                SpeechStatus.INITIALIZING -> CenteredMessage("Warming up voice…")
                SpeechStatus.UNAVAILABLE -> VoiceUnavailableMessage()
                SpeechStatus.READY -> ReadyContent(speechService, wordRepository, progressStore, soundEffects, progress)
            }
        }
    }
}

@Composable
private fun ReadyContent(
    speechService: SpeechService,
    wordRepository: WordRepository,
    progressStore: ProgressStore,
    soundEffects: SoundEffects,
    progress: Progress,
) {
    val scope = rememberCoroutineScope()
    fun speak(text: String) {
        if (progress.ttsEnabled) speechService.speak(text)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = ROUTE_HOME) {
            composable(ROUTE_HOME) {
                HomeScreen(
                    progress = progress,
                    onSpeak = ::speak,
                    onLevelSelected = { level ->
                        speak(level.displayName)
                        navController.navigate("game/${level.name}")
                    },
                )
            }
            composable(
                route = ROUTE_GAME,
                arguments = listOf(navArgument("levelName") { type = NavType.StringType }),
            ) { backStackEntry ->
                val levelName = backStackEntry.arguments?.getString("levelName")
                val level = Level.entries.firstOrNull { it.name == levelName } ?: Level.FIRST_WORDS

                val viewModel: GameViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            GameViewModel(
                                level = level,
                                wordRepository = wordRepository,
                                speechService = speechService,
                                progressStore = progressStore,
                                soundEffects = soundEffects,
                            )
                        }
                    },
                )
                GameScreen(viewModel = viewModel, onHome = { navController.popBackStack() })
            }
        }

        var showParentGate by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        IconButton(
            onClick = { showParentGate = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        ) {
            Text("⚙️", fontSize = 28.sp)
        }

        if (showParentGate) {
            ParentGateDialog(
                onUnlocked = { showParentGate = false; showSettings = true },
                onDismiss = { showParentGate = false },
            )
        }

        if (showSettings) {
            SettingsDialog(
                progress = progress,
                onDismiss = { showSettings = false },
                onSetCorrectSound = { enabled -> scope.launch { progressStore.setCorrectSoundEnabled(enabled) } },
                onSetWrongSound = { enabled -> scope.launch { progressStore.setWrongSoundEnabled(enabled) } },
                onSetCelebrateSound = { enabled -> scope.launch { progressStore.setCelebrateSoundEnabled(enabled) } },
                onSetVibration = { enabled -> scope.launch { progressStore.setVibrationEnabled(enabled) } },
                onSetTts = { enabled -> scope.launch { progressStore.setTtsEnabled(enabled) } },
                onSetPhonics = { enabled -> scope.launch { progressStore.setPhonicsEnabled(enabled) } },
                onSetSpeechRate = { rate -> scope.launch { progressStore.setSpeechRate(rate) } },
                onSetDarkMode = { enabled -> scope.launch { progressStore.setDarkModeEnabled(enabled) } },
                onResetProgress = { scope.launch { progressStore.reset() } },
            )
        }
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun VoiceUnavailableMessage() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No voice installed",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Button(onClick = { context.startActivity(SpeechService.installVoiceDataIntent()) }) {
            Text("Install voice data", style = MaterialTheme.typography.labelLarge)
        }
    }
}

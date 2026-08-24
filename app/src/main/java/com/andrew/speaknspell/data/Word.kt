package com.andrew.speaknspell.data

data class Word(
    val text: String,
    val emoji: String,
    /** Optional custom illustration (drawable resource id) shown instead of [emoji] when set —
     * for words where no single emoji unambiguously depicts the word. */
    val imageRes: Int? = null,
) {
    init {
        require(text.isNotBlank()) { "Word text must not be blank" }
    }

    val letters: List<Char> get() = text.uppercase().toList()
}

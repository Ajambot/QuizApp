package com.ClassActivity1.geoquiz

import androidx.annotation.StringRes


data class Question(
    val text: String,
    val answer: String,       // Correct answer letter: "A", "B", "C", "D"
    val options: List<String> // List of 4 options in order: A, B, C, D
)

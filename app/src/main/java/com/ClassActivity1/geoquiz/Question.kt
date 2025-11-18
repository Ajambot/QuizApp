package com.ClassActivity1.geoquiz

data class Question(
    val text: String,
    val answer: String,       // Correct answer letter: "A", "B", "C", "D"
    val options: List<String> // A, B, C, D in order
)

package com.ClassActivity1.geoquiz

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Sample data class for a question
    data class Question(
        val text: String,
        val options: List<String>,
        val correctIndex: Int
    )

    // Sample quiz questions
    private val questions = listOf(
        Question("What is Kotlin?", listOf("A language", "A compiler", "A framework", "A library"), 0),
        Question("Which company developed Kotlin?", listOf("Google", "JetBrains", "Microsoft", "Oracle"), 1),
        Question("What file extension does Kotlin use?", listOf(".java", ".kt", ".class", ".kot"), 1)
    )

    private var currentIndex = 0
    private var score = 0

    private lateinit var tvQuestion: TextView
    private lateinit var rgOptions: RadioGroup
    private lateinit var btnNext: Button
    private lateinit var options: List<RadioButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        tvQuestion = findViewById(R.id.tvQuestion)
        rgOptions = findViewById(R.id.rgOptions)
        btnNext = findViewById(R.id.btnNext)
        options = listOf(
            findViewById(R.id.rbOption1),
            findViewById(R.id.rbOption2),
            findViewById(R.id.rbOption3),
            findViewById(R.id.rbOption4)
        )

        loadQuestion()

        btnNext.setOnClickListener {
            checkAnswer()
            currentIndex++
            if (currentIndex < questions.size) {
                loadQuestion()
            } else {
                Toast.makeText(this, "Quiz complete! Score: $score/${questions.size}", Toast.LENGTH_LONG).show()
                btnNext.isEnabled = false
            }
        }
    }

    private fun loadQuestion() {
        val question = questions[currentIndex]
        tvQuestion.text = question.text
        for (i in options.indices) {
            options[i].text = question.options[i]
        }
        rgOptions.clearCheck()
    }

    private fun checkAnswer() {
        val selectedId = rgOptions.checkedRadioButtonId
        if (selectedId != -1) {
            val selectedIndex = options.indexOfFirst { it.id == selectedId }
            if (selectedIndex == questions[currentIndex].correctIndex) {
                score++
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show()
        }
    }
}
package com.ClassActivity1.geoquiz

import android.os.Bundle
import android.os.SystemClock
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ClassActivity1.geoquiz.databinding.ActivityMainBinding
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        var questionBank: MutableList<Question> = mutableListOf()
        var answerMs: Long = 20000L
        var randomOrder: Boolean = true
    }

    private var currentIndex = 0
    private var questionStartAt = 0L
    private var score = 0.0
    private var streak = 0.0
    private var hasAnswered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (questionBank.isEmpty()) {
            Toast.makeText(this, "No questions loaded!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (randomOrder) questionBank.shuffle()

        val buttons = listOf(binding.AButton, binding.BButton, binding.CButton, binding.DButton)
        buttons.forEach { btn -> btn.setOnClickListener { onOptionSelected(btn) } }

        binding.nextButton.setOnClickListener {
            hasAnswered = false
            currentIndex = (currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        binding.backButton.setOnClickListener {
            currentIndex = if (currentIndex > 0) currentIndex - 1 else questionBank.size - 1
            updateQuestion()
        }

        updateQuestion()
    }

    private fun updateQuestion() {
        val q = questionBank[currentIndex]
        binding.questionTextView.text = q.text
        binding.AButton.text = q.options[0]
        binding.BButton.text = q.options[1]
        binding.CButton.text = q.options[2]
        binding.DButton.text = q.options[3]

        if (!hasAnswered) questionStartAt = SystemClock.elapsedRealtime()
    }

    private fun onOptionSelected(button: RadioButton) {
        if (hasAnswered) return
        val selected = when (button.id) {
            R.id.AButton -> "A"
            R.id.BButton -> "B"
            R.id.CButton -> "C"
            R.id.DButton -> "D"
            else -> ""
        }
        checkQuestion(selected == questionBank[currentIndex].answer)
    }

    private fun checkQuestion(isCorrect: Boolean) {
        hasAnswered = true
        val elapsed = SystemClock.elapsedRealtime() - questionStartAt
        val remaining = (answerMs - elapsed).coerceAtLeast(0)
        val percentageTime = remaining.toDouble() / answerMs

        if (isCorrect && remaining > 0) {
            val points = ceil(percentageTime * 100.0 * (1.0 + streak / 10.0))
            score += points
            streak += 1
            Toast.makeText(
                this,
                " Correct! +${points.toInt()} | Streak: $streak",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            streak = 0.0
            Toast.makeText(
                this,
                if (remaining == 0L) "Time's up!" else " Incorrect",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
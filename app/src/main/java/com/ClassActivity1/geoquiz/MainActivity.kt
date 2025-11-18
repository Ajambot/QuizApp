package com.ClassActivity1.geoquiz

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ClassActivity1.geoquiz.databinding.ActivityMainBinding
import android.widget.TextView
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {

    // ViewBinding for activity_main.xml
    private lateinit var binding: ActivityMainBinding


    companion object {
        var numQuestions: Int = 0

        // This will be filled from HomeActivity after the CSV is loaded
        var questionBank: MutableList<Question> = mutableListOf()

        // How long the user has to answer each question (in ms)
        var answerMs: Long = 20_000L

        // If true, we shuffle question order once at the start
        var randomOrder: Boolean = true

        //How many questions to request/generate per prompt
        var questionsPerPrompt: Int=10
        //List of answer questions index to prevent re answering questions
        var answeredQuestions: MutableSet<Int> = mutableSetOf()
    }

    // Index of the current question in the questionBank
    private var currentIndex = 0

    // Single CountDownTimer instance for the current question
    private var countDownTimer: CountDownTimer? = null

    // Which choice (A/B/C/D) the user picked on this question
    private var selectedChoice: String? = null

    // Timestamp (in uptime ms) when the current question started
    private var questionStartAt = 0L

    // Total score across all questions in this run
    private var score: Double = 0.0

    // Streak of correct answers in a row; used as a multiplier
    private var streak = 0.0

    // True as soon as the user has answered (or time runs out) for this question
    private var hasAnswered = false

    // Tracks how many times user has moved backwards through questions
    // Used to prevent abusing forward navigation to re-answer
    private var backwards = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // If no questions were loaded (for testing), add 1 default question
        if (questionBank.isEmpty()) {
            questionBank = mutableListOf(
                Question(
                    text = "Test Question: No CSV or prompt provided.",
                    answer = "A",
                    options = listOf("Option A", "Option B", "Option C", "Option D")
                )
            )
        }

        // Randomize questions if setting is turned on
        if (randomOrder) {
            questionBank.shuffle()
        }


        // Initial UI setup
        binding.scoreTextView.text = "Score: 0"
        binding.feedbackTextView.text = ""

        // Hook up the answer buttons (A/B/C/D)
        binding.AButton.setOnClickListener { onChoiceClicked("A") }
        binding.BButton.setOnClickListener { onChoiceClicked("B") }
        binding.CButton.setOnClickListener { onChoiceClicked("C") }
        binding.DButton.setOnClickListener { onChoiceClicked("D") }

        // "Next" button: move forward through questions
        binding.nextButton.setOnClickListener {
            // If user was going backwards, reduce that counter until we reach 0 again
            if (currentIndex < numQuestions - 1) {
                currentIndex++
                hasAnswered = false

                // Move to next question, wrap around if at end
                updateQuestion()
            }
        }

        // "Back" button: go to previous question
        binding.backButton.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                hasAnswered = true
                updateQuestion()
            }
        }

        // Exit button: confirm before closing the quiz
        binding.exitButton.setOnClickListener {
            showExitDialog()
        }

        // Load the very first question
        updateQuestion()
    }

    // Pops up a confirmation dialog when the user tries to leave the quiz
    private fun showExitDialog() {
        val dialog= AlertDialog.Builder(this)
            .setTitle("Exit Quiz")
            .setMessage("Are you sure you want to exit the quiz?")
            .setPositiveButton("Exit") { _, _ -> //using lambda with no paramaters or values, hence _,_
                // Stop timer and close the activity
                countDownTimer?.cancel()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
        //makes message black
        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
        dialog.findViewById<TextView>(android.R.id.title)?.setTextColor(Color.BLACK)

        //makes buttons black
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.RED)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.GRAY)
    }

    // Called when the user taps one of the answer choices
    private fun onChoiceClicked(letter: String) {
        // Visually mark that choice as selected
        selected(letter)
        // Check whether it's correct or not
        checkQuestion(letter)
    }

    // Enable/disable all four answer buttons at once
    private fun setOptions(enabled: Boolean) {
        listOf(binding.AButton, binding.BButton, binding.CButton, binding.DButton).forEach {
            it.isEnabled = enabled
        }
    }

    // Update radio buttons so the chosen one appears selected
    private fun selected(choice: String) {
        selectedChoice = choice

        val map = mapOf(
            "A" to binding.AButton,
            "B" to binding.BButton,
            "C" to binding.CButton,
            "D" to binding.DButton,
        )

        // For each button, highlight only the one matching `choice`
        map.forEach { (key, btn) ->
            val isSelected = key == choice
            btn.isSelected = isSelected
            btn.isPressed = isSelected
            btn.isChecked = isSelected
            // Slightly fade out the non-selected options
            btn.alpha = if (isSelected) 1f else 0.6f
        }
    }

    // Starts (or restarts) the countdown for the current question
    private fun startTimer() {
        // Cancel any previous timer to avoid multiple timers running
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(answerMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Round up remaining time to seconds
                val secsLeft = ((millisUntilFinished + 999) / 1000).toInt()
                binding.TimerVar.text = "Time Left: ${secsLeft}s"
            }

            override fun onFinish() {
                // Timer reached zero
                binding.TimerVar.text = "Time Left: 0s"
                if (!hasAnswered) {
                    // If the user still hasn't answered, count as timed out
                    streak = 0.0
                    hasAnswered = true
                    setOptions(false)
                    binding.feedbackTextView.text = "Time's up!"
                    binding.feedbackTextView.setTextColor(Color.parseColor("#DC2626"))
                }
            }
        }.start()
    }

    // Load and display the current question on screen
    private fun updateQuestion() {
        val question = questionBank[currentIndex]

        // Set the question text
        binding.questionTextView.text = question.text

        // Fill in the four options, if present
        if (question.options.size >= 4) {
            binding.AButton.text = question.options[0]
            binding.BButton.text = question.options[1]
            binding.CButton.text = question.options[2]
            binding.DButton.text = question.options[3]
        }

        // Disable options if already answered
        val alreadyAnswered = answeredQuestions.contains(currentIndex)
        setOptions(!alreadyAnswered)

        if (!alreadyAnswered) {
            // If the user hasn’t already answered, start timing this question
            questionStartAt = SystemClock.elapsedRealtime()
            startTimer()

            // Reset selection state for the new question
            selectedChoice = null
            listOf(binding.AButton, binding.BButton, binding.CButton, binding.DButton).forEach {
                it.isSelected = false
                it.isPressed = false
                it.isChecked = false
                it.alpha = 1f
            }

            // Clear any previous "Correct/Incorrect" message
            binding.feedbackTextView.text = ""
            binding.feedbackTextView.setTextColor(Color.parseColor("#111827"))
            hasAnswered = false
        } else {
            // If already answered, show feedback and disable timer
            hasAnswered = true
            binding.feedbackTextView.text = "Already answered. Correct: ${question.answer}"
            binding.feedbackTextView.setTextColor(Color.parseColor("#16A34A"))
            binding.TimerVar.text = "Time Left: 0s"
        }
    }


    // Handles scoring and feedback for a user's answer
    private fun onUserAnswer(isCorrect: Boolean) {
        // Only handle the answer once
        if (!hasAnswered) {
            // Stop the timer so it doesn't keep ticking
            countDownTimer
            countDownTimer?.cancel()

            // How long the user took on this question
            val elapsed = SystemClock.elapsedRealtime() - questionStartAt
            var remaining = answerMs - elapsed
            //if (remaining + 2000L > answerMs) remaining = answerMs

            val secsLeft = ((remaining + 999) / 1000).toInt() //calculates seconds left (adds 999 so it always shows the second rounded up to the nearest int)
            if (remaining + 3000L > answerMs) remaining = answerMs //for calculating score we add 3 seconds to give them some leeway
            val percentageTime = (remaining * 1.0) / (answerMs * 1.0) //percentage of time left

            if (isCorrect && remaining > 0) {
                //Score formula:
                //base 100 points * percentage of time left (with the 3 second leeway) +10 extra per streak bonus
                val pointsEarned = percentageTime * 100.0 * (1.0 + streak / 10.0)
                score += pointsEarned
                streak += 1.0

                //Positive answer message sent to user
                binding.feedbackTextView.text =
                    "Correct! +${pointsEarned.toInt()} (Time left: ${secsLeft}s)"
                binding.feedbackTextView.setTextColor(Color.parseColor("#16A34A"))
            } else {
                //On timeout resets the streak and indicates the questions answered incorrectly
                streak = 0.0
                if (remaining.toInt() == 0) {
                    binding.feedbackTextView.text = "Time's up!"
                } else {
                    binding.feedbackTextView.text = "Incorrect, the answer was ${questionBank[currentIndex].answer}"
                }
                binding.feedbackTextView.setTextColor(Color.parseColor("#DC2626"))
            }

            //Update score display (makes it an integer)
            binding.scoreTextView.text = "Score: ${score.toInt()}"
        }
    }

    // Check if the selected letter matches the correct answer for this question
    private fun checkQuestion(choiceLetter: String) {
        // Don't re-check if we've already locked in this question
        if (hasAnswered) return

        val correctLetter = questionBank[currentIndex].answer
        // Compare letters ignoring case (in case CSV uses lower/upper)
        val isCorrect = correctLetter.equals(choiceLetter, ignoreCase = true)

        // This will update score, streak, and feedback text
        onUserAnswer(isCorrect)
        hasAnswered = true

        // Disable answer buttons so user can't change after seeing result
        setOptions(false)

        //Add question index to track answered questions
        answeredQuestions.add(currentIndex)
    }

    // Clean up timer if the activity is destroyed (e.g., user leaves)
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

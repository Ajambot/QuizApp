package com.ClassActivity1.geoquiz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.ClassActivity1.geoquiz.databinding.ActivityMainBinding

import kotlin.math.ceil
import android.os.SystemClock
import kotlin.reflect.typeOf

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        lateinit var questionBank:MutableList<Question>;
    }


    private var currentIndex = 0


    private val PICK_CSV_FILE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Timer

        binding.trueButton.setOnClickListener {
            checkQuestion(true)
        }

        binding.falseButton.setOnClickListener {
            checkQuestion(false)
        }

        binding.nextButton.setOnClickListener {
            if (backwards<0){
                backwards+=1
            }
            else{
                hasAnswered=false //to track if a questions been answered
            }
            currentIndex = (currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        binding.backButton.setOnClickListener {
            backwards-=1
            currentIndex = if (currentIndex > 0) (currentIndex - 1) else questionBank.size - 1
            updateQuestion()
        }

        binding.moreSecondsButton.setOnClickListener{
            answerMS+=5000L
            Toast.makeText(
                this,
                "Time Per Question: $answerMS",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.lessSecondsButton.setOnClickListener{
            if (answerMS>5000L) {
                answerMS -= 5000L
                Toast.makeText(
                    this,
                    "Time Per Question: $answerMS",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                Toast.makeText(
                    this,
                    "Cannot go below: $answerMS",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        binding.importButton.setOnClickListener {
            openFile()
        }


        updateQuestion()
    }

    private var answerMS = 20000L
    private var questionStartAt = 0L
    private var score:Double = 0.0
    private var streak = 0.0
    private var hasAnswered = false
    private var backwards = 0
    private fun updateQuestion() {
        val questionText = questionBank[currentIndex].text
        binding.questionTextView.text = questionText

        //TIMER
        if (!hasAnswered) {
            questionStartAt = SystemClock.elapsedRealtime()
        }
    }

    private fun onUserAnswer(choice:Boolean){
        if (!hasAnswered) {
            val elapsed = SystemClock.elapsedRealtime() - questionStartAt
            var remaining = answerMS - elapsed
            if (remaining + 2000L < 0) {
                remaining = 0
            }
            if (remaining + 2000L > answerMS) {
                remaining = answerMS
            }

            val secsLeft = ((remaining + 999) / 1000).toInt()
            val percentageTime = (remaining * 1.0) / (answerMS * 1.0)
            if (choice && remaining > 0) {
                val pointsEarned =
                    ceil(percentageTime) * 100.0 * (1.0 + streak / 10.0) //add 10% extra per streak
                score += pointsEarned
                streak += 1
                Toast.makeText(
                    this,
                    "Time Left: $secsLeft, Score: $score, Streak: $streak",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val pointsEarned = 0
                streak = 0.0 //set streak to 0 on incorrect answer
                if (remaining.toInt() == 0) {
                    Toast.makeText(this, "Ran Out of Time", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/comma-separated-values"
        }
        startActivityForResult(intent, PICK_CSV_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CSV_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                loadQuestionsFromCsv(uri)
            }
        }
    }

    private fun loadQuestionsFromCsv(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.bufferedReader()?.useLines { lines ->
            val newQuestions = lines
                .map { line ->
                    val lastCommaIndex = line.lastIndexOf(',')
                    val questionText = line.substring(0, lastCommaIndex).trim().removeSurrounding("\"")
                    val answer = line.substring(lastCommaIndex + 1).trim().toBoolean()
                    Question(questionText, answer)
                }
                .toList()
            questionBank.clear()
            questionBank.addAll(newQuestions)
            currentIndex = 0
            updateQuestion()
        }
    }
    private fun checkQuestion(choice:Boolean){
        if(questionBank[currentIndex].answer==choice){
            onUserAnswer(true)
            hasAnswered = true
            Toast.makeText(
                this,
                R.string.correct_toast,
                Toast.LENGTH_SHORT
            ).show()
        }
        else{
            onUserAnswer(false)
            hasAnswered=true
            Toast.makeText(
                this,
                R.string.incorrect_toast,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

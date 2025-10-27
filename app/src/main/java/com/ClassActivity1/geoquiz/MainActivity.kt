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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var questionBank = mutableListOf(
        Question("Is Australia a country and a continent?", true),
        Question("Is the Pacific Ocean the largest ocean in the world?", true),
        Question("Is the Suez Canal in the Middle East?", false),
        Question("Is the Sahara Desert in Africa?", false),
        Question("Are the Andes Mountains in the Americas?", true),
        Question("Is Mount Everest in Asia?", true)
    )
    private var currentIndex = 0

    private val PICK_CSV_FILE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.trueButton.setOnClickListener {
            checkQuestion(true)
        }

        binding.falseButton.setOnClickListener {
            checkQuestion(false)
        }

        binding.nextButton.setOnClickListener {
            currentIndex = (currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        binding.backButton.setOnClickListener {
            currentIndex = if (currentIndex > 0) (currentIndex - 1) else questionBank.size - 1
            updateQuestion()
        }

        binding.importButton.setOnClickListener {
            openFile()
        }

        updateQuestion()
    }

    private fun updateQuestion() {
        val questionText = questionBank[currentIndex].text
        binding.questionTextView.text = questionText
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
            Toast.makeText(
                this,
                R.string.correct_toast,
                Toast.LENGTH_SHORT
            ).show()
        }
        else{
            Toast.makeText(
                this,
                R.string.incorrect_toast,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
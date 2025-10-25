package com.ClassActivity1.geoquiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.ClassActivity1.geoquiz.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true),
        )
    private var currentIndex = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate((layoutInflater))
        setContentView(binding.root)


        binding.trueButton.setOnClickListener { view: View ->
            checkQuestion(true)
        }

        binding.falseButton.setOnClickListener { view: View ->
            checkQuestion(false)
        }

        binding.nextButton.setOnClickListener{
            currentIndex = (currentIndex+1)%questionBank.size
            updateQuestion()
        }
        binding.backButton.setOnClickListener{
            currentIndex = if (currentIndex>0) (currentIndex-1) else questionBank.size-1
            updateQuestion()
        }
        updateQuestion()
    }
    private fun updateQuestion(){
        val questionTextResId = questionBank[currentIndex].textResId
        binding.questionTextView.setText(questionTextResId)
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
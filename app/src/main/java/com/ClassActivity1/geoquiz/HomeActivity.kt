package com.ClassActivity1.geoquiz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.ClassActivity1.geoquiz.databinding.ActivityHomeBinding;
import com.ClassActivity1.geoquiz.MainActivity;

import kotlin.math.ceil
import android.os.SystemClock

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sampleQuiz = binding.sampleQuiz;
        sampleQuiz.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java);
            val quiz = mutableListOf(
                Question("Is Australia a country and a continent?", true),
                Question("Is the Pacific Ocean the largest ocean in the world?", true),
                Question("Is the Suez Canal in the Middle East?", false),
                Question("Is the Sahara Desert in Africa?", false),
                Question("Are the Andes Mountains in the Americas?", true),
                Question("Is Mount Everest in Asia?", true)
            );
            MainActivity.questionBank = quiz;
            startActivity(intent);
        }
    }
}

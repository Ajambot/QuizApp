package com.ClassActivity1.geoquiz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ClassActivity1.geoquiz.databinding.ActivityEndScreenBinding

class EndActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEndScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEndScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val finalScore = intent.getIntExtra("score", 0)
        binding.scoreTextView.text = "Score: $finalScore"

        // BACK → Return to MainActivity (previous quiz)
        binding.Restart.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            finish()
        }

        // 4. Exit app
        binding.exitButton.setOnClickListener {
            finishAffinity()   // closes the entire app cleanly
        }
    }
}

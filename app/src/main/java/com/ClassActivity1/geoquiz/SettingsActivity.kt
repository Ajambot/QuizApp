package com.ClassActivity1.geoquiz

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity) // your ConstraintLayout XML

        val seekBar = findViewById<SeekBar>(R.id.setting_timer_seekbar)
        val randomSwitch = findViewById<Switch>(R.id.switch_random_order)
        val saveButton = findViewById<Button>(R.id.home_button)

        // Optionally set initial values from SharedPreferences
        val prefs = getSharedPreferences("quiz_settings", MODE_PRIVATE)
        seekBar.progress = prefs.getInt("timer_per_question", 30)
        randomSwitch.isChecked = prefs.getBoolean("randomize_questions", true)

        saveButton.setOnClickListener {
            // Save settings
            with(prefs.edit()) {
                putInt("timer_per_question", seekBar.progress)
                putBoolean("randomize_questions", randomSwitch.isChecked)
                apply()
            }

            // Go back to HomeActivity
            finish() // assuming home is the previous screen
        }
    }
}
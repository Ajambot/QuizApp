package com.ClassActivity1.geoquiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private var questionTime: Int = 30
    private var randomOrder: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        try {
            // Get current settings if passed from HomeActivity
            questionTime = intent.getIntExtra("currentQuestionTime", 30)
            randomOrder = intent.getBooleanExtra("currentRandomOrder", true)

            // Find views by ID
            val switchRandomOrder: Switch = findViewById(R.id.switch_random_order)
            val timerSeekBar: SeekBar = findViewById(R.id.setting_timer_seekbar)
            val saveButton: Button = findViewById(R.id.home_button)

            // Initialize values
            switchRandomOrder.isChecked = randomOrder
            timerSeekBar.progress = questionTime

            // Try to update description if it exists, but don't crash if it doesn't
            try {
                val descText: TextView = findViewById(R.id.setting_timer_desc)
                descText.text = "Current: $questionTime seconds per question"

                // SeekBar listener with description update
                timerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        questionTime = progress.coerceAtLeast(1)
                        descText.text = "Current: $questionTime seconds per question"
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            } catch (e: Exception) {
                // Description text doesn't exist, just use basic listener
                timerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        questionTime = progress.coerceAtLeast(1)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }

            // Switch listener
            switchRandomOrder.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                randomOrder = isChecked
            }

            // Save button
            saveButton.setOnClickListener {
                val resultIntent = Intent().apply {
                    putExtra("questionTime", questionTime)
                    putExtra("randomOrder", randomOrder)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            finish() // Close activity if there's a critical error
        }
    }
}
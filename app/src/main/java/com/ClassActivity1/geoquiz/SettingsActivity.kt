package com.ClassActivity1.geoquiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private var questionTime: Int = 30
    private var randomOrder: Boolean = true
    private var questionsPerPrompt: Int = 10
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)


        // Load current settings from intent
        questionTime = intent.getIntExtra("currentQuestionTime", 30)
        randomOrder = intent.getBooleanExtra("currentRandomOrder", true)
        questionsPerPrompt = intent.getIntExtra("currentPromptQuestionAmount", 10)


        // Find UI elements
        val switchRandomOrder: Switch = findViewById(R.id.switch_random_order)
        val timerSeekBar: SeekBar = findViewById(R.id.setting_timer_seekbar)
        val descText: TextView = findViewById(R.id.setting_timer_desc)
        val questionAmountSeekBar : SeekBar = findViewById(R.id.setting_questionAmount_seekbar)
        val questionAmountDesc: TextView = findViewById(R.id.setting_questionAmount_desc)
        val saveButton: Button = findViewById(R.id.home_button)

        // Initialize UI
        switchRandomOrder.isChecked = randomOrder
        timerSeekBar.progress = questionTime
        descText.text = "Current: $questionTime seconds per question"

        questionAmountSeekBar.progress = questionsPerPrompt
        questionAmountDesc.text = "Current: $questionsPerPrompt questions per prompt"

        // SeekBar listener (Timer)
        timerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                questionTime = progress.coerceAtLeast(1)
                descText.text = "Current: $questionTime seconds per question"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // SeekBar listener (Questions per Prompt)
        questionAmountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                questionsPerPrompt = progress.coerceAtLeast(5)
                questionAmountDesc.text = "Current: $questionsPerPrompt questions per prompt"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Switch listener
        switchRandomOrder.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            randomOrder = isChecked
        }

        // Save button: return results to HomeActivity
        saveButton.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("questionTime", questionTime)
                putExtra("randomOrder", randomOrder)
                putExtra("questionsPerPrompt", questionsPerPrompt)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}

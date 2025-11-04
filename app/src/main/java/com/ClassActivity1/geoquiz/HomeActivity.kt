package com.ClassActivity1.geoquiz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ClassActivity1.geoquiz.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val PICK_CSV_FILE = 1
    private var csvUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Upload CSV button
        binding.importButton.setOnClickListener {
            openFile()
        }

        // Start quiz button
        binding.startQuizButton.setOnClickListener {
            val promptText = binding.PromptEditText.text.toString().trim()

            // Check input or CSV
            if (promptText.isEmpty() && csvUri == null) {
                Toast.makeText(this, "Please upload a CSV or enter a prompt", Toast.LENGTH_SHORT).show()
            } else {
                // Start the quiz activity
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("promptText", promptText)
                csvUri?.let { intent.putExtra("csvUri", it.toString()) }
                startActivity(intent)
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
            data?.data?.let { uri ->
                csvUri = uri
                Toast.makeText(this, "CSV file selected!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

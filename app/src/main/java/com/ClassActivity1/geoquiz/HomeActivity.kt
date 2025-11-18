package com.ClassActivity1.geoquiz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.os.Bundle
import java.text.SimpleDateFormat
import java.util.Date
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.contract.ActivityResultContracts
import com.ClassActivity1.geoquiz.MainActivity.Companion.questionBank
import com.ClassActivity1.geoquiz.MainActivity.Companion.randomOrder
import com.ClassActivity1.geoquiz.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var csvUri: Uri? = null



    private val PICK_CSV_FILE = 1
    private val SETTINGS_REQUEST = 2
    private var pendingCsvText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //AI PART
        //ill try and add logic to count csv lines and end there

        // Import CSV
        binding.importButton.setOnClickListener { openFile() }

        // Start Quiz
        binding.startQuizButton.setOnClickListener {
            if(randomOrder) {
                questionBank.shuffle()
            }
            val prompt = binding.promptEditText.text.toString().trim()

            if (MainActivity.questionBank.isEmpty()) {
                Toast.makeText(this, "Please load saved questions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("promptText", prompt)
                csvUri?.let { putExtra("csvUri", it.toString()) }
            }
            startActivity(intent)
        }
        binding.promptButton.setOnClickListener {
            val prompt = binding.promptEditText.text.toString().trim()

            if (prompt.isEmpty()) {
                Toast.makeText(this, "Please enter a prompt first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addQuestionsFromApi()

        }

        // Settings
        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java).apply {
                putExtra("currentQuestionTime", (MainActivity.answerMs / 1000L).toInt())
                putExtra("currentRandomOrder", MainActivity.randomOrder)
                putExtra("currentQuestionsPerPrompt", MainActivity.questionsPerPrompt)
            }
            startActivityForResult(intent, SETTINGS_REQUEST)
        }
    }

    //Allows user to pick a name and location for the CSV, then attempts to save it to device
    private val createCsvFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->

            //Once the user picks where the file is going and its name, attempt to save it
            if (uri != null && pendingCsvText != null) {
                try {
                    //makes the pipe for sending bytes to the temp file pendingCsvText
                    contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(pendingCsvText!!.toByteArray()) //writes file to users device
                    }
                    Toast.makeText(this, "CSV saved successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error Saving CSV: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    pendingCsvText =
                        null //makes the pendingCsvText null so we can write again later
                }
            } else {
                pendingCsvText = null //if user cancels
            }
        }

    private fun addQuestionsFromApi() {
        val numQuestions =
            MainActivity.questionsPerPrompt //How many questions to generate (pulled from MainActivities companion object)
        val topic = binding.promptEditText.text.toString().trim()
            .ifBlank { "The beauties of Kotlin" } //reads the text in the text field, trims spaces, and if nothing is put in place it defaults to the beauties of kotlin (which should not be possible, but just in case)
        //Model used and linking to the hidden API key (its safer then leaving it in the code)
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
        )
        val deviceTime = SimpleDateFormat(
            "yyyyMMddHHmmss", //formats date and time
        ).format(Date())
        //We have to be VERY specific, its not perfect, but its pretty good at getting gemini to give us what we want
        val promptG =
            """
            Generate $numQuestions multiple-choice $topic questions in CSV format.
            Each line MUST follow this structure with 6 fields:
            question,optionA,optionB,optionC,optionD,correctOption.
            HARD RULES:
            - Each line must have exactly 6 fields separated by commas.
            - No header, no numbering, no quotes, no explanation.
            - DO NOT use commas inside any field except as separators. 
            - Output ONLY $numQuestions CSV Lines.
            - CorrectOption must be exactly one of: A, B, C, or D 
            - For every question in the Quiz Make sure the answers are Randomly distributed all four options making sure each question has a 25% chance of having the answer A,B,C,D
            
            """.trimIndent()

        //Coroutine to call Gemini off the main thread
        lifecycleScope.launch {
            try {
                val response = generativeModel.generateContent(promptG)
                //Log.d("HomeActivity", "Full Gemini response: $response")
                //val raw = response.text.orEmpty()
                //if nothing is given throw exception
                val raw = response.text ?: throw IllegalStateException("No text in Gemini Response")
                //Log.d("HomeActivity", "API Raw CSV: \n $raw")

                //Splits the questions by newlines, trim spaces, drop blank lines, and makes sure not to give more questions then the user asked for
                val lines = raw.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    .take(numQuestions) //just as a fail safe to the prompt giving more
                val csvText =
                    lines.joinToString("\n") //joins back into a csv format, basically adds \n at the end of each question section
                //Log.d("MainActivity", "Final CSV text:\n$csvText")

                pendingCsvText =
                    csvText //store the CSV as a text so that the file saver can access it
                createCsvFileLauncher.launch("quiz$deviceTime.csv") //names it with device time as making duplicate quizzes causes issues

            } catch (e: Exception) {
                //Log.e("HomeActivity", "API Error", e)
                Toast.makeText(
                    this@HomeActivity,
                    "Failed to load new questions. ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun countCsvLines(uri: Uri): Int {
        contentResolver.openInputStream(uri)?.bufferedReader()?.useLines { lines ->
            return lines.count { it.isNotBlank() }
        }
        return 0
    }



    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        startActivityForResult(intent, PICK_CSV_FILE)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_CSV_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                csvUri = uri
                loadQuestionsFromCsv(uri)
            }
        }

        if (requestCode == SETTINGS_REQUEST && resultCode == RESULT_OK) {
            val questionTime = data?.getIntExtra("questionTime", 30) ?: 30
            val randomOrder = data?.getBooleanExtra("randomOrder", true) ?: true
            val questionsPrompt = data?.getIntExtra("questionsPerPrompt", 10) ?: 10
            MainActivity.answerMs = questionTime * 1000L
            MainActivity.randomOrder = randomOrder
            MainActivity.questionsPerPrompt = questionsPrompt



        }
    }

    private fun loadQuestionsFromCsv(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.bufferedReader()?.useLines { linesSeq ->
                val lines = linesSeq.map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toList()

                // Count the number of valid CSV rows
                MainActivity.numQuestions = lines.size

                val questions = lines.map { line ->
                    val parts = line.split(",").map { it.trim() }
                    if (parts.size < 6) throw Exception("CSV must have 6 columns")
                    val text = parts[0]
                    val options = parts.subList(1, 5)
                    val answer = parts[5]
                    Question(text, answer, options)
                }.toMutableList()

                MainActivity.questionBank = questions

                Toast.makeText(this, "Loaded ${lines.size} questions", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error reading CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}
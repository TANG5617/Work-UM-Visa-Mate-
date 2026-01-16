package com.um.visamate.ui.faculty

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.um.visamate.R
import com.um.visamate.data.models.Submission
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FacultyUploadActivity : AppCompatActivity() {

    // --- UI Components ---
    private lateinit var btnBack: ImageView
    private lateinit var tvStudentName: TextView

    // Upload Section 1
    private lateinit var tvConfirmationStatus: TextView
    private lateinit var btnUploadConfirmation: MaterialButton

    // Upload Section 2
    private lateinit var tvResultStatus: TextView
    private lateinit var btnUploadResult: MaterialButton

    // --- Data Variables ---
    private var studentId: String? = null
    private var currentDocumentType: String? = null
    private var submission: Submission? = null

    // --- File Picker Launcher ---
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Document selected, save logic
            currentDocumentType?.let { handleFileUpload(it) }
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_upload)

        bindViews()

        studentId = intent.getStringExtra("studentId") ?: "user-ahmed-khan"
        val studentName = intent.getStringExtra("studentName") ?: "Ahmed Ali"

        tvStudentName.text = "Uploading for: $studentName"

        loadSubmissionStatus()
        setupClickListeners()
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btnBack)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvConfirmationStatus = findViewById(R.id.tvConfirmationStatus)
        btnUploadConfirmation = findViewById(R.id.btnUploadConfirmation)
        tvResultStatus = findViewById(R.id.tvResultStatus)
        btnUploadResult = findViewById(R.id.btnUploadResult)
    }

    private fun setupClickListeners() {
        // Back Button
        btnBack.setOnClickListener { finishWithResult() }

        // Upload Buttons
        btnUploadConfirmation.setOnClickListener {
            currentDocumentType = "ConfirmationLetter"
            openFilePicker()
        }
        btnUploadResult.setOnClickListener {
            currentDocumentType = "ResultTranscript"
            openFilePicker()
        }
    }

    private fun loadSubmissionStatus() {
        CoroutineScope(Dispatchers.Main).launch {
            submission = withContext(Dispatchers.IO) {
                studentId?.let { FakeDatabase.getSubmissionForUser(it) }
            }

            // Create submission if it doesn't exist
            if (submission == null) {
                val newSub = Submission(userId = studentId ?: "")
                withContext(Dispatchers.IO) { FakeDatabase.createSubmission(newSub) }
                submission = newSub
            }
            updateUI()
        }
    }

    private fun openFilePicker() {
        try {
            filePickerLauncher.launch("*/*")
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "File manager not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFileUpload(docType: String) {
        val currentSub = submission ?: return
        val updatedSubmission = when (docType) {
            "ConfirmationLetter" -> currentSub.copy(hasConfirmationLetter = true)
            "ResultTranscript" -> currentSub.copy(hasResultTranscript = true)
            else -> currentSub
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                FakeDatabase.updateSubmission(updatedSubmission)
            }
            submission = updatedSubmission
            Toast.makeText(this@FacultyUploadActivity, "Uploaded Successfully!", Toast.LENGTH_SHORT).show()
            updateUI()
        }
    }

    private fun updateUI() {
        val sub = submission ?: return

        // Update Confirmation Status
        if (sub.hasConfirmationLetter) {
            tvConfirmationStatus.text = "Status: Completed ✅"
            tvConfirmationStatus.setTextColor(Color.parseColor("#4CAF50"))
            btnUploadConfirmation.text = "Replace File"
        } else {
            tvConfirmationStatus.text = "Status: Pending"
            tvConfirmationStatus.setTextColor(Color.GRAY)
            btnUploadConfirmation.text = "Upload"
        }

        // Update Result Status
        if (sub.hasResultTranscript) {
            tvResultStatus.text = "Status: Completed ✅"
            tvResultStatus.setTextColor(Color.parseColor("#4CAF50"))
            btnUploadResult.text = "Replace File"
        } else {
            tvResultStatus.text = "Status: Pending"
            tvResultStatus.setTextColor(Color.GRAY)
            btnUploadResult.text = "Upload"
        }
    }

    private fun finishWithResult() {
        // Notify Portal that we are done
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        finishWithResult()
    }
}
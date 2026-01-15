package com.um.visamate.ui.documents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.um.visamate.R
import com.um.visamate.data.models.Submission
import com.um.visamate.data.models.SubmissionStatus
import com.um.visamate.data.models.User
import com.um.visamate.utils.FakeDatabase
import com.um.visamate.utils.RoleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocumentSubmissionActivity : AppCompatActivity() {
    private lateinit var btnUploadPassport: MaterialButton
    private lateinit var btnReplacePassport: MaterialButton
    private lateinit var btnUploadFinancial: MaterialButton
    private lateinit var btnRequestFaculty: MaterialButton
    private lateinit var btnBack: ImageView

    private var facultyDocumentsRequested = false
    private var user: User? = null

    // File picker launcher logic remains same
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedFileUri: Uri? = result.data?.data
            if (selectedFileUri != null) {
                Toast.makeText(this, "File selected: ${selectedFileUri.path}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getString(R.string.msg_no_file_selected), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_document_submission)

        // 1. Initialize Views
        btnUploadPassport = findViewById(R.id.btnUploadPassport)
        btnReplacePassport = findViewById(R.id.btnReplacePassport)
        btnUploadFinancial = findViewById(R.id.btnUploadFinancial)
        btnRequestFaculty = findViewById(R.id.btnRequestFaculty)
        btnBack = findViewById(R.id.btnBack)

        // 2. Fetch User Data from intent
        val userId = intent.getStringExtra("userId")
        user = userId?.let { FakeDatabase.findUserById(it) } ?: FakeDatabase.currentUser

        // 3. Permission Check
        if (!RoleManager.canSubmit(user)) {
            disableAllButtons()
            Toast.makeText(this, "Access Denied: Finalized or Unauthorized", Toast.LENGTH_LONG).show()
        }

        setupClickListeners()
        updateUiForChecklist()
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnUploadPassport.setOnClickListener { openFilePicker() }

        btnReplacePassport.setOnClickListener {
            Toast.makeText(this, "Replace scanned passport", Toast.LENGTH_SHORT).show()
        }

        btnUploadFinancial.setOnClickListener {
            val currentUser = user ?: return@setOnClickListener

            btnUploadFinancial.isEnabled = false
            btnUploadFinancial.text = "Uploading..."

            // Use lifecycleScope for better coroutine management in Activity
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Create new submission record
                    val submission = Submission(
                        id = "SUB-${System.currentTimeMillis()}",
                        userId = currentUser.id,
                        status = SubmissionStatus.PENDING
                    )

                    // CALL SUSPEND FUNCTION: This fixes the unresolved reference error
                    FakeDatabase.createSubmission(submission)

                    // Simulate upload delay
                    kotlinx.coroutines.delay(1000)

                    // Mock update logic: In a real app, this changes status based on server response
                    val updatedSubmission = submission.copy(
                        status = SubmissionStatus.PENDING,
                        submittedAt = System.currentTimeMillis()
                    )
                    FakeDatabase.updateSubmission(updatedSubmission)

                    // Return to main thread to update UI
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DocumentSubmissionActivity, "Upload Success!", Toast.LENGTH_SHORT).show()
                        btnUploadFinancial.text = "Uploaded"
                        btnUploadFinancial.backgroundTintList = ContextCompat.getColorStateList(this@DocumentSubmissionActivity, R.color.green_success)
                        updateUiForChecklist()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnUploadFinancial.isEnabled = true
                        btnUploadFinancial.text = "Upload"
                        Toast.makeText(this@DocumentSubmissionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnRequestFaculty.setOnClickListener {
            facultyDocumentsRequested = true
            updateUiForChecklist()
            Toast.makeText(this, "Faculty documents requested", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Select a file"))
        } catch (e: Exception) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableAllButtons() {
        btnUploadPassport.isEnabled = false
        btnUploadFinancial.isEnabled = false
        btnRequestFaculty.isEnabled = false
    }

    private fun updateUiForChecklist() {
        if (facultyDocumentsRequested) {
            btnRequestFaculty.isEnabled = false
            btnRequestFaculty.text = "Requested"
            btnRequestFaculty.backgroundTintList = ContextCompat.getColorStateList(this, R.color.secondary_steel_blue)
        }
    }
}
package com.um.visamate.ui.faculty

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.um.visamate.R
import com.um.visamate.ui.login.LoginActivity

class FacultyPortalActivity : AppCompatActivity() {

    // --- UI Components ---
    private lateinit var tvPendingCount: TextView
    private lateinit var tvCompletedCount: TextView
    private lateinit var btnUploadDocs1: MaterialButton
    private lateinit var cardAhmed: MaterialCardView
    private lateinit var btnLogout: LinearLayout

    // --- Activity Launcher ---
    // This listens for when FacultyUploadActivity finishes and returns
    private val uploadActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // The user successfully uploaded files and came back!
            updateDashboardCounts()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_portal)

        initializeViews()
        setupDashboardLogic()
    }

    private fun initializeViews() {
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        btnUploadDocs1 = findViewById(R.id.btnUploadDocs1)
        cardAhmed = findViewById(R.id.cardAhmed)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupDashboardLogic() {
        // 1. Logout Logic
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 2. Open Upload Activity
        btnUploadDocs1.setOnClickListener {
            val intent = Intent(this, FacultyUploadActivity::class.java).apply {
                // Pass the student details to the next screen
                putExtra("studentId", "user-ahmed-khan")
                putExtra("studentName", "Ahmed Ali")
            }
            // Launch the activity and wait for result
            uploadActivityLauncher.launch(intent)
        }
    }

    private fun updateDashboardCounts() {
        // Update the UI to show work is done
        tvPendingCount.text = "1"
        tvCompletedCount.text = "13"

        // Hide the card for Ahmed since we just finished him
        cardAhmed.visibility = View.GONE

        Toast.makeText(this, "Dashboard updated successfully", Toast.LENGTH_SHORT).show()
    }
}
package com.um.visamate.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.um.visamate.R
import com.um.visamate.data.models.SubmissionStatus
import com.um.visamate.data.models.User
import com.um.visamate.ui.documents.DocumentSubmissionFragment
import com.um.visamate.ui.login.LoginActivity
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.um.visamate.ui.payment.PaymentActivity

class DashboardActivity : AppCompatActivity() {
    private lateinit var btnContinueRenewal: MaterialButton
    private lateinit var tvWelcome: TextView
    private lateinit var tvStudentId: TextView
    private lateinit var btnLogout: LinearLayout
    private lateinit var fragmentContainer: View
    private var user: User? = null

    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshDashboardStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        btnContinueRenewal = findViewById(R.id.btnContinueRenewal)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvStudentId = findViewById(R.id.tvStudentId)
        btnLogout = findViewById(R.id.btnLogout)
        fragmentContainer = findViewById(R.id.fragment_container)

        user = FakeDatabase.currentUser
        user?.let {
            tvWelcome.text = "Welcome back, ${it.name}"
            tvStudentId.text = "Student ID: ${it.studentId ?: "N/A"}"
        }

        setupClickListeners()
        refreshDashboardStatus()

        // Handle the Back Button: Hide fragment container when back stack is empty
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                fragmentContainer.visibility = View.GONE
                refreshDashboardStatus()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDashboardStatus()
    }

    private fun setupClickListeners() {
        btnContinueRenewal.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val status = FakeDatabase.getSubmissionForUser(user?.id ?: "")?.status
                withContext(Dispatchers.Main) {
                    when (status) {
                        SubmissionStatus.APPROVED -> {
                            val intent = Intent(this@DashboardActivity, PaymentActivity::class.java)
                            paymentLauncher.launch(intent)
                        }
                        else -> showFragment(DocumentSubmissionFragment())
                    }
                }
            }
        }

        findViewById<View>(R.id.cardUploadDoc).setOnClickListener { showFragment(DocumentSubmissionFragment()) }
        findViewById<View>(R.id.cardPayFees).setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            paymentLauncher.launch(intent)
        }
        btnLogout.setOnClickListener {
            FakeDatabase.currentUser = null
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    fun refreshDashboardStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            val submission = FakeDatabase.getSubmissionForUser(user?.id ?: "")
            withContext(Dispatchers.Main) {
                // Determine if faculty part is done
                val isFacultyDone = submission != null && submission.hasConfirmationLetter && submission.hasResultTranscript

                when (submission?.status) {
                    SubmissionStatus.SUBMITTED -> updateUiToUploadStage()
                    SubmissionStatus.APPROVED -> updateUiToPaymentStage()
                    else -> resetUiToInitialStage(isFacultyDone) // Pass the flag
                }
                
                // Explicitly update faculty UI again if needed, or rely on resetUiToInitialStage handling it.
                // But to be safe, if isFacultyDone is true, ensure it's green.
                if (isFacultyDone) {
                    updateFacultyStatusToComplete()
                }
            }
        }
    }

    private fun updateFacultyStatusToComplete() {
        findViewById<ImageView>(R.id.iv_warning_emblem)?.let {
            it.setImageResource(R.drawable.ic_check)
            it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green_success)
        }
        findViewById<TextView>(R.id.tvStatusTitle)?.text = "Faculty Documents Ready"
        findViewById<TextView>(R.id.tvStatusDesc)?.text = "All documents uploaded"
    }

    private fun updateUiToPaymentStage() {
        // Faculty Status: Change to Approved (Green)
        updateFacultyStatusToComplete()

        // Payment Step: Becomes active (Step 1)
        findViewById<TextView>(R.id.tvPaymentStepCircle)?.let {
            it.text = "1"
            it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.accent_gold)
            it.setTextColor(ContextCompat.getColor(this, R.color.onPrimary))
        }
        btnContinueRenewal.visibility = View.VISIBLE
        btnContinueRenewal.text = "PROCEED TO PAYMENT"
    }

    /**
     * Stage: SUBMITTED (Payment is done, time to upload)
     */
    private fun updateUiToUploadStage() {
        // 1. Update Payment Step Visuals
        findViewById<TextView>(R.id.tvPaymentStepCircle)?.let {
            it.text = "✓"
            it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green_success)
            it.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        // 2. NEW: Update Payment Step Text
        // Using existing ID to change description
        findViewById<TextView>(R.id.tvPaymentStepStatus)?.apply {
            text = "Payment Completed" // Change from "Not Started"
            setTextColor(ContextCompat.getColor(context, R.color.green_success))
        }

        // 3. Update Visa Unit Step Visuals (Step 2)
        findViewById<TextView>(R.id.tvVisaUnitStepCircle)?.let {
            it.text = "2" // Show as the next active step
            it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.accent_gold)
            it.setTextColor(ContextCompat.getColor(this, R.color.onPrimary))
        }

        // 4. NEW: Update Visa Unit Step Text
        // Change title from "Visa Unit" or "Not Started" to "Ready for Submission"
        findViewById<TextView>(R.id.tvVisaUnitStepText)?.apply {
            text = "Final Upload Ready"
            setTextColor(ContextCompat.getColor(context, R.color.black))
            alpha = 1.0f
        }

        // 5. Update Main Action Button
        btnContinueRenewal.visibility = View.VISIBLE
        btnContinueRenewal.text = "UPLOAD FINAL DOCUMENTS"
    }

    private fun resetUiToInitialStage(isFacultyDone: Boolean) {
        if (isFacultyDone) {
             updateFacultyStatusToComplete()
        } else {
            findViewById<ImageView>(R.id.iv_warning_emblem)?.let {
                it.setImageResource(R.drawable.ic_warning)
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange_warning)
            }
            findViewById<TextView>(R.id.tvStatusTitle)?.text = "Waiting for Faculty"
            findViewById<TextView>(R.id.tvStatusDesc)?.text = "Documents requested on 11 Dec 2024"
        }
        
        findViewById<TextView>(R.id.tvPaymentStepCircle)?.let {
            it.text = "1"
            it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey_border)
            it.setTextColor(ContextCompat.getColor(this, R.color.grey_text))
        }
        btnContinueRenewal.visibility = View.VISIBLE
        btnContinueRenewal.text = "CONTINUE RENEWAL"
    }

    fun showFragment(fragment: Fragment) {
        fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
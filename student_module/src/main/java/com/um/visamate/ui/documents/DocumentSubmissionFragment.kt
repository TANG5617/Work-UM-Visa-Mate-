package com.um.visamate.ui.documents

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.um.visamate.R
import com.um.visamate.data.models.SubmissionStatus
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocumentSubmissionFragment : Fragment(R.layout.fragment_document_submission) {

    private lateinit var btnRequestFaculty: MaterialButton
    private lateinit var btnSubmitVisaUnit: MaterialButton
    
    // Cards
    private lateinit var cardPassport: MaterialCardView
    private lateinit var cardPassportScan: MaterialCardView
    private lateinit var cardFinancial: MaterialCardView

    // Icons
    private lateinit var ivPassportIcon: ImageView
    private lateinit var ivPassportScanIcon: ImageView
    private lateinit var ivFinancialIcon: ImageView

    // Buttons
    private lateinit var btnUploadPassport: MaterialButton
    private lateinit var btnReplacePassport: MaterialButton
    private lateinit var btnUploadFinancial: MaterialButton

    // Descs
    private lateinit var tvPassportDesc: TextView
    private lateinit var tvPassportScanDesc: TextView
    private lateinit var tvFinancialDesc: TextView

    private var userId: String = FakeDatabase.currentUser?.id ?: ""
    
    // File Pickers
    private val passportPhotoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        handleFileSelection(uri, "PassportPhoto")
    }
    
    private val passportScanPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        handleFileSelection(uri, "PassportScan")
    }

    private val financialProofPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        handleFileSelection(uri, "FinancialProof")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        btnRequestFaculty = view.findViewById(R.id.btnRequestFaculty)
        btnSubmitVisaUnit = view.findViewById(R.id.btnSubmitVisaUnit)
        
        cardPassport = view.findViewById(R.id.cardPassport)
        cardPassportScan = view.findViewById(R.id.cardPassportScan)
        cardFinancial = view.findViewById(R.id.cardFinancial)

        ivPassportIcon = view.findViewById(R.id.ivPassportIcon)
        ivPassportScanIcon = view.findViewById(R.id.ivPassportScanIcon)
        ivFinancialIcon = view.findViewById(R.id.ivFinancialIcon)

        btnUploadPassport = view.findViewById(R.id.btnUploadPassport)
        btnReplacePassport = view.findViewById(R.id.btnReplacePassport) // For Scanned Passport
        btnUploadFinancial = view.findViewById(R.id.btnUploadFinancial)
        
        tvPassportDesc = view.findViewById(R.id.tvPassportDesc)
        tvPassportScanDesc = view.findViewById(R.id.tvPassportScanDesc)
        tvFinancialDesc = view.findViewById(R.id.tvFinancialDesc)

        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupClickListeners()
        setupInitialUI()
        checkStatusAndSetupButtons()
    }

    private fun setupClickListeners() {
        btnUploadPassport.setOnClickListener {
            passportPhotoPicker.launch("image/*") // Only images
        }

        btnReplacePassport.setOnClickListener {
            passportScanPicker.launch("*/*") // PDF or Images
        }

        btnUploadFinancial.setOnClickListener {
            financialProofPicker.launch("*/*") // PDF or Images
        }
    }

    private fun handleFileSelection(uri: Uri?, type: String) {
        if (uri == null) return

        // Check file size (Max 5MB)
        if (getFileSize(uri) > 5 * 1024 * 1024) {
            Toast.makeText(context, "File too large! Max 5MB.", Toast.LENGTH_LONG).show()
            return
        }

        // Simulate Upload and Save
        simulateUpload(type, uri)
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (it.moveToFirst()) {
                    it.getLong(sizeIndex)
                } else 0L
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun simulateUpload(type: String, uri: Uri) {
        val (button, card, icon, descView) = when (type) {
            "PassportPhoto" -> Quadruple(btnUploadPassport, cardPassport, ivPassportIcon, tvPassportDesc)
            "PassportScan" -> Quadruple(btnReplacePassport, cardPassportScan, ivPassportScanIcon, tvPassportScanDesc)
            "FinancialProof" -> Quadruple(btnUploadFinancial, cardFinancial, ivFinancialIcon, tvFinancialDesc)
            else -> return
        }

        lifecycleScope.launch {
            button.isEnabled = false
            button.text = "..."
            
            delay(1000) // Simulate network

            // Save to Database
            val submission = FakeDatabase.getSubmissionForUser(userId) ?: com.um.visamate.data.models.Submission(userId = userId)
            
            // Only update the specific field
            val updatedSubmission = when(type) {
                "PassportPhoto" -> submission.copy(passportPhotoUrl = uri.toString())
                "PassportScan" -> submission.copy(passportScanUrl = uri.toString())
                "FinancialProof" -> submission.copy(financialProofUrl = uri.toString())
                else -> submission
            }
            
            FakeDatabase.updateSubmission(updatedSubmission)

            // Update UI
            updateCardUI(card, icon, button, descView, true)
            
            Toast.makeText(context, "$type Uploaded!", Toast.LENGTH_SHORT).show()
            
            // Re-check submit button status
            checkIfReadyToSubmit(updatedSubmission)
        }
    }

    private fun updateCardUI(card: MaterialCardView, icon: ImageView, button: MaterialButton, descView: TextView, isUploaded: Boolean) {
        val context = requireContext()
        if (isUploaded) {
            // Success State
            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.light_green_success))
            card.strokeColor = ContextCompat.getColor(context, R.color.green_success)
            
            icon.setImageResource(R.drawable.ic_check)
            icon.setColorFilter(ContextCompat.getColor(context, R.color.green_success))
            
            button.text = "Replace"
            button.isEnabled = true
            // Use Outlined Button Style programmatically or just reset background
            // Resetting tint to transparent for outline effect or keep it simple
            button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
            button.setTextColor(ContextCompat.getColor(context, R.color.green_success))
            button.strokeColor = ContextCompat.getColorStateList(context, R.color.green_success)
            button.strokeWidth = 4 // make it visible

            descView.text = "File uploaded successfully"
            descView.setTextColor(ContextCompat.getColor(context, R.color.green_success))
        } else {
            // Default State
            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            card.strokeColor = ContextCompat.getColor(context, R.color.grey_border)
            
            // Reset Icon (Need original resource IDs based on type, but for now we might lose the original icon if we don't track it)
            // Simplified: We assume this function is mostly called to set "Uploaded". 
            // If we needed to revert, we'd need to know which icon to put back (Passport vs Receipt).
            // For now, let's assume we don't revert to "Not Uploaded" in this flow easily.
        }
    }

    private fun checkIfReadyToSubmit(submission: com.um.visamate.data.models.Submission) {
        val isReady = !submission.passportPhotoUrl.isNullOrEmpty() && 
                      !submission.passportScanUrl.isNullOrEmpty() && 
                      !submission.financialProofUrl.isNullOrEmpty()

        if (isReady) {
            btnSubmitVisaUnit.isEnabled = true
            btnSubmitVisaUnit.alpha = 1.0f
            btnSubmitVisaUnit.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
        }
    }

    private fun setupInitialUI() {
        btnRequestFaculty.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                FakeDatabase.updateSubmissionStatus(userId, SubmissionStatus.PENDING)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Request sent to Faculty!", Toast.LENGTH_SHORT).show()
                    updateUiToAwaitingFaculty()
                }
            }
        }

        btnSubmitVisaUnit.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                FakeDatabase.updateSubmissionStatus(userId, SubmissionStatus.SUBMITTED)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Final Application Submitted!", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun checkStatusAndSetupButtons() {
        lifecycleScope.launch(Dispatchers.IO) {
            val submission = FakeDatabase.getSubmissionForUser(userId)
            withContext(Dispatchers.Main) {
                if (submission != null) {
                    if (!submission.passportPhotoUrl.isNullOrEmpty()) {
                        updateCardUI(cardPassport, ivPassportIcon, btnUploadPassport, tvPassportDesc, true)
                    }
                    if (!submission.passportScanUrl.isNullOrEmpty()) {
                        updateCardUI(cardPassportScan, ivPassportScanIcon, btnReplacePassport, tvPassportScanDesc, true)
                    }
                    if (!submission.financialProofUrl.isNullOrEmpty()) {
                        updateCardUI(cardFinancial, ivFinancialIcon, btnUploadFinancial, tvFinancialDesc, true)
                    }
                    
                    checkIfReadyToSubmit(submission)
                }

                when (submission?.status) {
                    SubmissionStatus.SUBMITTED -> updateUiToVisaUnitStage()
                    SubmissionStatus.PENDING, SubmissionStatus.APPROVED -> updateUiToAwaitingFaculty()
                    else -> resetToInitialState()
                }
            }
        }
    }

    private fun resetToInitialState() {
        btnRequestFaculty.isEnabled = true
        btnRequestFaculty.alpha = 1.0f
        btnRequestFaculty.text = "REQUEST FACULTY DOCUMENTS"

        btnSubmitVisaUnit.isEnabled = false
        btnSubmitVisaUnit.alpha = 0.5f
    }

    private fun updateUiToAwaitingFaculty() {
        btnRequestFaculty.text = "AWAITING FACULTY DOCUMENTS..."
        btnRequestFaculty.isEnabled = false
        btnRequestFaculty.alpha = 0.6f
    }

    private fun updateUiToVisaUnitStage() {
        btnRequestFaculty.text = "FACULTY DOCUMENTS UPLOADED"
        btnRequestFaculty.isEnabled = false
        btnRequestFaculty.alpha = 1.0f
        btnRequestFaculty.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_success)
        btnRequestFaculty.setIconResource(R.drawable.ic_check)
    }

    // Helper data class to keep the when statement clean
    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}

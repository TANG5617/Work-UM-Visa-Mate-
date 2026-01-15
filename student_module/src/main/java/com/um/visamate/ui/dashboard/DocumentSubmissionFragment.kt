package com.um.visamate.ui.documents

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
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
    private lateinit var btnUploadPassport: MaterialButton
    private lateinit var btnUploadFinancial: MaterialButton

    // NEW: Tracking variables for upload status
    private var isPassportUploaded = false
    private var isFinancialUploaded = false

    private var userId: String = FakeDatabase.currentUser?.id ?: ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnRequestFaculty = view.findViewById(R.id.btnRequestFaculty)
        btnSubmitVisaUnit = view.findViewById(R.id.btnSubmitVisaUnit)
        btnUploadPassport = view.findViewById(R.id.btnUploadPassport)
        btnUploadFinancial = view.findViewById(R.id.btnUploadFinancial)

        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnUploadPassport.setOnClickListener {
            simulateUpload(btnUploadPassport, "PASSPORT UPLOADED", true)
        }

        btnUploadFinancial.setOnClickListener {
            simulateUpload(btnUploadFinancial, "FINANCIAL PROOF UPLOADED", false)
        }

        setupInitialUI()
        checkStatusAndSetupButtons()
    }

    /**
     * Modified simulation to handle the "Enabled" logic of the submit button
     */
    private fun simulateUpload(button: MaterialButton, successText: String, isPassport: Boolean) {
        lifecycleScope.launch {
            button.isEnabled = false
            button.text = "UPLOADING..."
            button.alpha = 0.7f

            delay(1000)

            button.text = successText
            button.alpha = 1.0f
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_success))
            button.setIconResource(R.drawable.ic_check)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            // Update flags
            if (isPassport) isPassportUploaded = true else isFinancialUploaded = true

            Toast.makeText(context, "File Uploaded Successfully!", Toast.LENGTH_SHORT).show()

            // NEW: Check if we can enable the submit button
            checkIfReadyToSubmit()
        }
    }

    /**
     * NEW FUNCTION: Enables the button only if both files are ready
     */
    private fun checkIfReadyToSubmit() {
        if (isPassportUploaded && isFinancialUploaded) {
            btnSubmitVisaUnit.isEnabled = true
            btnSubmitVisaUnit.alpha = 1.0f
            // Change color to make it obvious it is now clickable
            btnSubmitVisaUnit.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
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
            Toast.makeText(context, "Final Application Submitted!", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun checkStatusAndSetupButtons() {
        lifecycleScope.launch(Dispatchers.IO) {
            val submission = FakeDatabase.getSubmissionForUser(userId)
            withContext(Dispatchers.Main) {
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

        btnSubmitVisaUnit.isEnabled = false
        btnSubmitVisaUnit.alpha = 0.5f
    }

    private fun updateUiToVisaUnitStage() {
        btnRequestFaculty.text = "FACULTY DOCUMENTS UPLOADED"
        btnRequestFaculty.isEnabled = false
        btnRequestFaculty.alpha = 1.0f
        btnRequestFaculty.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_success))
        btnRequestFaculty.setIconResource(R.drawable.ic_check)

        // Note: Even in this stage, the user still needs to upload Passport/Financial
        // to enable the Submit button via checkIfReadyToSubmit()
    }
}
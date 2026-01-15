package com.um.visamate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.um.visamate.R
import com.um.visamate.databinding.FragmentPaymentBinding
import com.um.visamate.data.models.SubmissionStatus
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeePaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Default selection is FPX
        updatePaymentSelection(isFpx = true)

        // --- Payment Method Selection Logic ---
        binding.cardFpx.setOnClickListener { updatePaymentSelection(isFpx = true) }
        binding.radioFpx.setOnClickListener { updatePaymentSelection(isFpx = true) }

        binding.cardCredit.setOnClickListener { updatePaymentSelection(isFpx = false) }
        binding.radioCredit.setOnClickListener { updatePaymentSelection(isFpx = false) }

        // Go back to the previous screen (Dashboard)
        binding.btnBack.setOnClickListener {
            requireActivity().finish()
        }

        binding.btnProceedPayment.setOnClickListener {
            if (binding.cbConfirm.isChecked) {
                // Use lifecycleScope for Coroutines
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val currentUserId = FakeDatabase.currentUser?.id ?: ""
                    val submission = FakeDatabase.getSubmissionForUser(currentUserId)

                    if (submission != null) {
                        // 1. Update status to SUBMITTED in the Fake Database
                        submission.status = SubmissionStatus.SUBMITTED
                        FakeDatabase.updateSubmission(submission)

                        withContext(Dispatchers.Main) {
                            // 2. Navigate to PaymentReceiptFragment
                            // IMPORTANT: We use R.id.payment_fragment_container because
                            // this fragment is now hosted inside PaymentActivity.
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.payment_fragment_container, PaymentReceiptFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Submission record missing", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please confirm the declaration", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * UI Logic to handle selection state between FPX and Credit Card
     */
    private fun updatePaymentSelection(isFpx: Boolean) {
        // 1. Sync RadioButton states
        binding.radioFpx.isChecked = isFpx
        binding.radioCredit.isChecked = !isFpx

        // 2. Define colors and stroke width
        val activeColor = ContextCompat.getColor(requireContext(), R.color.colorSecondary)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_border)
        val thicknessActive = dpToPx(3)
        val thicknessInactive = dpToPx(1)

        // 3. Update FPX Card UI
        binding.cardFpx.strokeColor = if (isFpx) activeColor else inactiveColor
        binding.cardFpx.strokeWidth = if (isFpx) thicknessActive else thicknessInactive

        // 4. Update Credit Card UI
        binding.cardCredit.strokeColor = if (isFpx) inactiveColor else activeColor
        binding.cardCredit.strokeWidth = if (isFpx) thicknessInactive else thicknessActive
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
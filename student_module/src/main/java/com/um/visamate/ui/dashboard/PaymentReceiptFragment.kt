package com.um.visamate.ui.dashboard

import android.app.Activity
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.um.visamate.databinding.FragmentPaymentReceiptBinding

class PaymentReceiptFragment : Fragment() {

    private var _binding: FragmentPaymentReceiptBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Logic 1: Handle physical back button ---
        // Ensure that even if the user uses the system back button, it returns to Dashboard properly
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnToDashboard()
            }
        })

        // --- Logic 2: Start checkmark animation ---
        // Animates the success checkmark icon if it supports Animatable (e.g., AnimatedVectorDrawable)
        val drawable = binding.ivCheck.drawable
        if (drawable is Animatable) {
            drawable.start()
        }

        // --- Logic 3: Back to Dashboard Button ---
        binding.btnBackToDashboard.setOnClickListener {
            returnToDashboard()
        }
    }

    /**
     * Finishes the PaymentActivity and sends a success result back to DashboardActivity.
     * This triggers the 'paymentLauncher' callback in DashboardActivity to refresh the UI.
     */
    private fun returnToDashboard() {
        // 1. Set the result to RESULT_OK so DashboardActivity knows payment was successful
        requireActivity().setResult(Activity.RESULT_OK)

        // 2. Finish the current Activity (PaymentActivity).
        // This automatically returns the user to the underlying DashboardActivity.
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
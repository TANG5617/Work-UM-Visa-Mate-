package com.um.visamate.ui.officer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.um.visamate.R
import com.um.visamate.data.models.SubmissionStatus
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class DocumentReviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnApprove = view.findViewById<Button>(R.id.button5)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack2)

        btnApprove.setOnClickListener {
            // --- 关键修复：使用 FakeDatabase 中正确的 ID "user-ahmed" ---
            MainScope().launch {
                FakeDatabase.updateSubmissionStatus("user-ahmed", SubmissionStatus.OFFICER_APPROVED)

                // 发送结果给 DashboardActivity 以更新官员端的列表
                parentFragmentManager.setFragmentResult("review_action", bundleOf("status" to "approved"))

                // 返回 Dashboard
                parentFragmentManager.popBackStack()
            }
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
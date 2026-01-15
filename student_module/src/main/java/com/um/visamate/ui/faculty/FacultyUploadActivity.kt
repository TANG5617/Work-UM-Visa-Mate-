package com.um.visamate.ui.faculty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.um.visamate.R
import com.um.visamate.data.models.SubmissionStatus
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.launch

class FacultyUploadFragment : Fragment() {

    private lateinit var btnUploadConfirmation: MaterialButton
    private lateinit var btnUploadResult: MaterialButton
    private lateinit var tvConfirmationStatus: TextView
    private lateinit var tvResultStatus: TextView
    // 1. 添加变量声明
    private lateinit var tvStudentName: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_faculty_upload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. 初始化 View
        tvStudentName = view.findViewById(R.id.tvStudentName)
        btnUploadConfirmation = view.findViewById(R.id.btnUploadConfirmation)
        btnUploadResult = view.findViewById(R.id.btnUploadResult)
        tvConfirmationStatus = view.findViewById(R.id.tvConfirmationStatus)
        tvResultStatus = view.findViewById(R.id.tvResultStatus)

        // 3. 核心修复：在这里直接把名字写死，确保演示时一定显示 Ahmed Ali
        tvStudentName.text = "Uploading for: Ahmed Ali"

        // 修复返回键：点击后触发 Activity 的 onBackPressed 来隐藏容器
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            activity?.onBackPressed()
        }

        btnUploadConfirmation.setOnClickListener { simulateUpload(tvConfirmationStatus, btnUploadConfirmation) }
        btnUploadResult.setOnClickListener { simulateUpload(tvResultStatus, btnUploadResult) }
    }

    private fun simulateUpload(statusView: TextView, button: MaterialButton) {
        button.isEnabled = false
        statusView.text = "Status: Uploading..."

        statusView.postDelayed({
            statusView.text = "Status: Completed ✅"
            statusView.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            checkFinalStatus()
        }, 1200)
    }

    private fun checkFinalStatus() {
        if (tvConfirmationStatus.text.contains("Completed") && tvResultStatus.text.contains("Completed")) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // 更新数据库
                    val submission = FakeDatabase.getSubmissionForUser("user-ahmed")
                    if (submission != null) {
                        submission.status = SubmissionStatus.APPROVED
                        FakeDatabase.updateSubmission(submission)

                        Toast.makeText(context, "Approval Successful!", Toast.LENGTH_SHORT).show()

                        // 回调 Activity，执行 UI 变更逻辑
                        (activity as? FacultyPortalActivity)?.onUploadSuccess()
                    } else {
                        Toast.makeText(context, "Error: Student not found", Toast.LENGTH_SHORT).show()
                        btnUploadConfirmation.isEnabled = true
                        btnUploadResult.isEnabled = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
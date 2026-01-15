package com.um.visamate.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.um.visamate.data.models.Submission
import com.um.visamate.data.models.SubmissionStatus
import com.um.visamate.utils.FakeDatabase
import com.um.visamate.utils.MockNetworkClient
import com.um.visamate.utils.RoleManager
import kotlinx.coroutines.launch

// SubmissionViewModel enforces FR and NFR constraints (Final Submission Lock, RBAC)
class SubmissionViewModel : ViewModel() {

    fun submit(userId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. 权限检查 (RBAC)
                val user = FakeDatabase.findUserById(userId)
                RoleManager.requireSubmit(user)

                // 2. 创建 Submission 对象，显式传入唯一 ID (解决报错)
                val dummy = Submission(
                    id = "SUB-${System.currentTimeMillis()}", // 必须传入 ID
                    userId = userId,
                    status = SubmissionStatus.PENDING
                )

                // 3. 在本地数据库创建初始记录
                FakeDatabase.createSubmission(dummy)

                // 4. 模拟网络上传
                val ok = MockNetworkClient.uploadSubmission(dummy, minDurationMs = 1000L)

                if (ok) {
                    // 5. 更新状态为已提交
                    // 注意：如果 Submission.kt 里 status 是 val，这里需要用 copy
                    val updatedSubmission = dummy.apply {
                        status = SubmissionStatus.APPROVED // 或者根据你的业务设为 APPROVED/REVIEWING
                        submittedAt = System.currentTimeMillis()
                    }

                    FakeDatabase.updateSubmission(updatedSubmission)
                    onResult(true, null)
                } else {
                    onResult(false, "Network timeout: Upload failed")
                }
            } catch (e: Exception) {
                // 捕获权限不足或其他异常
                onResult(false, e.message ?: "Unknown error occurred")
            }
        }
    }
}
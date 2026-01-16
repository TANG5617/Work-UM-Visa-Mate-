package com.um.visamate.data.models

enum class SubmissionStatus {
    PENDING,    // 待处理
    REVIEWING,  // 审核中
    APPROVED,   // 已批准 (学院通过，等待学生付钱)
    SUBMITTED,  // 已提交 (学生付完钱了，送往 Visa Unit)
    OFFICER_APPROVED, // <--- 必须加这一行！对应官员点击 Approve 后的状态
    REJECTED    // 已拒绝
}
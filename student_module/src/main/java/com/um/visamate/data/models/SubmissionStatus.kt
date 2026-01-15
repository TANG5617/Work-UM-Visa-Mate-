package com.um.visamate.data.models

enum class SubmissionStatus {
    PENDING,    // 待处理
    REVIEWING,  // 审核中
    APPROVED,   // 已批准 (学院通过，等待学生付钱)
    SUBMITTED,  // 已提交 (学生付完钱了，送往 Visa Unit) -> 新增这一行
    REJECTED    // 已拒绝
}
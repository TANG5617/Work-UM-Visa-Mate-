package com.um.visamate.data.models

data class Submission(
    val id: String,          // 必须有 ID 才能定位更新哪一条
    val userId: String,
    var status: SubmissionStatus = SubmissionStatus.PENDING,
    var submittedAt: Long = 0
)
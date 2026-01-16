package com.um.visamate.data.models

import java.util.UUID

data class Submission(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    var status: SubmissionStatus = SubmissionStatus.PENDING,
    var submittedAt: Long = 0,
    val hasConfirmationLetter: Boolean = false,
    val hasResultTranscript: Boolean = false
)
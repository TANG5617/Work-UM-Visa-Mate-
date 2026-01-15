package com.um.visamate.utils

import com.um.visamate.data.models.Submission
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

// MockNetworkClient simulates HTTPS interactions and enforces timing requirements.
object MockNetworkClient {
    // in-flight upload tracking to prevent duplicates
    private val inFlight = ConcurrentHashMap<String, Boolean>()

    // Uploads a submission, enforces minDurationMs (e.g., NFR 2.2 upload < 5s but we simulate minimum 500ms)
    // Returns true on success, false on simulated failure
    suspend fun uploadSubmission(submission: Submission, minDurationMs: Long = 500L): Boolean {
        val id = submission.id
        if (inFlight.putIfAbsent(id, true) == true) {
            // already uploading
            return false
        }
        val start = System.currentTimeMillis()
        try {
            // Simulate serialization/checksum work
            delay(150L)
            // Ensure we respect minimum duration
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < minDurationMs) delay(minDurationMs - elapsed)
            // Simulate success
            return true
        } finally {
            inFlight.remove(id)
        }
    }
}


package com.um.visamate.utils

import android.content.Context
import com.um.visamate.data.models.Role
import com.um.visamate.data.models.Submission
import com.um.visamate.data.models.SubmissionStatus
import com.um.visamate.data.models.User
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object FakeDatabase {
    private val mutex = Mutex()
    private val users: MutableList<User> = mutableListOf()
    private val submissions: MutableList<Submission> = mutableListOf()

    private var isFinalSubmissionLocked: Boolean = false
    var currentUser: User? = null

    fun initialize(context: Context) {
        if (users.isEmpty()) seedSampleUsers()
    }

    private fun seedSampleUsers() {
        users.add(User("user-ahmed", "Ahmed Ali", "ahmed@student.com", Role.APPLICANT, "S2024001", "124 days"))
        users.add(User("user-sarah", "Sarah Lee", "sarah@student.com", Role.APPLICANT, "S2024002", "45 days"))
        users.add(User("user-bob", "Bob Officer", "bob@faculty.com", Role.REVIEWER))
        users.add(User(id = "user-visa-officer", name = "John Visa", email = "visa@um.com", role = Role.OFFICER))

        submissions.add(Submission(
            id = "sub_ahmed_001",
            userId = "user-ahmed", // Match this ID
            status = SubmissionStatus.PENDING
        ))
    }

    fun userExists(email: String): Boolean {
        return users.any { it.email.equals(email, ignoreCase = true) }
    }

    fun login(email: String): Boolean {
        val user = users.find { it.email.equals(email, ignoreCase = true) }
        return if (user != null) {
            currentUser = user
            true
        } else {
            false
        }
    }

    /**
     * 新增：简单的状态更新函数
     * 解决 DocumentSubmissionFragment 里的报错
     */
    suspend fun updateSubmissionStatus(userId: String, newStatus: SubmissionStatus) {
        mutex.withLock {
            val index = submissions.indexOfFirst { it.userId == userId }
            if (index >= 0) {
                // 找到现有的，只改状态
                val old = submissions[index]
                submissions[index] = old.copy(status = newStatus)
            } else {
                // 没找到就新建一个
                submissions.add(Submission(
                    id = "sub_${System.currentTimeMillis()}",
                    userId = userId,
                    status = newStatus
                ))
            }
            android.util.Log.d("DatabaseSync", "User $userId status changed to $newStatus")
        }
    }

    suspend fun createSubmission(submission: Submission): Submission {
        mutex.withLock {
            val existing = submissions.find { it.userId == submission.userId }
            if (existing == null) {
                submissions.add(submission)
            }
            return submission
        }
    }

    suspend fun updateSubmission(submission: Submission) {
        mutex.withLock {
            val index = submissions.indexOfFirst { it.userId == submission.userId }
            if (index >= 0) {
                submissions[index] = submission
            } else {
                submissions.add(submission)
            }
        }
    }

    fun getSubmissionForUser(userId: String): Submission? {
        return submissions.find { it.userId == userId }
    }

    fun findUserById(id: String): User? = users.find { it.id == id }
    fun addUser(user: User) { users.add(user) }
    fun isFinalSubmissionLocked(): Boolean = isFinalSubmissionLocked
    fun setFinalSubmissionLocked(locked: Boolean) { this.isFinalSubmissionLocked = locked }
}
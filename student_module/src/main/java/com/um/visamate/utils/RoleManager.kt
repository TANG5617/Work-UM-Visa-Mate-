package com.um.visamate.utils

import com.um.visamate.data.models.Role
import com.um.visamate.data.models.User

// 定义自定义异常（解决报错的关键）
class UnauthorizedException(message: String) : Exception(message)

object RoleManager {
    fun canSubmit(user: User?): Boolean {
        if (user == null) return false
        return user.role == Role.APPLICANT
    }

    fun canReview(user: User?): Boolean {
        if (user == null) return false
        return user.role == Role.REVIEWER || user.role == Role.ADMIN
    }

    fun canApprove(user: User?): Boolean {
        if (user == null) return false
        return user.role == Role.ADMIN
    }

    fun requireSubmit(user: User?) {
        if (!canSubmit(user)) throw UnauthorizedException("User not allowed to submit")
    }

    fun requireReview(user: User?) {
        if (!canReview(user)) throw UnauthorizedException("User not allowed to review")
    }
}
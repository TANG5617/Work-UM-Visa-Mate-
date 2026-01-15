package com.um.visamate.data.models

// 注意：这里不要再写 enum class Role {...}，因为它已经在 Role.kt 里了
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: Role, // 这里直接引用 Role.kt 里的定义
    val studentId: String? = null,
    val visaExpiry: String? = null
)
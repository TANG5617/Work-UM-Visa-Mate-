package com.um.visamate.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.um.visamate.data.models.Role
import com.um.visamate.data.models.User
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.launch

// AuthViewModel handles login logic (F.R.1.1 - F.R.1.4)
class AuthViewModel : ViewModel() {

    /**
     * 登录逻辑：优先从 FakeDatabase 匹配，如果没有则新建（Mock 逻辑）
     */
    fun login(email: String, role: Role, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            // 1. 尝试使用 email 登录
            val success = FakeDatabase.login(email)

            if (success) {
                // 如果登录成功，直接返回当前用户
                onResult(FakeDatabase.currentUser)
            } else {
                // 2. 如果没找到用户，创建一个新用户并存入数据库 (模拟注册)
                val newUser = User(
                    id = "user-${System.currentTimeMillis()}",
                    name = email.substringBefore("@"), // 用邮箱前缀当临时名字
                    email = email,
                    role = role
                )
                FakeDatabase.addUser(newUser)
                FakeDatabase.currentUser = newUser
                onResult(newUser)
            }
        }
    }
}
package com.um.visamate.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.um.visamate.R
import com.um.visamate.data.models.Role
import com.um.visamate.data.models.User
import com.um.visamate.ui.dashboard.DashboardActivity
import com.um.visamate.ui.faculty.FacultyPortalActivity
import com.um.visamate.ui.officer.OfficerDashboardActivity
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var cardRoleStudent: MaterialCardView
    private lateinit var cardRoleFaculty: MaterialCardView
    private lateinit var cardRoleVisa: MaterialCardView
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 确保数据库初始化，生成种子数据（Ahmed, Sarah, Bob 等）
        FakeDatabase.initialize(applicationContext)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cardRoleStudent = findViewById(R.id.cardRoleStudent)
        cardRoleFaculty = findViewById(R.id.cardRoleFaculty)
        cardRoleVisa = findViewById(R.id.cardRoleVisa)
        btnLogin = findViewById(R.id.btnLogin)

        setupRoleCards()

        btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun setupRoleCards() {
        val cards = listOf(cardRoleStudent, cardRoleFaculty, cardRoleVisa)
        cards.forEach { card ->
            card.isCheckable = true
            card.setOnClickListener {
                setSelectedCard(card)
            }
        }
        setSelectedCard(cardRoleStudent)
    }

    private fun setSelectedCard(card: MaterialCardView) {
        cardRoleStudent.isChecked = card == cardRoleStudent
        cardRoleFaculty.isChecked = card == cardRoleFaculty
        cardRoleVisa.isChecked = card == cardRoleVisa
    }

    private fun handleLogin() {
        val emailText = etEmail.text?.toString()?.trim().orEmpty()
        val passwordText = etPassword.text?.toString().orEmpty()

        // 基本校验
        if (emailText.isEmpty() || passwordText.isEmpty()) {
            if (emailText.isEmpty()) etEmail.error = "Required"
            if (passwordText.isEmpty()) etPassword.error = "Required"
            return
        }

        // 使用协程处理登录逻辑
        CoroutineScope(Dispatchers.IO).launch {
            // 核心逻辑：调用 FakeDatabase 的登录方法（内部会匹配 User 并存入 currentUser）
            val success = FakeDatabase.login(emailText)

            withContext(Dispatchers.Main) {
                if (success) {
                    val user = FakeDatabase.currentUser
                    if (user != null) {
                        navigateToDashboard(user)
                    }
                } else {
                    // 登录失败提示
                    Toast.makeText(
                        this@LoginActivity,
                        "User not found! \nStudent: alice@student.um.edu \nFaculty: bob@faculty.um.edu",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun navigateToDashboard(user: User) {
        // 根据数据库中该用户的真实 Role 进行跳转
        val intent = when (user.role) {
            Role.APPLICANT -> Intent(this, DashboardActivity::class.java)
            Role.REVIEWER -> Intent(this, FacultyPortalActivity::class.java)
            Role.ADMIN -> Intent(this, OfficerDashboardActivity::class.java)
            Role.OFFICER -> Intent(this, OfficerDashboardActivity::class.java)
        }

        // 传递用户 ID 并销毁登录页
        intent.putExtra("userId", user.id)
        startActivity(intent)
        finish()
    }
}
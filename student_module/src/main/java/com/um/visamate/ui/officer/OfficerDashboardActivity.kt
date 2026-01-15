package com.um.visamate.ui.officer

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.um.visamate.R
import com.um.visamate.ui.login.LoginActivity // 确保这里指向你真实的 LoginActivity 路径

/**
 * OfficerDashboardActivity (签证官员仪表盘)
 * 包含 Logout 跳转逻辑
 */
class OfficerDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_officer_dashboard)

        // 1. 初始化 Logout 按钮 (对应 XML 中的 LinearLayout)
        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)

        // 2. 设置点击跳转逻辑
        btnLogout.setOnClickListener {
            performLogout()
        }
    }

    /**
     * 执行注销逻辑并跳转回登录页面
     */
    private fun performLogout() {
        // 提示用户已退出
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // 创建跳转到 LoginActivity 的 Intent
        val intent = Intent(this, LoginActivity::class.java)

        // 关键：清除 Activity 任务栈
        // 这样用户在登录页按返回键时，不会回到这个已退出的 Dashboard
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        // 结束当前的 Activity
        finish()
    }
}
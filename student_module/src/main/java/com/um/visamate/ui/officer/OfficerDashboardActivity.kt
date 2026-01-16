package com.um.visamate.ui.officer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.um.visamate.R
import com.um.visamate.ui.login.LoginActivity

class OfficerDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_officer_dashboard)

        // --- 1. 你原有的 Logout 逻辑 (保持完全不变) ---
        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            performLogout()
        }

        // --- 2. 处理跳转到审核页面 ---
        // 找到 Ahmed Ali 的 Review 按钮
        val btnReview1 = findViewById<View>(R.id.btnReview1)

        // 找到包裹这个按钮的整个 MaterialCardView
        // 你的 XML 结构是 Button -> LinearLayout -> MaterialCardView
        // 所以调用两次 .parent 即可找到整个卡片容器
        val ahmedCard = btnReview1.parent.parent as View

        btnReview1.setOnClickListener {
            val fragment = DocumentReviewFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment) // 请确保在 XML 底部加了容器
                .addToBackStack(null)
                .commit()
        }

        // --- 3. 监听从审核页面返回的结果 ---
        supportFragmentManager.setFragmentResultListener("review_action", this) { _, bundle ->
            val status = bundle.getString("status")
            if (status == "approved") {
                // 执行 UI 更新逻辑
                updateDashboardUI(ahmedCard)
            }
        }
    }

    /**
     * 更新 Dashboard 的数字和卡片状态
     */
    private fun updateDashboardUI(cardToHide: View) {
        // 找到数字文本控件
        val tvSubmittedCount = findViewById<TextView>(R.id.tvSubmittedCount)
        val tvApprovedCount = findViewById<TextView>(R.id.tvApprovedCount)

        // 1. 更新数字 (8 -> 7, 45 -> 46)
        tvSubmittedCount.text = "7"
        tvApprovedCount.text = "46"

        // 2. 让 Ahmed Ali 的卡片彻底消失
        // 设置为 GONE 后，下方的卡片会自动上移填补空间
        cardToHide.visibility = View.GONE

        Toast.makeText(this, "Application Approved Successfully", Toast.LENGTH_SHORT).show()
    }

    /**
     * 注销逻辑 (保持不变)
     */
    private fun performLogout() {
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
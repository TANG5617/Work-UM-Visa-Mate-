package com.um.visamate.ui.faculty

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.um.visamate.R
import com.um.visamate.ui.login.LoginActivity

class FacultyPortalActivity : AppCompatActivity() {

    private lateinit var tvPendingCount: TextView
    private lateinit var tvCompletedCount: TextView
    private lateinit var btnUploadDocs1: MaterialButton
    private lateinit var cardAhmed: MaterialCardView
    private lateinit var fragmentContainer: View
    private lateinit var btnLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_portal)

        // 初始化
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        btnUploadDocs1 = findViewById(R.id.btnUploadDocs1)
        cardAhmed = findViewById(R.id.cardAhmed)
        fragmentContainer = findViewById(R.id.fragment_container)
        btnLogout = findViewById(R.id.btnLogout)

        // 登出逻辑
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 跳转逻辑
        btnUploadDocs1.setOnClickListener {
            fragmentContainer.visibility = View.VISIBLE // 显示容器
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, FacultyUploadFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // 修复：处理返回键，确保返回时隐藏容器，露出底下的 UI
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            fragmentContainer.visibility = View.GONE // 隐藏容器，防止遮挡
        } else {
            super.onBackPressed()
        }
    }

    // 成功回调：卡片消失，数字变动
    fun onUploadSuccess() {
        tvPendingCount.text = "1"
        tvCompletedCount.text = "13"
        cardAhmed.visibility = View.GONE
        fragmentContainer.visibility = View.GONE // 隐藏容器
        supportFragmentManager.popBackStack() // 退出上传页面
    }
}
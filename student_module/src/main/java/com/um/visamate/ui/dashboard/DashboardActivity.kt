package com.um.visamate.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.um.visamate.R
import com.um.visamate.data.models.SubmissionStatus
import com.um.visamate.data.models.User
import com.um.visamate.ui.documents.DocumentSubmissionFragment
import com.um.visamate.ui.login.LoginActivity
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.um.visamate.ui.payment.PaymentActivity

class DashboardActivity : AppCompatActivity() {
    private lateinit var btnContinueRenewal: MaterialButton
    private lateinit var tvWelcome: TextView
    private lateinit var tvStudentId: TextView
    private lateinit var btnLogout: LinearLayout
    private lateinit var fragmentContainer: View
    private var user: User? = null

    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshDashboardStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. 初始化所有 View
        btnContinueRenewal = findViewById(R.id.btnContinueRenewal)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvStudentId = findViewById(R.id.tvStudentId)
        btnLogout = findViewById(R.id.btnLogout)
        fragmentContainer = findViewById(R.id.fragment_container)

        user = FakeDatabase.currentUser
        user?.let {
            tvWelcome.text = "Welcome back, ${it.name}"
            tvStudentId.text = "Student ID: ${it.studentId ?: "N/A"}"
        }

        setupClickListeners()
        refreshDashboardStatus()

        // 监听 Fragment 关闭，自动刷新 Dashboard
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                fragmentContainer.visibility = View.GONE
                refreshDashboardStatus()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDashboardStatus()
    }

    private fun setupClickListeners() {
        btnContinueRenewal.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val submission = FakeDatabase.getSubmissionForUser(user?.id ?: "")
                withContext(Dispatchers.Main) {
                    when (submission?.status) {
                        SubmissionStatus.OFFICER_APPROVED -> { /* 下载 E-Visa 逻辑 */ }
                        SubmissionStatus.APPROVED -> { /* 学院通过了，跳转去付钱 */
                            val intent = Intent(this@DashboardActivity, PaymentActivity::class.java)
                            paymentLauncher.launch(intent)
                        }
                        SubmissionStatus.SUBMITTED -> { /* 已提交，不可点 */ }
                        else -> showFragment(DocumentSubmissionFragment())
                    }
                }
            }
        }

        findViewById<View>(R.id.cardUploadDoc).setOnClickListener {
            showFragment(DocumentSubmissionFragment())
        }

        findViewById<View>(R.id.cardPayFeesContainer).setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            paymentLauncher.launch(intent)
        }

        btnLogout.setOnClickListener {
            FakeDatabase.currentUser = null
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    // 核心 UI 刷新逻辑
    fun refreshDashboardStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            val submission = FakeDatabase.getSubmissionForUser(user?.id ?: "")
            withContext(Dispatchers.Main) {

                // 基础检查：Faculty 是否通过（通过了就应该显式变绿）
                val isFacultyDone = submission != null && (submission.hasConfirmationLetter || submission.status != SubmissionStatus.PENDING)

                when (submission?.status) {
                    // 状态 5：Officer 最终批准 (全绿勾)
                    SubmissionStatus.OFFICER_APPROVED -> updateUiToFinalSuccess()

                    // 状态 4：学生已付钱并上传，等 Officer 审批 (1绿, 2黄, 按钮禁用)
                    SubmissionStatus.SUBMITTED -> updateUiToWaitingOfficerApprove()

                    // 状态 3：学院通过了 (APPROVED)，学生需要付钱 (1号圈高亮)
                    SubmissionStatus.APPROVED -> updateUiToPaymentStage(isFacultyDone)

                    // 状态 2：审核中 (学院审核)
                    SubmissionStatus.REVIEWING -> updateUiToFacultyReviewing()

                    else -> resetUiToInitialStage(isFacultyDone)
                }
            }
        }
    }

    // --- 各阶段状态 UI 更新方法 ---

    private fun updateFacultyStatusToComplete() {
        findViewById<ImageView>(R.id.ic_warning)?.apply {
            setImageResource(R.drawable.ic_check)
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.green_success)
            imageTintList = ContextCompat.getColorStateList(context, R.color.white)
        }
        findViewById<TextView>(R.id.tvStatusTitle)?.text = "Faculty Documents Ready"
    }

    private fun updateUiToPaymentStage(isFacultyDone: Boolean) {
        if (isFacultyDone) updateFacultyStatusToComplete()
        setStepCircleActive(R.id.tvPaymentStepCircle, "1")
        findViewById<TextView>(R.id.tvPaymentStepStatus)?.text = "Waiting for Payment"

        btnContinueRenewal.isEnabled = true
        btnContinueRenewal.text = "PROCEED TO PAYMENT"
    }

    private fun updateUiToWaitingOfficerApprove() {
        updateFacultyStatusToComplete()
        setStepCircleCompleted(R.id.tvPaymentStepCircle)

        // 步骤 2 变成黄色/活动状态
        findViewById<TextView>(R.id.tvVisaUnitStepCircle)?.apply {
            text = "2"
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.accent_gold)
            setTextColor(ContextCompat.getColor(context, R.color.white))
        }
        findViewById<TextView>(R.id.tvVisaUnitStepText)?.text = "Documents with Visa Unit"

        btnContinueRenewal.isEnabled = false
        btnContinueRenewal.text = "PENDING OFFICER APPROVAL"
        btnContinueRenewal.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_text))
    }

    private fun updateUiToFinalSuccess() {
        // 必须强制刷新所有步骤为绿色勾选，防止状态清空感
        updateFacultyStatusToComplete()
        setStepCircleCompleted(R.id.tvPaymentStepCircle)
        setStepCircleCompleted(R.id.tvVisaUnitStepCircle)
        setStepCircleCompleted(R.id.tvStep3Circle)

        findViewById<TextView>(R.id.tvStatusTitle)?.text = "Renewal Process Complete"
        findViewById<TextView>(R.id.tvVisaUnitStepText)?.text = "Documents Approved"
        findViewById<TextView>(R.id.tvStep3Status)?.text = "Visa Issued Successfully"

        btnContinueRenewal.isEnabled = true
        btnContinueRenewal.text = "DOWNLOAD E-VISA"
        btnContinueRenewal.setBackgroundColor(ContextCompat.getColor(this, R.color.green_success))
    }

    private fun updateUiToFacultyReviewing() {
        findViewById<TextView>(R.id.tvStatusTitle)?.text = "Faculty Reviewing Documents..."
        btnContinueRenewal.isEnabled = false
        btnContinueRenewal.text = "UNDER FACULTY REVIEW"
    }

    private fun resetUiToInitialStage(isFacultyDone: Boolean) {
        if (isFacultyDone) updateFacultyStatusToComplete()
        btnContinueRenewal.isEnabled = true
        btnContinueRenewal.text = "CONTINUE RENEWAL"
    }

    // --- 工具方法 ---

    private fun setStepCircleCompleted(textViewId: Int) {
        findViewById<TextView>(textViewId)?.apply {
            text = "✓"
            setTextColor(ContextCompat.getColor(context, R.color.white))
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.green_success)
        }
    }

    private fun setStepCircleActive(textViewId: Int, stepNumber: String) {
        findViewById<TextView>(textViewId)?.apply {
            text = stepNumber
            setTextColor(ContextCompat.getColor(context, R.color.white))
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.accent_gold)
        }
    }

    fun showFragment(fragment: Fragment) {
        fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
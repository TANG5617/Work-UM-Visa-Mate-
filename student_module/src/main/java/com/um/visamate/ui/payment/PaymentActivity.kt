package com.um.visamate.ui.payment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.um.visamate.R
import com.um.visamate.ui.dashboard.FeePaymentFragment

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使用上面新建的布局
        setContentView(R.layout.activity_payment_wrapper)

        if (savedInstanceState == null) {
            // 第一次进入时，加载支付 Fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.payment_fragment_container, FeePaymentFragment())
                .commit()
        }
    }
}
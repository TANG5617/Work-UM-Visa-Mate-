package com.um.visamate.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.um.visamate.R
import com.um.visamate.ui.login.LoginActivity

// SettingsActivity - Updated for new design
class SettingsActivity : AppCompatActivity() {
    private lateinit var switchPush: SwitchCompat
    private lateinit var switchEmail: SwitchCompat
    private lateinit var switchDeadline: SwitchCompat
    private lateinit var btnBack: ImageView
    private lateinit var btnChangePassword: LinearLayout
    private lateinit var btnPrivacy: LinearLayout
    private lateinit var btnTerms: LinearLayout
    private lateinit var btnLogout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        switchPush = findViewById(R.id.switchPush)
        switchEmail = findViewById(R.id.switchEmail)
        switchDeadline = findViewById(R.id.switchDeadline)
        btnBack = findViewById(R.id.btnBack)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnPrivacy = findViewById(R.id.btnPrivacy)
        btnTerms = findViewById(R.id.btnTerms)
        btnLogout = findViewById(R.id.btnLogout)

        btnBack.setOnClickListener {
            finish()
        }

        // Notification toggles (mock implementation)
        switchPush.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Push notifications: $isChecked", Toast.LENGTH_SHORT).show()
        }

        switchEmail.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Email notifications: $isChecked", Toast.LENGTH_SHORT).show()
        }

        switchDeadline.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Deadline reminders: $isChecked", Toast.LENGTH_SHORT).show()
        }

        btnChangePassword.setOnClickListener {
            Toast.makeText(this, "Change password", Toast.LENGTH_SHORT).show()
        }

        btnPrivacy.setOnClickListener {
            Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show()
        }

        btnTerms.setOnClickListener {
            Toast.makeText(this, "Terms & Conditions", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            // Logout and return to login screen
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

package com.um.visamate.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
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

class RegisterActivity : AppCompatActivity() {
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var cardRoleStudent: MaterialCardView
    private lateinit var cardRoleFaculty: MaterialCardView
    private lateinit var cardRoleVisa: MaterialCardView
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLoginLink: TextView

    private var selectedRole: Role = Role.APPLICANT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cardRoleStudent = findViewById(R.id.cardRoleStudent)
        cardRoleFaculty = findViewById(R.id.cardRoleFaculty)
        cardRoleVisa = findViewById(R.id.cardRoleVisa)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)

        setupRoleCards()

        btnRegister.setOnClickListener {
            handleRegister()
        }

        tvLoginLink.setOnClickListener {
            finish() // Go back to LoginActivity
        }
    }

    private fun setupRoleCards() {
        cardRoleStudent.setOnClickListener { setRole(Role.APPLICANT, cardRoleStudent) }
        cardRoleFaculty.setOnClickListener { setRole(Role.REVIEWER, cardRoleFaculty) }
        cardRoleVisa.setOnClickListener { setRole(Role.OFFICER, cardRoleVisa) }
        
        // Default selection
        setRole(Role.APPLICANT, cardRoleStudent)
    }

    private fun setRole(role: Role, selectedCard: MaterialCardView) {
        selectedRole = role
        
        cardRoleStudent.isChecked = false
        cardRoleFaculty.isChecked = false
        cardRoleVisa.isChecked = false
        
        selectedCard.isChecked = true
    }

    private fun handleRegister() {
        val nameText = etName.text?.toString()?.trim().orEmpty()
        val emailText = etEmail.text?.toString()?.trim().orEmpty()
        val passwordText = etPassword.text?.toString().orEmpty()

        if (nameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty()) {
            if (nameText.isEmpty()) etName.error = "Required"
            if (emailText.isEmpty()) etEmail.error = "Required"
            if (passwordText.isEmpty()) etPassword.error = "Required"
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            // Check if user already exists
            val alreadyExists = FakeDatabase.userExists(emailText)

            if (alreadyExists) {
                 withContext(Dispatchers.Main) {
                     Toast.makeText(this@RegisterActivity, "User already exists! Please login.", Toast.LENGTH_SHORT).show()
                 }
                 return@launch
            }

            // Create new user
            // We use email prefix as ID for simplicity
            val userId = "user-${System.currentTimeMillis()}" 
            val newUser = User(
                id = userId,
                name = nameText,
                email = emailText,
                role = selectedRole
            )

            FakeDatabase.addUser(newUser)
            FakeDatabase.currentUser = newUser

            withContext(Dispatchers.Main) {
                Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                navigateToDashboard(newUser)
            }
        }
    }

    private fun navigateToDashboard(user: User) {
        val intent = when (user.role) {
            Role.APPLICANT -> Intent(this, DashboardActivity::class.java)
            Role.REVIEWER -> Intent(this, FacultyPortalActivity::class.java)
            Role.ADMIN -> Intent(this, OfficerDashboardActivity::class.java)
            Role.OFFICER -> Intent(this, OfficerDashboardActivity::class.java)
        }
        intent.putExtra("userId", user.id)
        // Clear back stack so user can't go back to register page
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

package com.example.zenflow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zenflow.data.ProfileSharedPreferencesManager
import com.example.zenflow.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var profilePrefs: ProfileSharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profilePrefs = ProfileSharedPreferencesManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.signupScreen) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Sign up button - validate and save
        binding.signupBtnMain.setOnClickListener {
            performSignup()
        }

        // Sign in text - navigate to SigninActivity
        binding.signinBtnSmall.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performSignup() {
        val name = binding.signupName.text.toString().trim()
        val email = binding.signupEmail.text.toString().trim()
        val password = binding.signupPwd.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            binding.signupName.error = "Name is required"
            return
        }

        if (email.isEmpty()) {
            binding.signupEmail.error = "Email is required"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signupEmail.error = "Invalid email address"
            return
        }

        if (password.isEmpty()) {
            binding.signupPwd.error = "Password is required"
            return
        }

        if (password.length < 6) {
            binding.signupPwd.error = "Password must be at least 6 characters"
            return
        }

        // Save user credentials
        profilePrefs.saveUserCredentials(name, email, password)
        Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()

        // Navigate to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
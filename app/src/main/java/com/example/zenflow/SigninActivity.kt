package com.example.zenflow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zenflow.data.ProfileSharedPreferencesManager
import com.example.zenflow.databinding.ActivitySigninBinding

class SigninActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var profilePrefs: ProfileSharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profilePrefs = ProfileSharedPreferencesManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.signinScreen) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Sign in button - validate credentials
        binding.signinBtnMain.setOnClickListener {
            performSignin()
        }

        // Sign up text - navigate to SignupActivity
        binding.signupBtnSmall.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performSignin() {
        val email = binding.signinEmail.text.toString().trim()
        val password = binding.signinPwd.text.toString().trim()

        // Validation
        if (email.isEmpty()) {
            binding.signinEmail.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            binding.signinPwd.error = "Password is required"
            return
        }

        // Verify credentials
        if (profilePrefs.verifyCredentials(email, password)) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }
}
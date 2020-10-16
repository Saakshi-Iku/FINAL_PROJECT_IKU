package com.iku

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.iku.databinding.ActivityEmailInputBinding

class EmailInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailInputBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailInputBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = Firebase.analytics
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        binding.enterEmail.requestFocus()
        binding.backButton.setOnClickListener { onBackPressed() }
        binding.emailNextButton.setOnClickListener {
            binding.emailNextButton.isEnabled = false
            val email = binding.enterEmail.text.toString().trim()
            if (email.isEmpty()) {
                binding.enterEmail.error = "Cannot be empty"
                binding.emailNextButton.isEnabled = true
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter valid email-ID", Toast.LENGTH_SHORT).show()
                binding.emailNextButton.isEnabled = true
                return@setOnClickListener
            }
            checkUser(email)
        }
    }

    private fun checkUser(email: String) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(this) { task ->
                    if (task.result.signInMethods!!.size == 0) {
                        firebaseAnalytics.logEvent("email_entered") {
                            param(FirebaseAnalytics.Param.METHOD, "Email")
                            param("state", "new user")
                        }
                        startActivity(Intent(this, NewPasswordInputActivity::class.java).putExtra("email",email))
                    } else {
                        firebaseAnalytics.logEvent("email_entered") {
                            param(FirebaseAnalytics.Param.METHOD, "Email")
                            param("state", "existing user")
                        }
                        startActivity(Intent(this, PasswordInputActivity::class.java).putExtra("email",email))
                    }
                }
                .addOnFailureListener { e ->
                    binding.emailNextButton.isEnabled = true
                    e.printStackTrace()
                    firebaseAnalytics.logEvent("email_entered") {
                        param(FirebaseAnalytics.Param.METHOD, "Email")
                        param("state", "failed to fetch method")
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        binding.emailNextButton.isEnabled = true
    }
}
package com.iku

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.iku.databinding.ActivityNewPasswordInputBinding

class NewPasswordInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewPasswordInputBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPasswordInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val email = intent.getStringExtra("email")
        mAuth = FirebaseAuth.getInstance()
        mFirebaseAnalytics = Firebase.analytics
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        binding.backButton.setOnClickListener { onBackPressed() }
        binding.newPasswordNextButton.setOnClickListener {
            val password = binding.enterNewPassword.text.toString().trim()
            binding.enterNewPassword.isEnabled = false
            if (password.isEmpty()) {
                binding.enterNewPassword.isEnabled = true
                binding.enterNewPassword.error = "Password cannot be empty"
                return@setOnClickListener
            }
            if (password.length < 8) {
                binding.enterNewPassword.isEnabled = true
                binding.enterNewPassword.error = "Password must be at least 8 characters."
                return@setOnClickListener
            }
            mAuth.createUserWithEmailAndPassword(email.toString(), password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
                        param(FirebaseAnalytics.Param.METHOD, "Email")
                        param("auth_status", "created")
                    }
                    val user: FirebaseUser? = mAuth.currentUser
                    user?.sendEmailVerification()?.addOnSuccessListener {
                        Toast.makeText(this@NewPasswordInputActivity, "Verification email sent.", Toast.LENGTH_SHORT).show()
                        mFirebaseAnalytics.logEvent("user_verified") {
                            param(FirebaseAnalytics.Param.METHOD, "Email")
                            param("verification_email_status", "sent")
                        }
                    }?.addOnFailureListener {
                        binding.enterNewPassword.isEnabled = true
                        mFirebaseAnalytics.logEvent("user_verified") {
                            param(FirebaseAnalytics.Param.METHOD, "Email")
                            param("verification_email_status", "failed")
                        }
                    }
                    startActivity(Intent(this, NameInputActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) })
                } else
                    Toast.makeText(this@NewPasswordInputActivity, "There was an error creating your account.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        binding.enterNewPassword.isEnabled = true
        super.onResume()
    }
}
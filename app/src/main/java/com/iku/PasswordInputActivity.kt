package com.iku

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.iku.app.AppConfig
import com.iku.databinding.ActivityPasswordInputBinding
import java.text.SimpleDateFormat
import java.util.*


class PasswordInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordInputBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var db: FirebaseFirestore
    private lateinit var user: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        mFirebaseAnalytics = Firebase.analytics
        val email = intent.getStringExtra("email")
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        binding.enterPassword.doOnTextChanged { _, _, _, _ -> binding.enterPasswordLayout.isErrorEnabled = false }
        binding.enterPassword.requestFocus()
        binding.backButton.setOnClickListener { onBackPressed() }
        binding.signinButton.setOnClickListener {
            progressBarView(true)
            binding.signinButton.isEnabled = false
            if (binding.enterPassword.text.toString().isEmpty()) {
                progressBarView(false)
                binding.signinButton.isEnabled = true
                Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                mAuth.signInWithEmailAndPassword(email.toString(), binding.enterPassword.text.toString()).addOnCompleteListener { tasks: Task<AuthResult?> ->
                    if (tasks.isSuccessful) {
                        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                            if (it.isSuccessful) {
                                // Token
                                val token = it.result.token
                                user = mAuth.currentUser!!
                                db.collection(AppConfig.USERS_COLLECTION).document(user.uid).get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
                                    if (task.isSuccessful) {
                                        if (task.result.exists()) {
                                            sendRegistrationToken(token, user.uid)
                                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                                                param(FirebaseAnalytics.Param.METHOD, "Email")
                                                param("status", "login_successful")
                                            }
                                            startActivity(Intent(this, HomeActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) })
                                        } else {
                                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                                                param(FirebaseAnalytics.Param.METHOD, "Email")
                                                param("user_returned", "completing_name_input")
                                            }
                                            startActivity(Intent(this, NameInputActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) })
                                        }
                                    } else {
                                        binding.signinButton.isEnabled = true
                                    }
                                }
                            }
                        }
                    } else {
                        progressBarView(false)
                        binding.signinButton.isEnabled = true
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                            param(FirebaseAnalytics.Param.METHOD, "Email")
                            param("failure_reason", "incorrect_password")
                        }
                        binding.enterPasswordLayout.error = "Incorrect Password!"
                    }
                }
            }
        }
        binding.forgotPasswordTextView.setOnClickListener {
            mAuth.sendPasswordResetEmail(email.toString()).addOnSuccessListener {
                Toast.makeText(this, "Password reset instructions sent via email", Toast.LENGTH_LONG).show()
                mFirebaseAnalytics.logEvent("password_reset_email") {
                    param("state", "password_reset_sent")
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Email not sent. Contact support.", Toast.LENGTH_LONG).show()
                mFirebaseAnalytics.logEvent("password_reset_email") {
                    param("state", "Failed_for " + user.uid)
                }
            }
            object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.forgotPasswordTextView.isEnabled = false
                    binding.forgotPasswordTextView.text = applicationContext.getString(R.string.resend_email_timer_text, SimpleDateFormat("ss", Locale.US).format(Date(millisUntilFinished)))
                }

                override fun onFinish() {
                    binding.forgotPasswordTextView.isEnabled = true
                    binding.forgotPasswordTextView.text = getString(R.string.forgot_password_question)
                }
            }.start()
        }
    }

    override fun onResume() {
        binding.signinButton.isEnabled = true
        super.onResume()
    }

    private fun progressBarView(value: Boolean) {
        if (value) {
            binding.progressBar.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            binding.progressBar.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun sendRegistrationToken(token: String, uid: String) {
        val data = mapOf(
                "registrationToken" to token,
                "uid" to uid
        )
        db.collection(AppConfig.REGISTRATION_TOKENS_COLLECTION).document(uid).set(data).addOnSuccessListener { }.addOnFailureListener { }
    }
}
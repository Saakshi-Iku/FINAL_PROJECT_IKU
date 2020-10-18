package com.iku

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.iku.PasswordInputActivity
import com.iku.databinding.ActivityPasswordInputBinding
import java.text.SimpleDateFormat
import java.util.*

class PasswordInputActivity : AppCompatActivity() {
    private var binding: ActivityPasswordInputBinding? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var db: FirebaseFirestore? = null
    private var mProgress: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordInputBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val extras = intent.extras
        val enteredEmail = extras!!.getString("email")
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        binding!!.enterPassword.requestFocus()
        initProgressDialog()
        binding!!.backButton.setOnClickListener { view: View? -> onBackPressed() }
        binding!!.signinButton.setOnClickListener { view: View? ->
            binding!!.signinButton.isEnabled = false
            if (binding!!.enterPassword.text.toString().isEmpty()) {
                binding!!.signinButton.isEnabled = true
                Toast.makeText(this@PasswordInputActivity, "Please enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                mProgress!!.show()
                firebaseAuth!!.signInWithEmailAndPassword(enteredEmail!!, binding!!.enterPassword.text.toString())
                        .addOnCompleteListener(this@PasswordInputActivity) { tasks: Task<AuthResult?> ->
                            FirebaseInstanceId.getInstance().instanceId
                                    .addOnCompleteListener { task: Task<InstanceIdResult> ->
                                        if (!task.isSuccessful) {
                                            return@addOnCompleteListener
                                        }

                                        // Get new Instance ID token
                                        val token = task.result.token
                                        if (tasks.isSuccessful) {
                                            user = firebaseAuth!!.currentUser
                                            val rootRef = FirebaseFirestore.getInstance()
                                            val docIdRef = rootRef.collection("users").document(user!!.uid)
                                            docIdRef.get().addOnCompleteListener { task1: Task<DocumentSnapshot> ->
                                                if (task1.isSuccessful) {
                                                    val document = task1.result
                                                    if (document.exists()) {
                                                        sendRegistrationToken(token, user!!.uid)
                                                        Toast.makeText(this@PasswordInputActivity, "Login successful", Toast.LENGTH_LONG).show()
                                                        //sending to Home Activity
                                                        mProgress!!.dismiss()
                                                        val goToHomeActivity = Intent(this@PasswordInputActivity, HomeActivity::class.java)
                                                        goToHomeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        startActivity(goToHomeActivity)

                                                        //log event
                                                        val password_bundle = Bundle()
                                                        password_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email")
                                                        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.LOGIN, password_bundle)
                                                    } else {
                                                        Toast.makeText(this@PasswordInputActivity, "User profile not filled!", Toast.LENGTH_LONG).show()
                                                        //sending to NameInput Activity
                                                        mProgress!!.dismiss()
                                                        val goToNameInputActivity = Intent(this@PasswordInputActivity, NameInputActivity::class.java)
                                                        goToNameInputActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        startActivity(goToNameInputActivity)
                                                    }
                                                } else {
                                                    binding!!.signinButton.isEnabled = true
                                                }
                                            }
                                        } else {
                                            mProgress!!.dismiss()
                                            binding!!.signinButton.isEnabled = true

                                            //log event
                                            val fail_bundle = Bundle()
                                            fail_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email")
                                            fail_bundle.putString("failure_reason", "incorrect password")
                                            mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.LOGIN, fail_bundle)
                                            Toast.makeText(this@PasswordInputActivity, "Incorrect password", Toast.LENGTH_LONG).show()
                                        }
                                    }
                        }
            }
        }
        binding!!.forgotPasswordTextView.setOnClickListener { view: View? ->
            firebaseAuth!!.sendPasswordResetEmail(enteredEmail!!)
                    .addOnSuccessListener { aVoid: Void? ->
                        Toast.makeText(this@PasswordInputActivity, "Password reset instructions sent via email", Toast.LENGTH_LONG).show()
                        //log event
                        val passwordReset_bundle = Bundle()
                        passwordReset_bundle.putString("state", "sent")
                        mFirebaseAnalytics!!.logEvent("password_reset_email", passwordReset_bundle)
                    }
                    .addOnFailureListener { e: Exception ->
                        Toast.makeText(this@PasswordInputActivity, "Email Not Sent" + e.message, Toast.LENGTH_LONG).show()
                        //log event
                        val failed_bundle = Bundle()
                        failed_bundle.putString("state", "failed")
                        mFirebaseAnalytics!!.logEvent("password_reset_email", failed_bundle)
                    }
            object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding!!.forgotPasswordTextView.isEnabled = false
                    binding!!.forgotPasswordTextView.text = "Resend in " + SimpleDateFormat("ss").format(Date(millisUntilFinished)) + "s"
                }

                override fun onFinish() {
                    binding!!.forgotPasswordTextView.text = "Forgot Password?"
                    binding!!.forgotPasswordTextView.isEnabled = true
                }
            }.start()
        }
    }

    private fun initProgressDialog() {
        mProgress = ProgressDialog(this)
        mProgress!!.setTitle("Logging in..")
        mProgress!!.setMessage("")
        mProgress!!.setCancelable(false)
        mProgress!!.isIndeterminate = true
    }

    override fun onResume() {
        super.onResume()
        binding!!.signinButton.isEnabled = true
    }

    private fun sendRegistrationToken(token: String, uid: String) {
        val userRegistrationTokenInfo: MutableMap<String, Any> = HashMap()
        userRegistrationTokenInfo["registrationToken"] = token
        userRegistrationTokenInfo["uid"] = uid
        db!!.collection("registrationTokens").document(uid)
                .set(userRegistrationTokenInfo)
                .addOnSuccessListener { aVoid: Void? -> }
                .addOnFailureListener { e: Exception? -> }
    }

    companion object {
        private val TAG = PasswordInputActivity::class.java.simpleName
    }
}
package com.iku

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        firebaseAnalytics = Firebase.analytics
        val user = mAuth.currentUser
        userInfoVerify()
        updateUI(user)
    }

    private fun updateUI(user: FirebaseUser?) {
        val sharedPref = this@SplashActivity.getPreferences(Context.MODE_PRIVATE)
        val previouslyStarted = sharedPref.getBoolean(getString(R.string.prev_started), false)
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        startActivity(Intent(this@SplashActivity, HomeActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) })
                    } else
                        startActivity(Intent(this@SplashActivity, NameInputActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) })
                }
            }
        } else if (!previouslyStarted) {
            with(sharedPref.edit()) {
                putBoolean(getString(R.string.prev_started), true)
                apply()
            }
            startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) })
        } else {
            startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) })
        }
    }

    private fun userInfoVerify() {
        if (mAuth.currentUser != null) {
            val userDevInfo = mapOf(
                    "Device" to Build.MANUFACTURER,
                    "Model" to Build.MODEL,
                    "Android" to Build.VERSION.SDK_INT,
                    "Release" to Build.VERSION.RELEASE,
                    "Kernel" to System.getProperty("os.version"),
                    "Version Name" to BuildConfig.VERSION_NAME,
                    "Version Code" to BuildConfig.VERSION_CODE
            )
            db.collection("usersVerifiedInfo").document(mAuth.uid!!)
                    .update(userDevInfo).addOnSuccessListener { }.addOnFailureListener { }
            db.collection("users").document(mAuth.uid!!).update("appVersion", BuildConfig.VERSION_NAME).addOnSuccessListener {
                firebaseAnalytics.logEvent("profile_update") {
                    param(FirebaseAnalytics.Param.METHOD, "user_updated_app")
                    param("app_update", "good ikulogist update app on " + FieldValue.serverTimestamp())
                }
            }
                    .addOnFailureListener { }
        }
    }
}
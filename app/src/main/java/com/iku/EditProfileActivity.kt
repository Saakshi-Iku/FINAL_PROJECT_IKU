package com.iku

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amulyakhare.textdrawable.TextDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.iku.app.AppConfig
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        mAuth = Firebase.auth
        db = Firebase.firestore
        firebaseAnalytics = Firebase.analytics
        val user = mAuth.currentUser
        initItems(user)
        getDetails(user)
    }

    private fun initItems(user: FirebaseUser?) {
        userBio.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                characterCount.text = (140 - s.toString().length).toString()
            }
        })
        back_button.setOnClickListener { onBackPressed() }
        save_button.setOnClickListener {
            val name = editUserNameField.text.toString().trim().capitalize(Locale.ROOT)
            if (linkInBio.text.toString().trim().isNotEmpty()) {
                if (!isValidUrl(linkInBio.text.toString().trim())) {
                    Toast.makeText(applicationContext, "Invalid URL", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            if (name.isNotEmpty() && user != null)
                saveUserDetails(user, name)
        }
    }

    private fun saveUserDetails(user: FirebaseUser, name: String) {
        val bio = userBio.text.toString().trim()
        val optionalLink = linkInBio.text.toString().trim()
        val parts = name.split(" ").toMutableList()
        val firstName = parts.firstOrNull()?.capitalize(Locale.ROOT)
        parts.removeAt(0)
        val lastName = parts.joinToString(" ").capitalize(Locale.ROOT)
        if (name == user.displayName) {
            updateBio(bio, optionalLink, user.uid)
        } else {
            MaterialAlertDialogBuilder(this)
                    .setTitle("Confirm")
                    .setMessage("Are you sure you want to change your name?\nThis cannot be undone")
                    .setNegativeButton("Cancel") { _, _ ->
                    }
                    .setPositiveButton("Obviously") { _, _ ->
                        val profileUpdates = userProfileChangeRequest {
                            displayName = name
                        }
                        db.collection(AppConfig.USERS_COLLECTION).document(user.uid).get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val document = task.result
                                if (document.exists()) {
                                    val nameCheck = document.get("nameEdited") as Boolean?
                                    if (nameCheck != true) {
                                        val nameData = mapOf(
                                                "firstName" to firstName,
                                                "lastName" to lastName,
                                                "nameEdited" to true,
                                                "nameUpdatedTime" to FieldValue.serverTimestamp()
                                        )
                                        user.updateProfile(profileUpdates).addOnCompleteListener {
                                            db.collection(AppConfig.USERS_COLLECTION).document(user.uid)
                                                    .update(nameData).addOnSuccessListener { }
                                        }
                                    }
                                }
                            }
                        }
                        updateBio(bio, optionalLink, user.uid)
                    }
                    .show()
        }
    }

    private fun updateBio(bio: String, link: String, uid: String) {
        val data = mapOf(
                "userBio" to bio,
                "userBioLink" to link,
                "bioLastUpdateOn" to FieldValue.serverTimestamp()
        )
        db.collection(AppConfig.USERS_COLLECTION).document(uid)
                .update(data).addOnSuccessListener { onBackPressed() }.addOnFailureListener {
                    firebaseAnalytics.logEvent("profile_update") {
                        param(FirebaseAnalytics.Param.METHOD, "User tried updating Info")
                        param("state", "Failure updating")
                    }
                }
    }

    private fun getDetails(user: FirebaseUser?) {
        if (user != null) {
            editUserNameField.setText(user.displayName)
            userName.text = user.displayName
            db.collection(AppConfig.USERS_COLLECTION).document(user.uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        val nameCheck = document.get("nameEdited") as Boolean?
                        if (nameCheck == true) {
                            editUserName.visibility = View.GONE
                            userName.visibility = View.VISIBLE
                        } else {
                            userName.visibility = View.GONE
                            editUserName.visibility = View.VISIBLE
                        }
                    }
                    info_layout.visibility = View.VISIBLE
                    progress_circular.visibility = View.GONE
                }
            }
            db.collection(AppConfig.USERS_COLLECTION).document(user.uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        userBio.setText(document.get("userBio") as String?)
                        linkInBio.setText(document.get("userBioLink") as String?)
                    }
                }
            }
            val profileUrl = intent.extras?.get("EXTRA_PROFILE_URL")
            if (profileUrl != null) {
                Picasso.get().load(profileUrl.toString())
                        .noFade()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(profileImage, object : Callback {
                            override fun onSuccess() {}
                            override fun onError(e: Exception) {
                                Picasso.get()
                                        .load(profileUrl.toString())
                                        .noFade()
                                        .into(profileImage)
                            }
                        })

            } else {
                val profileName: String = intent.extras?.get("EXTRA_PROFILE_NAME") as String
                val firstLetter = profileName[0].toString()
                val secondLetter: String = profileName.substring(profileName.indexOf(' ') + 1, profileName.indexOf(' ') + 2).trim { it <= ' ' }
                val drawable = TextDrawable.builder()
                        .beginConfig()
                        .width(200)
                        .height(200)
                        .endConfig()
                        .buildRect(firstLetter + secondLetter, Color.DKGRAY)

                profileImage.setImageDrawable(drawable)
            }
        }
    }

    /**
     * This is used to check the given URL is valid or not.
     *
     * @param url
     * @return true if url is valid, false otherwise.
     */
    private fun isValidUrl(url: String): Boolean {
        val p = Patterns.WEB_URL
        val m = p.matcher(url.toLowerCase(Locale.ROOT))
        return m.matches()
    }
}
package com.iku

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.amulyakhare.textdrawable.TextDrawable
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.iku.adapter.HabitsAdapter
import com.iku.app.AppConfig
import com.iku.models.HabitsModel
import com.iku.ui.UserPostsAdapter
import com.iku.viewmodel.UserPostsViewModel
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.add_habit.view.*
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var mAuth: FirebaseAuth
    private lateinit var photoUrl: String
    private lateinit var adapter: UserPostsAdapter
    private lateinit var habitsAdapter: HabitsAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(UserPostsViewModel::class.java) }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        db = Firebase.firestore
        mFirebaseAnalytics = Firebase.analytics
        mAuth = Firebase.auth
        user = mAuth.currentUser!!
        adapter = UserPostsAdapter(requireContext())
        observeData()
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userPostsRecyclerView.adapter = adapter
        initButtons()
        profileDetails()
        userDetails()
    }

    @SuppressLint("InflateParams")
    private fun initButtons() {
        settingsButton.setOnClickListener { startActivity(Intent(activity, SettingsActivity::class.java)) }
        edit_profile_button.setOnClickListener { startActivity(Intent(activity, SettingsActivity::class.java).putExtra("EXTRA_PROFILE_URL", "photoUrl").putExtra("EXTRA_PROFILE_NAME", user.displayName)) }
        linkInBio.setOnClickListener {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent: CustomTabsIntent = builder.build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse(linkInBio.text.toString().trim()))
        }
        add_a_new_habit_button.setOnClickListener {
            val bottomSheet = layoutInflater.inflate(R.layout.add_habit, null)
            val dialog = BottomSheetDialog(requireContext())
            bottomSheet.listHabitsPredefined.layoutManager = LinearLayoutManager(requireContext())
            bottomSheet.listHabitsPredefined.setHasFixedSize(true)
            val query = db.collection("habits")
            val options = FirestoreRecyclerOptions.Builder<HabitsModel>().setQuery(query, HabitsModel::class.java).setLifecycleOwner(this).build()
            habitsAdapter = HabitsAdapter(options)
            habitsAdapter.startListening()
            bottomSheet.listHabitsPredefined.adapter = habitsAdapter
            bottomSheet.addHabitButton.setOnClickListener {
                val editHabitBottomSheet = layoutInflater.inflate(R.layout.edit_habit_bottom_sheet, null)
                val editHabitDialog = BottomSheetDialog(requireContext())
                dialog.dismiss()
                editHabitDialog.setContentView(editHabitBottomSheet)
                editHabitDialog.show()
            }
            dialog.setContentView(bottomSheet)
            dialog.show()
        }
    }

    private fun observeData() {
        viewModel.fetchPostsData().observe(viewLifecycleOwner, {
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun profileDetails() {
        val originalPieceOfUrl = "s96-c"
        val newPieceOfUrlToAdd = "s800-c"
        userName.text = user.displayName
        val photoUri = user.photoUrl
        if (photoUri != null) {
            photoUrl = photoUri.toString()
            photoUrl = photoUrl.replace(originalPieceOfUrl, newPieceOfUrlToAdd)
            photoUrl = "$photoUrl?height=500"
            storePictureToDb(photoUrl)
            Picasso.get()
                    .load(photoUrl)
                    .noFade()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(profileImage, object : Callback {
                        override fun onSuccess() {}
                        override fun onError(e: Exception) {
                            Picasso.get()
                                    .load(photoUrl)
                                    .noFade()
                                    .into(profileImage)
                        }
                    })
        } else {
            val displayName = user.displayName
            val firstLetter: String
            val secondLetter: String
            if (displayName != null) {
                firstLetter = displayName[0].toString()
                secondLetter = displayName.substring(displayName.indexOf(' ') + 1, displayName.indexOf(' ') + 2).trim()
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

    private fun storePictureToDb(photoUrl: String) {
        db.collection(AppConfig.USERS_COLLECTION).document(user.uid).update("imageUrl", photoUrl).addOnSuccessListener {
            mFirebaseAnalytics.logEvent("profile_picture") {
                param("received_picture", "User has google or FB picture")
            }
        }
    }

    private fun userDetails() {
        db.collection(AppConfig.USERS_COLLECTION).whereEqualTo("uid", user.uid)
                .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot: QuerySnapshot?, _: FirebaseFirestoreException? ->
                    if (querySnapshot != null) {
                        for (change in querySnapshot.documentChanges) {
                            if (change.type == DocumentChange.Type.ADDED) {
                                val points = change.document["points"] as Long
                                val bio = change.document["userBio"] as String?
                                val link = change.document["userBioLink"] as String?
                                if (bio != null && bio != "") {
                                    userBioView.visibility = View.VISIBLE
                                    userBio.text = bio
                                }
                                if (link != null && link != "") {
                                    linkInBioView.visibility = View.VISIBLE
                                    linkInBio.text = link
                                }
                                if (points == 0L) {
                                    userHearts.visibility = View.GONE
                                    addnTextView!!.setText(R.string.yet_to_win_hearts)
                                } else userHearts.text = change.document.getLong("points").toString()
                            }
                        }
                    }
                }
    }
}
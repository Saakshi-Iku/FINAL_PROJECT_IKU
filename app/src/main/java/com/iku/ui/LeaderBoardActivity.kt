package com.iku.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.iku.HabitsActivity
import com.iku.R
import com.iku.UserProfileActivity
import com.iku.app.AppConfig
import com.iku.viewmodel.LeaderBoardViewModel
import kotlinx.android.synthetic.main.activity_leaderboard.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class LeaderBoardActivity : AppCompatActivity(), RecyclerView.OnItemTouchListener {
    private lateinit var adapter: LeaderBoardAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(LeaderBoardViewModel::class.java) }
    private var totalHearts = 0
    private var totalPlayers = 0
    private lateinit var tapDetector: GestureDetector
    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        back_button.setOnClickListener { onBackPressed() }
        hofButton.setOnClickListener { startActivity(Intent(this,HallOfFameActivity::class.java)) }
        tapDetector = GestureDetector(this, TapListener())
        firebaseAnalytics = Firebase.analytics
        FirebaseFirestore.getInstance().collection(AppConfig.USERS_COLLECTION).get().addOnCompleteListener { task: Task<QuerySnapshot> ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    totalPlayers++
                    val userPoints = document["points"] as Long?
                    if (userPoints != null) {
                        val i: Int = userPoints.toInt()
                        totalHearts += i
                    }
                }
                heartscount.text = totalHearts.toString()
                playerscount.text = totalPlayers.toString()
            }
        }
        adapter = LeaderBoardAdapter(this)
        leaderboard_recyclerview.layoutManager = LinearLayoutManager(this)
        leaderboard_recyclerview.addOnItemTouchListener(this)
        leaderboard_recyclerview.adapter = adapter
        observeData()
    }

    private fun observeData() {
        viewModel.fetchUserdata().observe(this, {
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }

    private inner class TapListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val view: View? = leaderboard_recyclerview.findChildViewUnder(e.x, e.y)
            if (view != null) {
                val position: Int = leaderboard_recyclerview.getChildAdapterPosition(view)
                val uid: String? = adapter.getItem(position)?.uid
                val name: String? = adapter.getItem(position)?.firstName + adapter.getItem(position)?.lastName
                if (uid.equals(user?.uid)) {
                    Toast.makeText(this@LeaderBoardActivity, "- drum roll -", Toast.LENGTH_SHORT).show()
                    viewConfetti.build()
                            .addColors(Color.BLUE, Color.LTGRAY, getColor(R.color.colorPrimary), getColor(R.color.colorPrimary))
                            .setDirection(0.0, 359.0)
                            .setSpeed(1f, 8f)
                            .setFadeOutEnabled(true)
                            .setTimeToLive(2000L)
                            .addShapes(Shape.Square, Shape.Circle)
                            .addSizes(Size(10, 10f))
                            .setPosition(-50f, viewConfetti.width + 50f, -50f, -50f)
                            .streamFor(300, 5000L)
                } else {
                    firebaseAnalytics.logEvent("viewed_profile") {
                        param(FirebaseAnalytics.Param.METHOD, "Clicked")
                        param("Reason", user?.displayName.toString() + "Wanted to know about his opponent " + name)
                    }
                    this@LeaderBoardActivity.startActivity(Intent(this@LeaderBoardActivity, UserProfileActivity::class.java).putExtra("EXTRA_PERSON_NAME", name).putExtra("EXTRA_PERSON_UID", uid))
                }
            }
            return super.onSingleTapConfirmed(e)
        }
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        tapDetector.onTouchEvent(e)
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        TODO("Not yet implemented")
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        TODO("Not yet implemented")
    }
}
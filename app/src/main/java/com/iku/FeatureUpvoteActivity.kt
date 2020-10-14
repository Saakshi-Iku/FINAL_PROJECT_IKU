package com.iku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iku.adapter.FeatureUpvoteAdapter
import com.iku.models.FeatureUpvoteModel
import kotlinx.android.synthetic.main.activity_feature_upvote.*

class FeatureUpvoteActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseDb: FirebaseFirestore
    private var adapter: FeatureUpvoteAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_upvote)
        mAuth = FirebaseAuth.getInstance()
        firebaseDb = FirebaseFirestore.getInstance()
        back_button.setOnClickListener { onBackPressed() }
        val query: Query = firebaseDb.collection("feature_upvote").orderBy("timestamp", Query.Direction.DESCENDING)
        val options: FirestoreRecyclerOptions<FeatureUpvoteModel> = FirestoreRecyclerOptions.Builder<FeatureUpvoteModel>().setQuery(query, FeatureUpvoteModel::class.java).build()
        adapter = FeatureUpvoteAdapter(options, this)
        recyclerview.layoutManager = LinearLayoutManager(this)
        (recyclerview.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerview.setHasFixedSize(true)
        recyclerview.adapter = adapter
        adapter!!.setOnItemClickListener { position: Int, snapshot: DocumentSnapshot ->
            val featureUpvoteModel = snapshot.toObject(FeatureUpvoteModel::class.java)
            if (featureUpvoteModel != null) {
                val data = mapOf("feature" to featureUpvoteModel.title, "uid" to mAuth.uid, "timestamp" to System.currentTimeMillis(), "sequence" to featureUpvoteModel.upvote_count + 1, "row" to featureUpvoteModel.row)
                firebaseDb.collection("feature_upvote_users").add(data)
                firebaseDb.collection("feature_upvote").document(snapshot.id).update("upvote_count", FieldValue.increment(1), "upVotedUser", FieldValue.arrayUnion(mAuth.uid))
                adapter!!.notifyItemChanged(position)
            }
        }
    }

    override fun onStart() {
        adapter!!.startListening()
        super.onStart()
    }
}
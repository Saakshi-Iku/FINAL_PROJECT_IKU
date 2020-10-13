
package com.iku;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.iku.adapter.FeatureUpvoteAdapter;
import com.iku.databinding.ActivityFeatureUpvoteBinding;
import com.iku.models.FeatureUpvoteModel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FeatureUpvoteActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private FeatureUpvoteAdapter adapter;
    private ActivityFeatureUpvoteBinding featureUpvoteBinding;
    private String TAG = FeatureUpvoteActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        featureUpvoteBinding = ActivityFeatureUpvoteBinding.inflate(getLayoutInflater());
        setContentView(featureUpvoteBinding.getRoot());

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        featureUpvoteBinding.backButton.setOnClickListener(view -> onBackPressed());

        Query query = firebaseFirestore.collection("feature_upvote").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<FeatureUpvoteModel> options = new FirestoreRecyclerOptions.Builder<FeatureUpvoteModel>()
                .setQuery(query, FeatureUpvoteModel.class)
                .build();

        adapter = new FeatureUpvoteAdapter(options, getApplicationContext());
        featureUpvoteBinding.recyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ((SimpleItemAnimator) featureUpvoteBinding.recyclerview.getItemAnimator()).setSupportsChangeAnimations(false);
        featureUpvoteBinding.recyclerview.setLayoutManager(linearLayoutManager);
        featureUpvoteBinding.recyclerview.setAdapter(adapter);

        adapter.setOnItemClickListener((position, snapshot) -> {
            FeatureUpvoteModel featureUpvoteModel = snapshot.toObject(FeatureUpvoteModel.class);
            if (featureUpvoteModel != null) {
                Date d = new Date();
                long timestamp = d.getTime();
                Map<String, Object> data = new HashMap<>();
                data.put("feature", featureUpvoteModel.getTitle());
                data.put("uid", mAuth.getUid());
                data.put("timestamp", timestamp);
                data.put("sequence", FieldValue.increment(1));
                data.put("row", featureUpvoteModel.getRow());
                firebaseFirestore.collection("feature_upvote_users").add(data)
                        .addOnSuccessListener(documentReference -> {
                        })
                        .addOnFailureListener(e -> {
                        });

                firebaseFirestore.collection("feature_upvote")
                        .document(snapshot.getId())
                        .update("upvote_count", FieldValue.increment(1),
                                "upVotedUser", FieldValue.arrayUnion(mAuth.getUid()))
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                        });
                adapter.notifyItemChanged(position);
            }
        });
        adapter.startListening();
    }
}
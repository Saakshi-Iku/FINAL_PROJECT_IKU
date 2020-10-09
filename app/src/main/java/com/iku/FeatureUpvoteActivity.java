package com.iku;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.iku.adapter.FeatureUpvoteAdapter;
import com.iku.databinding.ActivityFeatureUpvoteBinding;
import com.iku.models.FeatureUpvoteModel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FeatureUpvoteActivity extends AppCompatActivity {

    private RecyclerView featureUpvoteRecyclerView;
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
        featureUpvoteRecyclerView = findViewById(R.id.recyclerview);

        Query upvotesQuery = firebaseFirestore.collection("feature_upvote").orderBy("upvote_count", Query.Direction.DESCENDING);
        Query timestampQuery = firebaseFirestore.collection("feature_upvote").orderBy("timestamp", Query.Direction.DESCENDING);
        queryDB(upvotesQuery);
        featureUpvoteBinding.mostRequested.setOnClickListener(view -> queryDB(upvotesQuery));
        featureUpvoteBinding.recentlyAdded.setOnClickListener(view -> queryDB(timestampQuery));

    }

    private void queryDB(Query query) {
        FirestoreRecyclerOptions<FeatureUpvoteModel> options = new FirestoreRecyclerOptions.Builder<FeatureUpvoteModel>()
                .setQuery(query, FeatureUpvoteModel.class)
                .build();

        adapter = new FeatureUpvoteAdapter(options);
        adapter.startListening();
        featureUpvoteRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ((SimpleItemAnimator) featureUpvoteRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        featureUpvoteRecyclerView.setLayoutManager(linearLayoutManager);
        featureUpvoteRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new FeatureUpvoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, DocumentSnapshot snapshot) {
                FeatureUpvoteModel FeatureUpvoteModel = snapshot.toObject(FeatureUpvoteModel.class);
                Date d = new Date();
                long timestamp = d.getTime();
                long present_upvote_count = FeatureUpvoteModel.getUpvote_count();

                Map<String, Object> data = new HashMap<>();
                data.put("feature", FeatureUpvoteModel.getTitle());
                data.put("uid", mAuth.getUid());
                data.put("timestamp", timestamp);
                data.put("sequence", present_upvote_count + 1);
                data.put("row", FeatureUpvoteModel.getRow());
                firebaseFirestore.collection("feature_upvote_users").add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });

                // Updating update count values

                firebaseFirestore.collection("feature_upvote")
                        .document(snapshot.getId())
                        .update("upvote_count", present_upvote_count + 1)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
                adapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
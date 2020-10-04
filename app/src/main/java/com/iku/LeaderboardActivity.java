package com.iku;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iku.adapter.LeaderBoardAdapter;
import com.iku.models.UserModel;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView mLeaderboardList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private LeaderBoardAdapter leaderboardadapter;

    private TextView heartscount;
    private TextView playerscount;
    private FirebaseFirestore db;
    private ImageView backButton;
    private MaterialTextView firstNameTextView;
    private int totalHearts, totalPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        backButton = findViewById(R.id.back_button);
        heartscount = findViewById(R.id.heartscount);
        playerscount = findViewById(R.id.playerscount);
        mLeaderboardList = findViewById(R.id.leaderboard_recyclerview);
        firstNameTextView = findViewById(R.id.firstname);

        Query query = firebaseFirestore.collection("users").orderBy("points", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(100)
                .setPageSize(25)
                .build();
        FirestorePagingOptions<UserModel> options = new FirestorePagingOptions.Builder<UserModel>()
                .setQuery(query, config, snapshot -> {
                    UserModel userModel = snapshot.toObject(UserModel.class);
                    String uid = snapshot.getId();
                    userModel.setUid(uid);
                    return userModel;
                })
                .setLifecycleOwner(this)
                .build();
        mLeaderboardList.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mLeaderboardList.setLayoutManager(linearLayoutManager);
        leaderboardadapter = new LeaderBoardAdapter(options);
        leaderboardadapter.startListening();
        mLeaderboardList.setAdapter(leaderboardadapter);

        firebaseFirestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            totalPlayers++;
                            Long userPoints = (Long) document.get("points");
                            if (userPoints != null) {
                                int i;
                                i = userPoints.intValue();
                                totalHearts += i;
                            }
                        }
                        heartscount.setText(Integer.toString(totalHearts));
                        playerscount.setText(Integer.toString(totalPlayers));
                    }
                });


        backButton.setOnClickListener(view -> onBackPressed());

        leaderboardadapter.setOnItemClickOfSameUidListener(() -> {
            final KonfettiView viewConfetti = findViewById(R.id.viewConfetti);

            Toast.makeText(LeaderboardActivity.this, "- drum roll -", Toast.LENGTH_SHORT).show();
            viewConfetti.build()
                    .addColors(Color.BLUE, Color.LTGRAY, getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent))
                    .setDirection(0.0, 359.0)
                    .setSpeed(1f, 8f)
                    .setFadeOutEnabled(true)
                    .setTimeToLive(2000L)
                    .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                    .addSizes(new Size(10, 10f))
                    .setPosition(-50f, viewConfetti.getWidth() + 50f, -50f, -50f)
                    .streamFor(300, 5000L);

        });

        leaderboardadapter.setOnItemClickOfDiffUidListener((String uid, String name) -> {
            Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
            if (user != null && name != null) {
                /*Log event*/
                Bundle curiousBundlle = new Bundle();
                curiousBundlle.putString(FirebaseAnalytics.Param.METHOD, "Clicked");
                curiousBundlle.putString("Reason", user.getDisplayName() + " Wanted to know about his opponent " + name);
                mFirebaseAnalytics.logEvent("viewed_someone", curiousBundlle);
            }
            userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
            userProfileIntent.putExtra("EXTRA_PERSON_UID", uid);
            startActivity(userProfileIntent);
        });
    }
}


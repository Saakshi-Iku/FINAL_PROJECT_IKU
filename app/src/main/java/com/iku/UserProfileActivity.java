package com.iku;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;
import com.iku.databinding.ActivityUserPofileBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {


    private ImageView profilePicture;
    private MaterialTextView nameTextView, userHeartsTextView, userLinkTextView, linkHeaderTextView,bioHeaderTextView,bioTextView;
    private FirebaseFirestore db;

    private String userName;

    private String userUID;

    private ActivityUserPofileBinding userPofileBinding;

    private String TAG = UserProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userPofileBinding = ActivityUserPofileBinding.inflate(getLayoutInflater());
        setContentView(userPofileBinding.getRoot());

        db = FirebaseFirestore.getInstance();

        Bundle extras = this.getIntent().getExtras();
        userName = extras.getString("EXTRA_PERSON_NAME");
        userUID = extras.getString("EXTRA_PERSON_UID");

        initButtons();

        nameTextView = findViewById(R.id.userName);
        nameTextView.setText(userName);
        profilePicture = findViewById(R.id.profileImage);
        userHeartsTextView = findViewById(R.id.userHearts);
        userLinkTextView = findViewById(R.id.linkInBio);
        linkHeaderTextView = findViewById(R.id.linkHeader);
        bioTextView=findViewById(R.id.userBio);
        bioHeaderTextView=findViewById(R.id.userBioHeader);
        getUserDetails(userUID);
        getPicture(userUID);
    }

    private void initButtons() {
        userPofileBinding.backButton.setOnClickListener(view -> {
            onBackPressed();
        });
        userPofileBinding.linkInBio.setOnClickListener(view -> {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(userPofileBinding.linkInBio.getText().toString().trim()));
        });
    }

    private void getPicture(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String firstLetter, secondLetter;
                                String url = (String) document.get("imageUrl");

                                if (url != null) {
                                    Picasso.get()
                                            .load(url)
                                            .noFade()
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(profilePicture, new Callback() {

                                                @Override
                                                public void onSuccess() {
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Picasso.get()
                                                            .load(url)
                                                            .noFade()
                                                            .into(profilePicture);
                                                }
                                            });
                                } else {

                                    firstLetter = String.valueOf(userName.charAt(0));
                                    secondLetter = userName.substring(userName.indexOf(' ') + 1, userName.indexOf(' ') + 2).trim();

                                    TextDrawable drawable = TextDrawable.builder()
                                            .beginConfig()
                                            .width(200)
                                            .height(200)
                                            .endConfig()
                                            .buildRect(firstLetter + secondLetter, Color.DKGRAY);

                                    profilePicture.setImageDrawable(drawable);
                                }
                            }
                        }
                    }
                });
    }

    private void getUserDetails(String uid) {
        db.collection("users").whereEqualTo("uid", uid)
                .addSnapshotListener(MetadataChanges.INCLUDE, (querySnapshot, e) -> {
                    if (e != null) {
                        return;
                    }

                    for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            long points = (long) change.getDocument().get("points");
                            String link = (String) change.getDocument().get("userBioLink");
                            String bio = (String) change.getDocument().get("userBio");
                            if (bio!=null&& !bio.equals("")){
                                userPofileBinding.userBioHeader.setVisibility(View.VISIBLE);
                                userPofileBinding.userBio.setVisibility(View.VISIBLE);
                                userPofileBinding.userBio.setText(bio);
                            }
                            if (link!=null&& !link.equals("")){
                                userPofileBinding.linkHeader.setVisibility(View.VISIBLE);
                                userPofileBinding.linkInBio.setVisibility(View.VISIBLE);
                                userPofileBinding.linkInBio.setText(link);
                            }

                            if (points == 0)
                                userHeartsTextView.setText(R.string.yet_to_win_hearts);
                            else
                                userHeartsTextView.setText("Hearts Won: " + change.getDocument().get("points"));
                        }
                    }
                });
    }
}
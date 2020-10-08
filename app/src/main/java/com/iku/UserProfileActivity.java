package com.iku;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.MetadataChanges;
import com.iku.databinding.ActivityUserPofileBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private String userName;

    private ActivityUserPofileBinding userPofileBinding;

    private String TAG = UserProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userPofileBinding = ActivityUserPofileBinding.inflate(getLayoutInflater());
        setContentView(userPofileBinding.getRoot());

        db = FirebaseFirestore.getInstance();
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            userName = extras.getString("EXTRA_PERSON_NAME");
            String userUID = extras.getString("EXTRA_PERSON_UID");
            initButtons();
            userPofileBinding.userName.setText(userName);
            getUserDetails(userUID);
            getPicture(userUID);
        }
    }

    private void initButtons() {
        userPofileBinding.backButton.setOnClickListener(view -> onBackPressed());
        userPofileBinding.linkInBio.setOnClickListener(view -> {
            Uri page;
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            if (!userPofileBinding.linkInBio.getText().toString().trim().startsWith("http://") && !userPofileBinding.linkInBio.getText().toString().trim().startsWith("https://")) {
                page = Uri.parse("http://" + userPofileBinding.linkInBio.getText().toString().trim());
                customTabsIntent.launchUrl(view.getContext(), page);
            } else
                customTabsIntent.launchUrl(view.getContext(), Uri.parse(userPofileBinding.linkInBio.getText().toString().trim()));
        });
    }

    private void getPicture(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
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
                                        .into(userPofileBinding.profileImage, new Callback() {

                                            @Override
                                            public void onSuccess() {
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Picasso.get()
                                                        .load(url)
                                                        .noFade()
                                                        .into(userPofileBinding.profileImage);
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

                                userPofileBinding.profileImage.setImageDrawable(drawable);
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

                    if (querySnapshot != null) {
                        for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                long points = (long) change.getDocument().get("points");
                                String link = (String) change.getDocument().get("userBioLink");
                                String bio = (String) change.getDocument().get("userBio");
                                if (bio != null && !bio.equals("")) {
                                    userPofileBinding.userBioView.setVisibility(View.VISIBLE);
                                    userPofileBinding.userBio.setText(bio);
                                }
                                if (link != null && !link.equals("")) {
                                    userPofileBinding.linkInBioView.setVisibility(View.VISIBLE);
                                    userPofileBinding.linkInBio.setText(link);
                                }

                                if (points == 0) {
                                    userPofileBinding.userHearts.setVisibility(View.GONE);
                                    userPofileBinding.userHearts.setText(R.string.yet_to_win_hearts);
                                } else
                                    userPofileBinding.userHearts.setText(String.valueOf(change.getDocument().getLong("points")));
                            }
                        }
                    }
                });
    }
}
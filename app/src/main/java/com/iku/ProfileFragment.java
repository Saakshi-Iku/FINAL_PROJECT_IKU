package com.iku;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.MetadataChanges;
import com.iku.databinding.FragmentProfileBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseUser user;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String photoUrl;
    private MaterialTextView userHeartsTextView, userHeartsAddnTextView, userBioTextView, userLinkTextView;
    private LinearLayout userBioView,userLinkView;
    private FragmentProfileBinding profileBinding;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = profileBinding.getRoot();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(view.getContext());
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        userHeartsTextView = view.findViewById(R.id.userHearts);
        userHeartsAddnTextView = view.findViewById(R.id.addnTextView);
        userBioTextView = view.findViewById(R.id.userBio);
        userLinkTextView = view.findViewById(R.id.linkInBio);
        userBioView = view.findViewById(R.id.userBioView);
        userLinkView = view.findViewById(R.id.linkInBioView);

        initButtons();
        getUserDetails();
        getProfileDetails();

        return view;
    }

    private void initButtons() {

        profileBinding.settingsButton.setOnClickListener(view -> {
            Intent goToSettingsPage = new Intent(getActivity(), SettingsActivity.class);
            startActivity(goToSettingsPage);
        });

        profileBinding.editProfileButton.setOnClickListener(view -> {
            Intent goToEditPage = new Intent(getActivity(), EditProfileActivity.class);
            goToEditPage.putExtra("EXTRA_PROFILE_URL", photoUrl);
            goToEditPage.putExtra("EXTRA_PROFILE_NAME", user.getDisplayName());
            startActivity(goToEditPage);
        });

        profileBinding.linkInBio.setOnClickListener(view -> {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(view.getContext(), Uri.parse(profileBinding.linkInBio.getText().toString().trim()));
        });

        profileBinding.addHabitButton.setOnClickListener(view -> {
            Intent goToHabitsPage = new Intent(getActivity(), HabitsActivity.class);
            startActivity(goToHabitsPage);
        });

    }

    private void getProfileDetails() {
        if (user != null) {
            String originalPieceOfUrl = "s96-c";
            String newPieceOfUrlToAdd = "s800-c";

            String personName = user.getDisplayName();
            profileBinding.userName.setText(personName);

            Uri photoUri = user.getPhotoUrl();
            if (photoUri != null) {

                photoUrl = photoUri.toString();

                photoUrl = photoUrl.replace(originalPieceOfUrl, newPieceOfUrlToAdd);

                photoUrl = photoUrl + "?height=500";

                storePictureToDB(photoUrl);

                Picasso.get()
                        .load(photoUrl)
                        .noFade()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(profileBinding.profileImage, new Callback() {

                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(photoUrl)
                                        .noFade()
                                        .into(profileBinding.profileImage);
                            }
                        });
            } else {
                String displayName = user.getDisplayName();
                String firstLetter, secondLetter;
                if (displayName != null) {
                    firstLetter = String.valueOf(displayName.charAt(0));
                    secondLetter = displayName.substring(displayName.indexOf(' ') + 1, displayName.indexOf(' ') + 2).trim();
                    TextDrawable drawable = TextDrawable.builder()
                            .beginConfig()
                            .width(200)
                            .height(200)
                            .endConfig()
                            .buildRect(firstLetter + secondLetter, Color.DKGRAY);

                    profileBinding.profileImage.setImageDrawable(drawable);
                }
            }
        }
    }

    private void storePictureToDB(String photoUrl) {
        if (user != null) {

            Map<String, Object> docData = new HashMap<>();
            docData.put("imageUrl", photoUrl);

            db.collection("users").document(user.getUid())
                    .update(docData)
                    .addOnSuccessListener(aVoid -> {

                        //Log event
                        Bundle down_params = new Bundle();
                        down_params.putString("received_picture", "User has google or FB picture");
                        mFirebaseAnalytics.logEvent("profile_picture", down_params);
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    private void getUserDetails() {
        if (user != null) {
            db.collection("users").whereEqualTo("uid", user.getUid())
                    .addSnapshotListener(MetadataChanges.INCLUDE, (querySnapshot, e) -> {
                        if (e != null) {
                            return;
                        }
                        if (querySnapshot != null) {
                            for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                                if (change.getType() == DocumentChange.Type.ADDED) {
                                    long points = (long) change.getDocument().get("points");
                                    String bio = (String) change.getDocument().get("userBio");
                                    String link = (String) change.getDocument().get("userBioLink");
                                    if (bio != null && !bio.equals("")) {
                                        userBioView.setVisibility(View.VISIBLE);
                                        userBioTextView.setText(bio);
                                    }
                                    if (link != null && !link.equals("")) {
                                        userLinkView.setVisibility(View.VISIBLE);
                                        userLinkTextView.setText(link);
                                    }

                                    if (points == 0) {
                                        userHeartsTextView.setVisibility(View.GONE);
                                        userHeartsAddnTextView.setText(R.string.yet_to_win_hearts);
                                    } else
                                        userHeartsTextView.setText(String.valueOf(change.getDocument().getLong("points")));
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserDetails();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        profileBinding = null;
    }
}

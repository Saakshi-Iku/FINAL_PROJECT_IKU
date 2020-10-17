package com.iku;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iku.databinding.ActivityHomeBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();
    boolean doubleBackToExitPressedOnce = false;
    private ActivityHomeBinding homeBinding;
    private FragmentManager fragmentManager;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        db = FirebaseFirestore.getInstance();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();

        verifyAdmin();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                if (findViewById(R.id.messageTextField) != null) {
                    if (findViewById(R.id.messageTextField).hasFocus())
                        homeBinding.animatedBottomBar.setVisibility(View.GONE);
                    else
                        homeBinding.animatedBottomBar.setVisibility(View.VISIBLE);
                }
            }
        }, 1000);

        if (savedInstanceState == null) {
            homeBinding.animatedBottomBar.selectTabById(R.id.chat, true);
            fragmentManager = getSupportFragmentManager();
            ChatFragment chatFragment = new ChatFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, chatFragment)
                    .commit();
        }

        homeBinding.animatedBottomBar.setOnTabSelectListener((lastIndex, lastTab, newIndex, newTab) -> {
            Fragment fragment = null;
            switch (newTab.getId()) {
                case R.id.chat:
                    fragment = new ChatFragment();
                    /*Log event*/
                    Bundle chat_bundle = new Bundle();
                    chat_bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Main Chat");
                    chat_bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "View");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, chat_bundle);
                    break;

                case R.id.profile:
                    fragment = new ProfileFragment();
                    /*Log event*/
                    Bundle profile_bundle = new Bundle();
                    profile_bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "My Profile");
                    profile_bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "View");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, profile_bundle);
                    hideKeyboard(HomeActivity.this);
                    break;
            }

            if (fragment != null) {
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                        .commit();
            }
        });
    }

    private void setLastSeen() {
        if (mAuth.getUid() != null) {
            Date d = new Date();
            long timestamp = d.getTime();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("lastSeen", timestamp);
            userInfo.put("online", false);
            userInfo.put("lastSeenTimeStamp", FieldValue.serverTimestamp());

            db.collection("users").document(mAuth.getUid())
                    .update(userInfo)
                    .addOnSuccessListener(aVoid -> {
                        /*Log event*/
                        Bundle status_bundle = new Bundle();
                        status_bundle.putString(FirebaseAnalytics.Param.METHOD, "Last seen status");
                        status_bundle.putString("Updating_last_seen", "He/She went offline");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, status_bundle);
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    private void verifyAdmin() {
        SharedPreferences prefs = getSharedPreferences("iku_earth", Context.MODE_PRIVATE);
        DocumentReference docRef = db.collection("groups").document("iku_earth");
        if (mAuth.getUid() != null) {
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> admins = (ArrayList) document.get("admins");
                        boolean isAdmin = false;
                        if (admins != null) {
                            for (String element : admins) {
                                if (element.contains(mAuth.getUid())) {
                                    isAdmin = true;
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putBoolean("isAdmin", true);
                                    edit.apply();
                                    break;
                                }
                            }
                            if (!isAdmin) {
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putBoolean("isAdmin", false);
                                edit.apply();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        setLastSeen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        } else {
            if (findViewById(R.id.messageTextField) != null) {
                findViewById(R.id.messageTextField).clearFocus();
                homeBinding.animatedBottomBar.setVisibility(View.VISIBLE);
            }
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler()
                .postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}
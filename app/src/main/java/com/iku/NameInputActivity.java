package com.iku;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.iku.databinding.ActivityNameInputBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NameInputActivity extends AppCompatActivity {

    public static final String TAG = NameInputActivity.class.getSimpleName();
    private ActivityNameInputBinding binding;
    private String email;
    private FirebaseFirestore db;
    private FirebaseAuth fAuth;
    private FirebaseUser user;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNameInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        fAuth = FirebaseAuth.getInstance();

        user = fAuth.getCurrentUser();
        if (user != null)
            email = user.getEmail();

        db = FirebaseFirestore.getInstance();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!user.isEmailVerified()) {
                    handler.postDelayed(this, 1000);
                    user.reload();
                } else {
                    // do actions
                    binding.resendEmailButton.setVisibility(View.GONE);
                    binding.verificationMessage.setVisibility(View.GONE);
                    binding.enterFirstName.setVisibility(View.VISIBLE);
                    binding.enterLastName.setVisibility(View.VISIBLE);
                    binding.namesNextButton.setVisibility(View.VISIBLE);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    //log event
                    Bundle verify_bundle = new Bundle();
                    verify_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                    verify_bundle.putString("verification_email_status", "verified");
                    mFirebaseAnalytics.logEvent("user_verification", verify_bundle);
                    Toast.makeText(NameInputActivity.this, "Email verification successful!", Toast.LENGTH_SHORT).show();
                }
            }
        }, 1000);

        binding.resendEmailButton.setOnClickListener(view -> {
            user.sendEmailVerification().addOnSuccessListener(aVoid -> {
                Toast.makeText(NameInputActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                //log event
                Bundle password_bundle = new Bundle();
                password_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                password_bundle.putString("verification_email_status", "sent");
                mFirebaseAnalytics.logEvent("resend_verification_email", password_bundle);
            }).addOnFailureListener(e -> {
                //log event
                Bundle password_bundle = new Bundle();
                password_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                password_bundle.putString("verification_email_status", "failed");
                mFirebaseAnalytics.logEvent("resend_verification_email", password_bundle);
            });
            new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) {
                    binding.resendEmailButton.setEnabled(false);
                    binding.resendEmailButton.setText("Resend in " + new SimpleDateFormat("ss").format(new Date(millisUntilFinished)) + "s");
                    binding.resendEmailButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorTextSecondary));
                }

                public void onFinish() {
                    binding.resendEmailButton.setText("Resend Verification Email");
                    binding.resendEmailButton.setEnabled(true);
                    binding.resendEmailButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                }
            }.start();
        });

        binding.namesNextButton.setOnClickListener(view -> {
            user.reload();
            if (binding.enterFirstName.getText().toString().trim().isEmpty() || binding.enterLastName.getText().toString().trim().isEmpty())
                Toast.makeText(this, "Please fill your name.", Toast.LENGTH_SHORT).show();
            else {
                if (binding.enterFirstName.getText().toString().length() > 0 &&
                        binding.enterLastName.getText().toString().length() > 0) {
                    if (!user.isEmailVerified()) {
                        Toast.makeText(NameInputActivity.this, "Verify your email before proceeding.", Toast.LENGTH_SHORT).show();
                    } else {
                        String firstName = binding.enterFirstName.getText().toString().trim();
                        firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();

                        String lastName = binding.enterLastName.getText().toString().trim();
                        lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();

                        if (firstName.isEmpty() && lastName.isEmpty())
                            Toast.makeText(this, "Please fill your name.", Toast.LENGTH_SHORT).show();
                        else
                            newUserSignUp(firstName, lastName, email);
                    }
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(NameInputActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(NameInputActivity.this, "Sign in to continue.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void newUserSignUp(final String firstName, final String lastName, final String email) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();
                    // Create the arguments to the callable function.
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("firstName", firstName);
                    userInfo.put("lastName", lastName);
                    userInfo.put("email", email);
                    userInfo.put("uid", fAuth.getUid());
                    userInfo.put("points", 0);
                    userInfo.put("firstMessage", false);
                    userInfo.put("firstImage", false);
                    userInfo.put("signUpTime", FieldValue.serverTimestamp());
                    userInfo.put("role", "member");
                    userInfo.put("appVersion", BuildConfig.VERSION_NAME);

                    Map<String, Object> userRegistrationTokenInfo = new HashMap<>();
                    userRegistrationTokenInfo.put("registrationToken", token);
                    userRegistrationTokenInfo.put("uid", fAuth.getUid());


                    final String userID = fAuth.getUid();

                    if (userID != null) {

                        db.collection("users").document(fAuth.getUid())
                                .set(userInfo)
                                .addOnSuccessListener(aVoid -> {
                                    DocumentReference groupRef = db.collection("groups").document("iku_earth");
                                    groupRef.update("members", FieldValue.arrayUnion(userID));
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(firstName + " " + lastName).build();
                                    Map<String, Object> userDevInfo = new HashMap<>();
                                    userDevInfo.put("Device", Build.MANUFACTURER);
                                    userDevInfo.put("Model", Build.MODEL);
                                    userDevInfo.put("Android", Build.VERSION.SDK_INT);
                                    userDevInfo.put("Release", Build.VERSION.RELEASE);
                                    userDevInfo.put("Kernel", System.getProperty("os.version"));
                                    userDevInfo.put("Version Name", BuildConfig.VERSION_NAME);
                                    userDevInfo.put("Version Code", BuildConfig.VERSION_CODE);
                                    userDevInfo.put("infoTime", FieldValue.serverTimestamp());
                                    db.collection("usersVerifiedInfo").document(userID)
                                            .set(userDevInfo)
                                            .addOnSuccessListener(aVoid2 -> {
                                            })
                                            .addOnFailureListener(e -> {
                                            });

                                    user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> updateUI(user));

                                    /*Log event*/
                                    Bundle signup_bundle = new Bundle();
                                    signup_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, signup_bundle);
                                    Toast.makeText(NameInputActivity.this, "Welcome to the community", Toast.LENGTH_LONG).show();

                                })
                                .addOnFailureListener(e -> {
                                    /*Log event*/
                                    Bundle failed_bundle = new Bundle();
                                    failed_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                    failed_bundle.putString("failure_reason", "DB write failed");
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, failed_bundle);

                                });

                        db.collection("registrationTokens").document(userID)
                                .set(userRegistrationTokenInfo)
                                .addOnSuccessListener(aVoid -> {
                                })
                                .addOnFailureListener(e -> {
                                });
                    }
                });
    }
}
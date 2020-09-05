package com.iku;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iku.databinding.ActivityPasswordInputBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PasswordInputActivity extends AppCompatActivity {

    private ActivityPasswordInputBinding binding;

    private FirebaseAuth firebaseAuth;

    private FirebaseUser user;

    private FirebaseAnalytics mFirebaseAnalytics;

    private ProgressDialog mProgress;
    private static final String TAG = PasswordInputActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle extras = getIntent().getExtras();
        final String enteredEmail = extras.getString("email");

        firebaseAuth = FirebaseAuth.getInstance();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        initProgressDialog();

        binding.backButton.setOnClickListener(view -> onBackPressed());

        binding.signinButton.setOnClickListener(view -> {

            if (binding.enterPassword.getText().toString().isEmpty()) {
                Toast.makeText(PasswordInputActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            } else {
                mProgress.show();
                firebaseAuth.signInWithEmailAndPassword(enteredEmail, binding.enterPassword.getText().toString())
                        .addOnCompleteListener(PasswordInputActivity.this, task -> {
                            if (task.isSuccessful()) {
                                user = firebaseAuth.getCurrentUser();
                                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                                DocumentReference docIdRef = rootRef.collection("users").document(user.getUid());
                                docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {

                                                Toast.makeText(PasswordInputActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                                                //sending to Home Activity
                                                mProgress.dismiss();
                                                Intent goToHomeActivity = new Intent(PasswordInputActivity.this, HomeActivity.class);
                                                goToHomeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(goToHomeActivity);

                                                //log event
                                                Bundle password_bundle = new Bundle();
                                                password_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, password_bundle);
                                            } else {
                                                Toast.makeText(PasswordInputActivity.this, "User profile not filled!", Toast.LENGTH_LONG).show();
                                                //sending to NameInput Activity
                                                mProgress.dismiss();
                                                Intent goToNameInputActivity = new Intent(PasswordInputActivity.this, NameInputActivity.class);
                                                goToNameInputActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(goToNameInputActivity);
                                            }
                                        } else {
                                            Log.d(TAG, "Failed with: ", task.getException());
                                        }
                                    }
                                });


                            } else {
                                mProgress.dismiss();

                                //log event
                                Bundle fail_bundle = new Bundle();
                                fail_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                fail_bundle.putString("failure_reason", "incorrect password");
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, fail_bundle);
                                Toast.makeText(PasswordInputActivity.this, "Incorrect password", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        binding.forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.sendPasswordResetEmail(enteredEmail)

                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(PasswordInputActivity.this, "Password reset instructions sent via email", Toast.LENGTH_LONG).show();
                            //log event
                            Bundle passwordReset_bundle = new Bundle();
                            passwordReset_bundle.putString("state", "sent");
                            mFirebaseAnalytics.logEvent("password_reset_email", passwordReset_bundle);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PasswordInputActivity.this, "Email Not Sent" + e.getMessage(), Toast.LENGTH_LONG).show();
                            //log event
                            Bundle failed_bundle = new Bundle();
                            failed_bundle.putString("state", "failed");
                            mFirebaseAnalytics.logEvent("password_reset_email", failed_bundle);

                        });
                new CountDownTimer(60000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        binding.forgotPasswordTextView.setEnabled(false);
                        binding.forgotPasswordTextView.setText("Resend in " + new SimpleDateFormat("ss").format(new Date(millisUntilFinished)) + "s");
                    }

                    public void onFinish() {
                        binding.forgotPasswordTextView.setText("Forgot Password?");
                        binding.forgotPasswordTextView.setEnabled(true);

                    }
                }.start();
            }

        });

//        binding.forgotPasswordTextView.setOnClickListener(view ->
//                firebaseAuth.sendPasswordResetEmail(enteredEmail).addOnSuccessListener(
//                        aVoid ->
//                Toast.makeText(PasswordInputActivity.this, "Password reset instructions sent via email", Toast.LENGTH_LONG).show()).addOnFailureListener(e ->
//                Toast.makeText(PasswordInputActivity.this, "Email Not Sent" + e.getMessage(), Toast.LENGTH_LONG).show()));

    }

    private void initProgressDialog() {
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }
}
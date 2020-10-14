package com.iku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iku.databinding.ActivityNewPasswordInputBinding;

public class NewPasswordInputActivity extends AppCompatActivity {

    private final String TAG = NewPasswordInputActivity.class.getSimpleName();
    private ActivityNewPasswordInputBinding binding;
    private String email, password;
    private FirebaseAuth fAuth;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewPasswordInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        fAuth = FirebaseAuth.getInstance();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Intent myIntent = getIntent();
        email = myIntent.getStringExtra("email");

        binding.newPasswordNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password = binding.enterNewPassword.getText().toString().trim();
                binding.enterNewPassword.setEnabled(false);

                //validations
                if (password.isEmpty()) {
                    binding.enterNewPassword.setEnabled(true);
                    binding.enterNewPassword.setError("password cannot be empty");
                    return;
                }
                if (password.length() < 8) {
                    binding.enterNewPassword.setEnabled(true);
                    binding.enterNewPassword.setError("password must be at least 8 characters");
                    return;
                }

                //Firebase registrations
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    //log event
                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                    bundle.putString("auth_status", "created");
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);

                                    FirebaseUser user = fAuth.getCurrentUser();

                                    if (user != null) {
                                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(NewPasswordInputActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();

                                                //log event
                                                Bundle password_bundle = new Bundle();
                                                password_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                                password_bundle.putString("verification_email_status", "sent");
                                                mFirebaseAnalytics.logEvent("user_verified", password_bundle);


                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                binding.enterNewPassword.setEnabled(true);

                                                //log event
                                                Bundle password_bundle = new Bundle();
                                                password_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                                password_bundle.putString("verification_email_status", "failed");
                                                mFirebaseAnalytics.logEvent("user_verified", password_bundle);
                                            }
                                        });

                                        Toast.makeText(NewPasswordInputActivity.this, "User Registered", Toast.LENGTH_SHORT).show();
                                        //sending to Profile Activity
                                        Intent goToNameActivity = new Intent(NewPasswordInputActivity.this, NameInputActivity.class);
                                        goToNameActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(goToNameActivity);

                                    }
                                } else {
                                    Toast.makeText(NewPasswordInputActivity.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.enterNewPassword.setEnabled(true);
    }
}
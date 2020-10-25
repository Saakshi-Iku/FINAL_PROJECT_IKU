package com.iku;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.iku.app.AppConfig;
import com.iku.databinding.ActivityWelcomeBinding;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {

    public static final String TAG = WelcomeActivity.class.getSimpleName();
    private final int RC_SIGN_IN = 121;
    private CallbackManager mCallbackManager;
    private ActivityWelcomeBinding binding;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private FirebaseFirestore db;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        initItems();
        initProgressDialog();
        initButtons();

    }

    private void initItems() {
        db = FirebaseFirestore.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
    }

    private void initButtons() {

        binding.googleSigninButton.setOnClickListener(view -> signIn());

        binding.emailSigninButton.setOnClickListener(view -> {
            Intent enterEmailIntent = new Intent(WelcomeActivity.this, EmailInputActivity.class);
            startActivity(enterEmailIntent);
        });

        binding.facebookLoginButton.setOnClickListener(view -> {
            mProgress.show();
            LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this,
                    Arrays.asList("email", "public_profile"));
            LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "facebook:onSuccess:" + loginResult);
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "facebook:onCancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(TAG, "facebook:onError", error);
                }
            });
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String displayName = user.getDisplayName();
                            if (displayName != null) {
                                String firstName = displayName.substring(0, displayName.indexOf(' ')).trim();
                                String lastName = displayName.substring(displayName.indexOf(' ')).trim();
                                String email = user.getEmail();
                                newUserSignUp(firstName, lastName, email);

                                /*Log event*/
                                Bundle signup_bundle = new Bundle();
                                signup_bundle.putString(FirebaseAnalytics.Param.METHOD, "Facebook");
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, signup_bundle);
                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            signInResult(task);
        }
    }

    private void signInResult(Task<GoogleSignInAccount> signInAccountTask) {
        try {
            GoogleSignInAccount acc = signInAccountTask.getResult(ApiException.class);
            FirebaseGoogleAuth(acc);
        } catch (ApiException e) {
            Log.e(TAG, "signInResult: Sign In Fail" + e);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount inAccount) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(inAccount.getIdToken(), null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(WelcomeActivity.this);
                String firstName = null, lastName = null, email = null;
                if (acct != null) {
                    firstName = acct.getGivenName();
                    lastName = acct.getFamilyName();
                    email = acct.getEmail();
                    mProgress.show();
                    newUserSignUp(firstName, lastName, email);

                    /*Log event*/
                    Bundle signup_bundle = new Bundle();
                    signup_bundle.putString(FirebaseAnalytics.Param.METHOD, "Google");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, signup_bundle);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mProgress.dismiss();
            Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(WelcomeActivity.this, "Sign in to continue.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void newUserSignUp(final String firstName, final String lastName, final String email) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }
                    // Get new Instance ID token
                    String token = task.getResult().getToken();

                    // Create the arguments to the callable function.
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("firstName", firstName);
                    userInfo.put("lastName", lastName);
                    userInfo.put("email", email);
                    userInfo.put("uid", mAuth.getUid());
                    userInfo.put("points", 0);
                    userInfo.put("firstMessage", false);
                    userInfo.put("firstImage", false);
                    userInfo.put("signUpTime", FieldValue.serverTimestamp());
                    userInfo.put("signUpTimestamp", new Date().getTime());
                    userInfo.put("role", "member");
                    userInfo.put("appVersion", BuildConfig.VERSION_NAME);

                    final String userID = mAuth.getUid();

                    if (userID != null) {
                        sendRegistrationToken(token, userID);

                        DocumentReference docRef = db.collection(AppConfig.USERS_COLLECTION).document(mAuth.getUid());
                        docRef.get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot document = task1.getResult();
                                if (document.exists()) {

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);

                                    //log event
                                    Bundle user_bundle = new Bundle();
                                    user_bundle.putString(FirebaseAnalytics.Param.METHOD, "Google/FB Sign In");
                                    user_bundle.putString("user_status", "Previous user authenticated via social media");
                                    mFirebaseAnalytics.logEvent("who_is_this_user", user_bundle);
                                } else {
                                    db.collection(AppConfig.USERS_COLLECTION).document(mAuth.getUid())
                                            .set(userInfo)
                                            .addOnSuccessListener(aVoid -> {
                                                Map<String, Object> userDevInfo = new HashMap<>();
                                                userDevInfo.put("Device", Build.MANUFACTURER);
                                                userDevInfo.put("Model", Build.MODEL);
                                                userDevInfo.put("Android", Build.VERSION.SDK_INT);
                                                userDevInfo.put("Release", Build.VERSION.RELEASE);
                                                userDevInfo.put("Kernel", System.getProperty("os.version"));
                                                userDevInfo.put("Version Name", BuildConfig.VERSION_NAME);
                                                userDevInfo.put("Version Code", BuildConfig.VERSION_CODE);
                                                userDevInfo.put("infoTime", FieldValue.serverTimestamp());
                                                db.collection(AppConfig.USER_VERIFIED_COLLECTION).document(userID)
                                                        .set(userDevInfo)
                                                        .addOnSuccessListener(aVoid2 -> {
                                                        })
                                                        .addOnFailureListener(e -> {
                                                        });
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                DocumentReference groupRef = db.collection("groups").document("aD4dmKaHhQmEJ0yqeyLQ");
                                                groupRef.update("members", FieldValue.arrayUnion(userID));
                                                updateUI(user);
                                                Toast.makeText(WelcomeActivity.this, "Welcome to the community", Toast.LENGTH_LONG).show();
                                            })
                                            .addOnFailureListener(e -> {
                                            });
                                }
                            }
                        });
                    }
                });
    }

    private void initProgressDialog() {
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Logging in..");
        mProgress.setMessage("");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }

    private void sendRegistrationToken(String token, String uid) {
        Map<String, Object> userRegistrationTokenInfo = new HashMap<>();
        userRegistrationTokenInfo.put("registrationToken", token);
        userRegistrationTokenInfo.put("uid", uid);
        db.collection(AppConfig.REGISTRATION_TOKENS_COLLECTION).document(uid)
                .set(userRegistrationTokenInfo)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });
    }
}
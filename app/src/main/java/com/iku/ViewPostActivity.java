package com.iku;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.iku.adapter.CommentAdapter;
import com.iku.databinding.ActivityViewPostBinding;
import com.iku.models.CommentModel;
import com.iku.models.UserModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;

public class ViewPostActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener {

    private static final String TAG = ViewPostActivity.class.getSimpleName();

    private SimpleDateFormat sfdMainDate = new SimpleDateFormat("hh:mm a, MMMM dd, yyyy");

    private Bundle extras;

    private ActivityViewPostBinding viewPostBinding;

    private FirebaseUser user;

    private CommentAdapter adapter;

    private String messageId;

    private FirebaseFirestore db;

    private FirebaseAnalytics mFirebaseAnalytics;

    private GestureDetectorCompat detector;

    private GestureDetector swipeDetector;

    private int scrollStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPostBinding = ActivityViewPostBinding.inflate(getLayoutInflater());
        setContentView(viewPostBinding.getRoot());

        initItems();
        setDetails();
        initButtons();
        initialEmoticons(messageId);
        reactions(messageId);
        initCommentsView();
    }

    private void initCommentsView() {
        Query query = db.collection("iku_earth_messages").document(messageId).collection("comments").orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<CommentModel> options = new FirestoreRecyclerOptions.Builder<CommentModel>()
                .setQuery(query, CommentModel.class)
                .build();
        adapter = new CommentAdapter(options);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ((SimpleItemAnimator) viewPostBinding.commentsView.getItemAnimator()).setSupportsChangeAnimations(false);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                viewPostBinding.scrollView.smoothScrollTo(0, viewPostBinding.heartsArea.getBottom());
            }
        });

        adapter.setOnItemClickListener(new CommentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, DocumentSnapshot snapshot) {
                CommentModel commentModel = snapshot.toObject(CommentModel.class);
                if (commentModel != null) {
                    String name = commentModel.getCommenterName();
                    String uid = commentModel.getUid();
                    Intent userProfileIntent = new Intent(ViewPostActivity.this, UserProfileActivity.class);
                    userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
                    userProfileIntent.putExtra("EXTRA_PERSON_UID", uid);
                    startActivity(userProfileIntent);
                }
            }

            @Override
            public void onHeartClick(int pos, DocumentSnapshot snapshot) {
                CommentModel commentModel = snapshot.toObject(CommentModel.class);
                final String documentID = snapshot.getId();
                if (commentModel != null) {
                    boolean isLiked = false;
                    int heartsCount = commentModel.getHeartsCount();
                    ArrayList<String> heartsList = commentModel.getHeartsArray();
                    String myUID = user.getUid();
                    if (heartsCount >= 0) {
                        for (String element : heartsList) {
                            if (element.contains(myUID)) {
                                isLiked = true;
                                break;
                            }
                        }
                        if (!isLiked) {
                            db.collection("iku_earth_messages").document(messageId).collection("comments").document(documentID)
                                    .update("heartsCount", commentModel.getHeartsCount() + 1,
                                            "heartsArray", FieldValue.arrayUnion(myUID))
                                    .addOnSuccessListener(aVoid -> {
                                        //Log event
                                        Bundle heart_params = new Bundle();
                                        heart_params.putString("type", "heart_up");
                                        heart_params.putString("commentID", documentID);
                                        heart_params.putString("author_UID", commentModel.getUid());
                                        heart_params.putString("action_by", user.getUid());
                                        mFirebaseAnalytics.logEvent("comment_hearts", heart_params);
                                    })
                                    .addOnFailureListener(e -> {
                                    });
                        } else {
                            db.collection("iku_earth_messages").document(messageId).collection("comments").document(documentID)
                                    .update("heartsCount", commentModel.getHeartsCount() - 1,
                                            "heartsArray", FieldValue.arrayRemove(myUID))
                                    .addOnSuccessListener(aVoid -> {
                                        //Log event
                                        Bundle params = new Bundle();
                                        params.putString("type", "heart_down");
                                        params.putString("messageID", documentID);
                                        params.putString("author_UID", commentModel.getUid());
                                        params.putString("action_by", user.getUid());
                                        mFirebaseAnalytics.logEvent("comment_hearts", params);
                                    })
                                    .addOnFailureListener(e -> {
                                    });
                        }
                    }
                    adapter.notifyItemChanged(pos);
                }
            }
        });
        viewPostBinding.commentsView.setHasFixedSize(true);
        viewPostBinding.commentsView.setLayoutManager(linearLayoutManager);
        viewPostBinding.commentsView.setAdapter(adapter);
    }

    private void initItems() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        detector = new GestureDetectorCompat(this, new RecyclerViewOnGestureListener());
        swipeDetector = new GestureDetector(this, new SwipeListener());
        viewPostBinding.commentsView.addOnItemTouchListener(this);

        extras = this.getIntent().getExtras();
        if (extras != null)
            messageId = extras.getString("EXTRA_MESSAGE_ID");

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        String message = extras.getString("EXTRA_MESSAGE");
        viewPostBinding.postDescription.setText(message);
        viewPostBinding.postDescriptionPreview.setText(message);

        String imageUrl = extras.getString("EXTRA_IMAGE_URL");
        if (imageUrl != null) {
            Picasso.get()
                    .load(imageUrl)
                    .noFade()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.progress_animation)
                    .resize(5000, 5000)
                    .onlyScaleDown()
                    .into(viewPostBinding.viewedImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(imageUrl)
                                    .noFade()
                                    .resize(5000, 5000)
                                    .onlyScaleDown()
                                    .placeholder(R.drawable.progress_animation)
                                    .into(viewPostBinding.viewedImage);
                        }
                    });
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPostBinding.scrollView.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.appBar);
            viewPostBinding.scrollView.setLayoutParams(params);
            viewPostBinding.imageContainer.setVisibility(View.GONE);
            viewPostBinding.messageArea.setVisibility(View.VISIBLE);
            viewPostBinding.messageTextField.requestFocus();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewPostBinding.postDescription.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initButtons() {
        viewPostBinding.backButton.setOnClickListener(view -> onBackPressed());
        viewPostBinding.sendMessageButton.setOnClickListener(view -> {
            final String message = viewPostBinding.messageTextField.getText().toString().trim();
            if (!message.isEmpty()) {
                sendComment(message);
                viewPostBinding.messageTextField.setText("");
                viewPostBinding.messageTextField.clearFocus();
            }
        });

        viewPostBinding.viewHandle.setOnTouchListener((view, e) -> {
            swipeDetector.onTouchEvent(e);
            return false;
        });

        viewPostBinding.viewHandle.setOnClickListener(view -> {

        });
        viewPostBinding.userName.setOnClickListener(view -> {
            String name = extras.getString("EXTRA_PERSON_NAME");
            String uid = extras.getString("EXTRA_USER_ID");
            if (uid != null) {
                if (!uid.equals(user.getUid())) {
                    Intent userProfileIntent = new Intent(ViewPostActivity.this, UserProfileActivity.class);
                    userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
                    userProfileIntent.putExtra("EXTRA_PERSON_UID", uid);
                    startActivity(userProfileIntent);
                }
            }
        });
    }

    private void sendComment(String comment) {
        Date d = new Date();
        long timestamp = d.getTime();
        String authorUid = extras.getString("EXTRA_USER_ID");
        Map<String, Object> data = new HashMap<>();
        data.put("postAuthorUid", authorUid);
        data.put("comment", comment);
        data.put("uid", user.getUid());
        data.put("commenterName", user.getDisplayName());
        data.put("heartsCount", 0);
        ArrayList<Object> heartsUidArray = new ArrayList<>();
        data.put("heartsArray", heartsUidArray);
        data.put("timestamp", timestamp);
        data.put("commenterImageUrl", String.valueOf(user.getPhotoUrl()));
        data.put("readableTimestamp", FieldValue.serverTimestamp());
        ArrayList<Object> spamArray = new ArrayList<>();
        data.put("spamReportedBy", spamArray);
        data.put("spamCount", 0);
        data.put("spam", false);
        data.put("deleted", false);

        db.collection("iku_earth_messages").document(messageId)
                .collection("comments").add(data)
                .addOnSuccessListener(documentReference -> {
                    db.collection("iku_earth_messages").document(messageId)
                            .update("postCommentCount", FieldValue.increment(1))
                            .addOnSuccessListener(documentReferenceObj -> {
                            });
                })
                .addOnFailureListener(e -> {
                });
    }

    private void setDetails() {

        String userName = extras.getString("EXTRA_PERSON_NAME");
        long timestamp = extras.getLong("EXTRA_POST_TIMESTAMP");

        viewPostBinding.userName.setText(userName);
        viewPostBinding.postTime.setText(sfdMainDate.format(timestamp));
    }

    private void initialEmoticons(String messageId) {
        DocumentReference docRef = db.collection("iku_earth_messages").document(messageId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    long upvotesCount, downvotesCount;
                    ArrayList<String> HeartUpArray = (ArrayList) document.get("upvoters");
                    ArrayList<String> emoji1Array = (ArrayList) document.get("emoji1");
                    ArrayList<String> emoji2Array = (ArrayList) document.get("emoji2");
                    ArrayList<String> emoji3Array = (ArrayList) document.get("emoji3");
                    ArrayList<String> emoji4Array = (ArrayList) document.get("emoji4");
                    ArrayList<String> HeartDownArray = (ArrayList) document.get("downvoters");
                    upvotesCount = (long) document.get("upvoteCount");
                    downvotesCount = (long) document.get("downvoteCount");

                    if (upvotesCount >= 0) {
                        for (String element : HeartUpArray) {
                            if (element.contains(user.getUid())) {
                                viewPostBinding.heartUp.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            }
                        }
                        for (String element : emoji1Array) {
                            if (element.contains(user.getUid())) {
                                viewPostBinding.emoji1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            }
                        }
                        for (String element : emoji2Array) {
                            if (element.contains(user.getUid())) {
                                viewPostBinding.emoji2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            }
                        }
                        for (String element : emoji3Array) {
                            if (element.contains(user.getUid())) {
                                viewPostBinding.emoji3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            }
                        }
                        for (String element : emoji4Array) {
                            if (element.contains(user.getUid())) {
                                viewPostBinding.emoji4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            }
                        }
                    }
                    if (downvotesCount >= 0) {
                        for (String element : HeartDownArray) {
                            if (element.contains(user.getUid())) {
                                viewPostBinding.heartDown.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            }
                        }
                    }

                }
            }
        });
    }

    private void reactions(String messageId) {

        viewPostBinding.choose.setOnClickListener(v -> {
            userVote(messageId, "upvoters");
            disableEmoticonButtons(false);
        });
        viewPostBinding.choose1.setOnClickListener(v -> {
            userVote(messageId, "emoji1");
            disableEmoticonButtons(false);
        });
        viewPostBinding.choose2.setOnClickListener(v -> {
            userVote(messageId, "emoji2");
            disableEmoticonButtons(false);
        });
        viewPostBinding.choose3.setOnClickListener(v -> {
            userVote(messageId, "emoji3");
            disableEmoticonButtons(false);
        });
        viewPostBinding.choose4.setOnClickListener(v -> {
            userVote(messageId, "emoji4");
            disableEmoticonButtons(false);
        });
        viewPostBinding.choose6.setOnClickListener(v -> {
            userVote(messageId, "downvoters");
            disableEmoticonButtons(false);
        });
    }

    public void userVote(String messageDocumentID, String emoji) {
        DocumentReference docRef = db.collection("iku_earth_messages").document(messageDocumentID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String authorOfMessage;
                    long upvotesCount, downvotesCount;
                    authorOfMessage = (String) document.get("uid");
                    ArrayList<String> HeartUpArray = (ArrayList) document.get("upvoters");
                    ArrayList<String> emoji1Array = (ArrayList) document.get("emoji1");
                    ArrayList<String> emoji2Array = (ArrayList) document.get("emoji2");
                    ArrayList<String> emoji3Array = (ArrayList) document.get("emoji3");
                    ArrayList<String> emoji4Array = (ArrayList) document.get("emoji4");
                    ArrayList<String> HeartDownArray = (ArrayList) document.get("downvoters");
                    upvotesCount = (long) document.get("upvoteCount");
                    downvotesCount = (long) document.get("downvoteCount");
                    boolean HeartupLiked = false;
                    boolean emoji1Liked = false;
                    boolean emoji2Liked = false;
                    boolean emoji3Liked = false;
                    boolean emoji4Liked = false;
                    boolean disliked = false;

                    if (upvotesCount >= 0) {
                        for (String element : HeartUpArray) {
                            if (element.contains(user.getUid())) {
                                HeartupLiked = true;
                                break;
                            }
                        }
                        for (String element : emoji1Array) {
                            if (element.contains(user.getUid())) {
                                emoji1Liked = true;
                                break;
                            }
                        }
                        for (String element : emoji2Array) {
                            if (element.contains(user.getUid())) {
                                emoji2Liked = true;
                                break;
                            }
                        }
                        for (String element : emoji3Array) {
                            if (element.contains(user.getUid())) {
                                emoji3Liked = true;
                                break;
                            }
                        }
                        for (String element : emoji4Array) {
                            if (element.contains(user.getUid())) {
                                emoji4Liked = true;
                                break;
                            }
                        }
                    }
                    if (downvotesCount >= 0) {
                        for (String element : HeartDownArray) {
                            if (element.contains(user.getUid())) {
                                disliked = true;
                                break;
                            }
                        }
                    }
                    if (!HeartupLiked && !emoji1Liked && !emoji2Liked && !emoji3Liked && !emoji4Liked && !disliked) {
                        newLikeOrDislike(messageDocumentID, emoji, upvotesCount, downvotesCount, authorOfMessage);
                    } else {
                        if (HeartupLiked) {
                            changeLikesArray(messageDocumentID, emoji, "upvoters", upvotesCount, downvotesCount, authorOfMessage);
                        } else if (emoji1Liked) {
                            changeLikesArray(messageDocumentID, emoji, "emoji1", upvotesCount, downvotesCount, authorOfMessage);
                        } else if (emoji2Liked) {
                            changeLikesArray(messageDocumentID, emoji, "emoji2", upvotesCount, downvotesCount, authorOfMessage);
                        } else if (emoji3Liked) {
                            changeLikesArray(messageDocumentID, emoji, "emoji3", upvotesCount, downvotesCount, authorOfMessage);
                        } else if (emoji4Liked) {
                            changeLikesArray(messageDocumentID, emoji, "emoji4", upvotesCount, downvotesCount, authorOfMessage);
                        } else if (disliked) {
                            changeLikesArray(messageDocumentID, emoji, "downvoters", upvotesCount, downvotesCount, authorOfMessage);
                        }
                    }
                }
            }
        });
    }

    private void changeLikesArray(String messageDocumentID, String currentEmoji, String previousEmoji, long upvotesCount, long downvotesCount, String authorOfMessage) {
        if (currentEmoji.equals(previousEmoji)) {
            if (currentEmoji.equals("upvoters") || currentEmoji.equals("emoji1") || currentEmoji.equals("emoji2") || currentEmoji.equals("emoji3") || currentEmoji.equals("emoji4")) {
                if (!authorOfMessage.equals(user.getUid())) {
                    db.collection("users").document(authorOfMessage)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                db.collection("users").document(authorOfMessage)
                                        .update("points", FieldValue.increment(-1))
                                        .addOnSuccessListener(aVoid -> {
                                        });
                            });
                }
                db.collection("iku_earth_messages").document(messageDocumentID)
                        .update("upvoteCount", upvotesCount - 1,
                                currentEmoji, FieldValue.arrayRemove(user.getUid()))
                        .addOnSuccessListener(aVoid -> {
                            switch (currentEmoji) {
                                case "upvoters":
                                    viewPostBinding.heartUp.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                                    break;
                                case "emoji1":
                                    viewPostBinding.emoji1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                                    break;
                                case "emoji2":
                                    viewPostBinding.emoji2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                                    break;
                                case "emoji3":
                                    viewPostBinding.emoji3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                                    break;
                                case "emoji4":
                                    viewPostBinding.emoji4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                                    break;
                            }
                            disableEmoticonButtons(true);
                        }).addOnFailureListener(e -> disableEmoticonButtons(true));
            } else if (currentEmoji.equals("downvoters")) {
                if (!authorOfMessage.equals(user.getUid())) {
                    db.collection("users").document(authorOfMessage).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                final UserModel usersData = documentSnapshot.toObject(UserModel.class);
                                db.collection("users").document(authorOfMessage)
                                        .update("points", FieldValue.increment(1))
                                        .addOnSuccessListener(aVoid -> {
                                        });
                            });
                }
                db.collection("iku_earth_messages").document(messageDocumentID)
                        .update("downvoteCount", downvotesCount - 1,
                                currentEmoji, FieldValue.arrayRemove(user.getUid()))
                        .addOnSuccessListener(aVoid -> {
                            viewPostBinding.heartDown.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                            disableEmoticonButtons(true);
                        }).addOnFailureListener(e -> disableEmoticonButtons(true));
            }

        } else if ((!currentEmoji.equals(previousEmoji)) && (currentEmoji.equals("downvoters"))) {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            db.collection("users").document(authorOfMessage)
                                    .update("points", FieldValue.increment(-2))
                                    .addOnSuccessListener(aVoid -> {
                                    });
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", upvotesCount - 1,
                            "downvoteCount", downvotesCount + 1)
                    .addOnSuccessListener(aVoid -> {
                        changeEmoticonBackground(currentEmoji, previousEmoji);
                        disableEmoticonButtons(true);
                    }).addOnFailureListener(e -> disableEmoticonButtons(true));

        } else if ((previousEmoji.equals("downvoters")) && (!currentEmoji.equals(previousEmoji))) {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            db.collection("users").document(authorOfMessage)
                                    .update("points", FieldValue.increment(2))
                                    .addOnSuccessListener(aVoid -> {
                                    });
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", upvotesCount + 1,
                            "downvoteCount", downvotesCount - 1
                    )
                    .addOnSuccessListener(aVoid -> {
                        changeEmoticonBackground(currentEmoji, previousEmoji);
                        disableEmoticonButtons(true);
                    }).addOnFailureListener(e -> disableEmoticonButtons(true));
        } else {
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()))
                    .addOnSuccessListener(aVoid -> {
                        changeEmoticonBackground(currentEmoji, previousEmoji);
                        disableEmoticonButtons(true);
                    }).addOnFailureListener(e -> disableEmoticonButtons(true));
        }
    }

    private void newLikeOrDislike(String messageDocumentID, String emoji, long UpvotesCount, long DownvotesCount, String authorOfMessage) {
        if (emoji.equals("downvoters")) {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            db.collection("users").document(authorOfMessage)
                                    .update("points", FieldValue.increment(-1))
                                    .addOnSuccessListener(aVoid -> {

                                    });
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(emoji, FieldValue.arrayUnion(user.getUid()),
                            "downvoteCount", DownvotesCount + 1)
                    .addOnSuccessListener(aVoid -> {
                        viewPostBinding.heartDown.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                        disableEmoticonButtons(true);
                    }).addOnFailureListener(e -> disableEmoticonButtons(true));
        } else {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            db.collection("users").document(authorOfMessage)
                                    .update("points", FieldValue.increment(1))
                                    .addOnSuccessListener(aVoid -> {
                                    });
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(emoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", UpvotesCount + 1)
                    .addOnSuccessListener(aVoid -> {
                        switch (emoji) {
                            case "upvoters":
                                viewPostBinding.heartUp.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            case "emoji1":
                                viewPostBinding.emoji1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            case "emoji2":
                                viewPostBinding.emoji2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            case "emoji3":
                                viewPostBinding.emoji3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            case "emoji4":
                                viewPostBinding.emoji4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                                break;
                            default:
                                // code block
                        }
                        disableEmoticonButtons(true);
                    }).addOnFailureListener(e -> disableEmoticonButtons(true));
        }
    }

    private void changeEmoticonBackground(String currentEmoji, String previousEmoji) {
        switch (currentEmoji) {
            case "upvoters":
                viewPostBinding.heartUp.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji1":
                viewPostBinding.emoji1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji2":
                viewPostBinding.emoji2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji3":
                viewPostBinding.emoji3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji4":
                viewPostBinding.emoji4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "downvoters":
                viewPostBinding.heartDown.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
        }
        switch (previousEmoji) {
            case "upvoters":
                viewPostBinding.heartUp.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji1":
                viewPostBinding.emoji1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji2":
                viewPostBinding.emoji2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji3":
                viewPostBinding.emoji3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji4":
                viewPostBinding.emoji4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "downvoters":
                viewPostBinding.heartDown.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                break;
        }
    }

    private void disableEmoticonButtons(boolean status) {
        viewPostBinding.choose.setEnabled(status);
        viewPostBinding.choose1.setEnabled(status);
        viewPostBinding.choose2.setEnabled(status);
        viewPostBinding.choose3.setEnabled(status);
        viewPostBinding.choose4.setEnabled(status);
        viewPostBinding.choose6.setEnabled(status);
    }

    public static void slideView(View view, int currentHeight, int newHeight) {
        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(500);
        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */
        slideAnimator.addUpdateListener(animation1 -> {
            view.getLayoutParams().height = (Integer) animation1.getAnimatedValue();
            view.requestLayout();
        });
        /*  We use an animationSet to play the animation  */
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        detector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.i(TAG, "onDoubleTapEvent: " + e.getAction());
            return super.onDoubleTapEvent(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            View view = viewPostBinding.commentsView.findChildViewUnder(e.getX(), e.getY());
            Log.i(TAG, "onLongPress: " + e.getAction() + "\nView" + view);
            if (view != null) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ViewPostActivity.this);
                View parentView = getLayoutInflater().inflate(R.layout.user_bottom_sheet, null);
                RelativeLayout profileView = parentView.findViewById(R.id.profile_layout);
                RelativeLayout deleteMessageView = parentView.findViewById(R.id.delete_layout);
                RelativeLayout addCommentView = parentView.findViewById(R.id.comment_layout);
                RelativeLayout reportView = parentView.findViewById(R.id.report_layout);
                ConstraintLayout heartsArea = parentView.findViewById(R.id.heartsArea);
                MaterialTextView deleteHeader = parentView.findViewById(R.id.delete_header);
                deleteHeader.setText(R.string.delete_comment);

                heartsArea.setVisibility(View.GONE);
                addCommentView.setVisibility(View.GONE);

                int position = viewPostBinding.commentsView.getChildAdapterPosition(view);
                DocumentSnapshot snapshot = adapter.getSnapshots().getSnapshot(position);

                String documentID = snapshot.getId();
                String UID = adapter.getItem(position).getUid();

                if (UID.equals(user.getUid())) {
                    profileView.setVisibility(View.GONE);
                    reportView.setVisibility(View.GONE);
                    deleteMessageView.setVisibility(View.VISIBLE);
                    deleteMessageView.setOnClickListener(view1 -> {
                        bottomSheetDialog.dismiss();
                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(view1.getContext());
                        materialAlertDialogBuilder.setTitle("Delete Comment");
                        materialAlertDialogBuilder.setMessage("Are you sure?");
                        materialAlertDialogBuilder.setPositiveButton("Delete", (dialogInterface, i) -> {
                            deleteComment(documentID, "author");
                            //log event
                            Bundle delete_bundle = new Bundle();
                            delete_bundle.putString("uid", user.getUid());
                            mFirebaseAnalytics.logEvent("message_deleted", delete_bundle);
                        }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                            bottomSheetDialog.setContentView(parentView);
                            bottomSheetDialog.show();
                        }).show();
                    });
                    bottomSheetDialog.setContentView(parentView);
                    bottomSheetDialog.show();
                } else {
                    profileView.setVisibility(View.VISIBLE);
                    SharedPreferences pref = view.getContext().getSharedPreferences("iku_earth", Context.MODE_PRIVATE);
                    boolean isAdmin = pref.getBoolean("isAdmin", false);
                    if (isAdmin) {
                        deleteMessageView.setVisibility(View.VISIBLE);
                    }
                    bottomSheetDialog.setContentView(parentView);
                    bottomSheetDialog.show();

                    deleteMessageView.setOnClickListener(view1 -> {
                        bottomSheetDialog.dismiss();
                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(view1.getContext());
                        materialAlertDialogBuilder.setTitle("Delete Comment");
                        materialAlertDialogBuilder.setMessage("Are you sure?");
                        materialAlertDialogBuilder.setPositiveButton("Delete", (dialogInterface, i) -> {
                            deleteComment(documentID, "admin");
                            //log event
                            Bundle delete_bundle = new Bundle();
                            delete_bundle.putString("uid", user.getUid());
                            mFirebaseAnalytics.logEvent("message_deleted", delete_bundle);
                        }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                            bottomSheetDialog.setContentView(parentView);
                            bottomSheetDialog.show();
                        }).show();
                    });

                    boolean isReported = false;
                    ArrayList<String> SpamReportedByArray = adapter.getItem(position).getSpamReportedBy();
                    if (SpamReportedByArray != null) {
                        for (String element : SpamReportedByArray) {
                            if (element.contains(user.getUid())) {
                                isReported = true;
                                break;
                            }
                        }
                        if (!isReported) {
                            reportView.setVisibility(View.VISIBLE);
                            reportView.setOnClickListener(view13 -> {
                                bottomSheetDialog.dismiss();
                                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(view13.getContext());
                                materialAlertDialogBuilder.setTitle("Report Spam");
                                materialAlertDialogBuilder.setMessage("Are you sure?");
                                materialAlertDialogBuilder.setPositiveButton("Report", (dialogInterface, i) -> {
                                    DocumentReference docRef = db.collection("iku_earth_messages").document(messageId).collection("comments").document(documentID);
                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    ArrayList<String> spamReportedArray = (ArrayList) document.get("spamReportedBy");
                                                    long spamCount = (long) document.get("spamCount");
                                                    boolean spam = (boolean) document.get("spam");
                                                    if (!spamReportedArray.contains(user.getUid())) {
                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("spamReportedBy", FieldValue.arrayUnion(user.getUid()));
                                                        map.put("spamCount", spamCount + 1);
                                                        if (spamCount >= 4) {
                                                            map.put("spam", true);
                                                            map.put("deleted", true);
                                                            map.put("deletedBy", "users");
                                                            deleteComment(documentID, "users");
                                                        }
                                                        db.collection("iku_earth_messages").document(messageId).collection("comments").document(documentID)
                                                                .update(map)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    //log event
                                    Bundle spam_bundle = new Bundle();
                                    spam_bundle.putString("uid", user.getUid());
                                    mFirebaseAnalytics.logEvent("message_reported_spam", spam_bundle);
                                }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                                    bottomSheetDialog.setContentView(parentView);
                                    bottomSheetDialog.show();
                                }).show();
                            });
                        }
                    }
                    profileView.setOnClickListener(view2 -> {
                        Intent userProfileIntent = new Intent(ViewPostActivity.this, UserProfileActivity.class);

                        String name = adapter.getItem(position).getCommenterName();
                        String userUID = adapter.getItem(position).getUid();
                        if (name != null) {
                            userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
                            userProfileIntent.putExtra("EXTRA_PERSON_UID", userUID);
                            startActivity(userProfileIntent);
                        } else
                            return;
                        bottomSheetDialog.dismiss();
                    });
                }
            }
            super.onLongPress(e);
        }
    }

    private void deleteComment(String commentDocumentID, String deletedBy) {
        Map<String, Object> map = new HashMap<>();
        map.put("deleted", true);
        map.put("deletedBy", deletedBy);
        if (deletedBy.equals("admin"))
            map.put("spam", true);
        db.collection("iku_earth_messages").document(messageId).collection("comments").document(commentDocumentID)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        db.collection("iku_earth_messages").document(messageId)
                                .update("postCommentCount", FieldValue.increment(-1))
                                .addOnSuccessListener(documentReferenceObj -> {
                                });
                        //Log event
                        Bundle delete_bundle = new Bundle();
                        delete_bundle.putString("UID", user.getUid());
                        delete_bundle.putString("Name", user.getDisplayName());
                        mFirebaseAnalytics.logEvent("deleted_comment", delete_bundle);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private class SwipeListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            animateHeight();
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            animateHeight();
            return super.onSingleTapConfirmed(e);
        }
    }

    private void animateHeight() {
        if (scrollStatus == 0) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPostBinding.imageContainer.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.appBar);
            viewPostBinding.imageContainer.setLayoutParams(params);
            slideView(viewPostBinding.imageContainer, viewPostBinding.imageContainer.getLayoutParams().height, 400);
            viewPostBinding.seeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_next_36));
            viewPostBinding.seeIcon.setRotation(90);
            viewPostBinding.seeType.setText(R.string.see_less);
            viewPostBinding.messageArea.setVisibility(View.VISIBLE);
            viewPostBinding.postDescriptionPreview.setVisibility(View.GONE);
            scrollStatus = 1;
        } else if (scrollStatus == 1) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPostBinding.imageContainer.getLayoutParams();
            params.removeRule(RelativeLayout.BELOW);
            viewPostBinding.imageContainer.setLayoutParams(params);
            slideView(viewPostBinding.imageContainer, viewPostBinding.imageContainer.getLayoutParams().height, ConstraintLayout.LayoutParams.MATCH_PARENT);
            viewPostBinding.seeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_next_36));
            viewPostBinding.seeIcon.setRotation(-90);
            viewPostBinding.seeType.setText(R.string.see_more);
            viewPostBinding.messageArea.setVisibility(View.GONE);
            viewPostBinding.postDescriptionPreview.setVisibility(View.VISIBLE);
            scrollStatus = 0;
        }
    }
}
package com.iku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.iku.models.LeaderboardModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ViewPostActivity extends AppCompatActivity {

    private static final String TAG = "ViewPost";

    private SimpleDateFormat sfdMainDate = new SimpleDateFormat("hh:mm a, MMMM dd, yyyy");

    private Bundle extras;

    private ActivityViewPostBinding viewPostBinding;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private CommentAdapter adapter;

    private String messageId;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPostBinding = ActivityViewPostBinding.inflate(getLayoutInflater());
        setContentView(viewPostBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        extras = this.getIntent().getExtras();

        setImage();
        setDetails();
        initButtons();
        messageId = extras.getString("EXTRA_MESSAGE_ID");
        initalEmojis(messageId);
        reactions(messageId);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(12)
                .setPageSize(15)
                .build();
        Query query = db.collection("iku_earth_messages").document(messageId).collection("comments").orderBy("timestamp", Query.Direction.ASCENDING);
        FirestorePagingOptions<CommentModel> options = new FirestorePagingOptions.Builder<CommentModel>()
                .setQuery(query, config, CommentModel.class)
                .build();
        adapter = new CommentAdapter(options);
        viewPostBinding.commentsView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        viewPostBinding.commentsView.setLayoutManager(linearLayoutManager);
        viewPostBinding.commentsView.setAdapter(adapter);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    viewPostBinding.commentsView.scrollToPosition(positionStart);
                }
            }
        });

        adapter.setOnItemClickListener((String name, String uid) -> {
            Intent userProfileIntent = new Intent(ViewPostActivity.this, UserProfileActivity.class);
            userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
            userProfileIntent.putExtra("EXTRA_PERSON_UID", uid);
            startActivity(userProfileIntent);
        });
        initItems();
    }

    private void initItems() {
        String message = extras.getString("EXTRA_MESSAGE");
        viewPostBinding.postDescription.setText(message);
    }

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
    }

    private void sendComment(String comment) {
        Date d = new Date();
        long timestamp = d.getTime();
        Map<String, Object> data = new HashMap<>();
        data.put("comment", comment);
        data.put("uid", user.getUid());
        data.put("commenterName", user.getDisplayName());
        data.put("timestamp", timestamp);
        data.put("readableTimestamp", FieldValue.serverTimestamp());

        db.collection("iku_earth_messages").document(messageId)
                .collection("comments").add(data)
                .addOnSuccessListener(documentReference -> {
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

    private void setImage() {
        String imageUrl = extras.getString("EXTRA_IMAGE_URL");
        if (imageUrl != null) {
            Picasso.get()
                    .load(imageUrl)
                    .noFade()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(viewPostBinding.viewedImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(imageUrl)
                                    .noFade()
                                    .into(viewPostBinding.viewedImage);
                        }
                    });
        } else {
            finish();

        }
    }

    private void initalEmojis(String messageId) {
        DocumentReference docRef = db.collection("iku_earth_messages").document(messageId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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
                                    viewPostBinding.heartUp.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                            for (String element : emoji1Array) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.emoji1.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                            for (String element : emoji2Array) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.emoji2.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                            for (String element : emoji3Array) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.emoji3.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                            for (String element : emoji4Array) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.emoji4.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                        }
                        if (downvotesCount >= 0) {
                            for (String element : HeartDownArray) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.heartDown.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                        }

                    } else {
                    }
                } else {
                }
            }
        });
    }

    private void reactions(String messageId) {

        viewPostBinding.choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "upvoters");
                disableEmojiButtons(false);
            }
        });
        viewPostBinding.choose1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "emoji1");
                disableEmojiButtons(false);
            }
        });
        viewPostBinding.choose2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "emoji2");
                disableEmojiButtons(false);
            }
        });
        viewPostBinding.choose3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "emoji3");
                disableEmojiButtons(false);
            }
        });
        viewPostBinding.choose4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "emoji4");
                disableEmojiButtons(false);
            }
        });
        viewPostBinding.choose6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "downvoters");
                disableEmojiButtons(false);
            }
        });
    }

    public void userVote(String messageDocumentID, String emoji) {
        DocumentReference docRef = db.collection("iku_earth_messages").document(messageDocumentID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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
                            newLikeorDislike(messageDocumentID, emoji, upvotesCount, downvotesCount, authorOfMessage);
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
            }
        });
    }

    private void changeLikesArray(String messageDocumentID, String currentEmoji, String previousEmoji, long upvotesCount, long downvotesCount, String authorOfMessage) {
        if (currentEmoji.equals(previousEmoji)) {
            if (currentEmoji.equals("upvoters") || currentEmoji.equals("emoji1") || currentEmoji.equals("emoji2") || currentEmoji.equals("emoji3") || currentEmoji.equals("emoji4")) {
                if (!authorOfMessage.equals(user.getUid())) {
                    db.collection("users").document(authorOfMessage)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                    db.collection("users").document(authorOfMessage)
                                            .update("points", usersData.getPoints() - 1)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            });
                                }
                            });
                }
                db.collection("iku_earth_messages").document(messageDocumentID)
                        .update("upvoteCount", upvotesCount - 1,
                                currentEmoji, FieldValue.arrayRemove(user.getUid()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                switch (currentEmoji) {
                                    case "upvoters":
                                        viewPostBinding.heartUp.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                        break;
                                    case "emoji1":
                                        viewPostBinding.emoji1.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                        break;
                                    case "emoji2":
                                        viewPostBinding.emoji2.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                        break;
                                    case "emoji3":
                                        viewPostBinding.emoji3.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                        break;
                                    case "emoji4":
                                        viewPostBinding.emoji4.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                        break;
                                }
                                disableEmojiButtons(true);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        disableEmojiButtons(true);
                    }
                });
            } else if (currentEmoji == "downvoters") {
                if (!authorOfMessage.equals(user.getUid())) {
                    db.collection("users").document(authorOfMessage).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                    db.collection("users").document(authorOfMessage)
                                            .update("points", usersData.getPoints() + 1)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            });
                                }
                            });
                }
                db.collection("iku_earth_messages").document(messageDocumentID)
                        .update("downvoteCount", downvotesCount - 1,
                                currentEmoji, FieldValue.arrayRemove(user.getUid()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                viewPostBinding.heartDown.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                disableEmojiButtons(true);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        disableEmojiButtons(true);
                    }
                });
            }

        } else if ((!currentEmoji.equals(previousEmoji)) && (currentEmoji.equals("downvoters"))) {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                db.collection("users").document(authorOfMessage)
                                        .update("points", usersData.getPoints() - 2)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });
                            }
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", upvotesCount - 1,
                            "downvoteCount", downvotesCount + 1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            changeEmojiBackground(currentEmoji, previousEmoji);
                            disableEmojiButtons(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    disableEmojiButtons(true);
                }
            });

        } else if ((previousEmoji.equals("downvoters")) && (!currentEmoji.equals(previousEmoji))) {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                db.collection("users").document(authorOfMessage)
                                        .update("points", usersData.getPoints() + 2)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });
                            }
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", upvotesCount + 1,
                            "downvoteCount", downvotesCount - 1
                    )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            changeEmojiBackground(currentEmoji, previousEmoji);
                            disableEmojiButtons(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    disableEmojiButtons(true);
                }
            });
        } else {
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            changeEmojiBackground(currentEmoji, previousEmoji);
                            disableEmojiButtons(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    disableEmojiButtons(true);
                }
            });
        }
    }

    private void newLikeorDislike(String messageDocumentID, String emoji, long UpvotesCount, long DownvotesCount, String authorOfMessage) {
        if (emoji == "downvoters") {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                db.collection("users").document(authorOfMessage)
                                        .update("points", usersData.getPoints() - 1)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });
                            }
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(emoji, FieldValue.arrayUnion(user.getUid()),
                            "downvoteCount", DownvotesCount + 1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            viewPostBinding.heartDown.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                            disableEmojiButtons(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    disableEmojiButtons(true);
                }
            });
        } else {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                db.collection("users").document(authorOfMessage)
                                        .update("points", usersData.getPoints() + 1)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                            }
                                        });
                            }
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(emoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", UpvotesCount + 1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            switch (emoji) {
                                case "upvoters":
                                    viewPostBinding.heartUp.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                case "emoji1":
                                    viewPostBinding.emoji1.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                case "emoji2":
                                    viewPostBinding.emoji2.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                case "emoji3":
                                    viewPostBinding.emoji3.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                case "emoji4":
                                    viewPostBinding.emoji4.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                default:
                                    // code block
                            }
                            disableEmojiButtons(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    disableEmojiButtons(true);
                }
            });
        }
    }

    private void changeEmojiBackground(String currentEmoji, String previousEmoji) {
        switch (currentEmoji) {
            case "upvoters":
                viewPostBinding.heartUp.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji1":
                viewPostBinding.emoji1.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji2":
                viewPostBinding.emoji2.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji3":
                viewPostBinding.emoji3.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji4":
                viewPostBinding.emoji4.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "downvoters":
                viewPostBinding.heartDown.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
        }
        switch (previousEmoji) {
            case "upvoters":
                viewPostBinding.heartUp.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji1":
                viewPostBinding.emoji1.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji2":
                viewPostBinding.emoji2.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji3":
                viewPostBinding.emoji3.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji4":
                viewPostBinding.emoji4.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "downvoters":
                viewPostBinding.heartDown.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
        }
    }

    private void disableEmojiButtons(boolean status) {
        viewPostBinding.choose.setEnabled(status);
        viewPostBinding.choose1.setEnabled(status);
        viewPostBinding.choose2.setEnabled(status);
        viewPostBinding.choose3.setEnabled(status);
        viewPostBinding.choose4.setEnabled(status);
        viewPostBinding.choose6.setEnabled(status);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
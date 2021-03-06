package com.iku;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
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
import com.google.android.material.button.MaterialButton;
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
import com.iku.app.AppConfig;
import com.iku.databinding.ActivityViewPostBinding;
import com.iku.models.CommentModel;
import com.iku.models.UserModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;

public class ViewPostActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener {

    private static final String TAG = ViewPostActivity.class.getSimpleName();

    private final SimpleDateFormat sfdMainDate = new SimpleDateFormat("hh:mm a, MMMM dd, yyyy");

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

    private int parentHeight;

    private int editTextStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPostBinding = ActivityViewPostBinding.inflate(getLayoutInflater());
        setContentView(viewPostBinding.getRoot());

        initItems();
        setDetails();
        initButtons();
        initSendButton();
        initialEmoticons(messageId);
        reactions(messageId);
        initCommentsView();
    }

    private void initCommentsView() {
        Query query = db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId).collection("comments").orderBy("timestamp", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<CommentModel> options = new FirestoreRecyclerOptions.Builder<CommentModel>()
                .setQuery(query, CommentModel.class)
                .build();
        adapter = new CommentAdapter(options);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ((SimpleItemAnimator) viewPostBinding.commentsView.getItemAnimator()).setSupportsChangeAnimations(false);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                viewPostBinding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
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
                            db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId).collection("comments").document(documentID)
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
                            db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId).collection("comments").document(documentID)
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
        String clickType = extras.getString("EXTRA_CLICK_TYPE");
        viewPostBinding.postDescription.setText(message);
        viewPostBinding.postDescriptionPreview.setText(message);

        if (clickType != null) {
            if (clickType.equals("TOP_COMMENT")) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPostBinding.imageContainer.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.appBar);
                viewPostBinding.imageContainer.setLayoutParams(params);
                parentHeight = viewPostBinding.imageContainer.getMaxHeight();
                viewPostBinding.imageContainer.setMaxHeight(400);
                viewPostBinding.seeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_next_36));
                viewPostBinding.seeIcon.setRotation(90);
                viewPostBinding.seeType.setText(R.string.see_less);
                viewPostBinding.messageArea.setVisibility(View.VISIBLE);
                viewPostBinding.postDescriptionPreview.setVisibility(View.GONE);
                scrollStatus = 1;
            } else if (clickType.equals("ADD_COMMENT")) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPostBinding.imageContainer.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.appBar);
                viewPostBinding.imageContainer.setLayoutParams(params);
                viewPostBinding.imageContainer.setMaxHeight(parentHeight);
                viewPostBinding.seeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_next_36));
                viewPostBinding.seeIcon.setRotation(90);
                viewPostBinding.seeType.setText(R.string.see_less);
                viewPostBinding.messageArea.setVisibility(View.VISIBLE);
                viewPostBinding.postDescriptionPreview.setVisibility(View.GONE);
                viewPostBinding.messageTextField.requestFocus();
                scrollStatus = 1;
            }
        }

        String imageUrl = extras.getString("EXTRA_IMAGE_URL");
        if (imageUrl != null) {
            Picasso.get()
                    .load(imageUrl)
                    .noFade()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .resize(5000, 5000)
                    .onlyScaleDown()
                    .into(viewPostBinding.viewedImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            viewPostBinding.viewedImage.setVisibility(View.VISIBLE);
                            viewPostBinding.imageProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(imageUrl)
                                    .noFade()
                                    .resize(5000, 5000)
                                    .onlyScaleDown()
                                    .into(viewPostBinding.viewedImage);
                            viewPostBinding.viewedImage.setVisibility(View.VISIBLE);
                            viewPostBinding.imageProgressBar.setVisibility(View.GONE);
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

    private void initSendButton() {
        viewPostBinding.sendMessageButton.setOnClickListener(view -> {
            final String message = viewPostBinding.messageTextField.getText().toString().trim();
            if (!message.isEmpty()) {
                sendComment(message);
                viewPostBinding.messageTextField.getText().clear();
                viewPostBinding.messageTextField.requestFocus();
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
        data.put("edited", false);
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

        db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId)
                .collection("comments").add(data)
                .addOnSuccessListener(documentReference -> {
                    db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId)
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
        DocumentReference docRef = db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    long upvotesCount, downvotesCount, timestamp, spamCount;
                    boolean isDeleted;
                    String UID, message, messageType, name;
                    ArrayList<String> HeartUpArray = (ArrayList) document.get("upvoters");
                    ArrayList<String> emoji1Array = (ArrayList) document.get("emoji1");
                    ArrayList<String> emoji2Array = (ArrayList) document.get("emoji2");
                    ArrayList<String> emoji3Array = (ArrayList) document.get("emoji3");
                    ArrayList<String> emoji4Array = (ArrayList) document.get("emoji4");
                    ArrayList<String> HeartDownArray = (ArrayList) document.get("downvoters");
                    ArrayList<String> SpamReportedByArray = (ArrayList) document.get("spamReportedBy");
                    upvotesCount = (long) document.get("upvoteCount");
                    downvotesCount = (long) document.get("downvoteCount");
                    isDeleted = (boolean) document.get("deleted");
                    UID = (String) document.get("uid");
                    message = (String) document.get("message");
                    timestamp = (long) document.get("timestamp");
                    spamCount = (long) document.get("spamCount");
                    name = (String) document.get("userName");
                    messageType = (String) document.get("type");

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

                    viewPostBinding.optionsButton.setOnClickListener(view -> {
                        if (view != null) {
                            if (!isDeleted) {

                                SharedPreferences pref = view.getContext().getSharedPreferences("iku_earth", Context.MODE_PRIVATE);
                                boolean isAdmin = pref.getBoolean("isAdmin", false);

                                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(view.getContext());
                                View parentView = getLayoutInflater().inflate(R.layout.user_bottom_sheet, null);
                                RelativeLayout profileView = parentView.findViewById(R.id.profile_layout);
                                RelativeLayout updateMessageView = parentView.findViewById(R.id.edit_option_layout);
                                RelativeLayout deleteMessageView = parentView.findViewById(R.id.delete_layout);
                                RelativeLayout addCommentView = parentView.findViewById(R.id.comment_layout);
                                RelativeLayout reportView = parentView.findViewById(R.id.report_layout);

                                ImageButton heartUpView = parentView.findViewById(R.id.chooseHeart);
                                MaterialButton emoji1View = parentView.findViewById(R.id.choose1);
                                MaterialButton emoji2View = parentView.findViewById(R.id.choose2);
                                MaterialButton emoji3View = parentView.findViewById(R.id.choose3);
                                MaterialButton emoji4View = parentView.findViewById(R.id.choose4);
                                MaterialButton heartDownView = parentView.findViewById(R.id.choose6);

                                FrameLayout heartupLayout = parentView.findViewById(R.id.heartUp);
                                FrameLayout emoji1Layout = parentView.findViewById(R.id.emoji1);
                                FrameLayout emoji2Layout = parentView.findViewById(R.id.emoji2);
                                FrameLayout emoji3Layout = parentView.findViewById(R.id.emoji3);
                                FrameLayout emoji4Layout = parentView.findViewById(R.id.emoji4);
                                FrameLayout heartdownLayout = parentView.findViewById(R.id.heartDown);

                                for (String element : HeartUpArray) {
                                    if (element.contains(user.getUid())) {
                                        heartupLayout.setBackground(ResourcesCompat.getDrawable(view.getContext().getResources(), R.drawable.hearts_button_background_selected, view.getContext().getTheme()));
                                        break;
                                    }
                                }
                                for (String element : emoji1Array) {
                                    if (element.contains(user.getUid())) {
                                        emoji1Layout.setBackground(ResourcesCompat.getDrawable(view.getContext().getResources(), R.drawable.hearts_button_background_selected, view.getContext().getTheme()));
                                        break;
                                    }
                                }
                                for (String element : emoji2Array) {
                                    if (element.contains(user.getUid())) {
                                        emoji2Layout.setBackground(ResourcesCompat.getDrawable(view.getContext().getResources(), R.drawable.hearts_button_background_selected, view.getContext().getTheme()));
                                        break;
                                    }
                                }
                                for (String element : emoji3Array) {
                                    if (element.contains(user.getUid())) {
                                        emoji3Layout.setBackground(ResourcesCompat.getDrawable(view.getContext().getResources(), R.drawable.hearts_button_background_selected, view.getContext().getTheme()));
                                        break;
                                    }
                                }
                                for (String element : emoji4Array) {
                                    if (element.contains(user.getUid())) {
                                        emoji4Layout.setBackground(ResourcesCompat.getDrawable(view.getContext().getResources(), R.drawable.hearts_button_background_selected, view.getContext().getTheme()));
                                        break;
                                    }
                                }
                                for (String element : HeartDownArray) {
                                    if (element.contains(user.getUid())) {
                                        heartdownLayout.setBackground(ResourcesCompat.getDrawable(view.getContext().getResources(), R.drawable.hearts_button_background_selected, view.getContext().getTheme()));
                                        break;
                                    }
                                }

                                heartUpView.setOnClickListener(v -> {
                                    userVote(messageId, "upvoters");
                                    bottomSheetDialog.dismiss();
                                });
                                emoji1View.setOnClickListener(v -> {
                                    userVote(messageId, "emoji1");
                                    bottomSheetDialog.dismiss();
                                });
                                emoji2View.setOnClickListener(v -> {
                                    userVote(messageId, "emoji2");
                                    bottomSheetDialog.dismiss();
                                });
                                emoji3View.setOnClickListener(v -> {
                                    userVote(messageId, "emoji3");
                                    bottomSheetDialog.dismiss();
                                });
                                emoji4View.setOnClickListener(v -> {
                                    userVote(messageId, "emoji4");
                                    bottomSheetDialog.dismiss();
                                });
                                heartDownView.setOnClickListener(v -> {
                                    userVote(messageId, "downvoters");
                                    bottomSheetDialog.dismiss();
                                });

                                if (isAdmin) {
                                    addCommentView.setOnClickListener(view1 -> {
                                        bottomSheetDialog.dismiss();
                                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPostBinding.imageContainer.getLayoutParams();
                                        params.addRule(RelativeLayout.BELOW, R.id.appBar);
                                        viewPostBinding.imageContainer.setLayoutParams(params);
                                        viewPostBinding.imageContainer.setMaxHeight(parentHeight);
                                        viewPostBinding.seeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_next_36));
                                        viewPostBinding.seeIcon.setRotation(90);
                                        viewPostBinding.seeType.setText(R.string.see_less);
                                        viewPostBinding.messageArea.setVisibility(View.VISIBLE);
                                        viewPostBinding.postDescriptionPreview.setVisibility(View.GONE);
                                        viewPostBinding.messageTextField.requestFocus();
                                        scrollStatus = 1;
                                    });
                                    deleteMessageView.setVisibility(View.VISIBLE);
                                    deleteMessageView.setOnClickListener(view1 -> {
                                        bottomSheetDialog.dismiss();
                                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(view1.getContext());
                                        materialAlertDialogBuilder.setTitle("Delete Message");
                                        materialAlertDialogBuilder.setMessage("You are about to delete this message for everyone. Are you sure?");
                                        materialAlertDialogBuilder.setPositiveButton("Delete", (dialogInterface, i) -> {
                                            deleteMessage(messageId, "admin");
                                            //log event
                                            Bundle delete_bundle = new Bundle();
                                            delete_bundle.putString("uid", user.getUid());
                                            mFirebaseAnalytics.logEvent("message_deleted", delete_bundle);
                                        }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                                            bottomSheetDialog.setContentView(parentView);
                                            bottomSheetDialog.show();
                                        }).show();
                                    });
                                    if (UID.equals(user.getUid())) {
                                        profileView.setVisibility(View.GONE);
                                        if (!(timestamp < System.currentTimeMillis() - (60 * 60 * 1000))) {
                                            updateMessageView.setVisibility(View.VISIBLE);
                                            updateMessageView.setOnClickListener(view12 -> {
                                                viewPostBinding.editWarningLayout.setVisibility(View.VISIBLE);
                                                viewPostBinding.editWarningText.setText("Editing Message");
                                                viewPostBinding.messageTextField.requestFocus();
                                                viewPostBinding.cancelEditButton.setOnClickListener(view1 -> {
                                                    editTextStatus = 0;
                                                    viewPostBinding.messageTextField.setHint("Add a comment");
                                                    initSendButton();
                                                    viewPostBinding.editWarningLayout.setVisibility(View.GONE);
                                                    viewPostBinding.messageTextField.setText("");
                                                    viewPostBinding.messageTextField.clearFocus();
                                                });
                                                viewPostBinding.messageTextField.setText(message);
                                                viewPostBinding.messageTextField.setSelection(viewPostBinding.messageTextField.getText().length());
                                                bottomSheetDialog.dismiss();
                                                editTextStatus = 1;
                                                if (editTextStatus == 1) {
                                                    viewPostBinding.messageTextField.setHint("Enter message");
                                                    viewPostBinding.sendMessageButton.setOnClickListener(view121 -> {
                                                        viewPostBinding.editWarningLayout.setVisibility(View.GONE);
                                                        editTextStatus = 0;
                                                        if (!viewPostBinding.messageTextField.getText().toString().trim().isEmpty()) {
                                                            updateMessage(messageId, viewPostBinding.messageTextField.getText().toString().trim());
                                                            viewPostBinding.messageTextField.getText().clear();
                                                            viewPostBinding.messageTextField.requestFocus();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    } else {
                                        profileView.setVisibility(View.VISIBLE);
                                        boolean isReported = false;
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
                                                    materialAlertDialogBuilder.setTitle("Report Message");
                                                    materialAlertDialogBuilder.setMessage("You are about to report this message. The community chiefs will be notified to take action as needed. Are you sure?");
                                                    materialAlertDialogBuilder.setPositiveButton("Report", (dialogInterface, i) -> {

                                                    if (SpamReportedByArray != null) {
                                                        if (!SpamReportedByArray.contains(user.getUid())) {
                                                            Map<String, Object> map = new HashMap<>();
                                                            map.put("spamReportedBy", FieldValue.arrayUnion(user.getUid()));
                                                            map.put("spamCount", spamCount + 1);
                                                            if (spamCount >= 4) {
                                                                map.put("spam", true);
                                                                map.put("deleted", true);
                                                                map.put("deletedBy", "users");
                                                            }
                                                            db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId)
                                                                    .update(map)
                                                                    .addOnSuccessListener(aVoid -> {
                                                                    });
                                                        }
                                                    }
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
                                            Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
                                            if (name != null) {
                                                userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
                                                userProfileIntent.putExtra("EXTRA_PERSON_UID", UID);
                                                this.startActivity(userProfileIntent);
                                            } else
                                                return;
                                            bottomSheetDialog.dismiss();
                                        });
                                    }
                                } else {
                                    addCommentView.setOnClickListener(view1 -> {
                                        bottomSheetDialog.dismiss();
                                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPostBinding.imageContainer.getLayoutParams();
                                        params.addRule(RelativeLayout.BELOW, R.id.appBar);
                                        viewPostBinding.imageContainer.setLayoutParams(params);
                                        viewPostBinding.imageContainer.setMaxHeight(parentHeight);
                                        viewPostBinding.seeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_next_36));
                                        viewPostBinding.seeIcon.setRotation(90);
                                        viewPostBinding.seeType.setText(R.string.see_less);
                                        viewPostBinding.messageArea.setVisibility(View.VISIBLE);
                                        viewPostBinding.postDescriptionPreview.setVisibility(View.GONE);
                                        viewPostBinding.messageTextField.requestFocus();
                                        scrollStatus = 1;
                                    });
                                    if (UID.equals(user.getUid())) {
                                        profileView.setVisibility(View.GONE);
                                        reportView.setVisibility(View.GONE);
                                        if (!(timestamp < System.currentTimeMillis() - (60 * 60 * 1000))) {
                                            updateMessageView.setVisibility(View.VISIBLE);
                                            updateMessageView.setOnClickListener(view12 -> {
                                                viewPostBinding.editWarningLayout.setVisibility(View.VISIBLE);
                                                viewPostBinding.editWarningText.setText("Editing Message");
                                                viewPostBinding.messageTextField.requestFocus();
                                                viewPostBinding.cancelEditButton.setOnClickListener(view1 -> {
                                                    editTextStatus = 0;
                                                    viewPostBinding.messageTextField.setHint("Add a comment");
                                                    initSendButton();
                                                    viewPostBinding.editWarningLayout.setVisibility(View.GONE);
                                                    viewPostBinding.messageTextField.setText("");
                                                    viewPostBinding.messageTextField.clearFocus();
                                                });
                                                viewPostBinding.messageTextField.setText(message);
                                                viewPostBinding.messageTextField.setHint("Enter message");
                                                viewPostBinding.messageTextField.setSelection(viewPostBinding.messageTextField.getText().length());
                                                bottomSheetDialog.dismiss();
                                                editTextStatus = 1;
                                                if (editTextStatus == 1) {
                                                        viewPostBinding.sendMessageButton.setOnClickListener(view121 -> {
                                                            viewPostBinding.editWarningLayout.setVisibility(View.GONE);
                                                            editTextStatus = 0;
                                                            if (!viewPostBinding.messageTextField.getText().toString().trim().isEmpty()) {
                                                                updateMessage(messageId, viewPostBinding.messageTextField.getText().toString().trim());
                                                                viewPostBinding.messageTextField.getText().clear();
                                                                viewPostBinding.messageTextField.requestFocus();
                                                            }
                                                        });
                                                }
                                            });
                                            deleteMessageView.setVisibility(View.VISIBLE);
                                            deleteMessageView.setOnClickListener(view1 -> {
                                                bottomSheetDialog.dismiss();
                                                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(view1.getContext());
                                                materialAlertDialogBuilder.setTitle("Delete Message");
                                                materialAlertDialogBuilder.setMessage("You are about to delete this message for everyone. Are you sure?");
                                                materialAlertDialogBuilder.setPositiveButton("Delete", (dialogInterface, i) -> {
                                                    deleteMessage(messageId, "author");
                                                    //log event
                                                    Bundle delete_bundle = new Bundle();
                                                    delete_bundle.putString("uid", user.getUid());
                                                    mFirebaseAnalytics.logEvent("message_deleted", delete_bundle);
                                                }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                                                    bottomSheetDialog.setContentView(parentView);
                                                    bottomSheetDialog.show();
                                                }).show();
                                            });
                                        }
                                    } else {
                                        profileView.setVisibility(View.VISIBLE);
                                        boolean isReported = false;
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
                                                    materialAlertDialogBuilder.setTitle("Report Message");
                                                    materialAlertDialogBuilder.setMessage("You are about to report this message. The community chiefs will be notified to take action as needed. Are you sure?");
                                                    materialAlertDialogBuilder.setPositiveButton("Report", (dialogInterface, i) -> {
                                                    if (SpamReportedByArray != null) {
                                                        if (!SpamReportedByArray.contains(user.getUid())) {
                                                            Map<String, Object> map = new HashMap<>();
                                                            map.put("spamReportedBy", FieldValue.arrayUnion(user.getUid()));
                                                            map.put("spamCount", spamCount + 1);
                                                            if (spamCount >= 4) {
                                                                map.put("spam", true);
                                                                map.put("deleted", true);
                                                                map.put("deletedBy", "users");
                                                            }
                                                            db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId)
                                                                    .update(map)
                                                                    .addOnSuccessListener(aVoid -> {
                                                                    });
                                                        }
                                                    }

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
                                            Intent userProfileIntent = new Intent(this, UserProfileActivity.class);

                                            if (name != null) {
                                                userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
                                                userProfileIntent.putExtra("EXTRA_PERSON_UID", UID);
                                                this.startActivity(userProfileIntent);
                                            } else
                                                return;
                                            bottomSheetDialog.dismiss();
                                        });
                                    }
                                }
                                bottomSheetDialog.setContentView(parentView);
                                bottomSheetDialog.show();
                            }
                        }
                    });
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
        DocumentReference docRef = db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID);
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
                    db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage)
                                        .update("points", FieldValue.increment(-1))
                                        .addOnSuccessListener(aVoid -> {
                                        });
                            });
                }
                db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID)
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
                    db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                final UserModel usersData = documentSnapshot.toObject(UserModel.class);
                                db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage)
                                        .update("points", FieldValue.increment(1))
                                        .addOnSuccessListener(aVoid -> {
                                        });
                            });
                }
                db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID)
                        .update("downvoteCount", downvotesCount - 1,
                                currentEmoji, FieldValue.arrayRemove(user.getUid()))
                        .addOnSuccessListener(aVoid -> {
                            viewPostBinding.heartDown.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_viewpost_activity));
                            disableEmoticonButtons(true);
                        }).addOnFailureListener(e -> disableEmoticonButtons(true));
            }

        } else if ((!currentEmoji.equals(previousEmoji)) && (currentEmoji.equals("downvoters"))) {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage)
                                    .update("points", FieldValue.increment(-2))
                                    .addOnSuccessListener(aVoid -> {
                                    });
                        });
            }
            db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID)
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
                db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage)
                                    .update("points", FieldValue.increment(2))
                                    .addOnSuccessListener(aVoid -> {
                                    });
                        });
            }
            db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID)
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
            db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID)
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
                db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage)
                                    .update("points", FieldValue.increment(-1))
                                    .addOnSuccessListener(aVoid -> {

                                    });
                        });
            }
            db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID)
                    .update(emoji, FieldValue.arrayUnion(user.getUid()),
                            "downvoteCount", DownvotesCount + 1)
                    .addOnSuccessListener(aVoid -> {
                        viewPostBinding.heartDown.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hearts_button_background_selected_viewpost_activity));
                        disableEmoticonButtons(true);
                    }).addOnFailureListener(e -> disableEmoticonButtons(true));
        } else {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            db.collection(AppConfig.USERS_COLLECTION).document(authorOfMessage)
                                    .update("points", FieldValue.increment(1))
                                    .addOnSuccessListener(aVoid -> {
                                    });
                        });
            }
            db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID)
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

    private void deleteComment(String commentDocumentID, String deletedBy) {
        Map<String, Object> map = new HashMap<>();
        map.put("deleted", true);
        map.put("deletedBy", deletedBy);
        if (deletedBy.equals("admin"))
            map.put("spam", true);
        db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId).collection("comments").document(commentDocumentID)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId)
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

    private void animateHeight() {
        if (scrollStatus == 0) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPostBinding.imageContainer.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.appBar);
            viewPostBinding.imageContainer.setLayoutParams(params);
            parentHeight = viewPostBinding.imageContainer.getMaxHeight();
            viewPostBinding.imageContainer.setMaxHeight(400);
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
            viewPostBinding.imageContainer.setMaxHeight(parentHeight);
            viewPostBinding.seeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_next_36));
            viewPostBinding.seeIcon.setRotation(-90);
            viewPostBinding.seeType.setText(R.string.see_more);
            viewPostBinding.messageArea.setVisibility(View.GONE);
            viewPostBinding.postDescriptionPreview.setVisibility(View.VISIBLE);
            scrollStatus = 0;
        }
    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            View view = viewPostBinding.commentsView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ViewPostActivity.this);
                View parentView = getLayoutInflater().inflate(R.layout.user_bottom_sheet, null);
                RelativeLayout profileView = parentView.findViewById(R.id.profile_layout);
                RelativeLayout deleteMessageView = parentView.findViewById(R.id.delete_layout);
                RelativeLayout addCommentView = parentView.findViewById(R.id.comment_layout);
                RelativeLayout updateMessageView = parentView.findViewById(R.id.edit_option_layout);
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
                    if (!(adapter.getItem(position).getTimestamp() < System.currentTimeMillis() - (60 * 60 * 1000))) {
                        updateMessageView.setVisibility(View.VISIBLE);
                        updateMessageView.setOnClickListener(view12 -> {
                            viewPostBinding.editWarningLayout.setVisibility(View.VISIBLE);
                            viewPostBinding.messageTextField.requestFocus();
                            viewPostBinding.cancelEditButton.setOnClickListener(view1 -> {
                                editTextStatus = 0;
                                initSendButton();
                                viewPostBinding.editWarningLayout.setVisibility(View.GONE);
                                viewPostBinding.messageTextField.setText("");
                                viewPostBinding.messageTextField.clearFocus();
                            });
                            viewPostBinding.messageTextField.setText(adapter.getItem(position).getComment());
                            viewPostBinding.messageTextField.setSelection(viewPostBinding.messageTextField.getText().length());
                            bottomSheetDialog.dismiss();
                            editTextStatus = 1;
                            if (editTextStatus == 1) {
                                viewPostBinding.sendMessageButton.setOnClickListener(view121 -> {
                                    viewPostBinding.editWarningLayout.setVisibility(View.GONE);
                                    editTextStatus = 0;
                                    if (!viewPostBinding.messageTextField.getText().toString().trim().isEmpty()) {
                                        updateComment(documentID, position, viewPostBinding.messageTextField.getText().toString().trim());
                                        viewPostBinding.messageTextField.getText().clear();
                                        viewPostBinding.messageTextField.requestFocus();
                                    }
                                });
                            }
                        });
                    }
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
                        materialAlertDialogBuilder.setMessage("You are about to delete this comment. Are you sure?");
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
                                materialAlertDialogBuilder.setTitle("Report Comment");
                                materialAlertDialogBuilder.setMessage("You are about to report this comment. Are you sure?");
                                materialAlertDialogBuilder.setPositiveButton("Report", (dialogInterface, i) -> {
                                    DocumentReference docRef = db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId).collection("comments").document(documentID);
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
                                                        db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId).collection("comments").document(documentID)
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

    private void updateComment(String messageDocumentID, int position, String message) {
        Date d = new Date();
        long timestamp = d.getTime();
        Map<String, Object> map = new HashMap<>();
        map.put("comment", message);
        map.put("edited", true);
        map.put("commentUpdateTime", timestamp);
        map.put("readableCommentUpdateTime", FieldValue.serverTimestamp());
        db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageId)
                .collection("comments").document(messageDocumentID).update(map)
                .addOnSuccessListener(aVoid -> {
                    adapter.notifyItemChanged(position);
                    viewPostBinding.messageTextField.getText().clear();
                    viewPostBinding.messageTextField.requestFocus();
                    editTextStatus = 0;
                    initSendButton();
                })
                .addOnFailureListener(e -> {
                    editTextStatus = 0;
                    initSendButton();
                });
    }

    private void updateMessage(String messageDocumentID, String message) {
        Date d = new Date();
        long timestamp = d.getTime();
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("edited", true);
        map.put("messageUpdateTime", timestamp);
        map.put("readableMessageUpdateTime", FieldValue.serverTimestamp());
        db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID)
                .update(map)
                .addOnSuccessListener(aVoid -> {
                    viewPostBinding.messageTextField.setText("");
                    viewPostBinding.messageTextField.requestFocus();
                    viewPostBinding.postDescription.setText(message);
                    viewPostBinding.postDescriptionPreview.setText(message);
                    viewPostBinding.messageTextField.setHint("Add a comment");
                    editTextStatus = 0;
                    initSendButton();
                })
                .addOnFailureListener(e -> {
                    editTextStatus = 0;
                    viewPostBinding.messageTextField.setHint("Add a comment");
                    initSendButton();
                });
    }

    private void deleteMessage(String messageDocumentID, String deletedBy) {
        Map<String, Object> map = new HashMap<>();
        map.put("deleted", true);
        map.put("deletedBy", deletedBy);
        if (deletedBy.equals("admin"))
            map.put("spam", true);
        db.collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document(AppConfig.GROUPS_DOCUMENT).collection(AppConfig.MESSAGES_SUB_COLLECTION).document(messageDocumentID)
                .update(map)
                .addOnSuccessListener(aVoid -> {
                    //Log event
                    Bundle delete_bundle = new Bundle();
                    delete_bundle.putString("UID", user.getUid());
                    delete_bundle.putString("Name", user.getDisplayName());
                    mFirebaseAnalytics.logEvent("deleted_message", delete_bundle);
                    onBackPressed();
                })
                .addOnFailureListener(e -> {
                });
    }
}
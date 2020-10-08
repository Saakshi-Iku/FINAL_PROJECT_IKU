package com.iku;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.iku.adapter.ChatAdapter;
import com.iku.databinding.FragmentChatBinding;
import com.iku.models.ChatModel;
import com.iku.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.ponnamkarthik.richlinkpreview.MetaData;
import io.github.ponnamkarthik.richlinkpreview.ResponseListener;
import io.github.ponnamkarthik.richlinkpreview.RichPreview;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class ChatFragment extends Fragment implements RecyclerView.OnItemTouchListener {

    private static final String TAG = ChatFragment.class.getSimpleName();

    private SimpleDateFormat sfdMainDate = new SimpleDateFormat("MMMM dd, yyyy");

    private FirebaseUser user;

    private MaterialTextView memberCount;

    private FirebaseFirestore db;

    private FragmentChatBinding binding;

    private RecyclerView mChatRecyclerview;

    private ChatAdapter chatadapter;

    private long upvotesCount;
    private long downvotesCount;

    private String authorOfMessage;

    private boolean isLiked;
    private boolean isDisliked;

    private FirebaseAnalytics mFirebaseAnalytics;

    private GestureDetectorCompat detector;

    // 0 means normal send, 1 means update an old message
    private int editTextStatus = 0;
    private int STORAGE_PERMISSION_CODE = 10;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initItems(view);
        initButtons();
        initRecyclerView();
        watchTextBox();
        getGroupMemberCount();

        return view;
    }

    private void deleteMessage(String messageDocumentID, String deletedBy) {
        Map<String, Object> map = new HashMap<>();
        map.put("deleted", true);
        map.put("deletedBy", deletedBy);
        if (deletedBy.equals("admin"))
            map.put("spam", true);
        db.collection("iku_earth_messages").document(messageDocumentID)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log event
                        Bundle delete_bundle = new Bundle();
                        delete_bundle.putString("UID", user.getUid());
                        delete_bundle.putString("Name", user.getDisplayName());
                        mFirebaseAnalytics.logEvent("deleted_message", delete_bundle);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void initItems(View view) {
        db = FirebaseFirestore.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(view.getContext());
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        detector = new GestureDetectorCompat(getActivity(), new RecyclerViewOnGestureListener());

        memberCount = view.findViewById(R.id.memberCount);
        mChatRecyclerview = view.findViewById(R.id.chatRecyclerView);
        mChatRecyclerview.addOnItemTouchListener(this);
        binding.chatDate.setVisibility(View.GONE);
    }

    private void initButtons() {

        binding.groupIcon.setOnClickListener(view -> {
            Intent goToLeaderboard = new Intent(getActivity(), LeaderboardActivity.class);
            startActivity(goToLeaderboard);

            /*Log event*/
            Bundle leaderboard_bundle = new Bundle();
            leaderboard_bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Leaderboard");
            leaderboard_bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "View");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, leaderboard_bundle);
        });


        binding.choose.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(view.getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(view.getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
            } else {
                Intent goToImageSend = new Intent(getActivity(), ChatImageActivity.class);
                goToImageSend.putExtra("documentId", "default");
                startActivity(goToImageSend);
            }
        });

        initSendButton();

    }

    private void initSendButton() {
        binding.sendMessageButton.setOnClickListener(view -> {
            final String message = binding.messageTextField.getText().toString().trim();
            if (!message.isEmpty()) {
                sendTheMessage(message);
                binding.messageTextField.setText("");
                binding.messageTextField.requestFocus();
            }
        });
    }

    private void initRecyclerView() {

        Query query = db.collection("iku_earth_messages").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatModel> options = new FirestoreRecyclerOptions.Builder<ChatModel>()
                .setQuery(query, ChatModel.class)
                .build();

        mChatRecyclerview.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        ((SimpleItemAnimator) mChatRecyclerview.getItemAnimator()).setSupportsChangeAnimations(false);
        linearLayoutManager.setReverseLayout(true);
        mChatRecyclerview.setLayoutManager(linearLayoutManager);

        chatadapter = new ChatAdapter(getContext(), options);
        chatadapter.startListening();
        chatadapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mChatRecyclerview.smoothScrollToPosition(0);
            }
        });

        mChatRecyclerview.setAdapter(chatadapter);

        mChatRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                if (dy < 0) {
                    binding.chatDate.setVisibility(View.VISIBLE);
                    if (sfdMainDate.format(new Date(chatadapter.getItem(firstVisiblePosition).getTimestamp())).equals(sfdMainDate.format(new Date().getTime())))
                        binding.chatDate.setText(R.string.today_text);
                    else if (DateUtils.isToday(chatadapter.getItem(firstVisiblePosition).getTimestamp() + DateUtils.DAY_IN_MILLIS)) {
                        binding.chatDate.setText(R.string.yesterday_text);
                    } else
                        binding.chatDate.setText(sfdMainDate.format(chatadapter.getItem(firstVisiblePosition).getTimestamp()));
                } else if (dy > 0) {
                    binding.chatDate.setVisibility(View.GONE);
                }
            }
        });

        chatadapter.setOnItemClickListener((documentSnapshot, position) -> {
            Intent viewChatImageIntent = new Intent(getContext(), ViewPostActivity.class);
            ChatModel chatModel = documentSnapshot.toObject(ChatModel.class);
            if (chatModel != null) {
                String name = chatModel.getUserName();
                String url = chatModel.getimageUrl();
                String originalUrl = chatModel.getOriginalImageUrl();
                String message = chatModel.getMessage();
                long timestamp = chatModel.getTimestamp();
                String userUid = chatModel.getUID();
                String messageId = documentSnapshot.getId();
                if (name != null && url != null) {
                    viewChatImageIntent.putExtra("EXTRA_PERSON_NAME", name);
                    viewChatImageIntent.putExtra("EXTRA_MESSAGE", message);
                    if (originalUrl != null) {
                        viewChatImageIntent.putExtra("EXTRA_IMAGE_URL", originalUrl);
                        viewChatImageIntent.putExtra("EXTRA_IMAGE_SECOND_URL", url);
                    } else
                        viewChatImageIntent.putExtra("EXTRA_IMAGE_URL", url);
                    viewChatImageIntent.putExtra("EXTRA_POST_TIMESTAMP", timestamp);
                    viewChatImageIntent.putExtra("EXTRA_MESSAGE_ID", messageId);
                    viewChatImageIntent.putExtra("EXTRA_USER_ID", userUid);
                    startActivity(viewChatImageIntent);
                }
            }
        });
    }

    private void updateMessage(String messageDocumentID, int position, String message) {
        Date d = new Date();
        long timestamp = d.getTime();
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("edited", true);
        map.put("messageUpdateTime", timestamp);
        map.put("readableMessageUpdateTime", FieldValue.serverTimestamp());
        db.collection("iku_earth_messages").document(messageDocumentID)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        chatadapter.notifyItemChanged(position);
                        binding.messageTextField.setText("");
                        binding.messageTextField.requestFocus();
                        editTextStatus = 0;
                        initSendButton();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        editTextStatus = 0;
                        initSendButton();
                    }
                });
    }

    private void watchTextBox() {
        binding.messageTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    binding.choose.setVisibility(View.VISIBLE);
                } else {
                    binding.choose.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void sendTheMessage(String message) {
        Date d = new Date();
        long timestamp = d.getTime();
        List<String> extractedUrls = extractUrls(message);
        RichPreview richPreview = new RichPreview(new ResponseListener() {
            @Override
            public void onData(MetaData metaData) {
                if (metaData != null) {
                    String linkImageUrl = metaData.getImageurl();
                    String title = metaData.getTitle();
                    String description = metaData.getDescription();
                    String url = metaData.getUrl();
                    if (user != null) {
                        Map<String, Object> docData = new HashMap<>();
                        docData.put("message", message);
                        docData.put("timestamp", timestamp);
                        docData.put("uid", user.getUid());
                        docData.put("type", "text");
                        docData.put("userName", user.getDisplayName());
                        docData.put("upvoteCount", 0);

                        docData.put("linkPreview", 1);
                        docData.put("linkPreviewImageUrl", linkImageUrl);
                        docData.put("linkPreviewTitle", title);
                        docData.put("linkPreviewDesc", description);
                        docData.put("linkPreviewUrl", url);

                        ArrayList<Object> upvotersArray = new ArrayList<>();
                        docData.put("upvoters", upvotersArray);
                        ArrayList<Object> thumbsUpArray = new ArrayList<>();
                        docData.put("emoji1", thumbsUpArray);
                        ArrayList<Object> clapsArray = new ArrayList<>();
                        docData.put("emoji2", clapsArray);
                        ArrayList<Object> thinkArray = new ArrayList<>();
                        docData.put("emoji3", thinkArray);
                        ArrayList<Object> ideaArray = new ArrayList<>();
                        docData.put("emoji4", ideaArray);
                        ArrayList<Object> dounvotersArray = new ArrayList<>();
                        docData.put("downvoters", dounvotersArray);
                        docData.put("downvoteCount", 0);
                        docData.put("edited", false);
                        ArrayList<Object> spamArray = new ArrayList<>();
                        docData.put("spamReportedBy", spamArray);
                        docData.put("spamCount", 0);
                        docData.put("spam", false);
                        docData.put("deleted", false);

                        Map<String, Object> normalMessage = new HashMap<>();
                        normalMessage.put("firstMessage", true);

                        db.collection("iku_earth_messages")
                                .add(docData)
                                .addOnSuccessListener(documentReference -> db.collection("users").document(user.getUid()).get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Boolean isFirstMessage = (Boolean) document.get("firstMessage");
                                                    if (!isFirstMessage) {
                                                        db.collection("users").document(user.getUid())
                                                                .update(normalMessage)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    editTextStatus = 0;
                                                                    binding.viewConfetti.build()
                                                                            .addColors(Color.BLUE, Color.LTGRAY, getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent))
                                                                            .setDirection(0.0, 359.0)
                                                                            .setSpeed(1f, 8f)
                                                                            .setFadeOutEnabled(true)
                                                                            .setTimeToLive(2000L)
                                                                            .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                                                                            .addSizes(new Size(10, 10f))
                                                                            .setPosition(-50f, binding.viewConfetti.getWidth() + 50f, -50f, -50f)
                                                                            .streamFor(300, 5000L);

                                                                    //Log event
                                                                    Bundle params = new Bundle();
                                                                    params.putString("type", "text");
                                                                    params.putString("uid", user.getUid());
                                                                    mFirebaseAnalytics.logEvent("first_message", params);
                                                                })
                                                                .addOnFailureListener(e -> editTextStatus = 0);
                                                    }
                                                }
                                            }
                                        }))
                                .addOnFailureListener(e -> {
                                });
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                //handle error
            }
        });
        if (!extractedUrls.isEmpty()) {
            richPreview.getPreview(extractedUrls.get(0));
        } else {
            if (user != null) {
                Map<String, Object> docData = new HashMap<>();
                docData.put("message", message);
                docData.put("timestamp", timestamp);
                docData.put("uid", user.getUid());
                docData.put("type", "text");
                docData.put("userName", user.getDisplayName());
                docData.put("upvoteCount", 0);

                docData.put("linkPreview", 0);

                ArrayList<Object> upvotersArray = new ArrayList<>();
                docData.put("upvoters", upvotersArray);
                ArrayList<Object> thumbsUpArray = new ArrayList<>();
                docData.put("emoji1", thumbsUpArray);
                ArrayList<Object> clapsArray = new ArrayList<>();
                docData.put("emoji2", clapsArray);
                ArrayList<Object> thinkArray = new ArrayList<>();
                docData.put("emoji3", thinkArray);
                ArrayList<Object> ideaArray = new ArrayList<>();
                docData.put("emoji4", ideaArray);
                ArrayList<Object> dounvotersArray = new ArrayList<>();
                docData.put("downvoters", dounvotersArray);
                docData.put("downvoteCount", 0);
                docData.put("edited", false);
                ArrayList<Object> spamArray = new ArrayList<>();
                docData.put("spamReportedBy", spamArray);
                docData.put("spamCount", 0);
                docData.put("spam", false);

                Map<String, Object> normalMessage = new HashMap<>();
                normalMessage.put("firstMessage", true);

                db.collection("iku_earth_messages")
                        .add(docData)
                        .addOnSuccessListener(documentReference -> db.collection("users").document(user.getUid()).get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            Boolean isFirstMessage = (Boolean) document.get("firstMessage");
                                            if (!isFirstMessage) {
                                                db.collection("users").document(user.getUid())
                                                        .update(normalMessage)
                                                        .addOnSuccessListener(aVoid -> {
                                                            editTextStatus = 0;
                                                            binding.viewConfetti.build()
                                                                    .addColors(Color.BLUE, Color.LTGRAY, getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent))
                                                                    .setDirection(0.0, 359.0)
                                                                    .setSpeed(1f, 8f)
                                                                    .setFadeOutEnabled(true)
                                                                    .setTimeToLive(2000L)
                                                                    .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                                                                    .addSizes(new Size(10, 10f))
                                                                    .setPosition(-50f, binding.viewConfetti.getWidth() + 50f, -50f, -50f)
                                                                    .streamFor(300, 5000L);

                                                            //Log event
                                                            Bundle params = new Bundle();
                                                            params.putString("type", "text");
                                                            params.putString("uid", user.getUid());
                                                            mFirebaseAnalytics.logEvent("first_message", params);
                                                        })
                                                        .addOnFailureListener(e -> editTextStatus = 0);
                                            }
                                        }
                                    }
                                }))
                        .addOnFailureListener(e -> {
                        });
            }
        }

    }

    public void userVote(String messageDocumentID, String emoji, int position) {
        DocumentReference docRef = db.collection("iku_earth_messages").document(messageDocumentID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
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
                        newLikeorDislike(messageDocumentID, emoji, upvotesCount, downvotesCount, authorOfMessage, position);
                    } else {
                        if (HeartupLiked) {
                            changeLikesArray(messageDocumentID, emoji, "upvoters", upvotesCount, downvotesCount, authorOfMessage, position);
                        } else if (emoji1Liked) {
                            changeLikesArray(messageDocumentID, emoji, "emoji1", upvotesCount, downvotesCount, authorOfMessage, position);
                        } else if (emoji2Liked) {
                            changeLikesArray(messageDocumentID, emoji, "emoji2", upvotesCount, downvotesCount, authorOfMessage, position);
                        } else if (emoji3Liked) {
                            changeLikesArray(messageDocumentID, emoji, "emoji3", upvotesCount, downvotesCount, authorOfMessage, position);
                        } else if (emoji4Liked) {
                            changeLikesArray(messageDocumentID, emoji, "emoji4", upvotesCount, downvotesCount, authorOfMessage, position);
                        } else if (disliked) {
                            changeLikesArray(messageDocumentID, emoji, "downvoters", upvotesCount, downvotesCount, authorOfMessage, position);
                        }
                    }
                }
            }
        });
    }

    private void changeLikesArray(String messageDocumentID, String currentEmoji, String previousEmoji, long upvotesCount, long downvotesCount, String authorOfMessage, int position) {
        if (currentEmoji.equals(previousEmoji)) {
            if (currentEmoji.equals("upvoters") || currentEmoji.equals("emoji1") || currentEmoji.equals("emoji2") || currentEmoji.equals("emoji3") || currentEmoji.equals("emoji4")) {
                if (!authorOfMessage.equals(user.getUid())) {
                    db.collection("users").document(authorOfMessage).get()
                            .addOnSuccessListener(documentSnapshot -> db.collection("users").document(authorOfMessage)
                                    .update("points", FieldValue.increment(-1))
                                    .addOnSuccessListener(aVoid -> {
                                    }));
                }
                db.collection("iku_earth_messages").document(messageDocumentID)
                        .update("upvoteCount", FieldValue.increment(-1),
                                currentEmoji, FieldValue.arrayRemove(user.getUid()))
                        .addOnSuccessListener(aVoid -> {
                        });
            } else if (currentEmoji.equals("downvoters")) {
                if (!authorOfMessage.equals(user.getUid())) {
                    db.collection("users").document(authorOfMessage).get()
                            .addOnSuccessListener(documentSnapshot -> db.collection("users").document(authorOfMessage)
                                    .update("points", FieldValue.increment(1))
                                    .addOnSuccessListener(aVoid -> {
                                    }));
                }
                db.collection("iku_earth_messages").document(messageDocumentID)
                        .update("downvoteCount", FieldValue.increment(-1),
                                currentEmoji, FieldValue.arrayRemove(user.getUid()))
                        .addOnSuccessListener(aVoid -> {
                        });
            }

        } else if ((currentEmoji != previousEmoji) && (currentEmoji.equals("downvoters"))) {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> db.collection("users").document(authorOfMessage)
                                .update("points", FieldValue.increment(-2))
                                .addOnSuccessListener(aVoid -> {
                                }));
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", upvotesCount - 1,
                            "downvoteCount", downvotesCount + 1)
                    .addOnSuccessListener(aVoid -> {
                    });

        } else if ((previousEmoji.equals("downvoters")) && (currentEmoji != previousEmoji)) {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            final UserModel usersData = documentSnapshot.toObject(UserModel.class);
                            if (usersData != null) {
                                db.collection("users").document(authorOfMessage)
                                        .update("points", usersData.getPoints() + 2)
                                        .addOnSuccessListener(aVoid -> {

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
                    .addOnSuccessListener(aVoid -> {
                    });
        } else {
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()))
                    .addOnSuccessListener(aVoid -> {
                    });
        }
        chatadapter.notifyItemChanged(position);
    }

    private void newLikeorDislike(String messageDocumentID, String emoji, long UpvotesCount, long DownvotesCount, String authorOfMessage, int position) {
        if (emoji.equals("downvoters")) {

            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            final UserModel usersData = documentSnapshot.toObject(UserModel.class);
                            db.collection("users").document(authorOfMessage)
                                    .update("points", usersData.getPoints() - 1)
                                    .addOnSuccessListener(aVoid -> {
                                    });
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(emoji, FieldValue.arrayUnion(user.getUid()),
                            "downvoteCount", DownvotesCount + 1)
                    .addOnSuccessListener(aVoid -> {
                    });
        } else {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            final UserModel usersData = documentSnapshot.toObject(UserModel.class);
                            db.collection("users").document(authorOfMessage)
                                    .update("points", usersData.getPoints() + 1)
                                    .addOnSuccessListener(aVoid -> {
                                    });
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(emoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", UpvotesCount + 1)
                    .addOnSuccessListener(aVoid -> {
                    });
        }
        chatadapter.notifyItemChanged(position);
    }

    private void requestPermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Permission Granted!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getActivity(), ChatImageActivity.class);
            i.putExtra("documentId", "default");
            startActivity(i);
        }
    }

    private void getGroupMemberCount() {
        db.collection("groups").whereEqualTo("name", "iku Experiment")
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        if (querySnapshot!=null){
                            for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                                if (change.getType() == DocumentChange.Type.ADDED) {
                                    ArrayList<String> group = (ArrayList<String>) change.getDocument().get("members");
                                    memberCount.setText("Ikulogists: " + group.size());
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        detector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            View view = mChatRecyclerview.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                int position = mChatRecyclerview.getChildAdapterPosition(view);
                if (e.getAction() == 1) {
                    isLiked = false;
                    isDisliked = false;
                    String reactedEmojiArray = "upvoters";
                    int upvotesCount = chatadapter.getItem(position).getUpvoteCount();
                    int downvotesCount = chatadapter.getItem(position).getDownvoteCount();
                    ArrayList<String> upvotersList = chatadapter.getItem(position).getupvoters();
                    ArrayList<String> emoji1Array = chatadapter.getItem(position).getEmoji1();
                    ArrayList<String> emoji2Array = chatadapter.getItem(position).getEmoji2();
                    ArrayList<String> emoji3Array = chatadapter.getItem(position).getEmoji3();
                    ArrayList<String> emoji4Array = chatadapter.getItem(position).getEmoji4();
                    ArrayList<String> downvotersArray = chatadapter.getItem(position).getDownvoters();
                    String myUID = user.getUid();
                    if (downvotesCount >= 0) {
                        for (String element : downvotersArray) {
                            if (element.contains(myUID)) {
                                isDisliked = true;
                                reactedEmojiArray = "downvoters";
                                break;
                            }
                        }
                    }
                    if (upvotesCount >= 0) {
                        if (!isLiked) {
                            for (String element : upvotersList) {
                                if (element.contains(myUID)) {
                                    isLiked = true;
                                    reactedEmojiArray = "upvoters";
                                    break;
                                }
                            }
                            if (!isLiked) {
                                for (String element : emoji1Array) {
                                    if (element.contains(myUID)) {
                                        isLiked = true;
                                        reactedEmojiArray = "emoji1";
                                        break;
                                    }
                                }
                            }
                            if (!isLiked) {
                                for (String element : emoji2Array) {
                                    if (element.contains(myUID)) {
                                        isLiked = true;
                                        reactedEmojiArray = "emoji2";
                                        break;
                                    }
                                }
                            }
                            if (!isLiked) {
                                for (String element : emoji3Array) {
                                    if (element.contains(myUID)) {
                                        isLiked = true;
                                        reactedEmojiArray = "emoji3";
                                        break;
                                    }
                                }
                            }
                            if (!isLiked) {
                                for (String element : emoji4Array) {
                                    if (element.contains(myUID)) {
                                        isLiked = true;
                                        reactedEmojiArray = "emoji4";
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (!isLiked) {
                        Map<String, Object> docData = new HashMap<>();
                        if (isDisliked) {
                            docData.put("downvoteCount", chatadapter.getItem(position).getDownvoteCount() - 1);
                            docData.put("downvoters", FieldValue.arrayRemove(user.getUid()));
                        }
                        docData.put("upvoteCount", chatadapter.getItem(position).getUpvoteCount() + 1);
                        docData.put("upvoters", FieldValue.arrayUnion(user.getUid()));

                        DocumentSnapshot snapshot = chatadapter.getSnapshots().getSnapshot(position);
                        String documentID = snapshot.getId();
                        db.collection("iku_earth_messages").document(documentID)
                                .update(docData)
                                .addOnSuccessListener(aVoid -> {
                                    if (chatadapter.getItem(position).getUID().equals(user.getUid())) {
                                        //Log event
                                        Bundle heart_params = new Bundle();
                                        heart_params.putString("type", "heart_up");
                                        heart_params.putString("messageID", documentID);
                                        heart_params.putString("author_UID", chatadapter.getItem(position).getUID());
                                        heart_params.putString("action_by", user.getUid());
                                        mFirebaseAnalytics.logEvent("hearts", heart_params);
                                    } else {
                                        db.collection("users").document(chatadapter.getItem(position).getUID()).get()
                                                .addOnSuccessListener(documentSnapshot -> {
                                                    final UserModel usersData = documentSnapshot.toObject(UserModel.class);
                                                    if (usersData != null) {
                                                        Map<String, Object> docData1 = new HashMap<>();
                                                        if (isDisliked)
                                                            docData1.put("points", usersData.getPoints() + 2);
                                                        else
                                                            docData1.put("points", usersData.getPoints() + 1);
                                                        db.collection("users").document(chatadapter.getItem(position).getUID()).update(docData1)
                                                                .addOnSuccessListener(aVoid12 -> {
                                                                    //Log event
                                                                    Bundle heart_params = new Bundle();
                                                                    heart_params.putString("type", "heart_up");
                                                                    heart_params.putString("messageID", documentID);
                                                                    heart_params.putString("author_UID", chatadapter.getItem(position).getUID());
                                                                    heart_params.putString("action_by", user.getUid());
                                                                    mFirebaseAnalytics.logEvent("hearts", heart_params);
                                                                })
                                                                .addOnFailureListener(e14 -> {
                                                                });
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(e13 -> {
                                });
                    } else {
                        DocumentSnapshot snapshot = chatadapter.getSnapshots().getSnapshot(position);
                        String documentID = snapshot.getId();
                        db.collection("iku_earth_messages").document(documentID)
                                .update("upvoteCount", chatadapter.getItem(position).getUpvoteCount() - 1,
                                        reactedEmojiArray, FieldValue.arrayRemove(user.getUid()))
                                .addOnSuccessListener(aVoid -> {
                                    if (chatadapter.getItem(position).getUID().equals(user.getUid())) {
                                        //Log event
                                        Bundle params = new Bundle();
                                        params.putString("type", "heart_down");
                                        params.putString("messageID", documentID);
                                        params.putString("author_UID", chatadapter.getItem(position).getUID());
                                        params.putString("action_by", user.getUid());
                                        mFirebaseAnalytics.logEvent("hearts", params);
                                    } else {
                                        db.collection("users").document(chatadapter.getItem(position).getUID()).get()
                                                .addOnSuccessListener(documentSnapshot -> {
                                                    final UserModel usersData = documentSnapshot.toObject(UserModel.class);
                                                    if (usersData != null) {
                                                        db.collection("users").document(chatadapter.getItem(position).getUID())
                                                                .update("points", usersData.getPoints() - 1)
                                                                .addOnSuccessListener(aVoid1 -> {
                                                                    //Log event
                                                                    Bundle heart_params = new Bundle();
                                                                    heart_params.putString("type", "heart_down");
                                                                    heart_params.putString("messageID", documentID);
                                                                    heart_params.putString("author_UID", chatadapter.getItem(position).getUID());
                                                                    heart_params.putString("action_by", user.getUid());
                                                                    mFirebaseAnalytics.logEvent("hearts", heart_params);
                                                                })
                                                                .addOnFailureListener(e1 -> {
                                                                });
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(e12 -> {
                                });
                    }
                    chatadapter.notifyItemChanged(position);
                }
            }
            return super.onDoubleTapEvent(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = mChatRecyclerview.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                int position = mChatRecyclerview.getChildAdapterPosition(view);
                if (!chatadapter.getItem(position).isDeleted()) {
                    DocumentSnapshot snapshot = chatadapter.getSnapshots().getSnapshot(position);
                    String documentID = snapshot.getId();
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
                    ArrayList<String> HeartUpArray = chatadapter.getItem(position).getupvoters();
                    ArrayList<String> emoji1Array = chatadapter.getItem(position).getEmoji1();
                    ArrayList<String> emoji2Array = chatadapter.getItem(position).getEmoji2();
                    ArrayList<String> emoji3Array = chatadapter.getItem(position).getEmoji3();
                    ArrayList<String> emoji4Array = chatadapter.getItem(position).getEmoji4();
                    ArrayList<String> HeartDownArray = chatadapter.getItem(position).getDownvoters();


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
                        userVote(documentID, "upvoters", position);
                        bottomSheetDialog.dismiss();
                    });
                    emoji1View.setOnClickListener(v -> {
                        userVote(documentID, "emoji1", position);
                        bottomSheetDialog.dismiss();
                    });
                    emoji2View.setOnClickListener(v -> {
                        userVote(documentID, "emoji2", position);
                        bottomSheetDialog.dismiss();
                    });
                    emoji3View.setOnClickListener(v -> {
                        userVote(documentID, "emoji3", position);
                        bottomSheetDialog.dismiss();
                    });
                    emoji4View.setOnClickListener(v -> {
                        userVote(documentID, "emoji4", position);
                        bottomSheetDialog.dismiss();
                    });
                    heartDownView.setOnClickListener(v -> {
                        userVote(documentID, "downvoters", position);
                        bottomSheetDialog.dismiss();
                    });
                    String UID = chatadapter.getItem(position).getUID();
                    addCommentView.setOnClickListener(view1 -> {
                        Intent viewChatImageIntent = new Intent(getContext(), ViewPostActivity.class);
                        String name = chatadapter.getItem(position).getUserName();
                        String url = chatadapter.getItem(position).getimageUrl();
                        String originalUrl = chatadapter.getItem(position).getOriginalImageUrl();
                        String message = chatadapter.getItem(position).getMessage();
                        long timestamp = chatadapter.getItem(position).getTimestamp();
                        String userUid = chatadapter.getItem(position).getUID();
                        if (name != null) {
                            viewChatImageIntent.putExtra("EXTRA_PERSON_NAME", name);
                            viewChatImageIntent.putExtra("EXTRA_MESSAGE", message);
                            if (originalUrl != null) {
                                viewChatImageIntent.putExtra("EXTRA_IMAGE_URL", originalUrl);
                                viewChatImageIntent.putExtra("EXTRA_IMAGE_SECOND_URL", url);
                            } else
                                viewChatImageIntent.putExtra("EXTRA_IMAGE_URL", url);
                            viewChatImageIntent.putExtra("EXTRA_POST_TIMESTAMP", timestamp);
                            viewChatImageIntent.putExtra("EXTRA_MESSAGE_ID", documentID);
                            viewChatImageIntent.putExtra("EXTRA_USER_ID", userUid);
                            bottomSheetDialog.dismiss();
                            startActivity(viewChatImageIntent);
                        }
                    });

                    if (UID.equals(user.getUid())) {
                        profileView.setVisibility(View.GONE);
                        reportView.setVisibility(View.GONE);
                        if (!(chatadapter.getItem(position).getTimestamp() < System.currentTimeMillis() - (60 * 60 * 1000))) {
                            updateMessageView.setVisibility(View.VISIBLE);
                            updateMessageView.setOnClickListener(view12 -> {
                                binding.editWarning.setVisibility(View.VISIBLE);
                                binding.cancelEdit.setOnClickListener(view1 -> {
                                    editTextStatus = 0;
                                    initSendButton();
                                    binding.editWarning.setVisibility(View.GONE);
                                    binding.messageTextField.setText("");
                                    binding.messageTextField.clearFocus();
                                });
                                if (chatadapter.getItem(position).getType().equals("text")) {
                                    binding.messageTextField.setText(chatadapter.getItem(position).getMessage());
                                    binding.messageTextField.setSelection(binding.messageTextField.getText().length());
                                    bottomSheetDialog.dismiss();
                                    editTextStatus = 1;
                                    if (editTextStatus == 1) {
                                        binding.sendMessageButton.setOnClickListener(view121 -> {
                                            binding.editWarning.setVisibility(View.GONE);
                                            editTextStatus = 0;
                                            updateMessage(documentID, position, binding.messageTextField.getText().toString().trim());
                                            binding.messageTextField.setText("");
                                            binding.messageTextField.requestFocus();
                                        });
                                    }
                                } else if (chatadapter.getItem(position).getType().equals("image")) {
                                    binding.editWarning.setVisibility(View.GONE);
                                    Intent goToImageSend = new Intent(getActivity(), ChatImageActivity.class);
                                    goToImageSend.putExtra("documentId", documentID);
                                    goToImageSend.putExtra("message", chatadapter.getItem(position).getMessage());
                                    goToImageSend.putExtra("imageUrl", chatadapter.getItem(position).getimageUrl());
                                    bottomSheetDialog.dismiss();
                                    startActivity(goToImageSend);
                                }

                            });
                        }
                        deleteMessageView.setVisibility(View.VISIBLE);
                        deleteMessageView.setOnClickListener(view1 -> {
                            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(view1.getContext());
                            materialAlertDialogBuilder.setTitle("Delete Message");
                            materialAlertDialogBuilder.setMessage("Delete for everyone?");
                            materialAlertDialogBuilder.setPositiveButton("Delete", (dialogInterface, i) -> {
                                deleteMessage(documentID, "author");
                                bottomSheetDialog.dismiss();
                                //log event
                                Bundle delete_bundle = new Bundle();
                                delete_bundle.putString("uid", user.getUid());
                                mFirebaseAnalytics.logEvent("message_deleted", delete_bundle);
                            }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                            }).show();
                        });
                        bottomSheetDialog.setContentView(parentView);
                        bottomSheetDialog.show();
                    } else {
                        profileView.setVisibility(View.VISIBLE);
                        boolean isReported = false;
                        ArrayList<String> SpamReportedByArray = chatadapter.getItem(position).getSpamReportedBy();
                        for (String element : SpamReportedByArray) {
                            if (element.contains(user.getUid())) {
                                isReported = true;
                                break;
                            }
                        }
                        if(!isReported) {
                            reportView.setVisibility(View.VISIBLE);
                            reportView.setOnClickListener(view13 -> {
                                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(view13.getContext());
                                materialAlertDialogBuilder.setTitle("Report Spam");
                                materialAlertDialogBuilder.setMessage("Are you sure?");
                                materialAlertDialogBuilder.setPositiveButton("Report", (dialogInterface, i) -> {
                                    DocumentReference docRef = db.collection("iku_earth_messages").document(documentID);
                                    docRef.get().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                ArrayList<String> spamReportedArray = (ArrayList) document.get("spamReportedBy");
                                                long spamCount = (long) document.get("spamCount");
                                                if (spamReportedArray!=null){
                                                    if (!spamReportedArray.contains(user.getUid())) {
                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("spamReportedBy", FieldValue.arrayUnion(user.getUid()));
                                                        map.put("spamCount", spamCount + 1);
                                                        if (spamCount >= 4) {
                                                            map.put("spam", true);
                                                            map.put("deleted", true);
                                                            map.put("deletedBy", "users");
                                                        }
                                                        db.collection("iku_earth_messages").document(documentID)
                                                                .update(map)
                                                                .addOnSuccessListener(aVoid -> {
                                                                });
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    bottomSheetDialog.dismiss();
                                    //log event
                                    Bundle spam_bundle = new Bundle();
                                    spam_bundle.putString("uid", user.getUid());
                                    mFirebaseAnalytics.logEvent("message_reported_spam", spam_bundle);
                                }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                                }).show();
                            });
                        }

                        profileView.setOnClickListener(view2 -> {
                            Intent userProfileIntent = new Intent(ChatFragment.this.getContext(), UserProfileActivity.class);

                            String name = chatadapter.getItem(position).getUserName();
                            String userUID = chatadapter.getItem(position).getUID();
                            if (name != null) {
                                userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
                                userProfileIntent.putExtra("EXTRA_PERSON_UID", userUID);
                                ChatFragment.this.startActivity(userProfileIntent);
                            } else
                                return;
                            bottomSheetDialog.dismiss();
                        });

                        SharedPreferences pref = view.getContext().getSharedPreferences("iku_earth", Context.MODE_PRIVATE);
                        boolean isAdmin = pref.getBoolean("isAdmin", false);
                        if (isAdmin) {
                            deleteMessageView.setVisibility(View.VISIBLE);
                            deleteMessageView.setOnClickListener(view1 -> {
                                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(view1.getContext());
                                materialAlertDialogBuilder.setTitle("Delete Message");
                                materialAlertDialogBuilder.setMessage("Delete for everyone?");
                                materialAlertDialogBuilder.setPositiveButton("Delete", (dialogInterface, i) -> {
                                    deleteMessage(documentID, "admin");
                                    bottomSheetDialog.dismiss();
                                    //log event
                                    Bundle delete_bundle = new Bundle();
                                    delete_bundle.putString("uid", user.getUid());
                                    mFirebaseAnalytics.logEvent("message_deleted", delete_bundle);
                                }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                                }).show();
                            });
                        }
                        bottomSheetDialog.setContentView(parentView);
                        bottomSheetDialog.show();
                    }
                }
            }
            super.onLongPress(e);
        }
    }

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
            if (containedUrls.size() == 1)
                break;
        }
        return containedUrls;
    }
}
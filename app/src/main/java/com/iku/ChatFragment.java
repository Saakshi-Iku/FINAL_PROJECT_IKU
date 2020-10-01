package com.iku;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.fragment.app.Fragment;
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
import com.iku.models.LeaderboardModel;
import com.iku.utils.ItemClickSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class ChatFragment extends Fragment {

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

    private int STORAGE_PERMISSION_CODE = 10;

    private boolean isLiked;

    private boolean isDisliked;
    private FirebaseAnalytics mFirebaseAnalytics;

    // 0 means normal send, 1 means update an old message
    private int editTextStatus = 0;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    private void deleteMessage(String messageDocumentID) {
        db.collection("iku_earth_messages").document(messageDocumentID)
                .delete()
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
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        memberCount = view.findViewById(R.id.memberCount);
        mChatRecyclerview = view.findViewById(R.id.chatRecyclerView);
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
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getActivity(),
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
                        binding.chatDate.setText("Today");
                    else if (DateUtils.isToday(chatadapter.getItem(firstVisiblePosition).getTimestamp() + DateUtils.DAY_IN_MILLIS)) {
                        binding.chatDate.setText("Yesterday");
                    } else
                        binding.chatDate.setText(sfdMainDate.format(chatadapter.getItem(firstVisiblePosition).getTimestamp()));
                } else if (dy > 0) {
                    binding.chatDate.setVisibility(View.GONE);
                }
            }
        });

        ItemClickSupport.addTo(mChatRecyclerview).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
            }

            @Override
            public void onItemDoubleClicked(RecyclerView recyclerView, final int position, View v) {
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
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (chatadapter.getItem(position).getUID().equals(user.getUid())) {
                                        //Log event
                                        Bundle heart_params = new Bundle();
                                        heart_params.putString("type", "heart_up");
                                        heart_params.putString("messageID", documentID);
                                        heart_params.putString("author_UID", chatadapter.getItem(position).getUID());
                                        heart_params.putString("action_by", user.getUid());
                                        mFirebaseAnalytics.logEvent("hearts", heart_params);
                                    } else {
                                        db.collection("users").document(chatadapter.getItem(position).getUID())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                                        Map<String, Object> docData = new HashMap<>();
                                                        if (isDisliked)
                                                            docData.put("points", usersData.getPoints() + 2);
                                                        else
                                                            docData.put("points", usersData.getPoints() + 1);
                                                        db.collection("users").document(chatadapter.getItem(position).getUID())
                                                                .update(docData)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        //Log event
                                                                        Bundle heart_params = new Bundle();
                                                                        heart_params.putString("type", "heart_up");
                                                                        heart_params.putString("messageID", documentID);
                                                                        heart_params.putString("author_UID", chatadapter.getItem(position).getUID());
                                                                        heart_params.putString("action_by", user.getUid());
                                                                        mFirebaseAnalytics.logEvent("hearts", heart_params);
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                } else {
                    DocumentSnapshot snapshot = chatadapter.getSnapshots().getSnapshot(position);
                    String documentID = snapshot.getId();
                    db.collection("iku_earth_messages").document(documentID)
                            .update("upvoteCount", chatadapter.getItem(position).getUpvoteCount() - 1,
                                    reactedEmojiArray, FieldValue.arrayRemove(user.getUid()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (chatadapter.getItem(position).getUID().equals(user.getUid())) {
                                        //Log event
                                        Bundle params = new Bundle();
                                        params.putString("type", "heart_down");
                                        params.putString("messageID", documentID);
                                        params.putString("author_UID", chatadapter.getItem(position).getUID());
                                        params.putString("action_by", user.getUid());
                                        mFirebaseAnalytics.logEvent("hearts", params);
                                    } else {
                                        db.collection("users").document(chatadapter.getItem(position).getUID())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                                        db.collection("users").document(chatadapter.getItem(position).getUID())
                                                                .update("points", usersData.getPoints() - 1)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        //Log event
                                                                        Bundle heart_params = new Bundle();
                                                                        heart_params.putString("type", "heart_down");
                                                                        heart_params.putString("messageID", documentID);
                                                                        heart_params.putString("author_UID", chatadapter.getItem(position).getUID());
                                                                        heart_params.putString("action_by", user.getUid());
                                                                        mFirebaseAnalytics.logEvent("hearts", heart_params);
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }

                chatadapter.notifyItemChanged(position);
            }
        });

        chatadapter.setOnItemClickListener((documentSnapshot, position) -> {
            Intent viewChatImageIntent = new Intent(getContext(), ViewPostActivity.class);

            ChatModel chatModel = documentSnapshot.toObject(ChatModel.class);
            String name = chatModel.getUserName();
            String url = chatModel.getimageUrl();
            String message = chatModel.getMessage();
            long timestamp = chatModel.getTimestamp();
            String userUid = chatModel.getUID();
            String messageId = documentSnapshot.getId();
            if (name != null && url != null) {
                viewChatImageIntent.putExtra("EXTRA_PERSON_NAME", name);
                viewChatImageIntent.putExtra("EXTRA_MESSAGE", message);
                viewChatImageIntent.putExtra("EXTRA_IMAGE_URL", url);
                viewChatImageIntent.putExtra("EXTRA_POST_TIMESTAMP", timestamp);
                viewChatImageIntent.putExtra("EXTRA_MESSAGE_ID", messageId);
                viewChatImageIntent.putExtra("EXTRA_USER_ID", userUid);
                startActivity(viewChatImageIntent);
            }
        });

        chatadapter.setOnItemLongClickListener(new ChatAdapter.onItemLongClickListener() {
            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot, int position) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                View parentView = getLayoutInflater().inflate(R.layout.user_bottom_sheet, null);
                ChatModel chatModel = documentSnapshot.toObject(ChatModel.class);
                RelativeLayout profileView = parentView.findViewById(R.id.profile_layout);
                RelativeLayout updateMessageView = parentView.findViewById(R.id.edit_option_layout);
                RelativeLayout deleteMessageView = parentView.findViewById(R.id.delete_layout);
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
                ArrayList<String> HeartUpArray = (ArrayList) documentSnapshot.get("upvoters");
                ArrayList<String> emoji1Array = (ArrayList) documentSnapshot.get("emoji1");
                ArrayList<String> emoji2Array = (ArrayList) documentSnapshot.get("emoji2");
                ArrayList<String> emoji3Array = (ArrayList) documentSnapshot.get("emoji3");
                ArrayList<String> emoji4Array = (ArrayList) documentSnapshot.get("emoji4");
                ArrayList<String> HeartDownArray = (ArrayList) documentSnapshot.get("downvoters");


                for (String element : HeartUpArray) {
                    if (element.contains(user.getUid())) {
                        heartupLayout.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.hearts_button_background_selected, getContext().getTheme()));
                        break;
                    }
                }
                for (String element : emoji1Array) {
                    if (element.contains(user.getUid())) {
                        emoji1Layout.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.hearts_button_background_selected, getContext().getTheme()));
                        break;
                    }
                }
                for (String element : emoji2Array) {
                    if (element.contains(user.getUid())) {
                        emoji2Layout.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.hearts_button_background_selected, getContext().getTheme()));
                        break;
                    }
                }
                for (String element : emoji3Array) {
                    if (element.contains(user.getUid())) {
                        emoji3Layout.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.hearts_button_background_selected, getContext().getTheme()));
                        break;
                    }
                }
                for (String element : emoji4Array) {
                    if (element.contains(user.getUid())) {
                        emoji4Layout.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.hearts_button_background_selected, getContext().getTheme()));
                        break;
                    }
                }
                for (String element : HeartDownArray) {
                    if (element.contains(user.getUid())) {
                        heartdownLayout.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.hearts_button_background_selected, getContext().getTheme()));
                        break;
                    }
                }


                heartUpView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userVote(documentSnapshot.getId(), "upvoters", position);
                        bottomSheetDialog.dismiss();
                    }
                });
                emoji1View.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userVote(documentSnapshot.getId(), "emoji1", position);
                        bottomSheetDialog.dismiss();
                    }
                });
                emoji2View.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userVote(documentSnapshot.getId(), "emoji2", position);
                        bottomSheetDialog.dismiss();
                    }
                });
                emoji3View.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userVote(documentSnapshot.getId(), "emoji3", position);
                        bottomSheetDialog.dismiss();
                    }
                });
                emoji4View.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userVote(documentSnapshot.getId(), "emoji4", position);
                        bottomSheetDialog.dismiss();
                    }
                });
                heartDownView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userVote(documentSnapshot.getId(), "downvoters", position);
                        bottomSheetDialog.dismiss();
                    }
                });

                String UID = chatModel.getUID();
                if (UID.equals(user.getUid())) {
                    profileView.setVisibility(View.GONE);
                    reportView.setVisibility(View.GONE);
                    Log.i(TAG, "MESSAGE :" + chatModel.getTimestamp() + "\nSystem -1Hr" + (System.currentTimeMillis() - (60 * 60 * 1000)));
                    if (!(chatModel.getTimestamp() < System.currentTimeMillis() - (60 * 60 * 1000))) {
                        updateMessageView.setVisibility(View.VISIBLE);
                        updateMessageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                binding.editWarning.setVisibility(View.VISIBLE);
                                binding.cancelEdit.setOnClickListener(view1 -> {
                                    editTextStatus = 0;
                                    initSendButton();
                                    binding.editWarning.setVisibility(View.GONE);
                                    binding.messageTextField.setText("");
                                    binding.messageTextField.clearFocus();
                                });
                                if (chatModel.getType().equals("text")) {
                                    binding.messageTextField.setText(chatModel.getMessage());
                                    binding.messageTextField.setSelection(binding.messageTextField.getText().length());
                                    bottomSheetDialog.dismiss();
                                    editTextStatus = 1;
                                    if (editTextStatus == 1) {
                                        binding.sendMessageButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                binding.editWarning.setVisibility(View.GONE);
                                                editTextStatus = 0;
                                                updateMessage(documentSnapshot.getId(), position, binding.messageTextField.getText().toString().trim());
                                                binding.messageTextField.setText("");
                                                binding.messageTextField.requestFocus();
                                            }
                                        });
                                    }
                                } else if (chatModel.getType().equals("image")) {
                                    binding.editWarning.setVisibility(View.GONE);
                                    Intent goToImageSend = new Intent(getActivity(), ChatImageActivity.class);
                                    goToImageSend.putExtra("documentId", documentSnapshot.getId());
                                    goToImageSend.putExtra("message", chatModel.getMessage());
                                    goToImageSend.putExtra("imageUrl", chatModel.getimageUrl());
                                    bottomSheetDialog.dismiss();
                                    startActivity(goToImageSend);
                                }

                            }
                        });
                    }
                    deleteMessageView.setVisibility(View.VISIBLE);
                    deleteMessageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteMessage(documentSnapshot.getId());
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bottomSheetDialog.setContentView(parentView);
                    bottomSheetDialog.show();
                } else {
                    profileView.setVisibility(View.VISIBLE);
                    reportView.setVisibility(View.VISIBLE);
                    bottomSheetDialog.setContentView(parentView);
                    bottomSheetDialog.show();

                    reportView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DocumentReference docRef = db.collection("iku_earth_messages").document(documentSnapshot.getId());
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
                                                if (spamCount >= 4)
                                                    map.put("spam", true);
                                                db.collection("iku_earth_messages").document(documentSnapshot.getId())
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
                            bottomSheetDialog.dismiss();
                        }
                    });

                    profileView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent userProfileIntent = new Intent(ChatFragment.this.getContext(), UserProfileActivity.class);

                            String name = chatModel.getUserName();
                            String userUID = chatModel.getUID();
                            if (name != null) {
                                userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
                                userProfileIntent.putExtra("EXTRA_PERSON_UID", userUID);
                                ChatFragment.this.startActivity(userProfileIntent);
                            } else
                                return;
                            bottomSheetDialog.dismiss();
                        }
                    });

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
        if (user != null) {
            Map<String, Object> docData = new HashMap<>();
            docData.put("message", message);
            docData.put("timestamp", timestamp);
            docData.put("uid", user.getUid());
            docData.put("type", "text");
            docData.put("userName", user.getDisplayName());
            docData.put("upvoteCount", 0);
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
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            db.collection("users").document(user.getUid()).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Boolean isFirstMessage = (Boolean) document.get("firstMessage");
                                                    if (!isFirstMessage) {
                                                        db.collection("users").document(user.getUid())
                                                                .update(normalMessage)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
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


                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        editTextStatus = 0;
                                                                    }
                                                                });

                                                    }
                                                }
                                            }
                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    public void userVote(String messageDocumentID, String emoji, int position) {
        DocumentReference docRef = db.collection("iku_earth_messages").document(messageDocumentID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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
            }
        });
    }

    private void changeLikesArray(String messageDocumentID, String currentEmoji, String previousEmoji, long upvotesCount, long downvotesCount, String authorOfMessage, int position) {
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
                            }
                        });
            } else if (currentEmoji.equals("downvoters")) {
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
                            }
                        });
            }

        } else if ((currentEmoji != previousEmoji) && (currentEmoji.equals("downvoters"))) {
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
                        }
                    });

        } else if ((previousEmoji.equals("downvoters")) && (currentEmoji != previousEmoji)) {
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
                        }
                    });
        } else {
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
        }
        chatadapter.notifyItemChanged(position);
    }

    private void newLikeorDislike(String messageDocumentID, String emoji, long UpvotesCount, long DownvotesCount, String authorOfMessage, int position) {
        if (emoji.equals("downvoters")) {

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
                        }
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

                        for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                ArrayList<String> group = (ArrayList<String>) change.getDocument().get("members");
                                memberCount.setText("Ikulogists: " + group.size());
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
}
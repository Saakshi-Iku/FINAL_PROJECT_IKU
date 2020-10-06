package com.iku.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.util.Linkify;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.iku.R;
import com.iku.models.ChatModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatModel, RecyclerView.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_IMAGE_LEFT = 2;
    public static final int MSG_TYPE_IMAGE_RIGHT = 3;
    public static final int MSG_TYPE_DELETED_LEFT = 4;
    public static final int MSG_TYPE_DELETED_RIGHT = 5;
    private static final String TAG = ChatAdapter.class.getSimpleName();
    private ChatAdapter.OnItemClickListener listener;
    private ChatAdapter.onItemLongClickListener longClickListener;
    private Context mContext;
    private SimpleDateFormat sfd = new SimpleDateFormat("hh:mm a");
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public ChatAdapter(Context context, @NonNull FirestoreRecyclerOptions<ChatModel> options) {
        super(options);
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i, @NonNull final ChatModel chatModel) {

        switch (viewHolder.getItemViewType()) {

            case MSG_TYPE_DELETED_RIGHT:
                ChatRightDeletedViewHolder chatRightDeletedViewHolder = (ChatRightDeletedViewHolder) viewHolder;
                long timeStampDeletedRight = chatModel.getTimestamp();
                chatRightDeletedViewHolder.messageTime.setText(sfd.format(new Date(timeStampDeletedRight)));

                break;

            case MSG_TYPE_DELETED_LEFT:
                ChatLeftDeletedViewHolder chatLeftDeletedViewHolder = (ChatLeftDeletedViewHolder) viewHolder;
                long timeStampDeletedLeft = chatModel.getTimestamp();
                chatLeftDeletedViewHolder.senderName.setText(chatModel.getUserName());
                chatLeftDeletedViewHolder.messageTime.setText(sfd.format(new Date(timeStampDeletedLeft)));
                chatLeftDeletedViewHolder.messageTime2.setText(sfd.format(new Date(timeStampDeletedLeft)));
                if (chatLeftDeletedViewHolder.senderName.getVisibility() == View.VISIBLE) {
                    chatLeftDeletedViewHolder.messageTime.setVisibility(View.VISIBLE);
                    chatLeftDeletedViewHolder.messageTime2.setVisibility(View.GONE);
                } else {
                    chatLeftDeletedViewHolder.messageTime.setVisibility(View.GONE);
                    chatLeftDeletedViewHolder.messageTime2.setVisibility(View.VISIBLE);
                }
                break;

            case MSG_TYPE_LEFT:
                ChatLeftViewHolder chatLeftViewHolder = (ChatLeftViewHolder) viewHolder;
                long timeStampLeft = chatModel.getTimestamp();

                chatLeftViewHolder.messageText.setText(chatModel.getMessage());
                chatLeftViewHolder.messageText.setMovementMethod(BetterLinkMovementMethod.getInstance());
                chatLeftViewHolder.messageText.setLinkTextColor(Color.parseColor("#1111b7"));
                BetterLinkMovementMethod
                        .linkify(Linkify.WEB_URLS, (Activity) mContext)
                        .setOnLinkLongClickListener(((textView, url) -> {

                            return true;
                        }))
                        .setOnLinkClickListener((textView, url) -> {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(mContext, Uri.parse(url));
                            return true;
                        });

                chatLeftViewHolder.messageTime.setText(sfd.format(new Date(timeStampLeft)));
                chatLeftViewHolder.messageTime2.setText(sfd.format(new Date(timeStampLeft)));
                chatLeftViewHolder.messageTime3.setText(sfd.format(new Date(timeStampLeft)));
                chatLeftViewHolder.senderName.setText(chatModel.getUserName());
                /*if(chatModel.getSpamCount() > 0){
                    chatLeftViewHolder.reportLayout.setVisibility(View.VISIBLE);
                    chatLeftViewHolder.spamCount.setText(String.valueOf(chatModel.getSpamCount()));
                }*/
                if (chatModel.isEdited())
                    chatLeftViewHolder.edited.setVisibility(View.VISIBLE);
                else
                    chatLeftViewHolder.edited.setVisibility(View.GONE);

                //Change the visibilty according to the visibility of the sender's name.

                if (chatLeftViewHolder.senderName.getVisibility() == View.VISIBLE) {
                    chatLeftViewHolder.messageTime.setVisibility(View.VISIBLE);
                    chatLeftViewHolder.messageTime2.setVisibility(View.GONE);
                    chatLeftViewHolder.messageTime3.setVisibility(View.GONE);
                } else {
                    //Change the visibilities according to senderName's visibility
                    if (chatModel.isEdited()) {
                        chatLeftViewHolder.messageTime3.setVisibility(View.VISIBLE);
                        chatLeftViewHolder.messageTime.setVisibility(View.GONE);
                        chatLeftViewHolder.messageTime2.setVisibility(View.GONE);
                    } else {
                        if (chatModel.getMessage().length() <= 25) {
                            chatLeftViewHolder.messageTime2.setVisibility(View.VISIBLE);
                            chatLeftViewHolder.messageTime.setVisibility(View.GONE);
                            chatLeftViewHolder.messageTime3.setVisibility(View.GONE);
                        } else {
                            chatLeftViewHolder.messageTime3.setVisibility(View.VISIBLE);
                            chatLeftViewHolder.messageTime.setVisibility(View.GONE);
                            chatLeftViewHolder.messageTime2.setVisibility(View.GONE);
                        }
                    }
                }
                if (chatModel.getUpvoteCount() > 0) {
                    chatLeftViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                    chatLeftViewHolder.upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                    if (chatModel.getupvoters().size() > 0)
                        chatLeftViewHolder.itemView.findViewById(R.id.heartImage).setVisibility(View.VISIBLE);
                    else
                        chatLeftViewHolder.itemView.findViewById(R.id.heartImage).setVisibility(View.GONE);

                    if (chatModel.getEmoji1().size() > 0)
                        chatLeftViewHolder.itemView.findViewById(R.id.emoji1).setVisibility(View.VISIBLE);
                    else
                        chatLeftViewHolder.itemView.findViewById(R.id.emoji1).setVisibility(View.GONE);

                    if (chatModel.getEmoji2().size() > 0)
                        chatLeftViewHolder.itemView.findViewById(R.id.emoji2).setVisibility(View.VISIBLE);
                    else
                        chatLeftViewHolder.itemView.findViewById(R.id.emoji2).setVisibility(View.GONE);

                    if (chatModel.getEmoji3().size() > 0)
                        chatLeftViewHolder.itemView.findViewById(R.id.emoji3).setVisibility(View.VISIBLE);
                    else
                        chatLeftViewHolder.itemView.findViewById(R.id.emoji3).setVisibility(View.GONE);

                    if (chatModel.getEmoji4().size() > 0)
                        chatLeftViewHolder.itemView.findViewById(R.id.emoji4).setVisibility(View.VISIBLE);
                    else
                        chatLeftViewHolder.itemView.findViewById(R.id.emoji4).setVisibility(View.GONE);
                } else
                    chatLeftViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);

                break;

            case MSG_TYPE_RIGHT:
                ChatRightViewHolder chatRightViewHolder = (ChatRightViewHolder) viewHolder;
                long timeStampRight = chatModel.getTimestamp();
                if (chatModel.getUpvoteCount() > 0) {
                    chatRightViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                    chatRightViewHolder.upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));

                    if (chatModel.getupvoters().size() > 0)
                        chatRightViewHolder.itemView.findViewById(R.id.heartImage).setVisibility(View.VISIBLE);
                    else
                        chatRightViewHolder.itemView.findViewById(R.id.heartImage).setVisibility(View.GONE);

                    if (chatModel.getEmoji1().size() > 0)
                        chatRightViewHolder.itemView.findViewById(R.id.emoji1).setVisibility(View.VISIBLE);
                    else
                        chatRightViewHolder.itemView.findViewById(R.id.emoji1).setVisibility(View.GONE);

                    if (chatModel.getEmoji2().size() > 0)
                        chatRightViewHolder.itemView.findViewById(R.id.emoji2).setVisibility(View.VISIBLE);
                    else
                        chatRightViewHolder.itemView.findViewById(R.id.emoji2).setVisibility(View.GONE);

                    if (chatModel.getEmoji3().size() > 0)
                        chatRightViewHolder.itemView.findViewById(R.id.emoji3).setVisibility(View.VISIBLE);
                    else
                        chatRightViewHolder.itemView.findViewById(R.id.emoji3).setVisibility(View.GONE);

                    if (chatModel.getEmoji4().size() > 0)
                        chatRightViewHolder.itemView.findViewById(R.id.emoji4).setVisibility(View.VISIBLE);
                    else
                        chatRightViewHolder.itemView.findViewById(R.id.emoji4).setVisibility(View.GONE);
                } else
                    chatRightViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);

                chatRightViewHolder.messageText.setText(chatModel.getMessage());
                chatRightViewHolder.messageText.setMovementMethod(BetterLinkMovementMethod.getInstance());
                chatRightViewHolder.messageText.setLinkTextColor(Color.parseColor("#1111b7"));
                BetterLinkMovementMethod
                        .linkify(Linkify.WEB_URLS, (Activity) mContext)
                        .setOnLinkClickListener((textView, url) -> {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(mContext, Uri.parse(url));
                            return true;
                        });

                chatRightViewHolder.messageTime.setText(sfd.format(new Date(timeStampRight)));
                chatRightViewHolder.messageTime2.setText(sfd.format(new Date(timeStampRight)));
                chatRightViewHolder.upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));

                /*if(chatModel.getSpamCount() > 0){
                    chatRightViewHolder.reportLayout.setVisibility(View.VISIBLE);
                    chatRightViewHolder.spamCount.setText(String.valueOf(chatModel.getSpamCount()));
                }*/
                if (chatModel.isEdited()) {
                    chatRightViewHolder.edited.setVisibility(View.VISIBLE);
                    chatRightViewHolder.messageTime2.setVisibility(View.VISIBLE);
                    chatRightViewHolder.messageTime.setVisibility(View.GONE);
                } else {
                    chatRightViewHolder.edited.setVisibility(View.GONE);
                    if (chatModel.getMessage().length() <= 25) {
                        chatRightViewHolder.messageTime.setVisibility(View.VISIBLE);
                        chatRightViewHolder.messageTime2.setVisibility(View.GONE);
                    } else {
                        chatRightViewHolder.messageTime2.setVisibility(View.VISIBLE);
                        chatRightViewHolder.messageTime.setVisibility(View.GONE);

                    }
                }
                break;


            case MSG_TYPE_IMAGE_LEFT:
                final ChatLeftImageViewHolder chatLeftImageViewHolder = (ChatLeftImageViewHolder) viewHolder;
                long timeStampImageLeft = chatModel.getTimestamp();

                chatLeftImageViewHolder.messageText.setText(chatModel.getMessage());
                chatLeftImageViewHolder.messageText.setMovementMethod(BetterLinkMovementMethod.getInstance());
                chatLeftImageViewHolder.messageText.setLinkTextColor(Color.parseColor("#1111b7"));
                BetterLinkMovementMethod
                        .linkify(Linkify.WEB_URLS, (Activity) mContext)
                        .setOnLinkClickListener((textView, url) -> {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(mContext, Uri.parse(url));
                            return true;
                        });

                chatLeftImageViewHolder.messageTime.setText(sfd.format(new Date(timeStampImageLeft)));
                chatLeftImageViewHolder.messageTime2.setText(sfd.format(new Date(timeStampImageLeft)));
                chatLeftImageViewHolder.messageTime3.setText(sfd.format(new Date(timeStampImageLeft)));
                chatLeftImageViewHolder.senderName.setText(chatModel.getUserName());
                /*if(chatModel.getSpamCount() > 0){
                    chatLeftImageViewHolder.reportLayout.setVisibility(View.VISIBLE);
                    chatLeftImageViewHolder.spamCount.setText(String.valueOf(chatModel.getSpamCount()));
                }*/

                if (chatLeftImageViewHolder.senderName.getVisibility() == View.VISIBLE) {
                    chatLeftImageViewHolder.messageTime.setVisibility(View.VISIBLE);
                    chatLeftImageViewHolder.messageTime2.setVisibility(View.GONE);
                    chatLeftImageViewHolder.messageTime3.setVisibility(View.GONE);
                } else {
                    if (chatModel.isEdited()) {
                        chatLeftImageViewHolder.messageTime3.setVisibility(View.VISIBLE);
                        chatLeftImageViewHolder.messageTime.setVisibility(View.GONE);
                        chatLeftImageViewHolder.messageTime2.setVisibility(View.GONE);
                    } else {
                        if (chatModel.getMessage().length() <= 25) {
                            chatLeftImageViewHolder.messageTime2.setVisibility(View.VISIBLE);
                            chatLeftImageViewHolder.messageTime.setVisibility(View.GONE);
                            chatLeftImageViewHolder.messageTime3.setVisibility(View.GONE);
                        } else {
                            chatLeftImageViewHolder.messageTime3.setVisibility(View.VISIBLE);
                            chatLeftImageViewHolder.messageTime.setVisibility(View.GONE);
                            chatLeftImageViewHolder.messageTime2.setVisibility(View.GONE);
                        }
                    }
                }

                if (chatModel.getUpvoteCount() > 0) {
                    chatLeftImageViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                    chatLeftImageViewHolder.upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                    if (chatModel.getupvoters().size() > 0)
                        chatLeftImageViewHolder.itemView.findViewById(R.id.heartImage).setVisibility(View.VISIBLE);
                    else
                        chatLeftImageViewHolder.itemView.findViewById(R.id.heartImage).setVisibility(View.GONE);

                    if (chatModel.getEmoji1().size() > 0)
                        chatLeftImageViewHolder.itemView.findViewById(R.id.emoji1).setVisibility(View.VISIBLE);
                    else
                        chatLeftImageViewHolder.itemView.findViewById(R.id.emoji1).setVisibility(View.GONE);

                    if (chatModel.getEmoji2().size() > 0)
                        chatLeftImageViewHolder.itemView.findViewById(R.id.emoji2).setVisibility(View.VISIBLE);
                    else
                        chatLeftImageViewHolder.itemView.findViewById(R.id.emoji2).setVisibility(View.GONE);

                    if (chatModel.getEmoji3().size() > 0)
                        chatLeftImageViewHolder.itemView.findViewById(R.id.emoji3).setVisibility(View.VISIBLE);
                    else
                        chatLeftImageViewHolder.itemView.findViewById(R.id.emoji3).setVisibility(View.GONE);

                    if (chatModel.getEmoji4().size() > 0)
                        chatLeftImageViewHolder.itemView.findViewById(R.id.emoji4).setVisibility(View.VISIBLE);
                    else
                        chatLeftImageViewHolder.itemView.findViewById(R.id.emoji4).setVisibility(View.GONE);
                } else
                    chatLeftImageViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);

                Picasso.get()
                        .load(chatModel.getimageUrl())
                        .noFade()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(chatLeftImageViewHolder.receiverImage, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(chatModel.getimageUrl())
                                        .noFade()
                                        .placeholder(R.drawable.progress_animation)
                                        .into(chatLeftImageViewHolder.receiverImage);
                            }
                        });

                break;

            case MSG_TYPE_IMAGE_RIGHT:
                final ChatRightImageViewHolder chatRightImageViewHolder = (ChatRightImageViewHolder) viewHolder;
                long timeStampImageRight = chatModel.getTimestamp();

                chatRightImageViewHolder.messageText.setText(chatModel.getMessage());
                chatRightImageViewHolder.messageText.setMovementMethod(BetterLinkMovementMethod.getInstance());
                chatRightImageViewHolder.messageText.setLinkTextColor(Color.parseColor("#1111b7"));
                BetterLinkMovementMethod
                        .linkify(Linkify.WEB_URLS, (Activity) mContext)
                        .setOnLinkClickListener((textView, url) -> {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(mContext, Uri.parse(url));
                            return true;
                        });

                chatRightImageViewHolder.messageTime.setText(sfd.format(new Date(timeStampImageRight)));
                chatRightImageViewHolder.messageTime2.setText(sfd.format(new Date(timeStampImageRight)));

                /*if(chatModel.getSpamCount() > 0){
                    chatRightImageViewHolder.reportLayout.setVisibility(View.VISIBLE);
                    chatRightImageViewHolder.spamCount.setText(String.valueOf(chatModel.getSpamCount()));
                }*/

                if (chatModel.isEdited()) {
                    chatRightImageViewHolder.edited.setVisibility(View.VISIBLE);
                    chatRightImageViewHolder.messageTime2.setVisibility(View.VISIBLE);
                } else {
                    chatRightImageViewHolder.edited.setVisibility(View.GONE);
                    if (chatModel.getMessage().length() <= 25) {
                        chatRightImageViewHolder.messageTime.setVisibility(View.VISIBLE);
                        chatRightImageViewHolder.messageTime2.setVisibility(View.GONE);
                    } else {
                        chatRightImageViewHolder.messageTime2.setVisibility(View.VISIBLE);
                        chatRightImageViewHolder.messageTime.setVisibility(View.GONE);

                    }
                }

                if (chatModel.getUpvoteCount() > 0) {
                    chatRightImageViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                    chatRightImageViewHolder.upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                    if (chatModel.getupvoters().size() > 0)
                        chatRightImageViewHolder.itemView.findViewById(R.id.heartImage).setVisibility(View.VISIBLE);
                    else
                        chatRightImageViewHolder.itemView.findViewById(R.id.heartImage).setVisibility(View.GONE);

                    if (chatModel.getEmoji1().size() > 0)
                        chatRightImageViewHolder.itemView.findViewById(R.id.emoji1).setVisibility(View.VISIBLE);
                    else
                        chatRightImageViewHolder.itemView.findViewById(R.id.emoji1).setVisibility(View.GONE);

                    if (chatModel.getEmoji2().size() > 0)
                        chatRightImageViewHolder.itemView.findViewById(R.id.emoji2).setVisibility(View.VISIBLE);
                    else
                        chatRightImageViewHolder.itemView.findViewById(R.id.emoji2).setVisibility(View.GONE);

                    if (chatModel.getEmoji3().size() > 0)
                        chatRightImageViewHolder.itemView.findViewById(R.id.emoji3).setVisibility(View.VISIBLE);
                    else
                        chatRightImageViewHolder.itemView.findViewById(R.id.emoji3).setVisibility(View.GONE);

                    if (chatModel.getEmoji4().size() > 0)
                        chatRightImageViewHolder.itemView.findViewById(R.id.emoji4).setVisibility(View.VISIBLE);
                    else
                        chatRightImageViewHolder.itemView.findViewById(R.id.emoji4).setVisibility(View.GONE);
                } else
                    chatRightImageViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);

                Picasso.get()
                        .load(chatModel.getimageUrl())
                        .noFade()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(chatRightImageViewHolder.sentImage, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(chatModel.getimageUrl())
                                        .noFade()
                                        .placeholder(R.drawable.progress_animation)
                                        .into(chatRightImageViewHolder.sentImage);
                            }
                        });
                break;

        }
    }

    public void setOnItemClickListener(ChatAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(ChatAdapter.onItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
            return new ChatRightViewHolder(view);
        } else if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);
            return new ChatLeftViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left_image, parent, false);
            return new ChatLeftImageViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right_image, parent, false);
            return new ChatRightImageViewHolder(view);
        } else if (viewType == MSG_TYPE_DELETED_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_deleted_right, parent, false);
            return new ChatRightDeletedViewHolder(view);
        } else if (viewType == MSG_TYPE_DELETED_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_deleted_left, parent, false);
            return new ChatLeftDeletedViewHolder(view);
        } else
            return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isSpam()) {
            if (getItem(position).getUID().equals(user.getUid()))
                return MSG_TYPE_DELETED_RIGHT;
            else
                return MSG_TYPE_DELETED_LEFT;
        } else {
            if (getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text")) {
                return MSG_TYPE_RIGHT;
            } else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text")) {
                return MSG_TYPE_LEFT;
            } else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("image")) {
                return MSG_TYPE_IMAGE_LEFT;
            } else if (getItem(position).getType().equals("image") && getItem(position).getimageUrl() != null && getItem(position).getUID().equals(user.getUid()))
                return MSG_TYPE_IMAGE_RIGHT;
            else
                return 0;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public interface onItemLongClickListener {
        void onItemLongClick(DocumentSnapshot documentSnapshot, int position);
    }

    public class ChatLeftViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, messageTime2, messageTime3, senderName, upvoteCount, edited, spamCount;
        private LinearLayout reportLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatLeftViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            messageTime3 = itemView.findViewById(R.id.message_time3);
            senderName = itemView.findViewById(R.id.sender_name);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            reportLayout = itemView.findViewById(R.id.flag_layout);
            spamCount = itemView.findViewById(R.id.spamCount_textView);

            itemView.setOnLongClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                }
                return false;
            });
        }
    }

    public class ChatLeftImageViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, messageTime2, messageTime3, senderName, upvoteCount, edited, spamCount;
        private ImageView receiverImage;
        private MaterialButton viewPostBtn;
        private LinearLayout reportLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatLeftImageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            messageTime3 = itemView.findViewById(R.id.message_time3);
            senderName = itemView.findViewById(R.id.sender_name);
            receiverImage = itemView.findViewById(R.id.receivedImage);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            viewPostBtn = itemView.findViewById(R.id.viewPostButton);
            reportLayout = itemView.findViewById(R.id.flag_layout);
            spamCount = itemView.findViewById(R.id.spamCount_textView);

            itemView.setOnLongClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                }
                return false;
            });

            viewPostBtn.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public class ChatRightImageViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, messageTime2, messageTime3, upvoteCount, edited, spamCount;
        private ImageView sentImage;
        private MaterialButton viewPostBtn;
        private LinearLayout reportLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightImageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            messageTime3 = itemView.findViewById(R.id.message_time3);
            sentImage = itemView.findViewById(R.id.sentImage);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            viewPostBtn = itemView.findViewById(R.id.viewPostButton);
            spamCount = itemView.findViewById(R.id.spamCount_textView);
            reportLayout = itemView.findViewById(R.id.flag_layout);

            itemView.setOnLongClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                }
                return false;
            });

            viewPostBtn.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public class ChatRightViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, messageTime2, upvoteCount, edited, spamCount;
        private LinearLayout reportLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            reportLayout = itemView.findViewById(R.id.flag_layout);
            spamCount = itemView.findViewById(R.id.spamCount_textView);

            itemView.setOnLongClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                }
                return false;
            });
        }
    }

    private class ChatRightDeletedViewHolder extends RecyclerView.ViewHolder {
        private MaterialTextView messageTime;

        public ChatRightDeletedViewHolder(View itemView) {
            super(itemView);
            messageTime = itemView.findViewById(R.id.message_time);
        }
    }

    private class ChatLeftDeletedViewHolder extends RecyclerView.ViewHolder {
        private MaterialTextView messageTime, messageTime2, senderName;

        public ChatLeftDeletedViewHolder(View itemView) {
            super(itemView);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            senderName = itemView.findViewById(R.id.sender_name);
        }
    }
}

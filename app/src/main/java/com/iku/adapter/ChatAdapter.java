package com.iku.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import de.hdodenhof.circleimageview.CircleImageView;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatModel, RecyclerView.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_LEFT_LINK = 6;
    public static final int MSG_TYPE_RIGHT_LINK = 7;
    public static final int MSG_TYPE_IMAGE_LEFT = 2;
    public static final int MSG_TYPE_IMAGE_RIGHT = 3;
    public static final int MSG_TYPE_DELETED_LEFT = 4;
    public static final int MSG_TYPE_DELETED_RIGHT = 5;
    private static final String TAG = ChatAdapter.class.getSimpleName();
    private ChatAdapter.OnItemClickListener listener;
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
                chatLeftDeletedViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_LEFT:
                ChatLeftViewHolder chatLeftViewHolder = (ChatLeftViewHolder) viewHolder;
                chatLeftViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_LEFT_LINK:
                ChatLeftLinkViewHolder chatLeftLinkViewHolder = (ChatLeftLinkViewHolder) viewHolder;
                chatLeftLinkViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_RIGHT:
                ChatRightViewHolder chatRightViewHolder = (ChatRightViewHolder) viewHolder;
                chatRightViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_RIGHT_LINK:
                ChatRightLinkViewHolder chatRightLinkViewHolder = (ChatRightLinkViewHolder) viewHolder;
                chatRightLinkViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_IMAGE_LEFT:
                final ChatLeftImageViewHolder chatLeftImageViewHolder = (ChatLeftImageViewHolder) viewHolder;
                chatLeftImageViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_IMAGE_RIGHT:
                final ChatRightImageViewHolder chatRightImageViewHolder = (ChatRightImageViewHolder) viewHolder;
                chatRightImageViewHolder.bindChat(chatModel);
                break;
        }
    }

    public void setOnItemClickListener(ChatAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == MSG_TYPE_LEFT_LINK) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);
            return new ChatLeftLinkViewHolder(view);
        } else if (viewType == MSG_TYPE_RIGHT_LINK) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
            return new ChatRightLinkViewHolder(view);
        } else if (viewType == MSG_TYPE_RIGHT) {
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
            if (getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text") && getItem(position).getLinkPreview() == 0)
                return MSG_TYPE_RIGHT;
            else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text") && getItem(position).getLinkPreview() == 0)
                return MSG_TYPE_LEFT;
            else if (getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text") && getItem(position).getLinkPreview() == 1)
                return MSG_TYPE_RIGHT_LINK;
            else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text") && getItem(position).getLinkPreview() == 1)
                return MSG_TYPE_LEFT_LINK;
            else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("image"))
                return MSG_TYPE_IMAGE_LEFT;
            else if (getItem(position).getType().equals("image") && getItem(position).getimageUrl() != null && getItem(position).getUID().equals(user.getUid()))
                return MSG_TYPE_IMAGE_RIGHT;
            else
                return 0;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public class ChatLeftViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, messageTime2, messageTime3, senderName, upvoteCount, edited, spamCount, linkTitle, linkDescription, linkSource;
        private LinearLayout reportLayout;
        private ImageView linkPreviewImage;
        private ConstraintLayout linkPreviewLayout;

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
            linkTitle = itemView.findViewById(R.id.linkTitle);
            linkDescription = itemView.findViewById(R.id.linkPreviewDescription);
            linkSource = itemView.findViewById(R.id.linkSourceDomain);
            linkPreviewImage = itemView.findViewById(R.id.linkPreviewImage);
            linkPreviewLayout = itemView.findViewById(R.id.linkPreview);
        }

        void bindChat(ChatModel chatModel) {
            long timeStampLeft = chatModel.getTimestamp();

            messageText.setText(chatModel.getMessage());
            messageText.setMovementMethod(BetterLinkMovementMethod.getInstance());
            messageText.setLinkTextColor(Color.parseColor("#1111b7"));
            BetterLinkMovementMethod
                    .linkify(Linkify.WEB_URLS, (Activity) mContext)
                    .setOnLinkLongClickListener(((textView, url) -> {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("link", url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
                        return true;
                    }))
                    .setOnLinkClickListener((textView, url) -> {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(mContext, Uri.parse(url));
                        return true;
                    });

            messageTime.setText(sfd.format(new Date(timeStampLeft)));
            messageTime2.setText(sfd.format(new Date(timeStampLeft)));
            messageTime3.setText(sfd.format(new Date(timeStampLeft)));
            senderName.setText(chatModel.getUserName());
                /*if(chatModel.getSpamCount() > 0){
                    chatLeftViewHolder.reportLayout.setVisibility(View.VISIBLE);
                    chatLeftViewHolder.spamCount.setText(String.valueOf(chatModel.getSpamCount()));
                }*/
            if (chatModel.isEdited())
                edited.setVisibility(View.VISIBLE);
            else
                edited.setVisibility(View.GONE);

            //Change the visibilty according to the visibility of the sender's name.

            if (senderName.getVisibility() == View.VISIBLE) {
                messageTime.setVisibility(View.VISIBLE);
                messageTime2.setVisibility(View.GONE);
                messageTime3.setVisibility(View.GONE);
            } else {
                //Change the visibilities according to senderName's visibility
                if (chatModel.isEdited()) {
                    messageTime3.setVisibility(View.VISIBLE);
                    messageTime.setVisibility(View.GONE);
                    messageTime2.setVisibility(View.GONE);
                } else {
                    if (chatModel.getMessage().length() <= 25) {
                        messageTime2.setVisibility(View.VISIBLE);
                        messageTime.setVisibility(View.GONE);
                        messageTime3.setVisibility(View.GONE);
                    } else {
                        messageTime3.setVisibility(View.VISIBLE);
                        messageTime.setVisibility(View.GONE);
                        messageTime2.setVisibility(View.GONE);
                    }
                }
            }
            if (chatModel.getUpvoteCount() > 0) {
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                if (chatModel.getupvoters().size() > 0)
                    itemView.findViewById(R.id.heartImage).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.heartImage).setVisibility(View.GONE);

                if (chatModel.getEmoji1().size() > 0)
                    itemView.findViewById(R.id.emoji1).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji1).setVisibility(View.GONE);

                if (chatModel.getEmoji2().size() > 0)
                    itemView.findViewById(R.id.emoji2).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji2).setVisibility(View.GONE);

                if (chatModel.getEmoji3().size() > 0)
                    itemView.findViewById(R.id.emoji3).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji3).setVisibility(View.GONE);

                if (chatModel.getEmoji4().size() > 0)
                    itemView.findViewById(R.id.emoji4).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji4).setVisibility(View.GONE);
            } else
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);
        }
    }

    public class ChatLeftLinkViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, messageTime2, messageTime3, senderName, upvoteCount, edited, spamCount, linkTitle, linkDescription, linkSource;
        private LinearLayout reportLayout;
        private ImageView linkPreviewImage;
        private ConstraintLayout linkPreviewLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatLeftLinkViewHolder(@NonNull View itemView) {
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
            linkTitle = itemView.findViewById(R.id.linkTitle);
            linkDescription = itemView.findViewById(R.id.linkPreviewDescription);
            linkSource = itemView.findViewById(R.id.linkSourceDomain);
            linkPreviewImage = itemView.findViewById(R.id.linkPreviewImage);
            linkPreviewLayout = itemView.findViewById(R.id.linkPreview);
        }

        void bindChat(ChatModel chatModel) {
            long timeStampLeft = chatModel.getTimestamp();

            if (chatModel.getLinkPreview() == 1) {
                linkPreviewLayout.setVisibility(View.VISIBLE);
                Picasso.get().load(chatModel.getLinkPreviewImageUrl()).noFade().into(linkPreviewImage);
                linkTitle.setText(chatModel.getLinkPreviewTitle());
                linkDescription.setText(chatModel.getLinkPreviewDesc());
                linkSource.setText(chatModel.getLinkPreviewUrl());
            }
            messageText.setText(chatModel.getMessage());
            messageText.setMovementMethod(BetterLinkMovementMethod.getInstance());
            messageText.setLinkTextColor(Color.parseColor("#343493"));
            BetterLinkMovementMethod
                    .linkify(Linkify.WEB_URLS, (Activity) mContext)
                    .setOnLinkLongClickListener(((textView, url) -> {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("link", url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
                        return true;
                    }))
                    .setOnLinkClickListener((textView, url) -> {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(mContext, Uri.parse(url));
                        return true;
                    });

            messageTime.setText(sfd.format(new Date(timeStampLeft)));
            messageTime2.setText(sfd.format(new Date(timeStampLeft)));
            messageTime3.setText(sfd.format(new Date(timeStampLeft)));
            senderName.setText(chatModel.getUserName());
                /*if(chatModel.getSpamCount() > 0){
                    chatLeftViewHolder.reportLayout.setVisibility(View.VISIBLE);
                    chatLeftViewHolder.spamCount.setText(String.valueOf(chatModel.getSpamCount()));
                }*/
            if (chatModel.isEdited())
                edited.setVisibility(View.VISIBLE);
            else
                edited.setVisibility(View.GONE);

            //Change the visibilty according to the visibility of the sender's name.

            if (senderName.getVisibility() == View.VISIBLE) {
                messageTime.setVisibility(View.VISIBLE);
                messageTime2.setVisibility(View.GONE);
                messageTime3.setVisibility(View.GONE);
            } else {
                //Change the visibilities according to senderName's visibility
                if (chatModel.isEdited()) {
                    messageTime3.setVisibility(View.VISIBLE);
                    messageTime.setVisibility(View.GONE);
                    messageTime2.setVisibility(View.GONE);
                } else {
                    if (chatModel.getMessage().length() <= 25) {
                        messageTime2.setVisibility(View.VISIBLE);
                        messageTime.setVisibility(View.GONE);
                        messageTime3.setVisibility(View.GONE);
                    } else {
                        messageTime3.setVisibility(View.VISIBLE);
                        messageTime.setVisibility(View.GONE);
                        messageTime2.setVisibility(View.GONE);
                    }
                }
            }
            if (chatModel.getUpvoteCount() > 0) {
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                if (chatModel.getupvoters().size() > 0)
                    itemView.findViewById(R.id.heartImage).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.heartImage).setVisibility(View.GONE);

                if (chatModel.getEmoji1().size() > 0)
                    itemView.findViewById(R.id.emoji1).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji1).setVisibility(View.GONE);

                if (chatModel.getEmoji2().size() > 0)
                    itemView.findViewById(R.id.emoji2).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji2).setVisibility(View.GONE);

                if (chatModel.getEmoji3().size() > 0)
                    itemView.findViewById(R.id.emoji3).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji3).setVisibility(View.GONE);

                if (chatModel.getEmoji4().size() > 0)
                    itemView.findViewById(R.id.emoji4).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji4).setVisibility(View.GONE);
            } else
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);
        }
    }

    public class ChatLeftImageViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, messageTime2, messageTime3, senderName, upvoteCount, edited, spamCount, commentTextView;
        private ImageView receiverImage;
        private MaterialButton viewPostBtn;
        private LinearLayout reportLayout;
        private ConstraintLayout commentsLayout;
        private CircleImageView commenterProfilePicture;

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
            commentsLayout = itemView.findViewById(R.id.commentsPreview);
            commenterProfilePicture = itemView.findViewById(R.id.profileImage);
            commentTextView = itemView.findViewById(R.id.comment);

            viewPostBtn.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }

        void bindChat(ChatModel chatModel) {
            long timeStampImageLeft = chatModel.getTimestamp();

            messageText.setText(chatModel.getMessage());
            messageText.setMovementMethod(BetterLinkMovementMethod.getInstance());
            messageText.setLinkTextColor(Color.parseColor("#343493"));
            BetterLinkMovementMethod
                    .linkify(Linkify.WEB_URLS, (Activity) mContext)
                    .setOnLinkLongClickListener(((textView, url) -> {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("link", url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
                        return true;
                    }))
                    .setOnLinkClickListener((textView, url) -> {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(mContext, Uri.parse(url));
                        return true;
                    });

            if (chatModel.getTopComment() != null) {
                commentsLayout.setVisibility(View.VISIBLE);
                commentTextView.setText(chatModel.getTopComment());
                Picasso.get()
                        .load(chatModel.getTopCommenterImageUrl())
                        .noFade()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(commenterProfilePicture, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(chatModel.getTopCommenterImageUrl())
                                        .noFade()
                                        .placeholder(R.drawable.ic_circle_account)
                                        .into(commenterProfilePicture);
                            }
                        });
            }

            messageTime.setText(sfd.format(new Date(timeStampImageLeft)));
            messageTime2.setText(sfd.format(new Date(timeStampImageLeft)));
            messageTime3.setText(sfd.format(new Date(timeStampImageLeft)));
            senderName.setText(chatModel.getUserName());
                /*if(chatModel.getSpamCount() > 0){
                    chatLeftImageViewHolder.reportLayout.setVisibility(View.VISIBLE);
                    chatLeftImageViewHolder.spamCount.setText(String.valueOf(chatModel.getSpamCount()));
                }*/

            if (senderName.getVisibility() == View.VISIBLE) {
                messageTime.setVisibility(View.VISIBLE);
                messageTime2.setVisibility(View.GONE);
                messageTime3.setVisibility(View.GONE);
            } else {
                if (chatModel.isEdited()) {
                    messageTime3.setVisibility(View.VISIBLE);
                    messageTime.setVisibility(View.GONE);
                    messageTime2.setVisibility(View.GONE);
                } else {
                    if (chatModel.getMessage().length() <= 25) {
                        messageTime2.setVisibility(View.VISIBLE);
                        messageTime.setVisibility(View.GONE);
                        messageTime3.setVisibility(View.GONE);
                    } else {
                        messageTime3.setVisibility(View.VISIBLE);
                        messageTime.setVisibility(View.GONE);
                        messageTime2.setVisibility(View.GONE);
                    }
                }
            }

            if (chatModel.getUpvoteCount() > 0) {
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                if (chatModel.getupvoters().size() > 0)
                    itemView.findViewById(R.id.heartImage).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.heartImage).setVisibility(View.GONE);

                if (chatModel.getEmoji1().size() > 0)
                    itemView.findViewById(R.id.emoji1).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji1).setVisibility(View.GONE);

                if (chatModel.getEmoji2().size() > 0)
                    itemView.findViewById(R.id.emoji2).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji2).setVisibility(View.GONE);

                if (chatModel.getEmoji3().size() > 0)
                    itemView.findViewById(R.id.emoji3).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji3).setVisibility(View.GONE);

                if (chatModel.getEmoji4().size() > 0)
                    itemView.findViewById(R.id.emoji4).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji4).setVisibility(View.GONE);
            } else
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);

            Picasso.get()
                    .load(chatModel.getimageUrl())
                    .noFade()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(receiverImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(chatModel.getimageUrl())
                                    .noFade()
                                    .placeholder(R.drawable.progress_animation)
                                    .into(receiverImage);
                        }
                    });

        }

    }

    public class ChatRightImageViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, messageTime2, messageTime3, upvoteCount, edited, spamCount, commentTextView;
        private ImageView sentImage;
        private MaterialButton viewPostBtn;
        private LinearLayout reportLayout;
        private ConstraintLayout commentsLayout;
        private CircleImageView commenterProfilePicture;

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
            commentsLayout = itemView.findViewById(R.id.commentsPreview);
            commenterProfilePicture = itemView.findViewById(R.id.profileImage);
            commentTextView = itemView.findViewById(R.id.comment);

            viewPostBtn.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }

        void bindChat(ChatModel chatModel) {
            long timeStampImageRight = chatModel.getTimestamp();

            messageText.setText(chatModel.getMessage());
            messageText.setMovementMethod(BetterLinkMovementMethod.getInstance());
            messageText.setLinkTextColor(Color.parseColor("#343493"));
            BetterLinkMovementMethod
                    .linkify(Linkify.WEB_URLS, (Activity) mContext)
                    .setOnLinkLongClickListener(((textView, url) -> {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("link", url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
                        return true;
                    }))
                    .setOnLinkClickListener((textView, url) -> {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(mContext, Uri.parse(url));
                        return true;
                    });

            messageTime.setText(sfd.format(new Date(timeStampImageRight)));
            messageTime2.setText(sfd.format(new Date(timeStampImageRight)));

            /*if(chatModel.getSpamCount() > 0){
                    chatRightImageViewHolder.reportLayout.setVisibility(View.VISIBLE);
                    chatRightImageViewHolder.spamCount.setText(String.valueOf(chatModel.getSpamCount()));
            }*/

            if (chatModel.getTopComment() != null) {
                commentsLayout.setVisibility(View.VISIBLE);
                commentTextView.setText(chatModel.getTopComment());
                Picasso.get()
                        .load(chatModel.getTopCommenterImageUrl())
                        .noFade()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(commenterProfilePicture, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(chatModel.getTopCommenterImageUrl())
                                        .noFade()
                                        .placeholder(R.drawable.ic_circle_account)
                                        .into(commenterProfilePicture);
                            }
                        });
            }

            if (chatModel.isEdited()) {
                edited.setVisibility(View.VISIBLE);
                messageTime2.setVisibility(View.VISIBLE);
            } else {
                edited.setVisibility(View.GONE);
                if (chatModel.getMessage().length() <= 25) {
                    messageTime.setVisibility(View.VISIBLE);
                    messageTime2.setVisibility(View.GONE);
                } else {
                    messageTime2.setVisibility(View.VISIBLE);
                    messageTime.setVisibility(View.GONE);
                }
            }

            if (chatModel.getUpvoteCount() > 0) {
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                if (chatModel.getupvoters().size() > 0)
                    itemView.findViewById(R.id.heartImage).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.heartImage).setVisibility(View.GONE);

                if (chatModel.getEmoji1().size() > 0)
                    itemView.findViewById(R.id.emoji1).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji1).setVisibility(View.GONE);

                if (chatModel.getEmoji2().size() > 0)
                    itemView.findViewById(R.id.emoji2).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji2).setVisibility(View.GONE);

                if (chatModel.getEmoji3().size() > 0)
                    itemView.findViewById(R.id.emoji3).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji3).setVisibility(View.GONE);

                if (chatModel.getEmoji4().size() > 0)
                    itemView.findViewById(R.id.emoji4).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji4).setVisibility(View.GONE);
            } else
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);

            Picasso.get()
                    .load(chatModel.getimageUrl())
                    .noFade()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(sentImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(chatModel.getimageUrl())
                                    .noFade()
                                    .placeholder(R.drawable.progress_animation)
                                    .into(sentImage);
                        }
                    });
        }
    }

    public class ChatRightViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, messageTime2, upvoteCount, edited, spamCount, linkTitle, linkDescription, linkSource;
        private LinearLayout reportLayout;
        private ImageView linkPreviewImage;
        private ConstraintLayout linkPreviewLayout;

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
            linkTitle = itemView.findViewById(R.id.linkTitle);
            linkDescription = itemView.findViewById(R.id.linkPreviewDescription);
            linkSource = itemView.findViewById(R.id.linkSourceDomain);
            linkPreviewImage = itemView.findViewById(R.id.linkPreviewImage);
            linkPreviewLayout = itemView.findViewById(R.id.linkPreview);
        }

        void bindChat(ChatModel chatModel) {
            long timeStampRight = chatModel.getTimestamp();

            if (chatModel.getUpvoteCount() > 0) {
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));

                if (chatModel.getupvoters().size() > 0)
                    itemView.findViewById(R.id.heartImage).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.heartImage).setVisibility(View.GONE);

                if (chatModel.getEmoji1().size() > 0)
                    itemView.findViewById(R.id.emoji1).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji1).setVisibility(View.GONE);

                if (chatModel.getEmoji2().size() > 0)
                    itemView.findViewById(R.id.emoji2).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji2).setVisibility(View.GONE);

                if (chatModel.getEmoji3().size() > 0)
                    itemView.findViewById(R.id.emoji3).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji3).setVisibility(View.GONE);

                if (chatModel.getEmoji4().size() > 0)
                    itemView.findViewById(R.id.emoji4).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji4).setVisibility(View.GONE);
            } else
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);

            messageText.setText(chatModel.getMessage());
            messageText.setMovementMethod(BetterLinkMovementMethod.getInstance());
            messageText.setLinkTextColor(Color.parseColor("#1111b7"));
            BetterLinkMovementMethod
                    .linkify(Linkify.WEB_URLS, (Activity) mContext)
                    .setOnLinkLongClickListener(((textView, url) -> {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("link", url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
                        return true;
                    }))
                    .setOnLinkClickListener((textView, url) -> {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(mContext, Uri.parse(url));
                        return true;
                    });

            messageTime.setText(sfd.format(new Date(timeStampRight)));
            messageTime2.setText(sfd.format(new Date(timeStampRight)));
            upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));

            /*if(chatModel.getSpamCount() > 0){
                  chatRightViewHolder.reportLayout.setVisibility(View.VISIBLE);
                  chatRightViewHolder.spamCount.setText(String.valueOf(chatModel.getSpamCount()));
            }*/
            if (chatModel.isEdited()) {
                edited.setVisibility(View.VISIBLE);
                messageTime2.setVisibility(View.VISIBLE);
                messageTime.setVisibility(View.GONE);
            } else {
                edited.setVisibility(View.GONE);
                if (chatModel.getMessage().length() <= 25) {
                    messageTime.setVisibility(View.VISIBLE);
                    messageTime2.setVisibility(View.GONE);
                } else {
                    messageTime2.setVisibility(View.VISIBLE);
                    messageTime.setVisibility(View.GONE);
                }
            }
        }
    }

    public class ChatRightLinkViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, messageTime2, upvoteCount, edited, spamCount, linkTitle, linkDescription, linkSource;
        private LinearLayout reportLayout;
        private ImageView linkPreviewImage;
        private ConstraintLayout linkPreviewLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightLinkViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            reportLayout = itemView.findViewById(R.id.flag_layout);
            spamCount = itemView.findViewById(R.id.spamCount_textView);
            linkTitle = itemView.findViewById(R.id.linkTitle);
            linkDescription = itemView.findViewById(R.id.linkPreviewDescription);
            linkSource = itemView.findViewById(R.id.linkSourceDomain);
            linkPreviewImage = itemView.findViewById(R.id.linkPreviewImage);
            linkPreviewLayout = itemView.findViewById(R.id.linkPreview);
        }

        void bindChat(ChatModel chatModel) {
            long timeStampRight = chatModel.getTimestamp();

            if (chatModel.getLinkPreview() == 1) {
                linkPreviewLayout.setVisibility(View.VISIBLE);
                if (!chatModel.getLinkPreviewImageUrl().equals(""))
                    Picasso.get().load(chatModel.getLinkPreviewImageUrl()).noFade().into(linkPreviewImage);
                linkTitle.setText(chatModel.getLinkPreviewTitle());
                linkDescription.setText(chatModel.getLinkPreviewDesc());
                linkSource.setText(chatModel.getLinkPreviewUrl());
            }

            if (chatModel.getUpvoteCount() > 0) {
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));

                if (chatModel.getupvoters().size() > 0)
                    itemView.findViewById(R.id.heartImage).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.heartImage).setVisibility(View.GONE);

                if (chatModel.getEmoji1().size() > 0)
                    itemView.findViewById(R.id.emoji1).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji1).setVisibility(View.GONE);

                if (chatModel.getEmoji2().size() > 0)
                    itemView.findViewById(R.id.emoji2).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji2).setVisibility(View.GONE);

                if (chatModel.getEmoji3().size() > 0)
                    itemView.findViewById(R.id.emoji3).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji3).setVisibility(View.GONE);

                if (chatModel.getEmoji4().size() > 0)
                    itemView.findViewById(R.id.emoji4).setVisibility(View.VISIBLE);
                else
                    itemView.findViewById(R.id.emoji4).setVisibility(View.GONE);
            } else
                itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);

            messageText.setText(chatModel.getMessage());
            messageText.setMovementMethod(BetterLinkMovementMethod.getInstance());
            messageText.setLinkTextColor(Color.parseColor("#343493"));
            BetterLinkMovementMethod
                    .linkify(Linkify.WEB_URLS, (Activity) mContext)
                    .setOnLinkLongClickListener(((textView, url) -> {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("link", url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "Link copied to clipboard.", Toast.LENGTH_SHORT).show();
                        return true;
                    }))
                    .setOnLinkClickListener((textView, url) -> {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(mContext, Uri.parse(url));
                        return true;
                    });

            messageTime.setText(sfd.format(new Date(timeStampRight)));
            messageTime2.setText(sfd.format(new Date(timeStampRight)));
            upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));

            /*if(chatModel.getSpamCount() > 0){
                  chatRightViewHolder.reportLayout.setVisibility(View.VISIBLE);
                  chatRightViewHolder.spamCount.setText(String.valueOf(chatModel.getSpamCount()));
            }*/
            if (chatModel.isEdited()) {
                edited.setVisibility(View.VISIBLE);
                messageTime2.setVisibility(View.VISIBLE);
                messageTime.setVisibility(View.GONE);
            } else {
                edited.setVisibility(View.GONE);
                if (chatModel.getMessage().length() <= 25) {
                    messageTime.setVisibility(View.VISIBLE);
                    messageTime2.setVisibility(View.GONE);
                } else {
                    messageTime2.setVisibility(View.VISIBLE);
                    messageTime.setVisibility(View.GONE);
                }
            }
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

        void bindChat(ChatModel chatModel) {
            long timeStampDeletedLeft = chatModel.getTimestamp();
            senderName.setText(chatModel.getUserName());
            messageTime.setText(sfd.format(new Date(timeStampDeletedLeft)));
            messageTime2.setText(sfd.format(new Date(timeStampDeletedLeft)));
            if (senderName.getVisibility() == View.VISIBLE) {
                messageTime.setVisibility(View.VISIBLE);
                messageTime2.setVisibility(View.GONE);
            } else {
                messageTime.setVisibility(View.GONE);
                messageTime2.setVisibility(View.VISIBLE);
            }
        }
    }

}

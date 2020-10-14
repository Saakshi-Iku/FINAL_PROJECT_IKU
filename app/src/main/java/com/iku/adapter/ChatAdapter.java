package com.iku.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
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
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatModel, RecyclerView.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_LEFT_COMMENT = 16;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_RIGHT_COMMENT = 17;
    public static final int MSG_TYPE_LEFT_LINK = 6;
    public static final int MSG_TYPE_RIGHT_LINK = 7;
    public static final int MSG_TYPE_IMAGE_LEFT = 2;
    public static final int MSG_TYPE_IMAGE_RIGHT = 3;
    public static final int MSG_TYPE_DELETED_LEFT = 4;
    public static final int MSG_TYPE_DELETED_RIGHT = 5;
    public static final int MSG_TYPE_LEFT_SPAM = 8;
    public static final int MSG_TYPE_RIGHT_SPAM = 9;
    public static final int MSG_TYPE_IMAGE_LEFT_SPAM = 10;
    public static final int MSG_TYPE_IMAGE_RIGHT_SPAM = 11;
    public static final int MSG_TYPE_LEFT_LINK_SPAM = 12;
    public static final int MSG_TYPE_RIGHT_LINK_SPAM = 13;
    public static final int MSG_TYPE_IMAGE_LEFT_COMMENT = 14;
    public static final int MSG_TYPE_IMAGE_RIGHT_COMMENT = 15;

    private static final String TAG = ChatAdapter.class.getSimpleName();
    private final Context mContext;
    private final SimpleDateFormat sfd = new SimpleDateFormat("hh:mm a", Locale.US);
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ChatAdapter.OnItemClickListener listener;

    public ChatAdapter(Context context, @NonNull FirestoreRecyclerOptions<ChatModel> options) {
        super(options);
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i, @NonNull final ChatModel chatModel) {

        switch (viewHolder.getItemViewType()) {

            case MSG_TYPE_DELETED_RIGHT:
                ChatRightDeletedViewHolder chatRightDeletedViewHolder = (ChatRightDeletedViewHolder) viewHolder;
                chatRightDeletedViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_DELETED_LEFT:
                ChatLeftDeletedViewHolder chatLeftDeletedViewHolder = (ChatLeftDeletedViewHolder) viewHolder;
                chatLeftDeletedViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_LEFT:
                ChatLeftViewHolder chatLeftViewHolder = (ChatLeftViewHolder) viewHolder;
                chatLeftViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_LEFT_COMMENT:
                ChatLeftCommentViewHolder chatLeftCommentViewHolder = (ChatLeftCommentViewHolder) viewHolder;
                chatLeftCommentViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_LEFT_LINK:
                ChatLeftLinkViewHolder chatLeftLinkViewHolder = (ChatLeftLinkViewHolder) viewHolder;
                chatLeftLinkViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_RIGHT:
                ChatRightViewHolder chatRightViewHolder = (ChatRightViewHolder) viewHolder;
                chatRightViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_RIGHT_COMMENT:
                ChatRightCommentViewHolder chatRightCommentViewHolder = (ChatRightCommentViewHolder) viewHolder;
                chatRightCommentViewHolder.bindChat(chatModel);
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

            case MSG_TYPE_IMAGE_LEFT_COMMENT:
                final ChatLeftImageCommentViewHolder chatLeftImageCommentViewHolder = (ChatLeftImageCommentViewHolder) viewHolder;
                chatLeftImageCommentViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_IMAGE_RIGHT_COMMENT:
                final ChatRightCommentImageViewHolder chatRightCommentImageViewHolder = (ChatRightCommentImageViewHolder) viewHolder;
                chatRightCommentImageViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_LEFT_SPAM:
                ChatLeftSpamViewHolder chatLeftSpamViewHolder = (ChatLeftSpamViewHolder) viewHolder;
                chatLeftSpamViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_RIGHT_SPAM:
                ChatRightSpamViewHolder chatRightSpamViewHolder = (ChatRightSpamViewHolder) viewHolder;
                chatRightSpamViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_IMAGE_LEFT_SPAM:
                ChatLeftImageSpamViewHolder chatLeftImageSpamViewHolder = (ChatLeftImageSpamViewHolder) viewHolder;
                chatLeftImageSpamViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_IMAGE_RIGHT_SPAM:
                ChatRightImageSpamViewHolder chatRightImageSpamViewHolder = (ChatRightImageSpamViewHolder) viewHolder;
                chatRightImageSpamViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_LEFT_LINK_SPAM:
                ChatLeftLinkSpamViewHolder chatLeftLinkSpamViewHolder = (ChatLeftLinkSpamViewHolder) viewHolder;
                chatLeftLinkSpamViewHolder.bindChat(chatModel);
                break;

            case MSG_TYPE_RIGHT_LINK_SPAM:
                ChatRightLinkSpamViewHolder chatRightLinkSpamViewHolder = (ChatRightLinkSpamViewHolder) viewHolder;
                chatRightLinkSpamViewHolder.bindChat(chatModel);
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
        } else if (viewType == MSG_TYPE_RIGHT_COMMENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
            return new ChatRightCommentViewHolder(view);
        } else if (viewType == MSG_TYPE_LEFT_COMMENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);
            return new ChatLeftCommentViewHolder(view);
        } else if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);
            return new ChatLeftViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left_image, parent, false);
            return new ChatLeftImageViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right_image, parent, false);
            return new ChatRightImageViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_LEFT_COMMENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left_image, parent, false);
            return new ChatLeftImageCommentViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_RIGHT_COMMENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right_image, parent, false);
            return new ChatRightCommentImageViewHolder(view);
        } else if (viewType == MSG_TYPE_LEFT_SPAM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);
            return new ChatLeftSpamViewHolder(view);
        } else if (viewType == MSG_TYPE_RIGHT_SPAM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
            return new ChatRightSpamViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_LEFT_SPAM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left_image, parent, false);
            return new ChatLeftImageSpamViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_RIGHT_SPAM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right_image, parent, false);
            return new ChatRightImageSpamViewHolder(view);
        } else if (viewType == MSG_TYPE_LEFT_LINK_SPAM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);
            return new ChatLeftLinkSpamViewHolder(view);
        } else if (viewType == MSG_TYPE_RIGHT_LINK_SPAM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
            return new ChatRightLinkSpamViewHolder(view);
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
        if (getItem(position).isSpam() || getItem(position).isDeleted()) {
            if (getItem(position).getUID().equals(user.getUid()))
                return MSG_TYPE_DELETED_RIGHT;
            else
                return MSG_TYPE_DELETED_LEFT;
        } else {
            SharedPreferences pref = mContext.getSharedPreferences("iku_earth", Context.MODE_PRIVATE);
            boolean isAdmin = pref.getBoolean("isAdmin", false);
            if (isAdmin && getItem(position).getSpamCount() > 0) {
                if (getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text") && getItem(position).getLinkPreview() == 0)
                    return MSG_TYPE_RIGHT_SPAM;
                else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text") && getItem(position).getLinkPreview() == 0)
                    return MSG_TYPE_LEFT_SPAM;
                else if (getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text") && getItem(position).getLinkPreview() == 1)
                    return MSG_TYPE_RIGHT_LINK_SPAM;
                else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text") && getItem(position).getLinkPreview() == 1)
                    return MSG_TYPE_LEFT_LINK_SPAM;
                else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("image"))
                    return MSG_TYPE_IMAGE_LEFT_SPAM;
                else if (getItem(position).getType().equals("image") && getItem(position).getimageUrl() != null && getItem(position).getUID().equals(user.getUid()))
                    return MSG_TYPE_IMAGE_RIGHT_SPAM;
                else
                    return 0;
            } else if (getItem(position).getPostCommentCount() > 0) {
                if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("image"))
                    return MSG_TYPE_IMAGE_LEFT_COMMENT;
                else if (getItem(position).getimageUrl() != null && getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("image"))
                    return MSG_TYPE_IMAGE_RIGHT_COMMENT;
                else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text"))
                    return MSG_TYPE_LEFT_COMMENT;
                else if (getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text"))
                    return MSG_TYPE_RIGHT_COMMENT;
                else
                    return 0;
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
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);

        void onTopCommentClick(DocumentSnapshot documentSnapshot, int position);
    }

    public class ChatLeftViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageTime3;
        private final MaterialTextView senderName;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;

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
        }

        void bindChat(ChatModel chatModel) {
            long timeStampLeft = chatModel.getTimestamp();

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

    public class ChatLeftCommentViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageTime3;
        private final MaterialTextView senderName;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView commentTextView;
        private final ConstraintLayout commentsLayout;
        private final CircleImageView commenterProfilePicture;

        @SuppressLint("ClickableViewAccessibility")
        public ChatLeftCommentViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            messageTime3 = itemView.findViewById(R.id.message_time3);
            senderName = itemView.findViewById(R.id.sender_name);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            commentsLayout = itemView.findViewById(R.id.commentsPreview);
            commenterProfilePicture = itemView.findViewById(R.id.profileImage);
            commentTextView = itemView.findViewById(R.id.comment);

            commentsLayout.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTopCommentClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }

        void bindChat(ChatModel chatModel) {
            long timeStampLeft = chatModel.getTimestamp();

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

            commentsLayout.setVisibility(View.VISIBLE);
            commentTextView.setText(chatModel.getTopComment());
            String url = chatModel.getTopCommenterImageUrl();
            String firstLetter, secondLetter;
            if (url != null && !url.equals("null")) {
                Picasso.get().load(url).noFade().networkPolicy(NetworkPolicy.OFFLINE)
                        .into(commenterProfilePicture, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(url).noFade().into(commenterProfilePicture);
                            }
                        });
            } else {
                if (chatModel.getTopCommenterName() != null) {
                    firstLetter = String.valueOf(chatModel.getTopCommenterName().charAt(0));
                    secondLetter = chatModel.getTopCommenterName().substring(chatModel.getTopCommenterName().indexOf(' ') + 1, chatModel.getTopCommenterName().indexOf(' ') + 2).trim();
                    TextDrawable drawable = TextDrawable.builder()
                            .beginConfig()
                            .width(200)
                            .height(200)
                            .endConfig()
                            .buildRect(firstLetter + secondLetter, Color.DKGRAY);
                    commenterProfilePicture.setImageDrawable(drawable);
                }
            }

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

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageTime3;
        private final MaterialTextView senderName;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView linkTitle;
        private final MaterialTextView linkDescription;
        private final MaterialTextView linkSource;
        private final ImageView linkPreviewImage;
        private final ConstraintLayout linkPreviewLayout;

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
                if (!chatModel.getLinkPreviewImageUrl().equals(""))
                    Picasso.get().load(chatModel.getLinkPreviewImageUrl()).noFade().into(linkPreviewImage);
                else
                    linkPreviewImage.setVisibility(View.GONE);
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

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageTime3;
        private final MaterialTextView senderName;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final ImageView receiverImage;
        private final MaterialButton viewPostBtn;
        private final ProgressBar leftProgress;

        @SuppressLint("ClickableViewAccessibility")
        public ChatLeftImageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            messageTime3 = itemView.findViewById(R.id.message_time3);
            senderName = itemView.findViewById(R.id.sender_name);
            receiverImage = itemView.findViewById(R.id.receivedImage);
            leftProgress = itemView.findViewById(R.id.chatLeftProgress);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            viewPostBtn = itemView.findViewById(R.id.viewPostButton);

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
            messageTime.setText(sfd.format(new Date(timeStampImageLeft)));
            messageTime2.setText(sfd.format(new Date(timeStampImageLeft)));
            messageTime3.setText(sfd.format(new Date(timeStampImageLeft)));
            senderName.setText(chatModel.getUserName());

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
                            receiverImage.setVisibility(View.VISIBLE);
                            leftProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(chatModel.getimageUrl())
                                    .noFade()
                                    .into(receiverImage);
                            receiverImage.setVisibility(View.VISIBLE);
                            leftProgress.setVisibility(View.GONE);
                        }
                    });

        }

    }

    public class ChatLeftImageCommentViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageTime3;
        private final MaterialTextView senderName;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView commentTextView;
        private final ImageView receiverImage;
        private final MaterialButton viewPostBtn;
        private final ConstraintLayout commentsLayout;
        private final CircleImageView commenterProfilePicture;
        private final ProgressBar leftProgress;

        @SuppressLint("ClickableViewAccessibility")
        public ChatLeftImageCommentViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            messageTime3 = itemView.findViewById(R.id.message_time3);
            senderName = itemView.findViewById(R.id.sender_name);
            receiverImage = itemView.findViewById(R.id.receivedImage);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            leftProgress = itemView.findViewById(R.id.chatLeftProgress);
            viewPostBtn = itemView.findViewById(R.id.viewPostButton);
            commentsLayout = itemView.findViewById(R.id.commentsPreview);
            commenterProfilePicture = itemView.findViewById(R.id.profileImage);
            commentTextView = itemView.findViewById(R.id.comment);

            viewPostBtn.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });

            commentsLayout.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTopCommentClick(getSnapshots().getSnapshot(position), position);
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

            commentsLayout.setVisibility(View.VISIBLE);
            commentTextView.setText(chatModel.getTopComment());
            String url = chatModel.getTopCommenterImageUrl();
            String firstLetter, secondLetter;
            if (url != null && !url.equals("null")) {
                Picasso.get().load(url).noFade().networkPolicy(NetworkPolicy.OFFLINE)
                        .into(commenterProfilePicture, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(url).noFade().into(commenterProfilePicture);
                            }
                        });
            } else {
                if (chatModel.getTopCommenterName() != null) {
                    firstLetter = String.valueOf(chatModel.getTopCommenterName().charAt(0));
                    secondLetter = chatModel.getTopCommenterName().substring(chatModel.getTopCommenterName().indexOf(' ') + 1, chatModel.getTopCommenterName().indexOf(' ') + 2).trim();
                    TextDrawable drawable = TextDrawable.builder()
                            .beginConfig()
                            .width(200)
                            .height(200)
                            .endConfig()
                            .buildRect(firstLetter + secondLetter, Color.DKGRAY);
                    commenterProfilePicture.setImageDrawable(drawable);
                }
            }

            messageTime.setText(sfd.format(new Date(timeStampImageLeft)));
            messageTime2.setText(sfd.format(new Date(timeStampImageLeft)));
            messageTime3.setText(sfd.format(new Date(timeStampImageLeft)));
            senderName.setText(chatModel.getUserName());

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
                            receiverImage.setVisibility(View.VISIBLE);
                            leftProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(chatModel.getimageUrl())
                                    .noFade()
                                    .placeholder(R.drawable.progress_animation)
                                    .into(receiverImage);
                            receiverImage.setVisibility(View.VISIBLE);
                            leftProgress.setVisibility(View.GONE);
                        }
                    });

        }

    }

    public class ChatRightViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
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

    public class ChatRightCommentViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView commentTextView;
        private final ConstraintLayout commentsLayout;
        private final CircleImageView commenterProfilePicture;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightCommentViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            commentsLayout = itemView.findViewById(R.id.commentsPreview);
            commenterProfilePicture = itemView.findViewById(R.id.profileImage);
            commentTextView = itemView.findViewById(R.id.comment);

            commentsLayout.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTopCommentClick(getSnapshots().getSnapshot(position), position);
                }
            });
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

            if (chatModel.getTopComment() != null) {
                commentsLayout.setVisibility(View.VISIBLE);
                commentTextView.setText(chatModel.getTopComment());
                String url = chatModel.getTopCommenterImageUrl();
                String firstLetter, secondLetter;
                if (url != null && !url.equals("null")) {
                    Picasso.get().load(url).noFade().networkPolicy(NetworkPolicy.OFFLINE)
                            .into(commenterProfilePicture, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(url).noFade().into(commenterProfilePicture);
                                }
                            });
                } else {
                    if (chatModel.getTopCommenterName() != null) {
                        firstLetter = String.valueOf(chatModel.getTopCommenterName().charAt(0));
                        secondLetter = chatModel.getTopCommenterName().substring(chatModel.getTopCommenterName().indexOf(' ') + 1, chatModel.getTopCommenterName().indexOf(' ') + 2).trim();
                        TextDrawable drawable = TextDrawable.builder()
                                .beginConfig()
                                .width(200)
                                .height(200)
                                .endConfig()
                                .buildRect(firstLetter + secondLetter, Color.DKGRAY);
                        commenterProfilePicture.setImageDrawable(drawable);
                    }
                }
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

            messageTime.setText(sfd.format(new Date(timeStampRight)));
            messageTime2.setText(sfd.format(new Date(timeStampRight)));
            upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));

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

    public class ChatRightImageViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final ImageView sentImage;
        private final MaterialButton viewPostBtn;
        private final ProgressBar rightProgress;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightImageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            sentImage = itemView.findViewById(R.id.sentImage);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            rightProgress = itemView.findViewById(R.id.chatRightProgress);
            viewPostBtn = itemView.findViewById(R.id.viewPostButton);

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
                            sentImage.setVisibility(View.VISIBLE);
                            rightProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(chatModel.getimageUrl())
                                    .noFade()
                                    .placeholder(R.drawable.progress_animation)
                                    .into(sentImage);
                            sentImage.setVisibility(View.VISIBLE);
                            rightProgress.setVisibility(View.GONE);
                        }
                    });
        }
    }

    public class ChatRightCommentImageViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView commentTextView;
        private final ImageView sentImage;
        private final MaterialButton viewPostBtn;
        private final ConstraintLayout commentsLayout;
        private final CircleImageView commenterProfilePicture;
        private final ProgressBar rightProgress;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightCommentImageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            sentImage = itemView.findViewById(R.id.sentImage);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            viewPostBtn = itemView.findViewById(R.id.viewPostButton);
            commentsLayout = itemView.findViewById(R.id.commentsPreview);
            commenterProfilePicture = itemView.findViewById(R.id.profileImage);
            rightProgress = itemView.findViewById(R.id.chatRightProgress);
            commentTextView = itemView.findViewById(R.id.comment);

            viewPostBtn.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
            commentsLayout.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTopCommentClick(getSnapshots().getSnapshot(position), position);
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

            if (chatModel.getTopComment() != null) {
                commentsLayout.setVisibility(View.VISIBLE);
                commentTextView.setText(chatModel.getTopComment());
                String url = chatModel.getTopCommenterImageUrl();
                String firstLetter, secondLetter;
                if (url != null && !url.equals("null")) {
                    Picasso.get().load(url).noFade().networkPolicy(NetworkPolicy.OFFLINE)
                            .into(commenterProfilePicture, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(url).noFade().into(commenterProfilePicture);
                                }
                            });
                } else {
                    if (chatModel.getTopCommenterName() != null) {
                        firstLetter = String.valueOf(chatModel.getTopCommenterName().charAt(0));
                        secondLetter = chatModel.getTopCommenterName().substring(chatModel.getTopCommenterName().indexOf(' ') + 1, chatModel.getTopCommenterName().indexOf(' ') + 2).trim();
                        TextDrawable drawable = TextDrawable.builder()
                                .beginConfig()
                                .width(200)
                                .height(200)
                                .endConfig()
                                .buildRect(firstLetter + secondLetter, Color.DKGRAY);
                        commenterProfilePicture.setImageDrawable(drawable);
                    }
                }
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
                            sentImage.setVisibility(View.VISIBLE);
                            rightProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(chatModel.getimageUrl())
                                    .noFade()
                                    .placeholder(R.drawable.progress_animation)
                                    .into(sentImage);
                            sentImage.setVisibility(View.VISIBLE);
                            rightProgress.setVisibility(View.GONE);
                        }
                    });
        }
    }

    public class ChatRightLinkViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView linkTitle;
        private final MaterialTextView linkDescription;
        private final MaterialTextView linkSource;
        private final ImageView linkPreviewImage;
        private final ConstraintLayout linkPreviewLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightLinkViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
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
                else
                    linkPreviewImage.setVisibility(View.GONE);
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

    public class ChatLeftSpamViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageTime3;
        private final MaterialTextView senderName;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView spamCount;
        private final LinearLayout reportLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatLeftSpamViewHolder(@NonNull View itemView) {
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
        }

        void bindChat(ChatModel chatModel) {
            long timeStampLeft = chatModel.getTimestamp();

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

            reportLayout.setVisibility(View.VISIBLE);
            spamCount.setText(String.valueOf(chatModel.getSpamCount()));


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

    public class ChatLeftLinkSpamViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageTime3;
        private final MaterialTextView senderName;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView spamCount;
        private final MaterialTextView linkTitle;
        private final MaterialTextView linkDescription;
        private final MaterialTextView linkSource;
        private final LinearLayout reportLayout;
        private final ImageView linkPreviewImage;
        private final ConstraintLayout linkPreviewLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatLeftLinkSpamViewHolder(@NonNull View itemView) {
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
                if (!chatModel.getLinkPreviewImageUrl().equals(""))
                    Picasso.get().load(chatModel.getLinkPreviewImageUrl()).noFade().into(linkPreviewImage);
                else
                    linkPreviewImage.setVisibility(View.GONE);
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

            reportLayout.setVisibility(View.VISIBLE);
            spamCount.setText(String.valueOf(chatModel.getSpamCount()));

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

    public class ChatLeftImageSpamViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageTime3;
        private final MaterialTextView senderName;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView spamCount;
        private final MaterialTextView commentTextView;
        private final ImageView receiverImage;
        private final MaterialButton viewPostBtn;
        private final LinearLayout reportLayout;
        private final ConstraintLayout commentsLayout;
        private final CircleImageView commenterProfilePicture;
        private final ProgressBar leftProgress;

        @SuppressLint("ClickableViewAccessibility")
        public ChatLeftImageSpamViewHolder(@NonNull View itemView) {
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
            leftProgress = itemView.findViewById(R.id.chatLeftProgress);
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

            if (chatModel.getTopComment() != null) {
                commentsLayout.setVisibility(View.VISIBLE);
                commentTextView.setText(chatModel.getTopComment());
                String url = chatModel.getTopCommenterImageUrl();
                String firstLetter, secondLetter;
                if (url != null && !url.equals("null")) {
                    Picasso.get().load(url).noFade().networkPolicy(NetworkPolicy.OFFLINE)
                            .into(commenterProfilePicture, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(url).noFade().into(commenterProfilePicture);
                                }
                            });
                } else {
                    if (chatModel.getTopCommenterName() != null) {
                        firstLetter = String.valueOf(chatModel.getTopCommenterName().charAt(0));
                        secondLetter = chatModel.getTopCommenterName().substring(chatModel.getTopCommenterName().indexOf(' ') + 1, chatModel.getTopCommenterName().indexOf(' ') + 2).trim();
                        TextDrawable drawable = TextDrawable.builder()
                                .beginConfig()
                                .width(200)
                                .height(200)
                                .endConfig()
                                .buildRect(firstLetter + secondLetter, Color.DKGRAY);
                        commenterProfilePicture.setImageDrawable(drawable);
                    }
                }
            }

            messageTime.setText(sfd.format(new Date(timeStampImageLeft)));
            messageTime2.setText(sfd.format(new Date(timeStampImageLeft)));
            messageTime3.setText(sfd.format(new Date(timeStampImageLeft)));
            senderName.setText(chatModel.getUserName());

            reportLayout.setVisibility(View.VISIBLE);
            spamCount.setText(String.valueOf(chatModel.getSpamCount()));

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
                            receiverImage.setVisibility(View.VISIBLE);
                            leftProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(chatModel.getimageUrl())
                                    .noFade()
                                    .placeholder(R.drawable.progress_animation)
                                    .into(receiverImage);
                            receiverImage.setVisibility(View.VISIBLE);
                            leftProgress.setVisibility(View.GONE);
                        }
                    });

        }

    }

    public class ChatRightImageSpamViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageTime3;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView spamCount;
        private final MaterialTextView commentTextView;
        private final ImageView sentImage;
        private final MaterialButton viewPostBtn;
        private final LinearLayout reportLayout;
        private final ConstraintLayout commentsLayout;
        private final CircleImageView commenterProfilePicture;
        private final ProgressBar rightProgress;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightImageSpamViewHolder(@NonNull View itemView) {
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
            rightProgress = itemView.findViewById(R.id.chatRightProgress);

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

            reportLayout.setVisibility(View.VISIBLE);
            spamCount.setText(String.valueOf(chatModel.getSpamCount()));

            if (chatModel.getTopComment() != null) {
                commentsLayout.setVisibility(View.VISIBLE);
                commentTextView.setText(chatModel.getTopComment());
                String url = chatModel.getTopCommenterImageUrl();
                String firstLetter, secondLetter;
                if (url != null && !url.equals("null")) {
                    Picasso.get().load(url).noFade().networkPolicy(NetworkPolicy.OFFLINE)
                            .into(commenterProfilePicture, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(url).noFade().into(commenterProfilePicture);
                                }
                            });
                } else {
                    if (chatModel.getTopCommenterName() != null) {
                        firstLetter = String.valueOf(chatModel.getTopCommenterName().charAt(0));
                        secondLetter = chatModel.getTopCommenterName().substring(chatModel.getTopCommenterName().indexOf(' ') + 1, chatModel.getTopCommenterName().indexOf(' ') + 2).trim();
                        TextDrawable drawable = TextDrawable.builder()
                                .beginConfig()
                                .width(200)
                                .height(200)
                                .endConfig()
                                .buildRect(firstLetter + secondLetter, Color.DKGRAY);
                        commenterProfilePicture.setImageDrawable(drawable);
                    }
                }
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
                            sentImage.setVisibility(View.VISIBLE);
                            rightProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(chatModel.getimageUrl())
                                    .noFade()
                                    .placeholder(R.drawable.progress_animation)
                                    .into(sentImage);
                            sentImage.setVisibility(View.VISIBLE);
                            rightProgress.setVisibility(View.GONE);
                        }
                    });
        }
    }

    public class ChatRightSpamViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView spamCount;
        private final LinearLayout reportLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightSpamViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            edited = itemView.findViewById(R.id.editFlag);
            reportLayout = itemView.findViewById(R.id.flag_layout);
            spamCount = itemView.findViewById(R.id.spamCount_textView);
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

            reportLayout.setVisibility(View.VISIBLE);
            spamCount.setText(String.valueOf(chatModel.getSpamCount()));

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

    public class ChatRightLinkSpamViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView messageText;
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView upvoteCount;
        private final MaterialTextView edited;
        private final MaterialTextView spamCount;
        private final MaterialTextView linkTitle;
        private final MaterialTextView linkDescription;
        private final MaterialTextView linkSource;
        private final LinearLayout reportLayout;
        private final ImageView linkPreviewImage;
        private final ConstraintLayout linkPreviewLayout;

        @SuppressLint("ClickableViewAccessibility")
        public ChatRightLinkSpamViewHolder(@NonNull View itemView) {
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
                else
                    linkPreviewImage.setVisibility(View.GONE);
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

            reportLayout.setVisibility(View.VISIBLE);
            spamCount.setText(String.valueOf(chatModel.getSpamCount()));

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
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageText;

        public ChatRightDeletedViewHolder(View itemView) {
            super(itemView);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            messageText = itemView.findViewById(R.id.message);
        }

        void bindChat(ChatModel chatModel) {

            long timeStampDeletedRight = chatModel.getTimestamp();
            messageTime.setText(sfd.format(new Date(timeStampDeletedRight)));
            messageTime2.setText(sfd.format(new Date(timeStampDeletedRight)));

            if (chatModel.getDeletedBy().equals("admin"))
                messageText.setText(R.string.reported_spam_and_deleted);
            else
                messageText.setText(R.string.message_deleted);

            if (messageText.getText().length() <= 25) {
                messageTime.setVisibility(View.VISIBLE);
                messageTime2.setVisibility(View.GONE);
            } else {
                messageTime2.setVisibility(View.VISIBLE);
                messageTime.setVisibility(View.GONE);
            }
        }
    }

    private class ChatLeftDeletedViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView messageTime;
        private final MaterialTextView messageTime2;
        private final MaterialTextView messageTime3;
        private final MaterialTextView senderName;
        private final MaterialTextView messageText;

        public ChatLeftDeletedViewHolder(View itemView) {
            super(itemView);
            messageTime = itemView.findViewById(R.id.message_time);
            messageTime2 = itemView.findViewById(R.id.message_time2);
            messageTime3 = itemView.findViewById(R.id.message_time3);
            senderName = itemView.findViewById(R.id.sender_name);
            messageText = itemView.findViewById(R.id.message);
        }

        void bindChat(ChatModel chatModel) {
            long timeStampDeletedLeft = chatModel.getTimestamp();
            senderName.setText(chatModel.getUserName());
            messageTime.setText(sfd.format(new Date(timeStampDeletedLeft)));
            messageTime2.setText(sfd.format(new Date(timeStampDeletedLeft)));
            messageTime3.setText(sfd.format(new Date(timeStampDeletedLeft)));

            if (chatModel.getDeletedBy().equals("admin"))
                messageText.setText(R.string.reported_spam_and_deleted);
            else
                messageText.setText(R.string.message_deleted);

            if (senderName.getVisibility() == View.VISIBLE) {
                messageTime.setVisibility(View.VISIBLE);
                messageTime2.setVisibility(View.GONE);
                messageTime3.setVisibility(View.GONE);
            } else {
                if (messageText.getText().length() <= 25) {
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
    }

}

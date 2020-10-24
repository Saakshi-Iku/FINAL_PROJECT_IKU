package com.iku.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iku.R;
import com.iku.app.AppConfig;
import com.iku.models.CommentModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CommentAdapter extends FirestoreRecyclerAdapter<CommentModel, CommentAdapter.CommentViewHolder> {

    private static final String TAG = CommentAdapter.class.getSimpleName();
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private CommentAdapter.OnItemClickListener mListener;

    public CommentAdapter(@NonNull FirestoreRecyclerOptions<CommentModel> options) {
        super(options);
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        long day = TimeUnit.MILLISECONDS.toDays(diff);
        if (diff < MINUTE_MILLIS) {
            return "Just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "Moments ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " mins";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1h";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + "h";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Yesterday";
        } else if (day >= 7) {
            if (day > 360) {
                return (day / 360) + "y";
            } else if (day > 30) {
                return (day / 30) + "m";
            } else {
                return diff / DAY_MILLIS + "d";
            }
        } else {
            return diff / DAY_MILLIS + "d";
        }
    }

    public void setOnItemClickListener(CommentAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull CommentViewHolder commentViewHolder, int position, @NonNull CommentModel commentModel) {
        long timeStamp = commentModel.getTimestamp();
        if (commentModel.isSpam() || commentModel.isDeleted()) {
            commentViewHolder.commentTextView.setTypeface(commentViewHolder.commentTextView.getTypeface(), Typeface.ITALIC);
/*
            commentViewHolder.commentTextView.setTextColor(ContextCompat.getColor(mContext(R.color.deletedTextLight));
*/
            if (commentModel.getDeletedBy().equals("author"))
                commentViewHolder.commentTextView.setText(R.string.comment_deleted);
            else
                commentViewHolder.commentTextView.setText(R.string.reported_and_deleted_comment);
        } else {
            commentViewHolder.commentTextView.setTypeface(commentViewHolder.commentTextView.getTypeface(), Typeface.NORMAL);
            commentViewHolder.commentTextView.setText(commentModel.getComment());
        }
        commentViewHolder.commenterNameTextView.setText(commentModel.getCommenterName());
        commentViewHolder.timestampTextView.setText(getTimeAgo(timeStamp));
        commentViewHolder.commentHeartCountTextView.setText(String.valueOf(commentModel.getHeartsCount()));
        commentViewHolder.setImage(commentModel);
        ArrayList<String> heartsList = commentModel.getHeartsArray();
        if (commentModel.getHeartsCount() == 0) {
            commentViewHolder.heartImage.setImageResource(R.drawable.ic_heart_unfilled);
        }
        for (String element : heartsList) {
            if (element.contains(user.getUid())) {
                commentViewHolder.heartImage.setImageResource(R.drawable.ic_heart);
                break;
            } else
                commentViewHolder.heartImage.setImageResource(R.drawable.ic_heart_unfilled);
        }

        if(commentModel.isEdited()){
            commentViewHolder.editedTextView.setVisibility(View.VISIBLE);
        } else {
            commentViewHolder.editedTextView.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_data, parent, false);
        return new CommentAdapter.CommentViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(int pos, DocumentSnapshot snapshot);

        void onHeartClick(int pos, DocumentSnapshot snapshot);
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        private final TextView commentTextView;
        private final TextView commenterNameTextView;
        private final TextView timestampTextView;
        private final TextView commentHeartCountTextView;
        private final ImageView profileImageView;
        private final ImageView heartImage;
        private final TextView editedTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.comment);
            commenterNameTextView = itemView.findViewById(R.id.commentorName);
            profileImageView = itemView.findViewById(R.id.profileImage);
            timestampTextView = itemView.findViewById(R.id.timestamp);
            heartImage = itemView.findViewById(R.id.heartUpButton);
            commentHeartCountTextView = itemView.findViewById(R.id.commentHeartCount);
            editedTextView = itemView.findViewById(R.id.editFlag);

            commenterNameTextView.setOnClickListener(view -> {
                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position, getSnapshots().getSnapshot(position));
                    }
                }
            });
            profileImageView.setOnClickListener(view -> {
                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position, getSnapshots().getSnapshot(position));
                    }
                }
            });
            heartImage.setOnClickListener(view -> {
                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onHeartClick(position, getSnapshots().getSnapshot(position));
                    }
                }
            });
        }

        public void setImage(final CommentModel commentModel) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(AppConfig.USERS_COLLECTION).document(commentModel.getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String firstLetter, secondLetter;
                                    String url = (String) document.get("imageUrl");

                                    if (url != null) {
                                        Picasso.get()
                                                .load(url)
                                                .noFade()
                                                .networkPolicy(NetworkPolicy.OFFLINE)
                                                .into(profileImageView, new Callback() {

                                                    @Override
                                                    public void onSuccess() {
                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        Picasso.get()
                                                                .load(url)
                                                                .noFade()
                                                                .into(profileImageView);
                                                    }
                                                });
                                    } else {

                                        firstLetter = String.valueOf(commentModel.getCommenterName().charAt(0));
                                        secondLetter = commentModel.getCommenterName().substring(commentModel.getCommenterName().indexOf(' ') + 1, commentModel.getCommenterName().indexOf(' ') + 2).trim();

                                        TextDrawable drawable = TextDrawable.builder()
                                                .beginConfig()
                                                .width(200)
                                                .height(200)
                                                .endConfig()
                                                .buildRect(firstLetter + secondLetter, Color.DKGRAY);

                                        profileImageView.setImageDrawable(drawable);
                                    }
                                }
                            }
                        }
                    });
        }
    }
}
package com.iku.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iku.R;
import com.iku.models.CommentModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class CommentAdapter extends FirestorePagingAdapter<CommentModel, CommentAdapter.CommentViewHolder> {

    private FirebaseFirestore db;
    private static final String TAG = CommentAdapter.class.getSimpleName();

    public CommentAdapter(@NonNull FirestorePagingOptions<CommentModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CommentViewHolder commentViewHolder, int position, @NonNull CommentModel commentModel) {
        commentViewHolder.commentTextView.setText(commentModel.getComment());
        commentViewHolder.commenterNameTextView.setText(commentModel.getCommenterName());
        commentViewHolder.setImage(commentModel);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_data, parent, false);
        return new CommentAdapter.CommentViewHolder(view);
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        private TextView commentTextView,commenterNameTextView;
        private ImageView profileImageView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.comment);
            commenterNameTextView = itemView.findViewById(R.id.commenterName);
            profileImageView = itemView.findViewById(R.id.profileImage);
        }

        public void setImage(final CommentModel commentModel){
            db = FirebaseFirestore.getInstance();
            db.collection("users").document(commentModel.getUid()).get()
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
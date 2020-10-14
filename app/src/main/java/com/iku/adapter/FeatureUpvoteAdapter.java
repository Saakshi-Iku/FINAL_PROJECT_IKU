package com.iku.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.iku.R;
import com.iku.models.FeatureUpvoteModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class FeatureUpvoteAdapter extends FirestoreRecyclerAdapter<FeatureUpvoteModel, FeatureUpvoteAdapter.FeatureUpVoteViewHolder> {

    private final Context mContext;
    private OnItemClickListener mListener;

    public FeatureUpvoteAdapter(@NonNull FirestoreRecyclerOptions<FeatureUpvoteModel> options, Context mContext) {
        super(options);
        this.mContext = mContext;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull FeatureUpVoteViewHolder featureUpVoteViewHolder, int position, @NonNull FeatureUpvoteModel featureUpvoteModel) {
        featureUpVoteViewHolder.bindView(featureUpvoteModel);
    }

    @NonNull
    @Override
    public FeatureUpVoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upvote_data, parent, false);
        return new FeatureUpvoteAdapter.FeatureUpVoteViewHolder(view, mListener);
    }

    public interface OnItemClickListener {
        void onItemClick(int position, DocumentSnapshot snapshot);
    }

    public class FeatureUpVoteViewHolder extends RecyclerView.ViewHolder {

        private final TextView firstNameTextView;
        private final TextView pointsTextView;
        private final TextView descTextView;
        private final ImageView image;

        public FeatureUpVoteViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            Button upvoteFeatureButton = itemView.findViewById(R.id.button);
            firstNameTextView = itemView.findViewById(R.id.title);
            descTextView = itemView.findViewById(R.id.description);
            pointsTextView = itemView.findViewById(R.id.requestCount);
            image = itemView.findViewById(R.id.image);

            upvoteFeatureButton.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position, getSnapshots().getSnapshot(position));
                    }
                }
            });
        }

        void bindView(FeatureUpvoteModel featureUpvoteModel) {
            firstNameTextView.setText(featureUpvoteModel.getTitle());
            descTextView.setText(featureUpvoteModel.getDescription());
            pointsTextView.setText(mContext.getString(R.string.upvoted) + featureUpvoteModel.getUpvote_count());
            String[] p = featureUpvoteModel.getImage().split("/");
            String imageLink = "https://drive.google.com/uc?export=download&id=" + p[5];
            Picasso.get()
                    .load(imageLink)
                    .noFade()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(imageLink).noFade().into(image);
                        }
                    });
        }
    }
}
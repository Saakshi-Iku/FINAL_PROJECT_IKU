package com.iku.adapter;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iku.R;
import com.iku.models.UserModel;

public class LeaderBoardAdapter extends FirestorePagingAdapter<UserModel, RecyclerView.ViewHolder> {

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private LeaderBoardAdapter.OnItemClickOfSameUidListener listener;
    private LeaderBoardAdapter.OnItemClickOfDiffUidListener listenerDiff;
    public static final int CURRENT_USER = 0;
    public static final int OTHER_USER = 1;
    public static final int FIRST_PLACE = 2;
    public static final int SECOND_PLACE = 3;
    public static final int THIRD_PLACE = 4;

    public LeaderBoardAdapter(@NonNull FirestorePagingOptions<UserModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i, @NonNull UserModel userModel) {
        switch (viewHolder.getItemViewType()) {
            case CURRENT_USER:
                LeaderBoardAdapter.CurrentUserViewHolder currentUserViewHolder = (CurrentUserViewHolder) viewHolder;
                currentUserViewHolder.firstNameTextView.setText(userModel.getFirstName() + " " + userModel.getLastName());
                currentUserViewHolder.pointsTextView.setText(String.valueOf(userModel.getPoints()));
                break;
            case OTHER_USER:
            case THIRD_PLACE:
            case FIRST_PLACE:
            case SECOND_PLACE:
                LeaderBoardAdapter.OtherUserViewHolder otherUserViewHolder = (OtherUserViewHolder) viewHolder;
                otherUserViewHolder.firstNameTextView.setText(userModel.getFirstName() + " " + userModel.getLastName());
                otherUserViewHolder.pointsTextView.setText(String.valueOf(userModel.getPoints()));
                break;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == OTHER_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.otheruser_leaderboard_data, parent, false);
            return new LeaderBoardAdapter.OtherUserViewHolder(view);
        } else if (viewType == CURRENT_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_data, parent, false);
            return new LeaderBoardAdapter.CurrentUserViewHolder(view);
        } else if (viewType == FIRST_PLACE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.first_place_leaderboard, parent, false);
            return new LeaderBoardAdapter.OtherUserViewHolder(view);
        }else if (viewType == SECOND_PLACE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.second_place_leaderboard, parent, false);
            return new LeaderBoardAdapter.OtherUserViewHolder(view);
        }else if (viewType == THIRD_PLACE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.third_place_leaderboard, parent, false);
            return new LeaderBoardAdapter.OtherUserViewHolder(view);
        }
        else
            return null;
    }


    public void setOnItemClickOfSameUidListener(LeaderBoardAdapter.OnItemClickOfSameUidListener listener) {
        this.listener = listener;
    }

    public void setOnItemClickOfDiffUidListener(LeaderBoardAdapter.OnItemClickOfDiffUidListener listenerDiff) {
        this.listenerDiff = listenerDiff;
    }

    public interface OnItemClickOfSameUidListener {
        void onItemOfSameUidClick();
    }

    public interface OnItemClickOfDiffUidListener {
        void onItemOfDiffUidClick(String Uid, String name);
    }

    public class OtherUserViewHolder extends RecyclerView.ViewHolder {
        public MaterialTextView firstNameTextView, pointsTextView;

        public OtherUserViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
            itemView.setOnClickListener(view -> {
                UserModel userModel = getItem(getAdapterPosition()).toObject(UserModel.class);
                listenerDiff.onItemOfDiffUidClick(userModel.getUid(), userModel.getFirstName() + " " + userModel.getLastName());
            });
        }
    }


    public class CurrentUserViewHolder extends RecyclerView.ViewHolder {

        public MaterialTextView firstNameTextView, pointsTextView;

        public CurrentUserViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
            itemView.setOnClickListener(view -> {
                UserModel userModel = getItem(getAdapterPosition()).toObject(UserModel.class);
                if (userModel.getUid().equals(user.getUid())) {
                    listener.onItemOfSameUidClick();
                    new CountDownTimer(10000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            itemView.setEnabled(false);
                        }

                        public void onFinish() {
                            itemView.setEnabled(true);
                        }
                    }.start();
                }
            });
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).get("uid").equals(user.getUid()))
            return CURRENT_USER;
        if (position == 0)
            return FIRST_PLACE;
        if (position == 1)
            return SECOND_PLACE;
        if (position == 2)
            return THIRD_PLACE;
        else
            return OTHER_USER;
    }
}


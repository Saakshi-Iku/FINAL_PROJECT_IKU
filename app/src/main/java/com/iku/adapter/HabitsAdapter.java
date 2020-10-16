package com.iku.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.iku.R;
import com.iku.models.HabitsModel;

public class HabitsAdapter extends FirestoreRecyclerAdapter<HabitsModel, HabitsAdapter.HabitViewHolder> {


    public HabitsAdapter(@NonNull FirestoreRecyclerOptions<HabitsModel> options) {
        super(options);

    }

    @Override
    protected void onBindViewHolder(@NonNull HabitsAdapter.HabitViewHolder habitViewHolder, int position, @NonNull HabitsModel habitsModel) {
        Log.i("adapter", "onBindViewHolder: ${habitsModel.getHabit()}" + habitsModel.getHabit());
        habitViewHolder.habitNameText.setText(habitsModel.getHabit());
        habitViewHolder.habitIllustrationView.setImageResource(R.drawable.ic_iku);
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.habits_data, parent, false);
        return new HabitViewHolder(view);
    }

    public class HabitViewHolder extends RecyclerView.ViewHolder {

        private TextView habitNameText;
        private ImageView habitIllustrationView;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);

            habitNameText = itemView.findViewById(R.id.habitName);
            habitIllustrationView = itemView.findViewById(R.id.habitIllustration);
        }
    }
}
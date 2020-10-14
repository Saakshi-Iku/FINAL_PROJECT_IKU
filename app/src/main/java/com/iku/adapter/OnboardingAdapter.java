package com.iku.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iku.R;
import com.iku.models.OnboardingScreenModel;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<OnboardingScreenModel> onboardingScreenModels;

    public OnboardingAdapter(List<OnboardingScreenModel> onboardingScreenModels) {
        this.onboardingScreenModels = onboardingScreenModels;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OnboardingViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_onboarding, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {

        holder.setOnboardingData(onboardingScreenModels.get(position));


    }

    @Override
    public int getItemCount() {
        return onboardingScreenModels.size();
    }

    class OnboardingViewHolder extends RecyclerView.ViewHolder {

        //private TextView textTitle;
        private final TextView textDescription;
        private final ImageView imageOnboarding;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            //textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            imageOnboarding = itemView.findViewById(R.id.imageOnboarding);
        }

        void setOnboardingData(OnboardingScreenModel onboardingScreenModel) {
            //textTitle.setText(onboardingItem.getTitle());
            textDescription.setText(onboardingScreenModel.getDescription());
            imageOnboarding.setImageResource(onboardingScreenModel.getImage());
        }
    }
}

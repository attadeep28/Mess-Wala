package com.example.messwala.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messwala.R;
import com.example.messwala.modal.Review;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private List<Review> reviewList;
    Context context;
    public ReviewsAdapter(List<Review> reviewList, Context context) {
        this.reviewList = reviewList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.usernameTextView.setText(review.getUsername());
        holder.ratingBar.setRating(review.getRating());
        holder.reviewTextView.setText(review.getReviewText());
        holder.date.setText(review.getDate());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView usernameTextView;
        public RatingBar ratingBar;
        public TextView reviewTextView;
        public TextView date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.user_name);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            reviewTextView = itemView.findViewById(R.id.review_content);
            date = itemView.findViewById(R.id.review_date);
        }
    }
}

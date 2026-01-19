package com.example.bookin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookin.R;
import com.example.bookin.models.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews = new ArrayList<>();

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView reviewerImage;
        private final TextView reviewerName;
        private final TextView reviewDate;
        private final TextView reviewText;
        private final ImageView[] stars = new ImageView[5];

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewerImage = itemView.findViewById(R.id.reviewer_image);
            reviewerName = itemView.findViewById(R.id.reviewer_name);
            reviewDate = itemView.findViewById(R.id.review_date);
            reviewText = itemView.findViewById(R.id.review_text);
            stars[0] = itemView.findViewById(R.id.star_1);
            stars[1] = itemView.findViewById(R.id.star_2);
            stars[2] = itemView.findViewById(R.id.star_3);
            stars[3] = itemView.findViewById(R.id.star_4);
            stars[4] = itemView.findViewById(R.id.star_5);
        }

        void bind(Review review) {
            reviewerName.setText(review.getReviewerName());
            reviewText.setText(review.getText());

            // Set stars
            for (int i = 0; i < 5; i++) {
                if (i < review.getRating()) {
                    stars[i].setImageResource(R.drawable.ic_star_filled);
                } else {
                    stars[i].setImageResource(R.drawable.ic_star_outline);
                }
            }

            // Set date
            reviewDate.setText(getTimeAgo(review.getCreatedAt()));

            // Load reviewer image
            if (review.getReviewerImage() != null && !review.getReviewerImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(review.getReviewerImage())
                        .placeholder(R.drawable.default_profile)
                        .into(reviewerImage);
            } else {
                reviewerImage.setImageResource(R.drawable.default_profile);
            }
        }

        private String getTimeAgo(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days > 0) {
                return days + " hari lalu";
            }

            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            if (hours > 0) {
                return hours + " jam lalu";
            }

            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (minutes > 0) {
                return minutes + " menit lalu";
            }

            return "Baru saja";
        }
    }
}

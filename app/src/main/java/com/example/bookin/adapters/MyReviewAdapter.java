package com.example.bookin.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookin.R;
import com.example.bookin.SellerProfileActivity;
import com.example.bookin.models.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewAdapter.MyReviewViewHolder> {

    public interface OnReviewerInfoListener {
        void onReviewerInfoNeeded(String reviewerId, ReviewerInfoCallback callback);
    }

    public interface ReviewerInfoCallback {
        void onReviewerInfo(String name, String bio, String location, String imageUrl);
    }

    private List<Review> reviews = new ArrayList<>();
    private OnReviewerInfoListener listener;

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    public void setOnReviewerInfoListener(OnReviewerInfoListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_review, parent, false);
        return new MyReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class MyReviewViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView reviewerImage;
        private final TextView reviewerName;
        private final TextView reviewerBio;
        private final TextView reviewerLocation;
        private final TextView reviewDate;
        private final TextView reviewText;
        private final ImageView[] stars = new ImageView[5];

        MyReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewerImage = itemView.findViewById(R.id.reviewer_image);
            reviewerName = itemView.findViewById(R.id.reviewer_name);
            reviewerBio = itemView.findViewById(R.id.reviewer_bio);
            reviewerLocation = itemView.findViewById(R.id.reviewer_location);
            reviewDate = itemView.findViewById(R.id.review_date);
            reviewText = itemView.findViewById(R.id.review_text);
            stars[0] = itemView.findViewById(R.id.star_1);
            stars[1] = itemView.findViewById(R.id.star_2);
            stars[2] = itemView.findViewById(R.id.star_3);
            stars[3] = itemView.findViewById(R.id.star_4);
            stars[4] = itemView.findViewById(R.id.star_5);
        }

        void bind(Review review) {
            reviewText.setText(review.getText());
            reviewDate.setText(getTimeAgo(review.getCreatedAt()));

            // Set star rating
            for (int i = 0; i < 5; i++) {
                if (i < review.getRating()) {
                    stars[i].setImageResource(R.drawable.ic_star_filled);
                } else {
                    stars[i].setImageResource(R.drawable.ic_star_outline);
                }
            }

            // Set click listener to open reviewer profile
            itemView.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, SellerProfileActivity.class);
                intent.putExtra("seller_id", review.getReviewerId());
                context.startActivity(intent);
            });

            // Load reviewer info from Firebase
            if (listener != null) {
                listener.onReviewerInfoNeeded(review.getReviewerId(), (name, bio, location, imageUrl) -> {
                    reviewerName.setText(name != null ? name : "Pengguna");
                    reviewerBio.setText(bio != null && !bio.isEmpty() ? bio : "");

                    // Get shortest location (city only)
                    if (location != null && !location.isEmpty()) {
                        String shortLocation = getShortLocation(location);
                        reviewerLocation.setText(shortLocation);
                    } else {
                        reviewerLocation.setText("");
                    }

                    // Load image
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(itemView.getContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.default_profile)
                                .into(reviewerImage);
                    } else {
                        reviewerImage.setImageResource(R.drawable.default_profile);
                    }
                });
            } else {
                // Fallback to data from Review object
                reviewerName.setText(review.getReviewerName() != null ? review.getReviewerName() : "Pengguna");
                reviewerBio.setText("");
                reviewerLocation.setText("");

                if (review.getReviewerImage() != null && !review.getReviewerImage().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(review.getReviewerImage())
                            .placeholder(R.drawable.default_profile)
                            .into(reviewerImage);
                }
            }
        }

        private String getShortLocation(String fullLocation) {
            if (fullLocation == null || fullLocation.isEmpty()) {
                return "";
            }
            // Get just the first part (city name)
            String[] parts = fullLocation.split(",");
            if (parts.length > 0) {
                return parts[0].trim();
            }
            return fullLocation;
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

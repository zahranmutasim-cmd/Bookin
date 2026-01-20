package com.example.bookin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.bookin.adapters.MyReviewAdapter;
import com.example.bookin.databinding.ActivityMyReviewsBinding;
import com.example.bookin.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyReviewsActivity extends AppCompatActivity {

    private ActivityMyReviewsBinding binding;
    private MyReviewAdapter reviewAdapter;
    private DatabaseReference usersRef;
    private DatabaseReference reviewsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");

        setupViews();
        loadUserProfile();
        loadMyReviews();
        setupBottomNavigation();
    }

    private void setupViews() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        // Setup RecyclerView
        reviewAdapter = new MyReviewAdapter();
        reviewAdapter.setOnReviewerInfoListener((reviewerId, callback) -> {
            usersRef.child(reviewerId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String imageUrl = snapshot.child("profileImage").getValue(String.class);
                    callback.onReviewerInfo(name, bio, location, imageUrl);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onReviewerInfo(null, null, null, null);
                }
            });
        });

        binding.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void loadUserProfile() {
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileImage = snapshot.child("profileImage").getValue(String.class);

                binding.userName.setText(name != null ? name : "Pengguna");

                if (profileImage != null && !profileImage.isEmpty()) {
                    Glide.with(MyReviewsActivity.this)
                            .load(profileImage)
                            .placeholder(R.drawable.default_profile)
                            .into(binding.profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadMyReviews() {
        // Load reviews where the current user is the seller (reviews about me)
        reviewsRef.orderByChild("sellerId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Review> reviews = new ArrayList<>();
                        for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                            Review review = reviewSnap.getValue(Review.class);
                            if (review != null) {
                                review.setId(reviewSnap.getKey());
                                reviews.add(review);
                            }
                        }

                        // Sort by newest first
                        Collections.sort(reviews, (r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));

                        reviewAdapter.setReviews(reviews);

                        if (reviews.isEmpty()) {
                            binding.noReviewsText.setVisibility(View.VISIBLE);
                        } else {
                            binding.noReviewsText.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MyReviewsActivity.this, "Gagal memuat ulasan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBottomNavigation() {
        View navbar = binding.bottomNavbarContainer.getChildAt(0);
        if (navbar != null) {
            navbar.findViewById(R.id.nav_home).setOnClickListener(v -> {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
            navbar.findViewById(R.id.nav_chat).setOnClickListener(v -> {
                startActivity(new Intent(this, ChatActivity.class));
            });
            navbar.findViewById(R.id.nav_add).setOnClickListener(v -> {
                startActivity(new Intent(this, PostAdActivity.class));
            });
            navbar.findViewById(R.id.nav_ads).setOnClickListener(v -> {
                startActivity(new Intent(this, AdsActivity.class));
            });
            navbar.findViewById(R.id.nav_profile).setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }
    }
}

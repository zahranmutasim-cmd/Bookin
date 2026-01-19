package com.example.bookin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookin.FirebaseBookAdapter;
import com.example.bookin.adapters.ReviewAdapter;
import com.example.bookin.databinding.ActivitySellerProfileBinding;
import com.example.bookin.models.Book;
import com.example.bookin.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SellerProfileActivity extends AppCompatActivity {

    private ActivitySellerProfileBinding binding;
    private String sellerId;
    private String sellerName;
    private String sellerPhone;
    private String sellerImage;
    private DatabaseReference usersRef;
    private DatabaseReference booksRef;
    private DatabaseReference reviewsRef;
    private FirebaseBookAdapter bookAdapter;
    private ReviewAdapter reviewAdapter;
    private List<Book> sellerBooks = new ArrayList<>();
    private List<Review> sellerReviews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sellerId = getIntent().getStringExtra("seller_id");
        if (sellerId == null) {
            Toast.makeText(this, "Seller not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        booksRef = FirebaseDatabase.getInstance().getReference("books");
        reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");

        setupRecyclerViews();
        setupClickListeners();
        loadSellerData();
        loadSellerBooks();
        loadSellerReviews();
        setupBottomNavigation();
    }

    private void setupRecyclerViews() {
        // Books RecyclerView
        bookAdapter = new FirebaseBookAdapter(sellerBooks, book -> {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
            startActivity(intent);
        });
        binding.sellerBooksRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.sellerBooksRecyclerView.setAdapter(bookAdapter);

        // Reviews RecyclerView
        reviewAdapter = new ReviewAdapter();
        binding.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.reportButton.setOnClickListener(v -> showReportDialog());
        binding.reviewButton.setOnClickListener(v -> showReviewDialog());
        binding.shareButton.setOnClickListener(v -> shareSeller());

        binding.whatsappButton.setOnClickListener(v -> {
            if (sellerPhone != null && !sellerPhone.isEmpty()) {
                String formattedPhone = sellerPhone.startsWith("0")
                        ? "62" + sellerPhone.substring(1)
                        : sellerPhone;
                String url = "https://wa.me/" + formattedPhone;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSellerData() {
        usersRef.child(sellerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    sellerName = snapshot.child("name").getValue(String.class);
                    sellerPhone = snapshot.child("phone").getValue(String.class);
                    sellerImage = snapshot.child("profileImage").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    Double totalRating = snapshot.child("totalRating").getValue(Double.class);
                    Long ratingCount = snapshot.child("ratingCount").getValue(Long.class);

                    binding.sellerName.setText(sellerName != null ? sellerName : "Penjual");

                    if (bio != null && !bio.isEmpty()) {
                        binding.sellerBio.setText(bio);
                    } else {
                        binding.sellerBio.setText("Tidak ada bio");
                    }

                    // Calculate average rating
                    if (totalRating != null && ratingCount != null && ratingCount > 0) {
                        double avg = totalRating / ratingCount;
                        binding.sellerRating.setText(String.format("%.1f", avg));
                    } else {
                        binding.sellerRating.setText("0.0");
                    }

                    // Load profile image
                    if (sellerImage != null && !sellerImage.isEmpty()) {
                        Glide.with(SellerProfileActivity.this)
                                .load(sellerImage)
                                .placeholder(R.drawable.default_profile)
                                .into(binding.sellerProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SellerProfileActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSellerBooks() {
        booksRef.orderByChild("userId").equalTo(sellerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sellerBooks.clear();
                        for (DataSnapshot bookSnap : snapshot.getChildren()) {
                            Book book = bookSnap.getValue(Book.class);
                            if (book != null) {
                                book.setId(bookSnap.getKey());
                                sellerBooks.add(book);
                            }
                        }
                        bookAdapter.updateBooks(sellerBooks);

                        if (sellerBooks.isEmpty()) {
                            binding.noProductsText.setVisibility(View.VISIBLE);
                        } else {
                            binding.noProductsText.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadSellerReviews() {
        reviewsRef.orderByChild("sellerId").equalTo(sellerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sellerReviews.clear();
                        for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                            Review review = reviewSnap.getValue(Review.class);
                            if (review != null) {
                                review.setId(reviewSnap.getKey());
                                sellerReviews.add(review);
                            }
                        }
                        reviewAdapter.setReviews(sellerReviews);

                        if (sellerReviews.isEmpty()) {
                            binding.noReviewsText.setVisibility(View.VISIBLE);
                        } else {
                            binding.noReviewsText.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void showReportDialog() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId.equals(sellerId)) {
            Toast.makeText(this, "Tidak dapat melaporkan diri sendiri", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if already reported
        usersRef.child(sellerId).child("reporters").child(currentUserId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                        showAlreadyReportedDialog();
                    } else {
                        showReportConfirmDialog(currentUserId);
                    }
                });
    }

    private void showAlreadyReportedDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_already_reported, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.ok_button).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showReportConfirmDialog(String reporterId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.cancel_button).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.report_confirm_button).setOnClickListener(v -> {
            usersRef.child(sellerId).child("reporters").child(reporterId).setValue(true);

            usersRef.child(sellerId).child("reportCount").get()
                    .addOnSuccessListener(snapshot -> {
                        int currentCount = 0;
                        if (snapshot.exists()) {
                            Long count = snapshot.getValue(Long.class);
                            currentCount = count != null ? count.intValue() : 0;
                        }
                        int newCount = currentCount + 1;
                        usersRef.child(sellerId).child("reportCount").setValue(newCount);

                        if (newCount >= 5) {
                            DatabaseReference flaggedRef = FirebaseDatabase.getInstance().getReference("flagged_users");
                            flaggedRef.child(sellerId).child("reportCount").setValue(newCount);
                            flaggedRef.child(sellerId).child("userName").setValue(sellerName);
                            flaggedRef.child(sellerId).child("flaggedAt").setValue(System.currentTimeMillis());
                        }

                        Toast.makeText(this, "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void showReviewDialog() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId.equals(sellerId)) {
            Toast.makeText(this, "Tidak dapat mengulas diri sendiri", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if already reviewed
        reviewsRef.orderByChild("reviewerId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean alreadyReviewed = false;
                        for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                            Review review = reviewSnap.getValue(Review.class);
                            if (review != null && sellerId.equals(review.getSellerId())) {
                                alreadyReviewed = true;
                                break;
                            }
                        }

                        if (alreadyReviewed) {
                            Toast.makeText(SellerProfileActivity.this,
                                    "Anda sudah memberikan ulasan untuk penjual ini", Toast.LENGTH_SHORT).show();
                        } else {
                            showReviewInputDialog(currentUserId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void showReviewInputDialog(String reviewerId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_review, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView[] stars = new ImageView[5];
        stars[0] = dialogView.findViewById(R.id.star_1);
        stars[1] = dialogView.findViewById(R.id.star_2);
        stars[2] = dialogView.findViewById(R.id.star_3);
        stars[3] = dialogView.findViewById(R.id.star_4);
        stars[4] = dialogView.findViewById(R.id.star_5);
        TextView ratingText = dialogView.findViewById(R.id.rating_text);
        EditText reviewInput = dialogView.findViewById(R.id.review_input);

        final int[] selectedRating = { 0 };
        String[] ratingLabels = { "Sangat Buruk", "Buruk", "Cukup", "Baik", "Sangat Baik" };

        for (int i = 0; i < 5; i++) {
            final int starIndex = i;
            stars[i].setOnClickListener(v -> {
                selectedRating[0] = starIndex + 1;
                for (int j = 0; j < 5; j++) {
                    if (j <= starIndex) {
                        stars[j].setImageResource(R.drawable.ic_star_filled);
                    } else {
                        stars[j].setImageResource(R.drawable.ic_star_outline);
                    }
                }
                ratingText.setText(ratingLabels[starIndex]);
            });
        }

        dialogView.findViewById(R.id.cancel_button).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.submit_button).setOnClickListener(v -> {
            if (selectedRating[0] == 0) {
                Toast.makeText(this, "Pilih rating terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            String reviewText = reviewInput.getText().toString().trim();
            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Tulis ulasan terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get current user info
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String reviewerName = snapshot.child("name").getValue(String.class);
                    String reviewerImage = snapshot.child("profileImage").getValue(String.class);

                    Review review = new Review(
                            currentUserId,
                            reviewerName != null ? reviewerName : "Pengguna",
                            reviewerImage,
                            sellerId,
                            selectedRating[0],
                            reviewText);

                    // Save review
                    String reviewId = reviewsRef.push().getKey();
                    if (reviewId != null) {
                        reviewsRef.child(reviewId).setValue(review);
                    }

                    // Update seller rating
                    usersRef.child(sellerId).child("raters").child(currentUserId).setValue(selectedRating[0]);

                    usersRef.child(sellerId).get().addOnSuccessListener(sellerSnap -> {
                        double totalRating = 0;
                        int ratingCount = 0;

                        if (sellerSnap.child("totalRating").exists()) {
                            Double t = sellerSnap.child("totalRating").getValue(Double.class);
                            totalRating = t != null ? t : 0;
                        }
                        if (sellerSnap.child("ratingCount").exists()) {
                            Long c = sellerSnap.child("ratingCount").getValue(Long.class);
                            ratingCount = c != null ? c.intValue() : 0;
                        }

                        usersRef.child(sellerId).child("totalRating").setValue(totalRating + selectedRating[0]);
                        usersRef.child(sellerId).child("ratingCount").setValue(ratingCount + 1);
                    });

                    Toast.makeText(SellerProfileActivity.this, "Ulasan berhasil dikirim!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SellerProfileActivity.this, "Gagal mengirim ulasan", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void shareSeller() {
        String shareText = "📦 Cek penjual ini di Bookin!\n\n";
        shareText += "👤 " + (sellerName != null ? sellerName : "Penjual") + "\n";
        shareText += "⭐ Rating: " + binding.sellerRating.getText().toString() + "\n\n";
        shareText += "Download Bookin sekarang!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Penjual: " + sellerName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Bagikan via"));
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

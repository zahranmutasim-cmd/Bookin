package com.example.bookin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.bookin.models.Book;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";

    private ViewPager2 imageViewPager;
    private LinearLayout indicatorContainer;
    private TextView bookTitle, bookPrice, bookLocationSmall, bookDescription;
    private TextView sellerName, sellerJoinDate, sellerLocationText, sellerLocationDetail;
    private CircleImageView sellerProfileImage;
    private EditText messageInput;
    private MaterialButton sendButton, whatsappButton;
    private LinearLayout reportButton, ratingButton, shareButton;
    private ImageView mapPreview;
    private View mapClickOverlay;
    private RecyclerView suggestedRecyclerView;

    private DatabaseReference booksRef;
    private DatabaseReference usersRef;
    private Book currentBook;
    private String bookId;

    private List<String> imageUrls;
    private ImageSliderAdapter imageAdapter;
    private FirebaseBookAdapter suggestedAdapter;
    private List<Book> suggestedBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        if (bookId == null) {
            Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        booksRef = FirebaseDatabase.getInstance().getReference("books");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initializeViews();
        setupListeners();
        loadBookDetails();
        loadSuggestedBooks();
    }

    private void initializeViews() {
        // Header
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Image carousel
        imageViewPager = findViewById(R.id.image_view_pager);
        indicatorContainer = findViewById(R.id.indicator_container);
        imageUrls = new ArrayList<>();
        imageAdapter = new ImageSliderAdapter(imageUrls);
        imageViewPager.setAdapter(imageAdapter);

        // Book info
        bookTitle = findViewById(R.id.book_title);
        bookPrice = findViewById(R.id.book_price);
        bookLocationSmall = findViewById(R.id.book_location_small);
        bookDescription = findViewById(R.id.book_description);

        // Chat section
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        whatsappButton = findViewById(R.id.whatsapp_button);

        // Action buttons
        reportButton = findViewById(R.id.report_button);
        ratingButton = findViewById(R.id.rating_button);
        shareButton = findViewById(R.id.share_button);

        // Seller info
        sellerProfileImage = findViewById(R.id.seller_profile_image);
        sellerName = findViewById(R.id.seller_name);
        sellerJoinDate = findViewById(R.id.seller_join_date);

        // Location
        mapPreview = findViewById(R.id.map_preview);
        mapClickOverlay = findViewById(R.id.map_click_overlay);
        sellerLocationText = findViewById(R.id.seller_location_text);
        sellerLocationDetail = findViewById(R.id.seller_location_detail);

        // Suggested books
        suggestedRecyclerView = findViewById(R.id.suggested_recycler_view);
        suggestedBooks = new ArrayList<>();
        suggestedAdapter = new FirebaseBookAdapter(suggestedBooks, book -> {
            Intent intent = new Intent(BookDetailActivity.this, BookDetailActivity.class);
            intent.putExtra(EXTRA_BOOK_ID, book.getId());
            startActivity(intent);
        });
        suggestedRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        suggestedRecyclerView.setAdapter(suggestedAdapter);
    }

    private void setupListeners() {
        // WhatsApp button - implicit intent
        whatsappButton.setOnClickListener(v -> {
            if (currentBook != null && currentBook.getUserPhone() != null && !currentBook.getUserPhone().isEmpty()) {
                String phoneNumber = currentBook.getUserPhone();
                // Remove any non-digit characters and add country code if needed
                phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
                if (phoneNumber.startsWith("0")) {
                    phoneNumber = "62" + phoneNumber.substring(1);
                }

                String message = "Halo, saya tertarik dengan buku \"" + currentBook.getTitle()
                        + "\" yang Anda posting di Bookin.";
                String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Nomor telepon penjual tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });

        // Send button - open chat with seller
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Tulis pesan terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null 
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() 
                    : null;
            
            if (currentUserId == null) {
                Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (currentBook == null || currentBook.getUserId() == null) {
                Toast.makeText(this, "Data penjual tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Don't allow chatting with yourself
            if (currentUserId.equals(currentBook.getUserId())) {
                Toast.makeText(this, "Anda tidak bisa mengirim pesan ke diri sendiri", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Intent chatIntent = new Intent(this, ChatRoomActivity.class);
            chatIntent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_ID, currentBook.getUserId());
            chatIntent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_NAME, currentBook.getUserName());
            chatIntent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_IMAGE, currentBook.getUserProfileImage());
            chatIntent.putExtra(ChatRoomActivity.EXTRA_BOOK_ID, bookId);
            chatIntent.putExtra(ChatRoomActivity.EXTRA_BOOK_TITLE, currentBook.getTitle());
            chatIntent.putExtra(ChatRoomActivity.EXTRA_BOOK_IMAGE, currentBook.getFrontImageUrl());
            chatIntent.putExtra(ChatRoomActivity.EXTRA_BOOK_PRICE, currentBook.getPrice());
            chatIntent.putExtra(ChatRoomActivity.EXTRA_INITIAL_MESSAGE, message);
            chatIntent.putExtra(ChatRoomActivity.EXTRA_IS_NEW_CHAT, true);
            startActivity(chatIntent);
            messageInput.setText("");
        });

        // Action buttons
        reportButton.setOnClickListener(v -> showReportDialog());
        ratingButton.setOnClickListener(v -> showRatingDialog());
        shareButton.setOnClickListener(v -> shareBook());

        // Map click - open in Google Maps
        mapClickOverlay.setOnClickListener(v -> {
            if (currentBook != null && currentBook.getLatitude() != 0 && currentBook.getLongitude() != 0) {
                String uri = "geo:" + currentBook.getLatitude() + "," + currentBook.getLongitude() +
                        "?q=" + currentBook.getLatitude() + "," + currentBook.getLongitude() +
                        "(Lokasi Penjual)";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    // Fallback to browser
                    String browserUri = "https://www.google.com/maps?q=" +
                            currentBook.getLatitude() + "," + currentBook.getLongitude();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)));
                }
            }
        });

        // Seller info click - open SellerProfileActivity
        View sellerSection = findViewById(R.id.view_profile_arrow);
        View.OnClickListener sellerClickListener = v -> {
            if (currentBook != null && currentBook.getUserId() != null) {
                Intent intent = new Intent(BookDetailActivity.this, SellerProfileActivity.class);
                intent.putExtra("seller_id", currentBook.getUserId());
                startActivity(intent);
            }
        };
        sellerProfileImage.setOnClickListener(sellerClickListener);
        sellerName.setOnClickListener(sellerClickListener);
        sellerSection.setOnClickListener(sellerClickListener);

        // ViewPager page change listener for indicators
        imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }
        });

        // Image click to view fullscreen
        imageAdapter.setOnImageClickListener(position -> {
            Intent intent = new Intent(BookDetailActivity.this, FullScreenImageActivity.class);
            intent.putStringArrayListExtra(FullScreenImageActivity.EXTRA_IMAGE_URLS, new ArrayList<>(imageUrls));
            intent.putExtra(FullScreenImageActivity.EXTRA_CURRENT_POSITION, position);
            startActivity(intent);
        });
    }

    private void loadBookDetails() {
        booksRef.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentBook = snapshot.getValue(Book.class);
                if (currentBook != null) {
                    displayBookDetails();
                    loadSellerInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookDetailActivity.this, "Gagal memuat detail buku", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBookDetails() {
        // Title
        bookTitle.setText(currentBook.getTitle());

        // Price
        if (currentBook.getPrice() == 0) {
            bookPrice.setText("Gratis");
        } else {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            bookPrice.setText(formatter.format(currentBook.getPrice()));
        }

        // Location
        bookLocationSmall.setText(currentBook.getLocation());
        sellerLocationText.setText(currentBook.getLocation());
        sellerLocationDetail.setText("Lihat lokasi di peta");

        // Description
        bookDescription.setText(currentBook.getDescription());

        // Images
        imageUrls.clear();
        if (currentBook.getFrontImageUrl() != null && !currentBook.getFrontImageUrl().isEmpty()) {
            imageUrls.add(currentBook.getFrontImageUrl());
        }
        if (currentBook.getBackImageUrl() != null && !currentBook.getBackImageUrl().isEmpty()) {
            imageUrls.add(currentBook.getBackImageUrl());
        }
        imageAdapter.notifyDataSetChanged();
        setupIndicators();

        // Set location icon instead of map preview (more reliable)
        if (currentBook.getLatitude() != 0 && currentBook.getLongitude() != 0) {
            // Just show a location icon - user can click to open in maps
            mapPreview.setImageResource(R.drawable.ic_location_map);
            mapPreview.setScaleType(ImageView.ScaleType.CENTER);
        }
    }

    private void loadSellerInfo() {
        // Set seller name from book data
        sellerName.setText(currentBook.getUserName());

        // Load seller profile image
        if (currentBook.getUserProfileImage() != null && !currentBook.getUserProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(currentBook.getUserProfileImage())
                    .placeholder(R.drawable.default_profile)
                    .into(sellerProfileImage);
        }

        // Load seller join date from users database
        usersRef.child(currentBook.getUserId()).child("createdAt").get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Long createdAt = snapshot.getValue(Long.class);
                        if (createdAt != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"));
                            String joinDate = sdf.format(new Date(createdAt));
                            sellerJoinDate.setText("Bergabung pada Bookin pada " + joinDate);
                        }
                    } else {
                        sellerJoinDate.setText("Pengguna Bookin");
                    }
                })
                .addOnFailureListener(e -> sellerJoinDate.setText("Pengguna Bookin"));
    }

    private void loadSuggestedBooks() {
        booksRef.limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                suggestedBooks.clear();
                List<Book> tempBooks = new ArrayList<>();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    // Don't show current book in suggestions
                    if (book != null && !book.getId().equals(bookId)) {
                        tempBooks.add(book);
                    }
                }
                // Limit to 4 books
                int limit = Math.min(4, tempBooks.size());
                suggestedBooks.addAll(tempBooks.subList(0, limit));
                suggestedAdapter.updateBooks(suggestedBooks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silent fail
            }
        });
    }

    private void setupIndicators() {
        indicatorContainer.removeAllViews();
        for (int i = 0; i < imageUrls.size(); i++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(i == 0 ? R.drawable.indicator_active : R.drawable.indicator_inactive);
            indicatorContainer.addView(indicator);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            View indicator = indicatorContainer.getChildAt(i);
            indicator
                    .setBackgroundResource(i == position ? R.drawable.indicator_active : R.drawable.indicator_inactive);
        }
    }

    private void showReportDialog() {
        if (currentBook == null)
            return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        String sellerId = currentBook.getUserId();
        if (sellerId == null || sellerId.equals(currentUserId)) {
            Toast.makeText(this, "Tidak dapat melaporkan akun sendiri", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user already reported this AD (stored in books node)
        booksRef.child(bookId).child("reporters").child(currentUserId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                        showAlreadyReportedDialog();
                    } else {
                        showReportConfirmDialog(currentUserId, sellerId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memeriksa status laporan", Toast.LENGTH_SHORT).show();
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

    private void showReportConfirmDialog(String reporterId, String sellerId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.cancel_button).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.report_confirm_button).setOnClickListener(v -> {
            // 1. Mark this user as having reported this AD
            booksRef.child(bookId).child("reporters").child(reporterId).setValue(true);

            // 2. Get and increment ad report count
            booksRef.child(bookId).child("reportCount").get()
                    .addOnSuccessListener(snapshot -> {
                        int adReportCount = 0;
                        if (snapshot.exists()) {
                            Long count = snapshot.getValue(Long.class);
                            adReportCount = count != null ? count.intValue() : 0;
                        }
                        int newAdReportCount = adReportCount + 1;
                        booksRef.child(bookId).child("reportCount").setValue(newAdReportCount);

                        // 3. Auto-delete ad if reports > 5
                        if (newAdReportCount > 5) {
                            booksRef.child(bookId).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Iklan telah dihapus karena banyak laporan",
                                                Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                        finish();
                                    });
                        } else {
                            dialog.dismiss();
                        }

                        // 4. Also track on seller for flagging (separate from ad deletion)
                        usersRef.child(sellerId).child("reportCount").get()
                                .addOnSuccessListener(userSnapshot -> {
                                    int userReportCount = 0;
                                    if (userSnapshot.exists()) {
                                        Long count = userSnapshot.getValue(Long.class);
                                        userReportCount = count != null ? count.intValue() : 0;
                                    }
                                    int newUserReportCount = userReportCount + 1;
                                    usersRef.child(sellerId).child("reportCount").setValue(newUserReportCount);

                                    // Flag user for manual review if >= 5 reports
                                    if (newUserReportCount >= 5) {
                                        DatabaseReference flaggedRef = FirebaseDatabase.getInstance()
                                                .getReference("flagged_users");
                                        flaggedRef.child(sellerId).child("reportCount").setValue(newUserReportCount);
                                        flaggedRef.child(sellerId).child("userName")
                                                .setValue(currentBook.getUserName());
                                        flaggedRef.child(sellerId).child("userEmail")
                                                .setValue(currentBook.getUserEmail());
                                        flaggedRef.child(sellerId).child("flaggedAt")
                                                .setValue(System.currentTimeMillis());
                                    }
                                });

                        if (newAdReportCount <= 5) {
                            Toast.makeText(this, "Laporan berhasil dikirim. Terima kasih!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal mengirim laporan", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void showRatingDialog() {
        if (currentBook == null)
            return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        String sellerId = currentBook.getUserId();
        if (sellerId == null || sellerId.equals(currentUserId)) {
            Toast.makeText(this, "Tidak dapat memberikan rating untuk diri sendiri", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user already rated this seller
        usersRef.child(sellerId).child("raters").child(currentUserId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        Long previousRating = snapshot.getValue(Long.class);
                        showAlreadyRatedDialog(previousRating != null ? previousRating.intValue() : 0);
                    } else {
                        showRatingInputDialog(currentUserId, sellerId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memeriksa status rating", Toast.LENGTH_SHORT).show();
                });
    }

    private void showAlreadyRatedDialog(int userRating) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_already_rated, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView ratingText = dialogView.findViewById(R.id.your_rating_text);
        ratingText.setText("Anda memberikan rating " + userRating + " bintang untuk penjual ini");

        ImageView[] stars = new ImageView[5];
        stars[0] = dialogView.findViewById(R.id.star_1);
        stars[1] = dialogView.findViewById(R.id.star_2);
        stars[2] = dialogView.findViewById(R.id.star_3);
        stars[3] = dialogView.findViewById(R.id.star_4);
        stars[4] = dialogView.findViewById(R.id.star_5);

        for (int i = 0; i < 5; i++) {
            if (i < userRating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_outline);
            }
        }

        dialogView.findViewById(R.id.ok_button).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showRatingInputDialog(String raterId, String sellerId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null);
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

            // Save rater's rating to seller's raters list
            usersRef.child(sellerId).child("raters").child(raterId).setValue(selectedRating[0]);

            // Get current rating stats and update
            usersRef.child(sellerId).get()
                    .addOnSuccessListener(snapshot -> {
                        double currentTotalRating = 0;
                        int currentRatingCount = 0;

                        if (snapshot.child("totalRating").exists()) {
                            Double total = snapshot.child("totalRating").getValue(Double.class);
                            currentTotalRating = total != null ? total : 0;
                        }
                        if (snapshot.child("ratingCount").exists()) {
                            Long count = snapshot.child("ratingCount").getValue(Long.class);
                            currentRatingCount = count != null ? count.intValue() : 0;
                        }

                        double newTotalRating = currentTotalRating + selectedRating[0];
                        int newRatingCount = currentRatingCount + 1;

                        usersRef.child(sellerId).child("totalRating").setValue(newTotalRating);
                        usersRef.child(sellerId).child("ratingCount").setValue(newRatingCount);

                        double avgRating = newTotalRating / newRatingCount;
                        Toast.makeText(this,
                                String.format("Rating %.1f bintang untuk penjual berhasil dikirim!", avgRating),
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal mengirim rating", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void shareBook() {
        if (currentBook == null)
            return;

        // Generate shareable content
        String shareText = "📚 " + currentBook.getTitle() + "\n\n";

        if (currentBook.getPrice() == 0) {
            shareText += "💰 Gratis!\n";
        } else {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            shareText += "💰 " + formatter.format(currentBook.getPrice()) + "\n";
        }

        shareText += "📍 " + currentBook.getLocation() + "\n\n";
        shareText += "📖 " + currentBook.getDescription() + "\n\n";
        shareText += "Cek di aplikasi Bookin!\n";
        shareText += "ID: " + bookId;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Buku: " + currentBook.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Bagikan via"));
    }
}

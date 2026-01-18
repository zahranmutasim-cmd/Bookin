package com.example.bookin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookin.models.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class UploadAdConfirmActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_FRONT_IMAGE_URL = "extra_front_image_url";
    public static final String EXTRA_BACK_IMAGE_URL = "extra_back_image_url";
    public static final String EXTRA_SELECTED_TYPE = "extra_selected_type";
    public static final String EXTRA_CONDITION = "extra_condition";
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_PRICE = "extra_price";

    private String categoryName, frontImageUrl, backImageUrl, selectedType;
    private String condition, location, title, description;
    private double latitude, longitude;
    private long price;

    private TextView userNameTv, userPhoneTv, userEmailTv;
    private TextView bookTitleTv, bookTypeTv, bookConditionTv, locationDisplayTv, categoryBadge;
    private ImageView bookCoverImage;
    private CircleImageView userProfileImage;
    private Button continueButton;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference booksRef;

    private String userId, userName, userPhone, userEmail, userProfileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_ad_confirm);

        mAuth = FirebaseAuth.getInstance();
        booksRef = FirebaseDatabase.getInstance().getReference("books");

        // Get all data from intent
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        frontImageUrl = getIntent().getStringExtra(EXTRA_FRONT_IMAGE_URL);
        backImageUrl = getIntent().getStringExtra(EXTRA_BACK_IMAGE_URL);
        selectedType = getIntent().getStringExtra(EXTRA_SELECTED_TYPE);
        condition = getIntent().getStringExtra(EXTRA_CONDITION);
        location = getIntent().getStringExtra(EXTRA_LOCATION);
        latitude = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0);
        longitude = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0);
        title = getIntent().getStringExtra(EXTRA_TITLE);
        description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        price = getIntent().getLongExtra(EXTRA_PRICE, 0);

        initializeViews();

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        loadUserData();
        displayBookData();

        continueButton.setOnClickListener(v -> uploadBookToFirebase());
    }

    private void initializeViews() {
        userNameTv = findViewById(R.id.user_name);
        userPhoneTv = findViewById(R.id.user_phone);
        userEmailTv = findViewById(R.id.user_email);
        userProfileImage = findViewById(R.id.user_profile_image);

        bookTitleTv = findViewById(R.id.book_title_display);
        bookTypeTv = findViewById(R.id.book_type_display);
        bookConditionTv = findViewById(R.id.book_condition_display);
        locationDisplayTv = findViewById(R.id.location_display);
        bookCoverImage = findViewById(R.id.book_cover_image);
        categoryBadge = findViewById(R.id.category_badge);

        continueButton = findViewById(R.id.continue_button);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengunggah iklan...");
        progressDialog.setCancelable(false);
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userName = currentUser.getDisplayName();
            userEmail = currentUser.getEmail();
            userProfileImageUrl = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null;

            // Display basic info
            userNameTv.setText(userName != null && !userName.isEmpty() ? userName : "Pengguna");
            userEmailTv.setText(userEmail != null && !userEmail.isEmpty() ? userEmail : "-");

            // Load profile image
            if (userProfileImageUrl != null && !userProfileImageUrl.isEmpty()) {
                Glide.with(this).load(userProfileImageUrl).into(userProfileImage);
            }

            // Fetch phone number from Realtime Database
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.child(userId).child("phone").get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    userPhone = snapshot.getValue(String.class);
                    userPhoneTv.setText(userPhone != null && !userPhone.isEmpty() ? userPhone : "-");
                } else {
                    userPhoneTv.setText("-");
                }
            }).addOnFailureListener(e -> {
                userPhoneTv.setText("-");
            });
        }
    }

    private void displayBookData() {
        bookTitleTv.setText(title != null ? title : "Judul tidak tersedia");
        bookTypeTv.setText(selectedType != null ? selectedType : "-");
        bookConditionTv.setText(condition != null ? condition : "-");
        locationDisplayTv.setText(location != null ? location : "-");
        categoryBadge.setText(categoryName != null ? categoryName : "Buku");

        if (frontImageUrl != null && !frontImageUrl.isEmpty()) {
            Glide.with(this).load(frontImageUrl).into(bookCoverImage);
        }
    }

    private void uploadBookToFirebase() {
        if (frontImageUrl == null || frontImageUrl.isEmpty()) {
            Toast.makeText(this, "Gambar sampul tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null) {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        String bookId = booksRef.push().getKey();
        if (bookId == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Gagal membuat ID buku", Toast.LENGTH_SHORT).show();
            return;
        }

        Book book = new Book(
                bookId,
                title,
                description,
                price,
                selectedType,
                condition,
                location,
                latitude,
                longitude,
                frontImageUrl,
                backImageUrl,
                userId,
                userName != null ? userName : "Unknown User",
                userPhone != null ? userPhone : "",
                userEmail != null ? userEmail : "",
                userProfileImageUrl != null ? userProfileImageUrl : "",
                System.currentTimeMillis(),
                false,
                categoryName);

        booksRef.child(bookId).setValue(book)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(UploadAdConfirmActivity.this, "Iklan berhasil diunggah!", Toast.LENGTH_SHORT).show();

                    // Navigate back to PostAdActivity and clear the upload stack
                    Intent intent = new Intent(UploadAdConfirmActivity.this, PostAdActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UploadAdConfirmActivity.this, "Gagal mengunggah: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}

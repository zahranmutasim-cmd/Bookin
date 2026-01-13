package com.example.bookin;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = "ProfileActivity";

    private ImageView profileImageView;
    private ProgressBar loadingIndicator;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadImageToFirebase(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNavigationBar();

        profileImageView = findViewById(R.id.profile_image_view);
        loadingIndicator = findViewById(R.id.profile_loading_indicator);
        TextView usernameTextView = findViewById(R.id.profile_username);
        TextView emailTextView = findViewById(R.id.profile_email);
        MaterialButton logoutButton = findViewById(R.id.logout_button);

        // Change the color of the profile icon
        ImageView profileIcon = findViewById(R.id.nav_profile_icon);
        if (profileIcon != null) {
            profileIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(profileImageView);
            }
            usernameTextView.setText(user.getDisplayName());
            emailTextView.setText(user.getEmail());
        }

        profileImageView.setOnClickListener(v -> mGetContent.launch("image/*"));

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        loadingIndicator.setVisibility(View.VISIBLE);

        StorageReference profilePicRef = FirebaseStorage.getInstance()
                .getReference("profile_pictures/" + user.getUid() + ".jpg");

        profilePicRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profilePicRef.getDownloadUrl().addOnSuccessListener(this::updateUserProfile)
                            .addOnFailureListener(e -> {
                                loadingIndicator.setVisibility(View.GONE);
                                Log.e(TAG, "Failed to get download URL.", e);
                                Toast.makeText(ProfileActivity.this, "Error: Could not get image URL after upload. Check Storage Rules.", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Log.e(TAG, "Upload itself failed.", e);
                    Toast.makeText(ProfileActivity.this, "Error: Image upload failed. Check Storage Rules and network.", Toast.LENGTH_LONG).show();
                });
    }

    private void updateUserProfile(Uri downloadUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUri)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    loadingIndicator.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Glide.with(this).load(downloadUri).into(profileImageView);
                        Toast.makeText(ProfileActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Profile update failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

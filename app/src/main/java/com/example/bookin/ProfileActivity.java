package com.example.bookin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bookin.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = "ProfileActivity";
    private ActivityProfileBinding binding;

    // IMPORTANT: Make sure this name exactly matches the unsigned upload preset in your Cloudinary dashboard.
    private final String UPLOAD_PRESET = "bookin_unsigned";

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadImageToCloudinary(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupBottomNavigationBar();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Load current user data
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(binding.profileImageView);
            }
            binding.profileUsername.setText(user.getDisplayName());
            binding.profileEmail.setText(user.getEmail());
        }

        binding.profileImageView.setOnClickListener(v -> mGetContent.launch("image/*"));

        binding.logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        binding.profileLoadingIndicator.setVisibility(View.VISIBLE);

        MediaManager.get().upload(imageUri).unsigned(UPLOAD_PRESET).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d(TAG, "Cloudinary upload started");
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {}

            @Override
            public void onSuccess(String requestId, Map resultData) {
                String secureUrl = (String) resultData.get("secure_url");
                if (secureUrl != null) {
                    updateUserProfile(Uri.parse(secureUrl));
                } else {
                    binding.profileLoadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Upload failed: URL not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                binding.profileLoadingIndicator.setVisibility(View.GONE);
                Log.e(TAG, "Cloudinary upload error: " + error.getDescription());
                Toast.makeText(ProfileActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {}
        }).dispatch();
    }

    private void updateUserProfile(Uri downloadUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUri)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            binding.profileLoadingIndicator.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Glide.with(ProfileActivity.this).load(downloadUri).into(binding.profileImageView);
                Toast.makeText(ProfileActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Firebase profile update failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

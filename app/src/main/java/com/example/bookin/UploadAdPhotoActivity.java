package com.example.bookin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bookin.databinding.ActivityUploadAdPhotoBinding;

import java.util.Map;

public class UploadAdPhotoActivity extends AppCompatActivity {

    private static final String TAG = "UploadAdPhotoActivity";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private ActivityUploadAdPhotoBinding binding;

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
        binding = ActivityUploadAdPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        if (categoryName != null) {
            binding.headerTitle.setText(categoryName);
        }

        binding.backButton.setOnClickListener(v -> finish());
        binding.uploadImageCard.setOnClickListener(v -> mGetContent.launch("image/*"));
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        // Show loading indicator
        binding.cameraIcon.setVisibility(View.GONE);
        binding.coverText.setVisibility(View.GONE);

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
                    Uri uploadedImageUri = Uri.parse(secureUrl);
                    binding.adImageView.setVisibility(View.VISIBLE);
                    Glide.with(UploadAdPhotoActivity.this).load(uploadedImageUri).into(binding.adImageView);
                    Toast.makeText(UploadAdPhotoActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    resetUploadUI();
                    Toast.makeText(UploadAdPhotoActivity.this, "Upload failed: URL not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                resetUploadUI();
                Log.e(TAG, "Cloudinary upload error: " + error.getDescription());
                Toast.makeText(UploadAdPhotoActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {}
        }).dispatch();
    }

    private void resetUploadUI() {
        binding.adImageView.setVisibility(View.GONE);
        binding.cameraIcon.setVisibility(View.VISIBLE);
        binding.coverText.setVisibility(View.VISIBLE);
    }
}

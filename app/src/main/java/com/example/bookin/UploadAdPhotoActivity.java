package com.example.bookin;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bookin.databinding.ActivityUploadAdPhotoBinding;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class UploadAdPhotoActivity extends AppCompatActivity {

    private static final String TAG = "UploadAdPhotoActivity";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_FRONT_IMAGE_URL = "extra_front_image_url";
    public static final String EXTRA_BACK_IMAGE_URL = "extra_back_image_url";

    private ActivityUploadAdPhotoBinding binding;

    private final String UPLOAD_PRESET = "bookin_post";
    private boolean isFrontImage;
    private Uri tempImageUri;
    private String categoryName;
    private String frontImageUrl;
    private String backImageUrl;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    startCrop(uri);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    startCrop(tempImageUri);
                }
            });

    private final ActivityResultLauncher<CropImageContractOptions> cropImageFront = registerForActivityResult(
            new CropImageContract(),
            result -> {
                if (result.isSuccessful()) {
                    uploadImageToCloudinary(result.getUriContent(), true);
                }
            });

    private final ActivityResultLauncher<CropImageContractOptions> cropImageBack = registerForActivityResult(
            new CropImageContract(),
            result -> {
                if (result.isSuccessful()) {
                    uploadImageToCloudinary(result.getUriContent(), false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadAdPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        if (categoryName != null) {
            binding.headerTitle.setText(categoryName);
        }

        binding.backButton.setOnClickListener(v -> finish());
        binding.uploadImageCardFront.setOnClickListener(v -> {
            isFrontImage = true;
            showImageSourceDialog();
        });
        binding.uploadImageCardBack.setOnClickListener(v -> {
            isFrontImage = false;
            showImageSourceDialog();
        });

        binding.continueButton.setOnClickListener(v -> {
            if (frontImageUrl == null || frontImageUrl.isEmpty()) {
                Toast.makeText(this, "Harap unggah setidaknya sampul depan", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(UploadAdPhotoActivity.this, UploadAdDetailsActivity.class);
            intent.putExtra(UploadAdDetailsActivity.EXTRA_CATEGORY_NAME, categoryName);
            intent.putExtra(UploadAdDetailsActivity.EXTRA_FRONT_IMAGE_URL, frontImageUrl);
            intent.putExtra(UploadAdDetailsActivity.EXTRA_BACK_IMAGE_URL, backImageUrl);
            startActivity(intent);
        });
    }

    private void showImageSourceDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image_source);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        dialog.findViewById(R.id.gallery_button).setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
            dialog.dismiss();
        });

        dialog.findViewById(R.id.camera_button).setOnClickListener(v -> {
            openCamera();
            dialog.dismiss();
        });
    }

    private void openCamera() {
        try {
            File tempFile = File.createTempFile("temp_image", ".jpg", getCacheDir());
            tempImageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider",
                    tempFile);
            cameraLauncher.launch(tempImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCrop(Uri uri) {
        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;
        options.aspectRatioX = 3;
        options.aspectRatioY = 4;
        options.fixAspectRatio = true;

        CropImageContractOptions contractOptions = new CropImageContractOptions(uri, options);

        if (isFrontImage) {
            cropImageFront.launch(contractOptions);
        } else {
            cropImageBack.launch(contractOptions);
        }
    }

    private void uploadImageToCloudinary(Uri imageUri, boolean isFront) {
        if (imageUri == null)
            return;

        if (isFront) {
            binding.cameraIconFront.setVisibility(View.GONE);
        } else {
            binding.cameraIconBack.setVisibility(View.GONE);
        }

        MediaManager.get().upload(imageUri).unsigned(UPLOAD_PRESET).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d(TAG, "Cloudinary upload started");
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                String secureUrl = (String) resultData.get("secure_url");
                if (secureUrl != null) {
                    Uri uploadedImageUri = Uri.parse(secureUrl);
                    if (isFront) {
                        frontImageUrl = secureUrl;
                        binding.adImageViewFront.setVisibility(View.VISIBLE);
                        Glide.with(UploadAdPhotoActivity.this).load(uploadedImageUri).into(binding.adImageViewFront);
                    } else {
                        backImageUrl = secureUrl;
                        binding.adImageViewBack.setVisibility(View.VISIBLE);
                        Glide.with(UploadAdPhotoActivity.this).load(uploadedImageUri).into(binding.adImageViewBack);
                    }
                    Toast.makeText(UploadAdPhotoActivity.this, "Gambar berhasil diunggah!", Toast.LENGTH_SHORT).show();
                } else {
                    resetUploadUI(isFront);
                    Toast.makeText(UploadAdPhotoActivity.this, "Gagal mengunggah: URL tidak ditemukan.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                resetUploadUI(isFront);
                Log.e(TAG, "Cloudinary upload error: " + error.getDescription());
                Toast.makeText(UploadAdPhotoActivity.this, "Gagal mengunggah: " + error.getDescription(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
            }
        }).dispatch();
    }

    private void resetUploadUI(boolean isFront) {
        if (isFront) {
            binding.adImageViewFront.setVisibility(View.GONE);
            binding.cameraIconFront.setVisibility(View.VISIBLE);
        } else {
            binding.adImageViewBack.setVisibility(View.GONE);
            binding.cameraIconBack.setVisibility(View.VISIBLE);
        }
    }
}

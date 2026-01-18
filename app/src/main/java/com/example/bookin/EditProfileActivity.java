package com.example.bookin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bookin.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private ActivityEditProfileBinding binding;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private String currentPhotoUrl;

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
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        setupUI();
        loadUserData();
    }

    private void setupUI() {
        binding.closeButton.setOnClickListener(v -> finish());
        binding.saveButton.setOnClickListener(v -> saveProfile());
        binding.editPhotoButton.setOnClickListener(v -> mGetContent.launch("image/*"));
    }

    private void loadUserData() {
        // Load Firebase Auth data
        if (currentUser.getDisplayName() != null) {
            binding.nameInput.setText(currentUser.getDisplayName());
        }
        if (currentUser.getEmail() != null) {
            binding.emailInput.setText(currentUser.getEmail());
        }
        if (currentUser.getPhotoUrl() != null) {
            currentPhotoUrl = currentUser.getPhotoUrl().toString();
            Glide.with(this).load(currentPhotoUrl).into(binding.profileImage);
        }

        // Load Realtime Database data
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String bio = snapshot.child("bio").getValue(String.class);
                    String phone = snapshot.child("phoneNumber").getValue(String.class);

                    if (bio != null) {
                        binding.bioInput.setText(bio);
                    }
                    if (phone != null) {
                        binding.phoneInput.setText(phone);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load user data: " + error.getMessage());
                Toast.makeText(EditProfileActivity.this, "Gagal memuat data profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = binding.nameInput.getText().toString().trim();
        String bio = binding.bioInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();

        // Validate name (required)
        if (TextUtils.isEmpty(name)) {
            binding.nameInput.setError("Nama tidak boleh kosong");
            binding.nameInput.requestFocus();
            return;
        }

        // Show progress (disable save button)
        binding.saveButton.setEnabled(false);
        binding.saveButton.setText("Menyimpan...");

        // Update Firebase Auth display name
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Save bio and phone to Realtime Database
                userRef.child("bio").setValue(bio);
                userRef.child("phoneNumber").setValue(phone).addOnCompleteListener(dbTask -> {
                    binding.saveButton.setEnabled(true);
                    binding.saveButton.setText("Simpan");

                    if (dbTask.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Gagal menyimpan data kontak", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            } else {
                binding.saveButton.setEnabled(true);
                binding.saveButton.setText("Simpan");
                Toast.makeText(EditProfileActivity.this, "Gagal memperbarui nama", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.editPhotoButton.setEnabled(false);

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
                    updateUserPhoto(Uri.parse(secureUrl));
                } else {
                    binding.loadingIndicator.setVisibility(View.GONE);
                    binding.editPhotoButton.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Gagal mengunggah: URL tidak ditemukan.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                binding.loadingIndicator.setVisibility(View.GONE);
                binding.editPhotoButton.setEnabled(true);
                Log.e(TAG, "Cloudinary upload error: " + error.getDescription());
                Toast.makeText(EditProfileActivity.this, "Gagal mengunggah: " + error.getDescription(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
            }
        }).dispatch();
    }

    private void updateUserPhoto(Uri downloadUri) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUri)
                .build();

        currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.editPhotoButton.setEnabled(true);

            if (task.isSuccessful()) {
                currentPhotoUrl = downloadUri.toString();
                Glide.with(EditProfileActivity.this).load(downloadUri).into(binding.profileImage);
                Toast.makeText(EditProfileActivity.this, "Foto profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditProfileActivity.this, "Gagal memperbarui foto profil.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.example.bookin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bookin.databinding.ActivityProfileBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = "ProfileActivity";
    private ActivityProfileBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // IMPORTANT: Make sure this name exactly matches the unsigned upload preset in
    // your Cloudinary dashboard.
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadUserProfile();

        binding.profileImageView.setOnClickListener(v -> mGetContent.launch("image/*"));

        binding.editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        binding.helpCenterItem.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HelpCenterActivity.class);
            startActivity(intent);
        });

        binding.wishlistItem.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WishlistActivity.class);
            startActivity(intent);
        });

        binding.myReviewsItem.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyReviewsActivity.class);
            startActivity(intent);
        });

        binding.logoutButton.setOnClickListener(v -> showLogoutConfirmDialog());

        fetchLocation();
    }

    private void showLogoutConfirmDialog() {
        // Create dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout_confirm, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Set up buttons
        com.google.android.material.button.MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);
        com.google.android.material.button.MaterialButton logoutButton = dialogView
                .findViewById(R.id.logout_button_confirm);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        logoutButton.setOnClickListener(v -> {
            dialog.dismiss();

            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Clear any local data if needed
            Toast.makeText(ProfileActivity.this, "Berhasil keluar", Toast.LENGTH_SHORT).show();

            // Redirect to login page
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Load current user data from Firebase Auth
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(binding.profileImageView);
            }
            binding.profileUsername.setText(user.getDisplayName());

            // Load bio from Realtime Database
            com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("bio")
                    .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(
                                @androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            String bio = snapshot.getValue(String.class);
                            if (bio != null && !bio.isEmpty()) {
                                binding.profileSubtitle.setText(bio);
                            }
                        }

                        @Override
                        public void onCancelled(
                                @androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                            Log.e(TAG, "Failed to load bio: " + error.getMessage());
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile(); // Refresh profile when returning from EditProfileActivity
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        binding.profileLoadingIndicator.setVisibility(View.VISIBLE);

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
                    updateUserProfile(Uri.parse(secureUrl));
                } else {
                    binding.profileLoadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Gagal mengunggah: URL tidak ditemukan.", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                binding.profileLoadingIndicator.setVisibility(View.GONE);
                Log.e(TAG, "Cloudinary upload error: " + error.getDescription());
                Toast.makeText(ProfileActivity.this, "Gagal mengunggah: " + error.getDescription(), Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
            }
        }).dispatch();
    }

    private void updateUserProfile(Uri downloadUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUri)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            binding.profileLoadingIndicator.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Glide.with(ProfileActivity.this).load(downloadUri).into(binding.profileImageView);
                Toast.makeText(ProfileActivity.this, "Gambar profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Gagal memperbarui profil Firebase.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                                        location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    String city = addresses.get(0).getLocality();
                                    binding.userLocation.setText(city != null ? city : "Lokasi tidak ditemukan");
                                } else {
                                    binding.userLocation.setText("Lokasi tidak ditemukan");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                binding.userLocation.setText("Layanan lokasi tidak tersedia");
                            }
                        } else {
                            binding.userLocation.setText("Gagal mendapatkan lokasi");
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show();
                binding.userLocation.setText("Izin lokasi ditolak");
            }
        }
    }
}

package com.example.bookin;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookin.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "CreateAccountActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        EditText usernameEditText = findViewById(R.id.username_edit_text);
        EditText emailEditText = findViewById(R.id.email_edit_text);
        EditText passwordEditText = findViewById(R.id.password_edit_text);
        MaterialButton createAccountButton = findViewById(R.id.continue_button);

        createAccountButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(CreateAccountActivity.this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            if (firebaseUser != null) {
                                // Update FirebaseAuth profile with display name
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();

                                firebaseUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");

                                                // Save user data to Realtime Database with creation timestamp
                                                saveUserToDatabase(firebaseUser.getUid(), username, email);
                                            }
                                        });
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            String errorMessage = "Autentikasi gagal.";
                            if (task.getException() != null) {
                                errorMessage = task.getException().getMessage();
                            }
                            Toast.makeText(CreateAccountActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        MaterialButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void saveUserToDatabase(String userId, String username, String email) {
        long currentTime = System.currentTimeMillis();

        // Create User object with creation timestamp
        User user = new User(
                userId,
                username,
                email,
                "", // phone - can be added later
                "", // profileImage - can be added later
                "", // bio - can be added later
                currentTime, // createdAt
                currentTime // lastLoginAt
        );

        // Save to Firebase Realtime Database
        usersRef.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User data saved to database");
                    } else {
                        Log.e(TAG, "Failed to save user data", task.getException());
                    }

                    // Sign out and show success dialog regardless
                    FirebaseAuth.getInstance().signOut();
                    showSuccessDialog();
                });
    }

    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
        dialogMessage.setText("Pembuatan Akun Berhasil!\nSilakan login kembali untuk mengakses aplikasi Bookin");

        MaterialButton dialogButton = dialog.findViewById(R.id.dialog_button);
        dialogButton.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}

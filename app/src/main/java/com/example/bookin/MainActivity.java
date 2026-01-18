package com.example.bookin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private ActivityResultLauncher<IntentSenderRequest> googleSignInLauncher;
    private CardView loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        loadingIndicator = findViewById(R.id.loading_indicator);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateLastLoginTime(currentUser.getUid());
            navigateToHome();
        }

        EditText emailEditText = findViewById(R.id.email_edit_text);
        EditText passwordEditText = findViewById(R.id.password_edit_text);

        // Email/Password Sign-In
        MaterialButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Email dan kata sandi tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            showLoading();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        hideLoading();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                updateLastLoginTime(user.getUid());
                            }
                            navigateToHome();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Autentikasi gagal.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        showLoading();
                        try {
                            SignInCredential credential = Identity.getSignInClient(this)
                                    .getSignInCredentialFromIntent(result.getData());
                            AuthCredential googleAuthCredential = GoogleAuthProvider
                                    .getCredential(credential.getGoogleIdToken(), null);
                            mAuth.signInWithCredential(googleAuthCredential)
                                    .addOnCompleteListener(this, task -> {
                                        hideLoading();
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "signInWithGoogle:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                                // For Google sign-in, also create/update user in database
                                                createOrUpdateGoogleUser(user);
                                            }
                                            navigateToHome();
                                        } else {
                                            Log.w(TAG, "signInWithGoogle:failure", task.getException());
                                            Toast.makeText(MainActivity.this, "Gagal masuk dengan Google.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } catch (ApiException e) {
                            hideLoading();
                            Log.w(TAG, "Google sign in failed", e);
                        }
                    }
                });

        MaterialButton googleSignInButton = findViewById(R.id.google_sign_up_button);
        googleSignInButton.setOnClickListener(v -> {
            showLoading();
            GetSignInIntentRequest request = GetSignInIntentRequest.builder()
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .build();

            Identity.getSignInClient(this).getSignInIntent(request)
                    .addOnSuccessListener(result -> {
                        hideLoading();
                        googleSignInLauncher.launch(new IntentSenderRequest.Builder(result.getIntentSender()).build());
                    })
                    .addOnFailureListener(e -> {
                        hideLoading();
                        Log.e(TAG, "Google Sign-In failed to launch", e);
                    });
        });

        TextView createAccount = findViewById(R.id.create_account);
        createAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void updateLastLoginTime(String userId) {
        usersRef.child(userId).child("lastLoginAt").setValue(System.currentTimeMillis());
    }

    private void createOrUpdateGoogleUser(FirebaseUser user) {
        long currentTime = System.currentTimeMillis();

        // Update basic info for Google users
        usersRef.child(user.getUid()).child("name").setValue(user.getDisplayName());
        usersRef.child(user.getUid()).child("email").setValue(user.getEmail());
        usersRef.child(user.getUid()).child("lastLoginAt").setValue(currentTime);

        // Set createdAt only if it doesn't exist (first time login)
        usersRef.child(user.getUid()).child("createdAt").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().getValue() == null) {
                usersRef.child(user.getUid()).child("createdAt").setValue(currentTime);
            }
        });

        // Set profile image from Google if available
        if (user.getPhotoUrl() != null) {
            usersRef.child(user.getUid()).child("profileImage").setValue(user.getPhotoUrl().toString());
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoading() {
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loadingIndicator.setVisibility(View.GONE);
    }
}

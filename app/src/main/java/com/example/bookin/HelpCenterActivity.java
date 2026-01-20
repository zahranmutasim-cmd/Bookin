package com.example.bookin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class HelpCenterActivity extends BaseActivity {

    private static final String CS_PHONE_NUMBER = "+6282179575502";
    private static final String CS_EMAIL = "bookin26@gmail.com";

    private CircleImageView profileImage;
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);
        setupBottomNavigationBar();

        initViews();
        loadUserProfile();
        setupClickListeners();

        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void initViews() {
        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.user_name);
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String imageUrl = snapshot.child("profileImage").getValue(String.class);

                    if (name != null && !name.isEmpty()) {
                        userName.setText(name);
                    } else if (user.getDisplayName() != null) {
                        userName.setText(user.getDisplayName());
                    }

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(HelpCenterActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.default_profile)
                                .into(profileImage);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Use display name as fallback
                    if (user.getDisplayName() != null) {
                        userName.setText(user.getDisplayName());
                    }
                }
            });
        }
    }

    private void setupClickListeners() {
        // Pengaduan Saya - Open My Reports Activity
        CardView cardPengaduan = findViewById(R.id.card_pengaduan_saya);
        cardPengaduan.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyReportsActivity.class);
            startActivity(intent);
        });

        // Contact Email - Open email app
        CardView cardEmail = findViewById(R.id.card_contact_email);
        cardEmail.setOnClickListener(v -> openEmailApp());

        // Contact CS - Show dialog
        CardView cardCS = findViewById(R.id.card_contact_cs);
        cardCS.setOnClickListener(v -> showContactDialog());

        // Settings icon
        ImageView settingsIcon = findViewById(R.id.settings_icon);
        settingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private String getPrewrittenMessage() {
        String name = userName.getText().toString();
        if (name.isEmpty() || name.equals("Nama Pengguna")) {
            name = "Pengguna Bookin";
        }
        return "Hai, Saya " + name
                + ". Saya ingin menghubungi Customer Service Bookin untuk mendapatkan bantuan terkait aplikasi. Terima kasih.";
    }

    private void openEmailApp() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + CS_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Bantuan Bookin");
        intent.putExtra(Intent.EXTRA_TEXT, getPrewrittenMessage());

        try {
            startActivity(Intent.createChooser(intent, "Pilih aplikasi email"));
        } catch (Exception e) {
            Toast.makeText(this, "Tidak ada aplikasi email yang tersedia", Toast.LENGTH_SHORT).show();
        }
    }

    private void showContactDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_contact_cs);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialButton btnPhone = dialog.findViewById(R.id.btn_call_phone);
        MaterialButton btnWhatsApp = dialog.findViewById(R.id.btn_whatsapp);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);

        btnPhone.setOnClickListener(v -> {
            dialog.dismiss();
            makePhoneCall();
        });

        btnWhatsApp.setOnClickListener(v -> {
            dialog.dismiss();
            openWhatsApp();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void makePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + CS_PHONE_NUMBER));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Tidak dapat melakukan panggilan", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWhatsApp() {
        String message = getPrewrittenMessage();
        String encodedMessage = Uri.encode(message);
        String url = "https://wa.me/" + CS_PHONE_NUMBER.replace("+", "") + "?text=" + encodedMessage;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp tidak tersedia", Toast.LENGTH_SHORT).show();
        }
    }
}

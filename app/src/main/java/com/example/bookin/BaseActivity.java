package com.example.bookin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavigationBar() {
        LinearLayout homeButton = findViewById(R.id.nav_home);
        LinearLayout chatButton = findViewById(R.id.nav_chat);
        LinearLayout addButton = findViewById(R.id.nav_add);
        LinearLayout adsButton = findViewById(R.id.nav_ads);
        LinearLayout profileButton = findViewById(R.id.nav_profile);

        homeButton.setOnClickListener(v -> {
            if (!(this instanceof HomeActivity)) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        chatButton.setOnClickListener(v -> {
            if (!(this instanceof ChatActivity)) {
                startActivity(new Intent(this, ChatActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        addButton.setOnClickListener(v -> {
            startActivity(new Intent(this, PostAdActivity.class));
            overridePendingTransition(0, 0);
        });

        adsButton.setOnClickListener(v -> {
            if (!(this instanceof AdsActivity)) {
                startActivity(new Intent(this, AdsActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        profileButton.setOnClickListener(v -> {
            if (!(this instanceof ProfileActivity)) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }
}

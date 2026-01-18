package com.example.bookin;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

        setAnimatedClickListener(homeButton, HomeActivity.class);
        setAnimatedClickListener(chatButton, ChatActivity.class);
        setAnimatedClickListener(adsButton, AdsActivity.class);
        setAnimatedClickListener(profileButton, ProfileActivity.class);

        addButton.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                startActivity(new Intent(this, PostAdActivity.class));
            }).start();
        });

        updateNavigationBarState();
    }

    private void setAnimatedClickListener(View view, Class<?> cls) {
        view.setOnClickListener(v -> {
            if (!this.getClass().equals(cls)) {
                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150);
                    Intent intent = new Intent(this, cls);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }).start();
            } else {
                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                }).start();
            }
        });
    }

    private void updateNavigationBarState() {
        ImageView homeIcon = findViewById(R.id.nav_home_icon);
        TextView homeText = findViewById(R.id.nav_home_text);
        ImageView chatIcon = findViewById(R.id.nav_chat_icon);
        TextView chatText = findViewById(R.id.nav_chat_text);
        ImageView adsIcon = findViewById(R.id.nav_ads_icon);
        TextView adsText = findViewById(R.id.nav_ads_text);
        ImageView profileIcon = findViewById(R.id.nav_profile_icon);
        TextView profileText = findViewById(R.id.nav_profile_text);

        int gray = ContextCompat.getColor(this, R.color.gray_medium);
        homeIcon.setColorFilter(gray, PorterDuff.Mode.SRC_IN);
        homeText.setTextColor(gray);
        chatIcon.setColorFilter(gray, PorterDuff.Mode.SRC_IN);
        chatText.setTextColor(gray);
        adsIcon.setColorFilter(gray, PorterDuff.Mode.SRC_IN);
        adsText.setTextColor(gray);
        profileIcon.setColorFilter(gray, PorterDuff.Mode.SRC_IN);
        profileText.setTextColor(gray);

        int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);

        if (this instanceof HomeActivity) {
            homeIcon.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
            homeText.setTextColor(primaryColor);
        } else if (this instanceof ChatActivity) {
            chatIcon.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
            chatText.setTextColor(primaryColor);
        } else if (this instanceof AdsActivity) {
            adsIcon.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
            adsText.setTextColor(primaryColor);
        } else if (this instanceof ProfileActivity) {
            profileIcon.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
            profileText.setTextColor(primaryColor);
        }
    }
}

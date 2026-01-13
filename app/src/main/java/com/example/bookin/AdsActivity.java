package com.example.bookin;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

public class AdsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);
        setupBottomNavigationBar();

        // Change the color of the ads icon
        ImageView adsIcon = findViewById(R.id.nav_ads_icon);
        if (adsIcon != null) {
            adsIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }
    }
}

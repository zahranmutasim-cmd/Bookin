package com.example.bookin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 2500; // Delay for 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the Introduction Activity
                Intent mainIntent = new Intent(SplashActivity.this, IntroductionActivity.class);
                startActivity(mainIntent);
                finish(); // Close the splash activity
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}

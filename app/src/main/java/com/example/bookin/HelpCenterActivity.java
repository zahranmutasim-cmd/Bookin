package com.example.bookin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookin.databinding.ActivityHelpCenterBinding;

public class HelpCenterActivity extends AppCompatActivity {

    private ActivityHelpCenterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpCenterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v -> finish());
    }
}

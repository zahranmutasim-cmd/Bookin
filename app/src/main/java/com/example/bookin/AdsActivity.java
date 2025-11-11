package com.example.bookin;

import android.os.Bundle;

public class AdsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);
        setupBottomNavigationBar();
    }
}

package com.example.bookin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class FullScreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URLS = "extra_image_urls";
    public static final String EXTRA_CURRENT_POSITION = "extra_current_position";

    private ViewPager2 viewPager;
    private TextView pageIndicator;
    private List<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        // Get data from intent
        imageUrls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
        int currentPosition = getIntent().getIntExtra(EXTRA_CURRENT_POSITION, 0);

        if (imageUrls == null || imageUrls.isEmpty()) {
            finish();
            return;
        }

        // Initialize views
        viewPager = findViewById(R.id.fullscreen_view_pager);
        pageIndicator = findViewById(R.id.page_indicator);
        ImageView closeButton = findViewById(R.id.close_button);

        // Setup adapter
        FullScreenImageAdapter adapter = new FullScreenImageAdapter(imageUrls);
        viewPager.setAdapter(adapter);

        // Set initial position
        viewPager.setCurrentItem(currentPosition, false);
        updatePageIndicator(currentPosition);

        // Page change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updatePageIndicator(position);
            }
        });

        // Close button
        closeButton.setOnClickListener(v -> finish());
    }

    private void updatePageIndicator(int position) {
        pageIndicator.setText((position + 1) + " / " + imageUrls.size());
    }
}

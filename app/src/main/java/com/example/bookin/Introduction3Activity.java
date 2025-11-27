package com.example.bookin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Introduction3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduction3_pages);

        TextView arrowLeft = findViewById(R.id.arrow_left_3);
        TextView arrowRight = findViewById(R.id.arrow_right_3);

        // Navigate back to the second introduction page
        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Introduction3Activity.this, Introduction2Activity.class);
                startActivity(intent);
                finish(); // Close this activity
            }
        });

        // Navigate to the main login page (MainActivity)
        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Introduction3Activity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close this activity to prevent going back to intros
            }
        });
    }
}

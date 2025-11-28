package com.example.bookin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Introduction2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FIX: Corrected the layout file name to the proper one without typos.
        setContentView(R.layout.introdution2_pages);

        TextView arrowLeft = findViewById(R.id.arrow_left_2);
        TextView arrowRight = findViewById(R.id.arrow_right_2);

        // LOGIC KIRI: Kembali ke halaman Introduction 1
        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Introduction2Activity.this, IntroductionActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // LOGIC KANAN: Navigasi ke Introduction3Activity
        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Introduction2Activity.this, Introduction3Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });
    }
}

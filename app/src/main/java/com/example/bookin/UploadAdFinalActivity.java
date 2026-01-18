package com.example.bookin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadAdFinalActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_FRONT_IMAGE_URL = "extra_front_image_url";
    public static final String EXTRA_BACK_IMAGE_URL = "extra_back_image_url";
    public static final String EXTRA_SELECTED_TYPE = "extra_selected_type";
    public static final String EXTRA_CONDITION = "extra_condition";
    public static final String EXTRA_LOCATION = "extra_location";

    private String categoryName, frontImageUrl, backImageUrl, selectedType, condition, location;
    private EditText titleInput, descriptionInput, priceInput;
    private View priceInputLayout, gratisInfoCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_ad_final);

        // Get data from previous activity
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        frontImageUrl = getIntent().getStringExtra(EXTRA_FRONT_IMAGE_URL);
        backImageUrl = getIntent().getStringExtra(EXTRA_BACK_IMAGE_URL);
        selectedType = getIntent().getStringExtra(EXTRA_SELECTED_TYPE);
        condition = getIntent().getStringExtra(EXTRA_CONDITION);
        location = getIntent().getStringExtra(EXTRA_LOCATION);

        // Initialize views
        TextView headerTitle = findViewById(R.id.header_title);
        ImageView backButton = findViewById(R.id.back_button);
        Button continueButton = findViewById(R.id.continue_button);
        titleInput = findViewById(R.id.title_edit_text);
        descriptionInput = findViewById(R.id.description_edit_text);
        priceInput = findViewById(R.id.price_edit_text);
        priceInputLayout = findViewById(R.id.price_input_layout);
        gratisInfoCard = findViewById(R.id.gratis_info_card);

        // Set header title
        if (categoryName != null) {
            headerTitle.setText(categoryName);
            // Show/hide price input based on category
            if (categoryName.contains("Gratis")) {
                priceInputLayout.setVisibility(View.GONE);
                gratisInfoCard.setVisibility(View.VISIBLE);
            } else {
                priceInputLayout.setVisibility(View.VISIBLE);
                gratisInfoCard.setVisibility(View.GONE);
            }
        }

        backButton.setOnClickListener(v -> finish());

        continueButton.setOnClickListener(v -> {
            // Validate inputs
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Harap isi judul iklan", Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) {
                Toast.makeText(this, "Harap isi keterangan iklan", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get price (0 for Gratis categories)
            long price = 0;
            if (categoryName != null && !categoryName.contains("Gratis")) {
                String priceStr = priceInput.getText().toString().trim();
                if (!priceStr.isEmpty()) {
                    try {
                        price = Long.parseLong(priceStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Harga tidak valid", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            // Navigate to confirmation page
            Intent intent = new Intent(this, UploadAdConfirmActivity.class);
            intent.putExtra(UploadAdConfirmActivity.EXTRA_CATEGORY_NAME, categoryName);
            intent.putExtra(UploadAdConfirmActivity.EXTRA_FRONT_IMAGE_URL, frontImageUrl);
            intent.putExtra(UploadAdConfirmActivity.EXTRA_BACK_IMAGE_URL, backImageUrl);
            intent.putExtra(UploadAdConfirmActivity.EXTRA_SELECTED_TYPE, selectedType);
            intent.putExtra(UploadAdConfirmActivity.EXTRA_CONDITION, condition);
            intent.putExtra(UploadAdConfirmActivity.EXTRA_LOCATION, location);
            intent.putExtra(UploadAdConfirmActivity.EXTRA_TITLE, title);
            intent.putExtra(UploadAdConfirmActivity.EXTRA_DESCRIPTION, description);
            intent.putExtra(UploadAdConfirmActivity.EXTRA_PRICE, price);
            startActivity(intent);
        });
    }
}

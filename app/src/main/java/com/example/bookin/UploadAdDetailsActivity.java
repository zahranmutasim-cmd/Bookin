package com.example.bookin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UploadAdDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_FRONT_IMAGE_URL = "extra_front_image_url";
    public static final String EXTRA_BACK_IMAGE_URL = "extra_back_image_url";
    public static final String EXTRA_SELECTED_TYPE = "extra_selected_type";
    public static final String EXTRA_CONDITION = "extra_condition";
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private String categoryName;
    private String frontImageUrl;
    private String backImageUrl;
    private String currentLocation = "";
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private CategoryAdapterWithSelection categoryAdapter;

    private FusedLocationProviderClient fusedLocationClient;
    private TextView lokasiText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_ad_details);

        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        frontImageUrl = getIntent().getStringExtra(EXTRA_FRONT_IMAGE_URL);
        backImageUrl = getIntent().getStringExtra(EXTRA_BACK_IMAGE_URL);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        TextView headerTitle = findViewById(R.id.header_title);
        if (categoryName != null) {
            headerTitle.setText(categoryName);
        }

        RecyclerView tipeRecyclerView = findViewById(R.id.tipe_recycler_view);
        ImageView backButton = findViewById(R.id.back_button);
        View continueButton = findViewById(R.id.continue_button);
        RadioGroup kondisiGroup = findViewById(R.id.kondisi_radio_group);
        lokasiText = findViewById(R.id.lokasi_text);
        ImageView refreshLocationButton = findViewById(R.id.refresh_location_button);

        backButton.setOnClickListener(v -> finish());

        // Fetch location on start
        fetchLocation();

        // Refresh location button
        refreshLocationButton.setOnClickListener(v -> {
            lokasiText.setText("Mencari lokasi...");
            fetchLocation();
        });

        continueButton.setOnClickListener(v -> {
            // Get selected type
            String selectedType = categoryAdapter.getSelectedCategory();
            if (selectedType == null || selectedType.isEmpty()) {
                Toast.makeText(this, "Harap pilih tipe buku", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected condition
            int selectedConditionId = kondisiGroup.getCheckedRadioButtonId();
            if (selectedConditionId == -1) {
                Toast.makeText(this, "Harap pilih kondisi buku", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton selectedConditionButton = findViewById(selectedConditionId);
            String condition = selectedConditionButton.getText().toString();

            // Check location
            if (currentLocation.isEmpty() || currentLocation.equals("Mencari lokasi...")) {
                Toast.makeText(this, "Menunggu lokasi, harap coba lagi", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(UploadAdDetailsActivity.this, UploadAdFinalActivity.class);
            intent.putExtra(UploadAdFinalActivity.EXTRA_CATEGORY_NAME, categoryName);
            intent.putExtra(UploadAdFinalActivity.EXTRA_FRONT_IMAGE_URL, frontImageUrl);
            intent.putExtra(UploadAdFinalActivity.EXTRA_BACK_IMAGE_URL, backImageUrl);
            intent.putExtra(UploadAdFinalActivity.EXTRA_SELECTED_TYPE, selectedType);
            intent.putExtra(UploadAdFinalActivity.EXTRA_CONDITION, condition);
            intent.putExtra(UploadAdFinalActivity.EXTRA_LOCATION, currentLocation);
            intent.putExtra(UploadAdFinalActivity.EXTRA_LATITUDE, currentLatitude);
            intent.putExtra(UploadAdFinalActivity.EXTRA_LONGITUDE, currentLongitude);
            startActivity(intent);
        });

        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("Komik", R.drawable.comic_icon));
        categoryList.add(new Category("Novel", R.drawable.novel_icon));
        categoryList.add(new Category("Majalah", R.drawable.majalah_icon));
        categoryList.add(new Category("Buku Anak", R.drawable.buku_anak_icon));
        categoryList.add(new Category("Pelajaran", R.drawable.pelajaran_icon));
        categoryList.add(new Category("Keuangan", R.drawable.keuangan_icon));
        categoryList.add(new Category("Self-Improv.", R.drawable.self_improvement));
        categoryList.add(new Category("Kamus", R.drawable.kamus_bahasa_icon));
        categoryList.add(new Category("Cerita", R.drawable.buku_cerita_icon));
        categoryList.add(new Category("Buku Gratis", R.drawable.buku_gratis_icon));

        categoryAdapter = new CategoryAdapterWithSelection(categoryList);
        tipeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tipeRecyclerView.setAdapter(categoryAdapter);
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(
                                        location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    String subLocality = address.getSubLocality();
                                    String locality = address.getLocality();
                                    String adminArea = address.getAdminArea();

                                    StringBuilder locationBuilder = new StringBuilder();
                                    if (subLocality != null) {
                                        locationBuilder.append(subLocality);
                                    }
                                    if (locality != null) {
                                        if (locationBuilder.length() > 0)
                                            locationBuilder.append(", ");
                                        locationBuilder.append(locality);
                                    }
                                    if (adminArea != null) {
                                        if (locationBuilder.length() > 0)
                                            locationBuilder.append(", ");
                                        locationBuilder.append(adminArea);
                                    }

                                    currentLocation = locationBuilder.toString();
                                    currentLatitude = location.getLatitude();
                                    currentLongitude = location.getLongitude();
                                    lokasiText.setText(currentLocation);
                                } else {
                                    lokasiText.setText("Lokasi tidak ditemukan");
                                    currentLocation = "";
                                    currentLatitude = 0;
                                    currentLongitude = 0;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                lokasiText.setText("Layanan lokasi tidak tersedia");
                                currentLocation = "";
                            }
                        } else {
                            lokasiText.setText("Gagal mendapatkan lokasi");
                            currentLocation = "";
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show();
                lokasiText.setText("Izin lokasi ditolak");
            }
        }
    }
}

package com.example.bookin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends BaseActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationText;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupBottomNavigationBar();

        TextView greetingText = findViewById(R.id.greeting_text);
        ImageView profilePicture = findViewById(R.id.profile_picture);
        locationText = findViewById(R.id.location_text);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Set Greeting
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                greetingText.setText("Hai, " + displayName + "!");
            } else {
                String email = user.getEmail();
                if (email != null && !email.isEmpty()) {
                    greetingText.setText("Hai, " + email.split("@")[0] + "!");
                }
            }

            // Set Profile Picture
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(profilePicture);
            }
        }

        // Setup Category RecyclerView
        setupCategoryRecyclerView();

        // Setup Book RecyclerViews
        setupBookRecyclerView(R.id.popular_recycler_view, getSampleBookData());
        setupBookRecyclerView(R.id.latest_recycler_view, getSampleBookData());
        setupBookRecyclerView(R.id.nearby_recycler_view, getSampleBookData());

        // Get and display location
        fetchLocation();
    }

    private void setupCategoryRecyclerView() {
        RecyclerView categoryRecyclerView = findViewById(R.id.category_recycler_view);
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        // This data can also be moved to Firestore if you wish
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("Buku Gratis", R.drawable.buku_gratis_icon));
        categoryList.add(new Category("Pelajaran", R.drawable.pelajaran_icon));
        categoryList.add(new Category("Novel", R.drawable.novel_icon));
        categoryList.add(new Category("Comic", R.drawable.comic_icon));
        categoryList.add(new Category("Buku Cerita", R.drawable.buku_cerita_icon));
        categoryList.add(new Category("Kamus Bahasa", R.drawable.kamus_bahasa_icon));
        categoryList.add(new Category("Buku Anak", R.drawable.buku_anak_icon));
        categoryList.add(new Category("Majalah", R.drawable.majalah_icon));
        categoryList.add(new Category("Keuangan", R.drawable.keuangan_icon));
        categoryList.add(new Category("Self Improvment", R.drawable.self_improvement));
        CategoryAdapter categoryAdapter = new CategoryAdapter(categoryList);
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupBookRecyclerView(int recyclerViewId, List<Book> bookList) {
        RecyclerView bookRecyclerView = findViewById(recyclerViewId);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        BookAdapter bookAdapter = new BookAdapter(bookList);
        bookRecyclerView.setAdapter(bookAdapter);
    }

    private List<Book> getSampleBookData() {
        List<Book> bookList = new ArrayList<>();
        bookList.add(new Book("Bicara Itu Ada Seninya", "Termurah! Kualitas Gacor Anti Gedor...", "Rp 1.000.000", "Palembang Kota, Sumatera Selatan", R.drawable.buku1));
        bookList.add(new Book("Atomic Habits", "Perubahan Kecil yang Memberikan Hasil Luar Biasa", "Rp 95.000", "Jakarta Pusat", R.drawable.gambar4));
        bookList.add(new Book("Filosofi Teras", "Filsafat Yunani-Romawi Kuno untuk Mental Tangguh Masa Kini", "Rp 80.000", "Bandung", R.drawable.buku3));
        bookList.add(new Book("Sebuah Seni untuk Bersikap Bodo Amat", "Pendekatan yang Waras Demi Menjalani Hidup yang Baik", "Rp 75.000", "Surabaya", R.drawable.buku2));
        return bookList;
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    String address = addresses.get(0).getSubLocality();
                                    String city = addresses.get(0).getLocality();
                                    locationText.setText(address + ", " + city);
                                } else {
                                    locationText.setText("Lokasi tidak ditemukan");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                locationText.setText("Layanan lokasi tidak tersedia");
                            }
                        } else {
                            locationText.setText("Gagal mendapatkan lokasi");
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show();
                locationText.setText("Izin lokasi ditolak");
            }
        }
    }
}

package com.example.bookin;

import android.Manifest;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupBottomNavigationBar();

        TextView greetingText = findViewById(R.id.greeting_text);
        ImageView profilePicture = findViewById(R.id.profile_picture);

        // MapView initialization
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    } else {
                        // Default to Palembang if location not found
                        LatLng palembang = new LatLng(-2.976074, 104.775429);
                        googleMap.addMarker(new MarkerOptions().position(palembang).title("Palembang"));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(palembang, 12));
                        Toast.makeText(this, "Could not get current location, showing Palembang.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // Permission to access the location is missing. Request it.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                enableMyLocation();
            } else {
                // Permission was denied. Display a toast.
                Toast.makeText(this, "Location permission is required to show the map.", Toast.LENGTH_SHORT).show();
                 // Default to Palembang
                LatLng palembang = new LatLng(-2.976074, 104.775429);
                if (googleMap != null) {
                    googleMap.addMarker(new MarkerOptions().position(palembang).title("Palembang"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(palembang, 12));
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
}

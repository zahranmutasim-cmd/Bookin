package com.example.bookin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.bookin.models.Book;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends BaseActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationText;
    private EditText searchEditText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private DatabaseReference booksRef;
    private DatabaseReference usersRef;
    private FirebaseBookAdapter latestAdapter, popularAdapter, nearbyAdapter, searchAdapter;
    private List<Book> latestBooks, popularBooks, nearbyBooks, searchResults, allBooks;

    // User location for distance calculation
    private double userLatitude = 0;
    private double userLongitude = 0;

    // Views for search mode
    private View searchResultsSection;
    private RecyclerView searchRecyclerView;
    private View normalContentSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupBottomNavigationBar();

        TextView greetingText = findViewById(R.id.greeting_text);
        ImageView profilePicture = findViewById(R.id.profile_picture);
        locationText = findViewById(R.id.location_text);
        searchEditText = findViewById(R.id.search_edit_text);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        booksRef = FirebaseDatabase.getInstance().getReference("books");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Setup SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // Initialize all books list for search
        allBooks = new ArrayList<>();
        searchResults = new ArrayList<>();

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

        // Initialize book lists and adapters
        initializeBookRecyclerViews();

        // Setup search functionality
        setupSearchBar();

        // Fetch books from Firebase
        fetchBooksFromFirebase();

        // Get and display location
        fetchLocation();
    }

    private void setupSearchBar() {
        // Handle search on keyboard action
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });

        // Real-time search as user types
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // Show normal content when search is cleared
                    showNormalContent();
                } else if (query.length() >= 2) {
                    // Start searching after 2 characters
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void performSearch(String query) {
        searchResults.clear();
        String lowerQuery = query.toLowerCase();

        for (Book book : allBooks) {
            if (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerQuery)) {
                searchResults.add(book);
            }
        }

        // Update the latest section to show search results
        updateSearchResults();
    }

    private void updateSearchResults() {
        // Use the latest recycler view to display search results
        RecyclerView latestRecyclerView = findViewById(R.id.latest_recycler_view);
        TextView latestTitle = findViewById(R.id.latest_title);

        if (!searchResults.isEmpty()) {
            latestTitle.setText("Hasil Pencarian (" + searchResults.size() + ")");
            latestAdapter.updateBooks(searchResults);
        } else {
            latestTitle.setText("Tidak ditemukan");
            latestAdapter.updateBooks(new ArrayList<>());
        }

        // Hide other sections during search
        findViewById(R.id.popular_title).setVisibility(View.GONE);
        findViewById(R.id.see_all_popular).setVisibility(View.GONE);
        findViewById(R.id.popular_recycler_view).setVisibility(View.GONE);
        findViewById(R.id.nearby_title).setVisibility(View.GONE);
        findViewById(R.id.see_all_nearby).setVisibility(View.GONE);
        findViewById(R.id.nearby_recycler_view).setVisibility(View.GONE);
        findViewById(R.id.see_all_latest).setVisibility(View.GONE);
        findViewById(R.id.end_message).setVisibility(View.GONE);
    }

    private void showNormalContent() {
        // Restore normal section titles and visibility
        TextView latestTitle = findViewById(R.id.latest_title);
        latestTitle.setText("Terbaru");
        latestAdapter.updateBooks(latestBooks);

        // Show all sections
        findViewById(R.id.popular_title).setVisibility(View.VISIBLE);
        findViewById(R.id.see_all_popular).setVisibility(View.VISIBLE);
        findViewById(R.id.popular_recycler_view).setVisibility(View.VISIBLE);
        findViewById(R.id.nearby_title).setVisibility(View.VISIBLE);
        findViewById(R.id.see_all_nearby).setVisibility(View.VISIBLE);
        findViewById(R.id.nearby_recycler_view).setVisibility(View.VISIBLE);
        findViewById(R.id.see_all_latest).setVisibility(View.VISIBLE);
        findViewById(R.id.end_message).setVisibility(View.VISIBLE);
    }

    private void setupCategoryRecyclerView() {
        RecyclerView categoryRecyclerView = findViewById(R.id.category_recycler_view);
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
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

    private void initializeBookRecyclerViews() {
        // Initialize lists
        latestBooks = new ArrayList<>();
        popularBooks = new ArrayList<>();
        nearbyBooks = new ArrayList<>();

        // Latest books RecyclerView
        RecyclerView latestRecyclerView = findViewById(R.id.latest_recycler_view);
        latestRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        latestAdapter = new FirebaseBookAdapter(latestBooks);
        latestRecyclerView.setAdapter(latestAdapter);

        // Popular books RecyclerView
        RecyclerView popularRecyclerView = findViewById(R.id.popular_recycler_view);
        popularRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        popularAdapter = new FirebaseBookAdapter(popularBooks);
        popularRecyclerView.setAdapter(popularAdapter);

        // Nearby books RecyclerView
        RecyclerView nearbyRecyclerView = findViewById(R.id.nearby_recycler_view);
        nearbyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        nearbyAdapter = new FirebaseBookAdapter(nearbyBooks);
        nearbyRecyclerView.setAdapter(nearbyAdapter);
    }

    private void fetchBooksFromFirebase() {
        // Fetch all books for search functionality
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allBooks.clear();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
                        allBooks.add(book);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silently fail for all books fetch
            }
        });

        // Fetch latest books (ordered by timestamp, descending)
        Query latestQuery = booksRef.orderByChild("timestamp").limitToLast(10);
        latestQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                latestBooks.clear();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
                        latestBooks.add(book);
                    }
                }
                // Reverse to show newest first
                Collections.reverse(latestBooks);

                // Only update if not in search mode
                if (searchEditText.getText().toString().isEmpty()) {
                    latestAdapter.updateBooks(latestBooks);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Gagal memuat buku terbaru", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch popular/featured books
        Query popularQuery = booksRef.orderByChild("isFeatured").equalTo(true).limitToLast(10);
        popularQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                popularBooks.clear();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
                        popularBooks.add(book);
                    }
                }

                // If no featured books, show all books
                if (popularBooks.isEmpty()) {
                    fetchAllBooksForPopular();
                } else {
                    popularAdapter.updateBooks(popularBooks);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Gagal memuat buku populer", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch nearby books - sorted by distance to user
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nearbyBooks.clear();
                List<Book> allBooksWithLocation = new ArrayList<>();

                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null && book.getLatitude() != 0 && book.getLongitude() != 0) {
                        allBooksWithLocation.add(book);
                    }
                }

                // Sort by distance if user location is available
                if (userLatitude != 0 && userLongitude != 0) {
                    Collections.sort(allBooksWithLocation, (b1, b2) -> {
                        double dist1 = calculateDistance(userLatitude, userLongitude, b1.getLatitude(),
                                b1.getLongitude());
                        double dist2 = calculateDistance(userLatitude, userLongitude, b2.getLatitude(),
                                b2.getLongitude());
                        return Double.compare(dist1, dist2);
                    });
                }

                // Take top 10 nearest
                int limit = Math.min(10, allBooksWithLocation.size());
                nearbyBooks.addAll(allBooksWithLocation.subList(0, limit));
                nearbyAdapter.updateBooks(nearbyBooks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Gagal memuat buku terdekat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllBooksForPopular() {
        booksRef.limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                popularBooks.clear();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
                        popularBooks.add(book);
                    }
                }
                Collections.reverse(popularBooks);
                popularAdapter.updateBooks(popularBooks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silently fail
            }
        });
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                                        location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address addr = addresses.get(0);
                                    String subLocality = addr.getSubLocality();
                                    String city = addr.getLocality();
                                    String state = addr.getAdminArea();

                                    String locationString = "";
                                    if (subLocality != null)
                                        locationString = subLocality;
                                    if (city != null) {
                                        if (!locationString.isEmpty())
                                            locationString += ", ";
                                        locationString += city;
                                    }

                                    locationText.setText(locationString);

                                    // Store user coordinates for distance calculation
                                    userLatitude = location.getLatitude();
                                    userLongitude = location.getLongitude();

                                    // Refresh nearby books with new location
                                    fetchNearbyBooks();

                                    // Save location to Firebase for current user
                                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                    if (currentUser != null) {
                                        String fullLocation = locationString;
                                        if (state != null)
                                            fullLocation += ", " + state;
                                        usersRef.child(currentUser.getUid()).child("location").setValue(fullLocation);
                                    }
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

    private void refreshData() {
        // Refresh all data
        fetchBooksFromFirebase();
        fetchLocation();

        // Stop the refresh animation after a short delay
        swipeRefreshLayout.postDelayed(() -> {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show();
        }, 1500);
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
                locationText.setText("Izin lokasi ditolak");
            }
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * 
     * @return distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private void fetchNearbyBooks() {
        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nearbyBooks.clear();
                List<Book> allBooksWithLocation = new ArrayList<>();

                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null && book.getLatitude() != 0 && book.getLongitude() != 0) {
                        allBooksWithLocation.add(book);
                    }
                }

                // Sort by distance
                if (userLatitude != 0 && userLongitude != 0) {
                    Collections.sort(allBooksWithLocation, (b1, b2) -> {
                        double dist1 = calculateDistance(userLatitude, userLongitude, b1.getLatitude(),
                                b1.getLongitude());
                        double dist2 = calculateDistance(userLatitude, userLongitude, b2.getLatitude(),
                                b2.getLongitude());
                        return Double.compare(dist1, dist2);
                    });
                }

                int limit = Math.min(10, allBooksWithLocation.size());
                nearbyBooks.addAll(allBooksWithLocation.subList(0, limit));
                nearbyAdapter.updateBooks(nearbyBooks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silent fail
            }
        });
    }
}

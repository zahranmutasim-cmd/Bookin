package com.example.bookin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookin.models.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WishlistActivity extends AppCompatActivity {

    private static final String WISHLIST_PREFS = "wishlist_prefs";

    private RecyclerView wishlistRecyclerView;
    private LinearLayout emptyState;
    private ProgressBar loadingIndicator;
    private TextView wishlistCount;

    private FirebaseBookAdapter adapter;
    private List<Book> wishlistBooks;
    private DatabaseReference booksRef;
    private Set<String> wishlistIds;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        // Initialize views
        ImageView backButton = findViewById(R.id.back_button);
        wishlistRecyclerView = findViewById(R.id.wishlist_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        loadingIndicator = findViewById(R.id.loading_indicator);
        wishlistCount = findViewById(R.id.wishlist_count);

        // Initialize Firebase
        booksRef = FirebaseDatabase.getInstance().getReference("books");

        // Get current user ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user != null ? user.getUid() : null;

        // Initialize list and adapter
        wishlistBooks = new ArrayList<>();
        adapter = new FirebaseBookAdapter(wishlistBooks);
        wishlistRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        wishlistRecyclerView.setAdapter(adapter);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Load wishlist IDs from SharedPreferences
        loadWishlistIds();

        // Fetch wishlisted books from Firebase
        fetchWishlistBooks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload wishlist when returning to this activity
        loadWishlistIds();
        fetchWishlistBooks();
    }

    private String getWishlistKey() {
        // Return user-specific wishlist key
        if (currentUserId != null && !currentUserId.isEmpty()) {
            return "wishlist_" + currentUserId;
        }
        return "wishlist_anonymous";
    }

    private void loadWishlistIds() {
        SharedPreferences prefs = getSharedPreferences(WISHLIST_PREFS, MODE_PRIVATE);
        wishlistIds = new HashSet<>(prefs.getStringSet(getWishlistKey(), new HashSet<>()));
    }

    private void fetchWishlistBooks() {
        if (wishlistIds == null || wishlistIds.isEmpty()) {
            showEmptyState();
            return;
        }

        showLoading();

        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistBooks.clear();

                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null && wishlistIds.contains(book.getId())) {
                        wishlistBooks.add(book);
                    }
                }

                hideLoading();
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                showEmptyState();
            }
        });
    }

    private void updateUI() {
        if (wishlistBooks.isEmpty()) {
            showEmptyState();
        } else {
            emptyState.setVisibility(View.GONE);
            wishlistRecyclerView.setVisibility(View.VISIBLE);
            adapter.updateBooks(wishlistBooks);
            wishlistCount.setText(wishlistBooks.size() + " item");
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        wishlistRecyclerView.setVisibility(View.GONE);
        wishlistCount.setText("0 item");
    }

    private void showLoading() {
        loadingIndicator.setVisibility(View.VISIBLE);
        wishlistRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingIndicator.setVisibility(View.GONE);
    }
}

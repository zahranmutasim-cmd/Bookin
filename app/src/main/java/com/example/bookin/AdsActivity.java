package com.example.bookin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookin.models.Book;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdsActivity extends BaseActivity implements MyAdsAdapter.OnAdActionListener {

    private RecyclerView rvMyAds;
    private LinearLayout emptyStateContainer;
    private MaterialButton btnAddAd;
    
    private MyAdsAdapter adapter;
    private List<Book> myBooksList;
    
    private DatabaseReference booksRef;
    private FirebaseUser currentUser;
    private ValueEventListener booksListener;
    private Query myBooksQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);
        setupBottomNavigationBar();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        booksRef = FirebaseDatabase.getInstance().getReference("books");
        
        initializeViews();
        loadMyAds();
    }

    private void initializeViews() {
        rvMyAds = findViewById(R.id.rv_my_ads);
        emptyStateContainer = findViewById(R.id.empty_state_container);
        btnAddAd = findViewById(R.id.btn_add_ad);

        myBooksList = new ArrayList<>();
        adapter = new MyAdsAdapter(myBooksList, this);

        rvMyAds.setLayoutManager(new LinearLayoutManager(this));
        rvMyAds.setAdapter(adapter);

        // Button tambah iklan di empty state
        btnAddAd.setOnClickListener(v -> {
            Intent intent = new Intent(this, UploadAdPhotoActivity.class);
            startActivity(intent);
        });
    }

    private void loadMyAds() {
        // Query buku milik user saat ini
        myBooksQuery = booksRef.orderByChild("userId").equalTo(currentUser.getUid());
        
        booksListener = myBooksQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myBooksList.clear();
                
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    showEmptyState();
                    return;
                }

                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
                        book.setId(bookSnapshot.getKey());
                        
                        // Load favorite count for this book
                        loadFavoriteCount(book);
                        
                        myBooksList.add(book);
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(myBooksList, (a, b) -> 
                    Long.compare(b.getTimestamp(), a.getTimestamp()));

                hideEmptyState();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdsActivity.this, "Gagal memuat iklan", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void loadFavoriteCount(Book book) {
        FirebaseDatabase.getInstance().getReference("favorites")
            .orderByChild("bookId").equalTo(book.getId())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = (int) snapshot.getChildrenCount();
                    book.setFavoriteCount(count);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Silent fail
                }
            });
    }

    private void showEmptyState() {
        emptyStateContainer.setVisibility(View.VISIBLE);
        rvMyAds.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyStateContainer.setVisibility(View.GONE);
        rvMyAds.setVisibility(View.VISIBLE);
    }

    // ===== OnAdActionListener Implementation =====

    @Override
    public void onSoldStatusClick(Book book, int position) {
        boolean newStatus = !book.isSold();
        String statusText = newStatus ? "TERJUAL" : "BELUM TERJUAL";
        
        new AlertDialog.Builder(this)
            .setTitle("Ubah Status Iklan")
            .setMessage("Apakah Anda yakin ingin mengubah status menjadi \"" + statusText + "\"?")
            .setPositiveButton("Ya", (dialog, which) -> {
                // Update di Firebase
                booksRef.child(book.getId()).child("isSold").setValue(newStatus)
                    .addOnSuccessListener(aVoid -> {
                        book.setSold(newStatus);
                        adapter.updateSoldStatus(position, newStatus);
                        Toast.makeText(this, "Status berhasil diubah menjadi " + statusText, 
                            Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal mengubah status", Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    @Override
    public void onDeleteClick(Book book, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Hapus Iklan")
            .setMessage("Apakah Anda yakin ingin menghapus iklan \"" + book.getTitle() + "\"? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus", (dialog, which) -> {
                // Delete from Firebase
                booksRef.child(book.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        adapter.removeAd(position);
                        Toast.makeText(this, "Iklan berhasil dihapus", Toast.LENGTH_SHORT).show();
                        
                        if (myBooksList.isEmpty()) {
                            showEmptyState();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menghapus iklan", Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    @Override
    public void onViewClick(Book book) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myBooksQuery != null && booksListener != null) {
            myBooksQuery.removeEventListener(booksListener);
        }
    }
}

package com.example.bookin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookin.models.Book;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryBooksActivity extends BaseActivity {

    public static final String EXTRA_CATEGORY_NAME = "category_name";

    private RecyclerView rvBooks;
    private LinearLayout emptyStateContainer;
    private TextView categoryTitle;
    private TextView emptyMessage;

    private FirebaseBookAdapter adapter;
    private List<Book> bookList;
    private DatabaseReference booksRef;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_books);
        setupBottomNavigationBar();

        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        if (categoryName == null) {
            categoryName = getIntent().getStringExtra("category_name");
        }
        if (categoryName == null) {
            finish();
            return;
        }

        booksRef = FirebaseDatabase.getInstance().getReference("books");

        initializeViews();
        loadBooks();
    }

    private void initializeViews() {
        rvBooks = findViewById(R.id.rv_category_books);
        emptyStateContainer = findViewById(R.id.empty_state_container);
        categoryTitle = findViewById(R.id.category_title);
        emptyMessage = findViewById(R.id.empty_message);

        categoryTitle.setText(categoryName);
        emptyMessage.setText("Belum ada buku dalam kategori " + categoryName + ".");

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        bookList = new ArrayList<>();
        adapter = new FirebaseBookAdapter(bookList, book -> {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
            startActivity(intent);
        });

        rvBooks.setLayoutManager(new GridLayoutManager(this, 2));
        rvBooks.setAdapter(adapter);
    }

    private void loadBooks() {
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookList.clear();

                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
                        book.setId(bookSnapshot.getKey());

                        // Filter by category and not sold
                        if (!book.isSold() && matchesCategory(book)) {
                            bookList.add(book);
                        }
                    }
                }

                if (bookList.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    adapter.updateBooks(bookList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryBooksActivity.this,
                        "Gagal memuat buku", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean matchesCategory(Book book) {
        String bookCategory = book.getCategoryName();

        // Handle "Buku Gratis" - show all free books
        if (categoryName.equalsIgnoreCase("Buku Gratis")) {
            return book.getPrice() == 0;
        }

        // Check if category matches
        if (bookCategory != null && bookCategory.equalsIgnoreCase(categoryName)) {
            return true;
        }

        // Also check book type for some categories
        String bookType = book.getType();
        if (bookType != null) {
            if (categoryName.equalsIgnoreCase("Novel") && bookType.equalsIgnoreCase("Novel")) {
                return true;
            }
            if (categoryName.equalsIgnoreCase("Comic") &&
                    (bookType.equalsIgnoreCase("Comic") || bookType.equalsIgnoreCase("Komik"))) {
                return true;
            }
        }

        return false;
    }

    private void showEmptyState() {
        emptyStateContainer.setVisibility(View.VISIBLE);
        rvBooks.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyStateContainer.setVisibility(View.GONE);
        rvBooks.setVisibility(View.VISIBLE);
    }
}

package com.example.bookin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupBottomNavigationBar();

        TextView greetingText = findViewById(R.id.greeting_text);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                greetingText.setText("Hai, " + displayName + "!");
            } else {
                String email = user.getEmail();
                if (email != null && !email.isEmpty()) {
                    greetingText.setText("Hai, " + email.split("@")[0] + "!");
                }
            }
        }

        // Setup Category RecyclerView
        RecyclerView categoryRecyclerView = findViewById(R.id.category_recycler_view);
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));

        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("Buku Gratis", R.drawable.buku_gratis_icon));
        categoryList.add(new Category("Pelajaran", R.drawable.pelajaran_icon)); // Placeholder
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

        // Setup Book RecyclerViews
        setupBookRecyclerView(R.id.popular_recycler_view, getSampleBookData());
        setupBookRecyclerView(R.id.latest_recycler_view, getSampleBookData());
        setupBookRecyclerView(R.id.nearby_recycler_view, getSampleBookData());
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
}

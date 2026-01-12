package com.example.bookin;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PostAdActivity extends BaseActivity { // Corrected this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ad);

        ImageView closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());

        // Setup Category RecyclerView
        RecyclerView categoryRecyclerView = findViewById(R.id.category_recycler_view);
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

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
}

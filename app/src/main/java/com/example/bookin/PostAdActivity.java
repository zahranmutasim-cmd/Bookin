package com.example.bookin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PostAdActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ad);

        ImageView closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());

        // Setup Category RecyclerView
        RecyclerView categoryRecyclerView = findViewById(R.id.category_recycler_view);
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        List<Category> categoryList = getCategories();
        CategoryAdapter categoryAdapter = new CategoryAdapter(categoryList, this::onCategoryClicked);
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Buku Gratis", R.drawable.buku_gratis_icon));
        categories.add(new Category("Pelajaran", R.drawable.pelajaran_icon));
        categories.add(new Category("Novel", R.drawable.novel_icon));
        categories.add(new Category("Comic", R.drawable.comic_icon));
        categories.add(new Category("Buku Cerita", R.drawable.buku_cerita_icon));
        categories.add(new Category("Kamus Bahasa", R.drawable.kamus_bahasa_icon));
        categories.add(new Category("Buku Anak", R.drawable.buku_anak_icon));
        categories.add(new Category("Majalah", R.drawable.majalah_icon));
        categories.add(new Category("Keuangan", R.drawable.keuangan_icon));
        categories.add(new Category("Self Improvment", R.drawable.self_improvement));
        return categories;
    }

    private void onCategoryClicked(Category category) {
        if ("Buku Gratis".equals(category.getName())) {
            showFreeCategoriesDialog();
        } else {
            Intent intent = new Intent(this, UploadAdPhotoActivity.class);
            intent.putExtra(UploadAdPhotoActivity.EXTRA_CATEGORY_NAME, category.getName());
            startActivity(intent);
        }
    }

    private void showFreeCategoriesDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_free_categories);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ImageView closeButton = dialog.findViewById(R.id.dialog_close_button);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        LinearLayout listContainer = dialog.findViewById(R.id.dialog_list_container);

        List<Category> freeCategories = getFreeCategories();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Category cat : freeCategories) {
            View itemView = inflater.inflate(R.layout.item_dialog_category, listContainer, false);
            ImageView icon = itemView.findViewById(R.id.dialog_item_icon);
            TextView name = itemView.findViewById(R.id.dialog_item_name);

            icon.setImageResource(cat.getIconResource());
            name.setText(cat.getName());

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(this, UploadAdPhotoActivity.class);
                intent.putExtra(UploadAdPhotoActivity.EXTRA_CATEGORY_NAME, cat.getName());
                startActivity(intent);
                dialog.dismiss();
            });

            listContainer.addView(itemView);
        }

        dialog.show();
    }

    private List<Category> getFreeCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Buku Pelajaran (Gratis)", R.drawable.pelajaran_icon));
        categories.add(new Category("Buku Novel (Gratis)", R.drawable.novel_icon));
        categories.add(new Category("Buku Komik (Gratis)", R.drawable.comic_icon));
        categories.add(new Category("Buku Cerita (Gratis)", R.drawable.buku_cerita_icon));
        categories.add(new Category("Kamus Bahasa (Gratis)", R.drawable.kamus_bahasa_icon));
        categories.add(new Category("Buku Anak (Gratis)", R.drawable.buku_anak_icon));
        categories.add(new Category("Majalah (Gratis)", R.drawable.majalah_icon));
        categories.add(new Category("Buku Keuangan (Gratis)", R.drawable.keuangan_icon));
        categories.add(new Category("Buku Self Improvment (Gratis)", R.drawable.self_improvement));
        return categories;
    }
}

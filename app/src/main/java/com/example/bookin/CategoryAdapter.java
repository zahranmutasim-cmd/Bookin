package com.example.bookin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categoryList;
    private final OnCategoryClickListener listener;
    private int selectedPosition = -1;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    // Constructor for when no click listener is needed
    public CategoryAdapter(List<Category> categoryList) {
        this(categoryList, null);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.name.setText(category.getName());
        holder.icon.setImageResource(category.getIconResource());

        // Highlight selected item
        if (position == selectedPosition) {
            holder.itemView.setSelected(true);
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.itemView.setSelected(false);
            holder.itemView.setAlpha(0.6f);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public String getSelectedCategory() {
        if (selectedPosition >= 0 && selectedPosition < categoryList.size()) {
            return categoryList.get(selectedPosition).getName();
        }
        return null;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.category_name);
            icon = itemView.findViewById(R.id.category_icon);
        }
    }
}

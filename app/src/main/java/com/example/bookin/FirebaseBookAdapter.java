package com.example.bookin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookin.models.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FirebaseBookAdapter extends RecyclerView.Adapter<FirebaseBookAdapter.BookViewHolder> {

    private static final String WISHLIST_PREFS = "wishlist_prefs";

    private List<Book> bookList;
    private Context context;
    private OnBookClickListener listener;
    private SharedPreferences sharedPreferences;
    private Set<String> wishlistIds;
    private String currentUserId;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public FirebaseBookAdapter(List<Book> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    public FirebaseBookAdapter(List<Book> bookList) {
        this(bookList, null);
    }

    public void updateBooks(List<Book> newBooks) {
        this.bookList = newBooks;
        notifyDataSetChanged();
    }

    private String getWishlistKey() {
        // Return user-specific wishlist key
        if (currentUserId != null && !currentUserId.isEmpty()) {
            return "wishlist_" + currentUserId;
        }
        return "wishlist_anonymous";
    }

    private void loadWishlist() {
        if (sharedPreferences == null)
            return;

        // Get current user ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user != null ? user.getUid() : null;

        // Load user-specific wishlist
        wishlistIds = new HashSet<>(sharedPreferences.getStringSet(getWishlistKey(), new HashSet<>()));
    }

    private void saveWishlist() {
        if (sharedPreferences == null)
            return;
        sharedPreferences.edit().putStringSet(getWishlistKey(), wishlistIds).apply();
    }

    private boolean isInWishlist(String bookId) {
        return wishlistIds != null && wishlistIds.contains(bookId);
    }

    private void toggleWishlist(String bookId) {
        if (wishlistIds == null)
            wishlistIds = new HashSet<>();

        if (wishlistIds.contains(bookId)) {
            wishlistIds.remove(bookId);
        } else {
            wishlistIds.add(bookId);
        }
        saveWishlist();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        sharedPreferences = context.getSharedPreferences(WISHLIST_PREFS, Context.MODE_PRIVATE);
        loadWishlist();
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        // Set title
        holder.title.setText(book.getTitle());

        // Set description
        holder.description.setText(book.getDescription());

        // Set price - format as currency or show "Gratis"
        if (book.getPrice() == 0) {
            holder.price.setText("Gratis");
        } else {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            String formattedPrice = formatter.format(book.getPrice());
            holder.price.setText(formattedPrice);
        }

        // Set location - show only city (first part)
        String fullLocation = book.getLocation();
        if (fullLocation != null && !fullLocation.isEmpty()) {
            String[] parts = fullLocation.split(",");
            holder.location.setText(parts[0].trim());
        } else {
            holder.location.setText("");
        }

        // Load cover image from URL using Glide
        if (book.getFrontImageUrl() != null && !book.getFrontImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getFrontImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.cover);
        } else {
            holder.cover.setImageResource(R.drawable.ic_launcher_background);
        }

        // Handle "Produk Terbaik" banner visibility
        if (book.isFeatured()) {
            holder.bestProductBanner.setVisibility(View.VISIBLE);
        } else {
            holder.bestProductBanner.setVisibility(View.GONE);
        }

        // Show/hide sold overlay and badge
        if (book.isSold()) {
            holder.soldOverlay.setVisibility(View.VISIBLE);
            holder.soldBadge.setVisibility(View.VISIBLE);
        } else {
            holder.soldOverlay.setVisibility(View.GONE);
            holder.soldBadge.setVisibility(View.GONE);
        }

        // Handle favorite/wishlist icon
        updateFavoriteIcon(holder.favoriteIcon, book.getId());

        // Handle favorite icon click - toggle wishlist
        holder.favoriteIcon.setOnClickListener(v -> {
            toggleWishlist(book.getId());
            updateFavoriteIcon(holder.favoriteIcon, book.getId());

            if (isInWishlist(book.getId())) {
                Toast.makeText(context, "Ditambahkan ke wishlist", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Dihapus dari wishlist", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle item click - navigate to BookDetailActivity
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            } else {
                // Default: open BookDetailActivity
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
                context.startActivity(intent);
            }
        });
    }

    private void updateFavoriteIcon(ImageView favoriteIcon, String bookId) {
        if (isInWishlist(bookId)) {
            favoriteIcon.setColorFilter(Color.YELLOW);
        } else {
            favoriteIcon.setColorFilter(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, price, location, soldBadge;
        ImageView cover, favoriteIcon;
        LinearLayout bestProductBanner;
        View soldOverlay;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.book_title);
            description = itemView.findViewById(R.id.book_description);
            price = itemView.findViewById(R.id.book_price);
            location = itemView.findViewById(R.id.book_location);
            cover = itemView.findViewById(R.id.book_cover);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
            bestProductBanner = itemView.findViewById(R.id.best_product_banner);
            soldOverlay = itemView.findViewById(R.id.sold_overlay);
            soldBadge = itemView.findViewById(R.id.sold_badge);
        }
    }
}

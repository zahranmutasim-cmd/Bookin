package com.example.bookin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookin.models.Book;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyAdsAdapter extends RecyclerView.Adapter<MyAdsAdapter.MyAdViewHolder> {

    private List<Book> bookList;
    private Context context;
    private OnAdActionListener listener;

    public interface OnAdActionListener {
        void onSoldStatusClick(Book book, int position);

        void onDeleteClick(Book book, int position);

        void onViewClick(Book book);
    }

    public MyAdsAdapter(List<Book> bookList, OnAdActionListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyAdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_ad, parent, false);
        return new MyAdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdViewHolder holder, int position) {
        Book book = bookList.get(position);

        // Set tanggal
        holder.tvDate.setText("dari : " + formatDate(book.getTimestamp()));

        // Set judul buku
        holder.tvTitle.setText(book.getTitle());

        // Set lokasi
        holder.tvLocation.setText(book.getLocation() != null ? book.getLocation() : "-");

        // Set harga
        if (book.getPrice() == 0) {
            holder.tvPrice.setText("Gratis");
        } else {
            holder.tvPrice.setText(formatPrice(book.getPrice()));
        }

        // Set favorite count
        int favCount = book.getFavoriteCount();
        if (favCount > 0) {
            holder.tvFavoriteCount.setText(favCount + " Disukai");
        } else {
            holder.tvFavoriteCount.setText("Disukai");
        }

        // Load gambar buku
        if (book.getFrontImageUrl() != null && !book.getFrontImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getFrontImageUrl())
                    .placeholder(R.drawable.bookin_logo_biru)
                    .into(holder.ivBookImage);
        } else {
            holder.ivBookImage.setImageResource(R.drawable.bookin_logo_biru);
        }

        // Set status tombol Terjual/Belum Terjual
        updateSoldButton(holder.btnSoldStatus, book.isSold());

        // Show/hide sold overlay and badge
        if (book.isSold()) {
            holder.soldOverlay.setVisibility(View.VISIBLE);
            holder.soldBadge.setVisibility(View.VISIBLE);
        } else {
            holder.soldOverlay.setVisibility(View.GONE);
            holder.soldBadge.setVisibility(View.GONE);
        }

        // Click listeners
        holder.btnSoldStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSoldStatusClick(book, holder.getAdapterPosition());
            }
        });

        holder.btnDeleteAd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(book, holder.getAdapterPosition());
            }
        });

        holder.btnViewAd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewClick(book);
            }
        });
    }

    private void updateSoldButton(MaterialButton button, boolean isSold) {
        if (isSold) {
            button.setText("TERJUAL");
            button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green_sold));
        } else {
            button.setText("BELUM TERJUAL");
            button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red_not_sold));
        }
    }

    public void updateSoldStatus(int position, boolean isSold) {
        if (position >= 0 && position < bookList.size()) {
            bookList.get(position).setSold(isSold);
            notifyItemChanged(position);
        }
    }

    public void removeAd(int position) {
        if (position >= 0 && position < bookList.size()) {
            bookList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, bookList.size());
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0)
            return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy", new Locale("id", "ID"));
        return sdf.format(new Date(timestamp));
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formatted = formatter.format(price);
        // Remove decimal places for whole numbers
        return formatted.replaceAll(",00", "").replaceAll("\\,00", "");
    }

    static class MyAdViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvLocation, tvPrice, tvFavoriteCount, soldBadge;
        ImageView ivBookImage;
        View soldOverlay;
        MaterialButton btnSoldStatus, btnDeleteAd, btnViewAd;

        MyAdViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_ad_date);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvLocation = itemView.findViewById(R.id.tv_book_location);
            tvPrice = itemView.findViewById(R.id.tv_book_price);
            tvFavoriteCount = itemView.findViewById(R.id.tv_favorite_count);
            ivBookImage = itemView.findViewById(R.id.iv_book_image);
            soldOverlay = itemView.findViewById(R.id.sold_overlay);
            soldBadge = itemView.findViewById(R.id.sold_badge);
            btnSoldStatus = itemView.findViewById(R.id.btn_sold_status);
            btnDeleteAd = itemView.findViewById(R.id.btn_delete_ad);
            btnViewAd = itemView.findViewById(R.id.btn_view_ad);
        }
    }
}

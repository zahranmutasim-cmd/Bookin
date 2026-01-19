package com.example.bookin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookin.models.Message;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_BOOK_SENT = 3;
    private static final int VIEW_TYPE_BOOK_RECEIVED = 4;

    private List<Message> messages;
    private String currentUserId;
    private Context context;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        boolean isSent = message.getSenderId().equals(currentUserId);
        boolean isBookInfo = Message.TYPE_BOOK_INFO.equals(message.getMessageType());
        
        if (isBookInfo) {
            return isSent ? VIEW_TYPE_BOOK_SENT : VIEW_TYPE_BOOK_RECEIVED;
        } else {
            return isSent ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        
        switch (viewType) {
            case VIEW_TYPE_SENT:
                return new SentMessageViewHolder(
                        inflater.inflate(R.layout.item_message_sent, parent, false));
            case VIEW_TYPE_RECEIVED:
                return new ReceivedMessageViewHolder(
                        inflater.inflate(R.layout.item_message_received, parent, false));
            case VIEW_TYPE_BOOK_SENT:
                return new BookSentMessageViewHolder(
                        inflater.inflate(R.layout.item_message_book_sent, parent, false));
            case VIEW_TYPE_BOOK_RECEIVED:
                return new BookReceivedMessageViewHolder(
                        inflater.inflate(R.layout.item_message_book_received, parent, false));
            default:
                return new SentMessageViewHolder(
                        inflater.inflate(R.layout.item_message_sent, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        String timeText = formatTime(message.getTimestamp());
        
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_SENT:
                SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
                sentHolder.messageText.setText(message.getMessage());
                sentHolder.messageTime.setText(timeText);
                break;
                
            case VIEW_TYPE_RECEIVED:
                ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
                receivedHolder.messageText.setText(message.getMessage());
                receivedHolder.messageTime.setText(timeText);
                break;
                
            case VIEW_TYPE_BOOK_SENT:
                BookSentMessageViewHolder bookSentHolder = (BookSentMessageViewHolder) holder;
                bookSentHolder.messageText.setText(message.getMessage());
                bookSentHolder.messageTime.setText(timeText);
                bookSentHolder.bookTitle.setText(message.getBookTitle());
                bookSentHolder.bookPrice.setText(formatPrice(message.getBookPrice()));
                if (message.getBookImage() != null && !message.getBookImage().isEmpty()) {
                    Glide.with(context)
                            .load(message.getBookImage())
                            .into(bookSentHolder.bookImage);
                }
                break;
                
            case VIEW_TYPE_BOOK_RECEIVED:
                BookReceivedMessageViewHolder bookReceivedHolder = (BookReceivedMessageViewHolder) holder;
                bookReceivedHolder.messageText.setText(message.getMessage());
                bookReceivedHolder.messageTime.setText(timeText);
                bookReceivedHolder.bookTitle.setText(message.getBookTitle());
                bookReceivedHolder.bookPrice.setText(formatPrice(message.getBookPrice()));
                if (message.getBookImage() != null && !message.getBookImage().isEmpty()) {
                    Glide.with(context)
                            .load(message.getBookImage())
                            .into(bookReceivedHolder.bookImage);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String formatPrice(long price) {
        if (price == 0) {
            return "Gratis";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price);
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
        }
    }

    static class BookSentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, bookTitle, bookPrice;
        ImageView bookImage;

        BookSentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookPrice = itemView.findViewById(R.id.book_price);
            bookImage = itemView.findViewById(R.id.book_image);
        }
    }

    static class BookReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, bookTitle, bookPrice;
        ImageView bookImage;

        BookReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookPrice = itemView.findViewById(R.id.book_price);
            bookImage = itemView.findViewById(R.id.book_image);
        }
    }
}

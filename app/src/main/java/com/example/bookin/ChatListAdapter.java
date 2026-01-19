package com.example.bookin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookin.models.ChatRoom;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<ChatRoom> chatRooms;
    private Context context;
    private OnChatClickListener listener;
    private OnMenuClickListener menuListener;

    public interface OnChatClickListener {
        void onChatClick(ChatRoom chatRoom);
    }

    public interface OnMenuClickListener {
        void onMenuClick(ChatRoom chatRoom, int position);
    }

    public ChatListAdapter(List<ChatRoom> chatRooms, OnChatClickListener listener) {
        this.chatRooms = chatRooms;
        this.listener = listener;
    }

    public void setOnMenuClickListener(OnMenuClickListener menuListener) {
        this.menuListener = menuListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        
        // Set user name
        holder.userName.setText(chatRoom.getOtherUserName());
        
        // Set book title
        if (chatRoom.getBookTitle() != null && !chatRoom.getBookTitle().isEmpty()) {
            holder.bookTitle.setText(chatRoom.getBookTitle());
            holder.bookTitle.setVisibility(View.VISIBLE);
        } else {
            holder.bookTitle.setVisibility(View.GONE);
        }
        
        // Set last message
        holder.lastMessage.setText(chatRoom.getLastMessage());
        
        // Set timestamp
        holder.timestamp.setText(formatTimestamp(chatRoom.getLastMessageTime()));
        
        // Load profile image
        if (chatRoom.getOtherUserImage() != null && !chatRoom.getOtherUserImage().isEmpty()) {
            Glide.with(context)
                    .load(chatRoom.getOtherUserImage())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.default_profile);
        }
        
        // Click listener for item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chatRoom);
            }
        });

        // Click listener for menu button (3 dots)
        holder.menuButton.setOnClickListener(v -> {
            if (menuListener != null) {
                menuListener.onMenuClick(chatRoom, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public void updateChatRooms(List<ChatRoom> newChatRooms) {
        this.chatRooms = newChatRooms;
        notifyDataSetChanged();
    }

    public void removeChatRoom(int position) {
        if (position >= 0 && position < chatRooms.size()) {
            chatRooms.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, chatRooms.size());
        }
    }

    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "";
        
        Calendar now = Calendar.getInstance();
        Calendar messageTime = Calendar.getInstance();
        messageTime.setTimeInMillis(timestamp);
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        
        // Check if same day
        if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
            return timeFormat.format(new Date(timestamp));
        }
        
        // Check if yesterday
        now.add(Calendar.DAY_OF_YEAR, -1);
        if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
            return "Kemarin";
        }
        
        // Otherwise show date
        return dateFormat.format(new Date(timestamp));
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName;
        TextView bookTitle;
        TextView lastMessage;
        TextView timestamp;
        ImageButton menuButton;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.chat_profile_image);
            userName = itemView.findViewById(R.id.chat_user_name);
            bookTitle = itemView.findViewById(R.id.chat_book_title);
            lastMessage = itemView.findViewById(R.id.chat_last_message);
            timestamp = itemView.findViewById(R.id.chat_timestamp);
            menuButton = itemView.findViewById(R.id.chat_menu_button);
        }
    }
}

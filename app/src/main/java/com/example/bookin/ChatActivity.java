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

import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.example.bookin.models.ChatRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends BaseActivity {

    private RecyclerView chatRecyclerView;
    private LinearLayout emptyStateContainer;
    private ChatListAdapter adapter;
    private List<ChatRoom> chatRooms;

    private DatabaseReference chatRoomsRef;
    private DatabaseReference userChatsRef;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;
    private ValueEventListener chatRoomsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setupBottomNavigationBar();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatRooms");
        userChatsRef = FirebaseDatabase.getInstance().getReference("userChats").child(currentUser.getUid());
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initializeViews();
        loadChatRooms();
    }

    private void initializeViews() {
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        emptyStateContainer = findViewById(R.id.empty_state_container);

        chatRooms = new ArrayList<>();
        adapter = new ChatListAdapter(chatRooms, this::openChatRoom);
        adapter.setOnMenuClickListener(this::showChatMenuBottomSheet);
        
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);
    }

    private void loadChatRooms() {
        chatRoomsListener = userChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRooms.clear();
                
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    showEmptyState();
                    return;
                }

                final long totalChats = snapshot.getChildrenCount();
                final long[] loadedChats = {0};

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String chatRoomId = chatSnapshot.getKey();
                    
                    chatRoomsRef.child(chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot roomSnapshot) {
                            ChatRoom chatRoom = roomSnapshot.getValue(ChatRoom.class);
                            if (chatRoom != null) {
                                chatRoom.setChatRoomId(chatRoomId);
                                
                                // Find the other participant
                                String otherUserId = null;
                                if (chatRoom.getParticipants() != null) {
                                    for (String participantId : chatRoom.getParticipants().keySet()) {
                                        if (!participantId.equals(currentUser.getUid())) {
                                            otherUserId = participantId;
                                            break;
                                        }
                                    }
                                }
                                
                                if (otherUserId != null) {
                                    chatRoom.setOtherUserId(otherUserId);
                                    loadOtherUserInfo(chatRoom, otherUserId, totalChats, loadedChats);
                                } else {
                                    loadedChats[0]++;
                                    checkLoadComplete(totalChats, loadedChats[0]);
                                }
                            } else {
                                loadedChats[0]++;
                                checkLoadComplete(totalChats, loadedChats[0]);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loadedChats[0]++;
                            checkLoadComplete(totalChats, loadedChats[0]);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showEmptyState();
            }
        });
    }

    private void loadOtherUserInfo(ChatRoom chatRoom, String userId, long totalChats, long[] loadedChats) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileImage = snapshot.child("profileImage").getValue(String.class);
                
                chatRoom.setOtherUserName(name != null ? name : "Pengguna");
                chatRoom.setOtherUserImage(profileImage);
                
                chatRooms.add(chatRoom);
                loadedChats[0]++;
                checkLoadComplete(totalChats, loadedChats[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                chatRoom.setOtherUserName("Pengguna");
                chatRooms.add(chatRoom);
                loadedChats[0]++;
                checkLoadComplete(totalChats, loadedChats[0]);
            }
        });
    }

    private void checkLoadComplete(long total, long loaded) {
        if (loaded >= total) {
            if (chatRooms.isEmpty()) {
                showEmptyState();
            } else {
                // Sort by last message time (newest first)
                Collections.sort(chatRooms, (a, b) -> 
                    Long.compare(b.getLastMessageTime(), a.getLastMessageTime()));
                
                hideEmptyState();
                adapter.updateChatRooms(chatRooms);
            }
        }
    }

    private void showEmptyState() {
        emptyStateContainer.setVisibility(View.VISIBLE);
        chatRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyStateContainer.setVisibility(View.GONE);
        chatRecyclerView.setVisibility(View.VISIBLE);
    }

    private void openChatRoom(ChatRoom chatRoom) {
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ROOM_ID, chatRoom.getChatRoomId());
        intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_ID, chatRoom.getOtherUserId());
        intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_NAME, chatRoom.getOtherUserName());
        intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_IMAGE, chatRoom.getOtherUserImage());
        intent.putExtra(ChatRoomActivity.EXTRA_BOOK_ID, chatRoom.getBookId());
        intent.putExtra(ChatRoomActivity.EXTRA_BOOK_TITLE, chatRoom.getBookTitle());
        intent.putExtra(ChatRoomActivity.EXTRA_BOOK_IMAGE, chatRoom.getBookImage());
        intent.putExtra(ChatRoomActivity.EXTRA_BOOK_PRICE, chatRoom.getBookPrice());
        startActivity(intent);
    }

    private void showChatMenuBottomSheet(ChatRoom chatRoom, int position) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_chat_menu, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Hapus Chat button
        bottomSheetView.findViewById(R.id.btn_delete_chat).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showDeleteConfirmation(chatRoom, position);
        });

        // Batalkan button
        bottomSheetView.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void showDeleteConfirmation(ChatRoom chatRoom, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Hapus Chat")
            .setMessage("Apakah Anda yakin ingin menghapus percakapan ini?")
            .setPositiveButton("Hapus", (dialog, which) -> {
                deleteChatRoom(chatRoom, position);
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void deleteChatRoom(ChatRoom chatRoom, int position) {
        String chatRoomId = chatRoom.getChatRoomId();
        String currentUserId = currentUser.getUid();
        String otherUserId = chatRoom.getOtherUserId();

        // Remove from userChats for current user
        userChatsRef.child(chatRoomId).removeValue();

        // Remove from userChats for other user
        if (otherUserId != null) {
            FirebaseDatabase.getInstance().getReference("userChats")
                .child(otherUserId)
                .child(chatRoomId)
                .removeValue();
        }

        // Remove the chat room itself
        chatRoomsRef.child(chatRoomId).removeValue()
            .addOnSuccessListener(aVoid -> {
                adapter.removeChatRoom(position);
                Toast.makeText(this, "Chat berhasil dihapus", Toast.LENGTH_SHORT).show();
                
                if (chatRooms.isEmpty()) {
                    showEmptyState();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal menghapus chat", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userChatsRef != null && chatRoomsListener != null) {
            userChatsRef.removeEventListener(chatRoomsListener);
        }
    }
}

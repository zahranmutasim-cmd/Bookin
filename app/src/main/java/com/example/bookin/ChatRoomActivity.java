package com.example.bookin;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookin.models.ChatRoom;
import com.example.bookin.models.Message;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomActivity extends AppCompatActivity {

    private static final String TAG = "ChatRoomActivity";

    public static final String EXTRA_CHAT_ROOM_ID = "extra_chat_room_id";
    public static final String EXTRA_OTHER_USER_ID = "extra_other_user_id";
    public static final String EXTRA_OTHER_USER_NAME = "extra_other_user_name";
    public static final String EXTRA_OTHER_USER_IMAGE = "extra_other_user_image";
    public static final String EXTRA_BOOK_ID = "extra_book_id";
    public static final String EXTRA_BOOK_TITLE = "extra_book_title";
    public static final String EXTRA_BOOK_IMAGE = "extra_book_image";
    public static final String EXTRA_BOOK_PRICE = "extra_book_price";
    public static final String EXTRA_INITIAL_MESSAGE = "extra_initial_message";
    public static final String EXTRA_IS_NEW_CHAT = "extra_is_new_chat";

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private MaterialButton sendButton;
    private TextView headerUserName, headerBookTitle;
    private CircleImageView headerProfileImage;
    private ImageView backButton;

    private MessageAdapter adapter;
    private List<Message> messages;

    private DatabaseReference chatRoomsRef;
    private DatabaseReference messagesRef;
    private DatabaseReference usersRef;
    private DatabaseReference userChatsRef;

    private FirebaseUser currentUser;
    private String chatRoomId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserImage;
    private String bookId;
    private String bookTitle;
    private String bookImage;
    private long bookPrice;
    private String currentUserName;
    private String currentUserImage;
    private boolean isUserInfoLoaded = false;
    private String pendingInitialMessage = null;
    private boolean pendingIncludeBookInfo = false;

    private ChildEventListener messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get intent extras
        chatRoomId = getIntent().getStringExtra(EXTRA_CHAT_ROOM_ID);
        otherUserId = getIntent().getStringExtra(EXTRA_OTHER_USER_ID);
        otherUserName = getIntent().getStringExtra(EXTRA_OTHER_USER_NAME);
        otherUserImage = getIntent().getStringExtra(EXTRA_OTHER_USER_IMAGE);
        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        bookTitle = getIntent().getStringExtra(EXTRA_BOOK_TITLE);
        bookImage = getIntent().getStringExtra(EXTRA_BOOK_IMAGE);
        bookPrice = getIntent().getLongExtra(EXTRA_BOOK_PRICE, 0);

        chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatRooms");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        userChatsRef = FirebaseDatabase.getInstance().getReference("userChats");

        initializeViews();
        setupListeners();
        
        // Load current user info first, then proceed with chat setup
        loadCurrentUserInfo();
    }

    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        headerUserName = findViewById(R.id.header_user_name);
        headerBookTitle = findViewById(R.id.header_book_title);
        headerProfileImage = findViewById(R.id.header_profile_image);
        backButton = findViewById(R.id.back_button);

        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, currentUser.getUid());
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(adapter);

        // Set header info
        headerUserName.setText(otherUserName != null ? otherUserName : "Pengguna");
        headerBookTitle.setText(bookTitle != null ? bookTitle : "");
        
        if (otherUserImage != null && !otherUserImage.isEmpty()) {
            Glide.with(this)
                    .load(otherUserImage)
                    .placeholder(R.drawable.default_profile)
                    .into(headerProfileImage);
        }
    }

    private void loadCurrentUserInfo() {
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserName = snapshot.child("name").getValue(String.class);
                currentUserImage = snapshot.child("profileImage").getValue(String.class);
                
                if (currentUserName == null || currentUserName.isEmpty()) {
                    currentUserName = currentUser.getDisplayName();
                }
                if (currentUserName == null || currentUserName.isEmpty()) {
                    currentUserName = "Pengguna";
                }
                
                isUserInfoLoaded = true;
                Log.d(TAG, "User info loaded: " + currentUserName);
                
                // Now proceed with chat setup
                proceedWithChatSetup();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load user info: " + error.getMessage());
                currentUserName = currentUser.getDisplayName();
                if (currentUserName == null || currentUserName.isEmpty()) {
                    currentUserName = "Pengguna";
                }
                isUserInfoLoaded = true;
                
                // Proceed anyway
                proceedWithChatSetup();
            }
        });
    }

    private void proceedWithChatSetup() {
        // Check if this is a new chat from BookDetailActivity
        boolean isNewChat = getIntent().getBooleanExtra(EXTRA_IS_NEW_CHAT, false);
        String initialMessage = getIntent().getStringExtra(EXTRA_INITIAL_MESSAGE);

        if (isNewChat && chatRoomId == null) {
            // Create new chat room
            createOrGetChatRoom(initialMessage);
        } else if (chatRoomId != null) {
            // Load existing messages
            messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatRoomId);
            loadMessages();
        } else {
            Log.e(TAG, "No chat room ID and not a new chat");
            Toast.makeText(this, "Error: Chat room tidak valid", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                if (messagesRef != null) {
                    sendMessage(messageText, false);
                    messageInput.setText("");
                } else {
                    Toast.makeText(this, "Mohon tunggu, sedang memuat...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createOrGetChatRoom(String initialMessage) {
        // Generate chat room ID based on both user IDs
        chatRoomId = ChatRoom.generateChatRoomId(currentUser.getUid(), otherUserId);
        Log.d(TAG, "Chat room ID: " + chatRoomId);
        
        chatRoomsRef.child(chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatRoomId);
                
                if (!snapshot.exists()) {
                    // Create new chat room
                    Log.d(TAG, "Creating new chat room");
                    
                    Map<String, Object> chatRoomData = new HashMap<>();
                    chatRoomData.put("chatRoomId", chatRoomId);
                    
                    Map<String, Boolean> participants = new HashMap<>();
                    participants.put(currentUser.getUid(), true);
                    participants.put(otherUserId, true);
                    chatRoomData.put("participants", participants);
                    
                    chatRoomData.put("bookId", bookId);
                    chatRoomData.put("bookTitle", bookTitle);
                    chatRoomData.put("bookImage", bookImage);
                    chatRoomData.put("bookPrice", bookPrice);
                    chatRoomData.put("lastMessage", "");
                    chatRoomData.put("lastMessageTime", System.currentTimeMillis());
                    chatRoomData.put("lastSenderId", currentUser.getUid());
                    
                    chatRoomsRef.child(chatRoomId).setValue(chatRoomData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Chat room created successfully");
                            
                            // Add to both users' chat lists
                            userChatsRef.child(currentUser.getUid()).child(chatRoomId).setValue(true);
                            userChatsRef.child(otherUserId).child(chatRoomId).setValue(true);
                            
                            // Start listening for messages
                            loadMessages();
                            
                            // Send initial message with book info if provided
                            if (initialMessage != null && !initialMessage.isEmpty()) {
                                sendMessage(initialMessage, true);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to create chat room: " + e.getMessage());
                            Toast.makeText(ChatRoomActivity.this, 
                                "Gagal membuat chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                } else {
                    // Chat room already exists
                    Log.d(TAG, "Chat room already exists");
                    loadMessages();
                    
                    // Send initial message with book info if provided
                    if (initialMessage != null && !initialMessage.isEmpty()) {
                        sendMessage(initialMessage, true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(ChatRoomActivity.this, 
                    "Gagal memuat chat: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        if (messagesRef == null) {
            Log.e(TAG, "messagesRef is null in loadMessages");
            return;
        }
        
        Log.d(TAG, "Loading messages from: " + messagesRef.toString());
        
        messagesListener = messagesRef.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    message.setMessageId(snapshot.getKey());
                    messages.add(message);
                    adapter.notifyItemInserted(messages.size() - 1);
                    messagesRecyclerView.scrollToPosition(messages.size() - 1);
                    Log.d(TAG, "Message loaded: " + message.getMessage());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load messages: " + error.getMessage());
            }
        });
    }

    private void sendMessage(String messageText, boolean includeBookInfo) {
        if (messagesRef == null) {
            Log.e(TAG, "Cannot send message: messagesRef is null");
            Toast.makeText(this, "Error: Chat belum siap", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = messagesRef.push().getKey();
        if (messageId == null) {
            Log.e(TAG, "Cannot send message: failed to generate message ID");
            return;
        }

        long timestamp = System.currentTimeMillis();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageId", messageId);
        messageData.put("senderId", currentUser.getUid());
        messageData.put("senderName", currentUserName);
        messageData.put("senderImage", currentUserImage);
        messageData.put("message", messageText);
        messageData.put("timestamp", timestamp);
        messageData.put("isRead", false);

        // Include book info for the first message
        if (includeBookInfo && bookId != null) {
            messageData.put("messageType", Message.TYPE_BOOK_INFO);
            messageData.put("bookId", bookId);
            messageData.put("bookTitle", bookTitle);
            messageData.put("bookImage", bookImage);
            messageData.put("bookPrice", bookPrice);
        } else {
            messageData.put("messageType", Message.TYPE_TEXT);
        }

        Log.d(TAG, "Sending message: " + messageText + " to " + messagesRef.child(messageId).toString());

        messagesRef.child(messageId).setValue(messageData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Message sent successfully");
                
                // Update chat room last message
                Map<String, Object> chatRoomUpdates = new HashMap<>();
                chatRoomUpdates.put("lastMessage", messageText);
                chatRoomUpdates.put("lastMessageTime", timestamp);
                chatRoomUpdates.put("lastSenderId", currentUser.getUid());
                
                chatRoomsRef.child(chatRoomId).updateChildren(chatRoomUpdates)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update chat room: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to send message: " + e.getMessage());
                Toast.makeText(ChatRoomActivity.this, 
                    "Gagal mengirim pesan: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
    }
}
